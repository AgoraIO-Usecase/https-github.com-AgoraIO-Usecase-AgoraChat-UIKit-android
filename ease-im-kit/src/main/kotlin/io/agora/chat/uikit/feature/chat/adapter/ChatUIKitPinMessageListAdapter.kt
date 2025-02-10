package io.agora.chat.uikit.feature.chat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.databinding.UikitPinlistDefaultLayoutBinding
import io.agora.chat.uikit.databinding.UikitPinlistImageLayoutBinding
import io.agora.chat.uikit.databinding.UikitPinlistTextLayoutBinding
import io.agora.chat.uikit.feature.chat.pin.holder.ChatUIKitPinDefaultViewHolder
import io.agora.chat.uikit.feature.chat.pin.holder.ChatUIKitPinImageMessageViewHolder
import io.agora.chat.uikit.feature.chat.pin.holder.ChatUIKitPinTextMessageViewHolder

class ChatUIKitPinMessageListAdapter: ChatUIKitBaseRecyclerViewAdapter<ChatMessage>() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        mData?.let {
            return it[position].type.ordinal
        }
        return -1
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        val viewHolder: ViewHolder<ChatMessage>

        when (ChatMessageType.values()[viewType]) {
            ChatMessageType.TXT -> viewHolder = ChatUIKitPinTextMessageViewHolder(
                mItemSubViewListener,
                UikitPinlistTextLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            )

            ChatMessageType.IMAGE -> viewHolder = ChatUIKitPinImageMessageViewHolder(
                mItemSubViewListener,
                UikitPinlistImageLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            )

            else -> viewHolder = ChatUIKitPinDefaultViewHolder(
                mItemSubViewListener,
                UikitPinlistDefaultLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
            )
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatMessage>, position: Int) {
        super.onBindViewHolder(holder, position)
    }
}