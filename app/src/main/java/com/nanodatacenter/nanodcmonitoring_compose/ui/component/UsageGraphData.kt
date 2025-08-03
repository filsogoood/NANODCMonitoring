package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.ui.graphics.Color
import com.nanodatacenter.nanodcmonitoring_compose.util.BC02DataMapper

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
    DASHBOARD,     // 대시보드 스타일 레이아웃
    STORAGE_FOCUSED, // Storage 중심 레이아웃 (BC02 Storage 전용)
    POST_WORKER_FOCUSED, // PostWorker 중심 레이아웃 (BC02 PostWorker 전용)
    NAS_FOCUSED    // NAS 중심 레이아웃 (BC02 NAS 전용)
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
    val ssdHealth: UsageGraphData?, // null이면 그래프 자체를 안보이게 변경
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
 * BC02 노드의 경우 카테고리별로 특별한 레이아웃 적용
 */
fun getLayoutPatternForNode(nodeIndex: Int, nodeName: String): GraphLayoutPattern {
    // BC02 노드 확인 및 카테고리별 레이아웃 적용
    return when {
        // BC02 노드 식별 및 카테고리별 처리
        isBC02Node(nodeName) -> {
            val category = getBC02CategoryFromNodeName(nodeName)
            when (category) {
                BC02DataMapper.BC02NodeCategory.POST_WORKER -> GraphLayoutPattern.POST_WORKER_FOCUSED // PostWorker 계열: PostWorker 중심 레이아웃
                BC02DataMapper.BC02NodeCategory.NODE_MINER -> GraphLayoutPattern.MIXED_LAYOUT         // NodeMiner 계열: 혼합 레이아웃
                BC02DataMapper.BC02NodeCategory.NAS -> GraphLayoutPattern.NAS_FOCUSED                 // NAS 계열: NAS 중심 레이아웃
                BC02DataMapper.BC02NodeCategory.UNKNOWN -> GraphLayoutPattern.GRID_2X2                // 알 수 없는 BC02 노드: 기본 그리드
            }
        }
        // 기존 일반 노드 처리
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
 * BC02 노드인지 확인하는 함수
 * BC02DataMapper의 기능을 재사용하여 일관성 유지
 */
private fun isBC02Node(nodeName: String): Boolean {
    return BC02DataMapper.isBC02Node(nodeName) ||
           nodeName.startsWith("BC02", ignoreCase = true) ||
           (nodeName.contains("Filecoin", ignoreCase = true) && nodeName.contains("Miner", ignoreCase = true)) ||
           (nodeName.contains("3080Ti", ignoreCase = true) || nodeName.contains("GPU Worker", ignoreCase = true)) ||
           nodeName.contains("Post Worker", ignoreCase = true) ||
           nodeName.matches(Regex(".*NAS[1-5].*", RegexOption.IGNORE_CASE))
}

/**
 * BC02 노드 이름에서 카테고리를 추출하는 함수
 * BC02DataMapper의 기능을 재사용하여 일관성 유지
 */
private fun getBC02CategoryFromNodeName(nodeName: String): BC02DataMapper.BC02NodeCategory {
    return BC02DataMapper.getBC02SectorFromNodeName(nodeName)
}

/**
 * NodeUsage에서 ExtendedUsageData로 변환하는 확장 함수
 */
fun com.nanodatacenter.nanodcmonitoring_compose.network.model.NodeUsage.toExtendedUsageData(): ExtendedUsageData {
    return ExtendedUsageData(
        cpuUsage = UsageGraphData(
            name = "CPU Usage",
            percentage = this.cpuUsagePercent?.toFloatOrNull() ?: 0f,
            color = UsageMetrics.CPU_COLOR,
            type = GraphType.CIRCULAR
        ),
        memoryUsage = UsageGraphData(
            name = "Memory",
            percentage = this.memUsagePercent?.toFloatOrNull() ?: 0f,
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
            value = "${this.usedStorageGb ?: "N/A"}GB"
        ),
        cpuTemp = UsageGraphData(
            name = "CPU Temperature",
            percentage = (this.cpuTemp?.toFloatOrNull() ?: 0f).let { temp ->
                // 온도를 퍼센트로 변환 (0-100도 기준)
                (temp / 100f * 100f).coerceIn(0f, 100f)
            },
            color = UsageMetrics.TEMPERATURE_COLOR,
            type = GraphType.BAR,
            value = "${this.cpuTemp ?: "N/A"}°C",
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
        ssdHealth = this.ssdHealthPercent?.let { healthPercent ->
            UsageGraphData(
                name = "SSD Health",
                percentage = healthPercent.toFloatOrNull() ?: 0f,
                color = UsageMetrics.HEALTH_COLOR,
                type = GraphType.BAR,
                value = "${healthPercent}%"
            )
        }, // null이면 그래프 자체가 안보임
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