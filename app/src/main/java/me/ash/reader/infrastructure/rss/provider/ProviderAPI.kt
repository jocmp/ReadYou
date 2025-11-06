package me.ash.reader.infrastructure.rss.provider

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.ash.reader.infrastructure.di.UserAgentInterceptor
import me.ash.reader.infrastructure.di.cachingHttpClient
import okhttp3.OkHttpClient
import java.lang.reflect.Type

abstract class ProviderAPI(context: Context, clientCertificateAlias: String? = null) {

    protected val client: OkHttpClient = cachingHttpClient(
        context = context,
        clientCertificateAlias = clientCertificateAlias,
    )
        .newBuilder()
        .addNetworkInterceptor(UserAgentInterceptor)
        .build()

    protected val gson: Gson = GsonBuilder().create()

    protected inline fun <reified T> toDTO(jsonStr: String): T =
        gson.fromJson(jsonStr, T::class.java)!!

    protected inline fun <reified T> toDTOList(jsonStr: String): T {
        val type: Type = object : TypeToken<T>() {}.type
        return gson.fromJson(jsonStr, type)!!
    }
}
