package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

/**
 * Storage capacity를 보여주는 막대 그래프 뷰
 */
class StorageBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    private var usedCapacity = 0f
    private var totalCapacity = 0f
    private var animatedUsed = 0f
    private var labelText = ""

    init {
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    fun setStorageData(used: Float, total: Float, label: String = "Storage") {
        usedCapacity = used
        totalCapacity = total
        labelText = label
        
        // 애니메이션 효과
        val animator = android.animation.ValueAnimator.ofFloat(0f, used)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            animatedUsed = animation.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (totalCapacity <= 0) return

        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()
        val padding = 40f

        // 레이블 텍스트 그리기
        textPaint.textSize = 24f
        textPaint.color = Color.WHITE
        canvas.drawText(labelText, width / 2, padding, textPaint)

        // 막대 그래프 영역 계산
        val barHeight = 40f  // 온도 막대와 동일한 두께로 조정
        val barTop = height / 2 - barHeight / 2
        val barBottom = barTop + barHeight
        val barLeft = padding
        val barRight = width - padding

        // 배경 막대 (전체 용량)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#2C3E50")
        rectF.set(barLeft, barTop, barRight, barBottom)
        canvas.drawRoundRect(rectF, 20f, 20f, paint)

        // 사용된 용량 막대
        val usedWidth = (animatedUsed / totalCapacity) * (barRight - barLeft)
        paint.color = when {
            animatedUsed / totalCapacity > 0.9f -> Color.parseColor("#E74C3C") // 90% 이상 빨간색
            animatedUsed / totalCapacity > 0.8f -> Color.parseColor("#F39C12") // 80% 이상 주황색
            animatedUsed / totalCapacity > 0.7f -> Color.parseColor("#F1C40F") // 70% 이상 노란색
            else -> Color.parseColor("#27AE60") // 70% 이하 초록색
        }
        rectF.set(barLeft, barTop, barLeft + usedWidth, barBottom)
        canvas.drawRoundRect(rectF, 20f, 20f, paint)

        // 퍼센트 텍스트
        val percentage = (animatedUsed / totalCapacity * 100).toInt()
        textPaint.textSize = 18f
        textPaint.color = Color.WHITE
        canvas.drawText("${percentage}%", width / 2, barTop + barHeight / 2 + 6f, textPaint)

        // 여유 공간 텍스트만 표시
        val freeSpace = totalCapacity - animatedUsed
        val freeText = "Free: ${String.format("%.1f", freeSpace)} TiB"
        textPaint.textSize = 14f
        textPaint.color = Color.parseColor("#27AE60")
        canvas.drawText(freeText, width / 2, barBottom + 30f, textPaint)

        // 구분선
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