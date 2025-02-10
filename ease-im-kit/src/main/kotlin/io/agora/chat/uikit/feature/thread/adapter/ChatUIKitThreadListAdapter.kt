package io.agora.chat.uikit.feature.thread.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatThread
import io.agora.chat.uikit.databinding.UikitItemChatThreadListItemBinding
import io.agora.chat.uikit.feature.thread.viewholder.ChatUIKitThreadListViewHolder

class ChatUIKitThreadListAdapter(

): ChatUIKitBaseRecyclerViewAdapter<ChatThread>(){
    private var messageMap:MutableMap<String,ChatMessage> = mutableMapOf()
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatThread> {
        return ChatUIKitThreadListViewHolder(UikitItemChatThreadListItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatThread>, position: Int) {
        if (holder is ChatUIKitThreadListViewHolder){
            holder.setLatestMessages(messageMap)
        }
        super.onBindViewHolder(holder, position)
    }

    fun setLatestMessages(latestMsgMap:MutableMap<String,ChatMessage>?){
        latestMsgMap?.let {
            messageMap.putAll(it)
            notifyDataSetChanged()
        }
    }

    fun getLatestMessages():MutableMap<String,ChatMessage>{
        return messageMap
    }

    fun clearLatestMessages(){
        messageMap.clear()
    }
}