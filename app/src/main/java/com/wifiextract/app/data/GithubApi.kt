package com.wifiextract.app.data

import kotlinx.coroutines.CancellationException
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
    val normalizedUsername = username.trim()
    if (!normalizedUsername.matches(Regex("[A-Za-z0-9-]{1,39}"))) {
        return Result.failure(IllegalArgumentException("Invalid GitHub username"))
    }

    cache[normalizedUsername.lowercase()]?.let { return Result.success(it) }

    return withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL("https://api.github.com/users/$normalizedUsername")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5_000
                readTimeout = 5_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Mobile-Network-Analyzer")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("GitHub API returned HTTP $responseCode")
            }

            val json = connection.inputStream.bufferedReader().use { reader ->
                JSONObject(reader.readText())
            }
            val login = json.getString("login")
            val user = GithubUser(
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
            cache[normalizedUsername.lowercase()] = user
            Result.success(user)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            connection?.disconnect()
        }
    }
}


suspend fun fetchGithubContributors(owner: String, repository: String): Result<List<String>> {
    if (!owner.matches(Regex("[A-Za-z0-9-]{1,39}")) ||
        !repository.matches(Regex("[A-Za-z0-9_.-]{1,100}"))
    ) {
        return Result.failure(IllegalArgumentException("Invalid GitHub repository"))
    }

    return withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL("https://api.github.com/repos/$owner/$repository/contributors?per_page=20")
                .openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5_000
                readTimeout = 5_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Mobile-Network-Analyzer")
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("GitHub API returned HTTP $responseCode")
            }

            val json = org.json.JSONArray(connection.inputStream.bufferedReader().use { it.readText() })
            val usernames = buildList {
                for (index in 0 until json.length()) {
                    val login = json.optJSONObject(index)?.optString("login").orEmpty()
                    if (login.isNotBlank() && !login.endsWith("[bot]")) add(login)
                }
            }.distinct()

            Result.success(usernames)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        } finally {
            connection?.disconnect()
        }
    }
}
