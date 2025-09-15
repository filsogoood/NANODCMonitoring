package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.R
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

/**
 * Aethir 노드 관련 데이터 클래스들
 */

/**
 * Aethir 지갑 정보 데이터 클래스
 */
data class AethirWalletInfo(
    val claimableServiceFee: String = "0.00",
    val claimablePocPodRewards: String = "158.75",
    val withdrawable: String = "0.0000",
    val vestingClaim: String = "88173.1976",
    val claimable: String = "15869.7551",
    val vestingWithdraw: String = "0.0000",
    val withdrawableAmount: String = "0.0000",
    val cashOutTotal: String = "149372.4039",
    val staked: String = "209542.8",
    val unstaking: String = "224115.8",
    val unstaked: String = "0.0000"
)

/**
 * BC01 전용 Aethir 지갑 정보 데이터 클래스
 */
data class BC01AethirWalletInfo(
    val vestingClaim: String = "18424.20",
    val claimable: String = "12406.76",
    val cashOutTotal: String = "128000.40",
    val staking: String = "72000.72",
    val unstaking: String = "221000.48",
    val reward: String = "386.07"
)

/**
 * 데이터센터별 Aethir 지갑 정보를 위한 통합 데이터 클래스
 */
data class DataCenterAethirWalletInfo(
    val vestingClaim: String,
    val claimable: String,
    val cashOutTotal: String,
    val staking: String,
    val unstaking: String,
    val reward: String
)

/**
 * 데이터센터별 Aethir 지갑 정보를 가져오는 함수
 */
fun getAethirWalletInfoForDataCenter(isBC01: Boolean): DataCenterAethirWalletInfo {
    return if (isBC01) {
        // BC01 데이터
        DataCenterAethirWalletInfo(
            vestingClaim = "18424.20",
            claimable = "12406.76", 
            cashOutTotal = "128000.40",
            staking = "72000.72",
            unstaking = "221000.48",
            reward = "386.07"
        )
    } else {
        // BC02 및 다른 데이터센터 수치
        DataCenterAethirWalletInfo(
            vestingClaim = "7403.95",
            claimable = "5747.79",
            cashOutTotal = "56089.60", 
            staking = "35114.40",
            unstaking = "91250.90",
            reward = "171.93"
        )
    }
}

/**
 * Aethir 대시보드 정보 데이터 클래스
 */
data class AethirDashboardInfo(
    val totalLocations: String = "1.0000",
    val totalServers: String = "205296.0000",
    val myAethirEarth: String = "5.0000",
    val myAethirAtmosphere: String = "0.0000",
    val totalRewards: String = "1032.7113",
    val pocRewards: String = "252140.3918",
    val podRewards: String = "242.2535"
)

/**
 * Aethir 수입 정보 데이터 클래스
 */
data class AethirIncomeInfo(
    val date: String = "2025-07-30",
    val serviceFee: String = "0.0000",
    val pocReward: String = "645.3498",
    val podReward: String = "0.0000",
    val totalDaily: String = "645.3498"
)

/**
 * 통합 Aethir 노드 정보 데이터 클래스
 */
data class AethirNodeInfo(
    val nodeName: String = "Aethir Node",
    val walletInfo: AethirWalletInfo = AethirWalletInfo(),
    val dashboardInfo: AethirDashboardInfo = AethirDashboardInfo(),
    val incomeInfo: AethirIncomeInfo = AethirIncomeInfo(),
    val isOnline: Boolean = true
)

/**
 * Aethir 노드 이미지와 정보를 표시하는 메인 컴포넌트
 */
@Composable
fun AethirNodeImageWithInfo(
    aethirNodeInfo: AethirNodeInfo,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 노드 상태에 따라 이미지 선택
    val imageType = if (aethirNodeInfo.isOnline) {
        ImageType.AETHIR
    } else {
        ImageType.AETHIR_NONE
    }
    
    Column(modifier = modifier) {
        // Aethir 이미지 표시 (클릭 가능)
        Image(
            painter = painterResource(id = imageType.drawableRes),
            contentDescription = aethirNodeInfo.nodeName,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (imageType.isClickable) {
                        isExpanded = !isExpanded 
                    }
                },
            contentScale = ContentScale.FillWidth
        )
        
        // 확장 정보 카드
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            AethirNodeInfoCard(aethirNodeInfo = aethirNodeInfo)
        }
    }
}

