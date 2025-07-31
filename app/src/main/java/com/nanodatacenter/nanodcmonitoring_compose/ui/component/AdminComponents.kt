package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.nanodatacenter.nanodcmonitoring_compose.data.DataCenterType
import com.nanodatacenter.nanodcmonitoring_compose.config.DeviceConfigurationManager
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 관리자 접근 팝업 다이얼로그
 * LOGO_ZETACUBE를 8번 클릭했을 때 표시됩니다.
 * 
 * @param isVisible 다이얼로그 표시 여부
 * @param onDismiss 다이얼로그 닫기 콜백
 * @param onAdminAccess 관리자 접근 콜백 (추후 확장 가능)
 */
@Composable
fun AdminAccessDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onAdminAccess: (() -> Unit)? = null,
    onDataCenterChanged: ((DataCenterType) -> Unit)? = null
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            AdminDialogContent(
                onDismiss = onDismiss,
                onAdminAccess = onAdminAccess,
                onDataCenterChanged = onDataCenterChanged
            )
        }
    }
}

/**
 * 관리자 다이얼로그의 내용 컴포넌트
 */
@Composable
private fun AdminDialogContent(
    onDismiss: () -> Unit,
    onAdminAccess: (() -> Unit)? = null,
    onDataCenterChanged: ((DataCenterType) -> Unit)? = null
) {
    var showDataCenterDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 제목
            Text(
                text = "Admin Access",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            // 설명 텍스트
            Text(
                text = "Administrator functions are available.\nPlease select the required management tasks.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 버튼 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 닫기 버튼
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = "Close",
                        fontSize = 14.sp
                    )
                }
                
                // 데이터센터 선택 버튼
                Button(
                    onClick = { showDataCenterDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Data Center",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
    
    // 데이터센터 선택 다이얼로그
    if (showDataCenterDialog) {
        DataCenterSelectionDialog(
            onDismiss = { showDataCenterDialog = false },
            onDataCenterSelected = { dataCenter ->
                showDataCenterDialog = false
                onDataCenterChanged?.invoke(dataCenter)
                onDismiss() // 관리자 다이얼로그도 닫기
            }
        )
    }
}

/**
 * 데이터센터 선택 다이얼로그
 */
@Composable
fun DataCenterSelectionDialog(
    onDismiss: () -> Unit,
    onDataCenterSelected: (DataCenterType) -> Unit
) {
    val context = LocalContext.current
    val deviceConfigManager = remember { DeviceConfigurationManager.getInstance(context) }
    val repository = remember { NanoDcRepository.getInstance() }
    
    // 다이얼로그가 열릴 때마다 최신 데이터센터 정보 가져오기
    var selectedCenter by remember { mutableStateOf(DataCenterType.GY01) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingCenter by remember { mutableStateOf<DataCenterType?>(null) }
    
    // 다이얼로그가 열릴 때마다 현재 설정된 데이터센터로 업데이트
    LaunchedEffect(Unit) {
        selectedCenter = deviceConfigManager.getSelectedDataCenter()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 제목
                Text(
                    text = "Select Data Center",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                // 현재 선택된 데이터센터 표시
                Text(
                    text = "Currently: ${selectedCenter.displayName}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                // 데이터센터 버튼들
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DataCenterType.entries.forEach { dataCenter ->
                        DataCenterButton(
                            dataCenter = dataCenter,
                            isSelected = selectedCenter == dataCenter,
                            isLoading = isLoading && loadingCenter == dataCenter,
                            isDisabled = isLoading,
                                                         onClick = {
                                 if (!isLoading) {
                                     selectedCenter = dataCenter
                                     isLoading = true
                                     loadingCenter = dataCenter
                                     
                                     // 실제 API 테스트 및 설정 저장
                                     MainScope().launch {
                                         try {
                                             // API 연결 테스트
                                             repository.testApiConnection(dataCenter.nanoDcId)
                                             
                                             // 설정 저장
                                             deviceConfigManager.setSelectedDataCenter(dataCenter)
                                             
                                             // 성공적으로 완료
                                             isLoading = false
                                             loadingCenter = null
                                             onDataCenterSelected(dataCenter)
                                             
                                         } catch (e: Exception) {
                                             // 실패 시에도 설정은 저장하고 사용자에게 알림
                                             android.util.Log.e("DataCenterSelection", "API test failed for ${dataCenter.displayName}: ${e.message}")
                                             deviceConfigManager.setSelectedDataCenter(dataCenter)
                                             
                                             isLoading = false
                                             loadingCenter = null
                                             onDataCenterSelected(dataCenter)
                                         }
                                     }
                                 }
                             }
                        )
                    }
                }
                
                // 로딩 중일 때 메시지
                if (isLoading && loadingCenter != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Loading data from ${loadingCenter!!.displayName}...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // 취소 버튼
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * 데이터센터 선택 버튼
 */
@Composable
private fun DataCenterButton(
    dataCenter: DataCenterType,
    isSelected: Boolean,
    isLoading: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isDisabled && !isLoading -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isDisabled && !isLoading -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Button(
        onClick = onClick,
        enabled = !isDisabled,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dataCenter.displayName,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = contentColor
                    )
                }
                isSelected -> {
                    Text(
                        text = "●",
                        fontSize = 20.sp,
                        color = contentColor
                    )
                }
            }
        }
    }
}

/**
 * 간단한 관리자 정보 카드 (미래 확장을 위한 컴포넌트)
 * 관리자 메뉴나 설정 화면으로 확장할 수 있습니다.
 */
@Composable
fun AdminInfoCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickableCard { onClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * 클릭 가능한 카드를 위한 확장 함수
 */
private fun Modifier.clickableCard(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable { onClick() }
    )
}
