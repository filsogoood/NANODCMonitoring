package com.nanodatacenter.nanodcmonitoring_compose.ui.component

import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nanodatacenter.nanodcmonitoring_compose.data.DeviceType
import com.nanodatacenter.nanodcmonitoring_compose.data.ImageType
import com.nanodatacenter.nanodcmonitoring_compose.data.DataCenterType
import com.nanodatacenter.nanodcmonitoring_compose.config.DeviceConfigurationManager
import com.nanodatacenter.nanodcmonitoring_compose.manager.AdminAccessManager
import com.nanodatacenter.nanodcmonitoring_compose.manager.ImageOrderManager
import com.nanodatacenter.nanodcmonitoring_compose.network.model.ApiResponse
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Node
import com.nanodatacenter.nanodcmonitoring_compose.network.model.HardwareSpec
import com.nanodatacenter.nanodcmonitoring_compose.network.model.NodeUsage
import com.nanodatacenter.nanodcmonitoring_compose.network.model.NdpTransaction
import com.nanodatacenter.nanodcmonitoring_compose.repository.NanoDcRepository
import com.nanodatacenter.nanodcmonitoring_compose.util.ImageScaleUtil
import com.nanodatacenter.nanodcmonitoring_compose.util.BC02DataMapper
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.BC02PostWorkerSectorGraph
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.BC02NodeMinerSectorGraph
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.BC02NASSectorGraph
import com.nanodatacenter.nanodcmonitoring_compose.ui.component.BC01NodeInfoCard
import kotlinx.coroutines.launch
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie

/**
 * 클릭 가능한 이미지 아이템 컴포넌트
 * 첫 번째 이미지(index 0) 클릭 시 스코어 카드를 표시합니다.
 * LOGO_ZETACUBE 클릭 시 관리자 접근 기능을 제공합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller는 클릭해도 카드가 나오지 않습니다.
 * SUPRA, POSTWORKER는 전체 노드 정보를 표시하고, FILECOIN과 NOT_STORAGE는 하드디스크 사용량 그래프, NODE_MINER는 FILECOIN 데이터로 전체 정보를 표시합니다.
 */
