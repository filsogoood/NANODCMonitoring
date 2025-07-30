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
    }
}
