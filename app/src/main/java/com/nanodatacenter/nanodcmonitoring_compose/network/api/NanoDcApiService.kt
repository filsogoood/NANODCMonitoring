package com.nanodatacenter.nanodcmonitoring_compose.network.api

import com.nanodatacenter.nanodcmonitoring_compose.network.model.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * NanoDC API 서비스 인터페이스
 */
interface NanoDcApiService {
    
    /**
     * 사용자 데이터 조회 API
     * @param nanodcId NanoDC ID
     * @return API 응답 데이터
     */
    @GET("/api/users/tlong/data")
    suspend fun getUserData(
        @Query("nanodc_id") nanodcId: String
    ): Response<ApiResponse>
}
