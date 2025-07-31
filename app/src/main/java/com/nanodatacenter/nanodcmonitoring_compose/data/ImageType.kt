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
    SUPRA(R.drawable.supra, "Supra"),
    SUPRA_NONE_1(R.drawable.supra_none, "Supra Inactive 1"),
    SUPRA_NONE_2(R.drawable.supra_none, "Supra Inactive 2"),
    SUPRA_NONE_3(R.drawable.supra_none, "Supra Inactive 3"),
    SYSTEMTOAI(R.drawable.systemtoai, "SystemToAI"),
    SYSTEMTOAI_NONE(R.drawable.systemtoai_none, "SystemToAI Inactive"),
    AETHIR(R.drawable.aethir, "Aethir"),
    AETHIR_NONE(R.drawable.aethir_none, "Aethir Inactive"),
    FILECOIN(R.drawable.filecoin, "Filecoin"),
    FILECOIN_NONE_1(R.drawable.filecoin_none, "Filecoin Inactive 1"),
    FILECOIN_NONE_2(R.drawable.filecoin_none, "Filecoin Inactive 2"),
    NOT_STORAGE(R.drawable.not_storage, "No Storage"),
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