/**
 * Aethir 노드 정보를 표시하는 통합 카드
 */
@Composable
fun AethirNodeInfoCard(
    aethirNodeInfo: AethirNodeInfo? = null,
    isBC01: Boolean = false,
    modifier: Modifier = Modifier
) {
    val nodeInfo = aethirNodeInfo ?: if (isBC01) {
        AethirNodeInfo(
            nodeName = "BC01 Aethir Node",
            walletInfo = AethirWalletInfo(),
            dashboardInfo = AethirDashboardInfo(),
            incomeInfo = AethirIncomeInfo(),
            isOnline = true
        )
    } else {
        AethirNodeInfo()
    }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 노드 이름 카드
        val nodeName = if (isBC01) "BC01 Aethir Node" else "Aethir Node"
        AethirNodeNameCard(nodeName = nodeName)
        
        // 모든 데이터센터에서 지갑 밸런스 카드와 스테이킹 정보 카드 표시
        val walletInfo = getAethirWalletInfoForDataCenter(isBC01)
        AethirWalletBalanceCard(walletInfo = walletInfo)
        AethirStakingInfoCard(walletInfo = walletInfo)
        
        // 대시보드 정보 카드 - 삭제됨
        // AethirDashboardCard(dashboardInfo = nodeInfo.dashboardInfo)
        
        // 수입 정보 카드 - 삭제됨
        // AethirIncomeCard(incomeInfo = nodeInfo.incomeInfo)
    }
}

/**
 * Aethir 노드 이름을 표시하는 카드
 */
