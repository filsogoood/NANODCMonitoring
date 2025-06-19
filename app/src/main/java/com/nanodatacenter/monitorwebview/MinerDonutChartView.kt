package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 마이너 정보를 도넛 차트로 표시하는 커스텀 뷰
 * 가용 잔액, 초기 서약, 잠긴 리워드를 시각적으로 표현
 */
class MinerDonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    // 차트 데이터
    private var values = floatArrayOf()
    private var labels = arrayOf<String>()
    private var colors = intArrayOf()

    // 기본 색상 - 파란색(가용), 녹색(서약), 빨간색(잠김)
    private val defaultColors = intArrayOf(
        Color.parseColor("#42A5F5"), // 가용 잔액 - 파란색
        Color.parseColor("#4CAF50"), // 초기 서약 - 녹색
        Color.parseColor("#F44336")  // 잠긴 리워드 - 빨간색
    )

    // 화면 적응형 레이아웃용 변수
    private var isNarrowScreen = false

    init {
        // 도넛 차트 기본 스타일 설정
        paint.style = Paint.Style.STROKE

        // 텍스트 설정
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER

        // 화면 크기 확인
        checkScreenWidth()

        // 기본 데이터 설정
        setDefaultData()
    }

    /**
     * 화면 너비에 따라 UI 요소 조정
     */
    private fun checkScreenWidth() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        // 화면 크기에 따른 텍스트 크기 조정
        textPaint.textSize = if (isNarrowScreen) 14f else 16f
    }

    /**
     * 기본 데이터 설정
     */
    private fun setDefaultData() {
        // 실제 FIL 값 (스크린샷 기준)
        values = floatArrayOf(1065.7558f, 2996.9603f, 133.8326f)
        labels = arrayOf("Available", "Pledged", "Locked")
        colors = defaultColors
    }

    /**
     * 차트 데이터 설정
     */
    fun setChartData(newValues: FloatArray, newLabels: Array<String> = arrayOf(), newColors: IntArray = intArrayOf()) {
        if (newValues.isEmpty()) return

        values = newValues

        if (newLabels.isNotEmpty()) {
            labels = newLabels
        }

        if (newColors.isNotEmpty()) {
            colors = newColors
        } else {
            colors = defaultColors
        }

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 화면 크기 확인
        checkScreenWidth()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (values.isEmpty()) return

        // 차트 그리기
        drawDonutChart(canvas)
    }

    /**
     * 도넛 차트 그리기
     */
    private fun drawDonutChart(canvas: Canvas) {
        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2
        val radius = Math.min(width, height) / 2 * 0.8f
        val strokeWidth = radius * 0.35f

        // 도넛 차트 설정
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth

        // 도넛 차트 영역 설정
        rectF.set(
            centerX - radius + strokeWidth/2,
            centerY - radius + strokeWidth/2,
            centerX + radius - strokeWidth/2,
            centerY + radius - strokeWidth/2
        )

        // 총 합계 계산
        val total = values.sum()

        // 각 섹션 그리기
        var startAngle = 0f
        for (i in values.indices) {
            val sweepAngle = (values[i] / total) * 360f
            paint.color = if (i < colors.size) colors[i] else defaultColors[i % defaultColors.size]
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
            startAngle += sweepAngle
        }

        // 도넛 효과를 위한 중앙 원 그리기
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#0D2C54") // 카드 배경색과 일치
        canvas.drawCircle(centerX, centerY, radius - strokeWidth, paint)
    }
}