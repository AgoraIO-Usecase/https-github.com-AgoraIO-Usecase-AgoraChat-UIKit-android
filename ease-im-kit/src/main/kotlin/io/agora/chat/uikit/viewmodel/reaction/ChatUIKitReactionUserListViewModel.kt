package io.agora.chat.uikit.viewmodel.reaction

import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.getParentId
import io.agora.chat.uikit.common.extensions.isSingleChat
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.feature.chat.reaction.interfaces.IReactionUserListResultView
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getMessageUser
import io.agora.chat.uikit.repository.ChatUIKitManagerRepository
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ChatUIKitReactionUserListViewModel: ChatUIKitBaseViewModel<IReactionUserListResultView>(), IReactionUserListRequest {
    private val chatRepository by lazy { ChatUIKitManagerRepository() }
    private var nextCursor: String? = null

    override fun removeReaction(message: ChatMessage, reaction: String?) {
        viewModelScope.launch {
            flow {
                emit(chatRepository.removeReaction(message.msgId, reaction))
            }
                .catchChatException { e->
                    view?.removeReactionFail(message.msgId, e.errorCode, e.description)
                }
                .collect {
                    view?.removeReactionSuccess(message.msgId)
                }
        }
    }

    override fun fetchReactionDetail(message: ChatMessage, reaction: String?, pageSize: Int) {
        nextCursor = null
        fetchReactionUserList(message, reaction, "", pageSize, { nextCursor, userList ->
            val result = userList.toMutableList()
            if (message.messageReaction?.filter { it.isAddedBySelf }?.find { it.reaction == reaction } != null) {
                val currentUser = ChatClient.getInstance().currentUser
                val user = if (message.isSingleChat()) {
                    ChatUIKitUser(currentUser).getMessageUser(message)
                } else {
                    val groupId = if (message.isChatThreadMessage) {
                        message.getParentId() ?: message.conversationId()
                    } else {
                        message.conversationId()
                    }
                    ChatUIKitProfile.getGroupMember(groupId, currentUser)?.toUser() ?: ChatUIKitUser(currentUser)
                }
                result.add(0, user)
            }
            view?.fetchReactionDetailSuccess(message.msgId, nextCursor, result)
        }, { errorCode, errorMsg ->
            view?.fetchReactionDetailFail(message.msgId, errorCode, errorMsg)
        })
    }

    override fun fetchMoreReactionDetail(
        message: ChatMessage,
        reaction: String?,
        cursor: String?,
        pageSize: Int
    ) {
        fetchReactionUserList(message, reaction, cursor, pageSize, { nextCursor, userList ->
            this.nextCursor = nextCursor
            view?.fetchMoreReactionDetailSuccess(message.msgId, nextCursor, userList)
        }, { errorCode, errorMsg ->
            view?.fetchMoreReactionDetailFail(message.msgId, errorCode, errorMsg)
        })
    }

    /**
     * Fetch reaction users and exclude current user.
     */
    private fun fetchReactionUserList(
        message: ChatMessage,
        reaction: String?,
        cursor: String?,
        pageSize: Int,
        onSuccess: (nextCursor: String, List<ChatUIKitUser>) -> Unit = {_,_ ->},
        onError: (Int, String) -> Unit = {_,_ ->}) {
        viewModelScope.launch {
            flow {
                emit(chatRepository.fetchReactionDetail(message.msgId, reaction, cursor, pageSize))
            }
                .catchChatException { e->
                    onError(e.errorCode, e.description)
                }
                .collect {
                    it.data?.let { data ->
                        val userList = if (data.isEmpty()) {
                            arrayListOf()
                        } else {
                            data[0].userList
                                .filter { userId ->
                                    userId != ChatClient.getInstance().currentUser
                                }.map { user ->
                                    if (message.isSingleChat()) {
                                        ChatUIKitUser(user).getMessageUser(message)
                                    } else {
                                        val groupId = if (message.isChatThreadMessage) {
                                            message.getParentId() ?: message.conversationId()
                                        } else {
                                            message.conversationId()
                                        }
                                        ChatUIKitProfile.getGroupMember(groupId, user)?.toUser() ?: ChatUIKitUser(user)
                                    }
                                }
                        }
                        onSuccess(it.cursor, userList)
                    } ?: run {
                        onError(ChatError.GENERAL_ERROR, "data is null")
                    }
                }
        }
    }

}