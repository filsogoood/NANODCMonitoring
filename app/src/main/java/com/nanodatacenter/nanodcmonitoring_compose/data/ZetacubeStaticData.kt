package com.nanodatacenter.nanodcmonitoring_compose.data

import com.nanodatacenter.nanodcmonitoring_compose.network.model.Node
import com.nanodatacenter.nanodcmonitoring_compose.network.model.HardwareSpec
import com.nanodatacenter.nanodcmonitoring_compose.network.model.NodeUsage
import com.nanodatacenter.nanodcmonitoring_compose.network.model.Score

/**
 * ZETACUBE 데이터센터용 정적 데이터 제공자
 * ZETACUBE는 API를 사용하지 않고 하드코딩된 데이터를 표시합니다.
 */
object ZetacubeStaticData {

    private const val ZETACUBE_NANODC_ID = "zetacube-0000-0000-0000-000000000000"

    /**
     * ZETACUBE의 이미지 타입별 정적 데이터를 반환합니다.
     *
     * @param imageType 이미지 타입
     * @param imageIndex 이미지 인덱스 (같은 타입이 여러 개일 때 구분)
     * @return ZetacubeNodeData 객체 (노드 정보, 하드웨어 스펙, 사용량, 스코어)
     */
    fun getStaticDataForImage(imageType: ImageType, imageIndex: Int): ZetacubeNodeData? {
        return when (imageType) {
            // 1. NDP 정보 (NDP_INFO)
            ImageType.NDP_INFO -> createNdpInfoData()

            // 2. Filecoin Info (NODE_INFO)
            ImageType.NODE_INFO -> createNodeInfoData()

            // 3. Status (NODE_INFO_AETHIR)
            ImageType.NODE_INFO_AETHIR -> createStatusData()

            // 5. Web UI Server (WEBUI_SERVER)
            ImageType.WEBUI_SERVER -> createWebUiServerData()

            // Web UI Server None (테스트용)
            ImageType.WEBUI_SERVER_NONE -> createWebUiServerData()

            // 6, 8, 9. SAI (SYSTEMTOAI_ACTIVE) - 3개
            ImageType.SYSTEMTOAI_ACTIVE -> createSaiData(imageIndex)

            // 7. Supra (SUPRA)
            ImageType.SUPRA -> createSupraData()

            // 11. Filecoin (FILECOIN_ACTIVE)
            ImageType.FILECOIN_ACTIVE -> createFilecoinData()

            // NAS Storage (DANGSAN 등에서 사용)
            ImageType.STORAGE_NAS -> createStorageData()

            // WORLD IT SHOW SAI 서버들
            ImageType.ZAH200 -> createWorldItShowSaiData(imageType, imageIndex)
            ImageType.ZAH100 -> createWorldItShowSaiData(imageType, imageIndex)
            ImageType.ZAA100 -> createWorldItShowSaiData(imageType, imageIndex)
            ImageType.ZAP6000 -> createWorldItShowSaiData(imageType, imageIndex)
            ImageType.ZA5090 -> createWorldItShowSaiData(imageType, imageIndex)
            ImageType.ZA4090 -> createWorldItShowSaiData(imageType, imageIndex)

            // 인프라 장비는 getInfraDataForImage() 사용
            else -> null
        }
    }

