package io.agora.chat.uikit.feature.chat.reaction.interfaces

import io.agora.chat.uikit.common.interfaces.IControlDataView
import io.agora.chat.uikit.model.ChatUIKitReaction

interface IChatReactionResultView: IControlDataView {

    /**
     * Get default reactions in message menu dialog successfully.
     */
    fun getDefaultReactionsSuccess(reactions: List<ChatUIKitReaction>) {}

    /**
     * Get all chat default reactions successfully.
     */
    fun getAllChatReactionsSuccess(reactions: List<ChatUIKitReaction>) {}

    /**
     * Get message reactions successfully.
     */
    fun getMessageReactionSuccess(reactions: List<ChatUIKitReaction>) {}

    /**
     * Add a reaction to the message successfully.
     */
    fun addReactionSuccess(messageId: String) {}

    /**
     * Add a reaction to the message failed.
     */
    fun addReactionFail(messageId: String, errorCode: Int, errorMsg: String?) {}

    /**
     * Remove a reaction from the message successfully.
     */
    fun removeReactionSuccess(messageId: String) {}

    /**
     * Remove a reaction from the message failed.
     */
    fun removeReactionFail(messageId: String, errorCode: Int, errorMsg: String?) {}

}