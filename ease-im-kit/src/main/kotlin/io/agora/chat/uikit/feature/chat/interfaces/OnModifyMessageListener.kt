package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage

/**
 * The user listens for modifing messages successfully
 */
interface OnModifyMessageListener {
    /**
     * modify message success
     * @param messageModified
     */
    fun onModifyMessageSuccess(messageModified: ChatMessage?)

    /**
     * modify message failure
     * @param messageId
     * @param code
     * @param error
     */
    fun onModifyMessageFailure(messageId: String?, code: Int, error: String?)
}