    /**
     * NDP 정보용 정적 데이터
     */
    private fun createNdpInfoData(): ZetacubeNodeData {
        return ZetacubeNodeData(
            node = Node(
                id = 1,
                nodeId = "zetacube-ndp-001",
                userUuid = "zetacube-user-001",
                status = "active",
                createAt = "2024-01-01T00:00:00Z",
                updateAt = "2024-01-01T00:00:00Z",
                nodeName = "ZETACUBE NDP Server",
                nanodcId = ZETACUBE_NANODC_ID
            ),
            hardwareSpec = HardwareSpec(
                id = 1,
                nodeId = "zetacube-ndp-001",
                cpuModel = "AMD EPYC 7763",
                cpuCores = "128",
                gpuModel = "NVIDIA RTX 4090",
                gpuVramGb = "48",
                totalRamGb = "192",
                storageType = "NVMe SSD",
                storageTotalGb = "100000",
                cpuCount = "2",
                gpuCount = "1",
                nvmeCount = "8",
                nanodcId = ZETACUBE_NANODC_ID,
                totalHarddiskGb = "100000"
            ),
            nodeUsage = NodeUsage(
                id = 1,
                nodeId = "zetacube-ndp-001",
                timestamp = "2024-01-01T00:00:00Z",
                cpuUsagePercent = "35",
                memUsagePercent = "48",
                cpuTemp = "42",
                gpuUsagePercent = null,
                gpuTemp = null,
                usedStorageGb = "45000",
                ssdHealthPercent = "98",
                gpuVramPercent = null,
                harddiskUsedPercent = "45",
                stageUsed = null
            ),
            score = Score(
                id = 1,
                nodeId = "zetacube-ndp-001",
                cpuScore = "95",
                gpuScore = "0",
                ssdScore = "98",
                ramScore = "92",
                networkScore = "96",
                hardwareHealthScore = "97",
                totalScore = "478",
                averageScore = "95.6"
            )
        )
    }

    /**
     * NODE_INFO용 정적 데이터 (Filecoin Info)
     */
    private fun createNodeInfoData(): ZetacubeNodeData {
        return ZetacubeNodeData(
            node = Node(
                id = 2,
                nodeId = "zetacube-node-001",
                userUuid = "zetacube-user-001",
                status = "active",
                createAt = "2024-01-01T00:00:00Z",
                updateAt = "2024-01-01T00:00:00Z",
                nodeName = "ZETACUBE Filecoin Info",
                nanodcId = ZETACUBE_NANODC_ID
            ),
            hardwareSpec = HardwareSpec(
                id = 2,
                nodeId = "zetacube-node-001",
                cpuModel = "AMD EPYC 7543",
                cpuCores = "64",
                gpuModel = "NVIDIA RTX 4090",
                gpuVramGb = "48",
                totalRamGb = "192",
                storageType = "NVMe SSD",
                storageTotalGb = "200000",
                cpuCount = "2",
                gpuCount = "1",
                nvmeCount = "16",
                nanodcId = ZETACUBE_NANODC_ID,
                totalHarddiskGb = "200000"
            ),
            nodeUsage = NodeUsage(
                id = 2,
                nodeId = "zetacube-node-001",
                timestamp = "2024-01-01T00:00:00Z",
                cpuUsagePercent = "72",
                memUsagePercent = "65",
                cpuTemp = "58",
                gpuUsagePercent = "45",
                gpuTemp = "52",
                usedStorageGb = "150000",
                ssdHealthPercent = "95",
                gpuVramPercent = "38",
                harddiskUsedPercent = "75",
                stageUsed = null
            ),
            score = Score(
                id = 2,
                nodeId = "zetacube-node-001",
                cpuScore = "92",
                gpuScore = "88",
                ssdScore = "95",
                ramScore = "90",
                networkScore = "94",
                hardwareHealthScore = "93",
                totalScore = "552",
                averageScore = "92.0"
            )
        )
    }

