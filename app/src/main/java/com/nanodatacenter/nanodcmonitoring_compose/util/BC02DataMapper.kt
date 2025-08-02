package com.nanodatacenter.nanodcmonitoring_compose.util

import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Node

/**
 * BC02 데이터센터 전용 데이터 매핑 유틸리티
 * 
 * BC02 매핑 규칙:
 * - LONOVO_POST (imageIndex 4) → BC02 Filecoin Miner
 * - LONOVO_POST (imageIndex 5) → BC02 3080Ti GPU Worker
 * - LONOVO_POST (imageIndex 6) → BC02 Post Worker
 * - STORAGE_1 (imageIndex 9) → BC02 NAS1
 * - STORAGE_1 (imageIndex 10) → BC02 NAS2
 * - STORAGE_1 (imageIndex 11) → BC02 NAS3
 * - STORAGE_1 (imageIndex 12) → BC02 NAS4
 * - STORAGE_1 (imageIndex 13) → BC02 NAS5
 */

object BC02DataMapper {
    
    /**
     * BC02 노드 카테고리 분류
     * 각 카테고리별로 다른 그래프 레이아웃을 적용하기 위함
     */
    enum class BC02NodeCategory {
        POST_WORKER,  // Post Worker
        NODE_MINER,   // Filecoin Miner, 3080Ti GPU Worker
        NAS,          // NAS1-5
        UNKNOWN       // 매핑되지 않은 노드
    }
    
    /**
     * BC02 이미지 인덱스별 노드 이름 매핑
     */
    private val BC02_IMAGE_NODE_MAPPING = mapOf(
        // LONOVO_POST 이미지들
        4 to "Filecoin Miner",      // 1번 lonovopost
        5 to "3080Ti GPU Worker",    // 2번 lonovopost  
        6 to "Post Worker",          // 3번 lonovopost
        
        // STORAGE_1 이미지들
        9 to "NAS1",    // 첫 번째 STORAGE_1
        10 to "NAS2",   // 두 번째 STORAGE_1
        11 to "NAS3",   // 세 번째 STORAGE_1
        12 to "NAS4",   // 네 번째 STORAGE_1
        13 to "NAS5"    // 다섯 번째 STORAGE_1
    )
    
    /**
     * BC02 이미지별 표시 이름 매핑
     */
    private val BC02_DISPLAY_NAMES = mapOf(
        4 to "BC02 Filecoin Miner",
        5 to "BC02 3080Ti GPU Worker",
        6 to "BC02 Post Worker",
        9 to "BC02 NAS1",
        10 to "BC02 NAS2",
        11 to "BC02 NAS3",
        12 to "BC02 NAS4",
        13 to "BC02 NAS5"
    )
    
    /**
     * BC02 이미지 인덱스별 카테고리 매핑
     */
    private val BC02_CATEGORY_MAPPING = mapOf(
        // Node Miner 카테고리 (Filecoin Miner, 3080Ti GPU Worker)
        4 to BC02NodeCategory.NODE_MINER,
        5 to BC02NodeCategory.NODE_MINER,
        
        // PostWorker 카테고리 (Post Worker)
        6 to BC02NodeCategory.POST_WORKER,
        
        // NAS 카테고리 (NAS1-5)
        9 to BC02NodeCategory.NAS,
        10 to BC02NodeCategory.NAS,
        11 to BC02NodeCategory.NAS,
        12 to BC02NodeCategory.NAS,
        13 to BC02NodeCategory.NAS
    )
    
