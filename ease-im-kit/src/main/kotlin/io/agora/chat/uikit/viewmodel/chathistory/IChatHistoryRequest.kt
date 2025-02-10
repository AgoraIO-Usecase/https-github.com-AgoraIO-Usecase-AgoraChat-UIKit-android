package io.agora.chat.uikit.viewmodel.chathistory

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.IAttachView

interface IChatHistoryRequest: IAttachView {

    /**
     * Download combine message attachment.
     */
    fun downloadCombineMessage(message: ChatMessage?)

}