    /**
     * NODE_INFO_AETHIR용 정적 데이터 (Status)
     */
    private fun createStatusData(): ZetacubeNodeData {
        return ZetacubeNodeData(
            node = Node(
                id = 3,
                nodeId = "zetacube-status-001",
                userUuid = "zetacube-user-001",
                status = "active",
                createAt = "2024-01-01T00:00:00Z",
                updateAt = "2024-01-01T00:00:00Z",
                nodeName = "ZETACUBE Status Monitor",
                nanodcId = ZETACUBE_NANODC_ID
            ),
            hardwareSpec = HardwareSpec(
                id = 3,
                nodeId = "zetacube-status-001",
                cpuModel = "Intel Xeon Gold 6338",
                cpuCores = "32",
                gpuModel = "NVIDIA RTX 4090",
                gpuVramGb = "48",
                totalRamGb = "192",
                storageType = "NVMe SSD",
                storageTotalGb = "50000",
                cpuCount = "1",
                gpuCount = "1",
                nvmeCount = "4",
                nanodcId = ZETACUBE_NANODC_ID,
                totalHarddiskGb = "50000"
            ),
            nodeUsage = NodeUsage(
                id = 3,
                nodeId = "zetacube-status-001",
                timestamp = "2024-01-01T00:00:00Z",
                cpuUsagePercent = "28",
                memUsagePercent = "42",
                cpuTemp = "38",
                gpuUsagePercent = null,
                gpuTemp = null,
                usedStorageGb = "20000",
                ssdHealthPercent = "99",
                gpuVramPercent = null,
                harddiskUsedPercent = "40",
                stageUsed = null
            ),
            score = Score(
                id = 3,
                nodeId = "zetacube-status-001",
                cpuScore = "98",
                gpuScore = "0",
                ssdScore = "99",
                ramScore = "97",
                networkScore = "98",
                hardwareHealthScore = "99",
                totalScore = "491",
                averageScore = "98.2"
            )
        )
    }

    /**
     * WEBUI_SERVER용 정적 데이터
     */
    private fun createWebUiServerData(): ZetacubeNodeData {
        return ZetacubeNodeData(
            node = Node(
                id = 4,
                nodeId = "zetacube-webui-001",
                userUuid = "zetacube-user-001",
                status = "active",
                createAt = "2024-01-01T00:00:00Z",
                updateAt = "2024-01-01T00:00:00Z",
                nodeName = "ZETACUBE Web UI Server",
                nanodcId = ZETACUBE_NANODC_ID
            ),
            hardwareSpec = HardwareSpec(
                id = 4,
                nodeId = "zetacube-webui-001",
                cpuModel = "AMD EPYC 7763",
                cpuCores = "128",
                gpuModel = "NVIDIA RTX 4090",
                gpuVramGb = "48",
                totalRamGb = "192",
                storageType = "NVMe SSD",
                storageTotalGb = "500000",
                cpuCount = "2",
                gpuCount = "1",
                nvmeCount = "32",
                nanodcId = ZETACUBE_NANODC_ID,
                totalHarddiskGb = "500000"
            ),
            nodeUsage = NodeUsage(
                id = 4,
                nodeId = "zetacube-webui-001",
                timestamp = "2024-01-01T00:00:00Z",
                cpuUsagePercent = "85",
                memUsagePercent = "78",
                cpuTemp = "65",
                gpuUsagePercent = "92",
                gpuTemp = "68",
                usedStorageGb = "380000",
                ssdHealthPercent = "92",
                gpuVramPercent = "85",
                harddiskUsedPercent = "76",
                stageUsed = null
            ),
            score = Score(
                id = 4,
                nodeId = "zetacube-webui-001",
                cpuScore = "88",
                gpuScore = "94",
                ssdScore = "92",
                ramScore = "86",
                networkScore = "90",
                hardwareHealthScore = "89",
                totalScore = "539",
                averageScore = "89.8"
            )
        )
    }

