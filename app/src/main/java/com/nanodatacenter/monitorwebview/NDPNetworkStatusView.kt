package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * NDP DePIN Network Status Visualization
 * Shows network infrastructure, node connections, and resource utilization
 */
class NDPNetworkStatusView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    // Network status data
    private val networkMetrics = mapOf(
        "Active Nodes" to "2,847",
        "Total Resources" to "348.6 TB",
        "Network Uptime" to "99.7%",
        "Connected Partners" to "4 Regions"
    )

    private val resourceTypes = arrayOf(
        "CPU/GPU", "Storage", "Network", "Memory"
    )
    
    private val resourceUtilization = floatArrayOf(
        78.5f, 92.3f, 67.8f, 84.2f
    )

    private val resourceColors = intArrayOf(
        Color.parseColor("#00D4FF"), // CPU/GPU - Cyan
        Color.parseColor("#4CAF50"), // Storage - Green  
        Color.parseColor("#FF9800"), // Network - Orange
        Color.parseColor("#9C27B0")  // Memory - Purple
    )

    private var isNarrowScreen = false
    private var centerX = 0f
    private var centerY = 0f

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
        
        textPaint.textSize = if (isNarrowScreen) 12f else 14f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        centerX = w / 2f
        centerY = h / 2f
        
        checkScreenWidth()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        drawNetworkVisualization(canvas)
        drawResourceUtilization(canvas)
        drawNetworkMetrics(canvas)
    }

    private fun drawNetworkVisualization(canvas: Canvas) {
        val networkRadius = if (isNarrowScreen) 80f else 100f
        val nodeRadius = if (isNarrowScreen) 6f else 8f
        
        // Central network hub
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#00D4FF")
        canvas.drawCircle(centerX, centerY - 30f, nodeRadius * 1.5f, paint)
        
        // Network connections
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.parseColor("#4400D4FF")
        
        val nodeCount = 8
        for (i in 0 until nodeCount) {
            val angle = (i * 360f / nodeCount) * Math.PI / 180
            val nodeX = centerX + cos(angle).toFloat() * networkRadius
            val nodeY = (centerY - 30f) + sin(angle).toFloat() * networkRadius
            
            // Connection line
            canvas.drawLine(centerX, centerY - 30f, nodeX, nodeY, paint)
            
            // Network node
            paint.style = Paint.Style.FILL
            paint.color = when (i % 4) {
                0 -> Color.parseColor("#4CAF50") // Active nodes
                1 -> Color.parseColor("#FF9800") // Processing nodes  
                2 -> Color.parseColor("#00D4FF") // Storage nodes
                else -> Color.parseColor("#9C27B0") // Compute nodes
            }
            canvas.drawCircle(nodeX, nodeY, nodeRadius, paint)
            
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            paint.color = Color.parseColor("#4400D4FF")
        }
        
        // Center label
        textPaint.color = Color.WHITE
        textPaint.textSize = if (isNarrowScreen) 10f else 12f
        canvas.drawText("DePIN", centerX, centerY - 25f, textPaint)
        canvas.drawText("Network", centerX, centerY - 10f, textPaint)
    }

    private fun drawResourceUtilization(canvas: Canvas) {
        val barStartY = centerY + 60f
        val barHeight = if (isNarrowScreen) 15f else 20f
        val barSpacing = if (isNarrowScreen) 25f else 30f
        val barMaxWidth = if (isNarrowScreen) 120f else 150f
        
        textPaint.textAlign = Paint.Align.LEFT
        val labelPaint = Paint(textPaint).apply {
            textSize = if (isNarrowScreen) 11f else 13f
            color = Color.parseColor("#B0BEC5")
        }
        
        val valuePaint = Paint(textPaint).apply {
            textSize = if (isNarrowScreen) 11f else 13f
            color = Color.WHITE
            textAlign = Paint.Align.RIGHT
        }

        for (i in resourceTypes.indices) {
            val y = barStartY + (i * barSpacing)
            val utilization = resourceUtilization[i]
            val barWidth = (utilization / 100f) * barMaxWidth
            
            // Background bar
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#33FFFFFF")
            canvas.drawRoundRect(
                centerX - barMaxWidth/2,
                y,
                centerX + barMaxWidth/2,
                y + barHeight,
                barHeight/2, barHeight/2,
                paint
            )
            
            // Utilization bar with gradient
            val gradient = LinearGradient(
                centerX - barMaxWidth/2, y,
                centerX - barMaxWidth/2 + barWidth, y,
                resourceColors[i],
                Color.parseColor("#80${Integer.toHexString(resourceColors[i]).substring(2)}"),
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRoundRect(
                centerX - barMaxWidth/2,
                y,
                centerX - barMaxWidth/2 + barWidth,
                y + barHeight,
                barHeight/2, barHeight/2,
                paint
            )
            paint.shader = null
            
            // Labels
            canvas.drawText(
                resourceTypes[i],
                centerX - barMaxWidth/2 - 10f,
                y + barHeight/2 + 5f,
                labelPaint
            )
            
            canvas.drawText(
                "${utilization.toInt()}%",
                centerX + barMaxWidth/2 + 10f,
                y + barHeight/2 + 5f,
                valuePaint
            )
        }
    }

    private fun drawNetworkMetrics(canvas: Canvas) {
        val metricsStartY = centerY + 180f
        val metricsSpacing = if (isNarrowScreen) 25f else 30f
        
        textPaint.textAlign = Paint.Align.CENTER
        val labelPaint = Paint(textPaint).apply {
            textSize = if (isNarrowScreen) 10f else 12f
            color = Color.parseColor("#B0BEC5")
        }
        
        val valuePaint = Paint(textPaint).apply {
            textSize = if (isNarrowScreen) 12f else 14f
            color = Color.parseColor("#00D4FF")
            typeface = Typeface.DEFAULT_BOLD
        }

        var index = 0
        networkMetrics.forEach { (label, value) ->
            val x = if (isNarrowScreen) {
                if (index % 2 == 0) centerX - 80f else centerX + 80f
            } else {
                centerX - 150f + (index * 100f)
            }
            val y = if (isNarrowScreen) {
                metricsStartY + (index / 2) * metricsSpacing
            } else {
                metricsStartY
            }
            
            // Metric background
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1A000000")
            canvas.drawRoundRect(
                x - 40f, y - 15f,
                x + 40f, y + 20f,
                10f, 10f,
                paint
            )
            
            // Value
            canvas.drawText(value, x, y, valuePaint)
            
            // Label
            canvas.drawText(label, x, y + 18f, labelPaint)
            
            index++
        }
    }
}