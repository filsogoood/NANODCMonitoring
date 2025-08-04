package com.nanodatacenter.nanodcmonitoring_compose.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 관리자 접근 기능을 관리하는 매니저 클래스
 * LOGO_ZETACUBE 클릭 시 관리자 팝업 표시 기능을 담당합니다.
 * 
 * 기능:
 * - 클릭 횟수 추적
 * - 3번 터치 후 토스트 메시지 표시
 * - 8번 클릭 시 관리자 팝업 표시
 */
class AdminAccessManager private constructor() {
    
    companion object {
        private const val REQUIRED_CLICKS_FOR_ADMIN = 8
        private const val TOAST_START_THRESHOLD = 3
        
        @Volatile
        private var INSTANCE: AdminAccessManager? = null
        
        fun getInstance(): AdminAccessManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdminAccessManager().also { INSTANCE = it }
            }
        }
    }
    
    // 클릭 횟수
    var clickCount by mutableIntStateOf(0)
        private set
    
    // 관리자 팝업 표시 상태
    var showAdminDialog by mutableStateOf(false)
        private set
    
    // 토스트 메시지 표시 상태
    var shouldShowToast by mutableStateOf(false)
        private set
    
    // 토스트 메시지 내용
    var toastMessage by mutableStateOf("")
        private set
    
    /**
     * LOGO_ZETACUBE 클릭 처리
     * 클릭 횟수를 증가시키고 필요에 따라 토스트나 관리자 팝업을 표시합니다.
     */
    fun handleLogoClick() {
        clickCount++
        
        when {
            clickCount >= REQUIRED_CLICKS_FOR_ADMIN -> {
                // 8번 클릭 시 관리자 팝업 표시
                showAdminDialog = true
                resetClickCount() // 관리자 팝업이 뜨면 카운트 리셋
            }
            clickCount >= TOAST_START_THRESHOLD -> {
                // 3번 터치 이후 토스트 표시
                val remainingClicks = REQUIRED_CLICKS_FOR_ADMIN - clickCount
                toastMessage = "Touch ${remainingClicks} again to display the administrator pop-up."
                shouldShowToast = true
            }
        }
    }
    
    /**
     * 관리자 팝업을 닫습니다.
     */
    fun dismissAdminDialog() {
        showAdminDialog = false
    }
    
    /**
     * 토스트 메시지를 표시했음을 알립니다.
     */
    fun onToastShown() {
        shouldShowToast = false
    }
    
    /**
     * 클릭 횟수를 리셋합니다.
     */
    fun resetClickCount() {
        clickCount = 0
    }
    
    /**
     * 현재 상태를 초기화합니다.
     */
    fun reset() {
        clickCount = 0
        showAdminDialog = false
        shouldShowToast = false
        toastMessage = ""
    }
    
    /**
     * 디버그용 - 현재 상태 정보를 반환합니다.
     */
    fun getDebugInfo(): String {
        return "AdminAccessManager - Clicks: $clickCount, Dialog: $showAdminDialog, Toast: $shouldShowToast"
    }
}
