package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

/**
 * VRAM 사용률을 표시하는 단순한 반원형 차트
 */
class VramGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    
    private var vramUsed = 0f
    private var vramTotal = 24f // 기본값 24GB (RTX 3090)
    private var percentage = 0f
    
    // 색상 정의
    private val greenColor = Color.parseColor("#4CAF50")
    private val yellowColor = Color.parseColor("#FFC107")
    private val orangeColor = Color.parseColor("#FF9800")
    private val redColor = Color.parseColor("#F44336")
    private val backgroundGray = Color.parseColor("#333333")
    
    init {
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }
    
    fun setVramUsage(used: Float, total: Float) {
        vramUsed = used
        vramTotal = total
        percentage = (used / total * 100).coerceIn(0f, 100f)
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height * 0.9f // 반원이므로 중심을 아래쪽으로
        val radius = minOf(width, height) * 0.4f
        
        // 바깥쪽 호의 굵기
        val strokeWidth = radius * 0.25f
        paint.strokeWidth = strokeWidth
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        
        // 배경 호 그리기 (회색)
        paint.color = backgroundGray
        canvas.drawArc(rect, 180f, 180f, false, paint)
        
        // 사용량 호 그리기 (색상은 퍼센트에 따라 변경)
        paint.color = getColorForPercentage(percentage)
        val sweepAngle = percentage * 1.8f // 180도를 100%로 계산
        canvas.drawArc(rect, 180f, sweepAngle, false, paint)
        
        // 가운데 퍼센트 텍스트
        textPaint.textSize = radius * 0.5f
        textPaint.color = getColorForPercentage(percentage)
        
        // 퍼센트 표시
        canvas.drawText(
            "${percentage.toInt()}%",
            centerX,
            centerY - radius * 0.1f,
            textPaint
        )
    }
    
    private fun getColorForPercentage(percentage: Float): Int {
        return when {
            percentage >= 85 -> redColor
            percentage >= 70 -> orangeColor
            percentage >= 50 -> yellowColor
            else -> greenColor
        }
    }
} 