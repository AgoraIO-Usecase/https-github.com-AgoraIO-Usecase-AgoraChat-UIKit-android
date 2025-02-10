package io.agora.chat.uikit.viewmodel.reaction

import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.parse
import io.agora.chat.uikit.feature.chat.reaction.interfaces.IMessageReactionListResultView
import io.agora.chat.uikit.model.ChatUIKitDefaultEmojiIconData
import io.agora.chat.uikit.repository.ChatUIKitManagerRepository
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ChatUIKitReactionListViewModel: ChatUIKitBaseViewModel<IMessageReactionListResultView>(), IMessageReactionListRequest {
    private val chatRepository by lazy { ChatUIKitManagerRepository() }

    override fun fetchReactionList(message: ChatMessage) {
        viewModelScope.launch {
            flow {
                emit(chatRepository.fetchReactionList(arrayListOf(message.msgId)
                    , message.chatType
                    , if (message.chatType == ChatType.GroupChat) message.conversationId() else null))
            }
            .catchChatException { e->
                view?.fetchReactionListFail(message.msgId, e.errorCode, e.description)
            }
            .collect {
                val result = it[message.msgId]?.map { item ->
                    val reaction = item.parse()
                    if (ChatUIKitDefaultEmojiIconData.mapData.containsKey(reaction.identityCode)) {
                        reaction.icon = ChatUIKitDefaultEmojiIconData.mapData[item.reaction]!!
                    }
                    reaction
                }
                view?.fetchReactionListSuccess(message.msgId, result)
            }
        }
    }

}