@Composable
private fun AethirNodeNameCard(
    nodeName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.CloudDownload,
                    contentDescription = "Aethir Node",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = nodeName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Aethir 지갑 정보를 표시하는 카드
 */
@Composable
private fun AethirWalletCard(
    walletInfo: AethirWalletInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .background(
                            Color(0xFFFBBF24),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wallet Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 클레임 가능한 금액들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirTokenCard(
                    title = "CLAIMABLE - SERVICE FEE",
                    amount = walletInfo.claimableServiceFee,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenCard(
                    title = "CLAIMABLE - POC & POD REWARDS",
                    amount = walletInfo.claimablePocPodRewards,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenCard(
                    title = "WITHDRAWABLE",
                    amount = walletInfo.withdrawable,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 진행 막대 (예시로 표시)
            AethirProgressBar(walletInfo = walletInfo)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 하단 정보들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirTokenCard(
                    title = "VESTING CLAIM",
                    amount = walletInfo.vestingClaim,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenCard(
                    title = "VESTING WITHDRAW",
                    amount = walletInfo.vestingWithdraw,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenCard(
                    title = "CASH OUT TOTAL",
                    amount = walletInfo.cashOutTotal,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 스테이킹 정보 - 가로 배치 및 강조 표시
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 스테이킹 정보 헤더
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(20.dp)
                                .background(
                                    Color(0xFFFBBF24),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "STAKING INFO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = "Staking",
                            tint = Color(0xFFFBBF24),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 스테이킹 데이터 가로 배치
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AethirStakingCard(
                            title = "STAKED",
                            amount = walletInfo.staked,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AethirStakingCard(
                            title = "UNSTAKING",
                            amount = walletInfo.unstaking,
                            color = Color(0xFFF59E0B),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        AethirStakingCard(
                            title = "UNSTAKED",
                            amount = walletInfo.unstaked,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 데이터센터별 지갑 밸런스 카드 - 원형 그래프로 변경
 */
@Composable
private fun AethirWalletBalanceCard(
    walletInfo: DataCenterAethirWalletInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .background(
                            Color(0xFFFBBF24),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wallet Balance",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 원형 그래프와 범례를 함께 표시
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 원형 그래프
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AethirWalletPieChart(walletInfo = walletInfo)
                }
                
                // 범례
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AethirWalletLegendItem(
                        title = "VESTING CLAIM",
                        amount = walletInfo.vestingClaim,
                        color = Color(0xFF10B981)
                    )
                    AethirWalletLegendItem(
                        title = "CLAIMABLE",
                        amount = walletInfo.claimable,
                        color = Color(0xFFFBBF24)
                    )
                    AethirWalletLegendItem(
                        title = "CASH OUT",
                        amount = walletInfo.cashOutTotal,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

/**
 * Aethir 지갑 원형 그래프
 */
@Composable
private fun AethirWalletPieChart(
    walletInfo: DataCenterAethirWalletInfo,
    modifier: Modifier = Modifier
) {
    // 데이터 값 변환
    val vestingClaimValue = walletInfo.vestingClaim.toDoubleOrNull() ?: 0.0
    val claimableValue = walletInfo.claimable.toDoubleOrNull() ?: 0.0
    val cashOutValue = walletInfo.cashOutTotal.toDoubleOrNull() ?: 0.0
    
    val totalValue = vestingClaimValue + claimableValue + cashOutValue
    
    // 각 섹션의 각도 계산
    val vestingClaimAngle = if (totalValue > 0) ((vestingClaimValue / totalValue) * 360f).toFloat() else 0f
    val claimableAngle = if (totalValue > 0) ((claimableValue / totalValue) * 360f).toFloat() else 0f
    val cashOutAngle = if (totalValue > 0) ((cashOutValue / totalValue) * 360f).toFloat() else 0f
    
    Box(
        modifier = modifier.size(140.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = (size.minDimension / 2) * 0.8f
            val strokeWidth = 20.dp.toPx()
            
            var currentAngle = -90f // 12시 방향부터 시작
            
            if (totalValue > 0) {
                // Vesting Claim 섹션
                if (vestingClaimAngle > 0) {
                    drawArc(
                        color = Color(0xFF10B981),
                        startAngle = currentAngle,
                        sweepAngle = vestingClaimAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    currentAngle += vestingClaimAngle
                }
                
                // Claimable 섹션
                if (claimableAngle > 0) {
                    drawArc(
                        color = Color(0xFFFBBF24),
                        startAngle = currentAngle,
                        sweepAngle = claimableAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                    currentAngle += claimableAngle
                }
                
                // Cash Out 섹션
                if (cashOutAngle > 0) {
                    drawArc(
                        color = Color(0xFFEF4444),
                        startAngle = currentAngle,
                        sweepAngle = cashOutAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2)
                    )
                }
            } else {
                // 데이터가 없을 때 회색 원 표시
                drawArc(
                    color = Color(0xFF374151),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }
        }
        
        // 중앙 텍스트
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOTAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "${String.format("%.1f", totalValue)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "ATH",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFBBF24)
            )
        }
    }
}

/**
 * Aethir 지갑 범례 항목
 */
@Composable
private fun AethirWalletLegendItem(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 색상 표시 점
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(6.dp))
        )
        
        // 제목과 금액
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "$amount ATH",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

/**
 * 데이터센터별 스테이킹 정보 카드
 */
@Composable
private fun AethirStakingInfoCard(
    walletInfo: DataCenterAethirWalletInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 스테이킹 정보 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(24.dp)
                        .background(
                            Color(0xFFFBBF24),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "STAKING INFO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Savings,
                    contentDescription = "Staking",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 스테이킹 데이터 가로 배치 - 균등한 간격과 크기로 조정
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 균등한 간격 적용
            ) {
                AethirStakingCard(
                    title = "STAKING",
                    amount = walletInfo.staking,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                AethirStakingCard(
                    title = "UNSTAKING", 
                    amount = walletInfo.unstaking,
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
                AethirStakingCard(
                    title = "REWARD",
                    amount = walletInfo.reward,
                    color = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


/**
 * Aethir 스테이킹 정보를 표시하는 개별 카드 (확장된 버전)
 */
@Composable
private fun AethirStakingCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp), // 고정 높이로 모든 박스 크기 통일
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF111827)
        // 박스선 제거 - border 속성 삭제
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 스테이킹 상태 아이콘
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(5.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // 제목 - 텍스트 크기 2pt 줄임 (11sp → 9sp)
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 2, // 최대 2줄까지 허용
                lineHeight = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            
            // 금액 - 텍스트 크기 2pt 줄임 (14sp → 12sp)
            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2 // 긴 숫자의 경우 줄바꿈 허용
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // ATH 단위 - 텍스트 크기 2pt 줄임 (10sp → 8sp)
            Text(
                text = "ATH",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Aethir 토큰 금액을 표시하는 개별 카드
 */
@Composable
private fun AethirTokenCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp), // 고정 높이로 모든 박스 크기 통일
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 9.sp, // 폰트 크기 줄임
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 3, // 최대 3줄까지 허용
                lineHeight = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$amount ATH",
                fontSize = 11.sp, // 폰트 크기 줄임
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2 // 긴 숫자의 경우 줄바꿈 허용
            )
        }
    }
}

/**
 * Aethir 리워드 금액을 표시하는 카드
 */
@Composable
private fun AethirRewardCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amount ATH",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF10B981),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Aethir 리소스 정보를 표시하는 카드
 */
@Composable
private fun AethirResourceCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Aethir 수입 항목을 표시하는 카드
 */
@Composable
private fun AethirIncomeItemCard(
    title: String,
    amount: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isHighlight) Color(0xFF8B5CF6) else Color(0xFF374151)
    val textColor = if (isHighlight) Color(0xFF8B5CF6) else Color.White
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amount ATH",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Aethir 지갑 정보의 진행 막대 (예시)
 */
@Composable
private fun AethirProgressBar(
    walletInfo: AethirWalletInfo,
    modifier: Modifier = Modifier
) {
    // 간단한 진행 막대 표시 (실제 데이터 기반으로 구성 가능)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        color = Color(0xFF374151)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            // Vesting Claim 부분 (예시로 35% 표시)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
                    .background(Color(0xFF10B981), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            )
            // Claimable 부분 (예시로 20% 표시)  
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.20f)
                    .background(Color(0xFFFBBF24))
            )
            // Vesting Withdraw 부분 (예시로 0% 표시)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.0f)
                    .background(Color(0xFF8B5CF6))
            )
            // Withdrawable 부분 (예시로 0% 표시)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.0f)
                    .background(Color(0xFF06B6D4))
            )
            // Cash Out 부분 (예시로 45% 표시)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.45f)
                    .background(Color(0xFFEF4444), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
            )
        }
    }
    
    // 진행 막대 라벨
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AethirProgressLabel("Vesting Claim", Color(0xFF10B981))
        AethirProgressLabel("Claimable", Color(0xFFFBBF24))
        AethirProgressLabel("Vesting Withdraw", Color(0xFF8B5CF6))
        AethirProgressLabel("Withdrawable", Color(0xFF06B6D4))
        AethirProgressLabel("Cash Out", Color(0xFFEF4444))
    }
}

/**
 * 진행 막대 라벨
 */
@Composable
private fun AethirProgressLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 8.sp,
            color = Color(0xFF9CA3AF)
        )
    }
}



/**
 * 데모용 Aethir 노드 정보 화면
 * 실제 환경에서는 API나 데이터베이스에서 정보를 가져와서 사용
 */
@Composable
fun AethirNodeMonitoringScreen(
    modifier: Modifier = Modifier
) {
    // 데모 데이터 (실제로는 API에서 가져오거나 데이터베이스에서 로드)
    val sampleAethirNodes = remember {
        listOf(
            AethirNodeInfo(
                nodeName = "Aethir Node #001",
                walletInfo = AethirWalletInfo(
                    claimableServiceFee = "0.00",
                    claimablePocPodRewards = "158.75",
                    withdrawable = "0.0000",
                    vestingClaim = "88173.1976",
                    claimable = "15869.7551",
                    vestingWithdraw = "0.0000",
                    withdrawableAmount = "0.0000",
                    cashOutTotal = "149372.4039",
                    staked = "209542.8",
                    unstaking = "224115.8",
                    unstaked = "0.0000"
                ),
                dashboardInfo = AethirDashboardInfo(
                    totalLocations = "1.0000",
                    totalServers = "205296.0000",
                    myAethirEarth = "5.0000",
                    myAethirAtmosphere = "0.0000",
                    totalRewards = "1032.7113",
                    pocRewards = "252140.3918",
                    podRewards = "242.2535"
                ),
                incomeInfo = AethirIncomeInfo(
                    date = "2025-07-30",
                    serviceFee = "0.0000",
                    pocReward = "645.3498",
                    podReward = "0.0000",
                    totalDaily = "645.3498"
                ),
                isOnline = true
            ),
            AethirNodeInfo(
                nodeName = "Aethir Node #002", 
                isOnline = false
            ),
            AethirNodeInfo(
                nodeName = "Aethir Node #003",
                isOnline = true
            )
        )
    }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(sampleAethirNodes) { aethirNode ->
            AethirNodeImageWithInfo(
                aethirNodeInfo = aethirNode,
                modifier = Modifier.fillParentMaxWidth()
            )
        }
    }
}
