import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.morales.bnatest.R

/**
 * FunctionAdapter类：适配器类，继承自 RecyclerView.Adapter
 * @function:它的作用是将一个功能项列表（functionItemList）绑定到 RecyclerView中
 * @param:functionItemList: List<FunctionItem>
 * @return：无（构造函数）。
 * 2. FunctionViewHolder 内部类
 * */
class FunctionAdapter(private val functionItemList: List<FunctionItem>,
                      private val listener: OnItemClickListener//设置一个监听器
) :
    RecyclerView.Adapter<FunctionAdapter.FunctionViewHolder>() {

    private val TAG = "FunctionAdapter"

    /**
     * funciton:它用于持有 RecyclerView 每个条目中的视图组件（如图标和名称）
     * @param:itemView: View：表示 RecyclerView 条目的根视图
     * */

    class FunctionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val functionIcon: ImageView = itemView.findViewById(R.id.function_icon)
        val functionName: TextView = itemView.findViewById(R.id.function_name)
    }


    /**
     * function:创建一个新的 FunctionViewHolder，用于显示 RecyclerView 的条目
     * @param:parent: ViewGroup：表示 RecyclerView 的父视图，用于确定条目的布局参数
     * @param:viewType: Int：表示条目的视图类型
     * */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FunctionViewHolder {
        Log.d(TAG, "onCreateViewHolder called. Parent: ${parent.javaClass.simpleName}, ViewType: $viewType")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_function, parent, false)
        return FunctionViewHolder(view)
    }

    /**
     * funtion:将数据（FunctionItem）绑定到对应的 FunctionViewHolder，更新条目的显示内容。
     * @param:holder: FunctionViewHolder：表示当前条目的视图持有者。
     * @param:position: Int：表示当前条目在列表中的位置。
     * */
    override fun onBindViewHolder(holder: FunctionViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder called. Position: $position")
        try {
            val functionItem = functionItemList[position]
            Log.d(TAG, "Binding item at position $position: ${functionItem.functionName}")
            holder.functionIcon.setImageResource(functionItem.iconResId)
            holder.functionName.text = functionItem.functionName
        } catch (e: Exception) {
            Log.e(TAG, "Error binding item at position $position: ${e.message}", e)
        }

        holder.itemView.setOnClickListener {
            Log.d(TAG, "Item clicked at position: $position")
            listener.onItemClick(position as Int?) // 按照接口签名传递参数

        }

    }

    /**
     * function:返回 RecyclerView 中的条目总数。
     * */
    override fun getItemCount(): Int {
        val count = functionItemList.size
        Log.d(TAG, "getItemCount called. Returning count: $count")
        return count
    }

    // 定义点击监听接口
    interface OnItemClickListener {
        fun onItemClick(position: Int?)
    }
}