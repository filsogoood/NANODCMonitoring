package com.nanodatacenter.nanodcmonitoring_compose.network.api

import com.nanodatacenter.nanodcmonitoring_compose.network.model.ApiResponse
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
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
    
    /**
     * 스코어 데이터 조회 API
     * @param nodeId 노드 ID
     * @return 스코어 데이터
     */
    @GET("/api/scores")
    suspend fun getScore(
        @Query("node_id") nodeId: String
    ): Response<Score>
    
    /**
     * 특정 NanoDC의 스코어 데이터 조회 API
     * @param nanodcId NanoDC ID
     * @return 스코어 데이터
     */
    @GET("/api/scores/nanodc")
    suspend fun getScoreByNanoDcId(
        @Query("nanodc_id") nanodcId: String
    ): Response<Score>
    
    /**
     * 특정 노드의 NDP 트랜잭션 목록 조회 API
     * @param nodeId 노드 ID
     * @return NDP 트랜잭션 목록
     */
    @GET("/api/ndp/transactions")
    suspend fun getNdpTransactions(
        @Query("node_id") nodeId: String
    ): Response<List<com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction>>
    
    /**
     * 모든 NDP 트랜잭션 목록 조회 API
     * @param nanodcId NanoDC ID
     * @return 모든 NDP 트랜잭션 목록
     */
    @GET("/api/ndp/transactions/all")
    suspend fun getAllNdpTransactions(
        @Query("nanodc_id") nanodcId: String
    ): Response<List<com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction>>
}
