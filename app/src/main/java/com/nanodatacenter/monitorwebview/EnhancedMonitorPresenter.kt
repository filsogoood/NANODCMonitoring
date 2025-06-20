package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView
import kotlin.random.Random

/**
 * Utility class for enhanced monitoring data display
 * Provides improved UI/UX with visual elements and animations
 */
class EnhancedMonitorPresenter(private val context: Context) {

    /**
     * Colors and icons based on server status
     */
    enum class ServerStatus(val colorCode: String, val iconRes: Int) {
        NORMAL("#4CAF50", R.drawable.rack_info),       // Normal (Green)
        WARNING("#FF9800", R.drawable.reboot),        // Warning (Orange)
        ERROR("#F44336", R.drawable.shut_down)        // Error (Red)
    }

    /**
     * Create enhanced monitoring card - 서버 타입에 따라 다른 표시 방식 적용
     */
    fun createEnhancedMonitorCard(
        title: String,
        serverType: String,
        data: String,
        status: ServerStatus = ServerStatus.NORMAL
    ): View {
        // 화면 크기 적응형 레이아웃 유틸리티 사용
        val screenAdapter = ScreenAdaptiveLayout(context)
        // 화면 너비 확인
        val isNarrowScreen = screenAdapter.isNarrowScreen
        val isVeryNarrowScreen = screenAdapter.isVeryNarrowScreen

        // Check status from first line of data
        var statusToUse = status
        val lines = data.split("\n")
        for (line in lines) {
            if (line.contains("Status:")) {
                statusToUse = when {
                    line.contains("Warning") -> ServerStatus.WARNING
                    line.contains("Error") || line.contains("Alert") -> ServerStatus.ERROR
                    else -> status
                }
                break
            }
        }

        // Top container - modified
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // 여백 제거하여 전체 화면 너비 활용
            setPadding(0, 0, 0, 0)
        }

        // Create material card view - 좁은 화면에서 여백 더 줄임
        val cardView = MaterialCardView(context).apply {
            radius = 16f
            cardElevation = 12f
            setCardBackgroundColor(Color.parseColor("#0D2C54"))  // Dark blue
            strokeColor = Color.parseColor(statusToUse.colorCode)
            strokeWidth = 2

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // 좁은 화면에서는 여백 완전히 제거
            params.setMargins(
                if (isVeryNarrowScreen) 0 else if (isNarrowScreen) 2 else 5,
                0,
                if (isVeryNarrowScreen) 0 else if (isNarrowScreen) 2 else 5,
                0
            )
            layoutParams = params
        }

