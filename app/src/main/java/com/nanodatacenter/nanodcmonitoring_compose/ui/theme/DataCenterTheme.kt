package com.nanodatacenter.nanodcmonitoring_compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 데이터센터 모니터링 전용 색상 팔레트
object DataCenterColors {
    // 배경 색상
    val DeepSpace = Color(0xFF09162A) // XML과 동일한 배경색
    val DarkSpace = Color(0xFF1B1E28)
    val MidSpace = Color(0xFF2A2D3A)
    
    // 청록색 계열 (헤더, 로고)
    val CyberTeal = Color(0xFF00D4AA)
    val CyberTealLight = Color(0xFF4FFFDA)
    val CyberTealDark = Color(0xFF00A085)
    
    // 상태 인디케이터 색상
    val StatusGreen = Color(0xFF00FF88)      // 정상
    val StatusOrange = Color(0xFFFF8800)     // 경고  
    val StatusPurple = Color(0xFFAA44FF)     // 에러
    val StatusBlue = Color(0xFF0088FF)       // 정보
    val StatusRed = Color(0xFFFF4444)        // 중요
    val StatusYellow = Color(0xFFFFDD00)     // 주의
    
    // 텍스트 색상
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB0B7C3)
    val TextTertiary = Color(0xFF6B7280)
}

// 데이터센터 다크 테마
private val DataCenterDarkColorScheme = darkColorScheme(
    primary = DataCenterColors.CyberTeal,
    onPrimary = DataCenterColors.TextPrimary,
    primaryContainer = DataCenterColors.CyberTealDark,
    onPrimaryContainer = DataCenterColors.TextPrimary,
    
    secondary = DataCenterColors.StatusBlue,
    onSecondary = DataCenterColors.TextPrimary,
    secondaryContainer = DataCenterColors.MidSpace,
    onSecondaryContainer = DataCenterColors.TextSecondary,
    
    tertiary = DataCenterColors.StatusPurple,
    onTertiary = DataCenterColors.TextPrimary,
    
    background = DataCenterColors.DeepSpace,
    onBackground = DataCenterColors.TextPrimary,
    
    surface = DataCenterColors.DarkSpace,
    onSurface = DataCenterColors.TextPrimary,
    surfaceVariant = DataCenterColors.MidSpace,
    onSurfaceVariant = DataCenterColors.TextSecondary,
    
    outline = DataCenterColors.TextTertiary,
    outlineVariant = DataCenterColors.MidSpace
)

// 라이트 테마는 사용하지 않지만 정의
private val DataCenterLightColorScheme = lightColorScheme(
    primary = DataCenterColors.CyberTeal,
    onPrimary = Color.White,
    background = Color.White,
    onBackground = Color.Black
)

@Composable
fun DataCenterTheme(
    darkTheme: Boolean = true, // 항상 다크 테마 사용
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DataCenterDarkColorScheme
    } else {
        DataCenterLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
