package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage

interface OnMessageAckSendCallback {
    /**
     * Callback after the message ack is sent successfully
     * @param message
     */
    fun onSendAckSuccess(message: ChatMessage?) {}

    /**
     * Wrong message when sending message ack.
     * @param message
     * @param code
     * @param errorMsg
     */
    fun onSendAckError(message: ChatMessage?, code: Int, errorMsg: String?)
}