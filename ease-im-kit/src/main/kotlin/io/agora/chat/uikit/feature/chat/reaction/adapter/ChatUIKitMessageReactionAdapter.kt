package io.agora.chat.uikit.feature.chat.reaction.adapter

import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.model.ChatUIKitReaction

class ChatUIKitMessageReactionAdapter: ChatUIKitBaseRecyclerViewAdapter<ChatUIKitReaction>() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        return ChatUIKitMessageReactionViewHolderFactory.getViewType(getItem(position))
    }
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitReaction> {
        return ChatUIKitMessageReactionViewHolderFactory.createViewHolder(parent, viewType)
    }

}