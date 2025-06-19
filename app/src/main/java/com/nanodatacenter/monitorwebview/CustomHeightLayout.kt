package com.nanodatacenter.monitorwebview

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

/**
 * 최대 높이 제한 기능이 추가된 커스텀 LinearLayout
 */
class CustomHeightLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // 최대 높이 속성
    var maxHeight = 0
        set(value) {
            field = value
            requestLayout()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec
        if (maxHeight > 0) {
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            if (heightSize > maxHeight) {
                // 높이가 최대 높이보다 크면 최대 높이로 제한
                heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, heightMode)
            }
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }
}