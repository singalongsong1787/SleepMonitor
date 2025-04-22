package com.morales.bnatest.ui.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import android.util.Log

/**
 * ViewPagerAdapter 是一个自定义的适配器，用于管理 ViewPager2 中的 Fragment。
 * 它扩展了 [FragmentStateAdapter]，并提供了添加 Fragment 和获取标题的功能。
 * @param fragmentActivity FragmentActivity 的实例，用于创建适配器。
* */
class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    val TAG="createFragment"
    // 用于存储 Fragment 的列表
    private val fragmentList = ArrayList<Fragment>()
    // 用于存储 Fragment 标题的列表
    private val fragmentTitleList = ArrayList<String>()

    /**
     * 获取 ViewPager2 中的 Fragment 数量。
     * @return 返回 Fragment 的总数。
     */
    override fun getItemCount(): Int = fragmentList.size
    /**
     * 根据位置创建并返回对应的 Fragment。
     * @param position Fragment 的位置索引。
     * @return 返回指定位置的 Fragment。
     */
    override fun createFragment(position: Int): Fragment {
        val fragment = fragmentList[position]

        Log.d(TAG, "createFragment: Creating fragment at position $position: ${fragment::class.java.simpleName}")
        return fragment
    }

    /**
     * 添加一个 Fragment 到适配器中，并为其设置标题。
     * @param fragment 要添加的 Fragment。
     * @param title Fragment 的标题，用于显示在 TabLayout 中。
     */
    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitleList.add(title)
    }

    // 提供一个公共方法来获取标题
    /**
     * 获取指定位置的 Fragment 标题。
     * @param position Fragment 的位置索引。
     * @return 返回指定位置的 Fragment 标题。
     */
    fun getTitle(position: Int): String {
        return fragmentTitleList[position]
    }
}