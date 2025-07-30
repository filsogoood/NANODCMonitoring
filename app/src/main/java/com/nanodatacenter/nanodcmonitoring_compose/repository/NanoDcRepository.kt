package com.nanodatacenter.nanodcmonitoring_compose.repository

import android.util.Log
import com.nanodatacenter.nanodcmonitoring_compose.network.api.NanoDcApiService
import com.nanodatacenter.nanodcmonitoring_compose.network.client.RetrofitClient
import com.nanodatacenter.nanodcmonitoring_compose.network.model.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * NanoDC 데이터 레포지토리
 * API 호출을 관리하고 데이터를 처리하는 클래스
 */
class NanoDcRepository {
    
    private val apiService: NanoDcApiService = RetrofitClient.nanoDcApiService
    
    companion object {
        private const val TAG = "NanoDcRepository"
    }
    
    /**
     * 사용자 데이터 조회
     * @param nanodcId NanoDC ID
     * @return ApiResponse 또는 null (실패 시)
     */
    suspend fun getUserData(nanodcId: String): ApiResponse? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching user data for nanodcId: $nanodcId")
                
                val response = apiService.getUserData(nanodcId)
                
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d(TAG, "API call successful")
                    Log.d(TAG, "Hardware specs count: ${data?.hardwareSpecs?.size}")
                    Log.d(TAG, "Nodes count: ${data?.nodes?.size}")
                    Log.d(TAG, "Scores count: ${data?.scores?.size}")
                    Log.d(TAG, "NDP transactions count: ${data?.ndpListFiltered?.size}")
                    Log.d(TAG, "NanoDC count: ${data?.nanodc?.size}")
                    Log.d(TAG, "Node usage count: ${data?.nodeUsage?.size}")
                    data
                } else {
                    Log.e(TAG, "API call failed with code: ${response.code()}")
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during API call: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * API 연결 테스트
     * 로그를 통해 API 응답 상태를 확인
     */
    suspend fun testApiConnection(nanodcId: String) {
        Log.d(TAG, "==================== API Connection Test Start ====================")
        Log.d(TAG, "Target URL: http://211.176.180.172:8080/api/users/tlong/data?nanodc_id=$nanodcId")
        
        val result = getUserData(nanodcId)
        
        if (result != null) {
            Log.d(TAG, "✅ API Connection Test SUCCESSFUL")
            logDetailedApiResponse(result)
        } else {
            Log.e(TAG, "❌ API Connection Test FAILED")
        }
        
        Log.d(TAG, "==================== API Connection Test End ====================")
    }
    
    /**
     * API 응답 상세 로그 출력
     */
    private fun logDetailedApiResponse(response: ApiResponse) {
        Log.d(TAG, "📊 Detailed API Response:")
        
        // Hardware Specs 로그
        response.hardwareSpecs.forEach { spec ->
            Log.d(TAG, "🖥️ Hardware - Node: ${spec.nodeId}")
            Log.d(TAG, "   CPU: ${spec.cpuModel} (${spec.cpuCores} cores)")
            Log.d(TAG, "   GPU: ${spec.gpuModel} (${spec.gpuVramGb}GB VRAM)")
            Log.d(TAG, "   RAM: ${spec.totalRamGb}GB")
            Log.d(TAG, "   Storage: ${spec.storageType} ${spec.storageTotalGb}GB")
        }
        
        // Nodes 로그
        response.nodes.forEach { node ->
            Log.d(TAG, "🔗 Node - ID: ${node.nodeId}")
            Log.d(TAG, "   Name: ${node.nodeName}")
            Log.d(TAG, "   Status: ${node.status}")
            Log.d(TAG, "   Updated: ${node.updateAt}")
        }
        
        // Scores 로그
        response.scores.forEach { score ->
            Log.d(TAG, "📈 Score - Node: ${score.nodeId}")
            Log.d(TAG, "   CPU: ${score.cpuScore}, GPU: ${score.gpuScore}")
            Log.d(TAG, "   SSD: ${score.ssdScore}, RAM: ${score.ramScore}")
            Log.d(TAG, "   Network: ${score.networkScore}")
            Log.d(TAG, "   Total: ${score.totalScore} (Avg: ${score.averageScore})")
        }
        
        // NanoDC 로그
        response.nanodc.forEach { nanodc ->
            Log.d(TAG, "🏢 NanoDC - Name: ${nanodc.name}")
            Log.d(TAG, "   Location: ${nanodc.address}, ${nanodc.country}")
            Log.d(TAG, "   IP: ${nanodc.ip}")
            Log.d(TAG, "   Coordinates: ${nanodc.latitude}, ${nanodc.longitude}")
        }
        
        // Node Usage 로그
        response.nodeUsage.forEach { usage ->
            Log.d(TAG, "📊 Usage - Node: ${usage.nodeId}")
            Log.d(TAG, "   CPU: ${usage.cpuUsagePercent}%, Memory: ${usage.memUsagePercent}%")
            Log.d(TAG, "   GPU: ${usage.gpuUsagePercent}% (Temp: ${usage.gpuTemp}°C)")
            Log.d(TAG, "   Storage: ${usage.usedStorageGb}GB used")
            Log.d(TAG, "   SSD Health: ${usage.ssdHealthPercent}%")
            Log.d(TAG, "   Timestamp: ${usage.timestamp}")
        }
        
        // NDP Transactions 로그
        response.ndpListFiltered.forEach { transaction ->
            Log.d(TAG, "💰 Transaction - Node: ${transaction.nodeId}")
            Log.d(TAG, "   Amount: ${transaction.amount}")
            Log.d(TAG, "   From: ${transaction.from}")
            Log.d(TAG, "   To: ${transaction.to}")
            Log.d(TAG, "   Hash: ${transaction.txHash}")
            Log.d(TAG, "   Date: ${transaction.date}")
        }
    }
}
