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
import androidx.lifecycle.lifecycleScope

class MainActivity : AppCompatActivity() {
    private lateinit var scrollView: NestedScrollView
    private lateinit var progressBar: RelativeLayout
    private var mediaPlayer: MediaPlayer? = null

    // API 관련 추가
    private lateinit var autoLoginManager: AutoLoginManager

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
    # VRAM: 3.0GB/24GB
    # Temperature: 55.7°C
""".trimIndent(),

        // Storage 1 - storage server 1
        """
        # GPU Server: RTX 3090 × 8
        # CPU: 5.2%
        # Memory: 25.6GB/128GB
        # VRAM: 2.8GB/24GB
        # Temperature: 45.7°C
    """.trimIndent(),

// Storage 2 (Aethir GPU 서버)로 표시되는 부분
        """
        # GPU Server: RTX 3090 × 8
        # CPU: 65.8%
        # Memory: 90.4GB/128GB
        # VRAM: 3.5GB/24GB
        # Temperature: 68.2°C
    """.trimIndent(),
        // Storage 3 - storage server 3 requiring attention
        """
        # GPU Server: RTX 3090 × 8
        # CPU: 65.8%
        # Memory: 90.4GB/128GB
        # VRAM: 3.5GB/24GB
        # Temperature: 68.2°C
    """.trimIndent(),

        // Filecoin Storage - storage server 4 in normal state
        """
    # Server: Filecoin Storage
    # Total Capacity: 2.0 PiB (1.94 PiB)
    # Used Storage: 1,295.2 TiB (65.2%)
    # Free Space: 691.3 TiB
    # Temperature: 23°C (Normal)
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

    /**
     * 전체화면 설정을 안전하게 수행하는 메서드
     */
    private fun setupFullScreen() {
        try {
            // 최신 방식으로 전체화면 설정 (API 30+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false)
                window.insetsController?.let { controller ->
                    controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // 기존 방식 (API 30 미만)
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
                @Suppress("DEPRECATION")
                val decorView = window.decorView
                @Suppress("DEPRECATION")
                val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = uiOptions
            }
        } catch (e: Exception) {
            Log.e("NANODP_MAIN", "전체화면 설정 중 오류 발생: ${e.message}")
            // 오류 발생 시 기본 전체화면 모드로 대체
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    /**
     * ViewGroup에서 모든 VramGaugeView를 재귀적으로 찾는 메서드
     */
    private fun findVramGaugeViews(viewGroup: ViewGroup): List<VramGaugeView> {
        val vramGaugeViews = mutableListOf<VramGaugeView>()
        
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is VramGaugeView) {
                vramGaugeViews.add(child)
            } else if (child is ViewGroup) {
                vramGaugeViews.addAll(findVramGaugeViews(child))
            }
        }
        
        return vramGaugeViews
    }
    
