package io.agora.chat.uikit.feature.chat.reply.interfaces

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.reply.ChatUIKitMessageReplyViewModel

interface IChatMessageReply {
    /**
     * Set quote message.
     * @param message
     */
    fun startQuote(message: ChatMessage?)

    /**
     * Hide quote view.
     */
    fun hideQuoteView()

    /**
     * Set quote message viewModel.
     * @param viewModel
     */
    fun setViewModel(viewModel: ChatUIKitMessageReplyViewModel?)
}