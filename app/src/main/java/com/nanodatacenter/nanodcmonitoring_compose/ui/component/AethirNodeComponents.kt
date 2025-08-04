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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
        // 다른 데이터센터는 동일한 수치 (나중에 수정 가능)
        DataCenterAethirWalletInfo(
            vestingClaim = "18424.20",
            claimable = "12406.76",
            cashOutTotal = "128000.40", 
            staking = "72000.72",
            unstaking = "221000.48",
            reward = "386.07"
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
 * 데이터센터별 지갑 밸런스 카드
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
            
            // 지갑 밸런스 정보
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
                    title = "CLAIMABLE",
                    amount = walletInfo.claimable,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenCard(
                    title = "CASH OUT",
                    amount = walletInfo.cashOutTotal,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 진행 막대 추가
            AethirWalletProgressBar(walletInfo = walletInfo)
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
            
            // 스테이킹 데이터 가로 배치
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AethirStakingCard(
                    title = "STAKING",
                    amount = walletInfo.staking,
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
 * Aethir 대시보드 정보를 표시하는 카드
 */
@Composable
private fun AethirDashboardCard(
    dashboardInfo: AethirDashboardInfo,
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
                            Color(0xFF10B981),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resource Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Dashboard,
                    contentDescription = "Dashboard",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 상단 리워드 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirRewardCard(
                    title = "Service Fee (Today)",
                    amount = dashboardInfo.totalRewards,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirRewardCard(
                    title = "POC Rewards (Today)",
                    amount = dashboardInfo.pocRewards,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirRewardCard(
                    title = "POD Rewards (Today)",
                    amount = dashboardInfo.podRewards,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 리소스 개요
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirResourceCard(
                    title = "TOTAL LOCATIONS",
                    value = dashboardInfo.totalLocations,
                    icon = Icons.Default.LocationOn,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirResourceCard(
                    title = "TOTAL SERVERS",
                    value = dashboardInfo.totalServers,
                    icon = Icons.Default.Computer,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirResourceCard(
                    title = "MY AETHIR EARTH",
                    value = dashboardInfo.myAethirEarth,
                    icon = Icons.Default.Public,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirResourceCard(
                    title = "MY AETHIR ATMOSPHERE",
                    value = dashboardInfo.myAethirAtmosphere,
                    icon = Icons.Default.CloudQueue,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Aethir 수입 정보를 표시하는 카드
 */
@Composable
private fun AethirIncomeCard(
    incomeInfo: AethirIncomeInfo,
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
                            Color(0xFF8B5CF6),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Daily Income (${incomeInfo.date})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = "Income",
                    tint = Color(0xFF8B5CF6),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 수입 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirIncomeItemCard(
                    title = "SERVICE FEE",
                    amount = incomeInfo.serviceFee,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirIncomeItemCard(
                    title = "POC REWARD",
                    amount = incomeInfo.pocReward,
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirIncomeItemCard(
                    title = "POD REWARD",
                    amount = incomeInfo.podReward,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 총합 표시
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF111827),
                border = BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Daily Earnings",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${incomeInfo.totalDaily} ATH",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFBBF24),
                        textAlign = TextAlign.Center
                    )
                }
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
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 스테이킹 상태 아이콘
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // 제목
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // 금액
            Text(
                text = "$amount",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            // ATH 단위
            Text(
                text = "ATH",
                fontSize = 12.sp,
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
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111827),
        border = BorderStroke(1.dp, Color(0xFF374151).copy(alpha = 0.3f))
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
                color = Color.White,
                textAlign = TextAlign.Center
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
 * 데이터센터별 지갑 진행 막대
 */
@Composable
private fun AethirWalletProgressBar(
    walletInfo: DataCenterAethirWalletInfo,
    modifier: Modifier = Modifier
) {
    // 실제 데이터를 기반으로 비율 계산
    val vestingClaimValue = walletInfo.vestingClaim.toDoubleOrNull() ?: 0.0
    val claimableValue = walletInfo.claimable.toDoubleOrNull() ?: 0.0
    val cashOutValue = walletInfo.cashOutTotal.toDoubleOrNull() ?: 0.0
    
    val totalValue = vestingClaimValue + claimableValue + cashOutValue
    
    // 비율 계산 (총합이 0이면 기본값 사용)
    val vestingClaimRatio = if (totalValue > 0) (vestingClaimValue / totalValue).toFloat() else 0.144f
    val claimableRatio = if (totalValue > 0) (claimableValue / totalValue).toFloat() else 0.097f
    val cashOutRatio = if (totalValue > 0) (cashOutValue / totalValue).toFloat() else 0.759f
    
    // 최소값 보장 (weight는 0보다 커야 함)
    val minWeight = 0.001f
    val finalVestingClaimRatio = if (vestingClaimRatio > 0) vestingClaimRatio.coerceAtLeast(minWeight) else minWeight
    val finalClaimableRatio = if (claimableRatio > 0) claimableRatio.coerceAtLeast(minWeight) else minWeight  
    val finalCashOutRatio = if (cashOutRatio > 0) cashOutRatio.coerceAtLeast(minWeight) else minWeight
    
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
            // Vesting Claim 부분
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(finalVestingClaimRatio)
                    .background(Color(0xFF10B981), RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            )
            // Claimable 부분  
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(finalClaimableRatio)
                    .background(Color(0xFFFBBF24))
            )
            // Cash Out 부분
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(finalCashOutRatio)
                    .background(Color(0xFFEF4444), RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
            )
        }
    }
    
    // 진행 막대 라벨 - 실제 값이 있는 것만 표시
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AethirProgressLabel("Vesting Claim", Color(0xFF10B981))
        AethirProgressLabel("Claimable", Color(0xFFFBBF24))
        AethirProgressLabel("Cash Out", Color(0xFFEF4444))
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