    /**
     * ViewGroup에서 모든 TemperatureGaugeView를 재귀적으로 찾는 메서드
     */
    private fun findTemperatureGaugeViews(viewGroup: ViewGroup): List<TemperatureGaugeView> {
        val temperatureGaugeViews = mutableListOf<TemperatureGaugeView>()
        
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is TemperatureGaugeView) {
                temperatureGaugeViews.add(child)
            } else if (child is ViewGroup) {
                temperatureGaugeViews.addAll(findTemperatureGaugeViews(child))
            }
        }
        
        return temperatureGaugeViews
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        
        // 전체화면 설정을 setContentView 후에 실행
        setupFullScreen()

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

        // 자동 로그인 매니저 초기화
        autoLoginManager = AutoLoginManager(this, lifecycleScope)

        // Simulating delay for loading completion (instead of original webview loading)
        mHandler.postDelayed({
            progressBar.visibility = View.GONE
            scrollView.visibility = View.VISIBLE
            close_down_all()
            
            // 앱이 로드된 후 3초 뒤에 자동 로그인 시작
            Handler().postDelayed({
                Log.i("NANODP_MAIN", "🚀 자동 로그인 시작")
                autoLoginManager.startAutoLogin()
            }, 3000)
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
                // index 1과 13은 콘텐츠가 이미 설정된 상태이므로 별도 콘텐츠 설정 없이 진행
                // index 13 (filecoin storage)은 별도 처리하므로 스킵
                if (i == 13) continue

                // Apply animation only if the monitoring view is closed
                if (monitorView.visibility == View.GONE) {
                    monitorView.visibility = View.VISIBLE
                    val layoutParams = monitorView.layoutParams

                    // 카드의 실제 크기를 측정하여 동적으로 높이 설정
                    val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(screenWidth - 16, View.MeasureSpec.AT_MOST)
                    val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    
                    monitorView.measure(widthMeasureSpec, heightMeasureSpec)
                    val measuredHeight = monitorView.measuredHeight
                    
                    // 자식 뷰의 실제 높이를 기준으로 최소 여백만 추가
                    val targetHeight = if (measuredHeight > 0 && monitorView.childCount > 0) {
                        val child = monitorView.getChildAt(0)
                        val contentHeight = child.measuredHeight
                        
                        val minPadding = when {
                            isVeryNarrowScreen -> 20
                            isNarrowScreen -> 24  
                            else -> 28
                        }
                        contentHeight + minPadding
                    } else {
                        // 측정에 실패한 경우 기존 로직 사용
                        when (i) {
                            4 -> when {
                                isVeryNarrowScreen -> 660
                                isNarrowScreen -> 690
                                else -> 720
                            }
                            5 -> when {
                                isVeryNarrowScreen -> 660
                                isNarrowScreen -> 690
                                else -> 720
                            }
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
                            10 -> when {
                                isVeryNarrowScreen -> 630
                                isNarrowScreen -> 690
                                else -> 720
                            }
                            12 -> when {
                                isVeryNarrowScreen -> 630
                                isNarrowScreen -> 690
                                else -> 720
                            }
                            13 -> when {
                                isVeryNarrowScreen -> 440
                                isNarrowScreen -> 440
                                else -> 440
                            }
                            in listOf(11, 14, 15) -> when {
                                isVeryNarrowScreen -> 750
                                isNarrowScreen -> 780
                                else -> 820
                            }
                            else -> when {
                                isVeryNarrowScreen -> 600
                                isNarrowScreen -> 650
                                else -> 700
                            }
                        }
                    }

                    val viewAnimator = ValueAnimator.ofInt(0, targetHeight)
                    viewAnimator.addUpdateListener { animation ->
                        layoutParams.height = animation.animatedValue as Int
                        monitorView.layoutParams = layoutParams
                    }

                    viewAnimator.duration = 200
                    viewAnimator.start()
                    
                    // 기존에 생성된 MaterialCardView를 찾아서 애니메이션 적용
                    val childCount = monitorView.childCount
                    for (childIndex in 0 until childCount) {
                        val child = monitorView.getChildAt(childIndex)
                        if (child is LinearLayout) {
                            // LinearLayout 내부의 MaterialCardView 찾기
                            val innerChildCount = child.childCount
                            for (innerIndex in 0 until innerChildCount) {
                                val innerChild = child.getChildAt(innerIndex)
                                if (innerChild is MaterialCardView) {
                                    // MaterialCardView에 fade in 애니메이션 적용
                                    val fadeInAnimation = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
                                    fadeInAnimation.duration = 800
                                    fadeInAnimation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                                        override fun onAnimationStart(animation: android.view.animation.Animation?) {
                                            innerChild.alpha = 0f
                                            innerChild.animate().alpha(1f).setDuration(800).start()
                                        }
                                        override fun onAnimationEnd(animation: android.view.animation.Animation?) {}
                                        override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
                                    })
                                    innerChild.startAnimation(fadeInAnimation)
                                    
                                    // VramGaugeView 찾아서 애니메이션 재시작
                                    findVramGaugeViews(innerChild).forEach { vramGauge ->
                                        vramGauge.restartAnimationIfNeeded()
                                    }
                                    
                                    // TemperatureGaugeView 찾아서 애니메이션 재시작
                                    findTemperatureGaugeViews(innerChild).forEach { tempGauge ->
                                        tempGauge.restartAnimationIfNeeded()
                                    }
                                    
                                    // SUPRA WORKER 진행률 바 애니메이션 재시작
                                    EnhancedMonitorPresenter.restartProgressBarAnimations(innerChild)
                                    break
                                }
                            }
                        }
                    }
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

    // setupRackInfoView 메서드 - 웹의 SkynetScore와 동일한 UI
    private fun setupRackInfoView(container: LinearLayout) {
        // 기존 뷰를 모두 제거
        container.removeAllViews()

        // 화면 너비 확인
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        val isVeryNarrowScreen = screenWidth < (370 * displayMetrics.density)
        
        // 유니크한 로그 태그로 API 데이터 추적
        Log.d("SKYNET_SCORE_UI", "========== SkynetScore UI 데이터 검증 시작 ==========")
        
        // AutoLoginManager 상태 확인
        Log.i("SKYNET_SCORE_UI", "🔍 AutoLoginManager 상태 점검:")
        Log.i("SKYNET_SCORE_UI", "  - 데이터 로드 상태: ${autoLoginManager.isDataLoaded()}")
        Log.i("SKYNET_SCORE_UI", "  - 인증 토큰 존재: ${autoLoginManager.getAuthToken() != null}")
        
        // 데이터가 로드되었는지 확인
        if (!autoLoginManager.isDataLoaded()) {
            Log.e("SKYNET_SCORE_UI", "❌ API 데이터가 아직 로드되지 않았습니다!")
        }
        
        // 방법 1: BC02 점수 가져오기
        Log.d("SKYNET_SCORE_UI", "🔄 방법 1: BC02 점수 직접 조회")
        var bc02Score = autoLoginManager.getBC02Score()
        Log.d("SKYNET_SCORE_UI", "  - BC02 점수 결과: ${if (bc02Score != null) "성공" else "null"}")
        
        // 방법 2: BC02 점수가 없고 89점을 원한다면, 89점인 노드 찾기
        if (bc02Score == null) {
            Log.w("SKYNET_SCORE_UI", "🔄 방법 2: 89점인 노드 검색 시도")
            bc02Score = autoLoginManager.getScoreByAverage("89")
            Log.d("SKYNET_SCORE_UI", "  - 89점 노드 검색 결과: ${if (bc02Score != null) "성공" else "null"}")
        }
        
        // 방법 3: 그래도 없으면 첫 번째 노드 사용
        if (bc02Score == null) {
            Log.w("SKYNET_SCORE_UI", "🔄 방법 3: 첫 번째 노드 사용 시도")
            bc02Score = autoLoginManager.getScoreByIndex(0)
            Log.d("SKYNET_SCORE_UI", "  - 첫 번째 노드 검색 결과: ${if (bc02Score != null) "성공" else "null"}")
        }
        
        // 최종 점수 데이터 상세 로그
        if (bc02Score == null) {
            Log.e("SKYNET_SCORE_UI", "❌ 모든 방법으로도 점수를 찾을 수 없음!")
            Log.w("SKYNET_SCORE_UI", "🔧 기본값을 사용하여 UI 구성")
        } else {
            Log.d("SKYNET_SCORE_UI", "✅ 최종 BC02 점수 데이터:")
            Log.d("SKYNET_SCORE_UI", "  - averageScore: ${bc02Score.averageScore ?: "null"}")
            Log.d("SKYNET_SCORE_UI", "  - cpuScore: ${bc02Score.cpuScore ?: "null"}")
            Log.d("SKYNET_SCORE_UI", "  - gpuScore: ${bc02Score.gpuScore ?: "null"}")
            Log.d("SKYNET_SCORE_UI", "  - ssdScore: ${bc02Score.ssdScore ?: "null"}")
        }
        
        // 점수 데이터 변환 및 처리
        val averageScore = bc02Score?.averageScore ?: "none"
        val scoreFloat = averageScore.toFloatOrNull() ?: 0f  // null일 때 0으로 표시
        
        Log.i("SKYNET_SCORE_UI", "📊 점수 변환 결과:")
        Log.i("SKYNET_SCORE_UI", "  - 원본 averageScore: '$averageScore'")
        Log.i("SKYNET_SCORE_UI", "  - 변환된 scoreFloat: $scoreFloat")
        
        // BC02 점수에서 개별 메트릭 추출 (실제 API 값 사용)
        val cpuScore = bc02Score?.cpuScore?.toFloatOrNull() ?: 0f
        val gpuScore = bc02Score?.gpuScore?.toFloatOrNull() ?: 0f  
        val ramScore = bc02Score?.ramScore?.toFloatOrNull() ?: 0f        // 실제 API 값 사용!
        val ssdScore = bc02Score?.ssdScore?.toFloatOrNull() ?: 0f
        val networkScore = bc02Score?.networkScore?.toFloatOrNull() ?: 0f  // 실제 API 값 사용!
        val healthScore = bc02Score?.hardwareHealthScore?.toFloatOrNull() ?: 0f  // 실제 API 값 사용!
        
        Log.i("SKYNET_SCORE_UI", "📈 개별 메트릭 점수:")
        Log.i("SKYNET_SCORE_UI", "  - CPU: ${if (cpuScore == 0f && bc02Score?.cpuScore == null) "null" else cpuScore} (원본: ${bc02Score?.cpuScore ?: "null"})")
        Log.i("SKYNET_SCORE_UI", "  - GPU: ${if (gpuScore == 0f && bc02Score?.gpuScore == null) "null" else gpuScore} (원본: ${bc02Score?.gpuScore ?: "null"})")
        Log.i("SKYNET_SCORE_UI", "  - RAM: ${if (ramScore == 0f && bc02Score?.ramScore == null) "null" else ramScore} (원본: ${bc02Score?.ramScore ?: "null"})")
        Log.i("SKYNET_SCORE_UI", "  - SSD: ${if (ssdScore == 0f && bc02Score?.ssdScore == null) "null" else ssdScore} (원본: ${bc02Score?.ssdScore ?: "null"})")
        Log.i("SKYNET_SCORE_UI", "  - Network: ${if (networkScore == 0f && bc02Score?.networkScore == null) "null" else networkScore} (원본: ${bc02Score?.networkScore ?: "null"})")
        Log.i("SKYNET_SCORE_UI", "  - Health: ${if (healthScore == 0f && bc02Score?.hardwareHealthScore == null) "null" else healthScore} (원본: ${bc02Score?.hardwareHealthScore ?: "null"})")

        // 웹의 SkynetScore와 동일한 스타일의 메인 카드
        val mainCard = MaterialCardView(this).apply {
            radius = 20f
            cardElevation = 16f
            setCardBackgroundColor(Color.parseColor("#1F2937"))  // bg-gray-800/50
            strokeColor = Color.parseColor("#374151")  // border-gray-700/50
            strokeWidth = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(8, 8, 8, 8)
            }
        }

        val mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(20, 20, 20, 20)
        }

        // 헤더 - 웹과 동일한 구조
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
            gravity = Gravity.CENTER_VERTICAL
        }

        // 제목과 파란색 라인
        val titleContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER_VERTICAL
        }

        // 파란색 라인 (웹의 w-1.5 h-6 bg-blue-500)
        val blueLine = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                6,  // 1.5 * 4 = 6dp
                24  // 6 * 4 = 24dp
            ).apply {
                marginEnd = 8
            }
            setBackgroundColor(Color.parseColor("#3B82F6"))  // bg-blue-500
        }

        val titleText = TextView(this).apply {
            text = "Score"
            textSize = if (isNarrowScreen) 18f else 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
        }

        titleContainer.addView(blueLine)
        titleContainer.addView(titleText)

        // Shield 아이콘 (웹의 Shield 컴포넌트)
        val shieldIcon = ImageView(this).apply {
            setImageResource(android.R.drawable.ic_secure)  // 기본 보안 아이콘 사용
            layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                marginStart = 8
            }
            setColorFilter(Color.parseColor("#60A5FA"))  // text-blue-400
        }

        headerLayout.addView(titleContainer)
        headerLayout.addView(shieldIcon)

        // 점수 표시 박스 - 웹과 동일한 스타일
        val scoreCard = MaterialCardView(this).apply {
            radius = 12f
            cardElevation = 8f
            setCardBackgroundColor(Color.parseColor("#111827"))  // bg-gray-900/50
            strokeColor = Color.parseColor("#4B5563")  // border-gray-600/30
            strokeWidth = 1
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 24)
            }
        }

        val scoreContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(16, 16, 16, 16)
        }

        // 큰 점수 숫자 (웹의 text-5xl font-bold text-yellow-400)
        val scoreText = TextView(this).apply {
            val displayText = if (averageScore == "none") {
                "none"
            } else if (scoreFloat == 0f) {
                "null" 
            } else {
                String.format("%.2f", scoreFloat)
            }
            
            Log.i("SKYNET_SCORE_UI", "🎯 메인 점수 표시:")
            Log.i("SKYNET_SCORE_UI", "  - 표시할 텍스트: '$displayText'")
            Log.i("SKYNET_SCORE_UI", "  - averageScore: '$averageScore'")
            Log.i("SKYNET_SCORE_UI", "  - scoreFloat: $scoreFloat")
            
            text = displayText
            textSize = if (isNarrowScreen) 36f else 48f  // text-5xl
            setTextColor(if (displayText == "none" || displayText == "null") {
                Color.parseColor("#9CA3AF")  // 회색으로 표시
            } else {
                Color.parseColor("#FBBF24")  // text-yellow-400
            })
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        scoreContainer.addView(scoreText)
        scoreCard.addView(scoreContainer)

        // 육각형 차트 컨테이너
        val chartContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
        }

        // SkynetScoreView 추가 - 웹과 동일한 육각형 차트
        val skynetScoreView = SkynetScoreView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                if (isNarrowScreen) 320 else 400,
                if (isNarrowScreen) 320 else 400
            )
            
            Log.i("SKYNET_SCORE_UI", "🎨 SkynetScoreView 메트릭 설정:")
            Log.i("SKYNET_SCORE_UI", "  - CPU 점수 전달: ${if (cpuScore == 0f && bc02Score?.cpuScore == null) "null" else cpuScore}")
            Log.i("SKYNET_SCORE_UI", "  - GPU 점수 전달: ${if (gpuScore == 0f && bc02Score?.gpuScore == null) "null" else gpuScore}") 
            Log.i("SKYNET_SCORE_UI", "  - RAM 점수 전달: ${if (ramScore == 0f && bc02Score?.ramScore == null) "null" else ramScore}")
            Log.i("SKYNET_SCORE_UI", "  - SSD 점수 전달: ${if (ssdScore == 0f && bc02Score?.ssdScore == null) "null" else ssdScore}")
            Log.i("SKYNET_SCORE_UI", "  - Network 점수 전달: ${if (networkScore == 0f && bc02Score?.networkScore == null) "null" else networkScore}")
            Log.i("SKYNET_SCORE_UI", "  - Health 점수 전달: ${if (healthScore == 0f && bc02Score?.hardwareHealthScore == null) "null" else healthScore}")
            
            // API 데이터 존재 여부 확인
            Log.i("SKYNET_SCORE_UI", "🔍 API 데이터 검증:")
            Log.i("SKYNET_SCORE_UI", "  - bc02Score 객체: ${if (bc02Score != null) "존재" else "null"}")
            if (bc02Score != null) {
                Log.i("SKYNET_SCORE_UI", "  - 원본 RAM 점수: '${bc02Score.ramScore}'")
                Log.i("SKYNET_SCORE_UI", "  - 원본 Network 점수: '${bc02Score.networkScore}'")
                Log.i("SKYNET_SCORE_UI", "  - 원본 Health 점수: '${bc02Score.hardwareHealthScore}'")
            }
            
            // BC02 점수 데이터로 메트릭 설정
            setMetrics(
                cpu = cpuScore,
                gpu = gpuScore,
                ram = ramScore,
                ssd = ssdScore,
                network = networkScore,
                health = healthScore
            )
            
            Log.d("SKYNET_SCORE_UI", "✅ SkynetScoreView 메트릭 설정 완료")
        }

        chartContainer.addView(skynetScoreView)

        // 모든 컴포넌트를 메인 컨테이너에 추가
        mainContainer.addView(headerLayout)
        mainContainer.addView(scoreCard)
        mainContainer.addView(chartContainer)

        mainCard.addView(mainContainer)
        container.addView(mainCard)

        // 애니메이션 적용
        val animation = android.view.animation.AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        animation.duration = 1000
        mainCard.startAnimation(animation)
        
        Log.d("SKYNET_SCORE_UI", "🎉 SkynetScore UI 구성 완료")
        Log.d("SKYNET_SCORE_UI", "========== SkynetScore UI 데이터 검증 종료 ==========")
    }

    /**
     * DePIN SCORE 행 생성 메서드
     */
    private fun createDePINScoreRow(): LinearLayout {
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 16)
            }
            gravity = Gravity.CENTER_VERTICAL

            // DePIN SCORE 레이블
            val scoreLabel = TextView(this@MainActivity).apply {
                text = "DePIN SCORE:"
                textSize = if (isNarrowScreen) 14f else 16f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 16
                }
            }

            // DePIN SCORE 값 (94점, 초록색, BOLD)
            val scoreValue = TextView(this@MainActivity).apply {
                text = "94"
                textSize = if (isNarrowScreen) 24f else 28f
                setTextColor(Color.parseColor("#4CAF50"))
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
            }

            // 점수 단위
            val scoreUnit = TextView(this@MainActivity).apply {
                text = "/ 100"
                textSize = if (isNarrowScreen) 14f else 16f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // 상태 설명
            val statusText = TextView(this@MainActivity).apply {
                text = "Excellent"
                textSize = if (isNarrowScreen) 12f else 14f
                setTextColor(Color.parseColor("#4CAF50"))
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = 16
                }
                gravity = Gravity.END
            }

            addView(scoreLabel)
            addView(scoreValue)
            addView(scoreUnit)
            addView(statusText)
        }
    }

    /**
     * 단일 토큰 정보 행 생성 메서드
     */
    private fun createSingleTokenInfoRow(label: String, value: String): LinearLayout {
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            gravity = Gravity.CENTER_VERTICAL

            // 레이블
            val labelView = TextView(this@MainActivity).apply {
                text = label
                textSize = if (isNarrowScreen) 12f else 14f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 16
                }
            }

            // 값
            val valueView = TextView(this@MainActivity).apply {
                text = value
                textSize = if (isNarrowScreen) 13f else 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            addView(labelView)
            addView(valueView)
        }
    }

    /**
     * 색상이 적용된 토큰 정보 행 생성 메서드
     */
    private fun createTokenInfoRowWithColor(
        label: String,
        value: String,
        color: Int
    ): LinearLayout {
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
            gravity = Gravity.CENTER_VERTICAL

            // 색상 표시기 (작은 사각형) - 주석처리
            // val colorView = View(this@MainActivity).apply {
            //     layoutParams = LinearLayout.LayoutParams(
            //         16,
            //         16
            //     ).apply {
            //         marginEnd = 12
            //     }
            //     setBackgroundColor(color)
            // }

            // 레이블
            val labelView = TextView(this@MainActivity).apply {
                text = label
                textSize = if (isNarrowScreen) 12f else 14f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 16
                }
            }

            // 값
            val valueView = TextView(this@MainActivity).apply {
                text = value
                textSize = if (isNarrowScreen) 13f else 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // addView(colorView)  // 색상 표시기 주석처리
            addView(labelView)
            addView(valueView)
        }
    }

    /**
     * 토큰 정보 행 생성 헬퍼 메서드
     */
    private fun createTokenInfoRow(label1: String, value1: String, label2: String, value2: String): LinearLayout {
        val displayMetrics = resources.displayMetrics
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }

            // 첫 번째 정보
            val leftContainer = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginEnd = if (isNarrowScreen) 8 else 16
                }
                gravity = Gravity.CENTER_VERTICAL
            }

            val leftLabel = TextView(this@MainActivity).apply {
                text = label1
                textSize = if (isNarrowScreen) 12f else 14f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
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
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                gravity = Gravity.CENTER_VERTICAL
            }

            val rightLabel = TextView(this@MainActivity).apply {
                text = label2
                textSize = if (isNarrowScreen) 12f else 14f
                setTextColor(Color.parseColor("#B0BEC5"))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = 8
                }
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
            ).apply {
                setMargins(8, 8, 8, 8)
            }
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
            setPadding(horizontalPadding, 8, horizontalPadding, 16)
        }

        // 헤더 생성
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 4)
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
            text = "Miner Address: f02368818"
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
            "541.69 PiB",
            listOf(
                Pair("Raw Byte Power:", "541.69 PiB"),
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
            setColorFilter(Color.parseColor("#4CAF50")) // 초록색 필터
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

        // 막대 차트 (원형 차트 대신)
        val storageChart = StorageBarChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (isNarrowScreen) 180 else 200
            )
            setStorageData(1295.2f, 1986.56f, "Storage") // 65.2% 사용률
        }

        chartSection.addView(storageChart)

        // 온도계와 스토리지 차트를 나란히 배치하는 컨테이너
        val metricsContainer = LinearLayout(this).apply {
            orientation = if (isNarrowScreen) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 0)
        }

        // 온도계 그래프 추가
        val temperatureGauge = TemperatureGaugeView(this).apply {
            layoutParams = if (isNarrowScreen) {
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    220
                )
            } else {
                LinearLayout.LayoutParams(
                    0,
                    240,
                    1f
                )
            }
            setTemperature(23f) // 23도로 설정
        }

        metricsContainer.addView(temperatureGauge)
        chartSection.addView(metricsContainer)
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
            "NVIDA RTX 3090 X 4",
            "GPU Server RTX",
            "Aethir GPU Server",
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
                "SAi GPU Server",
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
                "SAi GPU Server",
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

            // 인덱스 0을 제외한 모든 카드에 상하좌우 마진 8 적용
            if (i != 0) {
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                enhancedView.layoutParams = layoutParams
            }

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

            // 터치시 반응 없음 (로고 제외)
            if (index == 2 || index == 3 || index == 7 || index == 8 || index == 9 ||  index == 11 || index == 14 || index == 15) {
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
                                // 로고 더블탭 대기 (index 16)
                                if (index == 16) {
                                    // 로고는 첫 번째 탭에서 아무것도 하지 않고 더블탭을 대기
                                    Log.i("NANODP_MAIN", "🔄 로고 첫 번째 탭 - 더블탭 대기 중")
                                    
                                    // 1.5초 후에 touchCount 리셋 (더블탭 타이밍 제한)
                                    touchCountResetHandler.removeCallbacksAndMessages(null)
                                    touchCountResetHandler.postDelayed({
                                        touchCount = 0
                                        currentSelectedImageView = null
                                        Log.d("NANODP_MAIN", "🕐 더블탭 시간 초과 - 카운트 리셋")
                                    }, 1500)
                                }
                                // Rack Info (index 0) - show rack overview
                                else if (index == 0) {
                                    Log.d("BC02_SCORE_DEBUG", "========== Index 0 클릭 이벤트 ==========")
                                    Log.d("BC02_SCORE_DEBUG", "🔍 AutoLoginManager 상태 확인")
                                    
                                    // API 통신 상태 확인 로그 추가
                                    Log.i("NANODP_MAIN", "🔍 API 통신 상태 점검 시작")
                                    Log.i("NANODP_MAIN", "📱 인증 토큰 상태: ${if (autoLoginManager.getAuthToken() != null) "존재" else "없음"}")
                                    Log.i("NANODP_MAIN", "📊 데이터 로드 상태: ${if (autoLoginManager.isDataLoaded()) "완료" else "미완료"}")
                                    
                                    // 데이터가 로드되지 않은 경우 재시도
                                    if (!autoLoginManager.isDataLoaded()) {
                                        Log.w("NANODP_MAIN", "⚠️ 데이터가 로드되지 않음 - 자동 로그인 재시도")
                                        autoLoginManager.startAutoLogin()
                                        
                                        // 3초 후 다시 확인
                                        Handler().postDelayed({
                                            if (!autoLoginManager.isDataLoaded()) {
                                                Log.e("NANODP_MAIN", "❌ API 통신 실패 - 기본값으로 표시")
                                            }
                                        }, 3000)
                                    }
                                    
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
                                        isVeryNarrowScreen -> 650  // SkynetScore UI를 위해 높이 증가
                                        isNarrowScreen -> 700
                                        else -> 750
                                    }
                                    monitorView.layoutParams = layoutParams

                                    // 랙 정보 뷰 설정
                                    setupRackInfoView(monitorView)
                                    playSound(mainOpening)
                                    
                                    Log.d("BC02_SCORE_DEBUG", "========== Index 0 처리 완료 ==========")
                                }
                                // index 1은 setupMinerInfoView 후 자동 측정 적용
                                else if (index == 1) {
                                    // 먼저 setupMinerInfoView로 콘텐츠 설정
                                    setupMinerInfoView(monitorViews[index])
                                    // 그 다음 자동 측정으로 크기 조정
                                    showMonitorInfo(imageView)
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

                            2 -> {
                                // 로고 더블탭 - NDP Score 새로고침 (index 16)
                                if (index == 16) {
                                    Log.i("NANODP_MAIN", "🔄 로고 더블탭 성공 - NDP Score 새로고침 요청")
                                    Log.i("NDP_SCORE_MONITOR", "🔄 사용자 요청으로 인한 NDP Score 새로고침")
                                    touchCountResetHandler.removeCallbacksAndMessages(null)
                                    
                                    // NDP Score 새로고침 실행
                                    autoLoginManager.refreshNdpScore()
                                    
                                    // 터치 카운트 리셋
                                    touchCount = 0
                                    currentSelectedImageView = null
                                } else {
                                    // 다른 이미지들은 두 번째 터치에서 모든 것을 닫음
                                    closeEverything()
                                    touchCount = 0 // Reset touch count
                                }
                            }

                            else -> {
                                // 세 번째 이상 터치: 모든 것을 닫음
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
