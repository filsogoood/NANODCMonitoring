package com.nanodatacenter.nanodcmonitoring_compose

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.DataCenterMonitoringScreen
import com.nanodatacenter.nanodcmonitoring_compose.ui.theme.DataCenterTheme
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageConfigurationHelper
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private val repository = NanoDcRepository.getInstance()
    
    companion object {
        private const val TAG = "MainActivity"
        private const val TEST_NANODC_ID = "c236ea9c-3d7e-430b-98b8-1e22d0d6cf01"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 전체화면 모드 설정
        setupFullScreenMode()
        
        // API 연결 테스트 및 자동 갱신 시작
        testApiConnection()
        startAutoDataRefresh()
        
        enableEdgeToEdge()
        
        setContent {
            DataCenterTheme {
                MonitoringApp()
            }
        }
    }
    
    /**
     * API 연결 테스트 실행
     */
    private fun testApiConnection() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "🚀 Starting API connection test...")
                repository.testApiConnection(TEST_NANODC_ID)
            } catch (e: Exception) {
                Log.e(TAG, "❌ API connection test failed with exception: ${e.message}", e)
            }
        }
    }
    
    /**
     * 자동 데이터 갱신 시작
     * 20초마다 백그라운드에서 데이터를 갱신합니다
     */
    private fun startAutoDataRefresh() {
        Log.d(TAG, "🔄 Starting automatic data refresh...")
        repository.startAutoRefresh(TEST_NANODC_ID)
    }
    
    /**
     * 전체화면 모드 설정 (상태바, 네비게이션바 숨기기)
     */
    private fun setupFullScreenMode() {
        // 시스템 UI와 앱 콘텐츠가 겹치도록 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 화면을 항상 켜져있도록 설정 (모니터링 앱이므로)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // 시스템 UI 컨트롤러 가져오기
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        
        // 시스템 바들을 숨기고 몰입형 모드 설정
        windowInsetsController.apply {
            // 상태바와 네비게이션바 숨기기
            hide(WindowInsetsCompat.Type.systemBars())
            
            // 시스템 바가 자동으로 나타나지 않도록 설정
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // 추가적인 몰입형 모드 설정 (API 30 이하 호환성)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        
        // 포커스를 다시 얻었을 때 전체화면 모드 재설정
        if (hasFocus) {
            setupFullScreenMode()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Repository 정리 (자동 갱신 중지 및 리소스 해제)
        repository.cleanup()
        Log.d(TAG, "MainActivity destroyed, resources cleaned up")
    }
}

@Composable
fun MonitoringApp() {
    // 앱 시작 시 설정 초기화
    LaunchedEffect(Unit) {
        ImageConfigurationHelper.applyAllConfigurations()
        ImageOrderManager.getInstance().setCurrentDeviceType(DeviceType.DEFAULT)
    }
    
    // 전체 화면을 어두운 배경으로 설정하고 이미지들을 간격 없이 연속적으로 표시
    DataCenterMonitoringScreen(
        deviceType = DeviceType.DEFAULT,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        scaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH,
        useOriginalSize = true // 원본 크기로 표시하며 margin 적용
    )
}

@Preview(showBackground = true, name = "데이터센터 모니터링")
@Composable
fun MonitoringAppPreview() {
    DataCenterTheme {
        MonitoringApp()
    }
}
