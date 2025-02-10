package io.agora.chat.uikit.feature.conversation.viewholders

import android.view.LayoutInflater
import android.view.ViewGroup
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitItemConversationListBinding
import io.agora.chat.uikit.feature.conversation.config.ChatUIKitConvItemConfig
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.model.chatConversation

object ChatUIKitConversationViewHolderFactory {
    fun createViewHolder(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        viewType: ChatUIKitConvViewType = ChatUIKitConvViewType.VIEW_TYPE_CONVERSATION,
        style: ChatUIKitConvItemConfig = ChatUIKitConvItemConfig()
    ): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatUIKitConversation> {
        return when(viewType) {
            ChatUIKitConvViewType.VIEW_TYPE_CONVERSATION -> {
                ChatUIKitConversationViewHolder(
                    UikitItemConversationListBinding.inflate(inflater, parent, false),
                    style
                )
            }
            else -> {
                // Return default view holder
                ChatUIKitConversationViewHolder(
                    UikitItemConversationListBinding.inflate(inflater, parent, false),
                    style
                )
            }
        }

    }

    fun getViewType(conversation: ChatUIKitConversation?): Int {
        return getConversationType(conversation).value
    }

    private fun getConversationType(conversation: ChatUIKitConversation?): ChatUIKitConvViewType {
        val conv = conversation?.chatConversation()
        return if (conv == null) ChatUIKitConvViewType.VIEW_TYPE_CONVERSATION_UNKNOWN
        else ChatUIKitConvViewType.VIEW_TYPE_CONVERSATION
    }
}