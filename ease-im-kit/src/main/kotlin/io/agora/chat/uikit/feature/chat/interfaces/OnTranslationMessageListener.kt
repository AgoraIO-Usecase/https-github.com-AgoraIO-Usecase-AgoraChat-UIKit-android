package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage

interface OnTranslationMessageListener {
    /**
     * translation message success
     */
    fun onTranslationMessageSuccess(message: ChatMessage?){}

    /**
     * hide translation message
     */
    fun onHideTranslationMessage(message: ChatMessage?){}

    /**
     * translation message failure
     * @param code
     * @param error
     */
    fun onTranslationMessageFailure(code: Int, error: String?){}
}