package com.morales.bnatest

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.LruCache
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * class function:对reycleView进行解聘，将其内容截取为一个完整的Bitmap对象
 *  @return 返回包含 RecyclerView 所有内容的 Bitmap 对象，若 RecyclerView 无适配器则返回 null
 * */
class ScreenShot(private val recyclerView: RecyclerView) {

    fun captureRecyclerView():Bitmap?{

        // 获取 RecyclerView 的适配器
        val adapter = recyclerView.adapter//适配器是recycler中的一个属性
        Log.d("Screen","能够得到适配器")
        //检查设配器的一系列内容
       var bigBitmap:Bitmap?=null
        if (adapter != null) {
            // 获取 RecyclerView 中所有项的数量
            val size = adapter.itemCount
            // 用于记录 RecyclerView 中所有项的总高度
            var height = 0
            // 用于绘制 Bitmap 的画笔
            val paint = Paint()
            // 记录当前绘制的 Bitmap 在大 Bitmap 中的垂直偏移量
            var iHeight = 0
            // 获取应用程序可使用的最大内存，并将其转换为 KB
            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

            // 使用可用内存的 1/8 作为内存缓存的大小
            val cacheSize = maxMemory / 8
            // 创建一个 LruCache 对象，用于缓存每个 ViewHolder 的 Bitmap
            val bitmaCache = LruCache<String, Bitmap>(cacheSize)
            // 遍历 RecyclerView 中的所有项
            for (i in 0 until size) {
                // 创建一个 ViewHolder 实例
                val holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i))
                // 将数据绑定到 ViewHolder 上
                adapter.onBindViewHolder(holder, i)
                // 测量 ViewHolder 的 itemView 的尺寸，宽度使用 RecyclerView 的宽度，高度不做限制
                holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                )
                // 对 ViewHolder 的 itemView 进行布局
                holder.itemView.layout(0, 0, holder.itemView.measuredWidth, holder.itemView.measuredHeight)
                // 开启 ViewHolder 的 itemView 的绘图缓存功能
                holder.itemView.isDrawingCacheEnabled = true
                // 构建 ViewHolder 的 itemView 的绘图缓存
                holder.itemView.buildDrawingCache()
                // 获取 ViewHolder 的 itemView 的绘图缓存 Bitmap
                val drawingCache = holder.itemView.drawingCache
                // 检查绘图缓存 Bitmap 是否存在，若存在则将其存入 LruCache 中
                if (drawingCache != null) {
                    bitmaCache.put(i.toString(), drawingCache)
                }
                // 累加当前 ViewHolder 的 itemView 的高度到总高度中
                height += holder.itemView.measuredHeight
            }

            // 创建一个大的 Bitmap 对象，宽度为 RecyclerView 的宽度，高度为所有项的总高度
            bigBitmap = Bitmap.createBitmap(recyclerView.measuredWidth, height, Bitmap.Config.ARGB_8888)
            // 创建一个 Canvas 对象，用于在大 Bitmap 上进行绘制
            val bigCanvas = Canvas(bigBitmap)
            // 获取 RecyclerView 的背景 Drawable
            val lBackground = recyclerView.background
            // 检查背景 Drawable 是否为 ColorDrawable 类型
            if (lBackground is ColorDrawable) {
                // 获取背景颜色
                val lColor = lBackground.color
                // 使用背景颜色填充大 Bitmap
                bigCanvas.drawColor(lColor)
            }

            // 遍历 LruCache 中的所有 Bitmap，将它们依次绘制到大 Bitmap 上
            for (i in 0 until size) {
                // 从 LruCache 中获取当前位置的 Bitmap
                val bitmap = bitmaCache.get(i.toString())
                // 将 Bitmap 绘制到大 Bitmap 上，x 坐标为 0，y 坐标为当前垂直偏移量
                bigCanvas.drawBitmap(bitmap, 0f, iHeight.toFloat(), paint)
                // 累加当前 Bitmap 的高度到垂直偏移量中
                iHeight += bitmap.height
                // 回收当前 Bitmap 的内存，释放资源
                bitmap.recycle()
            }
        }
        // 返回最终生成的大 Bitmap 对象
        return bigBitmap

    }

}