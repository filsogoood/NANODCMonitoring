package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * Advanced NDP Price Performance Chart
 * Shows price trends, volume, and market indicators
 */
class NDPPriceChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rectF = RectF()

    // Sample price data (7 days)
    private val priceData = floatArrayOf(
        0.218f, 0.225f, 0.232f, 0.228f, 0.238f, 0.242f, 0.245f
    )
    
    private val volumeData = floatArrayOf(
        125000f, 148000f, 163000f, 142000f, 178000f, 195000f, 210000f
    )
    
    private val dayLabels = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    private var isNarrowScreen = false
    private var chartRect = RectF()

    init {
        textPaint.color = Color.WHITE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT
        
        checkScreenWidth()
    }

    private fun checkScreenWidth() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        isNarrowScreen = screenWidth < (400 * displayMetrics.density)
        
        textPaint.textSize = if (isNarrowScreen) 10f else 12f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        val padding = if (isNarrowScreen) 20f else 30f
        chartRect.set(padding, padding, w.toFloat() - padding, h.toFloat() - padding)
        
        checkScreenWidth()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        drawPriceChart(canvas)
        drawVolumeChart(canvas)
        drawGridLines(canvas)
        drawLabels(canvas)
        drawPriceInfo(canvas)
    }

    private fun drawGridLines(canvas: Canvas) {
        paint.color = Color.parseColor("#33FFFFFF")
        paint.strokeWidth = 1f
        paint.pathEffect = DashPathEffect(floatArrayOf(5f, 5f), 0f)
        
        // Horizontal grid lines
        for (i in 1..4) {
            val y = chartRect.top + (chartRect.height() / 5) * i
            canvas.drawLine(chartRect.left, y, chartRect.right, y, paint)
        }
        
        // Vertical grid lines
        for (i in 1..6) {
            val x = chartRect.left + (chartRect.width() / 7) * i
            canvas.drawLine(x, chartRect.top, x, chartRect.bottom, paint)
        }
        
        paint.pathEffect = null
    }

    private fun drawPriceChart(canvas: Canvas) {
        if (priceData.isEmpty()) return

        val maxPrice = priceData.maxOrNull() ?: 1f
        val minPrice = priceData.minOrNull() ?: 0f
        val priceRange = maxPrice - minPrice
        val chartHeight = chartRect.height() * 0.6f // Use 60% for price chart

        // Create gradient fill area
        path.reset()
        val segmentWidth = chartRect.width() / (priceData.size - 1)
        
        // Start from bottom
        path.moveTo(chartRect.left, chartRect.top + chartHeight)
        
        // Price line points
        for (i in priceData.indices) {
            val x = chartRect.left + i * segmentWidth
            val normalizedPrice = (priceData[i] - minPrice) / priceRange
            val y = chartRect.top + chartHeight - (normalizedPrice * chartHeight)
            
            if (i == 0) {
                path.lineTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        // Close path for fill
        path.lineTo(chartRect.right, chartRect.top + chartHeight)
        path.close()

        // Fill gradient
        val gradient = LinearGradient(
            0f, chartRect.top,
            0f, chartRect.top + chartHeight,
            Color.parseColor("#6600D4FF"),
            Color.parseColor("#1100D4FF"),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)

        // Draw price line
        paint.shader = null
        paint.color = Color.parseColor("#00D4FF")
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = if (isNarrowScreen) 3f else 4f
        
        path.reset()
        for (i in priceData.indices) {
            val x = chartRect.left + i * segmentWidth
            val normalizedPrice = (priceData[i] - minPrice) / priceRange
            val y = chartRect.top + chartHeight - (normalizedPrice * chartHeight)
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, paint)

        // Draw price points
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE
        for (i in priceData.indices) {
            val x = chartRect.left + i * segmentWidth
            val normalizedPrice = (priceData[i] - minPrice) / priceRange
            val y = chartRect.top + chartHeight - (normalizedPrice * chartHeight)
            
            canvas.drawCircle(x, y, if (isNarrowScreen) 4f else 6f, paint)
            
            // Inner point
            paint.color = Color.parseColor("#00D4FF")
            canvas.drawCircle(x, y, if (isNarrowScreen) 2f else 3f, paint)
            paint.color = Color.WHITE
        }
    }

    private fun drawVolumeChart(canvas: Canvas) {
        if (volumeData.isEmpty()) return

        val maxVolume = volumeData.maxOrNull() ?: 1f
        val volumeChartTop = chartRect.top + chartRect.height() * 0.65f
        val volumeChartHeight = chartRect.height() * 0.3f
        val barWidth = chartRect.width() / volumeData.size * 0.6f

        paint.style = Paint.Style.FILL
        
        for (i in volumeData.indices) {
            val x = chartRect.left + (i + 0.5f) * (chartRect.width() / volumeData.size)
            val normalizedVolume = volumeData[i] / maxVolume
            val barHeight = normalizedVolume * volumeChartHeight
            
            // Gradient for volume bars
            val barGradient = LinearGradient(
                0f, volumeChartTop,
                0f, volumeChartTop + barHeight,
                Color.parseColor("#4CAF50"),
                Color.parseColor("#81C784"),
                Shader.TileMode.CLAMP
            )
            paint.shader = barGradient
            
            canvas.drawRoundRect(
                x - barWidth/2,
                volumeChartTop + volumeChartHeight - barHeight,
                x + barWidth/2,
                volumeChartTop + volumeChartHeight,
                8f, 8f,
                paint
            )
        }
        
        paint.shader = null
    }

    private fun drawLabels(canvas: Canvas) {
        // Day labels
        textPaint.color = Color.parseColor("#B0BEC5")
        textPaint.textSize = if (isNarrowScreen) 10f else 12f
        
        for (i in dayLabels.indices) {
            val x = chartRect.left + (i + 0.5f) * (chartRect.width() / dayLabels.size)
            canvas.drawText(dayLabels[i], x, chartRect.bottom + 20f, textPaint)
        }

        // Price labels (left side)
        val maxPrice = priceData.maxOrNull() ?: 1f
        val minPrice = priceData.minOrNull() ?: 0f
        val priceStep = (maxPrice - minPrice) / 4

        for (i in 0..4) {
            val price = minPrice + (priceStep * i)
            val y = chartRect.bottom - (chartRect.height() * 0.6f * i / 4)
            canvas.drawText(
                "₩${String.format("%.3f", price)}",
                chartRect.left - 10f,
                y + 5f,
                textPaint
            )
        }
    }

    private fun drawPriceInfo(canvas: Canvas) {
        // Current price and change
        val currentPrice = priceData.lastOrNull() ?: 0f
        val previousPrice = if (priceData.size > 1) priceData[priceData.size - 2] else currentPrice
        val change = currentPrice - previousPrice
        val changePercent = if (previousPrice > 0) (change / previousPrice) * 100 else 0f

        // Background for price info
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#B3000000")
        canvas.drawRoundRect(
            chartRect.left + 10f,
            chartRect.top + 10f,
            chartRect.left + (if (isNarrowScreen) 140f else 180f),
            chartRect.top + (if (isNarrowScreen) 70f else 80f),
            12f, 12f,
            paint
        )

        // Price text
        textPaint.color = Color.WHITE
        textPaint.textSize = if (isNarrowScreen) 14f else 16f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        textPaint.textAlign = Paint.Align.LEFT
        
        canvas.drawText(
            "₩${String.format("%.3f", currentPrice)}",
            chartRect.left + 20f,
            chartRect.top + 35f,
            textPaint
        )

        // Change text
        textPaint.color = if (change >= 0) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        textPaint.textSize = if (isNarrowScreen) 12f else 14f
        
        val changeText = "${if (change >= 0) "+" else ""}${String.format("%.3f", change)} (${if (changePercent >= 0) "+" else ""}${String.format("%.1f", changePercent)}%)"
        canvas.drawText(
            changeText,
            chartRect.left + 20f,
            chartRect.top + 55f,
            textPaint
        )
    }
}