@Composable
fun ClickableImageItem(
    imageType: ImageType,
    imageIndex: Int = -1,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth,
    apiResponse: ApiResponse? = null,
    onDataCenterChanged: ((DataCenterType) -> Unit)? = null,
    nanoDcId: String? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var scoreData by remember { mutableStateOf<Score?>(null) }

    val repository = remember { NanoDcRepository.getInstance() }
    val adminManager = remember { AdminAccessManager.getInstance() }
    val context = LocalContext.current
    
    // StateFlow 구독
    val lastRefreshTime by repository.lastRefreshTime.collectAsState()

    // 현재 nanoDcId 결정 (매개변수로 받거나 DeviceConfigurationManager에서 가져오기)
    val deviceConfigManager = remember { DeviceConfigurationManager.getInstance(context) }
    val currentNanoDcId = nanoDcId ?: deviceConfigManager.getSelectedDataCenter().nanoDcId

    // 토스트 메시지 표시
    LaunchedEffect(adminManager.shouldShowToast) {
        if (adminManager.shouldShowToast) {
            Toast.makeText(context, adminManager.toastMessage, Toast.LENGTH_SHORT).show()
            adminManager.onToastShown()
        }
    }

    Column(modifier = modifier) {
        // 이미지 표시 (클릭 가능 여부에 따라 동작 분기)
        when {
            imageType.isAdminAccess -> {
                // 관리자 접근 이미지: LOGO_ZETACUBE 8번 클릭 기능
                SeamlessImageItem(
                    imageType = imageType,
                    modifier = Modifier.clickable {
                        adminManager.handleLogoClick()
                    },
                    contentScale = contentScale
                )
            }

            imageType.showsInfoCard -> {
                // 일반 클릭 가능한 이미지: 기존 로직 유지
                SeamlessImageItem(
                    imageType = imageType,
                    modifier = Modifier.clickable {
                        isExpanded = !isExpanded
                    },
                    contentScale = contentScale
                )
            }

            else -> {
                // 클릭 불가능한 이미지: 클릭 이벤트 없이 이미지만 표시
                SeamlessImageItem(
                    imageType = imageType,
                    modifier = Modifier,  // clickable 없음
                    contentScale = contentScale
                )
            }
        }

        // 확장 정보 카드 (일반 클릭 가능한 이미지에만 표시)
        if (imageType.showsInfoCard) {
            // 커스텀 스케일 이미지의 경우 카드 겹침 방지를 위한 여백 추가
            if (ImageScaleUtil.hasCustomScale(imageType)) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                when {
                    // 이미지 타입별 처리를 먼저 확인 (우선순위)
                    imageType == ImageType.NDP_INFO -> {
                        // NDP 트랜잭션 정보 로드 및 표시 (현재 선택된 데이터센터 사용)
                        NdpTransactionContainer(
                            nodeId = null, // 전체 트랜잭션 표시
                            nanodcId = currentNanoDcId
                        )
                    }
                    // NODE_INFO_AETHIR 이미지의 경우 Aethir 노드 정보 표시
                    imageType == ImageType.NODE_INFO_AETHIR -> {
                        // Aethir 노드 정보를 간단하게 표시
                        AethirNodeInfoCard()
                    }
                    // SUPRA, POSTWORKER, FILECOIN, NODE_MINER, NODE_INFO, NOT_STORAGE, STORAGE, LONOVO_POST 이미지의 경우 노드 정보 표시
                    imageType == ImageType.SUPRA || imageType == ImageType.POSTWORKER || imageType == ImageType.FILECOIN || imageType == ImageType.NODE_MINER || imageType == ImageType.NODE_INFO || imageType == ImageType.NOT_STORAGE || imageType == ImageType.STORAGE_1 || imageType == ImageType.STORAGE_2 || imageType == ImageType.STORAGE_3 || imageType == ImageType.STORAGE_4 || imageType == ImageType.STORAGE_5 || imageType == ImageType.STORAGE_6 || imageType == ImageType.LONOVO_POST -> {
                        apiResponse?.let { response ->
                            // 디버그 로그 추가
                            android.util.Log.d("DataCenterComponents", "🔍 Debug Info:")
                            android.util.Log.d("DataCenterComponents", "   Image Type: $imageType")
                            android.util.Log.d(
                                "DataCenterComponents",
                                "   Current NanoDC ID: $currentNanoDcId"
                            )
                            android.util.Log.d(
                                "DataCenterComponents",
                                "   Available Nodes: ${response.nodes.map { it.nodeName }}"
                            )

                            // 데이터센터 타입 확인
                            val isBC01 = currentNanoDcId.equals(
                                "dcf1bb07-f621-4b4d-9d61-45fc3cf5ac20",
                                ignoreCase = true
                            )
                            val isBC02 = currentNanoDcId.equals(
                                "5e807a27-7c3a-4a22-8df2-20c392186ed3",
                                ignoreCase = true
                            )

                            // 이미지 타입에 따라 해당 노드 찾기
                            val targetNode = when (imageType) {
                                ImageType.SUPRA -> response.nodes.find {
                                    it.nodeName.contains(
                                        "Supra",
                                        ignoreCase = true
                                    )
                                }

                                ImageType.POSTWORKER -> response.nodes.find {
                                    it.nodeName.contains(
                                        "PostWorker",
                                        ignoreCase = true
                                    )
                                }

                                ImageType.FILECOIN -> response.nodes.find {
                                    it.nodeName.contains(
                                        "Filecoin",
                                        ignoreCase = true
                                    )
                                }

                                ImageType.LONOVO_POST -> {
                                    // BC02의 경우 LONOVO_POST 이미지를 특정 노드에 매핑하고 섹터별 그래프 적용
                                    if (isBC02) {
                                        android.util.Log.d(
                                            "DataCenterComponents",
                                            "🎯 BC02 LONOVO_POST: Processing imageIndex=$imageIndex"
                                        )
                                        when (imageIndex) {
                                            4 -> { // 첫 번째 LONOVO_POST - BC02 Filecoin Miner (1번 lonovopost)
                                                android.util.Log.d(
                                                    "DataCenterComponents",
                                                    "   Looking for Filecoin Miner"
                                                )
                                                response.nodes.find {
                                                    it.nodeName.contains(
                                                        "Filecoin",
                                                        ignoreCase = true
                                                    ) && it.nodeName.contains(
                                                        "Miner",
                                                        ignoreCase = true
                                                    )
                                                }
                                            }

                                            5 -> { // 두 번째 LONOVO_POST - BC02 3080Ti GPU Worker (2번 lonovopost)
                                                android.util.Log.d(
                                                    "DataCenterComponents",
                                                    "   Looking for 3080Ti GPU Worker"
                                                )
                                                response.nodes.find {
                                                    it.nodeName.contains(
                                                        "3080Ti",
                                                        ignoreCase = true
                                                    ) || it.nodeName.contains(
                                                        "GPU Worker",
                                                        ignoreCase = true
                                                    )
                                                }
                                            }

                                            6 -> { // 세 번째 LONOVO_POST - BC02 Post Worker (3번 lonovopost)
                                                android.util.Log.d(
                                                    "DataCenterComponents",
                                                    "   Looking for Post Worker"
                                                )
                                                response.nodes.find {
                                                    it.nodeName.contains(
                                                        "Post Worker",
                                                        ignoreCase = true
                                                    )
                                                }
                                            }

                                            else -> {
                                                android.util.Log.d(
                                                    "DataCenterComponents",
                                                    "   Default LONOVO_POST fallback"
                                                )
                                                response.nodes.find {
                                                    it.nodeName.contains(
                                                        "Post",
                                                        ignoreCase = true
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        // 다른 데이터센터는 기본 Post Worker 찾기
                                        response.nodes.find {
                                            it.nodeName.contains(
                                                "Post",
                                                ignoreCase = true
                                            )
                                        }
                                    }
                                }

                                ImageType.NODE_MINER -> {
                                    when {
                                        isBC01 -> {
                                            android.util.Log.d(
                                                "DataCenterComponents",
                                                "🎯 BC01 NODE_MINER: Looking for Filecoin-Miner"
                                            )
                                            response.nodes.find {
                                                it.nodeName.contains(
                                                    "Filecoin-Miner",
                                                    ignoreCase = true
                                                )
                                            }
                                        }

                                        isBC02 -> {
                                            android.util.Log.d(
                                                "DataCenterComponents",
                                                "🎯 BC02 NODE_MINER: Looking for Filecoin Miner"
                                            )
                                            response.nodes.find {
                                                it.nodeName.contains(
                                                    "Filecoin",
                                                    ignoreCase = true
                                                ) && it.nodeName.contains(
                                                    "Miner",
                                                    ignoreCase = true
                                                )
                                            }
                                        }

                                        else -> {
                                            response.nodes.find {
                                                it.nodeName.contains(
                                                    "Filecoin",
                                                    ignoreCase = true
                                                )
                                            }
                                        }
                                    }
                                }

                                ImageType.NOT_STORAGE -> response.nodes.find {
                                    it.nodeName.contains(
                                        "Filecoin",
                                        ignoreCase = true
                                    )
                                } // FILECOIN과 동일한 데이터 사용
                                ImageType.STORAGE_1, ImageType.STORAGE_2, ImageType.STORAGE_3, ImageType.STORAGE_4, ImageType.STORAGE_5, ImageType.STORAGE_6 -> {
                                    when {
                                        isBC01 -> {
                                            // BC01의 경우 기존 로직 유지
                                            android.util.Log.d(
                                                "DataCenterComponents",
                                                "🎯 BC01 STORAGE: Processing $imageType"
                                            )
                                            when (imageType) {
                                                ImageType.STORAGE_1 -> {
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for NAS5"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS5",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                ImageType.STORAGE_2 -> {
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for NAS3 or NAS4"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS3",
                                                            ignoreCase = true
                                                        ) || it.nodeName.contains(
                                                            "NAS4",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                ImageType.STORAGE_3 -> {
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for NAS2"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS2",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                ImageType.STORAGE_4 -> {
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for NAS1"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS1",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                ImageType.STORAGE_5 -> {
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for SAI Server"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "SAI Server",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                else -> response.nodes.find {
                                                    it.nodeName.contains(
                                                        "Filecoin",
                                                        ignoreCase = true
                                                    )
                                                }
                                            }
                                        }

                                        isBC02 -> {
                                            // BC02의 경우 STORAGE_1 이미지들을 각각 다른 NAS에 매핑
                                            android.util.Log.d(
                                                "DataCenterComponents",
                                                "🎯 BC02 STORAGE: Processing imageIndex=$imageIndex"
                                            )
                                            when (imageIndex) {
                                                9 -> { // 첫 번째 STORAGE_1 - BC02 NAS1
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for BC02 NAS1"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS1",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                10 -> { // 두 번째 STORAGE_1 - BC02 NAS2
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for BC02 NAS2"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS2",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                11 -> { // 세 번째 STORAGE_1 - BC02 NAS3
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for BC02 NAS3"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS3",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                12 -> { // 네 번째 STORAGE_1 - BC02 NAS4
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for BC02 NAS4"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS4",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                13 -> { // 다섯 번째 STORAGE_1 - BC02 NAS5
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Looking for BC02 NAS5"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS5",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }

                                                else -> {
                                                    android.util.Log.d(
                                                        "DataCenterComponents",
                                                        "   Default BC02 STORAGE fallback"
                                                    )
                                                    response.nodes.find {
                                                        it.nodeName.contains(
                                                            "NAS",
                                                            ignoreCase = true
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        else -> {
                                            // 기본 로직: FILECOIN 노드 사용
                                            response.nodes.find {
                                                it.nodeName.contains(
                                                    "Filecoin",
                                                    ignoreCase = true
                                                )
                                            }
                                        }
                                    }
                                }

                                ImageType.NODE_INFO -> response.nodes.firstOrNull() // NODE_INFO는 첫 번째 노드 사용 또는 특정 노드 지정
                                else -> null
                            }

                            android.util.Log.d(
                                "DataCenterComponents",
                                "   Found Node: ${targetNode?.nodeName ?: "NULL"}"
                            )

                            targetNode?.let { node ->
                                android.util.Log.d(
                                    "DataCenterComponents",
                                    "✅ Processing node: ${node.nodeName}"
                                )
                                val hardwareSpec =
                                    response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                val nodeUsage = response.nodeUsage.find { it.nodeId == node.nodeId }
                                val score = response.scores.find { it.nodeId == node.nodeId }

                                android.util.Log.d("DataCenterComponents", "📊 Data availability:")
                                android.util.Log.d(
                                    "DataCenterComponents",
                                    "   HardwareSpec: ${if (hardwareSpec != null) "✅" else "❌"}"
                                )
                                android.util.Log.d(
                                    "DataCenterComponents",
                                    "   NodeUsage: ${if (nodeUsage != null) "✅" else "❌"}"
                                )
                                android.util.Log.d(
                                    "DataCenterComponents",
                                    "   Score: ${if (score != null) "✅" else "❌"}"
                                )

                                when (imageType) {
                                    ImageType.NODE_INFO -> {
                                        // NODE_INFO는 헤더 카드와 마이닝 대시보드를 분리해서 표시
                                        val hardwareSpec =
                                            response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val nodeUsage =
                                            response.nodeUsage.find { it.nodeId == node.nodeId }

                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // 첫 번째 카드: 헤더 정보 (데이터센터별 주소 표시)
                                            NodeInfoHeaderCard(
                                                dataCenterName = when {
                                                    isBC01 -> "BC01"
                                                    isBC02 -> "BC02"
                                                    else -> "GY01"
                                                },
                                                node = node
                                            )

                                            // 분리된 카드들: Miner Overview, Adjusted Power
                                            NodeSeparateCards(
                                                node = node,
                                                hardwareSpec = hardwareSpec,
                                                nodeUsage = nodeUsage,
                                                dataCenterName = when {
                                                    isBC01 -> "BC01"
                                                    isBC02 -> "BC02"
                                                    else -> "GY01"
                                                }
                                            )
                                        }
                                    }

                                    ImageType.FILECOIN -> {
                                        // FILECOIN은 하드디스크 사용량 그래프 표시 (GY01 전용)
                                        val hardwareSpec =
                                            response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val nodeUsage =
                                            response.nodeUsage.find { it.nodeId == node.nodeId }

                                        FilecoinDiskUsageCard(
                                            node = node,
                                            hardwareSpec = hardwareSpec,
                                            nodeUsage = nodeUsage,
                                            displayName = when {
                                                isBC02 -> "BC02 Filecoin Miner"
                                                else -> "GY01 STORAGE"
                                            }
                                        )
                                    }

                                    ImageType.NOT_STORAGE -> {
                                        // NOT_STORAGE도 하드디스크 사용량 그래프 표시 (FILECOIN과 동일)
                                        val hardwareSpec =
                                            response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val nodeUsage =
                                            response.nodeUsage.find { it.nodeId == node.nodeId }

                                        FilecoinDiskUsageCard(
                                            node = node,
                                            hardwareSpec = hardwareSpec,
                                            nodeUsage = nodeUsage,
                                            displayName = "GY01 STORAGE"
                                        )
                                    }

                                    ImageType.STORAGE_1, ImageType.STORAGE_2, ImageType.STORAGE_3, ImageType.STORAGE_4, ImageType.STORAGE_5, ImageType.STORAGE_6 -> {
                                        val displayName = when {
                                            // BC01 데이터센터의 경우 실제 노드 이름 반영
                                            isBC01 -> when (imageType) {
                                                ImageType.STORAGE_1 -> "BC01 Storage 1"
                                                ImageType.STORAGE_2 -> "BC01 Storage 2"
                                                ImageType.STORAGE_3 -> "BC01 Storage 3"
                                                ImageType.STORAGE_4 -> "BC01 Storage 4"
                                                ImageType.STORAGE_5 -> "BC01 Storage 5"
                                                ImageType.STORAGE_6 -> "BC01 Storage 6"
                                                else -> "BC01 Storage"
                                            }
                                            // BC02 데이터센터의 경우 각 STORAGE_1을 다른 NAS로 표시
                                            isBC02 -> when (imageIndex) {
                                                9 -> "BC02 NAS1"
                                                10 -> "BC02 NAS2"
                                                11 -> "BC02 NAS3"
                                                12 -> "BC02 NAS4"
                                                13 -> "BC02 NAS5"
                                                else -> "BC02 Storage"
                                            }
                                            // 다른 데이터센터는 기존 방식
                                            else -> when (imageType) {
                                                ImageType.STORAGE_1 -> "GY01 STORAGE 1"
                                                ImageType.STORAGE_2 -> "GY01 STORAGE 2"
                                                ImageType.STORAGE_3 -> "GY01 STORAGE 3"
                                                ImageType.STORAGE_4 -> "GY01 STORAGE 4"
                                                ImageType.STORAGE_5 -> "GY01 STORAGE 5"
                                                ImageType.STORAGE_6 -> "GY01 STORAGE 6"
                                                else -> "GY01 STORAGE"
                                            }
                                        }

                                        android.util.Log.d(
                                            "DataCenterComponents",
                                            "🎨 Creating Storage Card:"
                                        )
                                        android.util.Log.d(
                                            "DataCenterComponents",
                                            "   DisplayName: $displayName"
                                        )
                                        android.util.Log.d(
                                            "DataCenterComponents",
                                            "   Node: ${node.nodeName}"
                                        )
                                        android.util.Log.d(
                                            "DataCenterComponents",
                                            "   HardwareSpec: ${hardwareSpec?.cpuModel ?: "N/A"}"
                                        )
                                        android.util.Log.d(
                                            "DataCenterComponents",
                                            "   NodeUsage: CPU=${nodeUsage?.cpuUsagePercent ?: "N/A"}%"
                                        )
                                        android.util.Log.d(
                                            "DataCenterComponents",
                                            "   Score: ${score?.averageScore ?: "N/A"}"
                                        )

                                        // BC01, BC02의 경우 각각 특별한 UI 사용, 다른 데이터센터는 일반 노드 정보 카드 표시
                                        if (isBC01) {
                                            android.util.Log.d(
                                                "DataCenterComponents",
                                                "   Using BC01NodeInfoCard for BC01"
                                            )
                                            BC01NodeInfoCard(
                                                node = node,
                                                hardwareSpec = hardwareSpec,
                                                score = score,
                                                nodeUsage = nodeUsage,
                                                nodeIndex = imageIndex
                                            )
                                        } else if (isBC02) {
                                            android.util.Log.d(
                                                "DataCenterComponents",
                                                "   Using BC02 NAS Sector Graph for BC02"
                                            )
                                            // BC02의 경우 NAS 섹터 그래프와 별도 Hardware Specifications 카드 표시
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                                                            BC02NASSectorGraph(
                                                node = node,
                                                hardwareSpec = hardwareSpec,
                                                nodeUsage = nodeUsage,
                                                score = score,
                                                displayName = displayName,
                                                lastRefreshTime = lastRefreshTime
                                            )
                                            }
                                        } else {
                                            android.util.Log.d(
                                                "DataCenterComponents",
                                                "   Using NodeInfoCard for BC01 and other centers"
                                            )
                                            NodeInfoCard(
                                                node = node,
                                                hardwareSpec = hardwareSpec,
                                                score = score,
                                                nodeUsage = nodeUsage,
                                                displayName = displayName,
                                                showNameCard = true
                                            )
                                        }
                                    }

                                    ImageType.LONOVO_POST -> {
                                        // BC02의 LONOVO_POST는 섹터별 그래프 적용
                                        val hardwareSpec =
                                            response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val score =
                                            response.scores.find { it.nodeId == node.nodeId }
                                        val nodeUsage =
                                            response.nodeUsage.find { it.nodeId == node.nodeId }

                                        // BC02의 경우 LONOVO_POST 이미지별로 다른 표시 이름과 섹터별 그래프 사용
                                        if (isBC02) {
                                            val displayName = when (imageIndex) {
                                                4 -> "BC02 Filecoin Miner"
                                                5 -> "BC02 3080Ti GPU Worker"
                                                6 -> "BC02 Post Worker"
                                                else -> "BC02 Post Worker"
                                            }

                                            // 섹터별 그래프 적용
                                            val category =
                                                BC02DataMapper.getBC02NodeCategory(imageIndex)

                                            // 섹터별 그래프와 별도 Hardware Specifications 카드 표시
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                when (category) {
                                                    BC02DataMapper.BC02NodeCategory.POST_WORKER -> {
                                                        BC02PostWorkerSectorGraph(
                                                            node = node,
                                                            hardwareSpec = hardwareSpec,
                                                            nodeUsage = nodeUsage,
                                                            score = score,
                                                            displayName = displayName,
                                                            lastRefreshTime = lastRefreshTime
                                                        )
                                                    }

                                                    BC02DataMapper.BC02NodeCategory.NODE_MINER -> {
                                                        BC02NodeMinerSectorGraph(
                                                            node = node,
                                                            hardwareSpec = hardwareSpec,
                                                            nodeUsage = nodeUsage,
                                                            score = score,
                                                            displayName = displayName,
                                                            lastRefreshTime = lastRefreshTime
                                                        )
                                                    }

                                                    else -> {
                                                        // 기본 카드 (UNKNOWN)
                                                        NodeInfoCard(
                                                            node = node,
                                                            hardwareSpec = hardwareSpec,
                                                            score = score,
                                                            nodeUsage = nodeUsage,
                                                            displayName = displayName,
                                                            showNameCard = true
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            // 다른 데이터센터는 기존 방식
                                            NodeInfoCard(
                                                node = node,
                                                hardwareSpec = hardwareSpec,
                                                score = score,
                                                nodeUsage = nodeUsage,
                                                displayName = "GY01 POSTWORKER",
                                                showNameCard = true
                                            )
                                        }
                                    }

                                    ImageType.NODE_MINER -> {
                                        // NODE_MINER는 전체 정보 표시 (BC01은 BC01NodeInfoCard, 다른 데이터센터는 NodeInfoCard 사용)
                                        val hardwareSpec =
                                            response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val score =
                                            response.scores.find { it.nodeId == node.nodeId }
                                        val nodeUsage =
                                            response.nodeUsage.find { it.nodeId == node.nodeId }

                                        if (isBC01) {
                                            BC01NodeInfoCard(
                                                node = node,
                                                hardwareSpec = hardwareSpec,
                                                score = score,
                                                nodeUsage = nodeUsage,
                                                nodeIndex = imageIndex
                                            )
                                        } else {
                                            NodeInfoCard(
                                                node = node,
                                                hardwareSpec = hardwareSpec,
                                                score = score,
                                                nodeUsage = nodeUsage,
                                                displayName = when {
                                                    isBC02 -> "BC02 Filecoin Miner"
                                                    else -> "GY01 NODE MINER"
                                                },
                                                showNameCard = true // 이름 카드 표시
                                            )
                                        }
                                    }

                                    else -> {
                                        // SUPRA, POSTWORKER는 전체 정보 표시
                                        val hardwareSpec =
                                            response.hardwareSpecs.find { it.nodeId == node.nodeId }
                                        val score =
                                            response.scores.find { it.nodeId == node.nodeId }
                                        val nodeUsage =
                                            response.nodeUsage.find { it.nodeId == node.nodeId }

                                        val displayName = when (imageType) {
                                            ImageType.SUPRA -> "GY01 SUPRA WORKER"
                                            ImageType.POSTWORKER -> "GY01 POSTWORKER"
                                            else -> "GY01 NODE"
                                        }

                                        NodeInfoCard(
                                            node = node,
                                            hardwareSpec = hardwareSpec,
                                            score = score,
                                            nodeUsage = nodeUsage,
                                            displayName = displayName,
                                            showNameCard = true // 이름 카드 표시
                                        )
                                    }
                                }
                            } ?: run {
                                android.util.Log.w(
                                    "DataCenterComponents",
                                    "❌ No matching node found for $imageType"
                                )
                                ExpandedInfoCard(imageType = imageType) // 노드를 찾지 못한 경우 기본 카드 표시
                            }
                        } ?: run {
                            android.util.Log.w(
                                "DataCenterComponents",
                                "❌ No API response available"
                            )
                            ExpandedInfoCard(imageType = imageType) // API 데이터가 없는 경우 기본 카드 표시
                        }
                    }
                    // 첫 번째 이미지이면서 위의 특수한 타입이 아닌 경우에만 스코어 카드 표시
                    imageIndex == 0 -> {
                        // 첫 번째 이미지인 경우 스코어 카드 표시
                        LaunchedEffect(Unit) {
                            // 스코어 데이터 로드
                            try {
                                scoreData = repository.getScoreForFirstImage()
                            } catch (e: Exception) {
                                // API 실패 시에도 기본값으로 표시
                                scoreData = null
                            }
                        }
                        ExpandedScoreCard(score = scoreData)
                    }

                    else -> {
                        // 다른 이미지는 기존 확장 정보 표시
                        ExpandedInfoCard(imageType = imageType)
                    }
                }
            }
        }
    }

    // 관리자 다이얼로그 표시
    if (imageType.isAdminAccess) {
        // 현재 선택된 데이터센터 가져오기
        val deviceConfigManager = remember { DeviceConfigurationManager.getInstance(context) }

        AdminAccessDialog(
            isVisible = adminManager.showAdminDialog,
            onDismiss = { adminManager.dismissAdminDialog() },
            onDataCenterChanged = { dataCenter ->
                // MainActivity의 콜백 호출
                onDataCenterChanged?.invoke(dataCenter)
                Toast.makeText(
                    context,
                    "Data center changed to: ${dataCenter.displayName}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onAdminAccess = {
                // 추후 관리자 메뉴 확장 시 사용
                Toast.makeText(context, "Admin menu access", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * FILECOIN과 NOT_STORAGE 하드디스크 사용량을 그래프로 표시하는 카드 컴포넌트
 */
@Composable
fun FilecoinDiskUsageCard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    displayName: String? = null, // 커스텀 표시 이름
    modifier: Modifier = Modifier
) {
    android.util.Log.d(
        "FilecoinDiskUsageCard",
        "🎨 Rendering card for: ${displayName ?: node.nodeName}"
    )
    android.util.Log.d("FilecoinDiskUsageCard", "   HardwareSpec exists: ${hardwareSpec != null}")
    android.util.Log.d("FilecoinDiskUsageCard", "   NodeUsage exists: ${nodeUsage != null}")
    if (hardwareSpec != null) {
        android.util.Log.d("FilecoinDiskUsageCard", "   TotalHDD: ${hardwareSpec.totalHarddiskGb}")
    }
    if (nodeUsage != null) {
        android.util.Log.d(
            "FilecoinDiskUsageCard",
            "   HDDUsage: ${nodeUsage.harddiskUsedPercent}%"
        )
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
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 노드 이름
            Text(
                text = displayName ?: node.nodeName, // displayName이 있으면 사용, 없으면 기본 이름
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 하드디스크 사용량 그래프
            if (hardwareSpec != null && nodeUsage != null) {
                val totalHarddiskGb = hardwareSpec.totalHarddiskGb?.toLongOrNull() ?: 0L
                val usagePercent = nodeUsage.harddiskUsedPercent?.toFloatOrNull() ?: 0f

                DiskUsageChart(
                    totalCapacityGb = totalHarddiskGb,
                    usagePercent = usagePercent
                )
            } else {
                Text(
                    text = "Data not available",
                    color = Color(0xFF9CA3AF),
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * 디스크 사용량 차트 (바 형태)
 */
@Composable
fun DiskUsageChart(
    totalCapacityGb: Long,
    usagePercent: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // 프로그레스 바 (더 큰 크기로)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    Color(0xFF374151),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((usagePercent / 100f).coerceIn(0f, 1f))
                    .background(
                        when {
                            usagePercent > 90f -> Color(0xFFEF4444) // 빨간색
                            usagePercent > 70f -> Color(0xFFF59E0B) // 주황색
                            else -> Color(0xFF10B981) // 초록색
                        },
                        RoundedCornerShape(20.dp)
                    )
            )

            // 바 안에 퍼센테이지 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${String.format("%.1f", usagePercent)}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 추가 정보 행
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Total: ${formatCapacity(totalCapacityGb)}",
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = when {
                    usagePercent > 90f -> "Critical"
                    usagePercent > 70f -> "Warning"
                    else -> "Normal"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    usagePercent > 90f -> Color(0xFFEF4444)
                    usagePercent > 70f -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }
            )
        }
    }
}

/**
 * 용량을 읽기 쉬운 형태로 포맷
 */
private fun formatCapacity(capacityGb: Long): String {
    return when {
        capacityGb >= 1024 * 1024 -> "${String.format("%.1f", capacityGb / (1024.0 * 1024.0))} PB"
        capacityGb >= 1024 -> "${String.format("%.1f", capacityGb / 1024.0)} TB"
        else -> "${capacityGb} GB"
    }
}

/**
 * 확장 정보를 보여주는 카드 컴포넌트
 */
@Composable
fun ExpandedInfoCard(
    imageType: ImageType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 제목
            Text(
                text = imageType.description ?: "Equipment Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 상태 정보 (추후 API 연동 시 실제 값으로 대체)
            InfoRow(label = "Status", value = "Normal")
            InfoRow(label = "Temperature", value = "23°C")
            InfoRow(label = "CPU Usage", value = "45%")
            InfoRow(label = "Memory Usage", value = "67%")
            InfoRow(label = "Network", value = "Connected")
            InfoRow(label = "Last Update", value = "2 minutes ago")
        }
    }
}

/**
 * 정보 행을 표시하는 컴포넌트
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 기본적으로 모든 이미지가 간격 없이 붙어서 표시되는 이미지 컴포넌트
 * 모든 이미지가 동일한 방식으로 표시되어 카드 레이아웃 문제를 방지
 * 특정 이미지 타입들에 대해서는 위아래만 90% 크기로 조정하면서 레이아웃 공간도 함께 조정하여 간격 제거
 */
@Composable
fun SeamlessImageItem(
    imageType: ImageType,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    // 커스텀 스케일이 적용된 이미지는 scale과 layout modifier를 결합하여 간격 완전 제거
    val imageModifier = if (ImageScaleUtil.hasCustomScale(imageType)) {
        val scaleFactor = ImageScaleUtil.getImageScaleFactor(imageType)
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .scale(scaleY = scaleFactor, scaleX = 1f)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val newHeight = (placeable.height * scaleFactor).toInt()
                layout(placeable.width, newHeight) {
                    placeable.place(0, 0)
                }
            }
    } else {
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
    }

    // 모든 이미지를 동일한 방식으로 처리하여 레이아웃 일관성 보장
    Image(
        painter = painterResource(id = imageType.drawableRes),
        contentDescription = imageType.description,
        modifier = imageModifier,
        contentScale = contentScale
    )
}

/**
 * 순수 이미지만 표시하는 컴포넌트 (카드, 박스 없음)
 * 원본 크기 및 다양한 스케일링 모드 지원
 * 클릭 가능한 이미지의 경우 첫 번째 이미지 클릭 시 스코어 모달을 표시합니다.
 * LOGO_ZETACUBE 클릭 시 관리자 접근 기능을 제공합니다.
 * None이 붙은 이미지들, 100G Switch, UPS Controller는 클릭해도 카드가 나오지 않습니다.
 * SUPRA, POSTWORKER는 전체 노드 정보를 표시하고, FILECOIN과 NOT_STORAGE는 하드디스크 사용량 그래프, NODE_MINER는 FILECOIN 데이터로 전체 정보를 표시합니다.
 */
@Composable
fun PureImageItem(
    imageType: ImageType,
    imageIndex: Int = -1,
    modifier: Modifier = Modifier,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH,
    apiResponse: ApiResponse? = null,
    onDataCenterChanged: ((DataCenterType) -> Unit)? = null,
    nanoDcId: String? = null
) {
    val contentScale = ImageScaleUtil.getContentScale(scaleMode)

    ClickableImageItem(
        imageType = imageType,
        imageIndex = imageIndex,
        modifier = modifier,
        contentScale = contentScale,
        apiResponse = apiResponse,
        onDataCenterChanged = onDataCenterChanged,
        nanoDcId = nanoDcId
    )
}

/**
 * 스크롤 없이 모든 이미지가 한 화면에 보이도록 하는 컴포넌트
 * 이미지들이 간격 없이 연속적으로 표시됨
 * API 데이터를 로드하여 SUPRA, POSTWORKER는 전체 노드 정보를 표시하고, FILECOIN과 NOT_STORAGE는 하드디스크 사용량 그래프, NODE_MINER는 FILECOIN 데이터로 전체 정보를 표시합니다.
 * 데이터센터별로 다른 이미지 순서를 지원합니다.
 */
@Composable
fun DataCenterMonitoringScreen(
    modifier: Modifier = Modifier,
    deviceType: DeviceType = DeviceType.DEFAULT,
    scaleMode: ImageScaleUtil.ScaleMode = ImageScaleUtil.ScaleMode.FIT_WIDTH,
    useOriginalSize: Boolean = false,
    onDataCenterChanged: ((DataCenterType) -> Unit)? = null
) {
    val imageOrderManager = ImageOrderManager.getInstance()

    // 현재 선택된 데이터센터 가져오기
    val context = LocalContext.current
    val deviceConfigManager = remember { DeviceConfigurationManager.getInstance(context) }
    val currentDataCenter = deviceConfigManager.getSelectedDataCenter()

    // 데이터센터별 이미지 순서 가져오기
    val imageOrder = imageOrderManager.getImageOrderForDataCenter(currentDataCenter)

    // API 데이터 로드 - StateFlow를 통한 자동 갱신 데이터 구독
    val repository = remember { NanoDcRepository.getInstance() }
    val apiResponse by repository.apiResponseState.collectAsState()
    val isLoading by repository.isLoading.collectAsState()
    val lastRefreshTime by repository.lastRefreshTime.collectAsState()

    val currentNanoDcId = currentDataCenter.nanoDcId

    // Repository가 아직 자동 갱신을 시작하지 않았다면 시작
    LaunchedEffect(Unit) {
        // MainActivity에서 이미 시작했지만, 혹시 모를 상황을 대비한 안전장치
        if (repository.apiResponseState.value == null) {
            android.util.Log.d(
                "DataCenterMonitoringScreen",
                "🔄 Ensuring auto refresh is active with: $currentNanoDcId"
            )
            repository.startAutoRefresh(currentNanoDcId)
        }
    }

    if (useOriginalSize) {
        // 원본 크기 모드: 각 이미지를 원본 크기로 표시하고 스크롤 가능
        SeamlessOriginalSizeContent(
            imageOrder = imageOrder,
            modifier = modifier,
            apiResponse = apiResponse,
            onDataCenterChanged = onDataCenterChanged,
            nanoDcId = currentNanoDcId
        )
    } else {
        // 기존 방식: 화면에 맞춰 이미지 크기 조정
        SeamlessFitScreenContent(
            imageOrder = imageOrder,
            scaleMode = scaleMode,
            modifier = modifier,
            apiResponse = apiResponse,
            onDataCenterChanged = onDataCenterChanged,
            nanoDcId = currentNanoDcId
        )
    }
}

/**
 * 원본 크기로 이미지를 표시하는 컴포넌트 (간격 없음)
 */
@Composable
private fun SeamlessOriginalSizeContent(
    imageOrder: List<ImageType>,
    modifier: Modifier = Modifier,
    apiResponse: ApiResponse? = null,
    onDataCenterChanged: ((DataCenterType) -> Unit)? = null,
    nanoDcId: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        imageOrder.forEachIndexed { index, imageType ->
            ClickableImageItem(
                imageType = imageType,
                imageIndex = index,
                contentScale = ContentScale.FillWidth,
                apiResponse = apiResponse,
                onDataCenterChanged = onDataCenterChanged,
                nanoDcId = nanoDcId
            )
        }
    }
}

/**
 * 화면에 맞춰 이미지 크기를 조정하는 컴포넌트 (간격 없음)
 */
@Composable
private fun SeamlessFitScreenContent(
    imageOrder: List<ImageType>,
    scaleMode: ImageScaleUtil.ScaleMode,
    modifier: Modifier = Modifier,
    apiResponse: ApiResponse? = null,
    onDataCenterChanged: ((DataCenterType) -> Unit)? = null,
    nanoDcId: String? = null
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    // 각 이미지의 높이 계산 (화면 높이를 이미지 개수로 나누기)
    val baseImageHeight = screenHeight / imageOrder.size

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        imageOrder.forEachIndexed { index, imageType ->
            // 특정 이미지 타입들에 대해서는 90% 높이 적용하되 레이아웃 공간도 함께 조정
            val adjustedHeight = if (ImageScaleUtil.hasCustomScale(imageType)) {
                (baseImageHeight * ImageScaleUtil.getImageScaleFactor(imageType)).toInt()
            } else {
                baseImageHeight
            }

            PureImageItem(
                imageType = imageType,
                imageIndex = index,
                modifier = Modifier.height(adjustedHeight.dp),
                scaleMode = scaleMode,
                apiResponse = apiResponse,
                onDataCenterChanged = onDataCenterChanged,
                nanoDcId = nanoDcId
            )
        }
    }
}

/**
 * 원본 크기 이미지들을 연속으로 표시하는 전체 화면 모니터링 컴포넌트
 * 데이터센터별로 다른 이미지 순서를 지원합니다.
 */
@Composable
fun OriginalSizeDataCenterScreen(
    modifier: Modifier = Modifier,
    deviceType: DeviceType = DeviceType.DEFAULT
) {
    val imageOrderManager = ImageOrderManager.getInstance()

    // 현재 선택된 데이터센터 가져오기
    val context = LocalContext.current
    val deviceConfigManager = remember { DeviceConfigurationManager.getInstance(context) }
    val currentDataCenter = deviceConfigManager.getSelectedDataCenter()

    // 데이터센터별 이미지 순서 가져오기
    val imageOrder = imageOrderManager.getImageOrderForDataCenter(currentDataCenter)
    val currentNanoDcId = currentDataCenter.nanoDcId

    // API 데이터 로드
    val repository = remember { NanoDcRepository.getInstance() }
    var apiResponse by remember { mutableStateOf<ApiResponse?>(null) }

    LaunchedEffect(currentNanoDcId) {
        try {
            apiResponse = repository.getUserData(currentNanoDcId)
        } catch (e: Exception) {
            // 에러 처리 - 로그만 남기고 계속 진행
            android.util.Log.e("OriginalSizeDataCenterScreen", "Failed to load API data", e)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        itemsIndexed(
            items = imageOrder,
            key = { _, imageType -> imageType.name }
        ) { index, imageType ->
            ClickableImageItem(
                imageType = imageType,
                imageIndex = index,
                modifier = Modifier.fillParentMaxWidth(),
                contentScale = ContentScale.FillWidth,
                apiResponse = apiResponse
            )
        }
    }
}

/**
 * NODE_INFO 마이닝 종합 대시보드 컴포넌트
 * FileCoin과 유사한 UI로 노드의 상세 마이닝 정보를 표시
 */
@Composable
fun NodeMiningDashboard(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    modifier: Modifier = Modifier
) {
    var selectedTimeRange by remember { mutableStateOf("24 hour") }
    val timeRanges = listOf("24 hour", "7 day", "30 day", "1 year")

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
            // 헤더 정보 카드 (제목 + Address 통합)
            NodeInfoHeaderCard(
                dataCenterName = "GY01", // 기본값으로 GY01 사용
                node = node
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 시간 범위 선택 탭
            TimeRangeSelector(
                selectedRange = selectedTimeRange,
                ranges = timeRanges,
                onRangeSelected = { selectedTimeRange = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 메인 정보 섹션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 왼쪽: Address Balance 원형 차트
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Miner Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    NodeBalanceChart(hardwareSpec = hardwareSpec, nodeUsage = nodeUsage)
                }

                // 오른쪽: Adjusted Power 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Adjusted Power",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    NodePowerInfo(hardwareSpec = hardwareSpec, nodeUsage = nodeUsage)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 하드웨어 사용량 섹션
            if (hardwareSpec != null && nodeUsage != null) {
                Text(
                    text = "Hardware Usage",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                NodeHardwareUsageChart(
                    hardwareSpec = hardwareSpec,
                    nodeUsage = nodeUsage
                )
            }
        }
    }
}

/**
 * NODE_INFO용 마이닝 대시보드 (헤더 없는 버전)
 */
@Composable
fun NodeMiningDashboardWithoutHeader(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
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
            modifier = Modifier.padding(20.dp)
        ) {
            // 메인 정보 섹션
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 왼쪽: Address Balance 원형 차트
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Miner Overview",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    NodeBalanceChart(hardwareSpec = hardwareSpec, nodeUsage = nodeUsage)
                }

                // 오른쪽: Adjusted Power 정보
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Adjusted Power",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    NodePowerInfo(hardwareSpec = hardwareSpec, nodeUsage = nodeUsage)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * NODE_INFO 전용 헤더 카드 (제목 + Address 통합)
 */
@Composable
fun NodeInfoHeaderCard(
    dataCenterName: String = "GY01",
    node: Node? = null
) {
    // 데이터센터별 주소 정보
    val addressInfo = when (dataCenterName.uppercase()) {
        "BC01" -> Pair("BC01 ADDRESS: ", "f03091958") // BC01용 주소
        "BC02" -> Pair("BC02 ADDRESS: ", "f03134685") // BC02용 주소 (이미지 값)
        "GY01" -> Pair("GY01 ADDRESS: ", "f03132919") // GY01용 주소 (기존)
        else -> Pair("ADDRESS: ", "f03132919") // 기본값
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Address 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = addressInfo.first,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = addressInfo.second,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 노드 마이너 헤더 (노드 이름과 주소)
 */
@Composable
fun NodeMinerHeader(
    node: Node,
    dataCenterName: String = "GY01"
) {
    // 데이터센터별 주소 정보
    val addressValue = when (dataCenterName.uppercase()) {
        "BC01" -> "f03091958" // BC01용 주소
        "BC02" -> "f03134685" // BC02용 주소 (이미지 값)
        "GY01" -> "f03132919" // GY01용 주소 (기존)
        else -> "f03132919" // 기본값
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = node.nodeName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Address $addressValue", // 데이터센터별 동적 주소
            fontSize = 14.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(top = 4.dp)
        )

        // Claim account 링크 스타일
        Text(
            text = "Claim account>",
            fontSize = 12.sp,
            color = Color(0xFF3B82F6),
            modifier = Modifier
                .padding(top = 2.dp)
                .clickable { /* 클레임 액션 구현 */ }
        )
    }
}

/**
 * 시간 범위 선택 탭
 */
@Composable
fun TimeRangeSelector(
    selectedRange: String,
    ranges: List<String>,
    onRangeSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ranges.forEach { range ->
            val isSelected = selectedRange == range
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) Color(0xFF3B82F6) else Color(0xFF374151),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { onRangeSelected(range) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = range,
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 노드 Balance 도넛형 차트
 */
@Composable
fun NodeBalanceChart(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?
) {
    // 샘플 데이터 - 실제 환경에서는 API에서 가져와야 함
    val addressBalance = 18072.2546f
    val availableBalance = 445.0850f
    val initialPledge = 16853.3007f
    val lockedRewards = 773.8689f

    Box(
        modifier = Modifier.size(150.dp),
        contentAlignment = Alignment.Center
    ) {
        // 도넛형 차트 (Canvas로 구현)
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.minDimension / 2 * 0.8f
            val innerRadius = outerRadius * 0.5f // 도넛 홀 크기 조절
            val strokeWidth = outerRadius - innerRadius

            // 전체 원 (배경) - 도넛형
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF374151),
                radius = outerRadius,
                center = center,
                style = Stroke(strokeWidth)
            )

            // Available balance 부분 - 도넛형
            val availableAngle = (availableBalance / addressBalance) * 360f
            drawArc(
                color = androidx.compose.ui.graphics.Color(0xFF10B981),
                startAngle = -90f,
                sweepAngle = availableAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2)
            )

            // Locked rewards 부분 - 도넛형
            val lockedAngle = (lockedRewards / addressBalance) * 360f
            drawArc(
                color = androidx.compose.ui.graphics.Color(0xFFF59E0B),
                startAngle = -90f + availableAngle,
                sweepAngle = lockedAngle,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                size = Size(outerRadius * 2, outerRadius * 2)
            )
        }

        // 중앙 텍스트 (도넛 홀 안에 표시)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Address Balance",
                fontSize = 9.sp,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = "${String.format("%.1f", addressBalance / 1000)}K FIL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }

    // 범례
    Spacer(modifier = Modifier.height(12.dp))

    Column(
        horizontalAlignment = Alignment.Start
    ) {
        NodeBalanceLegendItem(
            color = Color(0xFF10B981),
            label = "Available Balance",
            value = "${String.format("%.4f", availableBalance)} FIL"
        )
        NodeBalanceLegendItem(
            color = Color(0xFFF59E0B),
            label = "Locked Rewards",
            value = "${String.format("%.4f", lockedRewards)} FIL"
        )
        NodeBalanceLegendItem(
            color = Color(0xFF6B7280),
            label = "Initial Pledge",
            value = "${String.format("%.4f", initialPledge)} FIL"
        )
    }
}

/**
 * 노드 Balance 범례 아이템
 */
@Composable
fun NodeBalanceLegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 노드 Power 정보 표시
 */
@Composable
fun NodePowerInfo(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    dataCenterName: String = "GY01"
) {
    // 데이터센터별 Power 정보
    val powerInfo = when (dataCenterName.uppercase()) {
        "BC01" -> Pair(
            Triple("Adjusted Power", "4.07 PiB", "Rate: 0.015%"),
            Triple("Total Reward", "4,407.94 FIL", "Win Count: 708")
        )
        "BC02" -> Pair(
            Triple("Adjusted Power", "3.84 PiB", "Rate: 0.02%"),
            Triple("Total Reward", "3,763.23 FIL", "Win Count: 598")
        )
        "GY01" -> Pair(
            Triple("Adjusted Power", "3.88 PiB", "Rate: 0.02%"),
            Triple("Total Reward", "3,426.10 FIL", "Win Count: 557")
        )
        else -> Pair(
            Triple("Adjusted Power", "3.88 PiB", "Rate: 0.02%"),
            Triple("Total Reward", "3,426.10 FIL", "Win Count: 557")
        )
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NodePowerInfoItem(
            label = powerInfo.first.first,
            value = powerInfo.first.second,
            subInfo = powerInfo.first.third
        )
        NodePowerInfoItem(
            label = powerInfo.second.first,
            value = powerInfo.second.second,
            subInfo = powerInfo.second.third
        )
    }
}

/**
 * 노드 Power 정보 아이템
 */
@Composable
fun NodePowerInfoItem(
    label: String,
    value: String,
    subInfo: String
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = subInfo,
            fontSize = 10.sp,
            color = Color(0xFF9CA3AF)
        )
    }
}


/**
 * 노드 마이닝 통계 카드
 */
@Composable
fun NodeMiningStatCard(
    title: String,
    value: String,
    subtitle: String,
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
                .padding(12.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 노드 하드웨어 사용량 차트
 */
@Composable
fun NodeHardwareUsageChart(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?
) {
    if (hardwareSpec == null || nodeUsage == null) {
        Text(
            text = "Hardware data not available",
            color = Color(0xFF9CA3AF),
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CPU 사용량
        nodeUsage.cpuUsagePercent?.toFloatOrNull()?.let { cpuUsage ->
            HardwareUsageBar(
                label = "CPU Usage",
                percentage = cpuUsage,
                color = Color(0xFF3B82F6)
            )
        }

        // 메모리 사용량
        nodeUsage.memUsagePercent?.toFloatOrNull()?.let { memUsage ->
            HardwareUsageBar(
                label = "Memory Usage",
                percentage = memUsage,
                color = Color(0xFF10B981)
            )
        }

        // GPU 사용량
        nodeUsage.gpuUsagePercent?.toFloatOrNull()?.let { gpuUsage ->
            HardwareUsageBar(
                label = "GPU Usage",
                percentage = gpuUsage,
                color = Color(0xFFF59E0B)
            )
        }

        // SSD 상태
        nodeUsage.ssdHealthPercent?.toFloatOrNull()?.let { ssdHealth ->
            HardwareUsageBar(
                label = "SSD Health",
                percentage = ssdHealth,
                color = Color(0xFF8B5CF6)
            )
        }
    }
}

/**
 * 하드웨어 사용량 바
 */
@Composable
fun HardwareUsageBar(
    label: String,
    percentage: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.1f", percentage)}%",
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    Color(0xFF374151),
                    RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((percentage / 100f).coerceIn(0f, 1f))
                    .background(
                        color,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * NODE_INFO용 분리된 카드들 (Miner Overview, Adjusted Power)
 */
@Composable
fun NodeSeparateCards(
    node: Node,
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    dataCenterName: String = "GY01",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Miner Overview 카드
        NodeMinerOverviewCard(
            hardwareSpec = hardwareSpec,
            nodeUsage = nodeUsage,
            dataCenterName = dataCenterName
        )

        // Adjusted Power 카드
        NodeAdjustedPowerCard(
            hardwareSpec = hardwareSpec,
            nodeUsage = nodeUsage,
            dataCenterName = dataCenterName
        )
    }
}

/**
 * Miner Overview 카드 (크기 확대 및 레이아웃 개선)
 */
@Composable
fun NodeMinerOverviewCard(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    dataCenterName: String = "GY01"
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // 제목을 더 크고 눈에 띄게
            Text(
                text = "MINER OVERVIEW",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 차트와 범례를 가로로 배치
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: 원형 차트
                NodeBalanceChartOnly(
                    hardwareSpec = hardwareSpec,
                    nodeUsage = nodeUsage,
                    dataCenterName = dataCenterName,
                    modifier = Modifier.weight(1f)
                )

                // 오른쪽: 범례
                NodeBalanceLegendOnly(
                    dataCenterName = dataCenterName,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Adjusted Power 카드 (가로 배치)
 */
@Composable
fun NodeAdjustedPowerCard(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    dataCenterName: String = "GY01"
) {
    // 데이터센터별 Power 정보
    val powerInfo = when (dataCenterName.uppercase()) {
        "BC01" -> Pair(
            Triple("Adjusted Power", "2.15 PiB", "Rate: 0.015%"),
            Triple("Total Reward", "2,847.32 FIL", "Win Count: 445")
        )
        "BC02" -> Pair(
            Triple("Adjusted Power", "3.84 PiB", "Rate: 0.02%"),
            Triple("Total Reward", "3,763.23 FIL", "Win Count: 598")
        )
        "GY01" -> Pair(
            Triple("Adjusted Power", "3.88 PiB", "Rate: 0.02%"),
            Triple("Total Reward", "3,426.10 FIL", "Win Count: 557")
        )
        else -> Pair(
            Triple("Adjusted Power", "3.88 PiB", "Rate: 0.02%"),
            Triple("Total Reward", "3,426.10 FIL", "Win Count: 557")
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1F2937)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "ADJUSTED POWER",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // 가로 배치로 변경 (회색 박스로 감싸기)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 왼쪽: Adjusted Power (회색 박스)
                PowerStatCard(
                    modifier = Modifier.weight(1f),
                    title = powerInfo.first.first,
                    value = powerInfo.first.second,
                    subtitle = powerInfo.first.third
                )

                // 오른쪽: Total Reward (회색 박스)
                PowerStatCard(
                    modifier = Modifier.weight(1f),
                    title = powerInfo.second.first,
                    value = powerInfo.second.second,
                    subtitle = powerInfo.second.third
                )
            }
        }
    }
}


/**
 * 원형 차트만 표시하는 컴포넌트 (범례 제외) - 차트 라이브러리 사용
 */
@Composable
fun NodeBalanceChartOnly(
    hardwareSpec: HardwareSpec?,
    nodeUsage: NodeUsage?,
    dataCenterName: String = "GY01",
    modifier: Modifier = Modifier
) {
    // 데이터센터별 Balance 정보 (이미지 참고)
    // Available Balance = Address Balance - Initial Pledge - Locked Rewards
    val balanceInfo = when (dataCenterName.uppercase()) {
        "BC01" -> Triple(
            20143.6398f, // Address Balance
            677.8146f,   // Available Balance
            960.1557f    // Locked Rewards
        )
        "BC02" -> Triple(
            18482.8764f, // Address Balance (이미지 값)
            572.8343f,   // Available Balance
            749.1920f    // Locked Rewards (이미지 값)
        )
        "GY01" -> Triple(
            18100.2043f, // Address Balance (이미지 값)
            475.5337f,   // Available Balance (이미지 값)
            770.1835f    // Locked Rewards (이미지 값)
        )
        else -> Triple(
            18100.2043f, // 기본값 (GY01과 동일)
            475.5337f,
            770.1835f
        )
    }
    
    val addressBalance = balanceInfo.first
    val availableBalance = balanceInfo.second
    val lockedRewards = balanceInfo.third
    val initialPledge = addressBalance - availableBalance - lockedRewards

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(180.dp),
            contentAlignment = Alignment.Center
        ) {
            // 차트 라이브러리를 사용한 도넛형 차트
            PieChart(
                modifier = Modifier.size(180.dp),
                data = listOf(
                    Pie(
                        label = "Available Balance",
                        data = availableBalance.toDouble(),
                        color = Color(0xFF22C55E),
                        selectedColor = Color(0xFF22C55E)
                    ),
                    Pie(
                        label = "Locked Rewards",
                        data = lockedRewards.toDouble(),
                        color = Color(0xFFEA580C),
                        selectedColor = Color(0xFFEA580C)
                    ),
                    Pie(
                        label = "Initial Pledge",
                        data = initialPledge.toDouble(),
                        color = Color(0xFF3B82F6),
                        selectedColor = Color(0xFF3B82F6)
                    )
                ),
                onPieClick = { /* 클릭 이벤트 처리 */ },
                selectedScale = 1.0f,
                style = Pie.Style.Stroke(width = 40.dp)
            )
        }

        // 차트 아래에 Address Balance 정보 표시
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Address Balance",
                fontSize = 16.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${String.format("%.4f", addressBalance)} FIL",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * 범례만 표시하는 컴포넌트
 */
@Composable
fun NodeBalanceLegendOnly(
    dataCenterName: String = "GY01",
    modifier: Modifier = Modifier
) {
    // 데이터센터별 Balance 정보 (이미지 참고)
    // Available Balance = Address Balance - Initial Pledge - Locked Rewards
    val balanceInfo = when (dataCenterName.uppercase()) {
        "BC01" -> Triple(
            20143.6398f, // Address Balance
            677.8146f,   // Available Balance  
            960.1557f    // Locked Rewards
        )
        "BC02" -> Triple(
            18482.8764f, // Address Balance (이미지 값)
            572.8343f,   // Available Balance
            749.1920f    // Locked Rewards (이미지 값)
        )
        "GY01" -> Triple(
            18100.2043f, // Address Balance (이미지 값)
            475.5337f,   // Available Balance (이미지 값)
            770.1835f    // Locked Rewards (이미지 값)
        )
        else -> Triple(
            18100.2043f, // 기본값 (GY01과 동일)
            475.5337f,
            770.1835f
        )
    }
    
    val addressBalance = balanceInfo.first
    val availableBalance = balanceInfo.second
    val lockedRewards = balanceInfo.third
    val initialPledge = addressBalance - availableBalance - lockedRewards
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        NodeBalanceLegendItem(
            color = Color(0xFF22C55E),
            label = "Available Balance",
            value = "${String.format("%.4f", availableBalance)} FIL"
        )
        NodeBalanceLegendItem(
            color = Color(0xFFEA580C),
            label = "Locked Rewards",
            value = "${String.format("%.4f", lockedRewards)} FIL"
        )
        NodeBalanceLegendItem(
            color = Color(0xFF3B82F6),
            label = "Initial Pledge",
            value = "${String.format("%.4f", initialPledge)} FIL"
        )
    }
}



/**
 * Power 통계 카드 (회색 박스, 중앙 정렬)
 */
@Composable
fun PowerStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp), // 고정 높이 설정
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF374151)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 세로 중앙 정렬
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Aethir 노드 정보를 표시하는 카드 컴포넌트
 * 이미지에서 확인한 Aethir 정보들을 표시합니다.
 */
@Composable
fun AethirNodeInfoCard(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Aethir 메인 헤더 카드
//        AethirMainHeaderCard()

        // 지갑 정보 카드 (첫 번째 3개 박스)
        AethirWalletInfoCard()

        // 지갑 잔액 카드 (도넛 차트 + 중간 3개 박스)
        AethirWalletBalanceCard()

        // 스테이킹 정보 카드 (마지막 3개 박스)
        AethirStakingInfoCard()
    }
}

/**
 * Aethir 메인 헤더 카드
 */
//@Composable
//private fun AethirMainHeaderCard() {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(0xFF1F2937)
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "AETHIR NODE INFORMATION",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White,
//                letterSpacing = 1.2.sp
//            )
//        }
//    }
//}

/**
 * Aethir 지갑 정보 카드 (첫 번째 3개 박스)
 */
@Composable
private fun AethirWalletInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "WALLET INFORMATION",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 클레임 가능한 금액들 (첫 번째 3개 박스)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirTokenInfoCard(
                    title = "CLAIMABLE - SERVICE FEE",
                    amount = "0.00",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenInfoCard(
                    title = "CLAIMABLE - POC & POD REWARDS",
                    amount = "158.75",
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenInfoCard(
                    title = "WITHDRAWABLE",
                    amount = "0.0000",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Aethir 지갑 잔액 카드 (도넛 차트 + 3개 박스)
 */
@Composable
private fun AethirWalletBalanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "WALLET BALANCE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Vesting 정보 도넛 차트 (범례가 오른쪽으로 이동됨)
            AethirVestingProgressBar()

            Spacer(modifier = Modifier.height(16.dp))

            // Vesting 관련 정보들 (STAKING INFO에서 다시 이동)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirTokenInfoCard(
                    title = "VESTING CLAIM",
                    amount = "88173.1976",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenInfoCard(
                    title = "VESTING WITHDRAW",
                    amount = "0.0000",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenInfoCard(
                    title = "CASH OUT TOTAL",
                    amount = "149372.4039",
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Aethir 스테이킹 정보 카드 (3개 박스만)
 */
@Composable
private fun AethirStakingInfoCard() {
    // 스테이킹 정보 - 가로 배치 및 강조 표시
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // 스테이킹 정보 헤더 - 개선된 디자인
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(
                            Color(0xFFFBBF24),
                            RoundedCornerShape(2.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "STAKING INFO",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.Default.Savings,
                    contentDescription = "Staking",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 스테이킹 데이터 세로 배치 - 개선된 레이아웃
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AethirStakingTokenCard(
                    title = "STAKED",
                    amount = "209542.8",
                    color = Color(0xFF10B981),
                    modifier = Modifier.fillMaxWidth()
                )
                AethirStakingTokenCard(
                    title = "UNSTAKING",
                    amount = "224115.8",
                    color = Color(0xFFF59E0B),
                    modifier = Modifier.fillMaxWidth()
                )
                AethirStakingTokenCard(
                    title = "UNSTAKED",
                    amount = "0.0000",
                    color = Color(0xFF6B7280),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Aethir 대시보드 정보 카드
 */
@Composable
private fun AethirDashboardInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "RESOURCE OVERVIEW",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 리소스 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirResourceInfoCard(
                    title = "TOTAL LOCATIONS",
                    value = "1.0000",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirResourceInfoCard(
                    title = "TOTAL SERVERS",
                    value = "205296.0000",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirResourceInfoCard(
                    title = "MY AETHIR EARTH",
                    value = "5.0000",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirResourceInfoCard(
                    title = "MY AETHIR ATMOSPHERE",
                    value = "0.0000",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Aethir 수입 정보 카드
 */
@Composable
private fun AethirIncomeInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "DAILY INCOME (2025-07-30)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 수입 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirIncomeItemInfoCard(
                    title = "SERVICE FEE",
                    amount = "0.0000",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirIncomeItemInfoCard(
                    title = "POC REWARD",
                    amount = "645.3498",
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirIncomeItemInfoCard(
                    title = "POD REWARD",
                    amount = "0.0000",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 진행 막대 (Vesting 정보 표시)
            AethirVestingProgressBar()

            Spacer(modifier = Modifier.height(16.dp))

            // 중간 정보들 (Vesting Claim, Vesting Withdraw, Cash Out Total)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AethirTokenInfoCard(
                    title = "VESTING CLAIM",
                    amount = "88173.1976",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenInfoCard(
                    title = "VESTING WITHDRAW",
                    amount = "0.0000",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AethirTokenInfoCard(
                    title = "CASH OUT TOTAL",
                    amount = "149372.4039",
                    isHighlight = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 총합 표시
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF111827)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                        text = "645.3498 ATH",
                        fontSize = 20.sp,
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
 * Aethir 토큰 정보 카드
 */
@Composable
private fun AethirTokenInfoCard(
    title: String,
    amount: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    // 모든 박스를 동일한 회색으로 통일
    val backgroundColor = Color(0xFF374151)
    val textColor = Color.White

    Card(
        modifier = modifier.height(80.dp), // 고정 높이 설정
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 세로 중앙 정렬
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 10.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amount ATH",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * Aethir 리소스 정보 카드
 */
@Composable
private fun AethirResourceInfoCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111827)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF10B981),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Aethir 수입 항목 정보 카드
 */
@Composable
private fun AethirIncomeItemInfoCard(
    title: String,
    amount: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isHighlight) Color(0xFF374151) else Color(0xFF111827)
    val textColor = if (isHighlight) Color(0xFF8B5CF6) else Color.White

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$amount ATH",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Aethir Vesting 정보 도넛형 차트 (범례가 오른쪽에 위치)
 * 이미지에서 확인한 색상 구분을 도넛형 그래프로 구현
 */
@Composable
private fun AethirVestingProgressBar(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 도넛형 차트를 중앙에 배치
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val outerRadius = size.minDimension / 2 * 0.8f
                val innerRadius = outerRadius * 0.6f // 도넛 홀 크기
                val strokeWidth = outerRadius - innerRadius

                // 전체 원 (배경) - 도넛형
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xFF374151),
                    radius = outerRadius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                )

                // 각 섹션의 각도 계산 (전체 360도를 비율로 분배)
                val vestingClaimAngle = 35f * 3.6f  // 35% -> 126도
                val claimableAngle = 18f * 3.6f     // 18% -> 64.8도  
                val cashOutAngle = 47f * 3.6f       // 47% -> 169.2도

                var currentAngle = -90f // 12시 방향부터 시작

                // Vesting Claim 섹션 (초록색)
                drawArc(
                    color = androidx.compose.ui.graphics.Color(0xFF10B981),
                    startAngle = currentAngle,
                    sweepAngle = vestingClaimAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    ),
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                )
                currentAngle += vestingClaimAngle

                // Claimable 섹션 (황색)
                drawArc(
                    color = androidx.compose.ui.graphics.Color(0xFFFBBF24),
                    startAngle = currentAngle,
                    sweepAngle = claimableAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    ),
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                )
                currentAngle += claimableAngle

                // Cash Out 섹션 (빨간색)
                drawArc(
                    color = androidx.compose.ui.graphics.Color(0xFFEF4444),
                    startAngle = currentAngle,
                    sweepAngle = cashOutAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    ),
                    topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                    size = androidx.compose.ui.geometry.Size(outerRadius * 2, outerRadius * 2)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 그래프 아래: 범례 텍스트들 (한 줄로 배치)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AethirDonutLegendItem("Vesting Claim", "88173.20 ATH", Color(0xFF10B981))
            AethirDonutLegendItem("Claimable", "15869.76 ATH", Color(0xFFFBBF24))
            AethirDonutLegendItem("Cash Out", "149372.40 ATH", Color(0xFFEF4444))
        }
    }
}

/**
 * Aethir 진행 막대 라벨
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
 * NDP 트랜잭션 컨테이너 컴포넌트
 * API를 통해 NDP 트랜잭션 데이터를 로드하고 표시상태를 관리합니다.
 */
@Composable
fun NdpTransactionContainer(
    nodeId: String? = null,
    nanodcId: String,
    modifier: Modifier = Modifier
) {
    val repository = remember { NanoDcRepository.getInstance() }
    var ndpTransactions by remember { mutableStateOf<List<NdpTransaction>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // NDP 트랜잭션 데이터 로드
    LaunchedEffect(nodeId, nanodcId) {
        isLoading = true
        errorMessage = null

        try {
            val transactions = repository.getNdpTransactionsWithFallback(
                nodeId = nodeId,
                nanodcId = nanodcId
            )
            ndpTransactions = transactions
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Failed to load NDP transactions: ${e.message}"
            isLoading = false
        }
    }

    when {
        isLoading -> {
            NdpTransactionLoadingCard(modifier = modifier)
        }

        errorMessage != null -> {
            NdpTransactionErrorCard(
                errorMessage = errorMessage!!,
                onRetry = {
                    // 재시도 로직
                    coroutineScope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            val transactions = repository.getNdpTransactionsWithFallback(
                                nodeId = nodeId,
                                nanodcId = nanodcId
                            )
                            ndpTransactions = transactions
                            isLoading = false
                        } catch (e: Exception) {
                            errorMessage = "Failed to load NDP transactions: ${e.message}"
                            isLoading = false
                        }
                    }
                },
                modifier = modifier
            )
        }

        ndpTransactions != null -> {
            // 트랜잭션 목록만 표시 (요약 카드 제거)
            NdpTransactionCard(
                transactions = ndpTransactions!!,
                modifier = modifier
            )
        }
    }
}

/**
 * 도넛 차트 범례 아이템
 */
@Composable
private fun AethirDonutLegendItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Aethir 스테이킹 정보를 표시하는 개별 카드 (확장된 버전)
 */
@Composable
private fun AethirStakingTokenCard(
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
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 스테이킹 상태 아이콘
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, RoundedCornerShape(5.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 제목
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))

            // 금액
            Text(
                text = amount,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))

            // ATH 단위
            Text(
                text = "ATH",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}
