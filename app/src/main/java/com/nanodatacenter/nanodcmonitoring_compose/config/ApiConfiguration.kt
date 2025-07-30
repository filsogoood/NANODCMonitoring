package com.nanodatacenter.nanodcmonitoring_compose.config

/**
 * API 설정 관리 클래스
 * 확장성을 고려하여 API 관련 설정들을 중앙에서 관리
 */
object ApiConfiguration {
    
    // Base URL Configuration
    const val BASE_URL = "http://211.176.180.172:8080"
    
    // API Endpoints
    object Endpoints {
        const val USER_DATA = "/api/users/tlong/data"
        const val SCORES = "/api/scores"
        const val SCORES_BY_NANODC = "/api/scores/nanodc"
    }
    
    // Query Parameters
    object QueryParams {
        const val NANODC_ID = "nanodc_id"
        const val NODE_ID = "node_id"
    }
    
    // Default Values
    object Defaults {
        /**
         * 기본 NanoDC ID
         * 요청된 새로운 ID로 설정
         */
        const val NANODC_ID = "c236ea9c-3d7e-430b-98b8-1e22d0d6cf01"
        
        /**
         * API 타임아웃 설정 (초)
         */
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
        
        /**
         * 기본 스코어 값 (API 실패 시 사용)
         */
        const val DEFAULT_SCORE = "80.00"
        const val DEFAULT_TOTAL_SCORE = "480.00"
    }
    
    // Network Configuration
    object Network {
        const val RETRY_COUNT = 3
        const val RETRY_DELAY_MS = 1000L
        const val CACHE_SIZE = 10 * 1024 * 1024L // 10MB
    }
    
    /**
     * 환경별 설정을 위한 확장 가능한 구조
     * 추후 개발/운영 환경 분리 시 사용
     */
    enum class Environment {
        DEVELOPMENT,
        PRODUCTION,
        STAGING
    }
    
    /**
     * 현재 환경 설정
     * 빌드 변형에 따라 변경 가능
     */
    val currentEnvironment: Environment = Environment.PRODUCTION
    
    /**
     * 환경별 Base URL 반환
     * 현재는 단일 환경이지만 향후 확장 가능
     */
    fun getBaseUrlForEnvironment(environment: Environment = currentEnvironment): String {
        return when (environment) {
            Environment.DEVELOPMENT -> "http://dev.api.example.com:8080"
            Environment.STAGING -> "http://staging.api.example.com:8080"
            Environment.PRODUCTION -> BASE_URL
        }
    }
}