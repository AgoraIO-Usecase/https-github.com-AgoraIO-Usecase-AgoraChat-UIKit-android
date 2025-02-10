package io.agora.chat.uikit.viewmodel.reaction

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.IAttachView

interface IReactionUserListRequest: IAttachView {

    /**
     * Remove a reaction from the message.
     */
    fun removeReaction(message: ChatMessage, reaction: String?)

    /**
     * Fetch reaction detail of the target reaction by page.
     */
    fun fetchReactionDetail(message: ChatMessage, reaction: String?, pageSize: Int)

    /**
     * Fetch more reaction detail of the target reaction by page.
     */
    fun fetchMoreReactionDetail(message: ChatMessage, reaction: String?, cursor: String?, pageSize: Int)
}