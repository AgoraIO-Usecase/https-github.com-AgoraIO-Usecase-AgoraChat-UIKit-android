package io.agora.chat.uikit.feature.chat.interfaces

import io.agora.chat.uikit.common.ChatMessage


interface OnReactionMessageListener {
    /**
     * add reaction success
     *
     * @param message
     */
    fun addReactionMessageSuccess(message: ChatMessage?)

    /**
     * add reaction fail
     *
     * @param message
     * @param code
     * @param error
     */
    fun addReactionMessageFail(message: ChatMessage?, code: Int, error: String?)

    /**
     * remove reaction success
     *
     * @param message
     */
    fun removeReactionMessageSuccess(message: ChatMessage?)

    /**
     * remove reaction fail
     *
     * @param message
     * @param code
     * @param error
     */
    fun removeReactionMessageFail(message: ChatMessage?, code: Int, error: String?)
}