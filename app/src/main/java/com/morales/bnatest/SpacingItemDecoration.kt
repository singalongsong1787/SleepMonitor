package com.morales.bnatest

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


//function:为每一个item增加间距

class SpacingItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    /**
     * function:为每一个Item设置偏移量，实现间距效果
     * @param：Rect对象，包含left、right、top、bottom
     * @param：vie对象
     * @param：parent：视图本身
     * @state：当前状态
     * */
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val spacingPx = dpToPx(spacing, parent.context)
        // 设置左、上、右、下四个方向的间距（这里统一设置为spacingPx的值）
        outRect.left = 0
        outRect.right = 0
        outRect.top = spacingPx
        outRect.bottom = spacingPx
    }
}

private fun dpToPx(dp: Int, context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density).toInt()
}

