package com.morales.bnatest.ui.dashboard


import Sleep
import android.content.Context

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.morales.bnatest.databinding.FragmentDashboardBinding
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.morales.bnatest.R
import com.morales.bnatest.fragment.DailyReportFragment


class DashboardFragment : Fragment() {

    // 定义一个可空的绑定变量，用于绑定布局文件
    private var _binding: FragmentDashboardBinding? = null
    // 提供一个只读属性 binding，用于简化代码访问绑定变量
    // 注意：这个属性只在 onCreateView 和 onDestroyView 之间有效
    private val binding get() = _binding!!

    private val TAG = "DashboardFragment"

    private var child0:android.widget.TextView? = null
    private var child1:android.widget.TextView? = null

    /**
     * 在 Fragment 创建视图时调用。
     * 该方法用于初始化 Fragment 的视图和绑定 ViewModel。
     *
     * @param inflater 用于将布局文件转换为 View 对象的工具。
     * @param container 父视图容器，用于存放加载后的视图。
     * @param savedInstanceState 保存的实例状态，用于恢复 Fragment 的状态。
     * @return 返回加载后的视图对象。
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 创建 ViewModel，用于管理数据
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)
        // 使用布局绑定工具加载布局文件 fragment_dashboard.xml
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 获取 ViewPager2 和 TabLayout 的引用
        val viewPager: ViewPager2 = binding.viewPager
        val tabLayout: TabLayout = binding.tabLayout

        // 设置 ViewPager2 的适配器
        setupViewPager(viewPager)
        //配置 TabLayout 和 ViewPager2 的联动
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "日报告"
                1 -> tab.text = "睡眠趋势"
            }
        }.attach()



        child1?.let{
            Log.d("Tab","位置1的文本为${child1!!.text}，大小为${child1!!.textSize}")
        }

        //添加TabLayout的点击监听
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    Log.d("DashboardFragment", "onTabSelected: Selected tab: ${it.text}")
                    setTabTextSize(true, it)
                }
            }
            /**
             * function:当一个已经选中的 Tab 被取消选中时会调用这个方法。
             * */
            override fun onTabUnselected(tab: TabLayout.Tab?) {

                tab?.let {
                    setTabTextSize(false, it)
                }


            }
            /**
             *function:当用户再次点击已经选中的 Tab 时会调用此方法。
             */
            override fun onTabReselected(tab: TabLayout.Tab?) {

                tab?.let {
                    setTabTextSize(false,it)
                }

            }
        })

        val firstTab = tabLayout.getTabAt(0)
        firstTab?.let {
            setTabTextSize(true,it)
        }

        val secondTab = tabLayout.getTabAt(1)
        secondTab?.let{
            setTabTextSize(false,it)
        }

        return root
    }

    /**
     * 在 Fragment 的视图创建完成后调用。
     * 可以在这里进行额外的初始化操作。
     *
     * @param view 当前 Fragment 的视图。
     * @param savedInstanceState 保存的实例状态。
     */

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Additional setup can be done here
    }

    /**
     * 设置 ViewPager2 的适配器。
     * 该方法用于初始化 ViewPager2 的适配器，并添加子 Fragment。
     *
     * @param viewPager ViewPager2 的实例。
     */

    private fun setupViewPager(viewPager: ViewPager2) {
        val adapter = ViewPagerAdapter(requireActivity())
        adapter.addFragment(DailyReportFragment(), "日报告")
        adapter.addFragment(Sleep(), "睡眠趋势")
        viewPager.adapter = adapter
    }

    /**
     * 在 Fragment 的视图被销毁时调用。
     * 用于清理资源，避免内存泄漏。
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 在 Fragment 恢复可见时调用，重新设置 Tab 文本样式
     */
    override fun onResume() {
        super.onResume()
        val tabLayout: TabLayout = binding.tabLayout
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            tab?.let {
                setTabTextSize(it.isSelected, it)
            }
        }
    }

    /**
     *function:修改字体大小
     * @param：（1）选中状态 （2）标签页
     * */

    private fun setTabTextSize(select: Boolean, tabFirst: TabLayout.Tab?) {
        tabFirst?.let { tab ->
            val context: Context = requireContext()
            //新建TextView对象
            val textView = TextView(context).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (select) 25f else 15f)
                setTextColor(ContextCompat.getColor(context, if (select) R.color.white else R.color.tablayout_unselected))
                gravity = Gravity.CENTER
                text = tab.text
            }
            tab.customView = textView
            if (textView.text == "睡眠趋势") {
                child1 = textView
            } else {
                child0 = textView
            }
        }
    }
}




