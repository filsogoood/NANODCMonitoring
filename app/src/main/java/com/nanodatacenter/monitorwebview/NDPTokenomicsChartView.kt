package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * Advanced NDP Tokenomics Chart View
 * Displays sophisticated token distribution and economic metrics
 */
class NDPTokenomicsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()
    private val path = Path()

    // Tokenomics data - Staking, Rewards 비중만
    private val tokenDistribution = floatArrayOf(
        68.7f,  // Staking (856,432 / (856,432 + 389,246) * 100)
        31.3f   // Rewards (389,246 / (856,432 + 389,246) * 100)
    )

    private val distributionLabels = arrayOf(
        "Staking",
        "Rewards"
    )

    private val distributionColors = intArrayOf(
        Color.parseColor("#4CAF50"), // Green - Staking
        Color.parseColor("#FF9800")  // Orange - Rewards
    )

    // Screen adaptation variables
    private var isNarrowScreen = false
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    init {
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
        
        checkScreenWidth()
    }

    private fun checkScreenWidth() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        
        textPaint.textSize = if (isNarrowScreen) 11f else 14f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        centerX = w / 2f
        // 반원형 차트이므로 centerY를 조정하여 공백 줄임
        centerY = h / 2f - 20f
        radius = Math.min(w, h) / 2f * 0.7f
        
        checkScreenWidth()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        drawTokenomicsChart(canvas)
        drawCenterInfo(canvas)
    }

    private fun drawTokenomicsChart(canvas: Canvas) {
        val strokeWidth = radius * 0.25f
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        
        // Set chart area
        rectF.set(
            centerX - radius + strokeWidth/2,
            centerY - radius + strokeWidth/2,
            centerX + radius - strokeWidth/2,
            centerY + radius - strokeWidth/2
        )

        var startAngle = 180f // Start from left side for semicircle
        
        for (i in tokenDistribution.indices) {
            // Calculate sweep angle for semicircle (180 degrees total)
            val sweepAngle = (tokenDistribution[i] / 100f) * 180f
            
            // Create gradient effect
            paint.color = distributionColors[i]
            
            // Draw arc with shadow effect
            val shadowPaint = Paint(paint).apply {
                color = Color.parseColor("#33000000")
                maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.OUTER)
            }

            canvas.drawArc(rectF, startAngle + 1, sweepAngle, false, shadowPaint)
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
            
            startAngle += sweepAngle
        }
    }

    private fun drawCenterInfo(canvas: Canvas) {
        // Center semicircle background - only upper half
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#0D2C54")
        val centerRadius = radius * 0.45f

        // Draw semicircle path
        path.reset()
        path.addArc(
            centerX - centerRadius,
            centerY - centerRadius,
            centerX + centerRadius,
            centerY + centerRadius,
            180f,
            180f
        )
        canvas.drawPath(path, paint)

        // Center semicircle border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.parseColor("#00D4FF")
        canvas.drawArc(
            centerX - centerRadius,
            centerY - centerRadius,
            centerX + centerRadius,
            centerY + centerRadius,
            180f,
            180f,
            false,
            paint
        )

        // Draw base line for semicircle
        canvas.drawLine(
            centerX - centerRadius,
            centerY,
            centerX + centerRadius,
            centerY,
            paint
        )

        // Center text - position adjusted for semicircle
        val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = if (isNarrowScreen) 12f else 16f
        }

        canvas.drawText("NDP", centerX, centerY - 15f, centerTextPaint)
    }

    // private fun drawLegend(canvas: Canvas) {
    //     val legendStartY = centerY + 40f // Reduced gap for semicircle
    //     val legendItemHeight = if (isNarrowScreen) 25f else 28f
    //     val itemsPerColumn = 2 // Split into two columns for better layout

    //     // Actual token amounts
    //     val tokenAmounts = arrayOf(
    //         "856,432 NDP",
    //         "389,246 NDP"
    //     )

    //     for (i in tokenDistribution.indices) {
    //         val column = i / itemsPerColumn
    //         val row = i % itemsPerColumn

    //         val x = centerX - radius + (column * (radius * 1.1f))
    //         val y = legendStartY + (row * legendItemHeight)

    //         // Legend color box
    //         paint.style = Paint.Style.FILL
    //         paint.color = distributionColors[i]
    //         canvas.drawRect(
    //             x,
    //             y - 8f,
    //             x + 16f,
    //             y + 8f,
    //             paint
    //         )

    //         // Legend text - label
    //         val legendTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    //             color = Color.WHITE
    //             textSize = if (isNarrowScreen) 11f else 13f
    //             textAlign = Paint.Align.LEFT
    //             typeface = Typeface.DEFAULT_BOLD
    //         }

    //         canvas.drawText(
    //             distributionLabels[i],
    //             x + 22f,
    //             y,
    //             legendTextPaint
    //         )

    //         // Legend text - amount
    //         legendTextPaint.apply {
    //             textSize = if (isNarrowScreen) 10f else 11f
    //             color = Color.parseColor("#B0BEC5")
    //             typeface = Typeface.DEFAULT
    //         }

    //         canvas.drawText(
    //             tokenAmounts[i],
    //             x + 22f,
    //             y + 12f,
    //             legendTextPaint
    //         )
    //     }
    // }
}
