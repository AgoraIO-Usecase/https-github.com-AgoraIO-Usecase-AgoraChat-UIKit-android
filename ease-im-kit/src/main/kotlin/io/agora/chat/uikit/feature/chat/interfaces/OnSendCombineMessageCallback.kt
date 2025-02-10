package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage

interface OnSendCombineMessageCallback {
    /**
     * Callback after the combine message is sent successfully
     * @param message
     */
    fun onSendCombineSuccess(message: ChatMessage?) {}

    /**
     * Wrong message in chat
     * @param code
     * @param errorMsg
     */
    fun onSendCombineError(message: ChatMessage?, code: Int, errorMsg: String?)
}