    /**
     * 이미지 인덱스와 타입에 따라 BC02의 대상 노드를 찾습니다.
     * 
     * @param imageType 이미지 타입
     * @param imageIndex 이미지 순서 인덱스
     * @param nodes 사용 가능한 노드 목록
     * @return 매칭되는 노드 또는 null
     */
    fun findBC02Node(
        imageType: ImageType,
        imageIndex: Int,
        nodes: List<Node>
    ): Node? {
        val targetNodeKeyword = BC02_IMAGE_NODE_MAPPING[imageIndex] ?: return null
        
        // 디버그 로깅
        android.util.Log.d("BC02DataMapper", "🔍 Finding BC02 node for imageIndex=$imageIndex")
        android.util.Log.d("BC02DataMapper", "   Target keyword: $targetNodeKeyword")
        android.util.Log.d("BC02DataMapper", "   Available nodes: ${nodes.map { it.nodeName }}")
        
        return when (imageType) {
            ImageType.LONOVO_POST -> {
                when (targetNodeKeyword) {
                    "Filecoin Miner" -> {
                        nodes.find { node ->
                            node.nodeName.contains("Filecoin", ignoreCase = true) && 
                            node.nodeName.contains("Miner", ignoreCase = true)
                        }
                    }
                    "3080Ti GPU Worker" -> {
                        nodes.find { node ->
                            node.nodeName.contains("3080Ti", ignoreCase = true) || 
                            node.nodeName.contains("GPU Worker", ignoreCase = true)
                        }
                    }
                    "Post Worker" -> {
                        nodes.find { node ->
                            node.nodeName.contains("Post Worker", ignoreCase = true)
                        }
                    }
                    else -> null
                }
            }
            ImageType.STORAGE_1 -> {
                // NAS 노드 찾기
                nodes.find { node ->
                    node.nodeName.contains(targetNodeKeyword, ignoreCase = true)
                }
            }
            else -> null
        }
    }
    
    /**
     * BC02 이미지의 표시 이름을 가져옵니다.
     * 
     * @param imageType 이미지 타입
     * @param imageIndex 이미지 순서 인덱스
     * @return 표시 이름 또는 기본 이름
     */
    fun getBC02DisplayName(
        imageType: ImageType,
        imageIndex: Int
    ): String? {
        return BC02_DISPLAY_NAMES[imageIndex]
    }
    
    /**
     * BC02 이미지 인덱스별 노드 카테고리를 가져옵니다.
     * 
     * @param imageIndex 이미지 순서 인덱스
     * @return BC02 노드 카테고리
     */
    fun getBC02NodeCategory(imageIndex: Int): BC02NodeCategory {
        return BC02_CATEGORY_MAPPING[imageIndex] ?: BC02NodeCategory.UNKNOWN
    }
    
    /**
     * 이미지 인덱스가 BC02 매핑 대상인지 확인합니다.
     * 
     * @param imageIndex 이미지 순서 인덱스
     * @return BC02 매핑 대상 여부
     */
    fun isBC02MappedImage(imageIndex: Int): Boolean {
        return BC02_IMAGE_NODE_MAPPING.containsKey(imageIndex)
    }
    
    /**
     * BC02 데이터 매핑 정보를 로그로 출력합니다.
     */
    fun logBC02MappingInfo() {
        android.util.Log.d("BC02DataMapper", "📋 BC02 Mapping Information:")
        BC02_IMAGE_NODE_MAPPING.forEach { (index, nodeName) ->
            val displayName = BC02_DISPLAY_NAMES[index]
            val category = BC02_CATEGORY_MAPPING[index]
            android.util.Log.d("BC02DataMapper", "   Index $index: $nodeName → $displayName (Category: $category)")
        }
    }
    
    /**
     * 노드 이름으로부터 BC02 섹터를 판별합니다.
     * 
     * @param nodeName 노드 이름 (예: "BC02 Post Worker", "BC02 Filecoin Miner" 등)
     * @return BC02 노드 카테고리
     */
    fun getBC02SectorFromNodeName(nodeName: String): BC02NodeCategory {
        return when {
            nodeName.contains("Post Worker", ignoreCase = true) -> BC02NodeCategory.POST_WORKER
            nodeName.contains("Filecoin", ignoreCase = true) && nodeName.contains("Miner", ignoreCase = true) -> BC02NodeCategory.NODE_MINER
            nodeName.contains("3080Ti", ignoreCase = true) || nodeName.contains("GPU Worker", ignoreCase = true) -> BC02NodeCategory.NODE_MINER
            nodeName.contains("NAS", ignoreCase = true) -> BC02NodeCategory.NAS
            else -> BC02NodeCategory.UNKNOWN
        }
    }
    
    /**
     * 노드가 BC02 데이터센터 소속인지 확인합니다.
     * 
     * @param nodeName 노드 이름
     * @return BC02 소속 여부
     */
    fun isBC02Node(nodeName: String): Boolean {
        return nodeName.contains("BC02", ignoreCase = true)
    }
}
