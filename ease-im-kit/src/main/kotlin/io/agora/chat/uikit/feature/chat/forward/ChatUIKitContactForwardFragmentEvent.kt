package io.agora.chat.uikit.feature.chat.forward

import android.os.Bundle
import android.view.View
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.feature.chat.forward.adapter.ChatUIKitContactForwardAdapter
import io.agora.chat.uikit.feature.chat.forward.dialog.ChatUIKitSearchForwardUserDialogFragment
import io.agora.chat.uikit.feature.contact.ChatUIKitContactsListFragment
import io.agora.chat.uikit.interfaces.OnForwardClickListener

class ChatUIKitContactForwardFragmentEvent: ChatUIKitContactsListFragment() {
    private var forwardClickListener: OnForwardClickListener? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.titleContact?.visibility = View.GONE
        binding?.listContact?.setSideBarVisible(false)
        val adapter = ChatUIKitContactForwardAdapter()
        adapter.setHasStableIds(true)
        binding?.listContact?.setListAdapter(adapter)
    }

    override fun initListener() {
        super.initListener()
        (binding?.listContact?.getListAdapter() as? ChatUIKitContactForwardAdapter)
            ?.setOnForwardClickListener(forwardClickListener)

        binding?.searchBar?.setOnClickListener {
            ChatUIKitSearchForwardUserDialogFragment().apply {
                setOnForwardClickListener(object : OnForwardClickListener {
                    override fun onForwardClick(view: View?, id: String, chatType: ChatType) {
                        (this@ChatUIKitContactForwardFragmentEvent.binding?.listContact?.getListAdapter() as? ChatUIKitContactForwardAdapter)
                            ?.setSentUserList(listOf(id))
                    }
                })
                setSentUserList((this@ChatUIKitContactForwardFragmentEvent.binding?.listContact?.getListAdapter()
                        as? ChatUIKitContactForwardAdapter)?.getSentUserList())
                show(this@ChatUIKitContactForwardFragmentEvent.childFragmentManager, "search_forward_user")
            }
        }
    }

    fun setOnForwardClickListener(listener: OnForwardClickListener?){
        this.forwardClickListener = listener
    }

    override fun onDestroyView() {
        forwardClickListener = null
        super.onDestroyView()
    }
}