package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.interfaces.IControlDataView

interface IChatHistoryResultView: IControlDataView {
    /**
     * Download combine message successfully.
     * @param messageList   Parsed message list.
     */
    fun downloadCombinedMessagesSuccess(messageList: List<ChatMessage>)

    /**
     * Download or parse combine message failed.
     * @param error
     * @param errorMsg
     */
    fun downloadCombinedMessagesFail(error: Int, errorMsg: String?)
}