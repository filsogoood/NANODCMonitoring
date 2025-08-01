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
    val unit: String = "%",    // 단위 (기본값: %)
    val type: GraphType = GraphType.CIRCULAR,  // 그래프 타입
    val value: String = "",    // 추가 값 (온도 등)
    val maxValue: Float = 100f // 최대값 (바 그래프용)
)

/**
 * 그래프 타입 열거형
 */
enum class GraphType {
    CIRCULAR,      // 원형 진행률 그래프
    BAR,           // 바 형태 그래프
    LINE,          // 라인 그래프
    DONUT,         // 도넛 그래프
    GAUGE          // 게이지 형태 그래프
}

/**
 * 레이아웃 패턴 열거형
 * 각 노드마다 다른 그래프 레이아웃을 적용하기 위함
 */
enum class GraphLayoutPattern {
    GRID_2X2,      // 2x2 그리드 레이아웃
    VERTICAL_BARS, // 세로 바 그래프 레이아웃
    MIXED_LAYOUT,  // 혼합 레이아웃 (원형 + 바)
    HORIZONTAL_BARS,// 가로 바 그래프 레이아웃
    DASHBOARD      // 대시보드 스타일 레이아웃
}

/**
 * 확장된 사용률 데이터
 * 온도, 스토리지, SSD Health 등의 추가 메트릭 포함
 */
data class ExtendedUsageData(
    val cpuUsage: UsageGraphData,
    val memoryUsage: UsageGraphData,
    val gpuUsage: UsageGraphData,
    val storageUsage: UsageGraphData,
    val cpuTemp: UsageGraphData,
    val gpuTemp: UsageGraphData?,
    val ssdHealth: UsageGraphData,
    val cpuVram: UsageGraphData?
)

/**
 * 사용률 그래프 메트릭
 */
object UsageMetrics {
    val CPU_COLOR = Color(0xFF3B82F6)        // 파란색
    val MEMORY_COLOR = Color(0xFF10B981)      // 초록색  
    val GPU_COLOR = Color(0xFF8B5CF6)         // 보라색
    val STORAGE_COLOR = Color(0xFFF59E0B)     // 주황색
    val HEALTH_COLOR = Color(0xFFEF4444)      // 빨간색
    val TEMPERATURE_COLOR = Color(0xFFEC4899) // 핑크색
    val VRAM_COLOR = Color(0xFF06B6D4)        // 시아색
    
    // 그래프 배경색
    val BACKGROUND_DARK = Color(0xFF374151)
    val CARD_BACKGROUND = Color(0xFF1F2937)
    val GRID_LINE_COLOR = Color(0xFF4A4A4A)
    
    // 텍스트 색상
    val TEXT_PRIMARY = Color.White
    val TEXT_SECONDARY = Color(0xFF9CA3AF)
    val TEXT_ACCENT = Color(0xFF60A5FA)
}

/**
 * 노드별 그래프 레이아웃 패턴 결정
 * 노드 이름이나 인덱스에 따라 다른 레이아웃 패턴 반환
 */
fun getLayoutPatternForNode(nodeIndex: Int, nodeName: String): GraphLayoutPattern {
    return when {
        nodeName.contains("Supra", ignoreCase = true) -> GraphLayoutPattern.VERTICAL_BARS
        nodeName.contains("PostWorker", ignoreCase = true) -> GraphLayoutPattern.HORIZONTAL_BARS
        nodeName.contains("Filecoin", ignoreCase = true) -> GraphLayoutPattern.MIXED_LAYOUT
        nodeName.contains("Aethir", ignoreCase = true) -> GraphLayoutPattern.DASHBOARD
        else -> when (nodeIndex % 4) {
            0 -> GraphLayoutPattern.GRID_2X2
            1 -> GraphLayoutPattern.VERTICAL_BARS
            2 -> GraphLayoutPattern.HORIZONTAL_BARS
            3 -> GraphLayoutPattern.MIXED_LAYOUT
            else -> GraphLayoutPattern.GRID_2X2
        }
    }
}

/**
 * NodeUsage에서 ExtendedUsageData로 변환하는 확장 함수
 */
fun com.nanodatacenter.nanodcmonitoring_compose.network.model.NodeUsage.toExtendedUsageData(): ExtendedUsageData {
    return ExtendedUsageData(
        cpuUsage = UsageGraphData(
            name = "CPU Usage",
            percentage = this.cpuUsagePercent.toFloatOrNull() ?: 0f,
            color = UsageMetrics.CPU_COLOR,
            type = GraphType.CIRCULAR
        ),
        memoryUsage = UsageGraphData(
            name = "Memory",
            percentage = this.memUsagePercent.toFloatOrNull() ?: 0f,
            color = UsageMetrics.MEMORY_COLOR,
            type = GraphType.CIRCULAR
        ),
        gpuUsage = UsageGraphData(
            name = "GPU Usage",
            percentage = this.gpuUsagePercent?.toFloatOrNull() ?: 0f,
            color = UsageMetrics.GPU_COLOR,
            type = GraphType.CIRCULAR
        ),
        storageUsage = UsageGraphData(
            name = "Storage Used",
            percentage = this.harddiskUsedPercent?.toFloatOrNull() ?: 0f,
            color = UsageMetrics.STORAGE_COLOR,
            type = GraphType.BAR,
            value = "${this.usedStorageGb}GB"
        ),
        cpuTemp = UsageGraphData(
            name = "CPU Temperature",
            percentage = (this.cpuTemp?.toFloatOrNull() ?: 0f).let { temp ->
                // 온도를 퍼센트로 변환 (0-100도 기준)
                (temp / 100f * 100f).coerceIn(0f, 100f)
            },
            color = UsageMetrics.TEMPERATURE_COLOR,
            type = GraphType.BAR,
            value = "${this.cpuTemp}°C",
            maxValue = 100f
        ),
        gpuTemp = this.gpuTemp?.let { temp ->
            UsageGraphData(
                name = "GPU Temperature",
                percentage = (temp.toFloatOrNull() ?: 0f).let { tempFloat ->
                    (tempFloat / 100f * 100f).coerceIn(0f, 100f)
                },
                color = UsageMetrics.TEMPERATURE_COLOR,
                type = GraphType.BAR,
                value = "${temp}°C",
                maxValue = 100f
            )
        },
        ssdHealth = UsageGraphData(
            name = "SSD Health",
            percentage = this.ssdHealthPercent?.toFloatOrNull() ?: 100f,
            color = UsageMetrics.HEALTH_COLOR,
            type = GraphType.BAR,
            value = "${this.ssdHealthPercent}%"
        ),
        cpuVram = this.gpuVramPercent?.let { vram ->
            UsageGraphData(
                name = "GPU VRAM",
                percentage = vram.toFloatOrNull() ?: 0f,
                color = UsageMetrics.VRAM_COLOR,
                type = GraphType.CIRCULAR,
                value = "${vram}%"
            )
        }
    )
}
