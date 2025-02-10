package io.agora.chat.uikit.feature.conversation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.feature.conversation.config.ChatUIKitConvItemConfig
import io.agora.chat.uikit.feature.conversation.viewholders.ChatUIKitConvViewType
import io.agora.chat.uikit.feature.conversation.viewholders.ChatUIKitConversationViewHolderFactory
import io.agora.chat.uikit.model.ChatUIKitConversation

/**
 * Adapter for conversation list.
 * @param config Conversation item config.
 */
open class ChatUIKitConversationListAdapter(
    private var config: ChatUIKitConvItemConfig = ChatUIKitConvItemConfig()
): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitConversation>() {

    override fun getItemNotEmptyViewType(position: Int): Int {
        return ChatUIKitConversationViewHolderFactory.getViewType(getItem(position))
    }
    override fun getViewHolder(parent: ViewGroup, viewType: Int) =
        ChatUIKitConversationViewHolderFactory.createViewHolder(LayoutInflater.from(parent.context), parent,
            ChatUIKitConvViewType.fromValue(viewType), config)

    /**
     * Set conversation item config.
     */
    fun setConversationItemConfig(config: ChatUIKitConvItemConfig) {
        this.config = config
        notifyDataSetChanged()
    }
}