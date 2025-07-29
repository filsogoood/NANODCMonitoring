package com.nanodatacenter.nanodcmonitoring_compose

import android.os.Bundle
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
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.DataCenterMonitoringScreen
import com.nanodatacenter.nanodcmonitoring_compose.ui.theme.DataCenterTheme
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageConfigurationHelper
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 전체화면 모드 설정
        setupFullScreenMode()
        
        enableEdgeToEdge()
        
        setContent {
            DataCenterTheme {
                MonitoringApp()
            }
        }
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
