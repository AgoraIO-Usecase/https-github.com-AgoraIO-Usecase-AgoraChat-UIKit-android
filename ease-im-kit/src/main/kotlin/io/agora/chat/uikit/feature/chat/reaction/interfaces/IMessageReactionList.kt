package io.agora.chat.uikit.feature.chat.reaction.interfaces

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.reaction.IMessageReactionListRequest

interface IMessageReactionList {
    /**
     * Set custom ViewModel.
     */
    fun setViewModel(viewModel: IMessageReactionListRequest?)

    /**
     * Fetch reaction list of the target message.
     */
    fun fetchReactionList(message: ChatMessage)
}