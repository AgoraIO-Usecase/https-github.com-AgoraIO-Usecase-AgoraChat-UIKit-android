package io.agora.chat.uikit.repository

import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatManager
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageBody
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatSearchDirection
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.ChatValueCallback
import io.agora.chat.uikit.common.ChatroomManager
import io.agora.chat.uikit.common.suspends.ackConversationToRead
import io.agora.chat.uikit.common.suspends.ackGroupMessageToRead
import io.agora.chat.uikit.common.suspends.ackMessageToRead
import io.agora.chat.uikit.common.suspends.addMessageReaction
import io.agora.chat.uikit.common.suspends.fetchHistoryMessages
import io.agora.chat.uikit.common.suspends.fetchPinChatMessageFromServer
import io.agora.chat.uikit.common.suspends.fetchReactionDetailBySuspend
import io.agora.chat.uikit.common.suspends.fetchReactionListBySuspend
import io.agora.chat.uikit.common.suspends.joinChatroom
import io.agora.chat.uikit.common.suspends.leaveChatroom
import io.agora.chat.uikit.common.suspends.modifyMessage
import io.agora.chat.uikit.common.suspends.pinChatMessage
import io.agora.chat.uikit.common.suspends.recallChatMessage
import io.agora.chat.uikit.common.suspends.removeMessageReaction
import io.agora.chat.uikit.common.suspends.reportChatMessage
import io.agora.chat.uikit.common.suspends.translationChatMessage
import io.agora.chat.uikit.common.suspends.unPinChatMessage
import io.agora.chat.uikit.common.utils.isMessageIdValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatUIKitManagerRepository(
    private val chatManager: ChatManager = ChatClient.getInstance().chatManager(),
    private val chatroomManager: ChatroomManager = ChatClient.getInstance().chatroomManager()
) {

    suspend fun joinChatroom(roomId: String) =
        withContext(Dispatchers.IO) {
            chatroomManager.joinChatroom(roomId)
        }

    suspend fun leaveChatroom(roomId: String) =
        withContext(Dispatchers.IO) {
            chatroomManager.leaveChatroom(roomId)
        }

    suspend fun loadLocalMessages(conversation: ChatConversation?, startMsgId: String?
                                      , pageSize: Int, direction: ChatSearchDirection): List<ChatMessage> {
        return withContext(Dispatchers.IO) {
            if (conversation == null) {
                throw ChatException(ChatError.INVALID_PARAM, "Should first set up with conversation.")
            }
            if (!isMessageIdValid(startMsgId)) {
                throw ChatException(ChatError.MESSAGE_INVALID, "Invalid message id.")
            }
            conversation.loadMoreMsgFromDB(startMsgId, pageSize, direction).map {
                if (it.status() == ChatMessageStatus.CREATE) {
                    it.setStatus(ChatMessageStatus.FAIL)
                }
                it
            }
        }
    }

    suspend fun searchMessagesByTimestamp(conversation: ChatConversation?, timestamp: Long, pageSize: Int, direction: ChatSearchDirection) =
        withContext(Dispatchers.IO) {
            if (conversation == null) {
                throw ChatException(ChatError.INVALID_PARAM, "Should first set up with conversation.")
            }
            conversation.searchMsgFromDB(timestamp, pageSize, direction).map {
                if (it.status() == ChatMessageStatus.CREATE) {
                    it.setStatus(ChatMessageStatus.FAIL)
                }
                it
            }
        }

    suspend fun fetchRoamMessages(conversation: ChatConversation?, startMsgId: String?
                                      , pageSize: Int, direction: ChatSearchDirection) =
        withContext(Dispatchers.IO) {
            if (conversation == null) {
                throw ChatException(ChatError.INVALID_PARAM, "Should first set up with conversation.")
            }
            if (!isMessageIdValid(startMsgId)) {
                throw ChatException(ChatError.MESSAGE_INVALID, "Invalid message id.")
            }
            chatManager.fetchHistoryMessages(conversation.conversationId(), conversation.type
                , startMsgId, pageSize, direction).data
        }

    suspend fun reportMessage(tag:String,reason:String?="",msgId: String):Int =
        withContext(Dispatchers.IO) {
            chatManager.reportChatMessage(msgId,tag,reason)
        }

    suspend fun recallMessage(message: ChatMessage?) =
        withContext(Dispatchers.IO) {
            chatManager.recallChatMessage(message)
        }

    suspend fun modifyMessage(messageId: String?, messageBodyModified: ChatMessageBody?) =
        withContext(Dispatchers.IO) {
            chatManager.modifyMessage(messageId, messageBodyModified)
        }

    suspend fun addReaction(message: ChatMessage?, reaction: String?) =
        withContext(Dispatchers.IO) {
            chatManager.addMessageReaction(message?.msgId, reaction)
        }

    suspend fun removeReaction(message: ChatMessage?, reaction: String?) =
        withContext(Dispatchers.IO) {
            chatManager.removeMessageReaction(message?.msgId, reaction)
        }

    suspend fun ackConversationRead(conversationId: String?) =
        withContext(Dispatchers.IO) {
            chatManager.ackConversationToRead(conversationId)
        }

    suspend fun ackGroupMessageRead(conversationId: String?, messageId: String?, ext: String?) =
        withContext(Dispatchers.IO) {
            chatManager.ackGroupMessageToRead(conversationId, messageId, ext)
        }

    suspend fun ackMessageRead(conversationId: String?, messageId: String?) =
        withContext(Dispatchers.IO) {
            chatManager.ackMessageToRead(conversationId, messageId)
        }

    suspend fun addReaction(messageId: String?, reaction: String) =
        withContext(Dispatchers.IO) {
            if (messageId.isNullOrEmpty()) {
                throw ChatException(ChatError.MESSAGE_INVALID, "Invalid message id.")
            }
            chatManager.addMessageReaction(messageId, reaction)
        }

    suspend fun removeReaction(messageId: String?, reaction: String?) =
        withContext(Dispatchers.IO) {
            if (messageId.isNullOrEmpty()) {
                throw ChatException(ChatError.MESSAGE_INVALID, "Invalid message id.")
            }
            if (reaction.isNullOrEmpty()) {
                throw ChatException(ChatError.INVALID_PARAM, "Invalid reaction.")
            }
            chatManager.removeMessageReaction(messageId, reaction)
        }

    suspend fun fetchReactionList(messageIdList: List<String>
                                  , chatType: ChatType
                                  , groupId: String? = null) =
        withContext(Dispatchers.IO) {
            chatManager.fetchReactionListBySuspend(messageIdList, chatType, groupId)
        }

    suspend fun fetchReactionDetail(messageId: String?, reaction: String?, cursor: String?, pageSize: Int) =
        withContext(Dispatchers.IO) {
            if (messageId.isNullOrEmpty()) {
                throw ChatException(ChatError.MESSAGE_INVALID, "Invalid message id.")
            }
            if (reaction.isNullOrEmpty()) {
                throw ChatException(ChatError.INVALID_PARAM, "Invalid reaction.")
            }
            chatManager.fetchReactionDetailBySuspend(messageId, reaction, cursor, pageSize)
        }

    suspend fun translationMessage(message:ChatMessage,languages:MutableList<String>) =
        withContext(Dispatchers.IO){
            chatManager.translationChatMessage(message,languages)
        }

    suspend fun downloadCombinedMessageAttachment(message: ChatMessage?, valueCallback: ChatValueCallback<List<ChatMessage>>) =
        withContext(Dispatchers.IO) {
            if (message == null) {
                throw ChatException(ChatError.MESSAGE_INVALID, "Message cannot be null.")
            }
            if (message.type != ChatMessageType.COMBINE) {
                throw ChatException(ChatError.MESSAGE_INVALID, "Message type should be combine.")
            }
            chatManager.downloadAndParseCombineMessage(message, valueCallback)
        }

    suspend fun pinMessage(message:ChatMessage?) =
        withContext(Dispatchers.IO){
            chatManager.pinChatMessage(message?.msgId)
        }

    suspend fun unPinMessage(message:ChatMessage?) =
        withContext(Dispatchers.IO){
            chatManager.unPinChatMessage(message?.msgId)
        }

    suspend fun fetchPinMessageFromService(conversationId:String?) =
        withContext(Dispatchers.IO){
           chatManager.fetchPinChatMessageFromServer(conversationId)
        }
}