    /**
     * SAI (SYSTEMTOAI_ACTIVE)용 정적 데이터
     * 인덱스에 따라 다른 SAI 노드 데이터 반환
     * H100 4장 기준, 활성화 노드별로 수치 차별화
     */
    private fun createSaiData(imageIndex: Int): ZetacubeNodeData {
        val saiIndex = when (imageIndex) {
            4 -> 1  // DANGSAN 첫 번째 SAI (활성화)
            5 -> 2  // DANGSAN 두 번째 SAI (활성화) / ZETACUBE 첫 번째 SAI
            7 -> 2  // ZETACUBE 두 번째 SAI (활성화)
            8 -> 3  // ZETACUBE 세 번째 SAI
            else -> 1
        }

        // 활성화 노드별 차별화된 수치
        val (cpuUsage, memUsage, gpuUsage, gpuVramUsage) = when (saiIndex) {
            1 -> listOf(72, 68, 85, 78)  // 첫 번째 활성화: 높은 GPU 사용률
            2 -> listOf(58, 52, 62, 55)  // 두 번째 활성화: 중간 사용률
            else -> listOf(45, 40, 50, 42)
        }

        return ZetacubeNodeData(
            node = Node(
                id = 10 + saiIndex,
                nodeId = "zetacube-sai-00$saiIndex",
                userUuid = "zetacube-user-001",
                status = "active",
                createAt = "2024-01-01T00:00:00Z",
                updateAt = "2024-01-01T00:00:00Z",
                nodeName = "ZETACUBE SAI Server $saiIndex",
                nanodcId = ZETACUBE_NANODC_ID
            ),
            hardwareSpec = HardwareSpec(
                id = 10 + saiIndex,
                nodeId = "zetacube-sai-00$saiIndex",
                cpuModel = "AMD EPYC 9654",
                cpuCores = "96",
                gpuModel = "NVIDIA H100",
                gpuVramGb = "384",
                totalRamGb = "768",
                storageType = "NVMe SSD",
                storageTotalGb = "100000",
                cpuCount = "2",
                gpuCount = "4",
                nvmeCount = "8",
                nanodcId = ZETACUBE_NANODC_ID,
                totalHarddiskGb = "100000"
            ),
            nodeUsage = NodeUsage(
                id = 10 + saiIndex,
                nodeId = "zetacube-sai-00$saiIndex",
                timestamp = "2024-01-01T00:00:00Z",
                cpuUsagePercent = "$cpuUsage",
                memUsagePercent = "$memUsage",
                cpuTemp = "${45 + saiIndex * 3}",
                gpuUsagePercent = "$gpuUsage",
                gpuTemp = "${52 + saiIndex * 4}",
                usedStorageGb = "${45000 + saiIndex * 5000}",
                ssdHealthPercent = "${96 - saiIndex}",
                gpuVramPercent = "$gpuVramUsage",
                harddiskUsedPercent = "${45 + saiIndex * 5}",
                stageUsed = null
            ),
            score = Score(
                id = 10 + saiIndex,
                nodeId = "zetacube-sai-00$saiIndex",
                cpuScore = "${90 + saiIndex}",
                gpuScore = "${92 + saiIndex}",
                ssdScore = "${94 - saiIndex}",
                ramScore = "${88 + saiIndex}",
                networkScore = "${91 + saiIndex}",
                hardwareHealthScore = "${93 - saiIndex}",
                totalScore = "${548 + saiIndex * 3}",
                averageScore = "${91.3 + saiIndex * 0.5}"
            )
        )
    }

    /**
     * SUPRA용 정적 데이터
     */
    private fun createSupraData(): ZetacubeNodeData {
        return ZetacubeNodeData(
            node = Node(
                id = 5,
                nodeId = "zetacube-supra-001",
                userUuid = "zetacube-user-001",
                status = "active",
                createAt = "2024-01-01T00:00:00Z",
                updateAt = "2024-01-01T00:00:00Z",
                nodeName = "ZETACUBE Supra Worker",
                nanodcId = ZETACUBE_NANODC_ID
            ),
            hardwareSpec = HardwareSpec(
                id = 5,
                nodeId = "zetacube-supra-001",
                cpuModel = "AMD EPYC 7713",
                cpuCores = "64",
                gpuModel = "NVIDIA RTX 4090",
                gpuVramGb = "48",
                totalRamGb = "192",
                storageType = "NVMe SSD",
                storageTotalGb = "150000",
                cpuCount = "2",
                gpuCount = "1",
                nvmeCount = "12",
                nanodcId = ZETACUBE_NANODC_ID,
                totalHarddiskGb = "150000"
            ),
            nodeUsage = NodeUsage(
                id = 5,
                nodeId = "zetacube-supra-001",
                timestamp = "2024-01-01T00:00:00Z",
                cpuUsagePercent = "55",
                memUsagePercent = "62",
                cpuTemp = "52",
                gpuUsagePercent = "48",
                gpuTemp = "45",
                usedStorageGb = "85000",
                ssdHealthPercent = "97",
                gpuVramPercent = "42",
                harddiskUsedPercent = "57",
                stageUsed = null
            ),
            score = Score(
                id = 5,
                nodeId = "zetacube-supra-001",
                cpuScore = "93",
                gpuScore = "91",
                ssdScore = "97",
                ramScore = "89",
                networkScore = "94",
                hardwareHealthScore = "95",
                totalScore = "559",
                averageScore = "93.2"
            )
        )
    }

