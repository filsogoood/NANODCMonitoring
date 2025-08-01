package com.nanodatacenter.nanodcmonitoring_compose.data

/**
 * 기기별 이미지 순서 설정을 관리하는 데이터 클래스
 * TODO: 향후 확장 시 이미지별 스케일 설정도 포함할 수 있도록 구조 설계
 */
data class ImageConfiguration(
    val deviceType: DeviceType,
    val imageOrder: List<ImageType>
) {
    companion object {
        /**
         * 기본 이미지 순서 (GY01 기본 설정)
         * 
         * 참고: 다음 이미지들은 ImageScaleUtil에서 90% 스케일이 적용됨:
         * - SYSTEMTOAI
         * - SYSTEMTOAI_NONE
         * - AETHIR
         * - AETHIR_NONE
         * - FILECOIN
         * - FILECOIN_NONE_1
         * - FILECOIN_NONE_2
         */
        val DEFAULT_ORDER = listOf(
            ImageType.NDP_INFO,
            ImageType.NODE_INFO,
            ImageType.NODE_INFO_AETHIR,
            ImageType.SWITCH_100G,
            ImageType.NODE_MINER,
            ImageType.POSTWORKER,
            ImageType.SUPRA,
            ImageType.SUPRA_NONE_1,
            ImageType.SUPRA_NONE_2,
            ImageType.SUPRA_NONE_3,
            ImageType.SYSTEMTOAI,
            ImageType.SYSTEMTOAI_NONE,
            ImageType.AETHIR,
            ImageType.AETHIR_NONE,
            ImageType.FILECOIN,
            ImageType.FILECOIN_NONE_1,
            ImageType.FILECOIN_NONE_2,
            ImageType.NOT_STORAGE,
            ImageType.UPS_CONTROLLER,
            ImageType.LOGO_ZETACUBE
        )

        /**
         * BC01 데이터센터 전용 이미지 순서
         * 
         * 요구사항:
         * - NDP_INFO: ndp_info.png
         * - NODE_INFO: node_info.jpg
         * - NODE_INFO_AETHIR: node_info_aethir.png
         * - SWITCH_100G: switch_100g.png [클릭 불가]
         * - SYSTEMTOAI: systemtoai_none.png [90% 스케일]
         * - AETHIR: aethir_none.png [90% 스케일]
         * - FILECOIN: filecoin_none.png [90% 스케일]
         * - STORAGE 1~5: storage2.png (5개)
         * - STORAGE2_NONE: storage2_none.png [클릭 불가]
         * - NODE_MINER: node_miner.jpg
         * - UPS_CONTROLLER: upscontroller.jpg [클릭 불가]
         * - LOGO_ZETACUBE: logo_zetacube.jpg [관리자 접근]
         */
        val BC01_ORDER = listOf(
            ImageType.NDP_INFO,           // 1. NDP 정보
            ImageType.NODE_INFO,          // 2. 노드 정보
            ImageType.NODE_INFO_AETHIR,   // 3. 노드 정보 에테르
            ImageType.SWITCH_100G,        // 4. 100G 스위치 [클릭 불가]
            ImageType.SYSTEMTOAI,         // 5. 시스템투AI [90% 스케일]
            ImageType.AETHIR,             // 6. 에테르 [90% 스케일] - aethir_none.png
            ImageType.FILECOIN,           // 7. 파일코인 [90% 스케일]
            ImageType.STORAGE_1,          // 8. 스토리지 1
            ImageType.STORAGE_2,          // 9. 스토리지 2
            ImageType.NODE_MINER,         // 10. 노드 마이너
            ImageType.STORAGE_3,          // 11. 스토리지 3
            ImageType.STORAGE_4,          // 12. 스토리지 4
            ImageType.STORAGE_5,          // 13. 스토리지 5
            ImageType.STORAGE2_NONE,      // 14. 스토리지 2 없음 - storage2_none.png
            ImageType.UPS_CONTROLLER,     // 15. UPS 컨트롤러 [클릭 불가]
            ImageType.LOGO_ZETACUBE       // 16. 제타큐브 로고 [관리자 접근]
        )

        /**
         * BC02 데이터센터 전용 이미지 순서
         * 
         * 요구사항:
         * - NDP_INFO: NDP 정보 (ndp_info.png)
         * - NODE_INFO: 노드 정보 (node_info.jpg)
         * - NODE_INFO_AETHIR: 노드 정보 에테르 (node_info_aethir.png)
         * - SWITCH_100G: 100G 스위치 (switch_100g.png) [클릭 불가]
         * - LONOVO_POST: 포스트워커 (lonovo_post.png) - 3개
         * - NODE_MINER: 노드 마이너 (node_miner.jpg)
         * - UPS_CONTROLLER: UPS 컨트롤러 (upscontroller.jpg) [클릭 불가]
         * - STORAGE 1~5: storage2.png (5개)
         * - LOGO_ZETACUBE: 제타큐브 로고 (logo_zetacube.jpg) [관리자 접근]
         */
        val BC02_ORDER = listOf(
            ImageType.NDP_INFO,           // 1. NDP 정보
            ImageType.NODE_INFO,          // 2. 노드 정보
            ImageType.NODE_INFO_AETHIR,   // 3. 노드 정보 에테르
            ImageType.SWITCH_100G,        // 4. 100G 스위치 [클릭 불가]
            ImageType.LONOVO_POST,        // 5. 포스트워커 1
            ImageType.LONOVO_POST,        // 6. 포스트워커 2
            ImageType.LONOVO_POST,        // 7. 포스트워커 3
            ImageType.NODE_MINER,         // 8. 노드 마이너
            ImageType.UPS_CONTROLLER,     // 9. UPS 컨트롤러 [클릭 불가]
            ImageType.STORAGE_1,          // 10. 스토리지 1
            ImageType.STORAGE_1,          // 11. 스토리지 2
            ImageType.STORAGE_1,          // 12. 스토리지 3
            ImageType.STORAGE_1,          // 13. 스토리지 4
            ImageType.STORAGE_1,          // 14. 스토리지 5
            ImageType.LOGO_ZETACUBE       // 15. 제타큐브 로고 [관리자 접근]
        )

        /**
         * 기본 설정을 생성합니다.
         */
        fun createDefault(deviceType: DeviceType = DeviceType.DEFAULT): ImageConfiguration {
            return ImageConfiguration(deviceType, DEFAULT_ORDER)
        }

        /**
         * BC01 데이터센터 전용 설정을 생성합니다.
         */
        fun createBC01(): ImageConfiguration {
            return ImageConfiguration(DeviceType.BC01, BC01_ORDER)
        }

        /**
         * BC02 데이터센터 전용 설정을 생성합니다.
         */
        fun createBC02(): ImageConfiguration {
            return ImageConfiguration(DeviceType.BC02, BC02_ORDER)
        }

        /**
         * 데이터센터 타입에 따라 적절한 이미지 순서를 반환합니다.
         */
        fun getOrderForDataCenter(dataCenterName: String): List<ImageType> {
            return when (dataCenterName.uppercase()) {
                "BC01" -> BC01_ORDER
                "BC02" -> BC02_ORDER
                "GY01" -> DEFAULT_ORDER
                else -> DEFAULT_ORDER
            }
        }
    }
}

/**
 * 기기 타입을 정의하는 enum
 * 추후 다른 기기별로 다른 순서가 필요할 때 사용
 */
enum class DeviceType(val displayName: String) {
    DEFAULT("기본"),
    BC01("BC01"),
    BC02("BC02"),
    GY01("GY01"),
    DEVICE_A("기기 A"),
    DEVICE_B("기기 B"),
    DEVICE_C("기기 C");
    
    companion object {
        fun fromString(value: String): DeviceType {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: DEFAULT
        }

        /**
         * 데이터센터 이름으로부터 DeviceType을 가져옵니다.
         */
        fun fromDataCenterName(dataCenterName: String): DeviceType {
            return when (dataCenterName.uppercase()) {
                "BC01" -> BC01
                "BC02" -> BC02
                "GY01" -> GY01
                else -> DEFAULT
            }
        }
    }
}
