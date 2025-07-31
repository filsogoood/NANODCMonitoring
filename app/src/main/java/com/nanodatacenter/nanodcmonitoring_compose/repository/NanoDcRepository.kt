package com.nanodatacenter.nanodcmonitoring_compose.repository

import android.util.Log
import com.nanodatacenter.nanodcmonitoring_compose.network.api.NanoDcApiService
import com.nanodatacenter.nanodcmonitoring_compose.network.client.RetrofitClient
import com.nanodatacenter.nanodcmonitoring_compose.network.model.ApiResponse
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.isActive

/**
 * NanoDC 데이터 레포지토리
 * API 호출을 관리하고 데이터를 처리하는 클래스
 * 20초마다 자동으로 데이터를 갱신하는 기능 제공
 * Singleton 패턴으로 앱 전체에서 하나의 인스턴스를 공유
 */
class NanoDcRepository private constructor() {
    
    private val apiService: NanoDcApiService = RetrofitClient.nanoDcApiService
    
    // 코루틴 스코프 - 자동 갱신을 위한 백그라운드 작업용
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // 자동 갱신 작업을 위한 Job
    private var autoRefreshJob: Job? = null
    
    // API 응답 데이터를 위한 StateFlow
    private val _apiResponseState = MutableStateFlow<ApiResponse?>(null)
    val apiResponseState: StateFlow<ApiResponse?> = _apiResponseState.asStateFlow()
    
    // 로딩 상태를 위한 StateFlow
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 마지막 갱신 시간
    private val _lastRefreshTime = MutableStateFlow(0L)
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()
    