    /**
     * FILECOIN_ACTIVE용 정적 데이터
     */
    private fun createFilecoinData(): ZetacubeNodeData {
        return ZetacubeNodeData(
            node = Node(
                id = 6,
                nodeId = "zetacube-filecoin-001",
                userUuid = "zetacube-user-001",
                status = "active",
                createAt = "2024-01-01T00:00:00Z",
                updateAt = "2024-01-01T00:00:00Z",
                nodeName = "ZETACUBE Filecoin Storage",
                nanodcId = ZETACUBE_NANODC_ID
            ),
            hardwareSpec = HardwareSpec(
                id = 6,
                nodeId = "zetacube-filecoin-001",
                cpuModel = "AMD EPYC 7763",
                cpuCores = "128",
                gpuModel = "NVIDIA RTX 4090",
                gpuVramGb = "48",
                totalRamGb = "192",
                storageType = "NVMe SSD",
                storageTotalGb = "1000000",
                cpuCount = "2",
                gpuCount = "1",
                nvmeCount = "64",
                nanodcId = ZETACUBE_NANODC_ID,
                totalHarddiskGb = "1000000"
            ),
            nodeUsage = NodeUsage(
                id = 6,
                nodeId = "zetacube-filecoin-001",
                timestamp = "2024-01-01T00:00:00Z",
                cpuUsagePercent = "68",
                memUsagePercent = "72",
                cpuTemp = "58",
                gpuUsagePercent = "55",
                gpuTemp = "50",
                usedStorageGb = "820000",
                ssdHealthPercent = "94",
                gpuVramPercent = "48",
                harddiskUsedPercent = "82",
                stageUsed = null
            ),
            score = Score(
                id = 6,
                nodeId = "zetacube-filecoin-001",
                cpuScore = "89",
                gpuScore = "87",
                ssdScore = "94",
                ramScore = "85",
                networkScore = "92",
                hardwareHealthScore = "91",
                totalScore = "538",
                averageScore = "89.7"
            )
        )
    }

    /**
     * STORAGE_2용 정적 데이터 (DANGSAN 스토리지 서버)
     */
    private fun createStorageData(): ZetacubeNodeData {
        return ZetacubeNodeData(
            node = Node(
                id = 20,
                nodeId = "dangsan-storage-001",
                userUuid = "dangsan-user-001",
                status = "active",
                createAt = "2025-06-01T00:00:00Z",
                updateAt = "2025-06-01T00:00:00Z",
                nodeName = "ZETACUBE NAS Storage",
                nanodcId = "dangsan-0000-0000-0000-000000000000"
            ),
            hardwareSpec = HardwareSpec(
                id = 20,
                nodeId = "dangsan-storage-001",
                cpuModel = "Intel Xeon E-2388G",
                cpuCores = "8",
                gpuModel = "N/A",
                gpuVramGb = "0",
                totalRamGb = "64",
                storageType = "HDD RAID-6",
                storageTotalGb = "240000",
                cpuCount = "1",
                gpuCount = "0",
                nvmeCount = "2",
                nanodcId = "dangsan-0000-0000-0000-000000000000",
                totalHarddiskGb = "240000"
            ),
            nodeUsage = NodeUsage(
                id = 20,
                nodeId = "dangsan-storage-001",
                timestamp = "2025-06-01T00:00:00Z",
                cpuUsagePercent = "18",
                memUsagePercent = "42",
                cpuTemp = "38",
                gpuUsagePercent = null,
                gpuTemp = null,
                usedStorageGb = "156000",
                ssdHealthPercent = "8",
                gpuVramPercent = null,
                harddiskUsedPercent = "65",
                stageUsed = null
            ),
            score = Score(
                id = 20,
                nodeId = "dangsan-storage-001",
                cpuScore = "88",
                gpuScore = "0",
                ssdScore = "96",
                ramScore = "90",
                networkScore = "94",
                hardwareHealthScore = "92",
                totalScore = "460",
                averageScore = "92.0"
            )
        )
    }

