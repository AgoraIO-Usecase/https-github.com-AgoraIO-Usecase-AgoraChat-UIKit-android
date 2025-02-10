package io.agora.chat.uikit.repository

import android.content.Context
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.suspends.agreeInviteAction
import io.agora.chat.uikit.common.suspends.getAllSystemMessage
import io.agora.chat.uikit.common.suspends.refuseInviteAction
import io.agora.chat.uikit.feature.invitation.helper.ChatUIKitNotificationMsgManager
import io.agora.chat.uikit.provider.fetchUsersBySuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatUIKitNotificationRepository(
    private val notificationMsgManager: ChatUIKitNotificationMsgManager = ChatUIKitNotificationMsgManager.getInstance()
) {

    companion object {
        private const val TAG = "NotificationRep"
    }

    suspend fun getAllMessage():List<ChatMessage> =
        withContext(Dispatchers.IO){
            notificationMsgManager.getAllSystemMessage()
        }

    suspend fun loadMoreMessage(startMsgId:String?,limit:Int):List<ChatMessage> =
        withContext(Dispatchers.IO){
            notificationMsgManager.loadMoreMessage(startMsgId,limit)
        }

    suspend fun fetchProfileInfo(members: List<String>?) =
        withContext(Dispatchers.IO){
            if (members.isNullOrEmpty()) {
                throw ChatException(ChatError.INVALID_PARAM, "members is null or empty.")
            }
            ChatUIKitClient.getUserProvider()?.fetchUsersBySuspend(members)
        }

    suspend fun agreeInvite(context: Context,msg:ChatMessage):Int =
        withContext(Dispatchers.IO){
            notificationMsgManager.agreeInviteAction(context,msg)
        }

    suspend fun refuseInvite(context: Context,msg:ChatMessage):Int =
        withContext(Dispatchers.IO){
            notificationMsgManager.refuseInviteAction(context,msg)
        }

    suspend fun removeInviteMsg(msg: ChatMessage):Boolean =
        withContext(Dispatchers.IO){
            notificationMsgManager.removeMessage(msg)
        }
}