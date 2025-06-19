package com.nanodatacenter.monitorwebview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

/**
 * 주간 리워드 차트를 표시하기 위한 커스텀 뷰
 * 리워드 데이터를 선 그래프로 시각화하고 요일 레이블과 값을 함께 표시
 */
@SuppressLint("NewApi")  // LinearGradient API 레벨 경고 무시
class WeeklyRewardsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val chartRect = RectF()
    private val path = Path()
    private val fillPath = Path()

    // 차트 데이터
    private var rewardValues = floatArrayOf()
    private var dayLabels = arrayOf<String>()
    private var maxValue = 16.0f // 최대 리워드 값
    private var minValue = 14.5f // 최소 리워드 값 (더 뚜렷한 변화를 보여주기 위함)

    // 화면 적응형 레이아웃용 변수
    private var isNarrowScreen = false
    private var pointRadius = 8f
    private var lineWidth = 5f

    // 색상 설정
    private val lineColor = Color.parseColor("#4CAF50") // 녹색 선
    private val pointColor = Color.parseColor("#FFFFFF") // 흰색 점
    private val textColor = Color.parseColor("#FFFFFF") // 흰색 텍스트
    private val fillStartColor = Color.parseColor("#804CAF50") // 반투명 녹색 시작
    private val fillEndColor = Color.parseColor("#104CAF50") // 거의 투명한 녹색 끝

    init {
        // 선 설정
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.color = lineColor

        // 텍스트 설정
        textPaint.color = textColor
        textPaint.textAlign = Paint.Align.CENTER

        // 채우기 설정
        fillPaint.style = Paint.Style.FILL

        // 화면 크기 확인
        checkScreenWidth()

        // 기본 데이터 설정
        setDefaultData()
    }

    /**
     * 화면 너비에 따라 UI 요소 조정
     */
    private fun checkScreenWidth() {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        isNarrowScreen = screenWidth < (400 * displayMetrics.density)

        // 화면 크기에 따른 설정 조정
        if (isNarrowScreen) {
            textPaint.textSize = 12f
            paint.strokeWidth = 3f
            pointRadius = 5f
            lineWidth = 3f
        } else {
            textPaint.textSize = 16f
            paint.strokeWidth = 5f
            pointRadius = 8f
            lineWidth = 5f
        }
    }

    /**
     * 기본 리워드 데이터 설정
     */
    private fun setDefaultData() {
        // 일주일간의 리워드 데이터 (15.0~15.5 범위)
        rewardValues = floatArrayOf(15.1f, 15.3f, 15.0f, 15.4f, 15.2f, 15.5f, 15.3f)

        // 요일 레이블
        dayLabels = arrayOf("월", "화", "수", "목", "금", "토", "일")
    }

    /**
     * 리워드 데이터 설정
     */
    fun setRewardData(values: FloatArray, labels: Array<String> = arrayOf()) {
        if (values.isEmpty()) return

        rewardValues = values
        if (labels.isNotEmpty()) {
            dayLabels = labels
        }

        // 최대/최소값 설정으로 변화가 더 잘 보이도록 조정
        val max = values.maxOrNull() ?: 16.0f
        val min = values.minOrNull() ?: 14.5f

        // 값 범위 확장으로 그래프가 전체 높이를 활용하도록 함
        minValue = min - 0.2f
        maxValue = max + 0.2f

        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // 화면 크기 확인
        checkScreenWidth()

        // 차트 영역 계산
        val padding = if (isNarrowScreen) 40f else 60f
        chartRect.set(
            padding,
            padding,
            width.toFloat() - padding,
            height.toFloat() - padding * 1.5f // 하단에 더 많은 공간 (요일 레이블용)
        )

        // 그라데이션 설정
        fillPaint.shader = LinearGradient(
            0f, chartRect.top,
            0f, chartRect.bottom,
            fillStartColor, fillEndColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (rewardValues.isEmpty()) return

        drawChart(canvas)
    }

    /**
     * 차트 그리기
     */
    private fun drawChart(canvas: Canvas) {
        val width = chartRect.width()
        val height = chartRect.height()
        val segmentWidth = width / (rewardValues.size - 1)

        // 패스 초기화
        path.reset()
        fillPath.reset()

        // 시작점
        val startX = chartRect.left
        val startY = chartRect.bottom - ((rewardValues[0] - minValue) / (maxValue - minValue)) * height

        path.moveTo(startX, startY)
        fillPath.moveTo(startX, chartRect.bottom)
        fillPath.lineTo(startX, startY)

        // 선 그리기를 위한 점들 연결
        for (i in 1 until rewardValues.size) {
            val x = chartRect.left + i * segmentWidth
            val y = chartRect.bottom - ((rewardValues[i] - minValue) / (maxValue - minValue)) * height
            path.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        // 채우기 패스 완성
        fillPath.lineTo(chartRect.right, chartRect.bottom)
        fillPath.close()

        // 영역 채우기 그리기
        canvas.drawPath(fillPath, fillPaint)

        // 선 그리기
        paint.strokeWidth = lineWidth
        canvas.drawPath(path, paint)

        // 점 및 값 라벨 그리기
        for (i in rewardValues.indices) {
            val x = chartRect.left + i * segmentWidth
            val y = chartRect.bottom - ((rewardValues[i] - minValue) / (maxValue - minValue)) * height

            // 점 그리기
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            canvas.drawCircle(x, y, pointRadius, paint)
            paint.color = lineColor
            canvas.drawCircle(x, y, pointRadius * 0.6f, paint)
            paint.style = Paint.Style.STROKE
            paint.color = lineColor

            // 값 라벨 그리기
            val valueText = String.format("%.1f", rewardValues[i])
            canvas.drawText(valueText, x, y - 15f, textPaint)

            // 요일 레이블 그리기
            if (dayLabels.isNotEmpty() && i < dayLabels.size) {
                canvas.drawText(dayLabels[i], x, chartRect.bottom + 25f, textPaint)
            }
        }
    }
}