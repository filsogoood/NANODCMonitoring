package com.nanodatacenter.nanodcmonitoring_compose.manager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong

/**
 * 앱 사용량 및 성능 모니터링 관리 클래스
 * 확장성을 고려하여 여러 기기에서의 사용 패턴을 분석하고 성능을 최적화
 */
class UsageManager private constructor() {
    
    // Usage Statistics
    private val appStartTime = AtomicLong(System.currentTimeMillis())
    private val totalApiCalls = AtomicLong(0)
    private val successfulApiCalls = AtomicLong(0)
    private val failedApiCalls = AtomicLong(0)
    private val totalDataTransferred = AtomicLong(0) // bytes
    
    // Performance Metrics
    private val averageResponseTime = AtomicLong(0)
    private val maxResponseTime = AtomicLong(0)
    private val minResponseTime = AtomicLong(Long.MAX_VALUE)
    private val responseTimeSum = AtomicLong(0)
    
    // Session Management
    private var currentSessionStart = System.currentTimeMillis()
    private var periodicLoggingJob: Job? = null
    
    companion object {
        private const val TAG = "UsageManager"
        private const val PERIODIC_LOG_INTERVAL = 300000L // 5 minutes
        
        @Volatile
        private var INSTANCE: UsageManager? = null
        
        /**
         * Singleton 인스턴스 반환
         */
        fun getInstance(): UsageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UsageManager().also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Usage Manager 초기화
     * 앱 시작 시 호출하여 모니터링 시작
     */
    fun initialize(context: Context) {
        currentSessionStart = System.currentTimeMillis()
        startPeriodicLogging(context)
        
        Log.d(TAG, "🚀 UsageManager initialized")
        Log.d(TAG, "📊 Session started at: ${formatTimestamp(currentSessionStart)}")
    }
    
    /**
     * 앱 종료 시 정리 작업
     */
    fun cleanup() {
        periodicLoggingJob?.cancel()
        logFinalStatistics()
        Log.d(TAG, "🏁 UsageManager cleanup completed")
    }
    
    /**
     * API 호출 기록
     * @param responseTimeMs 응답 시간 (밀리초)
     * @param success 성공 여부
     * @param dataSizeBytes 전송된 데이터 크기 (바이트)
     */
    fun recordApiCall(responseTimeMs: Long, success: Boolean, dataSizeBytes: Long = 0) {
        totalApiCalls.incrementAndGet()
        
        if (success) {
            successfulApiCalls.incrementAndGet()
        } else {
            failedApiCalls.incrementAndGet()
        }
        
        totalDataTransferred.addAndGet(dataSizeBytes)
        
        // 응답 시간 통계 업데이트
        updateResponseTimeStats(responseTimeMs)
        
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "📡 API Call recorded - Success: $success, Response time: ${responseTimeMs}ms, Data: ${dataSizeBytes}B")
        }
    }
    
    /**
     * 응답 시간 통계 업데이트
     */
    private fun updateResponseTimeStats(responseTimeMs: Long) {
        responseTimeSum.addAndGet(responseTimeMs)
        
        // 최대 응답 시간 업데이트
        var currentMax = maxResponseTime.get()
        while (responseTimeMs > currentMax && !maxResponseTime.compareAndSet(currentMax, responseTimeMs)) {
            currentMax = maxResponseTime.get()
        }
        
        // 최소 응답 시간 업데이트
        var currentMin = minResponseTime.get()
        while (responseTimeMs < currentMin && !minResponseTime.compareAndSet(currentMin, responseTimeMs)) {
            currentMin = minResponseTime.get()
        }
        
        // 평균 응답 시간 계산
        val totalCalls = totalApiCalls.get()
        if (totalCalls > 0) {
            averageResponseTime.set(responseTimeSum.get() / totalCalls)
        }
    }
    
    /**
     * 현재까지의 사용 통계 반환
     */
    fun getUsageStatistics(): UsageStatistics {
        val currentTime = System.currentTimeMillis()
        val totalRuntime = currentTime - appStartTime.get()
        val sessionRuntime = currentTime - currentSessionStart
        
        return UsageStatistics(
            totalRuntimeMs = totalRuntime,
            sessionRuntimeMs = sessionRuntime,
            totalApiCalls = totalApiCalls.get(),
            successfulApiCalls = successfulApiCalls.get(),
            failedApiCalls = failedApiCalls.get(),
            apiSuccessRate = calculateSuccessRate(),
            totalDataTransferredBytes = totalDataTransferred.get(),
            averageResponseTimeMs = averageResponseTime.get(),
            maxResponseTimeMs = if (maxResponseTime.get() == Long.MAX_VALUE) 0 else maxResponseTime.get(),
            minResponseTimeMs = if (minResponseTime.get() == Long.MAX_VALUE) 0 else minResponseTime.get()
        )
    }
    
    /**
     * API 성공률 계산
     */
    private fun calculateSuccessRate(): Double {
        val total = totalApiCalls.get()
        if (total == 0L) return 0.0
        
        return (successfulApiCalls.get().toDouble() / total.toDouble()) * 100.0
    }
    
    /**
     * 주기적 통계 로깅 시작
     */
    private fun startPeriodicLogging(context: Context) {
        periodicLoggingJob = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(PERIODIC_LOG_INTERVAL)
                logCurrentStatistics(context)
            }
        }
    }
    
    /**
     * 현재 통계 로그 출력
     */
    private fun logCurrentStatistics(context: Context) {
        val stats = getUsageStatistics()
        
        Log.d(TAG, "==================== Usage Statistics (${formatTimestamp(System.currentTimeMillis())}) ====================")
        Log.d(TAG, "📊 Runtime - Total: ${formatDuration(stats.totalRuntimeMs)}, Session: ${formatDuration(stats.sessionRuntimeMs)}")
        Log.d(TAG, "📡 API Calls - Total: ${stats.totalApiCalls}, Success: ${stats.successfulApiCalls}, Failed: ${stats.failedApiCalls}")
        Log.d(TAG, "✅ Success Rate: ${"%.2f".format(stats.apiSuccessRate)}%")
        Log.d(TAG, "📊 Data Transferred: ${formatDataSize(stats.totalDataTransferredBytes)}")
        Log.d(TAG, "⏱️ Response Time - Avg: ${stats.averageResponseTimeMs}ms, Max: ${stats.maxResponseTimeMs}ms, Min: ${stats.minResponseTimeMs}ms")
        
        // 메모리 사용량 정보
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory() / (1024 * 1024)
        val totalMemory = runtime.totalMemory() / (1024 * 1024)
        val freeMemory = runtime.freeMemory() / (1024 * 1024)
        val usedMemory = totalMemory - freeMemory
        
        Log.d(TAG, "💾 Memory - Used: ${usedMemory}MB, Free: ${freeMemory}MB, Max: ${maxMemory}MB")
        Log.d(TAG, "===============================================================")
    }
    
    /**
     * 최종 통계 로그 출력
     */
    private fun logFinalStatistics() {
        val stats = getUsageStatistics()
        
        Log.d(TAG, "==================== Final Usage Statistics ====================")
        Log.d(TAG, "🏁 App Session Ended at: ${formatTimestamp(System.currentTimeMillis())}")
        Log.d(TAG, "⏱️ Total Session Duration: ${formatDuration(stats.sessionRuntimeMs)}")
        Log.d(TAG, "📡 Total API Calls: ${stats.totalApiCalls} (Success: ${stats.successfulApiCalls}, Failed: ${stats.failedApiCalls})")
        Log.d(TAG, "✅ Overall Success Rate: ${"%.2f".format(stats.apiSuccessRate)}%")
        Log.d(TAG, "📊 Total Data Transferred: ${formatDataSize(stats.totalDataTransferredBytes)}")
        Log.d(TAG, "⚡ Performance - Avg Response: ${stats.averageResponseTimeMs}ms")
        Log.d(TAG, "============================================================")
    }
    
    /**
     * 세션 재시작
     * 앱이 백그라운드에서 돌아올 때 사용
     */
    fun restartSession() {
        currentSessionStart = System.currentTimeMillis()
        Log.d(TAG, "🔄 Session restarted at: ${formatTimestamp(currentSessionStart)}")
    }
    
    /**
     * 통계 초기화
     * 디버깅이나 테스트 목적으로 사용
     */
    fun resetStatistics() {
        totalApiCalls.set(0)
        successfulApiCalls.set(0)
        failedApiCalls.set(0)
        totalDataTransferred.set(0)
        averageResponseTime.set(0)
        maxResponseTime.set(0)
        minResponseTime.set(Long.MAX_VALUE)
        responseTimeSum.set(0)
        currentSessionStart = System.currentTimeMillis()
        
        Log.d(TAG, "🔄 Usage statistics reset")
    }
    
    // Utility Methods
    
    private fun formatTimestamp(timestamp: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
    
    private fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
    
    private fun formatDataSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "${"%.2f".format(bytes / (1024.0 * 1024.0))} MB"
            bytes >= 1024 -> "${"%.2f".format(bytes / 1024.0)} KB"
            else -> "$bytes B"
        }
    }
    
    /**
     * 사용 통계 데이터 클래스
     */
    data class UsageStatistics(
        val totalRuntimeMs: Long,
        val sessionRuntimeMs: Long,
        val totalApiCalls: Long,
        val successfulApiCalls: Long,
        val failedApiCalls: Long,
        val apiSuccessRate: Double,
        val totalDataTransferredBytes: Long,
        val averageResponseTimeMs: Long,
        val maxResponseTimeMs: Long,
        val minResponseTimeMs: Long
    )
}