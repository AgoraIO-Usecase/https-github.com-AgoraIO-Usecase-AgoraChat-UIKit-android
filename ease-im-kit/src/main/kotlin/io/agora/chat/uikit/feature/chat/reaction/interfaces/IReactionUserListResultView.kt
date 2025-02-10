package io.agora.chat.uikit.feature.chat.reaction.interfaces

import io.agora.chat.uikit.common.interfaces.IControlDataView
import io.agora.chat.uikit.model.ChatUIKitUser

interface IReactionUserListResultView: IControlDataView {
    /**
     * Remove a reaction from the message successfully.
     */
    fun removeReactionSuccess(messageId: String)

    /**
     * Remove a reaction from the message failed.
     */
    fun removeReactionFail(messageId: String, errorCode: Int, errorMsg: String?)

    /**
     * Fetch reaction detail of the target reaction by page successfully.
     */
    fun fetchReactionDetailSuccess(messageId: String, nextCursor: String, result: List<ChatUIKitUser>)

    /**
     * Fetch reaction detail of the target reaction by page failed.
     */
    fun fetchReactionDetailFail(messageId: String, errorCode: Int, errorMsg: String?)

    /**
     * Fetch reaction detail of the target reaction by page successfully.
     */
    fun fetchMoreReactionDetailSuccess(messageId: String, nextCursor: String, result: List<ChatUIKitUser>)

    /**
     * Fetch reaction detail of the target reaction by page failed.
     */
    fun fetchMoreReactionDetailFail(messageId: String, errorCode: Int, errorMsg: String?)

}