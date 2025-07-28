package com.nanodatacenter.monitorwebview

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.launch

/**
 * 자동 로그인 및 NDP Score 관리 클래스
 */
class AutoLoginManager(
    private val context: Context,
    private val lifecycleScope: LifecycleCoroutineScope
) {
    
    companion object {
        private const val TAG = "NANODP_AUTO_LOGIN"
        private const val SCORE_TAG = "NDP_SCORE_MONITOR"  // 유니크한 필터용 태그
        // 자동 로그인용 계정 정보
        private const val AUTO_LOGIN_ID = "allen"
        private const val AUTO_LOGIN_PASSWORD = "123123"
    }
    
    private val apiClient = NdpApiClient.getInstance()
    private var authToken: String? = null
    
    /**
     * 자동 로그인 및 NDP Score 조회 시작
     */
    fun startAutoLogin() {
        Log.i(TAG, "🚀 자동 로그인 시작")
        Log.i(SCORE_TAG, "===============================================")
        Log.i(SCORE_TAG, "🔄 NDP SCORE 모니터링 시작")
        Log.i(SCORE_TAG, "===============================================")
        
        lifecycleScope.launch {
            try {
                // 1단계: 자동 로그인
                performLogin()
                
                // 2단계: NDP Score 조회
                if (authToken != null) {
                    fetchNdpScore()
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "💥 자동 로그인 프로세스 실패: ${e.message}")
                Log.e(SCORE_TAG, "❌ NDP SCORE 조회 실패: ${e.message}")
            }
        }
    }
    
    /**
     * 로그인 수행
     */
    private suspend fun performLogin() {
        Log.d(TAG, "🔐 로그인 시도: $AUTO_LOGIN_ID")
        Log.i(SCORE_TAG, "🔐 인증 과정 시작...")
        
        val loginResult = apiClient.login(AUTO_LOGIN_ID, AUTO_LOGIN_PASSWORD)
        
        loginResult.onSuccess { loginResponse ->
            authToken = loginResponse.token
            Log.i(TAG, "✅ 로그인 성공!")
            Log.d(TAG, "🔑 토큰 저장: ${authToken?.take(20)}...")
            Log.i(SCORE_TAG, "✅ 인증 완료 - API 호출 준비됨")
            
        }.onFailure { exception ->
            Log.e(TAG, "❌ 로그인 실패: ${exception.message}")
            Log.e(SCORE_TAG, "❌ 인증 실패: ${exception.message}")
            throw exception
        }
    }
    
    /**
     * NDP Score 조회 및 로그 출력
     */
    private suspend fun fetchNdpScore() {
        val token = authToken ?: return
        
        Log.d(TAG, "📊 NDP Score 조회 시작")
        Log.i(SCORE_TAG, "📊 NDP SCORE 데이터 요청 중...")
        
        // GET 방식으로 먼저 시도
        val result = apiClient.getUserData(token)
        
        result.onSuccess { apiResponse ->
            Log.i(TAG, "✅ API 데이터 조회 성공")
            Log.i(SCORE_TAG, "✅ NDP SCORE 데이터 수신 완료")
            
            // NDP Score 데이터 처리
            processNdpScoreData(apiResponse)
            
        }.onFailure { exception ->
            Log.e(TAG, "❌ NDP Score 조회 실패: ${exception.message}")
            Log.w(SCORE_TAG, "⚠️ GET 방식 실패 - POST 방식으로 재시도")
            
            // GET이 실패하면 POST 방식으로 재시도
            Log.d(TAG, "🔄 POST 방식으로 재시도")
            tryPostMethod(token)
        }
    }
    
    /**
     * POST 방식으로 데이터 조회 재시도
     */
    private suspend fun tryPostMethod(token: String) {
        val postResult = apiClient.getUserDataPost(token)
        
        postResult.onSuccess { apiResponse ->
            Log.i(TAG, "✅ POST 방식으로 API 데이터 조회 성공")
            Log.i(SCORE_TAG, "✅ POST 방식으로 NDP SCORE 데이터 수신 완료")
            processNdpScoreData(apiResponse)
            
        }.onFailure { exception ->
            Log.e(TAG, "❌ POST 방식도 실패: ${exception.message}")
            Log.e(SCORE_TAG, "❌ 모든 방식 실패 - NDP SCORE 조회 불가: ${exception.message}")
        }
    }
    
    /**
     * NDP Score 데이터 처리 및 로그 출력
     */
    private fun processNdpScoreData(apiResponse: ApiResponse) {
        val allScores = apiResponse.allScores
        
        if (allScores.isNullOrEmpty()) {
            Log.w(TAG, "⚠️ NDP Score 데이터가 없습니다")
            Log.w(SCORE_TAG, "⚠️ 수신된 점수 데이터가 없습니다")
            return
        }
        
        Log.d(TAG, "📊 받은 점수 데이터 개수: ${allScores.size}")
        Log.i(SCORE_TAG, "📊 총 ${allScores.size}개의 노드 점수 데이터 수신")
        
        // 첫 번째 점수 데이터 사용 (또는 평균 계산)
        val scoreData = allScores.first()
        
        // 점수 정보 로깅
        Log.d(TAG, "🎯 Node ID: ${scoreData.nodeId}")
        Log.d(TAG, "💻 CPU Score: ${scoreData.cpuScore}")
        Log.d(TAG, "🎮 GPU Score: ${scoreData.gpuScore}")
        Log.d(TAG, "💾 SSD Score: ${scoreData.ssdScore}")
        Log.d(TAG, "🧠 RAM Score: ${scoreData.ramScore}")
        Log.d(TAG, "🌐 Network Score: ${scoreData.networkScore}")
        Log.d(TAG, "❤️ Health Score: ${scoreData.hardwareHealthScore}")
        Log.d(TAG, "📈 Total Score: ${scoreData.totalScore}")
        Log.d(TAG, "📊 Average Score: ${scoreData.averageScore}")
        
        // 유니크 필터로 NDP Score 정보 출력
        logNdpScoreDetails(scoreData)
        
        // 여러 점수가 있는 경우 모든 점수 표시
        if (allScores.size > 1) {
            showAdditionalScores(allScores)
        }
    }
    
    /**
     * NDP Score 상세 정보를 유니크 필터로 로그 출력
     */
    private fun logNdpScoreDetails(scoreData: ApiScore) {
        Log.i(SCORE_TAG, "===============================================")
        Log.i(SCORE_TAG, "🏆 NDP SCORE 조회 성공!")
        Log.i(SCORE_TAG, "===============================================")
        Log.i(SCORE_TAG, "🎯 Node ID: ${scoreData.nodeId}")
        Log.i(SCORE_TAG, "")
        Log.i(SCORE_TAG, "📊 === 전체 점수 ===")
        Log.i(SCORE_TAG, "📈 Total Score: ${scoreData.totalScore}")
        Log.i(SCORE_TAG, "📊 Average Score: ${scoreData.averageScore}")
        Log.i(SCORE_TAG, "")
        Log.i(SCORE_TAG, "🔧 === 세부 점수 ===")
        Log.i(SCORE_TAG, "💻 CPU Score: ${scoreData.cpuScore}")
        Log.i(SCORE_TAG, "🎮 GPU Score: ${scoreData.gpuScore}")
        Log.i(SCORE_TAG, "💾 SSD Score: ${scoreData.ssdScore}")
        Log.i(SCORE_TAG, "🧠 RAM Score: ${scoreData.ramScore}")
        Log.i(SCORE_TAG, "🌐 Network Score: ${scoreData.networkScore}")
        Log.i(SCORE_TAG, "❤️ Health Score: ${scoreData.hardwareHealthScore}")
        Log.i(SCORE_TAG, "===============================================")
    }
    
    /**
     * 여러 점수가 있는 경우 추가 점수 표시
     */
    private fun showAdditionalScores(allScores: List<ApiScore>) {
        lifecycleScope.launch {
            // 2초 후에 추가 정보 표시
            kotlinx.coroutines.delay(2000)
            
            val averageScore = calculateAverageScore(allScores)
            
            Log.i(SCORE_TAG, "")
            Log.i(SCORE_TAG, "📊 === 다중 노드 통계 ===")
            Log.i(SCORE_TAG, "📊 총 노드 수: ${allScores.size}개")
            Log.i(SCORE_TAG, "📊 평균 Total Score: $averageScore")
            Log.i(SCORE_TAG, "===============================================")
            
            // 모든 노드의 상세 정보 출력
            allScores.forEachIndexed { index, score ->
                Log.d(SCORE_TAG, "노드 ${index + 1} (${score.nodeId}): Total=${score.totalScore}, Avg=${score.averageScore}")
            }
        }
    }
    
    /**
     * 전체 점수의 평균 계산
     */
    private fun calculateAverageScore(allScores: List<ApiScore>): String {
        return try {
            val totalScores = allScores.mapNotNull { it.totalScore.toDoubleOrNull() }
            if (totalScores.isNotEmpty()) {
                String.format("%.2f", totalScores.average())
            } else {
                "계산 불가"
            }
        } catch (e: Exception) {
            "계산 오류"
        }
    }
    
    /**
     * 현재 인증 토큰 반환
     */
    fun getAuthToken(): String? = authToken
}
    
    /**
     * 수동으로 NDP Score 새로고침
     */
    fun refreshNdpScore() {
        val token = authToken
        if (token != null) {
            Log.i(TAG, "🔄 NDP Score 수동 새로고침")
            Log.i(SCORE_TAG, "===============================================")
            Log.i(SCORE_TAG, "🔄 NDP SCORE 수동 새로고침 요청")
            Log.i(SCORE_TAG, "===============================================")
            lifecycleScope.launch {
                fetchNdpScore()
            }
        } else {
            Log.w(TAG, "⚠️ 인증 토큰이 없어 새로고침 불가")
            Log.w(SCORE_TAG, "⚠️ 인증 토큰 없음 - 자동 로그인 재시도")
            startAutoLogin()
        }
    }
}