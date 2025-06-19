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

    // Tokenomics data (from user's specification)
    private val tokenDistribution = floatArrayOf(
        40f,  // Ecosystem Rewards
        15f,  // Team
        15f,  // DAO Reserve
        10f,  // Community & Marketing
        10f,  // Strategic Partners
        5f,   // Foundation
        5f    // Initial DEX Liquidity
    )

    private val distributionLabels = arrayOf(
        "Ecosystem\nRewards",
        "Team",
        "DAO\nReserve", 
        "Community &\nMarketing",
        "Strategic\nPartners",
        "Foundation",
        "DEX\nLiquidity"
    )

    private val distributionColors = intArrayOf(
        Color.parseColor("#00D4FF"), // Cyan - Ecosystem Rewards
        Color.parseColor("#4CAF50"), // Green - Team
        Color.parseColor("#FF9800"), // Orange - DAO Reserve
        Color.parseColor("#9C27B0"), // Purple - Community
        Color.parseColor("#F44336"), // Red - Strategic Partners
        Color.parseColor("#3F51B5"), // Indigo - Foundation
        Color.parseColor("#FFEB3B")  // Yellow - DEX Liquidity
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
        centerY = h / 2f
        radius = Math.min(w, h) / 2f * 0.7f
        
        checkScreenWidth()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        drawTokenomicsChart(canvas)
        drawCenterInfo(canvas)
        drawLegend(canvas)
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

        var startAngle = -90f // Start from top
        
        for (i in tokenDistribution.indices) {
            val sweepAngle = (tokenDistribution[i] / 100f) * 360f
            
            // Create gradient effect
            paint.color = distributionColors[i]
            
            // Draw arc with shadow effect
            val shadowPaint = Paint(paint).apply {
                color = Color.parseColor("#33000000")
                maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.OUTER)
            }
            
            canvas.drawArc(rectF, startAngle + 2, sweepAngle, false, shadowPaint)
            canvas.drawArc(rectF, startAngle, sweepAngle, false, paint)
            
            startAngle += sweepAngle
        }
    }

    private fun drawCenterInfo(canvas: Canvas) {
        // Center circle background
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#0D2C54")
        val centerRadius = radius * 0.45f
        canvas.drawCircle(centerX, centerY, centerRadius, paint)
        
        // Center circle border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = Color.parseColor("#00D4FF")
        canvas.drawCircle(centerX, centerY, centerRadius, paint)
        
        // Center text
        val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            textSize = if (isNarrowScreen) 16f else 20f
        }
        
        canvas.drawText("NDP", centerX, centerY - 10f, centerTextPaint)
        
        centerTextPaint.textSize = if (isNarrowScreen) 12f else 14f
        centerTextPaint.color = Color.parseColor("#00D4FF")
        canvas.drawText("2B Total Supply", centerX, centerY + 15f, centerTextPaint)
    }

    private fun drawLegend(canvas: Canvas) {
        val legendStartY = centerY + radius + 30f
        val legendItemHeight = if (isNarrowScreen) 25f else 30f
        
        for (i in tokenDistribution.indices) {
            val y = legendStartY + (i * legendItemHeight)
            
            // Legend color box
            paint.style = Paint.Style.FILL
            paint.color = distributionColors[i]
            canvas.drawRect(
                centerX - radius,
                y - 8f,
                centerX - radius + 16f,
                y + 8f,
                paint
            )
            
            // Legend text
            val legendTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = if (isNarrowScreen) 12f else 14f
                textAlign = Paint.Align.LEFT
            }
            
            val labelText = "${distributionLabels[i]}: ${tokenDistribution[i]}%"
            canvas.drawText(
                labelText,
                centerX - radius + 25f,
                y + 5f,
                legendTextPaint
            )
        }
    }
}