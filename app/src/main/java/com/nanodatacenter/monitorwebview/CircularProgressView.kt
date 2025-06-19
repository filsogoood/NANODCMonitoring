package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.ValueAnimator

/**
 * Custom view to display circular progress bar
 */
class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    // Circle colors
    private var progressColor = 0xFF4CAF50.toInt()  // Default green
    private var backgroundColor = 0x223C3E3F.toInt()  // Semi-transparent gray

    // Progress values
    private var maxProgress = 100
    private var currentProgress = 0
    private var animatedProgress = 0f

    // Text related
    private var showText = true
    private var progressText = "0%"
    private var labelText = ""

    // Animation related
    private var progressAnimator: ValueAnimator? = null

    // 화면 적응형 레이아웃을 위한 변수
    private var padding = 15f // 원 주변 패딩

    init {
        // Background circle setup - modified: increased thickness
        backgroundPaint.color = backgroundColor
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = 20f // Increased thickness

        // Progress circle setup - modified: increased thickness
        paint.color = progressColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 20f // Increased thickness
        paint.strokeCap = Paint.Cap.ROUND

        // Text setup - modified: increased font size
        textPaint.color = 0xFFFFFFFF.toInt()  // White
        textPaint.textSize = 50f // Increased font size
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    fun setProgress(progress: Int, animate: Boolean = true) {
        this.currentProgress = progress.coerceIn(0, maxProgress)
        updateProgressText()

        if (animate) {
            startProgressAnimation()
        } else {
            animatedProgress = currentProgress.toFloat()
            invalidate()
        }
    }

    fun setProgressColor(color: Int) {
        this.progressColor = color
        paint.color = color
        invalidate()
    }

    fun setLabel(text: String) {
        this.labelText = text
        invalidate()
    }

    fun setVramUsage(usedVram: Float, totalVram: Float) {
        val percentage = ((usedVram / totalVram) * 100).toInt().coerceIn(0, 100)

        val color = when {
            percentage >= 90 -> 0xFFE91E63.toInt()  // 핑크 (매우 높음)
            percentage >= 75 -> 0xFFFF9800.toInt()  // Orange (warning)
            else -> 0xFF9C27B0.toInt()              // Purple (normal)
        }

        setLabel("VRAM")
        setProgressColor(color)
        setProgress(percentage)
    }

    private fun updateProgressText() {
        progressText = "${(currentProgress * 100 / maxProgress)}%"
    }

    private fun startProgressAnimation() {
        progressAnimator?.cancel()

        progressAnimator = ValueAnimator.ofFloat(animatedProgress, currentProgress.toFloat()).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                animatedProgress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
    }



    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 화면 크기에 따라 패딩 조절
        val displayMetrics = context.resources.displayMetrics
        val isNarrowScreen = w < (200 * displayMetrics.density)

        // 좁은 화면에서는 더 작은 패딩 사용
        padding = if (isNarrowScreen) 8f else 12f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 화면 너비 체크
        val displayMetrics = context.resources.displayMetrics
        val viewWidth = width
        val viewHeight = height
        val isNarrowView = viewWidth < (200 * displayMetrics.density)
        val isVeryNarrowView = viewWidth < (150 * displayMetrics.density)

        val centerX = width / 2f
        val centerY = height / 2f

        // 원 크기 조정
        val scale = when {
            isVeryNarrowView -> 0.45f
            isNarrowView -> 0.55f
            else -> 0.65f
        }

        // 원의 실제 크기 계산
        val availableSize = Math.min(width, height)
        val actualRadius = (availableSize / 2) * scale

        // 원이 중앙에 위치하도록 설정 - 디스크는 약간 위로 이동
        val yOffset = if (labelText == "Disk") {
            -actualRadius * 0.1f
        } else if (isNarrowView) {
            -actualRadius * 0.05f
        } else {
            0f
        }

        // 원 경계 설정
        rect.set(
            centerX - actualRadius,
            centerY - actualRadius + yOffset,
            centerX + actualRadius,
            centerY + actualRadius + yOffset
        )

        // 배경 원 그리기
        backgroundPaint.strokeWidth = actualRadius * 0.11f
        canvas.drawArc(rect, 0f, 360f, false, backgroundPaint)

        // 진행 원 그리기
        paint.strokeWidth = actualRadius * 0.11f
        val sweepAngle = (animatedProgress / maxProgress) * 360
        canvas.drawArc(rect, -90f, sweepAngle, false, paint)

        // 텍스트 그리기 - Storage 라벨일 때 더 큰 텍스트 사용
        if (showText) {
            // Storage 라벨일 때 더 큰 퍼센트 텍스트 표시
            val percentTextSize = if (labelText == "Storage") {
                actualRadius * 0.6f // Storage일 때 더 큰 텍스트
            } else {
                actualRadius * 0.45f // 기본 크기
            }
            
            textPaint.textSize = percentTextSize
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                progressText,
                centerX,
                centerY + textPaint.textSize / 3 + yOffset,
                textPaint
            )

            // 라벨 텍스트 표시 - Storage일 때 더 위쪽에 표시
            if (labelText.isNotEmpty()) {
                textPaint.textSize = if (labelText == "Storage") {
                    actualRadius * 0.25f // Storage 라벨 텍스트 크기
                } else {
                    actualRadius * 0.35f // 기본 라벨 텍스트 크기
                }

                // Storage는 더 위쪽에 표시
                val labelY = if (labelText == "Storage") {
                    rect.top - textPaint.textSize * 0.5f
                } else {
                    rect.top - textPaint.textSize * 0.3f
                }

                canvas.drawText(
                    labelText,
                    centerX,
                    labelY,
                    textPaint
                )
            }
        }
    }

    // Set progress by status
    fun setProgressByStatus(value: Int, maxValue: Int = 100) {
        val percentage = (value * 100 / maxValue).coerceIn(0, 100)

        // Change color based on status
        val color = when {
            percentage >= 80 -> 0xFFF44336.toInt()  // Red (danger)
            percentage >= 60 -> 0xFFFF9800.toInt()  // Orange (warning)
            else -> 0xFF4CAF50.toInt()              // Green (normal)
        }

        setProgressColor(color)
        setProgress(percentage)
    }

    fun setCpuUsage(cpuPercentage: Int) {
        val color = when {
            cpuPercentage >= 80 -> 0xFFF44336.toInt()  // Red (danger)
            cpuPercentage >= 60 -> 0xFFFF9800.toInt()  // Orange (warning)
            else -> 0xFF4CAF50.toInt()                 // Green (normal)
        }

        setLabel("CPU")
        setProgressColor(color)
        setProgress(cpuPercentage)
    }

    // Set memory usage
    fun setMemoryUsage(usedMemory: Float, totalMemory: Float) {
        val percentage = ((usedMemory / totalMemory) * 100).toInt().coerceIn(0, 100)

        val color = when {
            percentage >= 90 -> 0xFFF44336.toInt()  // Red (danger)
            percentage >= 75 -> 0xFFFF9800.toInt()  // Orange (warning)
            else -> 0xFF2196F3.toInt()              // Blue (normal)
        }

        setLabel("Memory")
        setProgressColor(color)
        setProgress(percentage)
    }

    // Set disk usage
    fun setDiskUsage(usedSpace: Float, totalSpace: Float) {
        val percentage = ((usedSpace / totalSpace) * 100).toInt().coerceIn(0, 100)

        // Storage 차트를 위한 보라색/핑크색 계열 색상 사용
        val color = when {
            percentage >= 90 -> 0xFFE91E63.toInt()  // 핑크 (매우 높음)
            percentage >= 60 -> 0xFF9C27B0.toInt()  // 보라색 (높음)
            else -> 0xFF673AB7.toInt()              // 진보라색 (정상)
        }

        setLabel("Disk")
        setProgressColor(color)
        setProgress(percentage)
    }

    // Set temperature
    fun setTemperature(temperature: Float, maxNormal: Float = 70f) {
        val percentage = ((temperature / maxNormal) * 100).toInt().coerceIn(0, 100)

        val color = when {
            temperature >= maxNormal -> 0xFFF44336.toInt()                // Red (danger)
            temperature >= (maxNormal * 0.8) -> 0xFFFF9800.toInt()        // Orange (warning)
            else -> 0xFF009688.toInt()                                   // Teal (normal)
        }

        setLabel("Temp")
        setProgressColor(color)
        setProgress(percentage)
    }
}