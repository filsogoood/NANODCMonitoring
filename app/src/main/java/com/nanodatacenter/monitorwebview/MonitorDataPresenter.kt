package com.nanodatacenter.monitorwebview

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import kotlin.random.Random

/**
 * Utility class for displaying monitoring data
 * Uses material design elements to display monitoring information
 */
class MonitorDataPresenter(private val context: Context) {

    /**
     * Icons and colors based on server status
     */
    enum class ServerStatus(val iconRes: Int, val color: Int) {
        NORMAL(R.drawable.rack_info, Color.parseColor("#4CAF50")),    // Normal (green)
        WARNING(R.drawable.reboot, Color.parseColor("#FFC107")),      // Warning (yellow)
        ERROR(R.drawable.shut_down, Color.parseColor("#F44336"))      // Error (red)
    }

    /**
     * Create monitoring card
     */
    fun createMonitoringCard(
        title: String,
        data: String,
        serverType: String,
        status: ServerStatus = ServerStatus.NORMAL
    ): CardView {
        // 화면 너비 확인
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        // Create card view - 좁은 화면에서는 여백 줄임
        val cardView = CardView(context).apply {
            radius = 12f
            cardElevation = 8f
            setCardBackgroundColor(Color.parseColor("#162B46"))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(
                if (isNarrowScreen) 8 else 16,
                if (isNarrowScreen) 4 else 8,
                if (isNarrowScreen) 8 else 16,
                if (isNarrowScreen) 4 else 8
            )
            layoutParams = params
        }

        // Inner layout - 좁은 화면에서는 패딩 줄임
        val innerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(
                if (isNarrowScreen) 16 else 24,
                if (isNarrowScreen) 16 else 24,
                if (isNarrowScreen) 16 else 24,
                if (isNarrowScreen) 12 else 16
            )
        }

        // Header layout
        val headerLayout = createHeaderLayout(title, status, isNarrowScreen)

        // Display server type - 좁은 화면에서는 텍스트 크기 줄임
        val serverTypeView = TextView(context).apply {
            text = serverType
            textSize = if (isNarrowScreen) 10f else 12f
            setTextColor(Color.parseColor("#BBDEFB"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 0, 8, 8)
        }

        // Divider
        val divider = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(Color.parseColor("#33FFFFFF"))
            setPadding(0, 8, 0, 8)
        }

        // Data container
        val dataContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 16, 0, 0)
        }

        // Create data text view
        val dataView = createFormattedDataView(data, isNarrowScreen)

        // Real-time update text view - 좁은 화면에서는 텍스트 크기 줄임
        val liveUpdateView = TextView(context).apply {
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(status.color)
            setPadding(8, 4, 8, 4)
        }

        // Layout composition
        innerLayout.addView(headerLayout)
        innerLayout.addView(serverTypeView)
        innerLayout.addView(divider)
        dataContainer.addView(dataView)
        dataContainer.addView(liveUpdateView)
        innerLayout.addView(dataContainer)
        cardView.addView(innerLayout)

        // Real-time update simulation
        setupLiveUpdates(liveUpdateView, serverType, isNarrowScreen)

        return cardView
    }

    /**
     * Create header layout - 화면 크기에 맞게 조정
     */
    private fun createHeaderLayout(
        title: String,
        status: ServerStatus,
        isNarrowScreen: Boolean
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // Status icon - 좁은 화면에서는 아이콘 크기 줄임
            val statusIcon = ImageView(context).apply {
                setImageResource(status.iconRes)
                layoutParams = LinearLayout.LayoutParams(
                    if (isNarrowScreen) 36 else 48,
                    if (isNarrowScreen) 36 else 48
                )
            }

            // Title text - 좁은 화면에서는 텍스트 크기 줄임
            val titleText = TextView(context).apply {
                text = title
                textSize = if (isNarrowScreen) 16f else 18f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    marginStart = if (isNarrowScreen) 8 else 16
                }
            }

            // Status indicator - 좁은 화면에서는 크기 줄임
            val statusIndicator = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    if (isNarrowScreen) 8 else 12,
                    if (isNarrowScreen) 8 else 12
                ).apply {
                    gravity = Gravity.CENTER_VERTICAL
                    marginStart = 8
                }
                background = context.getDrawable(R.drawable.rack_info)?.apply {
                    setTint(status.color)
                }
            }

            addView(statusIcon)
            addView(titleText)
            addView(statusIndicator)
        }
    }

    /**
     * Create data text view - 화면 크기에 맞게 조정
     */
    private fun createFormattedDataView(data: String, isNarrowScreen: Boolean): TextView {
        return TextView(context).apply {
            textSize = if (isNarrowScreen) 12f else 14f
            setTextColor(Color.parseColor("#E0E0E0"))
            setPadding(8, 4, 8, 16)

            // Data formatting
            val lines = data.split("\n")
            val formattedText = StringBuilder()

            for (line in lines) {
                if (line.startsWith("#")) {
                    // Change style for title rows
                    val cleanLine = line.substring(1).trim()
                    formattedText.append("• $cleanLine\n")
                } else {
                    formattedText.append("  $line\n")
                }
            }

            text = formattedText.toString()
        }
    }

    /**
     * Setup real-time updates - 화면 크기에 맞게 조정
     */
    private fun setupLiveUpdates(
        textView: TextView,
        serverType: String,
        isNarrowScreen: Boolean
    ) {
        val updateHandler = android.os.Handler()
        updateHandler.post(object : Runnable {
            override fun run() {
                // 좁은 화면에서는 짧은 형식 사용
                when {
                    serverType.contains("Storage") -> {
                        val temperature = Random.nextInt(37, 48)
                        val ioRate = Random.nextInt(100, 600)
                        textView.text = if (isNarrowScreen) {
                            "T: ${temperature}°C | I/O: ${ioRate}MB/s"
                        } else {
                            "Temp: ${temperature}°C | I/O: ${ioRate}MB/s"
                        }
                    }
                    serverType.contains("UPS") -> {
                        val battery = Random.nextInt(90, 100)
                        val load = Random.nextInt(50, 85)
                        textView.text = if (isNarrowScreen) {
                            "Bat: ${battery}% | Load: ${load}%"
                        } else {
                            "Battery: ${battery}% | Load: ${load}%"
                        }
                    }
                    serverType.contains("Network") -> {
                        val traffic = Random.nextInt(100, 900)
                        val packets = Random.nextInt(1000, 5000)
                        textView.text = if (isNarrowScreen) {
                            "Trf: ${traffic}Mbps | Pkt: ${packets}/s"
                        } else {
                            "Traffic: ${traffic}Mbps | Packets: ${packets}/s"
                        }
                    }
                    else -> {
                        val cpu = Random.nextInt(30, 95)
                        val memory = Random.nextInt(6, 14)
                        textView.text = if (isNarrowScreen) {
                            "CPU: ${cpu}% | Mem: ${memory}GB/16GB"
                        } else {
                            "CPU: ${cpu}% | Memory: ${memory}GB / 16GB"
                        }
                    }
                }
                updateHandler.postDelayed(this, 3000)
            }
        })
    }
}