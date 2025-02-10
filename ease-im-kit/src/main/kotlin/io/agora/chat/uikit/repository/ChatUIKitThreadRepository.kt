package io.agora.chat.uikit.repository

import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatThreadManager
import io.agora.chat.uikit.common.suspends.createThread
import io.agora.chat.uikit.common.suspends.destroyThread
import io.agora.chat.uikit.common.suspends.getJoinedThreadsFromServer
import io.agora.chat.uikit.common.suspends.getThreadFromServer
import io.agora.chat.uikit.common.suspends.getThreadLatestMessage
import io.agora.chat.uikit.common.suspends.getThreadMembers
import io.agora.chat.uikit.common.suspends.getThreadsFromServer
import io.agora.chat.uikit.common.suspends.joinThread
import io.agora.chat.uikit.common.suspends.leaveThread
import io.agora.chat.uikit.common.suspends.removeMemberFromThread
import io.agora.chat.uikit.common.suspends.updateThreadName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatUIKitThreadRepository(
    private val threadManager: ChatThreadManager = ChatClient.getInstance().chatThreadManager(),
) {

    suspend fun createChatThread(
        parentId:String,
        msgId:String,
        chatThreadName:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.createThread(parentId,msgId, chatThreadName)
    }

    suspend fun fetchChatThreadFromServer(
        chatThreadId:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.getThreadFromServer(chatThreadId)
    }

    suspend fun fetchChatThreadsFromServer(
        parentId: String,
        limit: Int,
        cursor: String
    )= withContext(Dispatchers.IO) {
        threadManager.getThreadsFromServer(parentId, limit, cursor)
    }

    suspend fun joinChatThread(
        chatThreadId:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.joinThread(chatThreadId)
    }


    suspend fun destroyChatThread(
        chatThreadId:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.destroyThread(chatThreadId)
    }

    suspend fun leaveChatThread(
        chatThreadId:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.leaveThread(chatThreadId)
    }

    suspend fun updateChatThreadName(
        chatThreadId:String,
        chatThreadName:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.updateThreadName(chatThreadId,chatThreadName)
    }

    suspend fun removeMemberFromChatThread(
        chatThreadId:String,
        member:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.removeMemberFromThread(chatThreadId,member)
    }

    suspend fun getChatThreadMembers(
        chatThreadId:String,
        limit:Int,
        cursor:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.getThreadMembers(chatThreadId,limit,cursor)
    }

    suspend fun getJoinedChatThreadsFromServer(
        parentId:String? = null,
        limit:Int,
        cursor:String,
    ) = withContext(Dispatchers.IO) {
        threadManager.getJoinedThreadsFromServer(parentId,limit,cursor)
    }


    suspend fun getChatThreadLatestMessage(
        chatThreadIds:List<String>,
    ) = withContext(Dispatchers.IO) {
        threadManager.getThreadLatestMessage(chatThreadIds)
    }

}