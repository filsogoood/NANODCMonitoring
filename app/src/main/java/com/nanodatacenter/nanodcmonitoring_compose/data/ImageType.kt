package com.nanodatacenter.nanodcmonitoring_compose.data

import androidx.annotation.DrawableRes
import com.nanodatacenter.nanodcmonitoring_compose.R

/**
 * 모니터링 시스템에서 사용되는 이미지 타입을 정의하는 enum
 * 각 타입은 drawable 리소스와 매핑됩니다.
 */
enum class ImageType(@DrawableRes val drawableRes: Int, val description: String) {
    NDP_INFO(R.drawable.ndp_info, "NDP 정보"),
    NODE_INFO(R.drawable.node_info, "노드 정보"),
    ONBOARDING(R.drawable.onboarding, "온보딩"),
    SWITCH_100G(R.drawable.switch_100g, "100G 스위치"),
    NODE_MINER(R.drawable.node_miner, "노드 마이너"),
    POSTWORKER(R.drawable.postworker, "포스트워커"),
    SUPRA(R.drawable.supra, "수프라"),
    SUPRA_NONE_1(R.drawable.supra_none, "수프라 없음 1"),
    SUPRA_NONE_2(R.drawable.supra_none, "수프라 없음 2"),
    SUPRA_NONE_3(R.drawable.supra_none, "수프라 없음 3"),
    DEEPSEEK(R.drawable.deepseek, "딥시크"),
    DEEPSEEK_NONE(R.drawable.deepseek_none, "딥시크 없음"),
    AETHIR(R.drawable.aethir, "에테르"),
    AETHIR_NONE(R.drawable.aethir_none, "에테르 없음"),
    FILECOIN(R.drawable.filecoin, "파일코인"),
    FILECOIN_NONE_1(R.drawable.filecoin_none, "파일코인 없음 1"),
    FILECOIN_NONE_2(R.drawable.filecoin_none, "파일코인 없음 2"),
    NOT_STORAGE(R.drawable.not_storage, "스토리지 없음"),
    UPS_CONTROLLER(R.drawable.upscontroller, "UPS 컨트롤러"),
    LOGO_ZETACUBE(R.drawable.logo_zetacube, "제타큐브 로고");

    companion object {
        /**
         * drawable 리소스 ID로부터 ImageType을 찾습니다.
         */
        fun fromDrawableRes(@DrawableRes drawableRes: Int): ImageType? {
            return values().find { it.drawableRes == drawableRes }
        }
    }
}
