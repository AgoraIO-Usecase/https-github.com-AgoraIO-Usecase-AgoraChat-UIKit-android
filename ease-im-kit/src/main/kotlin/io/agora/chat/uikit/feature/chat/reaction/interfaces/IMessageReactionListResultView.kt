package io.agora.chat.uikit.feature.chat.reaction.interfaces

import io.agora.chat.uikit.common.interfaces.IControlDataView
import io.agora.chat.uikit.model.ChatUIKitReaction

interface IMessageReactionListResultView: IControlDataView {

    /**
     * Fetch reaction list of the target message successfully.
     */
    fun fetchReactionListSuccess(messageId: String, reactions: List<ChatUIKitReaction>?)

    /**
     * Fetch reaction list of the target message failed.
     */
    fun fetchReactionListFail(messageId: String, errorCode: Int, errorMsg: String?)

}