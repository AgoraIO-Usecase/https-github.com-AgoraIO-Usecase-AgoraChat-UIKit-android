package io.agora.chat.uikit.common.helper

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatThreadEvent
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.ChatUIKitConstant

object ChatUIKitThreadNotifyHelper {
    fun createThreadCreatedMsg(event:ChatThreadEvent?){
        event?.let {
            var content = ""
            val message = ChatMessage.createReceiveMessage(ChatMessageType.TXT)
            message.chatType = ChatType.GroupChat
            message.from = it.from
            message.to = it.chatThread.parentId
            message.msgId = it.chatThread.chatThreadId
            message.setAttribute(ChatUIKitConstant.THREAD_NOTIFICATION_TYPE,true)
            message.setAttribute(ChatUIKitConstant.THREAD_TOPIC_MESSAGE_ID,it.chatThread.messageId)

            ChatUIKitClient.getContext()?.let { con->
                content = con.getString(R.string.uikit_thread_notify_content,"",it.chatThread.chatThreadName)
            }
            message.body = ChatTextMessageBody(content)
            message.setStatus(ChatMessageStatus.SUCCESS)
            ChatClient.getInstance().chatManager().saveMessage(message)
        }
    }

    fun removeCreateThreadNotify(conversationId:String,threadId:String){
        ChatClient.getInstance().chatManager().getConversation(conversationId)?.let {
            it.removeMessage(threadId)
        }
    }
}