package com.nanodatacenter.monitorwebview

import android.content.Context
import android.util.DisplayMetrics

/**
 * 화면 크기에 따른 레이아웃 조정을 위한 유틸리티 클래스
 */
class ScreenAdaptiveLayout(val context: Context) {

    // 화면 너비 및 높이 정보
    private val displayMetrics: DisplayMetrics = context.resources.displayMetrics
    val screenWidth: Int = displayMetrics.widthPixels
    val screenHeight: Int = displayMetrics.heightPixels

    // 화면 크기 상태 계산
    val isNarrowScreen: Boolean = screenWidth < (400 * displayMetrics.density)
    val isVeryNarrowScreen: Boolean = screenWidth < (370 * displayMetrics.density)
    val isSmallScreen: Boolean = screenHeight < (700 * displayMetrics.density)

    // 화면 너비 단위
    val screenWidthDp: Float = screenWidth / displayMetrics.density
    val screenHeightDp: Float = screenHeight / displayMetrics.density

    // 원형 차트 크기 계산
    fun getCircleChartHeight(): Int {
        return when {
            isVeryNarrowScreen -> 140
            isNarrowScreen -> 160
            else -> 300
        }
    }

    // 텍스트 크기 계산
    fun getTextSize(baseSize: Float): Float {
        return when {
            isVeryNarrowScreen -> baseSize * 0.7f
            isNarrowScreen -> baseSize * 0.8f
            else -> baseSize
        }
    }

    // 패딩 계산
    fun getPadding(basePadding: Int): Int {
        return when {
            isVeryNarrowScreen -> (basePadding * 0.6f).toInt()
            isNarrowScreen -> (basePadding * 0.8f).toInt()
            else -> basePadding
        }
    }

    // 그래프 타입 결정 (좁은 화면에서는 막대 그래프 추천)
    fun shouldUseBarInsteadOfCircle(): Boolean {
        return isVeryNarrowScreen
    }

    // 원 크기 비율 계산
    fun getCircleScaleFactor(): Float {
        return when {
            isVeryNarrowScreen -> 0.5f
            isNarrowScreen -> 0.65f
            else -> 1.0f
        }
    }

    // 가로/세로 결정
    fun getOrientation(): Int {
        return if (isNarrowScreen) android.widget.LinearLayout.VERTICAL else android.widget.LinearLayout.HORIZONTAL
    }
}