package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

/**
 * 온도를 보여주는 가로 막대 그래프 뷰
 */
class TemperatureGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    private var currentTemp = 23f // 기본 온도
    private var animatedTemp = 0f
    private val minTemp = 0f
    private val maxTemp = 100f
    private val midTemp = 50f // 중간 기준점

    init {
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    fun setTemperature(temperature: Float) {
        currentTemp = temperature
        
        // 애니메이션 효과
        val animator = android.animation.ValueAnimator.ofFloat(0f, temperature)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            animatedTemp = animation.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val padding = 40f

        // 레이블 텍스트 그리기
        textPaint.textSize = 24f
        textPaint.color = Color.WHITE
        canvas.drawText("Temperature", width / 2, padding, textPaint)

        // 막대 그래프 영역 계산 (StorageBarChartView와 동일)
        val barHeight = 40f
        val barTop = height / 2 - barHeight / 2
        val barBottom = barTop + barHeight
        val barLeft = padding
        val barRight = width - padding

        // 배경 막대 (전체 범위) - StorageBarChartView와 동일한 스타일
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#2C3E50")
        rectF.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(rectF, 20f, 20f, paint)

        // 현재 온도 막대 - StorageBarChartView와 동일한 스타일
        val tempProgress = (animatedTemp - minTemp) / (maxTemp - minTemp)
        val tempWidth = tempProgress * (barRight - barLeft)
        
        // 온도에 따른 색상 결정
        paint.color = when {
            animatedTemp >= 80f -> Color.parseColor("#E74C3C") // 빨간색 (고온)
            animatedTemp >= 60f -> Color.parseColor("#F39C12") // 주황색 (높은 온도)
            animatedTemp >= midTemp -> Color.parseColor("#F1C40F") // 노란색 (중간 온도)
            else -> Color.parseColor("#27AE60") // 초록색 (낮은 온도)
        }
        
        rectF.set(barLeft, barTop, barLeft + tempWidth, barBottom)
        canvas.drawRoundRect(rectF, 20f, 20f, paint)

        // 온도 값 텍스트 (막대 중앙에 표시)
        textPaint.textSize = 18f
        textPaint.color = Color.WHITE
        canvas.drawText("${animatedTemp.toInt()}°C", width / 2, barTop + barHeight / 2 + 6f, textPaint)

        // 온도 범위 텍스트
        textPaint.textSize = 14f
        textPaint.color = Color.parseColor("#BDC3C7")
        
        // 최소 온도 텍스트
        canvas.drawText("${minTemp.toInt()}°C", barLeft + 30f, barBottom + 30f, textPaint)
        
        // 최대 온도 텍스트
        canvas.drawText("${maxTemp.toInt()}°C", barRight - 30f, barBottom + 30f, textPaint)
        
        // 중간 온도 표시 (같은 Y 좌표로 정렬)
        textPaint.color = Color.parseColor("#F1C40F")
        canvas.drawText("${midTemp.toInt()}°C", width / 2, barBottom + 30f, textPaint)

        // 구분선 (StorageBarChartView와 동일한 스타일)
        paint.color = Color.parseColor("#34495E")
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        rectF.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(rectF, 20f, 20f, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 400
        val desiredHeight = 200

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
} 