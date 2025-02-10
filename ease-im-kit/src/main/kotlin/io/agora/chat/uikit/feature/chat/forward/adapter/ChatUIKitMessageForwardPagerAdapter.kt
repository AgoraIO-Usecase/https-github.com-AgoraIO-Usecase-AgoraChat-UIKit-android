package io.agora.chat.uikit.feature.chat.forward.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.agora.chat.uikit.model.ChatUIKitPager

class ChatUIKitMessageForwardPagerAdapter(
    private val manager: FragmentManager,
    private val lifecycle: Lifecycle
): FragmentStateAdapter(manager, lifecycle) {
    private var mData: List<ChatUIKitPager>? = null

    fun setData(data: List<ChatUIKitPager>?) {
        this.mData = data
        notifyDataSetChanged()
    }

    fun getData(): List<ChatUIKitPager>? {
        return mData
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }

    override fun createFragment(position: Int): Fragment {
        mData!![position].let {
            return it.fragment
        }
    }
}