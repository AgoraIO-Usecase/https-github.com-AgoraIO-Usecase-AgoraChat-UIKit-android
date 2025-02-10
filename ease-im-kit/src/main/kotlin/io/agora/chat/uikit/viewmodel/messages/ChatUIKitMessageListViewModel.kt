package io.agora.chat.uikit.viewmodel.messages

import android.util.Log
import androidx.lifecycle.viewModelScope
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.viewmodel.ChatUIKitBaseViewModel
import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatSearchDirection
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.suspends.deleteMessage
import io.agora.chat.uikit.common.utils.isMessageIdValid
import io.agora.chat.uikit.feature.chat.interfaces.IChatMessageListResultView
import io.agora.chat.uikit.repository.ChatUIKitManagerRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

open class ChatUIKitMessageListViewModel(
    override var pageSize: Int = 10
): ChatUIKitBaseViewModel<IChatMessageListResultView>(), IChatMessageListRequest {

    private var _conversation: ChatConversation? = null
    private val chatRepository by lazy { ChatUIKitManagerRepository() }

    override var messageCursor: String? = null

    override fun setupWithConversation(conversation: ChatConversation?) {
        _conversation = conversation
        _conversation?.let {
            // Chat thread conversation should clear cache data.
            if (it.isChatThread) it.clear()
        }
    }

    override fun joinChatroom(roomId: String) {
        viewModelScope.launch {
            flow {
                emit(chatRepository.joinChatroom(roomId))
            }
            .catchChatException { e ->
                view?.joinChatRoomFail(e.errorCode, e.description)
            }
            .collect {
                view?.joinChatRoomSuccess(it)
            }
        }
    }

    override fun leaveChatroom(roomId: String) {
        viewModelScope.launch {
            flow {
                emit(chatRepository.leaveChatroom(roomId))
            }
            .catchChatException { e ->
                view?.leaveChatRoomFail(e.errorCode, e.description)
            }
            .collect {
                view?.leaveChatRoomSuccess()
            }
        }
    }

    override fun getAllCacheMessages() {
        viewModelScope.launch {
            if (_conversation == null) {
                view?.getAllMessagesFail(ChatError.INVALID_PARAM, "The conversation is null.")
                return@launch
            }
            _conversation?.run {
                if (ChatUIKitClient.getConfig()?.chatConfig?.showUnreadNotificationInChat == false) {
                    // Mark all messages as read. App may be crashed, so mark all messages as read when get all messages.
                    markAllMessagesAsRead()
                }
                view?.getAllMessagesSuccess(allMessages)
            }
        }
    }

    override fun loadLocalMessages(direction: ChatSearchDirection) {
        viewModelScope.launch {
            messageCursor = ""
            flow {
                emit(chatRepository.loadLocalMessages(_conversation, messageCursor, pageSize, direction))
            }
            .catchChatException { e ->
                view?.loadLocalMessagesFail(e.errorCode, e.description)
            }
            .collect {
                messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                view?.loadLocalMessagesSuccess(it)
            }
        }
    }

    override fun loadMoreLocalMessages(
        startMsgId: String?,
        direction: ChatSearchDirection
    ) {
        viewModelScope.launch {
            val startMessageId = if (startMsgId.isNullOrEmpty()) {
                if (messageCursor.isNullOrEmpty()) {
                    _conversation?.allMessages?.getFirstMessageId() ?: messageCursor
                } else messageCursor
            } else startMsgId
            flow {
                emit(chatRepository.loadLocalMessages(_conversation, startMessageId, pageSize, direction))
            }
            .catchChatException { e ->
                view?.loadMoreLocalMessagesFail(e.errorCode, e.description)
            }
            .collect {
                messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                view?.loadMoreLocalMessagesSuccess(it)
            }
        }
    }

    override fun fetchRoamMessages(direction: ChatSearchDirection) {
        viewModelScope.launch {
            messageCursor = ""
            flow {
                emit(chatRepository.fetchRoamMessages(_conversation, messageCursor, pageSize, direction))
            }
            .catchChatException { e ->
                view?.fetchRoamMessagesFail(e.errorCode, e.description)
            }
            .collect {
                messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                _conversation?.loadMoreMsgFromDB("",pageSize, direction)
                view?.fetchRoamMessagesSuccess(it)
            }
        }
    }

    override fun fetchMoreRoamMessages(
        startMsgId: String?,
        direction: ChatSearchDirection
    ) {
        viewModelScope.launch {
            val startMessageId = if (startMsgId.isNullOrEmpty()) messageCursor else startMsgId
            flow {
                emit(chatRepository.fetchRoamMessages(_conversation, startMessageId, pageSize, direction))
            }
            .catchChatException { e ->
                view?.fetchMoreRoamMessagesFail(e.errorCode, e.description)
            }
            .collect {
                messageCursor = if (it.firstMessageId()?.isEmpty() == true) messageCursor else it.firstMessageId()
                _conversation?.loadMoreMsgFromDB(startMessageId,pageSize,direction)
                view?.fetchMoreRoamMessagesSuccess(it)
            }
        }
    }

    override fun loadLocalHistoryMessages(
        startMsgId: String?,
        direction: ChatSearchDirection,
        isFirst: Boolean
    ) {
        viewModelScope.launch {
            if (isFirst) {
                if (_conversation == null) {
                    view?.loadLocalHistoryMessagesFail(ChatError.INVALID_PARAM, "Should first set up with conversation.")
                    return@launch
                }
                if (!isMessageIdValid(startMsgId)) {
                    view?.loadLocalHistoryMessagesFail(ChatError.MESSAGE_INVALID, "Invalid message id.")
                    return@launch
                }
                val message = ChatClient.getInstance().chatManager().getMessage(startMsgId)
                if (message == null) {
                    view?.loadLocalHistoryMessagesFail(ChatError.MESSAGE_INVALID, "Not found the message: $startMsgId.")
                    return@launch
                }
                flow {
                    emit(chatRepository.searchMessagesByTimestamp(_conversation, message.msgTime - 1, pageSize, direction))
                }
            } else {
                flow {
                    emit(chatRepository.loadLocalMessages(_conversation, startMsgId, pageSize, direction))
                }
            }
            .catchChatException { e ->
                view?.loadLocalHistoryMessagesFail(e.errorCode, e.description)
            }
            .collect {
                view?.loadLocalHistoryMessagesSuccess(it, direction)
            }
        }
    }

    override fun loadMoreRetrievalsMessages(msgId: String?, pageSize: Int) {
        viewModelScope.launch {
            flow {
                emit(chatRepository.loadLocalMessages(_conversation, msgId, pageSize, ChatSearchDirection.UP))
            }
            .catchChatException {}
            .collect {
                view?.loadMoreRetrievalsMessagesSuccess(it)
            }
        }
    }

    override fun removeMessage(message: ChatMessage?, isDeleteServerMessage: Boolean) {
        if (message == null) {
            ChatLog.e(TAG, "removeMessage: The message is null.")
            view?.removeMessageFail(ChatError.MESSAGE_INVALID, "The message is null.")
            return
        }
        if (_conversation == null) {
            ChatLog.e(TAG, "removeMessage: The conversation is null.")
            view?.removeMessageFail(ChatError.INVALID_PARAM, "The conversation is null.")
            return
        }
        viewModelScope.launch {
            if (isDeleteServerMessage) {
                flow {
                    emit(_conversation?.deleteMessage(mutableListOf(message.msgId)))
                }
                .catchChatException { e ->
                    view?.removeMessageFail(e.errorCode, e.description)
                }
                .collect {
                    _conversation?.removeMessage(message.msgId)
                    view?.removeMessageSuccess(message)
                }
            } else {
                _conversation?.removeMessage(message.msgId)
                view?.removeMessageSuccess(message)
            }
        }
    }

    companion object {
        private val TAG = ChatUIKitMessageListViewModel::class.java.simpleName
    }

    private fun List<ChatMessage>.firstMessageId(): String? {
        return if (isEmpty()) "" else first().msgId
    }

    private fun List<ChatMessage>.getFirstMessageId(): String? {
        return if (isEmpty()) "" else first().msgId
    }
}