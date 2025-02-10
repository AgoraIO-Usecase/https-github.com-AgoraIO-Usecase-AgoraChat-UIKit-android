package io.agora.chat.uikit.feature.chat.chathistory

import android.view.ViewGroup
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitMessagesAdapter
import io.agora.chat.uikit.feature.chat.chathistory.viewholder.ChatUIKitHistoryViewHolderFactory
import io.agora.chat.uikit.feature.chat.viewholders.ChatUIKitMessageViewType

class ChatUIKitHistoryAdapter : ChatUIKitMessagesAdapter() {
    override fun getItemNotEmptyViewType(position: Int): Int {
        return ChatUIKitHistoryViewHolderFactory.getViewType(mData?.get(position))
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        return ChatUIKitHistoryViewHolderFactory.createViewHolder(
            parent,
            ChatUIKitMessageViewType.from(viewType)
        )
    }

}
