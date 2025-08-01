package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.ui.graphics.Color

/**
 * 사용률 그래프 데이터 클래스
 * CPU, Memory, GPU 등의 사용률을 그래프로 표시하기 위한 데이터 구조
 */
data class UsageGraphData(
    val name: String,           // 메트릭 이름 (CPU, Memory, GPU 등)
    val percentage: Float,      // 사용률 퍼센트 (0.0 ~ 100.0)
    val color: Color,          // 그래프 색상
    val unit: String = "%"     // 단위 (기본값: %)
)

/**
 * 사용률 그래프 메트릭
 */
object UsageMetrics {
    val CPU_COLOR = Color(0xFF3B82F6)      // 파란색
    val MEMORY_COLOR = Color(0xFF10B981)    // 초록색  
    val GPU_COLOR = Color(0xFF8B5CF6)       // 보라색
    val STORAGE_COLOR = Color(0xFFF59E0B)   // 주황색
    val HEALTH_COLOR = Color(0xFFEF4444)    // 빨간색
}
