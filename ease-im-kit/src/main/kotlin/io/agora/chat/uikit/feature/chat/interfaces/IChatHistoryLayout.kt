package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitMessagesAdapter
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitMessageListLayout
import io.agora.chat.uikit.viewmodel.chathistory.IChatHistoryRequest

interface IChatHistoryLayout {
    /**
     * set custom ViewModel
     * @param viewModel
     */
    fun setViewModel(viewModel: IChatHistoryRequest?)

    /**
     * Set custom adapter.
     */
    fun setMessagesAdapter(adapter: ChatUIKitMessagesAdapter?)

    /**
     * Set the combine message download or parse callback.
     */
    fun setOnCombineMessageDownloadCallback(callback: OnCombineMessageDownloadCallback?)

    /**
     * Get the chat message list layout.
     */
    fun getChatMessageListLayout(): ChatUIKitMessageListLayout
}