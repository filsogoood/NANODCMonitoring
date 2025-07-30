package com.nanodatacenter.nanodcmonitoring_compose.network.client

import com.nanodatacenter.nanodcmonitoring_compose.network.api.NanoDcApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 클라이언트 관리 객체
 */
object RetrofitClient {
    
    private const val BASE_URL = "http://211.176.180.172:8080"
    
    /**
     * HTTP 로깅 인터셉터 생성
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * OkHttpClient 인스턴스
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * Retrofit 인스턴스
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /**
     * NanoDC API 서비스 인스턴스
     */
    val nanoDcApiService: NanoDcApiService = retrofit.create(NanoDcApiService::class.java)
}
