package com.wifiextract.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

data class InstalledVersion(val name: String, val code: Long)

data class ReleaseInfo(
    val tag: String,
    val title: String,
    val notes: String,
    val pageUrl: String,
    val apkName: String,
    val apkUrl: String,
    val apkSize: Long,
    val shaUrl: String
)

/**
 * Official-release updater. It only talks to the project's GitHub Releases, downloads the APK to the
 * app cache, verifies the published SHA-256 and the APK signing certificate against the installed app,
 * and never installs silently: the system installer always asks for confirmation.
 */
class UpdateRepository(context: Context) {
    private val context = context.applicationContext

    @Suppress("DEPRECATION")
    fun installedVersion(): InstalledVersion {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return InstalledVersion(info.versionName.orEmpty(), versionCode(info))
    }

    suspend fun fetchLatest(): ReleaseInfo = withContext(Dispatchers.IO) {
        val connection = open(API_URL, "application/vnd.github+json")
        try {
            if (connection.responseCode != 200) throw IOException("HTTP ${connection.responseCode}")
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val assets = json.getJSONArray("assets")
            var apk: JSONObject? = null
            var sha: JSONObject? = null
            for (i in 0 until assets.length()) {
                val asset = assets.getJSONObject(i)
                val name = asset.getString("name")
                if (name.endsWith("-android.apk")) apk = asset
                if (name.endsWith("-android.apk.sha256")) sha = asset
            }
            if (apk == null || sha == null) throw IOException("La versión no incluye APK y checksum verificables")
            val apkUrl = apk.getString("browser_download_url")
            val shaUrl = sha.getString("browser_download_url")
            if (!apkUrl.startsWith(ASSET_PREFIX) || !shaUrl.startsWith(ASSET_PREFIX)) {
                throw SecurityException("Origen de descarga no oficial")
            }
            ReleaseInfo(
                tag = json.getString("tag_name"),
                title = json.optString("name").ifBlank { json.getString("tag_name") },
                notes = json.optString("body"),
                pageUrl = json.optString("html_url"),
                apkName = apk.getString("name"),
                apkUrl = apkUrl,
                apkSize = apk.optLong("size", -1),
                shaUrl = shaUrl
            )
        } finally {
            connection.disconnect()
        }
    }

    /** Downloads, hashes and verifies the APK. Throws [SecurityException] if any check fails. */
    suspend fun download(info: ReleaseInfo, onProgress: (Int?) -> Unit): File = withContext(Dispatchers.IO) {
        val expected = fetchExpectedHash(info.shaUrl)
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val target = File(dir, info.apkName.replace(Regex("[^A-Za-z0-9._-]"), "_"))
        val digest = MessageDigest.getInstance("SHA-256")
        val connection = open(info.apkUrl)
        try {
            if (connection.responseCode != 200) throw IOException("HTTP ${connection.responseCode}")
            val total = connection.contentLengthLong
            if (total > MAX_APK_BYTES) throw SecurityException("El archivo supera el tamaño permitido")
            connection.inputStream.use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(16 * 1024)
                    var sum = 0L
                    var last = -1
                    while (true) {
                        ensureActive()
                        val read = input.read(buffer)
                        if (read == -1) break
                        sum += read
                        if (sum > MAX_APK_BYTES) throw SecurityException("El archivo supera el tamaño permitido")
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                        val pct = if (total > 0) (sum * 100 / total).toInt() else null
                        if (pct != last) {
                            last = pct ?: -1
                            onProgress(pct)
                        }
                    }
                }
            }
            val actual = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actual.equals(expected, ignoreCase = true)) throw SecurityException("el SHA-256 no coincide")
            verifyPackage(target)
            target
        } catch (e: Throwable) {
            target.delete()
            throw e
        } finally {
            connection.disconnect()
        }
    }

    fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    fun permissionIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun installIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun fetchExpectedHash(url: String): String {
        val connection = open(url)
        try {
            if (connection.responseCode != 200) throw IOException("HTTP ${connection.responseCode}")
            val text = connection.inputStream.use { it.readBytes().take(4096).toByteArray().decodeToString() }
            return Regex("\\b[A-Fa-f0-9]{64}\\b").find(text)?.value
                ?: throw SecurityException("el checksum publicado no es válido")
        } finally {
            connection.disconnect()
        }
    }

    @Suppress("DEPRECATION")
    private fun verifyPackage(file: File) {
        val pm = context.packageManager
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val archive = pm.getPackageArchiveInfo(file.absolutePath, flags)
            ?: throw SecurityException("el APK no es válido")
        if (archive.packageName != context.packageName) throw SecurityException("el APK pertenece a otra aplicación")
        val installed = pm.getPackageInfo(context.packageName, flags)
        val archiveSigners = signers(archive)
        if (archiveSigners.isEmpty() || archiveSigners != signers(installed)) {
            throw SecurityException("la firma del APK no coincide con la app instalada")
        }
        if (versionCode(archive) <= versionCode(installed)) throw SecurityException("el APK no es más reciente")
    }

    @Suppress("DEPRECATION")
    private fun signers(info: PackageInfo): Set<String> {
        val signatures: Array<Signature>? =
            if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
        return signatures.orEmpty().map { sha256(it.toByteArray()) }.toSet()
    }

    @Suppress("DEPRECATION")
    private fun versionCode(info: PackageInfo): Long =
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode else info.versionCode.toLong()

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun open(url: String, accept: String? = null): HttpURLConnection {
        require(url.startsWith("https://")) { "Solo HTTPS" }
        return (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "MobileNetworkAnalyzer")
            accept?.let { setRequestProperty("Accept", it) }
        }
    }

    companion object {
        private const val REPO = "K3NYEL/wifi-extract"
        private const val API_URL = "https://api.github.com/repos/$REPO/releases/latest"
        private const val ASSET_PREFIX = "https://github.com/$REPO/releases/download/"
        private const val MAX_APK_BYTES = 150L * 1024 * 1024
    }
}
