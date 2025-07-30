package com.nanodatacenter.nanodcmonitoring_compose.network.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.nanodatacenter.nanodcmonitoring_compose.config.ApiConfiguration
import kotlinx.coroutines.delay
import retrofit2.Response

/**
 * 네트워크 관련 유틸리티 관리 클래스
 * 확장성을 고려하여 네트워크 상태 확인, 재시도 로직 등을 제공
 */
object NetworkManager {
    
    private const val TAG = "NetworkManager"
    
    /**
     * 네트워크 연결 상태 확인
     * @param context Application Context
     * @return 네트워크 연결 가능 여부
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return try {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability: ${e.message}")
            false
        }
    }
    
    /**
     * API 호출 재시도 로직
     * @param apiCall API 호출 함수
     * @param maxRetries 최대 재시도 횟수
     * @param delayMs 재시도 간격 (밀리초)
     * @return API 응답 또는 null
     */
    suspend fun <T> retryApiCall(
        apiCall: suspend () -> Response<T>,
        maxRetries: Int = ApiConfiguration.Network.RETRY_COUNT,
        delayMs: Long = ApiConfiguration.Network.RETRY_DELAY_MS
    ): Response<T>? {
        repeat(maxRetries) { attempt ->
            try {
                val response = apiCall()
                if (response.isSuccessful) {
                    return response
                } else {
                    Log.w(TAG, "API call failed (attempt ${attempt + 1}/$maxRetries): ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "API call exception (attempt ${attempt + 1}/$maxRetries): ${e.message}")
            }
            
            if (attempt < maxRetries - 1) {
                delay(delayMs)
            }
        }
        
        Log.e(TAG, "All $maxRetries API call attempts failed")
        return null
    }
    
    /**
     * 네트워크 상태에 따른 API 호출
     * 네트워크가 연결되지 않은 경우 즉시 실패 처리
     * @param context Application Context
     * @param apiCall API 호출 함수
     * @return API 응답 또는 null
     */
    suspend fun <T> executeWithNetworkCheck(
        context: Context,
        apiCall: suspend () -> Response<T>?
    ): Response<T>? {
        if (!isNetworkAvailable(context)) {
            Log.e(TAG, "Network is not available. Skipping API call.")
            return null
        }
        
        return apiCall()
    }
    
    /**
     * HTTP 상태 코드에 따른 에러 메시지 반환
     * @param statusCode HTTP 상태 코드
     * @return 사용자 친화적인 에러 메시지
     */
    fun getErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Bad Request - Invalid parameters"
            401 -> "Unauthorized - Authentication required"
            403 -> "Forbidden - Access denied"
            404 -> "Not Found - Resource not available"
            408 -> "Request Timeout - Please try again"
            429 -> "Too Many Requests - Please wait and try again"
            500 -> "Internal Server Error - Server issue"
            502 -> "Bad Gateway - Server communication error"
            503 -> "Service Unavailable - Server temporarily unavailable"
            504 -> "Gateway Timeout - Server response timeout"
            else -> "Network Error (Code: $statusCode)"
        }
    }
    
    /**
     * API 응답 로깅 유틸리티
     * 개발 환경에서만 상세 로그 출력
     * @param tag 로그 태그
     * @param url 요청 URL
     * @param response API 응답
     */
    fun <T> logApiResponse(tag: String, url: String, response: Response<T>?) {
        if (response == null) {
            Log.e(tag, "❌ API Response is null for URL: $url")
            return
        }
        
        val statusCode = response.code()
        val isSuccessful = response.isSuccessful
        
        if (isSuccessful) {
            Log.d(tag, "✅ API Success - URL: $url, Status: $statusCode")
        } else {
            Log.e(tag, "❌ API Failed - URL: $url, Status: $statusCode, Message: ${getErrorMessage(statusCode)}")
            response.errorBody()?.let { errorBody ->
                Log.e(tag, "Error Body: ${errorBody.string()}")
            }
        }
    }
    
    /**
     * 네트워크 연결 타입 반환
     * @param context Application Context
     * @return 연결 타입 문자열
     */
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return try {
            val network = connectivityManager.activeNetwork ?: return "No Connection"
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"
            
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network type: ${e.message}")
            "Unknown"
        }
    }
}