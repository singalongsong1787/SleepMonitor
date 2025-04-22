// WaveformView.kt
package  com.morales.bnatest
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

// 自定义波形视图类，继承自 View
class WaveformView @JvmOverloads constructor(
    context: Context,//上下文对象，用于访问 Android 系统资源
    attrs: AttributeSet? = null,//属性集，通常来自 XML 布局文件
    defStyleAttr: Int = 0//默认的样式属性，用于指定默认的样式资源
) : View(context, attrs, defStyleAttr) {

    // 初始化画笔，设置抗锯齿、颜色、线宽和绘制样式
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE // 设置画笔颜色为红色
        strokeWidth = 2f // 设置画笔线宽为 2 像素
        style = Paint.Style.STROKE // 设置绘制样式为描边
    }

    // 用于存储音频采样数据的列表，默认为空
    private var samples: List<Short> = emptyList()

    // 提供一个方法用于设置音频采样数据
    fun setSamples(samples: List<Short>) {
        this.samples = samples // 更新采样数据
        invalidate() // 触发视图重绘
    }

    // 重写 onDraw 方法，用于绘制波形
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (samples.isEmpty()) return // 如果采样数据为空，直接返回

        // 获取视图的宽度和高度
        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()

        // 获取短整型的最大值（用于归一化波形）
        val maxAmplitude = Short.MAX_VALUE.toFloat()

        // 对采样数据进行降采样，以适应屏幕宽度
        val downsampled = downsample(samples)

        // 遍历降采样后的数据，绘制波形
        downsampled.forEachIndexed { index, value ->
            // 计算当前点的 x 坐标
            val x = (index * width / downsampled.size).toFloat()
            // 计算当前点的 y 坐标，根据振幅值进行归一化
            val y = height / 2 - (value * height / (2 * maxAmplitude))
            if (index > 0) {
                // 如果不是第一个点，绘制连接线
                val prevX = ((index - 1) * width / downsampled.size).toFloat()
                val prevY = height / 2 - (downsampled[index - 1] * height / (2 * maxAmplitude))
                canvas.drawLine(prevX, prevY, x, y, paint)
            }
        }
    }

    // 降采样方法，将大量采样数据压缩到屏幕宽度
    private fun downsample(samples: List<Short>): List<Short> {
        // 计算目标采样点数量（屏幕宽度的一半，每个点间隔 2 像素）
        val targetPoints = measuredWidth / 2
        // 计算采样步长，确保至少有一个点
        val step = max(1, samples.size / targetPoints)
        // 使用 filterIndexed 方法，按步长筛选采样点
        return samples.filterIndexed { index, _ -> index % step == 0 }
    }
}