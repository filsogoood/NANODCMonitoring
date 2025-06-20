package com.nanodatacenter.monitorwebview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

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
    private var animatedPercentage = 0f // 애니메이션용 변수
    private var animator: ValueAnimator? = null
    
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
    
    fun setVramUsage(used: Float, total: Float, animationDelay: Long = 0) {
        vramUsed = used
        vramTotal = total
        val targetPercentage = (used / total * 100).coerceIn(0f, 100f)
        
        // 초기값 설정
        percentage = 0f
        animatedPercentage = 0f
        invalidate()
        
        // 애니메이션 시작
        postDelayed({
            startAnimation(targetPercentage)
        }, animationDelay)
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
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 뷰가 화면에서 제거될 때 애니메이션 정리
        animator?.cancel()
        animator = null
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 뷰가 화면에 다시 표시될 때 애니메이션 재시작
        restartAnimationIfNeeded()
    }
    
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) {
            restartAnimationIfNeeded()
        }
    }
    
    fun restartAnimationIfNeeded() {
        if (vramUsed > 0 && vramTotal > 0) {
            val targetPercentage = (vramUsed / vramTotal * 100).coerceIn(0f, 100f)
            percentage = 0f
            animatedPercentage = 0f
            invalidate()
            
            postDelayed({
                startAnimation(targetPercentage)
            }, 100)
        }
    }
    
    private fun startAnimation(targetPercentage: Float) {
        animator?.cancel()
        
        animator = ValueAnimator.ofFloat(0f, targetPercentage).apply {
            duration = 1500
            startDelay = 0
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                animatedPercentage = animation.animatedValue as Float
                percentage = animatedPercentage
                invalidate()
            }
            start()
        }
    }
} 