    /**
     * WORLD IT SHOW SAI 서버용 정적 데이터
     * 장비 타입별로 GPU/스펙 차별화
     */
    private fun createWorldItShowSaiData(imageType: ImageType, imageIndex: Int): ZetacubeNodeData {
        data class SaiSpec(val gpuModel: String, val gpuVram: String, val gpuCount: String, val cpuModel: String, val cpuCores: String, val ram: String, val storage: String)
        val spec = when (imageType) {
            ImageType.ZAH200 -> SaiSpec("NVIDIA H200", "576", "4", "AMD EPYC 9654", "96", "1536", "100000")
            ImageType.ZAH100 -> SaiSpec("NVIDIA H100", "384", "4", "AMD EPYC 9654", "96", "768", "100000")
            ImageType.ZAA100 -> SaiSpec("NVIDIA A100", "320", "4", "AMD EPYC 7763", "128", "512", "80000")
            ImageType.ZAP6000 -> SaiSpec("NVIDIA RTX 6000 Ada", "192", "4", "AMD EPYC 9354", "64", "512", "60000")
            ImageType.ZA5090 -> SaiSpec("NVIDIA RTX 5090", "128", "4", "AMD EPYC 9554", "64", "256", "40000")
            ImageType.ZA4090 -> SaiSpec("NVIDIA RTX 4090", "96", "4", "AMD EPYC 7543", "64", "192", "40000")
            else -> SaiSpec("NVIDIA H100", "384", "4", "AMD EPYC 9654", "96", "768", "100000")
        }

        val (cpuUsage, memUsage, gpuUsage, gpuVramUsage) = when (imageType) {
            ImageType.ZAH200 -> listOf(78, 72, 92, 85)
            ImageType.ZAH100 -> listOf(72, 68, 85, 78)
            ImageType.ZAA100 -> listOf(65, 60, 78, 70)
            ImageType.ZAP6000 -> listOf(58, 55, 72, 65)
            ImageType.ZA5090 -> listOf(52, 48, 68, 60)
            ImageType.ZA4090 -> listOf(45, 42, 62, 55)
            else -> listOf(50, 50, 50, 50)
        }

        val saiIndex = when (imageType) {
            ImageType.ZAH200 -> 1
            ImageType.ZAH100 -> 2
            ImageType.ZAA100 -> 3
            ImageType.ZAP6000 -> 4
            ImageType.ZA5090 -> 5
            ImageType.ZA4090 -> 6
            else -> 1
        }

        val nodeName = when (imageType) {
            ImageType.ZAH200 -> "ZAH200 SAI Server"
            ImageType.ZAH100 -> "ZAH100 SAI Server"
            ImageType.ZAA100 -> "ZAA100 SAI Server"
            ImageType.ZAP6000 -> "ZAP6000 SAI Server"
            ImageType.ZA5090 -> "ZA5090 SAI Server"
            ImageType.ZA4090 -> "ZA4090 SAI Server"
            else -> "SAI Server"
        }

        return ZetacubeNodeData(
            node = Node(
                id = 100 + saiIndex,
                nodeId = "wis-sai-00$saiIndex",
                userUuid = "wis-user-001",
                status = "active",
                createAt = "2026-03-26T00:00:00Z",
                updateAt = "2026-03-26T00:00:00Z",
                nodeName = nodeName,
                nanodcId = DataCenterType.WORLD_IT_SHOW.nanoDcId
            ),
            hardwareSpec = HardwareSpec(
                id = 100 + saiIndex,
                nodeId = "wis-sai-00$saiIndex",
                cpuModel = spec.cpuModel,
                cpuCores = spec.cpuCores,
                gpuModel = spec.gpuModel,
                gpuVramGb = spec.gpuVram,
                totalRamGb = spec.ram,
                storageType = "NVMe SSD",
                storageTotalGb = spec.storage,
                cpuCount = "2",
                gpuCount = spec.gpuCount,
                nvmeCount = "8",
                nanodcId = DataCenterType.WORLD_IT_SHOW.nanoDcId,
                totalHarddiskGb = spec.storage
            ),
            nodeUsage = NodeUsage(
                id = 100 + saiIndex,
                nodeId = "wis-sai-00$saiIndex",
                timestamp = "2026-03-26T00:00:00Z",
                cpuUsagePercent = "$cpuUsage",
                memUsagePercent = "$memUsage",
                cpuTemp = "${42 + saiIndex * 3}",
                gpuUsagePercent = "$gpuUsage",
                gpuTemp = "${50 + saiIndex * 4}",
                usedStorageGb = "${(spec.storage.toLong() * 0.6).toLong()}",
                ssdHealthPercent = "${97 - saiIndex}",
                gpuVramPercent = "$gpuVramUsage",
                harddiskUsedPercent = "60",
                stageUsed = null
            ),
            score = Score(
                id = 100 + saiIndex,
                nodeId = "wis-sai-00$saiIndex",
                cpuScore = "${92 - saiIndex}",
                gpuScore = "${95 - saiIndex}",
                ssdScore = "${96 - saiIndex}",
                ramScore = "${90 - saiIndex}",
                networkScore = "${93 - saiIndex}",
                hardwareHealthScore = "${94 - saiIndex}",
                totalScore = "${560 - saiIndex * 6}",
                averageScore = "${93.3 - saiIndex * 1.0}"
            )
        )
    }

