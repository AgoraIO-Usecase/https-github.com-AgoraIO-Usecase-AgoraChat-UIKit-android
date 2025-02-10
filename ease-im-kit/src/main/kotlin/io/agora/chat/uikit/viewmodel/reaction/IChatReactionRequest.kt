package io.agora.chat.uikit.viewmodel.reaction

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.feature.chat.reaction.interfaces.IChatReactionResultView


interface IChatReactionRequest {

    /**
     * Attach to the target View.
     */
    fun attachView(message: ChatMessage, view: IChatReactionResultView, isDialog: Boolean = false)

    /**
     * Detach from the target View.
     */
    fun detachView(message: ChatMessage, isMenuDialog: Boolean = false)

    /**
     * Get the default reactions in message menu dialog.
     */
    fun getDefaultReactions(message: ChatMessage)

    /**
     * Get the default reactions in message menu dialog.
     */
    fun getAllChatReactions(message: ChatMessage)

    /**
     * Get the reactions of the target message.
     */
    fun getMessageReactions(message: ChatMessage)

    /**
     * Add a reaction to the message.
     */
    fun addReaction(message: ChatMessage, reaction: String)

    /**
     * Remove a reaction from the message.
     */
    fun removeReaction(message: ChatMessage, reaction: String)

}