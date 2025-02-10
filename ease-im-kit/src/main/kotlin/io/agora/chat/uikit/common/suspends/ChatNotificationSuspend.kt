package io.agora.chat.uikit.common.suspends

import android.content.Context
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatCallback
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.feature.invitation.helper.ChatUIKitNotificationMsgManager
import io.agora.chat.uikit.feature.invitation.enums.InviteMessageStatus
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend fun ChatUIKitNotificationMsgManager.getAllSystemMessage():List<ChatMessage>{
    return suspendCoroutine{ continuation ->
        val allMessage = getAllNotifyMessage()
        if (allMessage.isNotEmpty()){
            continuation.resume(allMessage)
        }else{
            continuation.resume(mutableListOf())
        }
    }
}

suspend fun ChatUIKitNotificationMsgManager.loadMoreMessage(startMsgId:String,limit:Int):List<ChatMessage>{
    return suspendCoroutine{ continuation ->
        val allMessage = loadMoreMessage(startMsgId,limit)
        if (allMessage.isNotEmpty()){
            continuation.resume(allMessage)
        }else{
            continuation.resume(mutableListOf())
        }
    }
}


suspend fun ChatUIKitNotificationMsgManager.agreeInviteAction(context: Context, msg:ChatMessage):Int{
    return suspendCoroutine{ continuation ->
        try {
            val statusParams = msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_STATUS)
            val status = InviteMessageStatus.valueOf(statusParams)
            var message = ""
            when(status){
                InviteMessageStatus.BEINVITEED -> {
                    message = context.resources.getString(R.string.uikit_invitation_reason)
                    ChatClient.getInstance().contactManager().asyncAcceptInvitation(
                        msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM),
                        object : ChatCallback{
                            override fun onSuccess() {
                                continuation.resume(ChatError.EM_NO_ERROR)
                            }

                            override fun onError(code: Int, error: String?) {
                                continuation.resumeWithException(ChatException(code,error))
                            }
                        })
                }
                else -> {}
            }

            msg.setAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.AGREED.name)
            msg.setAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_REASON, message)
            val body = ChatTextMessageBody(message)
            msg.body = body
            ChatUIKitNotificationMsgManager.getInstance().updateMessage(msg)
        }catch (e:ChatException){
            e.printStackTrace()
            msg.setAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_EXPIRED, R.string.system_msg_expired)
        }

    }
}

suspend fun ChatUIKitNotificationMsgManager.refuseInviteAction(context: Context, msg:ChatMessage):Int{
    return suspendCoroutine{ continuation ->
        try {
            val statusParams = msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_STATUS)
            val status = InviteMessageStatus.valueOf(statusParams)
            var message = ""
            when(status) {
                InviteMessageStatus.BEINVITEED -> {
                    message = context.resources.getString(R.string.system_decline_invite)
                    ChatClient.getInstance().contactManager().asyncDeclineInvitation(
                        msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM),
                        object : ChatCallback{
                            override fun onSuccess() {
                                continuation.resume(ChatError.EM_NO_ERROR)
                            }

                            override fun onError(code: Int, error: String?) {
                                continuation.resumeWithException(ChatException(code,error))
                            }
                        }
                    )
                }
                else -> {}
            }
            msg.setAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.AGREED.name)
            msg.setAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_REASON, message)
            val body = ChatTextMessageBody(message)
            msg.body = body
            ChatUIKitNotificationMsgManager.getInstance().updateMessage(msg)
        }catch (e:ChatException){
            e.printStackTrace()
            msg.setAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_EXPIRED, R.string.system_msg_expired)
        }
    }
}