    /**
     * 100G Switch용 정적 데이터
     * 네트워크 스위치에 맞는 정보만 포함
     */
    private fun createSwitch100GData(): ZetacubeInfraData {
        return ZetacubeInfraData(
            name = "100G Network Switch",
            status = "Online",
            specs = mapOf(
                "Ports" to "64 x 100GbE",
                "Throughput" to "12.8 Tbps"
            ),
            usage = mapOf(
                "Active Ports" to "48 / 64",
                "Uptime" to "99.99%"
            ),
            graphMetrics = listOf(
                InfraGraphMetric(
                    label = "Port Usage",
                    percentage = 75f, // 48/64 = 75%
                    value = "48 / 64",
                    color = 0xFF3B82F6 // Blue
                ),
                InfraGraphMetric(
                    label = "Traffic Load",
                    percentage = 68f,
                    value = "8.7 Tbps",
                    color = 0xFF10B981 // Green
                ),
                InfraGraphMetric(
                    label = "Buffer Usage",
                    percentage = 42f,
                    value = "42%",
                    color = 0xFFF59E0B // Amber
                ),
                InfraGraphMetric(
                    label = "CPU Load",
                    percentage = 28f,
                    value = "28%",
                    color = 0xFF8B5CF6 // Purple
                )
            )
        )
    }

    /**
     * UPS Controller용 정적 데이터
     * UPS 장비에 맞는 정보만 포함
     */
    private fun createUpsControllerData(): ZetacubeInfraData {
        return ZetacubeInfraData(
            name = "UPS Power Controller",
            status = "Normal",
            specs = mapOf(
                "Capacity" to "10 kVA"
            ),
            usage = mapOf(
                "Load" to "65%",
                "Battery" to "100%"
            ),
            graphMetrics = listOf(
                InfraGraphMetric(
                    label = "Load",
                    percentage = 65f,
                    value = "6.5 kVA",
                    color = 0xFF3B82F6 // Blue
                ),
                InfraGraphMetric(
                    label = "Battery",
                    percentage = 100f,
                    value = "100%",
                    color = 0xFF10B981 // Green
                ),
                InfraGraphMetric(
                    label = "Efficiency",
                    percentage = 94f,
                    value = "94%",
                    color = 0xFFF59E0B // Amber
                ),
                InfraGraphMetric(
                    label = "Temperature",
                    percentage = 50f, // 25°C / 50°C max = 50%
                    value = "25°C",
                    color = 0xFFEF4444 // Red
                )
            )
        )
    }

