package com.nanodatacenter.monitorwebview

import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import java.util.ArrayList
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {
    private lateinit var scrollView: NestedScrollView
    private lateinit var progressBar: RelativeLayout
    private var mediaPlayer: MediaPlayer? = null

    // Variables for tracking touch count
    private var currentSelectedImageView: ImageView? = null
    private var touchCount = 0
    private val touchCountResetHandler = Handler()

    // Variables for calculating screen width
    private var screenWidth = 0
    private var screenHeight = 0

    private val imageViewIds = listOf(
        R.id.rack_info,           // 0
        R.id.node_info,           // 1
        R.id.onboarding,          // 2
        R.id.switch_40,           // 3
        R.id.node_miner,          // 4
        R.id.post_worker,         // 5
        R.id.pc2_1,               // 6
        R.id.pc2_2,               // 7
        R.id.pc2_3,               // 8
        R.id.storage_6,           // 9
        R.id.upscontroller,       // 10
        R.id.storage_1,           // 11
        R.id.storage_2,           // 12
        R.id.storage_3,           // 13
        R.id.storage_4,           // 14
        R.id.storage_5,           // 15
        R.id.logo_zetacube        // 16
    )

    private val monitorViewIds = listOf(
        R.id.rack_info_monitor,
        R.id.node_info_monitor,
        R.id.onboarding_monitor,
        R.id.switch_40_monitor,
        R.id.node_miner_monitor,
        R.id.post_worker_monitor,
        R.id.pc2_1_monitor,
        R.id.pc2_2_monitor,
        R.id.pc2_3_monitor,
        R.id.storage_6_monitor,
        R.id.upscontroller_monitor,
        R.id.storage_1_monitor,
        R.id.storage_2_monitor,
        R.id.storage_3_monitor,
        R.id.storage_4_monitor,
        R.id.storage_5_monitor,
        R.id.logo_zetacube_monitor
    )

    private val existHardWareButton = listOf(
        false,
        false,
        false,
        false,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        false
    )

    // Monitoring data (adjusted to match Spring server format)
    private val monitoringData = listOf(
        // Rack Info - adjusted to match Spring server format
        """
            # Status: Normal
            # Temperature: 24.7°C
            # Running: 16/17 units
            # Capacity: 348.6/376.4TB
            """.trimIndent(),

        // Node Info - node with ID f03091958
        """
            # ID: f03091958
            # Status: Active
            # Power: 425.5 TiB
            # Blocks: 42,311
            """.trimIndent(),

        // Onboarding - onboarding service info
        """
            # Status: Active
            # Deals: 137 in process
            # Data: 78.4 TiB
            # Verifications: 4,231
            """.trimIndent(),

        // Switch - MSN2700-BS2F model info
        """
            # Model: MSN2700-BS2F
            # Ports: 32/32 normal
            # Throughput: 3.24 Tbps
            # Packet Loss: 0.0003%
            """.trimIndent(),

        // Node Miner - f03091958 miner node info
        """
            # CPU: 67.3%
            # Memory: 11.2/14.6GB
            # Temperature: 41.5°C
            # Tasks: Sealing x5
            """.trimIndent(),

        // Post Worker - post worker info
        """
            # Status: Processing
            # Queue: 17 items
            # CPU: 87.6%
            # GPU: 72°C
            """.trimIndent(),

        // PC2_1 - compute server 1 info
        """
            # System: PC2-1
            # CPU: 32.1%
            # Memory: 10.2/32GB
            # Disk: 450GB/1TB
            """.trimIndent(),

        // PC2_2 - compute server 2 info Aethir
        """
            # GPU Server: Aethir
            # FLOPS: 124.5 TFLOPS
            # GPU Temp: 72.3°C
            # Memory: 64GB/80GB
            # Rewards: 1,542 FIL
            """.trimIndent(),


        // PC2_3 - compute server 3 info
        """
            # System: PC2-4
            # CPU: 71.9%
            # Memory: 18.2/32GB
            # Disk: 1.3/2TB
            """.trimIndent(),

        // Storage 6 - storage server 6 info
        """
            # Status: Normal
            # Capacity: 328.7/360.1TB
            # Temperature: 37-47°C
            # Speed: 6Gbps x22
            """.trimIndent(),

        // UPS Controller - UPS controller info
        """
    # GPU Server: RTX 3090 × 4
    # CPU: 40.2%
    # Memory: 60.5GB/128GB
    # VRAM: 15.3GB/24GB
    # Temperature: 55.7°C
""".trimIndent(),

        // Storage 1 - storage server 1
        """
        # GPU Server: RTX 3090 × 8
        # CPU: 5.2%
        # Memory: 25.6GB/128GB
        # VRAM: 10.2GB/24GB
        # Temperature: 45.7°C
    """.trimIndent(),

// Storage 2 (Aethir GPU 서버)로 표시되는 부분
        """
        # GPU Server: RTX 3090 × 8
        # CPU: 65.8%
        # Memory: 90.4GB/128GB
        # VRAM: 18.7GB/24GB
        # Temperature: 68.2°C
    """.trimIndent(),
        // Storage 3 - storage server 3 requiring attention
        """
        # GPU Server: RTX 3090 × 8
        # CPU: 65.8%
        # Memory: 90.4GB/128GB
        # VRAM: 18.7GB/24GB
        # Temperature: 68.2°C
    """.trimIndent(),

        // Filecoin Storage - storage server 4 in normal state
        """
    # Server: Filecoin Storage
    # Total Capacity: 2.0 PiB (1.94 PiB)
    # Used Storage: 1,295.2 TiB (65.2%)
    # Free Space: 691.3 TiB
""".trimIndent(),
        // Storage 4 - storage server 4 in normal state
        """
            # Status: Normal
            # Disks: 8 active
            # Capacity: 56.1/65.5TB
            # IOPS: 527
            """.trimIndent(),

        // Logo Zetacube - system info
        """
            # Version: v1.5.2
            # Updated: 2025/03/15
            # Uptime: 24d 17h
            # Devices: 17 units
            """.trimIndent()
    )

    private lateinit var imageViewsScrollLocation: MutableList<Int>
    private val mainOpening = R.raw.door
    private val sideOpening = R.raw.short_door4
    private lateinit var monitorViews: ArrayList<LinearLayout>
    private val mHandler = Handler()
    private val mRunnable: Runnable = Runnable { close_down_all() }
    private var loadCnt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE
        decorView.systemUiVisibility = uiOptions

        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        progressBar = findViewById(R.id.progress_bar)
        scrollView = findViewById(R.id.scroll_view)

        // Calculate screen size
        val displayMetrics = resources.displayMetrics
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        monitorViews = ArrayList()
        imageViewsScrollLocation = ArrayList()
        monitorViewsInitializing()

        imageViewInitializing()

        // Simulating delay for loading completion (instead of original webview loading)
        mHandler.postDelayed({
            progressBar.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
            close_down_all()
        }, 2000)
    }

    //너비 조정
    private fun showMonitorInfo(imageView: ImageView) {
        // 화면 너비 확인
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // First close everything
        for (i in monitorViews.indices) {
            val monitorView = monitorViews[i]

            if (findViewById<ImageView>(imageViewIds[i]) == imageView) {
                // 첫 번째 인덱스(rack_info)는 별도 처리하므로 스킵
                if (i == 0) continue
                // index 1 (node_info)도 별도 처리하므로 스킵  
                if (i == 1) continue
                // index 13 (filecoin storage)도 별도 처리하므로 스킵
                if (i == 13) continue

                // Apply animation only if the monitoring view is closed
                if (monitorView.visibility == View.GONE) {
                    monitorView.visibility = View.VISIBLE
                    val layoutParams = monitorView.layoutParams

                    // 각 서버 타입별 맞춤 높이 설정
                    val targetHeight = when (i) {
                        1 -> when {
                            isVeryNarrowScreen -> 530
                            isNarrowScreen -> 560
                            else -> 600
                        }

                        // 마이너 노드 (Image 1)
                        4 -> when {
                            isVeryNarrowScreen -> 660
                            isNarrowScreen -> 690
                            else -> 720
                        }

                        // 포스트 워커 (Image 2)
                        5 -> when {
                            isVeryNarrowScreen -> 660
                            isNarrowScreen -> 690
                            else -> 720
                        }

                        // Supra
                        6 -> when {
                            isVeryNarrowScreen -> 610
                            isNarrowScreen -> 640
                            else -> 670
                        }

                        7 -> when {
                            isVeryNarrowScreen -> 820
                            isNarrowScreen -> 850
                            else -> 900
                        }

                        8 -> when {
                            isVeryNarrowScreen -> 820
                            isNarrowScreen -> 850
                            else -> 900
                        }

                        //deepseek
                        10 -> when {
                            isVeryNarrowScreen -> 630
                            isNarrowScreen -> 690
                            else -> 720
                        }

                        //aethir
                        12 -> when {
                            isVeryNarrowScreen -> 630
                            isNarrowScreen -> 690
                            else -> 720
                        }

                        13 -> when {
                            isVeryNarrowScreen -> 440
                            isNarrowScreen -> 440//690
                            else -> 440//720
                        }
                        // 스토리지 서버 (더 많은 정보 표시)
                        in listOf(11, 14, 15) -> when {
                            isVeryNarrowScreen -> 750
                            isNarrowScreen -> 780
                            else -> 820
                        }

                        // 다른 모든 화면
                        else -> when {
                            isVeryNarrowScreen -> 600
                            isNarrowScreen -> 650
                            else -> 700
                        }
                    }

                    val viewAnimator = ValueAnimator.ofInt(0, targetHeight)
                    viewAnimator.addUpdateListener { animation ->
                        layoutParams.height = animation.animatedValue as Int
                        monitorView.layoutParams = layoutParams
                    }

                    viewAnimator.duration = 200
                    viewAnimator.start()
                }
            } else {
                // Hide monitoring info and buttons for other images
                monitorView.visibility = View.GONE
                val layoutParams = monitorView.layoutParams
                layoutParams.height = 0
                monitorView.layoutParams = layoutParams
            }
        }
    }

    fun applyElasticEffect(view: View) {
        val imageView = view as ImageView

        val scaleXAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_X, 1.2f, 1.0f)
        val scaleYAnimator = ObjectAnimator.ofFloat(imageView, View.SCALE_Y, 0.8f, 1.0f)

        scaleXAnimator.duration = 1000
        scaleYAnimator.duration = 1000

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator)

        animatorSet.interpolator = android.view.animation.BounceInterpolator()

        animatorSet.start()
    }

    // setupRackInfoView 메서드
    private fun setupRackInfoView(container: LinearLayout) {
        // 기존 뷰를 모두 제거
        container.removeAllViews()

        // 화면 너비 확인
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 전체 컨테이너를 감쌀 LinearLayout 생성
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        // 헤더 카드 - NDP 로고와 기본 정보
        val headerCard = MaterialCardView(this).apply {
            radius = 20f
            cardElevation = 16f
            setCardBackgroundColor(Color.parseColor("#0D1B2A"))
            strokeColor = Color.parseColor("#00D4FF")
            strokeWidth = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val headerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 20, 20, 20)
        }

        // NDP 로고 (더 세련된 디자인)
        val logoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            background = resources.getDrawable(android.R.drawable.editbox_background)
            background.setTint(Color.parseColor("#4400D4FF"))
            setPadding(16, 12, 16, 12)
        }

        val logoView = TextView(this).apply {
            text = "NDP"
            textSize = if (isNarrowScreen) 24f else 28f
            setTextColor(Color.parseColor("#00D4FF"))
            typeface = Typeface.DEFAULT_BOLD
        }

        logoContainer.addView(logoView)

        // 제목 컨테이너
        val titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
                marginStart = 20
            }
        }

        val titleText = TextView(this).apply {
            text = "NDP Point"
            textSize = if (isNarrowScreen) 16f else 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        val subtitleText = TextView(this).apply {
            text = "Decentralized Physical Infrastructure Network"
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(Color.parseColor("#B0BEC5"))
        }

        val descText = TextView(this).apply {
            text = "Tokenizing & Connecting DePIN Resources"
            textSize = if (isNarrowScreen) 11f else 13f
            setTextColor(Color.parseColor("#00D4FF"))
            typeface = Typeface.DEFAULT_BOLD
        }

        titleContainer.addView(titleText)
        titleContainer.addView(subtitleText)
        titleContainer.addView(descText)

        headerContainer.addView(logoContainer)
        headerContainer.addView(titleContainer)
        headerCard.addView(headerContainer)

        // 토큰 정보 카드
        val tokenInfoCard = MaterialCardView(this).apply {
            radius = 16f
            cardElevation = 12f
            setCardBackgroundColor(Color.parseColor("#162B40"))
            strokeColor = Color.parseColor("#4CAF50")
            strokeWidth = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val tokenInfoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 16)
        }

        // 토큰 기본 정보
        val tokenBasicInfo = createTokenInfoRow(
            "Total Supply:", "2,000,000,000 NDP",
            "Blockchain:", "BNB Smart Chain (BEP-20)"
        )
        tokenInfoContainer.addView(tokenBasicInfo)

        val tokenUtilityInfo = createTokenInfoRow(
            "Primary Use:", "Staking & Governance",
            "Burn Mechanism:", "SLA Violation + 10% Auto-burn"
        )
        tokenInfoContainer.addView(tokenUtilityInfo)

        tokenInfoCard.addView(tokenInfoContainer)

        // 토크노믹스 차트 카드
        val tokenomicsCard = MaterialCardView(this).apply {
            radius = 16f
            cardElevation = 12f
            setCardBackgroundColor(Color.parseColor("#0A1929"))
            strokeColor = Color.parseColor("#FF9800")
            strokeWidth = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 12)
            }
        }

        val tokenomicsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 8)
        }

        // 토크노믹스 제목
        val tokenomicsTitle = TextView(this).apply {
            text = "Token Distribution"
            textSize = if (isNarrowScreen) 16f else 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            gravity = Gravity.CENTER
        }
        tokenomicsContainer.addView(tokenomicsTitle)

        // 토크노믹스 차트
        val tokenomicsChart = NDPTokenomicsChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (isNarrowScreen) 350 else 400
            )
        }
        tokenomicsContainer.addView(tokenomicsChart)

        tokenomicsCard.addView(tokenomicsContainer)

        // DePIN 네트워크 상태 카드
        val networkCard = MaterialCardView(this).apply {
            radius = 16f
            cardElevation = 12f
            setCardBackgroundColor(Color.parseColor("#0D2C54"))
            strokeColor = Color.parseColor("#9C27B0")
            strokeWidth = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val networkContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(16, 16, 16, 8)
        }

        // 네트워크 제목
        val networkTitle = TextView(this).apply {
            text = "DePIN Network Status"
            textSize = if (isNarrowScreen) 16f else 18f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            gravity = Gravity.CENTER
        }
        networkContainer.addView(networkTitle)

        // 네트워크 상태 차트
        val networkStatusView = NDPNetworkStatusView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (isNarrowScreen) 380 else 450
            )
        }
        networkContainer.addView(networkStatusView)

        networkCard.addView(networkContainer)

        // 모든 카드를 메인 컨테이너에 추가
        mainContainer.addView(headerCard)
        mainContainer.addView(tokenInfoCard)
        mainContainer.addView(tokenomicsCard)
        mainContainer.addView(networkCard)

        container.addView(mainContainer)

        // 애니메이션 적용
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        animation.duration = 1000
        mainContainer.startAnimation(animation)
    }

    /**
     * 토큰 정보 행 생성 헬퍼 메서드
     */
    private fun createTokenInfoRow(label1: String, value1: String, label2: String, value2: String): LinearLayout {
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        
        return LinearLayout(this).apply {
            orientation = if (isNarrowScreen) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }

            // 첫 번째 정보
            val leftContainer = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    if (isNarrowScreen) LinearLayout.LayoutParams.MATCH_PARENT else 0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (!isNarrowScreen) weight = 1f
                }
            }

            val leftLabel = TextView(this@MainActivity).apply {
                text = label1
                textSize = if (isNarrowScreen) 12f else 14f
                setTextColor(Color.parseColor("#B0BEC5"))
            }

            val leftValue = TextView(this@MainActivity).apply {
                text = value1
                textSize = if (isNarrowScreen) 13f else 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
            }

            leftContainer.addView(leftLabel)
            leftContainer.addView(leftValue)

            // 두 번째 정보
            val rightContainer = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    if (isNarrowScreen) LinearLayout.LayoutParams.MATCH_PARENT else 0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (!isNarrowScreen) weight = 1f
                    if (isNarrowScreen) topMargin = 16
                }
            }

            val rightLabel = TextView(this@MainActivity).apply {
                text = label2
                textSize = if (isNarrowScreen) 12f else 14f
                setTextColor(Color.parseColor("#B0BEC5"))
            }

            val rightValue = TextView(this@MainActivity).apply {
                text = value2
                textSize = if (isNarrowScreen) 13f else 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
            }

            rightContainer.addView(rightLabel)
            rightContainer.addView(rightValue)

            addView(leftContainer)
            addView(rightContainer)
        }
    }

    // setupMinerInfoView 메서드에서 변경된 부분
    private fun setupMinerInfoView(container: LinearLayout) {
        // 기존 뷰를 모두 제거 - 이 부분이 중요함
        container.removeAllViews()

        // 화면 너비 확인
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 전체 컨테이너를 감쌀 LinearLayout 생성
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 전체 컨테이너를 감쌀 MaterialCardView 생성
        val containerCard = MaterialCardView(this).apply {
            radius = 16f
            cardElevation = 12f
            setCardBackgroundColor(Color.parseColor("#0D2C54"))
            strokeColor = Color.parseColor("#4CAF50") // 녹색 테두리
            strokeWidth = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 실제 내용을 담을 내부 컨테이너
        val innerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // 패딩 조정
            val horizontalPadding = when {
                isVeryNarrowScreen -> 10
                isNarrowScreen -> 12
                else -> 16
            }
            setPadding(horizontalPadding, 16, horizontalPadding, 16)
        }

        // 헤더 생성
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        // 헤더 아이콘
        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.node_info)
            layoutParams = LinearLayout.LayoutParams(
                if (isNarrowScreen) 40 else 48,
                if (isNarrowScreen) 40 else 48
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
            }
            setColorFilter(Color.parseColor("#4CAF50")) // 녹색 아이콘
        }

        // 헤더 제목
        val titleTextView = TextView(this).apply {
            text = "Miner Overview|Address:f02368818"
            textSize = if (isNarrowScreen) 16f else 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginStart = if (isNarrowScreen) 8 else 16
            }
        }

        // 상태 표시기
        val statusView = TextView(this).apply {
            text = "Active"
            textSize = if (isNarrowScreen) 12f else 16f
            setTextColor(Color.parseColor("#4CAF50"))
            setBackgroundResource(android.R.drawable.editbox_background)
            background.setTint(Color.parseColor("#334CAF50"))
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
                marginStart = 16
            }
        }

        headerLayout.addView(iconView)
        headerLayout.addView(titleTextView)
        headerLayout.addView(statusView)
        innerContainer.addView(headerLayout)

        // 구분선 추가
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }
        innerContainer.addView(divider)

        // 메인 콘텐츠 레이아웃
        val contentLayout = LinearLayout(this).apply {
            orientation = if (isNarrowScreen) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 왼쪽: 도넛 차트 컨테이너
        val chartContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                if (isNarrowScreen) LinearLayout.LayoutParams.MATCH_PARENT else 0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (!isNarrowScreen) {
                    weight = 1.0f
                }
                gravity = Gravity.CENTER
                setMargins(0, 0, if (isNarrowScreen) 0 else 16, if (isNarrowScreen) 16 else 0)
            }
        }

        // 도넛 차트 뷰 생성
        val donutChartView = MinerDonutChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (isNarrowScreen) LinearLayout.LayoutParams.MATCH_PARENT else 220,
                if (isNarrowScreen) 200 else 220
            )
            // 차트 데이터 설정 - FIL 잔액 분포 표시
            setChartData(
                floatArrayOf(1065.7558f, 2996.9603f, 133.8326f), // 가용, 서약, 잠김
                arrayOf("Available", "Pledged", "Locked"),
                intArrayOf(
                    Color.parseColor("#42A5F5"), // 파란색 (가용)
                    Color.parseColor("#4CAF50"), // 녹색 (서약)
                    Color.parseColor("#F44336")  // 빨간색 (잠김)
                )
            )
        }
        chartContainer.addView(donutChartView)

        // 오른쪽: 마이너 정보 컨테이너
        val infoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                if (isNarrowScreen) LinearLayout.LayoutParams.MATCH_PARENT else 0,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (!isNarrowScreen) {
                    weight = 1.5f
                }
            }
        }

        // 왼쪽 정보 섹션 (주소 잔액)
        val leftInfoSection = createMinerInfoSection(
            "Address Balance",
            "4,196.5487 FIL",
            listOf(
                Pair("Available Balance ⓘ :", "1,065.7558 FIL"),
                Pair("Initial Pledge:", "2,996.9603 FIL"),
                Pair("Locked Rewards:", "133.8326 FIL")
            )
        )

        // 오른쪽 정보 섹션 (조정된 파워)
        val rightInfoSection = createMinerInfoSection(
            "Adjusted Power",
            "541.69 TiB",
            listOf(
                Pair("Raw Byte Power:", "541.69 TiB"),
                Pair("Total Reward:", "1,063.58 FIL"),
                Pair("Sector Status:", "")
            )
        )

        infoContainer.addView(leftInfoSection)
        infoContainer.addView(rightInfoSection)

        // 콘텐츠 레이아웃에 추가
        contentLayout.addView(chartContainer)
        contentLayout.addView(infoContainer)

        // 내부 컨테이너에 콘텐츠 레이아웃 추가
        innerContainer.addView(contentLayout)

        // 컨테이너 계층 구성
        containerCard.addView(innerContainer)
        mainContainer.addView(containerCard)
        container.addView(mainContainer)

        // 애니메이션 적용
        val animation =
            android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        animation.duration = 800
        containerCard.startAnimation(animation)
    }

    private fun createMinerInfoSection(
        title: String,
        value: String,
        items: List<Pair<String, String>>
    ): LinearLayout {
        val section = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 16)
            }
        }

        // 제목
        val titleText = TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(Color.parseColor("#B0BEC5"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        section.addView(titleText)

        // 값
        val valueText = TextView(this).apply {
            text = value
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 4, 0, 8)
            }
        }
        section.addView(valueText)

        // 추가 항목들
        for (item in items) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 2, 0, 2)
                }
            }

            // 항목 레이블
            val labelText = TextView(this).apply {
                text = item.first
                textSize = 14f
                setTextColor(Color.parseColor("#E0E0E0"))
            }
            itemLayout.addView(labelText)

            // 항목 값
            if (item.second.isNotEmpty()) {
                val itemValueText = TextView(this).apply {
                    text = " ${item.second}"
                    textSize = 14f
                    setTextColor(Color.parseColor("#FFFFFF"))
                    typeface = Typeface.DEFAULT_BOLD
                }
                itemLayout.addView(itemValueText)
            }

            section.addView(itemLayout)
        }

        return section
    }

    // Filecoin Storage 정보를 위한 setup 함수
    private fun setupFilecoinStorageView(container: LinearLayout) {
        // 기존 뷰를 모두 제거
        container.removeAllViews()

        // 화면 너비 확인
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // 전체 컨테이너
        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
        }

        // MaterialCardView 사용하여 다른 UI와 일관성 유지
        val containerCard = MaterialCardView(this).apply {
            radius = 16f
            cardElevation = 12f
            setCardBackgroundColor(Color.parseColor("#0D2C54"))
            strokeColor = Color.parseColor("#4CAF50") // 초록색 테두리
            strokeWidth = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 내부 컨테이너
        val innerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val horizontalPadding = when {
                isVeryNarrowScreen -> 12
                isNarrowScreen -> 16
                else -> 20
            }
            setPadding(horizontalPadding, 20, horizontalPadding, 20)
        }

        // 헤더 섹션
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, 16)
        }

        // Filecoin 아이콘
        val iconView = ImageView(this).apply {
            setImageResource(R.drawable.filecoin)
            layoutParams = LinearLayout.LayoutParams(
                if (isNarrowScreen) 36 else 42,
                if (isNarrowScreen) 36 else 42
            )
            setColorFilter(Color.parseColor("#FFD700")) // 금색 필터
        }

        // 제목 컨테이너
        val titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            setPadding(16, 0, 0, 0)
        }

        // 메인 제목
        val mainTitle = TextView(this).apply {
            text = "Filecoin Storage"
            textSize = if (isNarrowScreen) 18f else 22f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        // 부제목
        val subTitle = TextView(this).apply {
            text = "2PIB Storage Server"
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(Color.parseColor("#B0BEC5"))
        }

        // 상태 표시
        val statusView = TextView(this).apply {
            text = "Active"
            textSize = if (isNarrowScreen) 11f else 13f
            setTextColor(Color.parseColor("#4CAF50"))
            setBackgroundResource(android.R.drawable.editbox_background)
            background.setTint(Color.parseColor("#334CAF50"))
            setPadding(12, 6, 12, 6)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        titleContainer.addView(mainTitle)
        titleContainer.addView(subTitle)
        
        headerLayout.addView(iconView)
        headerLayout.addView(titleContainer)
        headerLayout.addView(statusView)
        innerContainer.addView(headerLayout)

        // 구분선
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
        }
        innerContainer.addView(divider)

        // 차트 섹션
        val chartSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 16)
        }

        // 원형 차트
        val storageChart = CircularProgressView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (isNarrowScreen) 280 else 320,
                if (isNarrowScreen) 280 else 320
            )
            setDiskUsage(1295.2f, 1986.56f) // 65.2% 사용률
            setLabel("Storage")
        }

        chartSection.addView(storageChart)
        innerContainer.addView(chartSection)

        // 정보 섹션
        val infoSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 0, 0, 8)
        }

        // 정보 항목들을 카드 스타일로 표시
        val infoItems = listOf(
            Pair("Total Capacity", "2.0 PIB (1.94 PIB)"),
            Pair("Used Storage", "1,295.2 TiB (65.2%)"),
            Pair("Free Storage", "691.3 TiB")
        )

        infoItems.forEach { (label, value) ->
            val infoItemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 8, 16, 8)
            }

            val labelText = TextView(this).apply {
                text = "$label:"
                textSize = if (isNarrowScreen) 14f else 16f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val valueText = TextView(this).apply {
                text = value
                textSize = if (isNarrowScreen) 14f else 16f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.END
            }

            infoItemLayout.addView(labelText)
            infoItemLayout.addView(valueText)
            infoSection.addView(infoItemLayout)
        }

        innerContainer.addView(infoSection)
        containerCard.addView(innerContainer)
        mainContainer.addView(containerCard)
        container.addView(mainContainer)

        // 애니메이션 적용
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        animation.duration = 800
        containerCard.startAnimation(animation)
    }

    fun playSound(soundResId: Int) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, soundResId)
        mediaPlayer?.start()
    }

    fun monitorViewsInitializing() {
        // 화면 너비 확인
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)

        // Create optimized monitoring data presenter
        val dataPresenter = EnhancedMonitorPresenter(this)

        // Define each item type
        val serverTypes = listOf(
            "Rack Info",
            "Node Info",
            "Onboarding Server",
            "Network Switch",
            "Miner Node",
            "Post Worker",
            "SUPRA WORKER",
            "Compute Server 2",
            "Compute Server 3",
            "Storage Server 6",
            "NVIDA RTX 3090 X 8",
            "GPU Server RTX",
            "NVIDA RTX 3090 X 8",
            "Filecoin Storage",
            "Storage Server 4",
            "Storage Server 5",
            "System Info"
        )
        // Define title for each item - 좁은 화면에서는 타이틀 짧게
        val titleTexts = if (isNarrowScreen) {
            listOf(
                "Rack Status",
                "Node Info",
                "Onboarding",
                "100G Switch",
                "Miner Node",
                "Post Worker",
                "SUPRA WORKER",
                "Server 2",
                "Server 3",
                "Storage 6",
                "DeepSeek Server",
                "RTX 3090",
                "Aethir GPU Server",
                "Filecoin Storage",
                "Storage 4",
                "Storage 5",
                "System"
            )
        } else {
            listOf(
                "Rack System Status",
                "Node Information",
                "Onboarding Service",
                "100G Switch",
                "Miner Node",
                "Post Worker",
                "Compute Server 1",
                "Compute Server 2",
                "Compute Server 3",
                "Storage Server 6",
                "DeepSeek Server",
                "NVIDIA RTX 3090 Cluster",
                "Storage Server",
                "Filecoin Storage",
                "Storage Server 4",
                "Storage Server 5",
                "Zetacube Monitoring"
            )
        }

        // Status settings - determine status for each equipment
        val statuses = List(monitorViewIds.size) { index ->
            EnhancedMonitorPresenter.ServerStatus.NORMAL
        }

        for (i in monitorViewIds.indices) {
            // CustomHeightLayout으로 교체하여 최대 높이 제한 지원
            val monitorView = if (isVeryNarrowScreen) {
                // 매우 좁은 화면에서는 CustomHeightLayout 사용
                val customLayout = CustomHeightLayout(this)
                customLayout.orientation = LinearLayout.VERTICAL
                customLayout.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // 최대 높이 설정 (화면 높이의 70%)
                customLayout.maxHeight = (screenHeight * 0.7).toInt()

                // 원래 뷰 대신 커스텀 뷰 사용
                val originalView = findViewById<LinearLayout>(monitorViewIds[i])
                val parentViewGroup = originalView.parent as ViewGroup
                val index = parentViewGroup.indexOfChild(originalView)

                // 원래 뷰 제거하고 커스텀 뷰로 교체
                parentViewGroup.removeView(originalView)
                parentViewGroup.addView(customLayout, index)

                customLayout
            } else {
                // 일반 화면에서는 기존 LinearLayout 사용
                findViewById<LinearLayout>(monitorViewIds[i])
            }

            // Set layout optimized for screen width
            monitorView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                // 좁은 화면에서는 여백 더 줄임
                setMargins(
                    if (isNarrowScreen) 5 else 10,
                    0,
                    if (isNarrowScreen) 2 else 5,
                    0
                )
            }

            // Create enhanced material design card
            val enhancedView = dataPresenter.createEnhancedMonitorCard(
                title = titleTexts[i],
                serverType = serverTypes[i],
                data = "",
                status = statuses[i]
            )

            monitorView.addView(enhancedView)
            monitorViews.add(monitorView)
            loadCnt++
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun imageViewInitializing() {
        for (i in imageViewIds.indices) {
            val imageView = findViewById<ImageView>(imageViewIds[i])
            imageViewsScrollLocation.add(imageView.top)
            val index = i

            // 터치시 반응 없음
            if (index == 2 || index == 3 || index == 7 || index == 8 || index == 9 || index == 11 || index == 14 || index == 15 || index == 16) {
                continue
            }

            imageView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        imageView.alpha = 0.5f
                    }

                    MotionEvent.ACTION_UP -> {
                        imageView.alpha = 1.0f

                        // Reset touch count if a different image is selected
                        if (currentSelectedImageView != imageView) {
                            touchCount = 0
                            currentSelectedImageView = imageView
                        }

                        // Increase touch count
                        touchCount++

                        when (touchCount) {
                            1 -> {
                                // Rack Info (index 0) - show rack overview
                                if (index == 0) {
                                    // 다른 모든 모니터링 뷰 닫기
                                    for (monitorView in monitorViews) {
                                        monitorView.visibility = View.GONE
                                    }

                                    // 현재 뷰만 표시
                                    val monitorView = monitorViews[index]
                                    monitorView.visibility = View.VISIBLE

                                    // 레이아웃 높이 설정
                                    val layoutParams = monitorView.layoutParams
                                    val displayMetrics = resources.displayMetrics
                                    val isNarrowScreen =
                                        screenWidth < (400 * displayMetrics.density)
                                    val isVeryNarrowScreen =
                                        screenWidth < (370 * displayMetrics.density)
                                    layoutParams.height = when {
                                        isVeryNarrowScreen -> 650
                                        isNarrowScreen -> 680
                                        else -> 720
                                    }
                                    monitorView.layoutParams = layoutParams

                                    // 랙 정보 뷰 설정
                                    setupRackInfoView(monitorView)
                                    playSound(mainOpening)
                                }
                                // 여기가 변경된 부분: index = 1 처리 방식 변경
                                else if (index == 1) {
                                    // 다른 모든 모니터링 뷰 닫기
                                    for (monitorView in monitorViews) {
                                        monitorView.visibility = View.GONE
                                    }

                                    // 현재 뷰만 표시
                                    val monitorView = monitorViews[index]
                                    monitorView.visibility = View.VISIBLE

                                    // 레이아웃 높이 설정
                                    val layoutParams = monitorView.layoutParams
                                    val displayMetrics = resources.displayMetrics
                                    val isNarrowScreen =
                                        screenWidth < (400 * displayMetrics.density)
                                    val isVeryNarrowScreen =
                                        screenWidth < (370 * displayMetrics.density)
                                    layoutParams.height = when {
                                        isVeryNarrowScreen -> 550
                                        isNarrowScreen -> 580
                                        else -> 620
                                    }
                                    monitorView.layoutParams = layoutParams

                                    // 기존 setupWeeklyRewardsChart 대신 setupMinerInfoView 호출
                                    setupMinerInfoView(monitorView)
                                    playSound(mainOpening)
                                }
                                // Filecoin Storage (index 13) - show filecoin storage overview
                                else if (index == 13) {
                                    // 다른 모든 모니터링 뷰 닫기
                                    for (monitorView in monitorViews) {
                                        monitorView.visibility = View.GONE
                                    }

                                    // 현재 뷰만 표시
                                    val monitorView = monitorViews[index]
                                    monitorView.visibility = View.VISIBLE

                                    // 레이아웃 높이 설정
                                    val layoutParams = monitorView.layoutParams
                                    val displayMetrics = resources.displayMetrics
                                    val isNarrowScreen =
                                        screenWidth < (400 * displayMetrics.density)
                                    val isVeryNarrowScreen =
                                        screenWidth < (370 * displayMetrics.density)
                                    layoutParams.height = when {
                                        isVeryNarrowScreen -> 650
                                        isNarrowScreen -> 680
                                        else -> 720
                                    }
                                    monitorView.layoutParams = layoutParams

                                    // Filecoin Storage 정보 뷰 설정
                                    setupFilecoinStorageView(monitorView)
                                    playSound(mainOpening)
                                } else {
                                    showMonitorInfo(imageView)
                                    playSound(mainOpening)
                                }
                            }

                            else -> {
                                // Second or more touch: Close everything
                                closeEverything()
                                touchCount = 0 // Reset touch count
                            }
                        }

                        applyElasticEffect(v)
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        imageView.alpha = 1.0f
                    }
                }
                true
            }
        }
    }

    fun close_down_all() {
        closeEverything()
        touchCount = 0
        currentSelectedImageView = null
    }

    // Function to close everything
    private fun closeEverything() {
        for (i in monitorViews.indices) {
            val monitorView = monitorViews[i]

            monitorView.visibility = View.GONE

            val layoutParams = monitorView.layoutParams
            layoutParams.height = 0
            monitorView.layoutParams = layoutParams
        }

        currentSelectedImageView = null
    }
}
