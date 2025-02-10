package io.agora.chat.uikit.feature.chat.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

class EmojiconPagerAdapter(private val views: List<View>) : PagerAdapter() {
    override fun getCount(): Int {
        return views.size
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }

    override fun instantiateItem(arg0: ViewGroup, arg1: Int): Any {
        (arg0 as ViewPager).addView(views[arg1])
        return views[arg1]
    }

    override fun destroyItem(arg0: ViewGroup, arg1: Int, arg2: Any) {
        (arg0 as ViewPager).removeView(views[arg1])
    }
}