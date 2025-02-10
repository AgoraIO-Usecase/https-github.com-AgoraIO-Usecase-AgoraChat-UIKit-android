package io.agora.chat.uikit.feature.invitation.helper

import android.text.TextUtils
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.model.ChatUIKitEvent
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID


class ChatUIKitNotificationMsgManager {

    companion object {

        private var instance: ChatUIKitNotificationMsgManager? = null
        fun getInstance(): ChatUIKitNotificationMsgManager {
            if (instance == null) {
                synchronized(ChatUIKitNotificationMsgManager::class.java) {
                    if (instance == null) {
                        instance = ChatUIKitNotificationMsgManager()
                    }
                }
            }
            return instance!!
        }
    }

    fun createMessage(message: String?, ext: Map<String, Any>): ChatMessage {
        val emMessage: ChatMessage = ChatMessage.createReceiveMessage(ChatMessageType.TXT)
        emMessage.from = ChatUIKitConstant.DEFAULT_SYSTEM_MESSAGE_ID
        emMessage.msgId = UUID.randomUUID().toString()
        emMessage.setStatus(ChatMessageStatus.SUCCESS)
        emMessage.addBody(ChatTextMessageBody(message))
        if (ext.isNotEmpty()) {
            val iterator = ext.keys.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val value = ext[key]
                value?.let {
                    putObject(emMessage, key, it)
                }
            }
        }
        emMessage.isUnread = true
        ChatClient.getInstance().chatManager().saveMessage(emMessage)
        return emMessage
    }

    private fun putObject(message: ChatMessage, key: String, value: Any) {
        if (TextUtils.isEmpty(key)) {
            return
        }
        when (value) {
            is String -> {
                message.setAttribute(key, value)
            }

            is Byte -> {
                message.setAttribute(key, (value as Int))
            }

            is Char -> {
                message.setAttribute(key, (value as Int))
            }

            is Short -> {
                message.setAttribute(key, (value as Int))
            }

            is Int -> {
                message.setAttribute(key, value)
            }

            is Boolean -> {
                message.setAttribute(key, value)
            }

            is Long -> {
                message.setAttribute(key, value)
            }

            is Float -> {
                val `object` = JSONObject()
                try {
                    `object`.put(key, value)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                message.setAttribute(key, `object`)
            }

            is Double -> {
                val `object` = JSONObject()
                try {
                    `object`.put(key, value)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                message.setAttribute(key, `object`)
            }

            is JSONObject -> {
                message.setAttribute(key, value)
            }

            is JSONArray -> {
                message.setAttribute(key, value)
            }

            else -> {
                val `object` = JSONObject()
                try {
                    `object`.put(key, value)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                message.setAttribute(key, `object`)
            }
        }
    }

    fun createMsgExt(): MutableMap<String, Any> {
        return HashMap()
    }

    /**
     * Get latest message
     * @param con
     * @return
     */
    fun getLastMessageByConversation(con: ChatConversation?): ChatMessage? {
        return con?.lastMessage
    }


    /**
     * Get notification conversation
     * @return
     */
    fun getConversation():ChatConversation{
        return getSystemConversation(true)
    }

    /**
     * Get notification conversation
     * @param createIfNotExists
     * @return
     */
    fun getSystemConversation(createIfNotExists:Boolean):ChatConversation{
        return ChatClient.getInstance().chatManager().getConversation(
            ChatUIKitConstant.DEFAULT_SYSTEM_MESSAGE_ID,
            ChatConversationType.Chat,
            createIfNotExists
        )
    }

    /**
     * Get all messages of notification
     * @return
     */
    fun getAllNotifyMessage():List<ChatMessage>{
        return getConversation().allMessages
    }

    /**
     * load more message of notification
     * @return
     */
    fun loadMoreMessage(startMsgId:String?="",limit:Int):List<ChatMessage>{
        return getConversation().loadMoreMsgFromDB(startMsgId,limit)
    }


    /**
     * Check whether is a notification message
     * @param message
     * @return
     */
    fun isNotificationMessage(message: ChatMessage): Boolean {
        return (message.type === ChatMessageType.TXT
                && TextUtils.equals(message.from, ChatUIKitConstant.DEFAULT_SYSTEM_MESSAGE_ID))
    }


    /**
     * Check whether is a notification conversation
     * @param conversation
     * @return
     */
    fun isNotificationConversation(conversation: ChatConversation): Boolean {
        return (conversation.type === ChatConversationType.Chat
            && TextUtils.equals(conversation.conversationId(), ChatUIKitConstant.DEFAULT_SYSTEM_MESSAGE_ID)
        )
    }


    /**
     * Get the message content
     * @param message
     * @return
     */
    fun getMessageContent(message: ChatMessage): String? {
        return if (message.body is ChatTextMessageBody) {
            (message.body as ChatTextMessageBody).message
        } else ""
    }

    /**
     * Update notification message
     * @param message
     * @return
     */
    fun updateMessage(message: ChatMessage?): Boolean {
        if (message == null || !isNotificationMessage(message)) {
            return false
        }
        ChatClient.getInstance().chatManager().updateMessage(message)
        return true
    }

    /**
     * Remove notification message
     * @param message
     * @return
     */
    fun removeMessage(message: ChatMessage?): Boolean {
        if (message == null || !isNotificationMessage(message)) {
            return false
        }
        val conversation: ChatConversation = ChatClient.getInstance().chatManager()
            .getConversation(ChatUIKitConstant.DEFAULT_SYSTEM_MESSAGE_ID)
        conversation.removeMessage(message.msgId)
        return true
    }

    /**
     * Make all message in notification conversation as read
     */
    fun markAllMessagesAsRead() {
        val conversation: ChatConversation = ChatClient.getInstance().chatManager()
            .getConversation(ChatUIKitConstant.DEFAULT_SYSTEM_MESSAGE_ID)
        conversation.markAllMessagesAsRead()

        val context = ChatUIKitClient.getContext()
        context?.let {
            it.mainScope().launch {
                ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).post(this,
                    ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.NOTIFY)
                )
            }
        }
    }
}