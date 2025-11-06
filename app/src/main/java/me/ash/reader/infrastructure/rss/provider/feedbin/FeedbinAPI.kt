package me.ash.reader.infrastructure.rss.provider.feedbin

import android.content.Context
import me.ash.reader.domain.data.SyncLogger
import me.ash.reader.infrastructure.net.RetryConfig
import me.ash.reader.infrastructure.net.withRetries
import me.ash.reader.infrastructure.rss.provider.ProviderAPI
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.executeAsync
import okio.IOException
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap

class FeedbinAPI
private constructor(
    context: Context,
    private val username: String,
    private val password: String,
    private val syncLogger: SyncLogger,
) : ProviderAPI(context) {

    private val credentials by lazy {
        Credentials.basic(username, password)
    }

    private val retryConfig = RetryConfig(
        onRetry = {
            Timber.e(it)
        }
    )

    suspend fun validCredentials(): Boolean {
        val response = try {
            client.newCall(
                Request.Builder()
                    .url("${BASE_URL}v2/authentication.json")
                    .header("Authorization", credentials)
                    .get()
                    .build()
            ).executeAsync()
        } catch (_: IOException) {
            return false
        }

        return response.code in 200..299
    }

    suspend fun subscriptions(): List<FeedbinDTO.Subscription> {
        return retryableGetRequest("v2/subscriptions.json")
    }

    suspend fun icons(): List<FeedbinDTO.Icon> {
        return retryableGetRequest("v2/icons.json")
    }

    suspend fun taggings(): List<FeedbinDTO.Tagging> {
        return retryableGetRequest("v2/taggings.json")
    }

    suspend fun savedSearches(): List<FeedbinDTO.SavedSearch> {
        return retryableGetRequest("v2/saved_searches.json")
    }

    suspend fun savedSearchEntries(savedSearchID: String): List<Long> {
        return retryableGetRequest("v2/saved_searches/$savedSearchID.json")
    }

    suspend fun unreadEntries(): List<Long> {
        return retryableGetRequest("v2/unread_entries.json")
    }

    suspend fun starredEntries(): List<Long> {
        return retryableGetRequest("v2/starred_entries.json")
    }

    suspend fun entries(
        since: String? = null,
        page: String? = null,
        ids: String? = null,
    ): List<FeedbinDTO.Entry> {
        val params = mutableListOf<Pair<String, String>>()
        since?.let { params.add("since" to it) }
        page?.let { params.add("page" to it) }
        ids?.let { params.add("ids" to it) }
        params.add("mode" to "extended")
        params.add("per_page" to "100")

        return retryableGetRequest("v2/entries.json", params)
    }

    private suspend inline fun <reified T> retryableGetRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        return withRetries(retryConfig) { getRequest<T>(query, params) }.getOrThrow()
    }

    private suspend inline fun <reified T> getRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        val urlBuilder = "$BASE_URL$query".toHttpUrl().newBuilder()
        params?.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }

        val response = client.newCall(
            Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", credentials)
                .get()
                .build()
        ).executeAsync()

        val body = response.body.string()
        when (response.code) {
            401 -> throw Exception("Unauthorized")
            !in 200..299 -> throw Exception("HTTP ${response.code}: $body")
        }

        return toDTOList(body)
    }

    private suspend inline fun <reified T> postRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
        form: okhttp3.RequestBody? = null,
    ): T {
        val urlBuilder = "$BASE_URL$query".toHttpUrl().newBuilder()
        params?.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }

        val response = client.newCall(
            Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", credentials)
                .post(form ?: okhttp3.RequestBody.create(null, ByteArray(0)))
                .build()
        ).executeAsync()

        val body = response.body.string()
        when (response.code) {
            401 -> throw Exception("Unauthorized")
            !in 200..299 -> throw Exception("HTTP ${response.code}: $body")
        }

        return toDTOList(body)
    }

    private suspend inline fun <reified T> deleteRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
    ): T {
        val urlBuilder = "$BASE_URL$query".toHttpUrl().newBuilder()
        params?.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }

        val response = client.newCall(
            Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", credentials)
                .delete()
                .build()
        ).executeAsync()

        val body = response.body.string()
        when (response.code) {
            401 -> throw Exception("Unauthorized")
            !in 200..299 -> throw Exception("HTTP ${response.code}: $body")
        }

        return toDTOList(body)
    }

    private suspend inline fun <reified T> patchRequest(
        query: String,
        params: List<Pair<String, String>>? = null,
        form: okhttp3.RequestBody? = null,
    ): T {
        val urlBuilder = "$BASE_URL$query".toHttpUrl().newBuilder()
        params?.forEach { (key, value) ->
            urlBuilder.addQueryParameter(key, value)
        }

        val response = client.newCall(
            Request.Builder()
                .url(urlBuilder.build())
                .addHeader("Authorization", credentials)
                .patch(form ?: run {
                    val content = ByteArray(0)
                    content.toRequestBody(null, 0, content.size)
                })
                .build()
        ).executeAsync()

        val body = response.body.string()
        when (response.code) {
            401 -> throw Exception("Unauthorized")
            !in 200..299 -> throw Exception("HTTP ${response.code}: $body")
        }

        return toDTOList(body)
    }

    companion object {
        private const val BASE_URL = "https://api.feedbin.com/"
        private val instances: ConcurrentHashMap<String, FeedbinAPI> = ConcurrentHashMap()

        fun getInstance(
            context: Context,
            username: String,
            password: String,
            syncLogger: SyncLogger,
        ): FeedbinAPI {
            val key = username
            return instances.getOrPut(key) {
                FeedbinAPI(
                    context,
                    username,
                    password,
                    syncLogger,
                )
            }
        }

        fun clearInstance() {
            instances.clear()
        }
    }
}
