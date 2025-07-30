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
    ONBOARDING(R.drawable.onboarding, "Onboarding"),
    SWITCH_100G(R.drawable.switch_100g, "100G Switch"),
    NODE_MINER(R.drawable.node_miner, "Node Miner"),
    POSTWORKER(R.drawable.postworker, "Post Worker"),
    SUPRA(R.drawable.supra, "Supra"),
    SUPRA_NONE_1(R.drawable.supra_none, "Supra Inactive 1"),
    SUPRA_NONE_2(R.drawable.supra_none, "Supra Inactive 2"),
    SUPRA_NONE_3(R.drawable.supra_none, "Supra Inactive 3"),
    DEEPSEEK(R.drawable.deepseek, "DeepSeek"),
    DEEPSEEK_NONE(R.drawable.deepseek_none, "DeepSeek Inactive"),
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
         * 클릭 시 카드를 표시하지 않을 이미지 타입들
         * None이 붙은 이미지들, 100G Switch, UPS Controller, ZetaCube Logo는 클릭해도 카드가 나오지 않음
         */
        private val NON_CLICKABLE_TYPES = setOf(
            SUPRA_NONE_1,
            SUPRA_NONE_2,
            SUPRA_NONE_3,
            DEEPSEEK_NONE,
            AETHIR_NONE,
            FILECOIN_NONE_1,
            FILECOIN_NONE_2,
            SWITCH_100G,
            UPS_CONTROLLER,
            LOGO_ZETACUBE
        )
    }
    
    /**
     * 해당 이미지 타입이 클릭 가능한지 확인합니다.
     * None이 붙은 이미지들, 100G Switch, UPS Controller, ZetaCube Logo는 클릭 불가능합니다.
     * 위치가 변경되어도 타입 기반으로 판단하므로 일관성이 보장됩니다.
     */
    val isClickable: Boolean
        get() = this !in NON_CLICKABLE_TYPES
}
