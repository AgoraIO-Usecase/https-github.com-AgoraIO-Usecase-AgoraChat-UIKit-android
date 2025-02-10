package io.agora.chat.uikit.feature.chat.forward

import android.os.Bundle
import android.view.View
import io.agora.chat.uikit.feature.chat.forward.adapter.ChatUIKitGroupListForwardAdapter
import io.agora.chat.uikit.feature.group.adapter.ChatUIKitGroupListAdapter
import io.agora.chat.uikit.feature.group.fragments.ChatUIKitGroupListFragment
import io.agora.chat.uikit.interfaces.OnForwardClickListener

class ChatUIKitGroupListForwardFragment: ChatUIKitGroupListFragment() {
    private var forwardClickListener: OnForwardClickListener? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.titleContact?.visibility = View.GONE
    }

    override fun getCustomAdapter(): ChatUIKitGroupListAdapter {
        return ChatUIKitGroupListForwardAdapter()
    }

    override fun initListener() {
        super.initListener()
        (this.adapter as? ChatUIKitGroupListForwardAdapter)
            ?.setOnForwardClickListener(forwardClickListener)
    }

    override fun onItemClick(view: View?, position: Int) {
        // do nothing
    }

    fun setOnForwardClickListener(listener: OnForwardClickListener?){
        this.forwardClickListener = listener
    }
}