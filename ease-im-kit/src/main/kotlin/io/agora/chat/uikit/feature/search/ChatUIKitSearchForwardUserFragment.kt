package io.agora.chat.uikit.feature.search

import android.os.Bundle
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.feature.chat.forward.adapter.ChatUIKitContactForwardAdapter
import io.agora.chat.uikit.interfaces.OnForwardClickListener
import io.agora.chat.uikit.model.ChatUIKitUser

class ChatUIKitSearchForwardUserFragment: ChatUIKitSearchUserFragment() {
    private var forwardClickListener: OnForwardClickListener? = null
    private var sentUserList: List<String>? = null

    override fun initAdapter(): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser> {
        return ChatUIKitContactForwardAdapter()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        if (mListAdapter is ChatUIKitContactForwardAdapter){
            (mListAdapter as ChatUIKitContactForwardAdapter).setSentUserList(sentUserList)
        }
    }

    override fun searchText(query: String) {
        super.searchText(query)
        if (mListAdapter is ChatUIKitContactForwardAdapter){
            (mListAdapter as ChatUIKitContactForwardAdapter).setSearchKey(query)
        }
    }

    override fun initListener() {
        super.initListener()
        if (mListAdapter is ChatUIKitContactForwardAdapter){
            (mListAdapter as ChatUIKitContactForwardAdapter).setOnForwardClickListener(forwardClickListener)
        }
    }

    fun setOnForwardClickListener(listener: OnForwardClickListener?){
        this.forwardClickListener = listener
    }

    fun setSentUserList(userList: List<String>?) {
        this.sentUserList = userList
    }

}