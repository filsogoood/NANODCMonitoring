package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * NDP 트랜잭션 목록과 총합을 표시하는 메인 컴포넌트
 * 웹 프로젝트의 디자인을 Android Compose로 구현
 */
@Composable
fun NdpTransactionCard(
    transactions: List<NdpTransaction>,
    modifier: Modifier = Modifier
) {
    // NDP 총합 계산
    val totalNdp = remember(transactions) {
        transactions.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 헤더 섹션
            NdpTransactionHeader(totalNdp = totalNdp)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 트랜잭션 리스트
            if (transactions.isNotEmpty()) {
                NdpTransactionList(transactions = transactions)
            } else {
                NdpEmptyState()
            }
        }
    }
}

/**
 * NDP 트랜잭션 헤더 (총합 표시)
 */
@Composable
private fun NdpTransactionHeader(totalNdp: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 제목
        Text(
            text = "NDP TRANSACTIONS",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // 설명
        Text(
            text = "Nano Data Protocol Token Transactions",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 총합 표시 카드
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF374151)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = "Total NDP",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Total NDP",
                        fontSize = 16.sp,
                        color = Color(0xFF9CA3AF),
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "${String.format("%.4f", totalNdp)} NDP",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

/**
 * NDP 트랜잭션 리스트
 */
@Composable
private fun NdpTransactionList(transactions: List<NdpTransaction>) {
    LazyColumn(
        modifier = Modifier.height(400.dp), // 최대 높이 제한
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            NdpTransactionItem(transaction = transaction)
        }
    }
}

/**
 * 개별 NDP 트랜잭션 아이템
 */
@Composable
private fun NdpTransactionItem(transaction: NdpTransaction) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 트랜잭션 ID와 금액
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction #${transaction.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${transaction.amount} NDP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 트랜잭션 세부 정보
            NdpTransactionDetail(
                icon = Icons.Default.Send,
                label = "From",
                value = transaction.from,
                isAddress = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NdpTransactionDetail(
                icon = Icons.Default.AccountBalance,
                label = "To",
                value = transaction.to,
                isAddress = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NdpTransactionDetail(
                icon = Icons.Default.Link,
                label = "Tx Hash",
                value = transaction.txHash,
                isAddress = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            NdpTransactionDetail(
                icon = Icons.Default.DateRange,
                label = "Date",
                value = formatTransactionDate(transaction.date),
                isAddress = false
            )
        }
    }
}

/**
 * NDP 트랜잭션 세부 정보 행
 */
@Composable
private fun NdpTransactionDetail(
    icon: ImageVector,
    label: String,
    value: String,
    isAddress: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.width(60.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (isAddress) {
            // 주소나 해시값은 줄임 표시
            Text(
                text = shortenAddress(value),
                fontSize = 12.sp,
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            Text(
                text = value,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * NDP 트랜잭션이 없을 때 표시하는 빈 상태
 */
@Composable
private fun NdpEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.AttachMoney,
            contentDescription = "No transactions",
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No NDP Transactions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "No NDP transactions found for this node",
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * NDP 트랜잭션 로딩 상태
 */
@Composable
fun NdpTransactionLoadingCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFF10B981),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Loading NDP Transactions...",
                fontSize = 16.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * NDP 트랜잭션 에러 상태
 */
@Composable
fun NdpTransactionErrorCard(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AttachMoney,
                contentDescription = "Error",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Failed to Load NDP Data",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = errorMessage,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981)
                )
            ) {
                Text(
                    text = "Retry",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 주소나 해시값을 줄여서 표시하는 유틸 함수
 */
private fun shortenAddress(address: String): String {
    return if (address.length > 16) {
        "${address.take(8)}...${address.takeLast(8)}"
    } else {
        address
    }
}

/**
 * 트랜잭션 날짜를 포맷하는 유틸 함수
 */
private fun formatTransactionDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        // 파싱 실패 시 원본 문자열 반환
        dateString
    }
}

/**
 * NDP 트랜잭션 통계 요약 카드
 */
@Composable
fun NdpTransactionSummaryCard(
    transactions: List<NdpTransaction>,
    modifier: Modifier = Modifier
) {
    val totalTransactions = transactions.size
    val totalAmount = transactions.sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
    val averageAmount = if (totalTransactions > 0) totalAmount / totalTransactions else 0.0
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "NDP TRANSACTION SUMMARY",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 총 트랜잭션 수
                StatCard(
                    title = "Total Transactions",
                    value = totalTransactions.toString(),
                    icon = Icons.Default.Send,
                    modifier = Modifier.weight(1f)
                )
                
                // 총 금액
                StatCard(
                    title = "Total Amount",
                    value = "${String.format("%.2f", totalAmount)} NDP",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
                
                // 평균 금액
                StatCard(
                    title = "Average Amount",
                    value = "${String.format("%.2f", averageAmount)} NDP",
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * 통계 카드 컴포넌트
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF374151)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
