package io.agora.chat.uikit.viewmodel.reply

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.IAttachView

interface IChatMessageReplyRequest: IAttachView {
    /**
     * Show reply message info.
     * @param message
     */
    fun showQuoteMessageInfo(message: ChatMessage?)
}