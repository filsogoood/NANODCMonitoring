package com.nanodatacenter.nanodcmonitoring_compose.config

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import java.util.Locale

/**
 * 기기별 설정 관리 클래스
 * 여러 기기에서 사용되는 앱의 설정을 관리하고 기기별 특성을 고려한 설정 제공
 */
class DeviceConfigurationManager private constructor(context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val deviceId: String = Settings.Secure.getString(
        context.contentResolver, 
        Settings.Secure.ANDROID_ID
    ) ?: "unknown_device"
    
    companion object {
        private const val TAG = "DeviceConfigManager"
        private const val PREFS_NAME = "nanodc_device_config"
        
        // SharedPreferences Keys
        private const val KEY_DEVICE_NAME = "device_name"
        private const val KEY_NANODC_ID = "nanodc_id"
        private const val KEY_PREFERRED_LANGUAGE = "preferred_language"
        private const val KEY_REFRESH_INTERVAL = "refresh_interval"
        private const val KEY_ENABLE_NOTIFICATIONS = "enable_notifications"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
        private const val KEY_API_TIMEOUT = "api_timeout"
        private const val KEY_ENABLE_DETAILED_LOGS = "enable_detailed_logs"
        
        // Default Values
        private const val DEFAULT_REFRESH_INTERVAL = 30000L // 30 seconds
        private const val DEFAULT_API_TIMEOUT = 30L // 30 seconds
        
        @Volatile
        private var INSTANCE: DeviceConfigurationManager? = null
        
        /**
         * Singleton 인스턴스 반환
         * @param context Application Context
         * @return DeviceConfigurationManager 인스턴스
         */
        fun getInstance(context: Context): DeviceConfigurationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeviceConfigurationManager(context.applicationContext).also { 
                    INSTANCE = it 
                }
            }
        }
    }
    
    /**
     * 기기 고유 ID 반환
     */
    fun getDeviceId(): String = deviceId
    
    /**
     * 기기 이름 설정/반환
     */
    fun setDeviceName(name: String) {
        sharedPreferences.edit().putString(KEY_DEVICE_NAME, name).apply()
        Log.d(TAG, "Device name set to: $name")
    }
    
    fun getDeviceName(): String {
        return sharedPreferences.getString(KEY_DEVICE_NAME, "Device_$deviceId") ?: "Unknown Device"
    }
    
    /**
     * NanoDC ID 설정/반환
     * 기본값은 ApiConfiguration에서 가져옴
     */
    fun setNanoDcId(nanoDcId: String) {
        sharedPreferences.edit().putString(KEY_NANODC_ID, nanoDcId).apply()
        Log.d(TAG, "NanoDC ID set to: $nanoDcId")
    }
    
    fun getNanoDcId(): String {
        return sharedPreferences.getString(KEY_NANODC_ID, ApiConfiguration.Defaults.NANODC_ID) 
            ?: ApiConfiguration.Defaults.NANODC_ID
    }
    
    /**
     * 언어 설정 관리
     */
    fun setPreferredLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_PREFERRED_LANGUAGE, language).apply()
        Log.d(TAG, "Preferred language set to: $language")
    }
    
    fun getPreferredLanguage(): String {
        return sharedPreferences.getString(KEY_PREFERRED_LANGUAGE, "en") ?: "en"
    }
    
    /**
     * 데이터 새로고침 간격 설정/반환 (밀리초)
     */
    fun setRefreshInterval(intervalMs: Long) {
        sharedPreferences.edit().putLong(KEY_REFRESH_INTERVAL, intervalMs).apply()
        Log.d(TAG, "Refresh interval set to: ${intervalMs}ms")
    }
    
    fun getRefreshInterval(): Long {
        return sharedPreferences.getLong(KEY_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL)
    }
    
    /**
     * 알림 설정 관리
     */
    fun setNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ENABLE_NOTIFICATIONS, enabled).apply()
        Log.d(TAG, "Notifications enabled: $enabled")
    }
    
    fun isNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_ENABLE_NOTIFICATIONS, true)
    }
    
    /**
     * 테마 모드 설정 관리
     */
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    fun setThemeMode(mode: ThemeMode) {
        sharedPreferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        Log.d(TAG, "Theme mode set to: $mode")
    }
    
    fun getThemeMode(): ThemeMode {
        val modeString = sharedPreferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeString ?: ThemeMode.SYSTEM.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }
    
    /**
     * 마지막 동기화 시간 관리
     */
    fun setLastSyncTime(timestamp: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply()
    }
    
    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC_TIME, 0L)
    }
    
    /**
     * API 타임아웃 설정 관리
     */
    fun setApiTimeout(timeoutSeconds: Long) {
        sharedPreferences.edit().putLong(KEY_API_TIMEOUT, timeoutSeconds).apply()
        Log.d(TAG, "API timeout set to: ${timeoutSeconds}s")
    }
    
    fun getApiTimeout(): Long {
        return sharedPreferences.getLong(KEY_API_TIMEOUT, DEFAULT_API_TIMEOUT)
    }
    
    /**
     * 상세 로그 설정 관리
     */
    fun setDetailedLogsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ENABLE_DETAILED_LOGS, enabled).apply()
        Log.d(TAG, "Detailed logs enabled: $enabled")
    }
    
    fun isDetailedLogsEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_ENABLE_DETAILED_LOGS, false)
    }
    
    /**
     * 기기 정보 로그 출력
     * 디버깅 및 지원을 위한 기기 정보 확인
     */
    fun logDeviceInfo(context: Context) {
        Log.d(TAG, "==================== Device Configuration Info ====================")
        Log.d(TAG, "Device ID: $deviceId")
        Log.d(TAG, "Device Name: ${getDeviceName()}")
        Log.d(TAG, "NanoDC ID: ${getNanoDcId()}")
        Log.d(TAG, "Language: ${getPreferredLanguage()}")
        Log.d(TAG, "Refresh Interval: ${getRefreshInterval()}ms")
        Log.d(TAG, "Notifications: ${isNotificationsEnabled()}")
        Log.d(TAG, "Theme Mode: ${getThemeMode()}")
        Log.d(TAG, "API Timeout: ${getApiTimeout()}s")
        Log.d(TAG, "Detailed Logs: ${isDetailedLogsEnabled()}")
        Log.d(TAG, "System Language: ${Locale.getDefault().language}")
        Log.d(TAG, "System Country: ${Locale.getDefault().country}")
        Log.d(TAG, "Last Sync: ${if (getLastSyncTime() > 0) java.util.Date(getLastSyncTime()) else "Never"}")
        Log.d(TAG, "================================================================")
    }
    
    /**
     * 설정 초기화
     * 앱 재설치나 설정 리셋 시 사용
     */
    fun resetConfiguration() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Device configuration reset completed")
    }
    
    /**
     * 기기별 맞춤 설정 적용
     * 기기 성능에 따른 최적화된 설정 제공
     */
    fun applyDeviceOptimizedSettings(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        // 메모리 용량에 따른 새로고침 간격 조정
        val totalMemoryMB = memoryInfo.totalMem / (1024 * 1024)
        val optimizedInterval = when {
            totalMemoryMB < 2048 -> 60000L // 2GB 미만: 60초
            totalMemoryMB < 4096 -> 45000L // 4GB 미만: 45초
            else -> 30000L // 4GB 이상: 30초
        }
        
        // 기존 설정이 없는 경우에만 최적화된 설정 적용
        if (getRefreshInterval() == DEFAULT_REFRESH_INTERVAL) {
            setRefreshInterval(optimizedInterval)
            Log.d(TAG, "Applied optimized refresh interval: ${optimizedInterval}ms for device with ${totalMemoryMB}MB RAM")
        }
    }
}