    companion object {
        private const val TAG = "NanoDcRepository"
        private const val DEFAULT_NANODC_ID = "c236ea9c-3d7e-430b-98b8-1e22d0d6cf01"
        private const val AUTO_REFRESH_INTERVAL = 20_000L // 20초
        
        @Volatile
        private var INSTANCE: NanoDcRepository? = null
        
        /**
         * Singleton 인스턴스 반환
         */
        fun getInstance(): NanoDcRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NanoDcRepository().also { INSTANCE = it }
            }
        }
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
            } catch (e: CancellationException) {
                Log.d(TAG, "🛑 Score API call cancelled")
                throw e
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
            } catch (e: CancellationException) {
                Log.d(TAG, "🛑 Score by NanoDC API call cancelled")
                throw e
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
            } catch (e: CancellationException) {
                Log.d(TAG, "🛑 First image score retrieval cancelled")
                throw e
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
            } catch (e: CancellationException) {
                Log.d(TAG, "🛑 API call cancelled")
                throw e // CancellationException은 다시 throw
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
     * 특정 노드의 NDP 트랜잭션 목록 조회
     * @param nodeId 노드 ID
     * @return NDP 트랜잭션 목록 또는 null (실패 시)
     */
    suspend fun getNdpTransactions(nodeId: String): List<com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction>? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching NDP transactions for nodeId: $nodeId")
                
                val response = apiService.getNdpTransactions(nodeId)
                
                if (response.isSuccessful) {
                    val transactions = response.body()
                    Log.d(TAG, "NDP transactions API call successful")
                    Log.d(TAG, "Transactions count: ${transactions?.size}")
                    transactions?.forEach { transaction ->
                        Log.d(TAG, "💰 Transaction ID: ${transaction.id}, Amount: ${transaction.amount} NDP")
                    }
                    transactions
                } else {
                    // 404는 예상된 상황 (엔드포인트가 아직 구현되지 않음)
                    if (response.code() == 404) {
                        Log.d(TAG, "Node NDP transactions API endpoint not available (404) - will use fallback method")
                    } else {
                        Log.e(TAG, "NDP transactions API call failed with code: ${response.code()}")
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    }
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during NDP transactions API call: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * 모든 NDP 트랜잭션 목록 조회 (NanoDC 기준)
     * @param nanodcId NanoDC ID (기본값 사용 가능)
     * @return 모든 NDP 트랜잭션 목록 또는 null (실패 시)
     */
    suspend fun getAllNdpTransactions(nanodcId: String = DEFAULT_NANODC_ID): List<com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction>? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching all NDP transactions for nanodcId: $nanodcId")
                
                val response = apiService.getAllNdpTransactions(nanodcId)
                
                if (response.isSuccessful) {
                    val transactions = response.body()
                    Log.d(TAG, "All NDP transactions API call successful")
                    Log.d(TAG, "Total transactions count: ${transactions?.size}")
                    
                    if (transactions != null) {
                        val totalAmount = transactions.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                        Log.d(TAG, "Total NDP amount: $totalAmount")
                    }
                    
                    transactions
                } else {
                    // 404는 예상된 상황 (엔드포인트가 아직 구현되지 않음)
                    if (response.code() == 404) {
                        Log.d(TAG, "NDP transactions API endpoint not available (404) - will use fallback method")
                    } else {
                        Log.e(TAG, "All NDP transactions API call failed with code: ${response.code()}")
                        Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    }
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during all NDP transactions API call: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * 기존 getUserData API에서 NDP 트랜잭션 추출
     * 새로운 API가 없을 경우 사용
     * @param nanodcId NanoDC ID
     * @return NDP 트랜잭션 목록 또는 빈 목록
     */
    suspend fun getNdpTransactionsFromUserData(nanodcId: String = DEFAULT_NANODC_ID): List<com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "📊 Using standard getUserData API to retrieve NDP transaction data")
                
                val userData = getUserData(nanodcId)
                val transactions = userData?.ndpListFiltered ?: emptyList()
                
                Log.d(TAG, "✅ Successfully extracted ${transactions.size} NDP transactions from user data")
                if (transactions.isNotEmpty()) {
                    val totalAmount = transactions.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
                    Log.d(TAG, "💰 Total NDP amount from user data: $totalAmount")
                }
                
                transactions
            } catch (e: Exception) {
                Log.e(TAG, "Exception during NDP extraction from user data: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    /**
     * NDP 트랜잭션 데이터 조회 (폴백 방식 포함)
     * 1차: 전용 API 호출
     * 2차: getUserData에서 추출
     * 3차: 빈 목록 반환
     * @param nodeId 노드 ID (선택사항)
     * @param nanodcId NanoDC ID
     * @return NDP 트랜잭션 목록
     */
    suspend fun getNdpTransactionsWithFallback(
        nodeId: String? = null,
        nanodcId: String = DEFAULT_NANODC_ID
    ): List<com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Attempting to get NDP transactions with fallback method")
                
                // 1차 시도: 노드별 트랜잭션 API 호출
                if (!nodeId.isNullOrEmpty()) {
                    val nodeTransactions = getNdpTransactions(nodeId)
                    if (!nodeTransactions.isNullOrEmpty()) {
                        Log.d(TAG, "Successfully retrieved NDP transactions via node API")
                        return@withContext nodeTransactions
                    }
                }
                
                // 2차 시도: 전체 트랜잭션 API 호출
                val allTransactions = getAllNdpTransactions(nanodcId)
                if (!allTransactions.isNullOrEmpty()) {
                    Log.d(TAG, "Successfully retrieved NDP transactions via all transactions API")
                    // 노드 ID가 지정된 경우 필터링
                    return@withContext if (!nodeId.isNullOrEmpty()) {
                        allTransactions.filter { it.nodeId == nodeId }
                    } else {
                        allTransactions
                    }
                }
                
                // 3차 시도: getUserData에서 추출 (정상적인 fallback 동작)
                Log.d(TAG, "✅ Using fallback method: extracting NDP transactions from getUserData API")
                val userDataTransactions = getNdpTransactionsFromUserData(nanodcId)
                
                // 노드 ID가 지정된 경우 필터링
                if (!nodeId.isNullOrEmpty()) {
                    userDataTransactions.filter { it.nodeId == nodeId }
                } else {
                    userDataTransactions
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during NDP transactions fallback: ${e.message}", e)
                emptyList()
            }
        }
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
    
    /**
     * 자동 데이터 갱신 시작
     * 20초마다 백그라운드에서 API 데이터를 가져와서 StateFlow를 업데이트
     * @param nanodcId NanoDC ID (기본값 사용 가능)
     */
    fun startAutoRefresh(nanodcId: String = DEFAULT_NANODC_ID) {
        // 기존 자동 갱신 작업이 있으면 취소
        stopAutoRefresh()
        
        Log.d(TAG, "🔄 Starting auto refresh every ${AUTO_REFRESH_INTERVAL / 1000} seconds")
        
        autoRefreshJob = repositoryScope.launch {
            try {
                // 즉시 첫 번째 데이터 로드
                refreshData(nanodcId)
                
                while (isActive) {
                    try {
                        delay(AUTO_REFRESH_INTERVAL)
                        if (isActive) {
                            refreshData(nanodcId)
                        }
                    } catch (e: CancellationException) {
                        // 코루틴이 정상적으로 취소됨 - 에러 로그 출력하지 않음
                        Log.d(TAG, "🛑 Auto refresh cancelled")
                        throw e // CancellationException은 다시 throw해야 함
                    } catch (e: Exception) {
                        Log.e(TAG, "Auto refresh error: ${e.message}", e)
                        // 일반 에러는 계속 시도 (백그라운드에서 조용히 처리)
                    }
                }
            } catch (e: CancellationException) {
                // 전체 코루틴이 취소됨
                Log.d(TAG, "🛑 Auto refresh job cancelled")
            }
        }
    }
    
    /**
     * 자동 데이터 갱신 중지
     */
    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
        Log.d(TAG, "⏹️ Auto refresh stopped")
    }
    
    /**
     * 데이터 새로고침
     * StateFlow를 업데이트하여 UI가 자동으로 갱신되도록 함
     * @param nanodcId NanoDC ID
     */
    private suspend fun refreshData(nanodcId: String) {
        try {
            // 로딩 상태 시작 (UI에 미세한 로딩 표시 가능하지만 차단하지 않음)
            _isLoading.value = true
            
            Log.d(TAG, "🔄 Refreshing data silently in background...")
            
            // 백그라운드에서 조용히 데이터 가져오기
            val newData = getUserData(nanodcId)
            
            if (newData != null) {
                // 새 데이터로 StateFlow 업데이트
                _apiResponseState.value = newData
                _lastRefreshTime.value = System.currentTimeMillis()
                Log.d(TAG, "✅ Data refreshed successfully in background")
            } else {
                Log.w(TAG, "⚠️ Failed to refresh data, keeping existing data")
            }
            
        } catch (e: CancellationException) {
            // 코루틴이 취소됨 - 정상적인 상황이므로 에러 로그 출력하지 않음
            Log.d(TAG, "🛑 Data refresh cancelled")
            throw e // CancellationException은 다시 throw해야 함
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during data refresh: ${e.message}", e)
            // 에러가 발생해도 기존 데이터는 유지
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * 수동으로 데이터 새로고침 (사용자가 요청한 경우)
     * @param nanodcId NanoDC ID
     */
    suspend fun manualRefresh(nanodcId: String = DEFAULT_NANODC_ID) {
        Log.d(TAG, "🔄 Manual refresh requested")
        refreshData(nanodcId)
    }
    
    /**
     * Repository 정리 (메모리 누수 방지)
     * Activity/Fragment의 생명주기에 맞춰 호출해야 함
     */
    fun cleanup() {
        stopAutoRefresh()
        Log.d(TAG, "🧹 Repository cleaned up")
    }
}
