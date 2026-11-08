package com.wifiextract.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

data class GithubUser(
    val login: String,
    val name: String,
    val avatarUrl: String,
    val htmlUrl: String,
    val bio: String?,
    val location: String?,
    val company: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int
)

private val cache = ConcurrentHashMap<String, GithubUser>()

private fun JSONObject.stringOrNull(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

suspend fun fetchGithubUser(username: String): Result<GithubUser> {
    cache[username]?.let { return Result.success(it) }
    return withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL("https://api.github.com/users/$username").openConnection() as HttpURLConnection
            try {
                conn.setRequestProperty("Accept", "application/vnd.github+json")
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                if (conn.responseCode != 200) error("HTTP ${conn.responseCode}")

                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val login = json.getString("login")
                GithubUser(
                    login = login,
                    name = json.stringOrNull("name") ?: login,
                    avatarUrl = json.getString("avatar_url"),
                    htmlUrl = json.getString("html_url"),
                    bio = json.stringOrNull("bio"),
                    location = json.stringOrNull("location"),
                    company = json.stringOrNull("company"),
                    publicRepos = json.optInt("public_repos"),
                    followers = json.optInt("followers"),
                    following = json.optInt("following")
                )
            } finally {
                conn.disconnect()
            }
        }.onSuccess { cache[username] = it }
    }
}
