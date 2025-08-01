package com.nanodatacenter.nanodcmonitoring_compose.data

import androidx.annotation.DrawableRes
import com.nanodatacenter.nanodcmonitoring_compose.R

/**
 * 모니터링 시스템에서 사용되는 이미지 타입을 정의하는 enum
 * 각 타입은 drawable 리소스와 매핑됩니다.
 */
enum class ImageType(@DrawableRes val drawableRes: Int, val description: String) {
    NDP_INFO(R.drawable.ndp_info, "NDP Information"),
    NODE_INFO(R.drawable.node_info, "Node Information"),
    NODE_INFO_AETHIR(R.drawable.node_info_aethir, "Node Info Aethir"),
    SWITCH_100G(R.drawable.switch_100g, "100G Switch"),
    NODE_MINER(R.drawable.node_miner, "Node Miner"),
    POSTWORKER(R.drawable.postworker, "Post Worker"),
    LONOVO_POST(R.drawable.lonovo_post, "Lonovo Post Worker"),
    SUPRA(R.drawable.supra, "Supra"),
    SUPRA_NONE_1(R.drawable.supra_none, "Supra Inactive 1"),
    SUPRA_NONE_2(R.drawable.supra_none, "Supra Inactive 2"),
    SUPRA_NONE_3(R.drawable.supra_none, "Supra Inactive 3"),
    SYSTEMTOAI(R.drawable.systemtoai_none, "SystemToAI"),
    SYSTEMTOAI_NONE(R.drawable.systemtoai_none, "SystemToAI Inactive"),
    AETHIR(R.drawable.aethir_none, "Aethir"),
    AETHIR_NONE(R.drawable.aethir_none, "Aethir Inactive"),
    FILECOIN(R.drawable.filecoin_none, "Filecoin"),
    FILECOIN_NONE_1(R.drawable.filecoin_none, "Filecoin Inactive 1"),
    FILECOIN_NONE_2(R.drawable.filecoin_none, "Filecoin Inactive 2"),
    NOT_STORAGE(R.drawable.not_storage, "Storage"),
    STORAGE_1(R.drawable.storage2, "Storage 1"),
    STORAGE_2(R.drawable.storage2, "Storage 2"),
    STORAGE_3(R.drawable.storage2, "Storage 3"),
    STORAGE_4(R.drawable.storage2, "Storage 4"),
    STORAGE_5(R.drawable.storage2, "Storage 5"),
    STORAGE_6(R.drawable.storage2, "Storage 6"),
    STORAGE2_NONE(R.drawable.storage2_none, "Storage 2 None"),
    UPS_CONTROLLER(R.drawable.upscontroller, "UPS Controller"),
    LOGO_ZETACUBE(R.drawable.logo_zetacube, "ZetaCube Logo");

    companion object {
        /**
         * drawable 리소스 ID로부터 ImageType을 찾습니다.
         */
        fun fromDrawableRes(@DrawableRes drawableRes: Int): ImageType? {
            return values().find { it.drawableRes == drawableRes }
        }
        
        /**
         * 클릭 시 일반 카드를 표시하지 않을 이미지 타입들
         * None이 붙은 이미지들, 100G Switch, UPS Controller는 클릭해도 카드가 나오지 않음
         * LOGO_ZETACUBE는 특별한 관리자 접근 기능을 제공
         */
        private val NON_CLICKABLE_TYPES = setOf(
            SUPRA_NONE_1,
            SUPRA_NONE_2,
            SUPRA_NONE_3,
            SYSTEMTOAI_NONE,
            AETHIR_NONE,
            FILECOIN_NONE_1,
            FILECOIN_NONE_2,
            STORAGE2_NONE,
            SWITCH_100G,
            UPS_CONTROLLER
        )
        
        /**
         * 관리자 접근 기능을 제공하는 이미지 타입들
         * 8번 클릭 시 관리자 팝업을 표시합니다.
         */
        private val ADMIN_ACCESS_TYPES = setOf(
            LOGO_ZETACUBE
        )
    }
    
    
    /**
     * 해당 이미지 타입이 클릭 가능한지 확인합니다.
     * None이 붙은 이미지들, 100G Switch, UPS Controller는 클릭 불가능합니다.
     * 위치가 변경되어도 타입 기반으로 판단하므로 일관성이 보장됩니다.
     */
    val isClickable: Boolean
        get() = this !in NON_CLICKABLE_TYPES
    
    /**
     * 해당 이미지 타입이 관리자 접근 기능을 제공하는지 확인합니다.
     * LOGO_ZETACUBE는 8번 클릭 시 관리자 팝업을 표시합니다.
     */
    val isAdminAccess: Boolean
        get() = this in ADMIN_ACCESS_TYPES
        
    /**
     * 해당 이미지 타입이 일반 정보 카드를 표시하는지 확인합니다.
     * 클릭 가능하면서 관리자 접근 타입이 아닌 경우에만 일반 카드를 표시합니다.
     */
    val showsInfoCard: Boolean
        get() = isClickable && !isAdminAccess
}

/**
 * 데이터센터 종류 enum
 */
enum class DataCenterType(
    val displayName: String,
    val nanoDcId: String
) {
    BC01("BC01", "dcf1bb07-f621-4b4d-9d61-45fc3cf5ac20"),
    BC02("BC02", "5e807a27-7c3a-4a22-8df2-20c392186ed3"),
    GY01("GY01", "c236ea9c-3d7e-430b-98b8-1e22d0d6cf01");
    
    companion object {
        /**
         * nanoDcId로 DataCenterType 찾기
         */
        fun fromNanoDcId(nanoDcId: String): DataCenterType? {
            return entries.find { it.nanoDcId == nanoDcId }
        }
        
        /**
         * displayName으로 DataCenterType 찾기
         */
        fun fromDisplayName(displayName: String): DataCenterType? {
            return entries.find { it.displayName == displayName }
        }
    }
}

/**
 * 데이터센터 상태 정보
 */
data class DataCenterState(
    val selectedCenter: DataCenterType = DataCenterType.GY01,
    val isLoading: Boolean = false,
    val loadingCenter: DataCenterType? = null
)
