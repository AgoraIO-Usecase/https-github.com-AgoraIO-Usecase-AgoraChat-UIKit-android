package io.agora.chat.uikit.viewmodel.reaction

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.IAttachView

interface IMessageReactionListRequest: IAttachView {

    /**
     * Fetch reaction list of the target message.
     */
    fun fetchReactionList(message: ChatMessage)
}