package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage


interface OnRecallMessageResultListener {
    /**
     * Recall successful
     * @param originalMessage The message was unsent
     * @param notification  The notification message
     */
    fun recallSuccess(originalMessage: ChatMessage?, notification: ChatMessage?)

    /**
     * Recall failed
     * @param code
     * @param errorMsg
     */
    fun recallFail(code: Int, errorMsg: String?)
}