        // Inner container - 패딩 더 줄임
        val innerContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // 좁은 화면에서는 패딩 더 줄임
            val horizontalPadding = when {
                isVeryNarrowScreen -> 10
                isNarrowScreen -> 12
                else -> 16
            }
            setPadding(horizontalPadding, 16, horizontalPadding, 16)
        }

        // Header area
        val headerLayout = createHeaderLayout(title, serverType, statusToUse, isNarrowScreen)
        innerContainer.addView(headerLayout)

        // Divider
        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
            setPadding(0, 0, 0, 0)
        }
        innerContainer.addView(divider)

        // Graph and status display area - 특정 서버 타입에 따라 다르게 처리
        val metricsLayout = LinearLayout(context).apply {
            orientation = if (isNarrowScreen) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // 좁은 화면에서는 패딩 줄임
            val verticalPadding = if (isVeryNarrowScreen) 8 else 12
            setPadding(0, verticalPadding, 0, verticalPadding)
            gravity = Gravity.CENTER_VERTICAL
        }
        when {
            serverType.contains("Filecoin Storage") || title.contains("Filecoin Storage") -> {
                setupFileCoinMetrics(metricsLayout, serverType, title)
            }

            // 특정 RTX GPU 서버 처리 (index = 11, 13)
            serverType.contains("NVIDA RTX 3090") -> {
                // 기존 코드 유지
                if (title.contains("GPU Server") && !title.contains("Aethir")) {
                    setupGpuCircularCharts(metricsLayout, 5, 25.6f, 10.2f, "")
                } else {
                    setupGpuCircularCharts(metricsLayout, 65, 90.4f, 18.7f, "")
                }
            }

            // GPU Server (index = 10)를 원형 차트로 변경 - Aethir가 아닌 일반 GPU Server
            (title.contains("GPU Server") && !title.contains("Aethir") && serverType.contains("Server")) -> {
                // GPU Server용 원형 차트로 변경  
                setupGpuCircularCharts(metricsLayout, 40, 60.5f, 15.3f, "")
            }

            // GPU Server Aethir (index = 12)를 원형 차트로 변경
            serverType.contains("Aethir") -> {
                // Aethir Server용 원형 차트로 변경
                setupGpuCircularCharts(metricsLayout, 75, 105.2f, 20.1f, "")
            }

            // 나머지 GPU 서버 처리 (Aethir가 아닌 경우)
            (serverType.contains("GPU") && !serverType.contains("GPU Server RTX") && !serverType.contains("Aethir") && !title.contains("GPU Server")) ||
                    (title.contains("GPU") && serverType.contains("Server") && !title.contains("GPU Server") && !serverType.contains("Aethir")) -> {
                setupGpuServerMetrics(metricsLayout, serverType, title)
            }

            // Storage servers display space usage and I/O performance
            serverType.contains("Storage") -> {
                setupFileCoinMetrics(metricsLayout, serverType, title)
            }

            // 마이너 노드 - 더 큰 원형 그래프
            serverType.contains("Miner Node") -> {
                setupMinerNodeMetrics(metricsLayout)
            }

            // 포스트 워커 - 이제 원형 차트로 변경됨
            serverType.contains("Post Worker") -> {
                setupPostWorkerMetrics(metricsLayout)
            }

            // 서버 1, 2, 3 - 커스텀 원형 그래프
            serverType.contains("SUPRA WORKER") -> {
                setupServerMetrics(metricsLayout, 1)
            }

            serverType.contains("Server 2") || serverType.contains("Compute Server 2") -> {
                setupServerMetrics(metricsLayout, 2)
            }

            serverType.contains("Server 3") || serverType.contains("Compute Server 3") -> {
                setupServerMetrics(metricsLayout, 3)
            }

            // 일반 GPU 서버 (Aethir 등)
            serverType.contains("Aethir") ||
                    (serverType.contains("GPU") && !serverType.contains("GPU Server RTX")) ||
                    (title.contains("GPU") && serverType.contains("Server")) -> {
                setupGpuServerMetrics(metricsLayout, serverType, title)
            }

            // Computing servers display CPU, memory, disk metrics in circular form
            serverType.contains("Compute") || serverType.contains("Node") || serverType.contains("Server") -> {
                setupComputeServerMetrics(metricsLayout)
            }

            // Network equipment displays traffic and connection status
            serverType.contains("Network") || serverType.contains("Switch") -> {
                setupNetworkMetrics(metricsLayout)
            }

            // Other general servers
            else -> {
                setupGeneralMetrics(metricsLayout)
            }
        }

        innerContainer.addView(metricsLayout)

        // Text data area
        val skipDataText = true

        if (!skipDataText) {
            val dataTextView = createDataTextView(data, isNarrowScreen)
            innerContainer.addView(dataTextView)
        }

        // 실시간 정보 업데이트. 하드코딩에서는 도저히 값을 동일하게 못하겠어서 뺌. 애초에 넣을 필요가 있었나?
        val skipUpdateView = true

        if (!skipUpdateView) {
            val updateContainer = createLiveUpdateView(serverType, isNarrowScreen, title)
            innerContainer.addView(updateContainer)
        }

        cardView.addView(innerContainer)
        container.addView(cardView)

        // Apply card animation
        applyCardAnimation(cardView)

        return container
    }

    /**
     * Create header area
     */
    private fun createHeaderLayout(
        title: String,
        serverType: String,
        status: ServerStatus,
        isNarrowScreen: Boolean
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 16)

            // Icon
            val iconView = ImageView(context).apply {
                setImageResource(status.iconRes)
                layoutParams = LinearLayout.LayoutParams(
                    if (isNarrowScreen) 40 else 48,
                    if (isNarrowScreen) 40 else 48
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
                // Change icon color
                setColorFilter(Color.parseColor(status.colorCode))
            }

            // Text container
            val textContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                ).apply {
                    marginStart = if (isNarrowScreen) 8 else 16
                    gravity = Gravity.CENTER_VERTICAL
                }
            }

            // Title - 좁은 화면에서는 텍스트 크기 줄임
            val titleView = TextView(context).apply {
                text = title
                textSize = if (isNarrowScreen) 16f else 20f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Server type - 좁은 화면에서는 텍스트 크기 줄임
            val typeView = TextView(context).apply {
                text = serverType
                textSize = if (isNarrowScreen) 12f else 16f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Status indicator - 좁은 화면에서는 크기 줄임
            val statusView = TextView(context).apply {
                text = when(status) {
                    ServerStatus.NORMAL -> "Normal"
                    ServerStatus.WARNING -> "Warning"
                    ServerStatus.ERROR -> "Alert"
                }
                textSize = if (isNarrowScreen) 12f else 16f
                setTextColor(Color.parseColor(status.colorCode))
                setBackgroundResource(android.R.drawable.editbox_background)
                background.setTint(Color.parseColor("#33" + status.colorCode.substring(1)))
                setPadding(
                    if (isNarrowScreen) 8 else 16,
                    if (isNarrowScreen) 4 else 8,
                    if (isNarrowScreen) 8 else 16,
                    if (isNarrowScreen) 4 else 8
                )
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
            }

            textContainer.addView(titleView)
            textContainer.addView(typeView)

            addView(iconView)
            addView(textContainer)
            addView(statusView)
        }
    }

    /**
     * Create text data view - 화면 크기에 따라 폰트 크기 조정
     */
    private fun createDataTextView(data: String, isNarrowScreen: Boolean): TextView {
        return TextView(context).apply {
            textSize = if (isNarrowScreen) 14f else 18f
            setTextColor(Color.parseColor("#E0E0E0"))
            setPadding(8, 16, 8, 8)

            // Data formatting
            val lines = data.split("\n")
            val formattedText = StringBuilder()

            for (line in lines) {
                if (line.startsWith("#")) {
                    // Change style for title rows
                    val cleanLine = line.substring(1).trim()
                    formattedText.append("• $cleanLine\n")
                } else {
                    formattedText.append("  $line\n")
                }
            }

            text = formattedText.toString()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    /**
     * Create real-time update view - 화면 크기에 따라 텍스트 크기 조정
     */
    private fun createLiveUpdateView(serverType: String, isNarrowScreen: Boolean, title: String): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 0)

            // Update icon
            val iconView = ImageView(context).apply {
                setImageResource(R.drawable.reboot)
                layoutParams = LinearLayout.LayoutParams(
                    if (isNarrowScreen) 24 else 32,
                    if (isNarrowScreen) 24 else 32
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    marginEnd = 8
                }
                setColorFilter(Color.parseColor("#4CAF50"))
            }

            // Update text - 좁은 화면에서는 텍스트 크기 줄임
            val updateText = TextView(context).apply {
                textSize = if (isNarrowScreen) 12f else 16f
                setTextColor(Color.parseColor("#4CAF50"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            addView(iconView)
            addView(updateText)

            // Real-time update simulation
            val handler = Handler()
            handler.post(object : Runnable {
                override fun run() {
                    // Different update messages based on server type
                    when {
                        serverType.contains("Storage") -> {
                            val temperature = Random.nextInt(37, 48)
                            val ioRate = Random.nextInt(100, 600)
                            updateText.text = ""
                        }
                        serverType.contains("Network") -> {
                            val traffic = Random.nextInt(100, 900)
                            val packets = Random.nextInt(1000, 5000)
                            updateText.text = ""
                        }
                        serverType.contains("GPU") || serverType.contains("Aethir") -> {
                            val gpu = Random.nextInt(70, 95)
                            val memory = Random.nextInt(50, 70)
                            val rewards = Random.nextDouble(10.0, 15.0)

                            // Aethir 서버인 경우 ATH로 표시, 아닌 경우 FIL로 표시
                            val rewardUnit = if (serverType.contains("Aethir") || title.contains("Aethir")) "ATH" else "FIL"

                            updateText.text = "Rewards: +${String.format("%.1f", rewards)} ${rewardUnit}/hr"
                        }
                        else -> {
                            val cpu = Random.nextInt(20, 95)
                            val memory = Random.nextInt(6, 14)
                            updateText.text = ""
                        }
                    }

                    // Icon blinking effect
                    iconView.alpha = 1.0f
                    iconView.animate().alpha(0.4f).setDuration(500).withEndAction {
                        iconView.alpha = 1.0f
                    }

                    handler.postDelayed(this, 3000)
                }
            })
        }
    }

    /**
     * Apply card animation
     */
    private fun applyCardAnimation(cardView: MaterialCardView) {
        val animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in).apply {
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }
        cardView.startAnimation(animation)
    }

    /**
     * 마이너 노드 메트릭스 설정 - 더 큰 원형 그래프
     */
    private fun setupMinerNodeMetrics(container: LinearLayout) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 항상 수직으로 배치
        container.orientation = LinearLayout.VERTICAL

        // CPU 사용량 표시 - 높이 감소
        val cpuContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(0, 16, 0, 0) // 상단 패딩 감소
        }

        // 더 작은 원형 차트 - 마이너 노드용
        val circleHeight = if (isVeryNarrowScreen) 180 else 200 // 높이 감소 (240 → 180~200)

        val cpuProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setCpuUsage(67)
        }
        cpuContainer.addView(cpuProgress)

        // Memory 사용량 표시 - 높이 감소
        val memoryContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        //Miner Node Memeory
        val memoryProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setMemoryUsage(11.2f, 16f)
        }
        memoryContainer.addView(memoryProgress)

        // Disk 사용량 표시 - 높이 감소
        val diskContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val diskProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setDiskUsage(Random.nextInt(30, 60).toFloat(), 100f)
        }
        diskContainer.addView(diskProgress)
        container.addView(cpuContainer)
        container.addView(memoryContainer)
        container.addView(diskContainer)
    }


    /**
     * 서버1, 서버2, 서버3 전용 메트릭스 설정 - 더 큰 원형 그래프와 하단 텍스트
     */
    private fun setupServerMetrics(container: LinearLayout, serverNumber: Int) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 항상 수직으로 배치
        container.orientation = LinearLayout.VERTICAL

        // 더 작은 원형 차트 높이
        val circleHeight = if (isVeryNarrowScreen) 170 else 190 // 높이 감소 (220 → 170~190)

        // CPU 사용량 표시
        val cpuProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setCpuUsage(if (serverNumber == 1) 88 else Random.nextInt(60, 95))
        }

        // Memory 사용량 표시
        val memoryProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setMemoryUsage(8f, 16f)
        }

        // Disk 사용량 표시
        val diskProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setDiskUsage(if (serverNumber == 1) 49f else Random.nextInt(30, 70).toFloat(), 100f)
        }

        container.addView(cpuProgress)
        container.addView(memoryProgress)
        container.addView(diskProgress)
    }

    /**
     * Post Worker 등 막대 그래프로 표시하는 서버에 텍스트 정보 추가
     * 공백 줄이기 위해 간격 최적화
     */
    private fun setupPostWorkerMetrics(container: LinearLayout) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 항상 수직으로 배치
        container.orientation = LinearLayout.VERTICAL

        // CPU 사용량 표시 - 높이 감소
        val cpuContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(0, 16, 0, 0) // 상단 패딩 감소
        }

        // 원형 차트 - Post Worker 수치
        val circleHeight = if (isVeryNarrowScreen) 180 else 200 // 높이 감소 (240 → 180~200)

        val cpuProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setCpuUsage(48) // Post Worker CPU 사용량 (더 높게 설정)
        }
        cpuContainer.addView(cpuProgress)

        // Memory 사용량 표시 - 높이 감소
        val memoryContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        // Post Worker Memory - 다른 메모리 사용량
        val memoryProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setMemoryUsage(9.8f, 16f) // Post Worker 메모리 사용량
        }
        memoryContainer.addView(memoryProgress)

        // Disk 사용량 표시 - 높이 감소 (Miner Node와 동일 종류의 차트)
        val diskContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
        }

        val diskProgress = CircularProgressView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                circleHeight)
            setDiskUsage(45.0f, 100f) // Post Worker Disk 사용량 (Miner Node와 다른 값)
        }
        diskContainer.addView(diskProgress)

        container.addView(cpuContainer)
        container.addView(memoryContainer)
        container.addView(diskContainer)
    }

    /**
     * Setup computing server metrics - 원형 그래프가 모두 보이도록 수정
     */
    private fun setupComputeServerMetrics(container: LinearLayout) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 좁은 화면에서는 수직으로 배치하되 원형 그래프 유지
        if (isNarrowScreen) {
            container.orientation = LinearLayout.VERTICAL

            // 높이 줄임
            val circleHeight = if (isVeryNarrowScreen) 160 else 180 // 높이 감소 (200~220 → 160~180)
            val circleTopMargin = if (isVeryNarrowScreen) 20 else 30 // 상단 여백 줄임

            // CPU 사용량 표시
            val cpuContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                setPadding(0, circleTopMargin, 0, 0)
            }

            val cpuProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    circleHeight)
                setCpuUsage(Random.nextInt(30, 95))
            }
            cpuContainer.addView(cpuProgress)

            // Memory 사용량 표시
            val memoryContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val memoryProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    circleHeight)
                setMemoryUsage(Random.nextInt(6, 14).toFloat(), 16f)
            }
            memoryContainer.addView(memoryProgress)

            // Disk 사용량 표시
            val diskContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val diskProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    circleHeight)
                setDiskUsage(Random.nextInt(400, 900).toFloat(), 1000f)
            }
            diskContainer.addView(diskProgress)

            container.addView(cpuContainer)
            container.addView(memoryContainer)
            container.addView(diskContainer)
        } else {
            // 기존 가로 배치 코드 - 높이 감소
            val cpuProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 300, 1.0f) // 높이 감소 (350 → 300)
                setCpuUsage(Random.nextInt(30, 95))
            }

            val memoryProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 300, 1.0f) // 높이 감소
                setMemoryUsage(Random.nextInt(6, 14).toFloat(), 16f)
            }

            val diskProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 300, 1.0f) // 높이 감소
                setDiskUsage(Random.nextInt(400, 900).toFloat(), 1000f)
            }

            container.addView(cpuProgress)
            container.addView(memoryProgress)
            container.addView(diskProgress)
        }
    }

    /**
     * Setup storage server metrics - 화면 크기에 따라 레이아웃 조정
     */
    private fun setupStorageServerMetrics(container: LinearLayout) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        if (isNarrowScreen) {
            // 좁은 화면에서는 수직으로 배치하고 원형 그래프 크기 축소
            container.orientation = LinearLayout.VERTICAL

            // 디스크 공간 표시 - 높이 줄임
            val circleHeight = if (isVeryNarrowScreen) 120 else 140 // 높이 감소 (140~160 → 120~140)

            val spaceProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    circleHeight)
                setPadding(0, 8, 0, 8) // 패딩 감소
                val usedPercentage = Random.nextInt(60, 95)
                setDiskUsage(usedPercentage.toFloat(), 100f)
            }
            container.addView(spaceProgress)

            // 성능 지표 표시
            val metricsView = StatusBarMetricsView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    220 // 높이 유지
                )
                setStorageMetrics(
                    Random.nextInt(60, 95).toFloat(),   // Space usage
                    Random.nextInt(70, 100).toFloat(),  // I/O rate
                    Random.nextInt(60, 90).toFloat(),   // Temperature
                )
            }
            container.addView(metricsView)
        } else {
            // 원래 가로 배치 코드 - 높이 감소
            val spaceProgress = CircularProgressView(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 280, 1.0f) // 높이 감소 (300 → 280)
                val usedPercentage = Random.nextInt(60, 95)
                setDiskUsage(usedPercentage.toFloat(), 100f)
            }

            // Right bar graph area
            val rightContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 2.0f)
            }

            // Add metrics bar
            val metricsView = StatusBarMetricsView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    280 // 높이 감소 (300 → 280)
                )
                setStorageMetrics(
                    Random.nextInt(60, 95).toFloat(),   // Space usage
                    Random.nextInt(70, 100).toFloat(),  // I/O rate
                    Random.nextInt(60, 90).toFloat(),   // Temperature
                )
            }
            rightContainer.addView(metricsView)

            container.addView(spaceProgress)
            container.addView(rightContainer)
        }
    }

    /**
     * Setup network equipment metrics - 화면 크기에 따라 레이아웃 조정
     */
    private fun setupNetworkMetrics(container: LinearLayout) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        if (isNarrowScreen) {
            // 좁은 화면에서는 수직으로 배치
            container.orientation = LinearLayout.VERTICAL

            // 차트 영역
            val chartView = CustomChartView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    200
                )
                setChartType(CustomChartView.ChartType.AREA)
                generateRandomData()
            }
            container.addView(chartView)

            // 네트워크 상태 정보
            val statusContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            // 네트워크 포트 상태 텍스트
            val statusText = TextView(context).apply {
                text = ""
                textSize = 14f // 작은 화면에 맞게 텍스트 크기 조정
                setTextColor(Color.parseColor("#4CAF50"))
                setPadding(0, 0, 0, 0)
            }

            // 처리량 텍스트
            val throughputText = TextView(context).apply {
                text = ""
                textSize = 14f
                setTextColor(Color.WHITE)
                setPadding(0, 0, 0, 0)
            }

            // 패킷 손실 텍스트
            val packetText = TextView(context).apply {
                text = ""
                textSize = 14f
                setTextColor(Color.parseColor("#4CAF50"))
                setPadding(0, 0, 0, 0)
            }

            statusContainer.addView(statusText)
            statusContainer.addView(throughputText)
            statusContainer.addView(packetText)

            container.addView(statusContainer)
        } else {
            // 원래 가로 배치 코드
            // Left graph area
            val chartContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.5f
                )
                gravity = Gravity.CENTER
            }

            // Add area chart
            val chartView = CustomChartView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    300
                )
                setChartType(CustomChartView.ChartType.AREA)
                generateRandomData()
            }
            chartContainer.addView(chartView)

            // Right status info area
            val statusContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            container.addView(chartContainer)
            container.addView(statusContainer)
        }
    }

    /**
     * Setup general server metrics - 화면 크기에 따라 레이아웃 조정
     */
    private fun setupGeneralMetrics(container: LinearLayout) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        container.orientation = LinearLayout.VERTICAL

        // 막대 그래프 메트릭스 표시
        val metricsView = StatusBarMetricsView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                280
            )
            setServerMetrics(
                Random.nextInt(30, 95).toFloat(),  // CPU
                Random.nextInt(30, 80).toFloat(),  // Memory
                Random.nextInt(30, 70).toFloat(),  // Disk
                Random.nextInt(20, 60).toFloat()   // Network
            )
        }
        container.addView(metricsView)

        // 하단에 텍스트 정보 추가 (원래 보였어야 할 정보)
        val additionalInfoView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.parseColor("#E0E0E0"))
            textSize = if (isNarrowScreen) 14f else 16f
            text = ""
            setPadding(0, 0, 0, 0)
        }
        container.addView(additionalInfoView)
    }

    /**
     * GPU 서버용 메트릭스 설정 (Aethir)
     */
    private fun setupGpuServerMetrics(container: LinearLayout, serverType: String, title: String) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 항상 수직으로 배치
        container.orientation = LinearLayout.VERTICAL

        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20
            )
        }
        container.addView(spacer)

        // GPU 성능 지표 표시 (막대 그래프)
        val metricsView = StatusBarMetricsView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                240
            )
            setGpuMetrics(
                Random.nextInt(70, 95).toFloat(),   // GPU 사용률
                Random.nextInt(60, 85).toFloat(),   // 온도
                Random.nextInt(70, 90).toFloat(),   // 메모리 사용량
                Random.nextInt(40, 99).toFloat()    // FLOPS 활용률
            )
        }
        container.addView(metricsView)

        // GPU 서버 정보 텍스트
        val gpuInfoText = TextView(context).apply {
            // Aethir 서버인 경우 ATH로 표시
            val rewardText = if (serverType.contains("Aethir") || title.contains("Aethir")) {
                "• Rewards: 1,542 ATH"
            } else {
                "• Rewards: 1,542 FIL"
            }
            setTextColor(Color.parseColor("#E0E0E0"))
            text = ""
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = -30 // 음수 마진으로 위로 이동
            layoutParams = params
        }
        container.addView(gpuInfoText)

        // 실시간 정보 뷰
        val liveInfoView = TextView(context).apply {
            val rewardUnit = if (serverType.contains("Aethir") || title.contains("Aethir")) "ATH" else "FIL"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.parseColor("#4CAF50"))
            textSize = if (isNarrowScreen) 12f else 14f
            text = "Rewards: +12.4 ${rewardUnit}/hr"
            setPadding(16, 4, 16, 8)
        }
        container.addView(liveInfoView)
    }

    private fun setupFileCoinMetrics(container: LinearLayout, serverType: String, title: String) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 항상 수직으로 배치
        container.orientation = LinearLayout.VERTICAL

        // 상단 여백 추가
        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20
            )
        }
        container.addView(spacer)

        // 스토리지 표시용 원형 차트 컨테이너
        val storageContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 8, 0, 0)
        }

        // 원형 차트 높이 설정 - 1920x400 화면에 최적화
        val circleHeight = if (isVeryNarrowScreen) 220 else 250

        // 스토리지 용량 계산 (실제 가용 용량으로 조정)
        val labeledStorage = 2048f     // 2PiB를 TiB 단위로 표시
        val actualStorage = 1986.56f   // 실제 가용 용량 1.94 PiB (TiB 단위)
        val usedStorage = 1295.24f     // 65.2% 사용 (실제 가용 용량 기준)
        val freeStorage = actualStorage - usedStorage

        // 막대 차트 생성 - 실제 가용 용량 기준으로 계산
        val storageProgress = StorageBarChartView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (isVeryNarrowScreen) 160 else 180
            )
            setStorageData(usedStorage, actualStorage, "Storage") // 디스크 사용량 설정
        }
        storageContainer.addView(storageProgress)
        container.addView(storageContainer)

        // 온도계 추가
        val temperatureContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        val temperatureGauge = TemperatureGaugeView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (isVeryNarrowScreen) 200 else 220
            )
            setTemperature(23f) // 23도로 설정
        }

        temperatureContainer.addView(temperatureGauge)
        container.addView(temperatureContainer)

        // 스토리지 정보 텍스트 추가
        val storageInfoText = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.parseColor("#E0E0E0"))
            textSize = if (isNarrowScreen) 14f else 16f
            text = """
            • Total Capacity: 2.0 PiB (1.94 PiB)
            • Used Storage: ${String.format("%,.1f", usedStorage)} TiB (${String.format("%.1f", usedStorage/actualStorage*100)}%)
            • Free Storage: ${String.format("%,.1f", freeStorage)} TiB
        """.trimIndent()
            setPadding(16, 16, 16, 8)
        }
        container.addView(storageInfoText)

        // 초록색 텍스트 추가 (storage_4는 제외)
        if (serverType != "2PiB Storage Server" && serverType != "Storage Server 4" &&
            title != "Filecoin Storage" && title != "Storage 4") {
            // 실시간 업데이트 정보 텍스트 추가
            val updateInfoText = TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setTextColor(Color.parseColor("#4CAF50"))
                textSize = if (isNarrowScreen) 12f else 14f
                text = "FIL Rewards: +8.3 FIL/hr | Data Transfer: 125.6 MB/s"
                setPadding(16, 4, 16, 12)
            }
            container.addView(updateInfoText)
        }
    }

    private fun setupGpuServerMetrics(container: LinearLayout) {
        // 항상 수직 레이아웃으로 설정
        container.orientation = LinearLayout.VERTICAL

        //여백용 공간
        val spacer = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                20
            )
        }
        container.addView(spacer)

        // 막대 그래프 메트릭스 (StatusBarMetricsView) 사용 - GPU 관련 항목만 표시
        val metricsView = StatusBarMetricsView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                200  // 높이 줄임 (항목이 줄어들었으므로)
            )

            setGpuMetrics(
                Random.nextInt(70, 95).toFloat(),   // GPU 사용률
                Random.nextInt(60, 85).toFloat(),   // 온도
                Random.nextInt(60, 80).toFloat(),   // VRAM 사용량
                Random.nextInt(70, 99).toFloat()    // FLOPS 활용률
            )
        }
        container.addView(metricsView)
    }

    private fun setupGpuCircularCharts(container: LinearLayout, cpuUsage: Int, memoryUsage: Float, vramUsage: Float, gpuCount: String) {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 항상 수직으로 배치
        container.orientation = LinearLayout.VERTICAL

        // GPU 정보 표시 (GPU 수 표시)
        val gpuInfoContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 0, 16, 8)
            gravity = Gravity.CENTER
        }

        // GPU 정보 텍스트
        val gpuInfoText = TextView(context).apply {
            text = gpuCount
            textSize = if (isNarrowScreen) 14f else 16f
            setTextColor(Color.parseColor("#FFFFFF"))
            typeface = Typeface.DEFAULT_BOLD
        }

        gpuInfoContainer.addView(gpuInfoText)
        container.addView(gpuInfoContainer)

        // 반원형 게이지 높이 설정
        val gaugeHeight = if (isVeryNarrowScreen) 120 else 140

        // VRAM 그래프를 2x2 그리드로 배치
        val gridContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 16, 0, 16)
        }

        // 상단 행 (VRAM 1, 2)
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // VRAM 1
        val vram1Container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            gravity = Gravity.CENTER
        }
        val vramGauge1 = VramGaugeView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                gaugeHeight
            )
            setVramUsage(vramUsage * 0.9f, 24f) // 각 GPU마다 약간 다른 사용률
        }
        val vramLabel1 = TextView(context).apply {
            text = "VRAM 1"
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }
        vram1Container.addView(vramGauge1)
        vram1Container.addView(vramLabel1)

        // VRAM 2
        val vram2Container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            gravity = Gravity.CENTER
        }
        val vramGauge2 = VramGaugeView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                gaugeHeight
            )
            setVramUsage(vramUsage * 0.95f, 24f)
        }
        val vramLabel2 = TextView(context).apply {
            text = "VRAM 2"
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }
        vram2Container.addView(vramGauge2)
        vram2Container.addView(vramLabel2)

        topRow.addView(vram1Container)
        topRow.addView(vram2Container)

        // 하단 행 (VRAM 3, 4)
        val bottomRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 0)
            }
        }

        // VRAM 3
        val vram3Container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            gravity = Gravity.CENTER
        }
        val vramGauge3 = VramGaugeView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                gaugeHeight
            )
            setVramUsage(vramUsage * 1.05f, 24f)
        }
        val vramLabel3 = TextView(context).apply {
            text = "VRAM 3"
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }
        vram3Container.addView(vramGauge3)
        vram3Container.addView(vramLabel3)

        // VRAM 4
        val vram4Container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            gravity = Gravity.CENTER
        }
        val vramGauge4 = VramGaugeView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                gaugeHeight
            )
            setVramUsage(vramUsage, 24f)
        }
        val vramLabel4 = TextView(context).apply {
            text = "VRAM 4"
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(Color.parseColor("#B0BEC5"))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 0)
            }
        }
        vram4Container.addView(vramGauge4)
        vram4Container.addView(vramLabel4)

        bottomRow.addView(vram3Container)
        bottomRow.addView(vram4Container)

        gridContainer.addView(topRow)
        gridContainer.addView(bottomRow)
        container.addView(gridContainer)
    }
}