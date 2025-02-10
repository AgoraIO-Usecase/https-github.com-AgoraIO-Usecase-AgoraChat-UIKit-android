package io.agora.chat.uikit.feature.chat.forward.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.databinding.UikitItemForwardLayoutBinding
import io.agora.chat.uikit.feature.chat.forward.viewholder.ChatUIKitGroupForwardViewHolder
import io.agora.chat.uikit.feature.group.adapter.ChatUIKitGroupListAdapter
import io.agora.chat.uikit.interfaces.OnForwardClickListener

class ChatUIKitGroupListForwardAdapter: ChatUIKitGroupListAdapter() {
    private var forwardClickListener: OnForwardClickListener? = null
    private val sentGroupList = mutableListOf<Int>()
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatGroup> {
        return ChatUIKitGroupForwardViewHolder(UikitItemForwardLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatGroup>, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        if (holder is ChatUIKitGroupForwardViewHolder){
            holder.btnForward.isEnabled = !sentGroupList.contains(position)
            holder.btnForward.text = if (sentGroupList.contains(position)) holder.itemView.context.getString(R.string.uikit_chat_reply_forwarded)
            else holder.itemView.context.getString(R.string.uikit_action_forward)
            holder.btnForward.setOnClickListener { view ->
                sentGroupList.add(position)
                holder.btnForward.isEnabled = false
                holder.btnForward.text = holder.itemView.context.getString(R.string.uikit_chat_reply_forwarded)
                item?.let {
                    forwardClickListener?.onForwardClick(view, it.groupId, ChatType.GroupChat)
                }
            }
        }
    }

    fun setOnForwardClickListener(listener: OnForwardClickListener?){
        this.forwardClickListener = listener
    }
}