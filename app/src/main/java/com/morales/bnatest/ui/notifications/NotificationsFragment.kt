package com.morales.bnatest.ui.notifications

import android.util.Log
import FunctionAdapter
import FunctionItem
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.morales.bnatest.CalendarActivity
import com.morales.bnatest.MultiSelectCalendarActivity
import com.morales.bnatest.R


class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FunctionAdapter
    private val functionItemList = mutableListOf<FunctionItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("NotificationsFragment", "onCreateView called")
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        //改变状态栏的颜色
        val window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.bar_DashboardFragment)

        recyclerView = view.findViewById(R.id.function_recycler_view)
        if (recyclerView == null) {
            Log.e("NotificationsFragment", "RecyclerView is null!")
        } else {
            Log.d("NotificationsFragment", "RecyclerView found successfully")
            Log.d("NotificationsFragment", "RecyclerView width: ${recyclerView.width}, height: ${recyclerView.height}")

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            Log.d("NotificationsFragment", "LayoutManager set to LinearLayoutManager")

            initFunctionList()
            Log.d("NotificationsFragment", "Function list initialized. Size: ${functionItemList.size}")

           // adapter = FunctionAdapter(functionItemList)

            /*创建适配器并设置点击监听器*/
            adapter = FunctionAdapter(functionItemList, object : FunctionAdapter.OnItemClickListener {
                override fun onItemClick(position: Int?) {
                    Log.d("calendar", "点击位置: $position")
                    if (position != null) {
                        when (position) {
                            0 -> {
                                Log.d("calendar", "日历准备打开")
                                val intent = Intent(requireContext(), CalendarActivity::class.java)
                                startActivity(intent)
                                Log.d("calendar", "日历正确打开")
                            }
                            1 -> {
                                Log.d("calendar", "多选日历准备打开")
                                val intent = Intent(requireContext(),  MultiSelectCalendarActivity::class.java)
                                startActivity(intent)
                                Log.d("calendar", "多选日历正确打开")
                            }
                        }
                    }
                }
            })

            recyclerView.adapter = adapter
            Log.d("NotificationsFragment", "Adapter set to RecyclerView")
        }

        return view
    }

    private fun initFunctionList() {
        Log.d("NotificationsFragment", "Initializing function list...")
        functionItemList.add(FunctionItem(R.drawable.chevron_right1, "查看报告"))
        functionItemList.add(FunctionItem(R.drawable.chevron_right1, "删除数据"))
        Log.d("NotificationsFragment", "Function list initialized with ${functionItemList.size} items.")
    }



}