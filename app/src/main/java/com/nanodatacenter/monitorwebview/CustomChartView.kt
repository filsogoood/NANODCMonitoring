package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

/**
 * Custom view for visual data chart representation
 */
class CustomChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chartRect = RectF()
    private val path = Path()

    // Chart types
    enum class ChartType {
        LINE, BAR, AREA, DONUT
    }

    // Chart data
    private var values = floatArrayOf()
    private var labels = arrayOf<String>()
    private var chartType = ChartType.LINE
    private var maxValue = 100f
    private val startColor = 0xFF4FC3F7.toInt() // Light blue
    private val endColor = 0xFF2196F3.toInt()   // Standard blue

    // Animation related
    private var animationProgress = 0f
    private var targetValues = floatArrayOf()
    private var isAnimating = false

    // 화면 적응형 레이아웃을 위한 변수
    private var isNarrowScreen = false

    init {
        // Generate default dummy data
        generateRandomData()

        // 초기 화면 너비 체크
        checkScreenWidth()
    }

    /**
     * 화면 너비에 따라 UI 요소 조정
     */
    private fun checkScreenWidth() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        isNarrowScreen = screenWidth < (400 * displayMetrics.density)
    }

    fun setChartType(type: ChartType) {
        chartType = type
        invalidate()
    }

    fun setData(newValues: FloatArray, newLabels: Array<String>, newMaxValue: Float = 100f) {
        // Save previous data
        if (values.isNotEmpty()) {
            targetValues = newValues.clone()
            isAnimating = true
            animationProgress = 0f
            // Add ValueAnimator here if you want to use animation
        } else {
            values = newValues.clone()
        }

        labels = newLabels
        maxValue = newMaxValue
        invalidate()
    }

    // Generate random data (for testing)
    fun generateRandomData() {
        val size = if (isNarrowScreen) 8 else 12 // 좁은 화면에서는 데이터 포인트 줄임
        values = FloatArray(size) { Random.nextFloat() * 80 + 10 }
        labels = Array(size) { "Point $it" }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 화면 너비 확인
        checkScreenWidth()

        // Calculate chart area - 좁은 화면에서는 패딩 줄임
        val padding = if (isNarrowScreen) 20f else 30f
        chartRect.set(
            padding,
            padding,
            width.toFloat() - padding,
            height.toFloat() - padding
        )

        // Set gradient
        when (chartType) {
            ChartType.LINE, ChartType.AREA -> {
                paint.shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    startColor, endColor,
                    Shader.TileMode.CLAMP
                )
            }
            else -> {
                paint.shader = null
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (values.isEmpty()) return

        when (chartType) {
            ChartType.LINE -> drawLineChart(canvas)
            ChartType.BAR -> drawBarChart(canvas)
            ChartType.AREA -> drawAreaChart(canvas)
            ChartType.DONUT -> drawDonutChart(canvas)
        }
    }

// CustomChartView.kt에 개선 메서드 추가

    // drawLineChart 메서드 내부에 그래프 값 라벨 표시 기능 추가
    private fun drawLineChart(canvas: Canvas) {
        if (values.isEmpty()) return

        val width = chartRect.width()
        val height = chartRect.height()
        val segmentWidth = width / (values.size - 1)

        // Draw line
        paint.color = 0xFF2196F3.toInt()  // Material Blue
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (isNarrowScreen) 3f else 5f
        paint.shader = null

        path.reset()
        path.moveTo(chartRect.left, chartRect.bottom - (values[0] / maxValue) * height)

        for (i in 1 until values.size) {
            val x = chartRect.left + i * segmentWidth
            val y = chartRect.bottom - (values[i] / maxValue) * height
            path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)

        // Draw points and value labels
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()  // White
        val pointRadius = if (isNarrowScreen) 5f else 8f
        val innerPointRadius = if (isNarrowScreen) 3f else 5f

        // 텍스트 설정
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = if (isNarrowScreen) 12f else 16f
            textAlign = Paint.Align.CENTER
        }

        for (i in values.indices) {
            val x = chartRect.left + i * segmentWidth
            val y = chartRect.bottom - (values[i] / maxValue) * height

            // 포인트 그리기
            canvas.drawCircle(x, y, pointRadius, paint)
            paint.color = 0xFF2196F3.toInt()  // Material Blue
            canvas.drawCircle(x, y, innerPointRadius, paint)
            paint.color = 0xFFFFFFFF.toInt()  // White

            // 값 라벨 표시 (리워드 차트용)
            val valueText = String.format("%.1f", values[i])
            canvas.drawText(valueText, x, y - 15f, valuePaint)

            // x축 레이블 표시 (요일)
            if (labels.isNotEmpty() && i < labels.size) {
                canvas.drawText(labels[i], x, chartRect.bottom + 25f, valuePaint)
            }
        }
    }

    // 특별히 리워드 차트용 메서드 추가
    fun setRewardsData(dailyRewards: FloatArray, dayLabels: Array<String>) {
        // 리워드 데이터 설정 - 최대값을 약간 높게 설정하여 그래프가 더 잘 보이도록 함
        val maxReward = dailyRewards.maxOrNull() ?: 16.0f
        setData(dailyRewards, dayLabels, maxReward + 0.5f)

        // 리워드 차트에 적합한 스타일 설정
        paint.color = 0xFF4CAF50.toInt()  // Green for rewards

        // 그라데이션 설정으로 그래프 더 돋보이게 함
        paint.shader = LinearGradient(
            0f, 0f,
            0f, height.toFloat(),
            0xFF4CAF50.toInt(),  // Green
            0xFF81C784.toInt(),  // Light Green
            Shader.TileMode.CLAMP
        )
    }

    private fun drawBarChart(canvas: Canvas) {
        if (values.isEmpty()) return

        val width = chartRect.width()
        val height = chartRect.height()
        val barWidth = width / (values.size * (if (isNarrowScreen) 1.5f else 2f)) // 좁은 화면에서는 바 너비 조정

        paint.style = Paint.Style.FILL

        for (i in values.indices) {
            // Assign different color for each bar
            val hue = (220 + i * 15) % 360
            paint.color = android.graphics.Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.7f, 0.9f))

            val x = chartRect.left + i * (width / values.size) + barWidth/2
            val barHeight = (values[i] / maxValue) * height

            canvas.drawRoundRect(
                x - barWidth/2,
                chartRect.bottom - barHeight,
                x + barWidth/2,
                chartRect.bottom,
                if (isNarrowScreen) 5f else 8f, // 좁은 화면에서는 모서리 곡률 줄임
                if (isNarrowScreen) 5f else 8f,
                paint
            )
        }
    }

    private fun drawAreaChart(canvas: Canvas) {
        if (values.isEmpty()) return

        val width = chartRect.width()
        val height = chartRect.height()
        val segmentWidth = width / (values.size - 1)

        // Fill area
        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(
            0f, 0f,
            0f, height,
            0x804FC3F7,  // Semi-transparent light blue
            0x102196F3,  // Almost transparent blue
            Shader.TileMode.CLAMP
        )

        path.reset()
        path.moveTo(chartRect.left, chartRect.bottom)
        path.lineTo(chartRect.left, chartRect.bottom - (values[0] / maxValue) * height)

        for (i in 1 until values.size) {
            val x = chartRect.left + i * segmentWidth
            val y = chartRect.bottom - (values[i] / maxValue) * height
            path.lineTo(x, y)
        }

        path.lineTo(chartRect.right, chartRect.bottom)
        path.close()

        canvas.drawPath(path, paint)

        // Draw line - 좁은 화면에서는 선 두께 줄임
        paint.shader = null
        paint.color = 0xFF2196F3.toInt()  // Material Blue
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (isNarrowScreen) 2f else 3f

        path.reset()
        path.moveTo(chartRect.left, chartRect.bottom - (values[0] / maxValue) * height)

        for (i in 1 until values.size) {
            val x = chartRect.left + i * segmentWidth
            val y = chartRect.bottom - (values[i] / maxValue) * height
            path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)
    }

    private fun drawDonutChart(canvas: Canvas) {
        if (values.isEmpty()) return

        val centerX = chartRect.centerX()
        val centerY = chartRect.centerY()
        val radius = Math.min(chartRect.width(), chartRect.height()) / 2 - (if (isNarrowScreen) 10f else 20f)
        val innerRadius = radius * (if (isNarrowScreen) 0.5f else 0.6f) // 좁은 화면에서는 내부 원 비율 줄임

        var startAngle = -90f  // Start from 12 o'clock
        val total = values.sum()

        for (i in values.indices) {
            // Assign different color for each section
            val hue = (220 + i * 25) % 360
            paint.color = android.graphics.Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.7f, 0.9f))
            paint.style = Paint.Style.FILL
            paint.shader = null

            val sweepAngle = (values[i] / total) * 360

            // Draw donut section
            path.reset()
            path.moveTo(centerX, centerY)
            path.arcTo(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius,
                startAngle,
                sweepAngle,
                false
            )
            path.arcTo(
                centerX - innerRadius,
                centerY - innerRadius,
                centerX + innerRadius,
                centerY + innerRadius,
                startAngle + sweepAngle,
                -sweepAngle,
                false
            )
            path.close()

            canvas.drawPath(path, paint)
            startAngle += sweepAngle
        }

        // Center dark circle
        paint.color = 0xFF37474F.toInt()  // Dark background color
        canvas.drawCircle(centerX, centerY, innerRadius * 0.95f, paint)
    }
}