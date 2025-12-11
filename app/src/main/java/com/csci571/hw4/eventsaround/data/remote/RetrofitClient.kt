package com.csci571.hw4.eventsaround.data.remote

import com.csci571.hw4.eventsaround.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // 使用你的实际后端 URL
    private const val BASE_URL = "https://csci571-472400.wl.r.appspot.com/"

    // For local testing with emulator
    // private const val BASE_URL = "http://10.0.2.2:8080/"

    // For local testing with physical device
    // private const val BASE_URL = "http://YOUR_COMPUTER_IP:8080/"

    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null

    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Only show logs in debug builds
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun getRetrofit(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getApiService(): ApiService {
        if (apiService == null) {
            apiService = getRetrofit().create(ApiService::class.java)
        }
        return apiService!!
    }

    /**
     * Reset the retrofit instance (useful for testing or changing base URL)
     */
    fun reset() {
        retrofit = null
        apiService = null
    }
}