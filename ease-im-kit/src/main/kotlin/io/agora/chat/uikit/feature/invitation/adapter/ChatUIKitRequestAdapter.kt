package io.agora.chat.uikit.feature.invitation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.databinding.UikitLayoutInvitationItemBinding
import io.agora.chat.uikit.feature.invitation.holder.ChatUIKitNewRequestsViewHolder

class ChatUIKitRequestAdapter: ChatUIKitBaseRecyclerViewAdapter<ChatMessage>() {

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        return ChatUIKitNewRequestsViewHolder(
            UikitLayoutInvitationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            mItemSubViewListener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatMessage>, position: Int) {
        super.onBindViewHolder(holder, position)
    }

}