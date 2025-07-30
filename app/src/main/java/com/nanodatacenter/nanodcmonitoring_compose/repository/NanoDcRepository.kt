package com.nanodatacenter.nanodcmonitoring_compose.repository

import android.util.Log
import com.nanodatacenter.nanodcmonitoring_compose.network.api.NanoDcApiService
import com.nanodatacenter.nanodcmonitoring_compose.network.client.RetrofitClient
import com.nanodatacenter.nanodcmonitoring_compose.network.model.ApiResponse
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
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
        private const val DEFAULT_NANODC_ID = "c236ea9c-3d7e-430b-98b8-1e22d0d6cf01"
    }
    
    /**
     * 스코어 데이터 조회 (노드 ID로)
     * @param nodeId 노드 ID
     * @return Score 또는 null (실패 시)
     */
    suspend fun getScore(nodeId: String): Score? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching score data for nodeId: $nodeId")
                
                val response = apiService.getScore(nodeId)
                
                if (response.isSuccessful) {
                    val score = response.body()
                    Log.d(TAG, "Score API call successful")
                    if (score != null) {
                        Log.d(TAG, "Score data - Node: ${score.nodeId}")
                        Log.d(TAG, "   CPU: ${score.cpuScore}, GPU: ${score.gpuScore}")
                        Log.d(TAG, "   RAM: ${score.ramScore}, SSD: ${score.ssdScore}")
                        Log.d(TAG, "   Network: ${score.networkScore}, Health: ${score.hardwareHealthScore}")
                        Log.d(TAG, "   Total: ${score.totalScore}, Average: ${score.averageScore}")
                    }
                    score
                } else {
                    Log.e(TAG, "Score API call failed with code: ${response.code()}")
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during score API call: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * 스코어 데이터 조회 (NanoDC ID로)
     * @param nanodcId NanoDC ID (기본값 사용 가능)
     * @return Score 또는 null (실패 시)
     */
    suspend fun getScoreByNanoDcId(nanodcId: String = DEFAULT_NANODC_ID): Score? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching score data for nanodcId: $nanodcId")
                
                val response = apiService.getScoreByNanoDcId(nanodcId)
                
                if (response.isSuccessful) {
                    val score = response.body()
                    Log.d(TAG, "Score by NanoDC API call successful")
                    if (score != null) {
                        Log.d(TAG, "Score data from NanoDC - Node: ${score.nodeId}")
                        Log.d(TAG, "   CPU: ${score.cpuScore}, GPU: ${score.gpuScore}")
                        Log.d(TAG, "   RAM: ${score.ramScore}, SSD: ${score.ssdScore}")
                        Log.d(TAG, "   Network: ${score.networkScore}, Health: ${score.hardwareHealthScore}")
                        Log.d(TAG, "   Total: ${score.totalScore}, Average: ${score.averageScore}")
                    }
                    score
                } else {
                    Log.e(TAG, "Score by NanoDC API call failed with code: ${response.code()}")
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during score by NanoDC API call: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * 첫 번째 이미지용 스코어 데이터 조회
     * 기존 API에서 스코어 데이터를 추출하거나, 전용 API를 호출
     * @param nanodcId NanoDC ID (기본값 사용 가능)
     * @return Score 또는 null (실패 시)
     */
    suspend fun getScoreForFirstImage(nanodcId: String = DEFAULT_NANODC_ID): Score? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching score data for first image display with nanodcId: $nanodcId")
                
                // 1차 시도: NanoDC ID로 스코어 조회
                var score = getScoreByNanoDcId(nanodcId)
                
                // 2차 시도: 기존 API에서 첫 번째 스코어 데이터 가져오기
                if (score == null) {
                    Log.d(TAG, "NanoDC score API failed, trying getUserData API")
                    val apiResponse = getUserData(nanodcId)
                    if (apiResponse != null && apiResponse.scores.isNotEmpty()) {
                        score = apiResponse.scores.first()
                        Log.d(TAG, "Using first score from getUserData API")
                    }
                }
                
                // 3차 시도: 실패 시 기본값 생성 (모든 값 80점)
                if (score == null) {
                    Log.w(TAG, "All score API calls failed, creating default score with 80 points")
                    score = createDefaultScore()
                }
                
                score
            } catch (e: Exception) {
                Log.e(TAG, "Exception during first image score retrieval: ${e.message}", e)
                // 예외 발생 시에도 기본값 반환
                createDefaultScore()
            }
        }
    }
    
    /**
     * 기본 스코어 데이터 생성 (모든 값 80점)
     * API 호출이 실패했을 때 사용
     */
    private fun createDefaultScore(): Score {
        return Score(
            id = 0,
            nodeId = "default-node",
            cpuScore = "80.00",
            gpuScore = "80.00",
            ssdScore = "80.00",
            ramScore = "80.00",
            networkScore = "80.00",
            hardwareHealthScore = "80.00",
            totalScore = "480.00",
            averageScore = "80.00"
        )
    }
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
