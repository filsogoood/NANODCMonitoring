package com.nanodatacenter.monitorwebview

import retrofit2.Response
import retrofit2.http.*

/**
 * NDP API 인터페이스
 */
interface NdpApiService {
    
    /**
     * 사용자 로그인
     */
    @POST("/api/users/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    /**
     * 사용자 데이터 조회 (GET)
     */
    @GET("/api/users/data")
    suspend fun getUserData(@Header("Authorization") token: String): Response<ApiResponse>
    
    /**
     * 사용자 데이터 조회 (POST) - 웹 프로젝트에서 사용하는 POST 방식
     */
    @POST("/api/users/data")
    suspend fun getUserDataPost(
        @Header("Authorization") token: String,
        @Body requestBody: Map<String, String> = emptyMap()
    ): Response<ApiResponse>
    
    /**
     * 노드 데이터 조회
     */
    @GET("/api/nodes/data")
    suspend fun getNodesData(@Header("Authorization") token: String): Response<ApiResponse>
}
