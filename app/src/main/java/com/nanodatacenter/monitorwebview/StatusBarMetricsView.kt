package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Custom view to display status information as horizontal bar graphs
 */
class StatusBarMetricsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private var maxValue = 100f
    private var values = floatArrayOf()
    private var labels = arrayOf<String>()
    private var colors = intArrayOf()

    // 화면 적응형 레이아웃을 위한 변수
    private var isNarrowScreen = false
    private var textOffset = 150f
    private var spacing = 40f

    init {
        // Background setup
        backgroundPaint.color = Color.parseColor("#22FFFFFF")  // Semi-transparent white
        backgroundPaint.style = Paint.Style.FILL

        // Bar setup
        paint.style = Paint.Style.FILL

        // Text setup - modified: increased font size
        textPaint.color = Color.WHITE
        textPaint.textSize = 32f // Increased font size

        // 초기 화면 너비 체크
        adaptToScreenWidth()
    }

    /**
     * 화면 너비에 따라 UI 요소 조정
     */
    private fun adaptToScreenWidth() {
        // 화면 너비 가져오기
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // 좁은 화면 감지 (400dp 미만)
        isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        // 좁은 화면일 경우 텍스트 크기와 여백 조정
        if (isNarrowScreen) {
            textPaint.textSize = 24f // 텍스트 크기 줄임
            textOffset = 80f  // 레이블 공간 줄임
            spacing = 30f     // 바 사이 간격 줄임
        } else {
            textPaint.textSize = 32f
            textOffset = 150f
            spacing = 40f
        }
    }

    fun setData(
        newValues: FloatArray,
        newLabels: Array<String>,
        newColors: IntArray,
        newMaxValue: Float = 100f
    ) {
        values = newValues
        labels = newLabels
        colors = newColors
        maxValue = newMaxValue
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (values.isEmpty() || labels.isEmpty() || colors.isEmpty()) return

        // 화면 크기에 따른 조정
        adaptToScreenWidth()

        // 막대 높이와 간격 줄여서 여러 개가 한 화면에 다 보이도록 함
        val barHeight = 30f // 더 작은 높이
        val spacing = 25f   // 더 좁은 간격
        val padding = 12f   // 더 작은 패딩

        // 좁은 화면에서도 텍스트가 잘 보이도록 크기 조정
        textPaint.textSize = if (isNarrowScreen) 20f else 24f

        for (i in values.indices) {
            val top = padding + i * (barHeight + spacing)

            // 레이블 그리기
            textPaint.textAlign = Paint.Align.RIGHT
            val labelText = if (isNarrowScreen) {
                // 매우 좁은 화면에서는 짧은 레이블 사용
                when (labels[i]) {
                    "CPU" -> "CPU"
                    "Memory" -> "Mem"
                    "Disk" -> "Dis"
                    "Network" -> "Net"
                    else -> labels[i].take(3)
                }
            } else {
                labels[i]
            }

            canvas.drawText(
                labelText,
                textOffset - 10f,
                top + barHeight / 2 + textPaint.textSize / 3,
                textPaint
            )

            // 배경 막대 그리기
            rect.set(
                textOffset,
                top,
                width.toFloat() - padding,
                top + barHeight
            )
            canvas.drawRoundRect(rect, 8f, 8f, backgroundPaint) // 모서리 곡률 줄임

            // 값 막대 그리기 - 최소 너비 설정
            val valueWidth = (values[i] / maxValue) * (width - textOffset - padding * 2)
            val minBarWidth = 40f // 최소 바 너비
            paint.color = colors[i]
            rect.right = textOffset + Math.max(valueWidth, minBarWidth)
            canvas.drawRoundRect(rect, 8f, 8f, paint)

            // 값 텍스트 표시 방식 개선 - 막대 안에 퍼센트 표시
            textPaint.textAlign = Paint.Align.CENTER
            val percentage = (values[i] / maxValue * 100).toInt()

            // 퍼센트 값을 막대 내부에 표시
            val textX = textOffset + valueWidth / 2
            val textColor = if (valueWidth > textPaint.measureText("$percentage%") + 20) {
                Color.WHITE // 충분히 긴 막대는 흰색 텍스트
            } else {
                colors[i] // 짧은 막대는 막대와 같은 색상의 텍스트를 외부에 표시
            }

            textPaint.color = textColor
            canvas.drawText(
                "$percentage%",
                textX,
                top + barHeight / 2 + textPaint.textSize / 3,
                textPaint
            )
            textPaint.color = Color.WHITE // 원래 색상으로 복원
        }
    }

    // Set server status data
    fun setServerMetrics(
        cpuUsage: Float,
        memoryUsage: Float,
        diskUsage: Float,
        networkUsage: Float
    ) {
        val values = floatArrayOf(
            cpuUsage,
            memoryUsage,
            diskUsage,
            networkUsage
        )

        val labels = arrayOf(
            "CPU",
            "Memory",
            "Disk",
            "Network"
        )

        val colors = intArrayOf(
            getColorForPercentage(cpuUsage),
            getColorForPercentage(memoryUsage),
            getColorForPercentage(diskUsage),
            getColorForPercentage(networkUsage)
        )

        setData(values, labels, colors)
    }

    // Set storage status data
    fun setStorageMetrics(spaceUsage: Float, ioRate: Float, temperature: Float) {
        val values = floatArrayOf(
            spaceUsage,
            ioRate,
            temperature,
        )

        val labels = arrayOf(
            "Space",
            "I/O Rate",
            "Temp",
        )

        val colors = intArrayOf(
            getColorForPercentage(spaceUsage),
            getColorForPercentage(ioRate, true),  // Higher is better
            getColorForPercentage(temperature),
        )

        setData(values, labels, colors)
    }

    fun setGpuMetrics(
        gpuUsage: Float,
        temperature: Float,
        memoryUsage: Float,
        flopsUsage: Float
    ) {
        val values = floatArrayOf(
            gpuUsage,
            temperature,
            memoryUsage,
            flopsUsage
        )

        val labels = arrayOf(
            "GPU",
            "Temp",
            "Memory",
            "FLOPS"
        )

        val colors = intArrayOf(
            getColorForPercentage(gpuUsage),
            getColorForPercentage(temperature),
            getColorForPercentage(memoryUsage),
            getColorForPercentage(flopsUsage, true)  // FLOPS는 높을수록 좋음
        )

        setData(values, labels, colors)
    }

    fun setGpuServerMetrics(
        cpuUsage: Float,
        gpuUsage: Float,
        memoryUsage: Float,
        diskUsage: Float,
        flopsUsage: Float
    ) {
        val values = floatArrayOf(
            cpuUsage,
            gpuUsage,
            memoryUsage,
            diskUsage,
            flopsUsage
        )

        val labels = arrayOf(
            "CPU",
            "GPU",
            "Memory",
            "Disk",
            "FLOPS"
        )

        val colors = intArrayOf(
            getColorForPercentage(cpuUsage),
            getColorForPercentage(gpuUsage),
            getColorForPercentage(memoryUsage),
            getColorForPercentage(diskUsage),
            getColorForPercentage(flopsUsage, true)  // FLOPS는 높을수록 좋음
        )

        setData(values, labels, colors)
    }

    // Return color based on percentage (lower is better)
    private fun getColorForPercentage(percentage: Float, inverse: Boolean = false): Int {
        val adjustedPercentage = if (inverse) 100 - percentage else percentage

        return when {
            adjustedPercentage >= 80 -> Color.parseColor("#F44336")  // Red (danger)
            adjustedPercentage >= 60 -> Color.parseColor("#FF9800")  // Orange (warning)
            else -> Color.parseColor("#4CAF50")                      // Green (normal)
        }
    }
}