    /**
     * 인프라 장비용 데이터 반환 (Switch, UPS 등)
     */
    fun getInfraDataForImage(imageType: ImageType): ZetacubeInfraData? {
        return when (imageType) {
            ImageType.SWITCH_100G -> createSwitch100GData()
            ImageType.UPS_CONTROLLER -> createUpsControllerData()
            ImageType.WLS_SMARTUPS -> createWlsSmartUpsData()
            else -> null
        }
    }

    /**
     * WLS SmartUPS용 정적 데이터
     */
    private fun createWlsSmartUpsData(): ZetacubeInfraData {
        return ZetacubeInfraData(
            name = "UPS",
            status = "Normal",
            specs = mapOf(
                "Capacity" to "10 kVA"
            ),
            usage = mapOf(
                "Load" to "65%",
                "Battery" to "100%"
            ),
            graphMetrics = listOf(
                InfraGraphMetric(
                    label = "Load",
                    percentage = 65f,
                    value = "6.5 kVA",
                    color = 0xFF3B82F6
                ),
                InfraGraphMetric(
                    label = "Battery",
                    percentage = 100f,
                    value = "100%",
                    color = 0xFF10B981
                ),
                InfraGraphMetric(
                    label = "Efficiency",
                    percentage = 94f,
                    value = "94%",
                    color = 0xFFF59E0B
                ),
                InfraGraphMetric(
                    label = "Temperature",
                    percentage = 50f,
                    value = "25°C",
                    color = 0xFFEF4444
                )
            )
        )
    }

    /**
     * ZETACUBE가 선택되었는지 확인
     */
    fun isZetacubeSelected(nanoDcId: String): Boolean {
        return nanoDcId == DataCenterType.ZETACUBE.nanoDcId
    }

    /**
     * MOALIFEPLUS가 선택되었는지 확인
     */
    fun isMoalifeplusSelected(nanoDcId: String): Boolean {
        return nanoDcId == DataCenterType.MOALIFEPLUS.nanoDcId
    }

    /**
     * DANGSAN이 선택되었는지 확인
     */
    fun isDangsanSelected(nanoDcId: String): Boolean {
        return nanoDcId == DataCenterType.DANGSAN.nanoDcId
    }

    /**
     * WORLD IT SHOW가 선택되었는지 확인
     */
    fun isWorldItShowSelected(nanoDcId: String): Boolean {
        return nanoDcId == DataCenterType.WORLD_IT_SHOW.nanoDcId
    }

    /**
     * 정적 데이터 사용 데이터센터인지 확인 (ZETACUBE, MOALIFEPLUS, DANGSAN, WORLD_IT_SHOW)
     */
    fun isStaticDataCenter(nanoDcId: String): Boolean {
        return isZetacubeSelected(nanoDcId) || isMoalifeplusSelected(nanoDcId) || isDangsanSelected(nanoDcId) || isWorldItShowSelected(nanoDcId)
    }
}

/**
 * ZETACUBE 노드 데이터 클래스
 */
data class ZetacubeNodeData(
    val node: Node,
    val hardwareSpec: HardwareSpec?,
    val nodeUsage: NodeUsage?,
    val score: Score?
)

/**
 * ZETACUBE 인프라 장비 데이터 클래스 (Switch, UPS 등)
 */
data class ZetacubeInfraData(
    val name: String,
    val status: String,
    val specs: Map<String, String>,
    val usage: Map<String, String>,
    val graphMetrics: List<InfraGraphMetric> = emptyList()
)

/**
 * 인프라 장비 그래프 메트릭 데이터
 */
data class InfraGraphMetric(
    val label: String,
    val percentage: Float,
    val value: String,
    val color: Long // Color value as Long (e.g., 0xFF10B981)
)
