package com.example.e_learning_platform.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    private var cachedBaseUrl: String? = null
    private var cachedRetrofit: Retrofit? = null

    fun getRetrofit(): Retrofit {
        val currentBaseUrl = ApiConfig.baseUrl
        val existing = cachedRetrofit
        if (existing != null && cachedBaseUrl == currentBaseUrl) {
            return existing
        }

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        cachedBaseUrl = currentBaseUrl
        cachedRetrofit = retrofit
        return retrofit
    }

    inline fun <reified T> createService(): T {
        return getRetrofit().create(T::class.java)
    }
}
