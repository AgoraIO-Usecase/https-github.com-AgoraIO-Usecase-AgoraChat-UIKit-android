package io.agora.chat.uikit.common.helper

import android.content.Context
import android.content.SharedPreferences
import android.util.LruCache
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatCursorResult
import io.agora.chat.uikit.common.ChatGroupReadAck
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.ChatValueCallback
import java.lang.ref.WeakReference

/**
 * For ding-type message handle.
 *
 *
 * Created by zhangsong on 18-1-22.
 */
class ChatUIKitDingMessageHelper internal constructor(context: Context) {
    /**
     * To notify if a ding-type msg acked users updated.
     */
    interface IAckUserUpdateListener {
        fun onUpdate(list: List<String>?)
    }

    // Map<msgId, IAckUserUpdateListener>
    private val listenerMap: MutableMap<String, WeakReference<IAckUserUpdateListener>>

    // Package level interface, for test.
    // LruCache<conversationId, LruCache<msgId, List<username>>>
    private val dataCache: LruCache<String, LruCache<String, MutableList<String>>?> by lazy { LruCache(
        CACHE_SIZE_CONVERSATION
    ) }

    // Package level interface, for test.
    private val dataPrefs: SharedPreferences
    private val prefsEditor: SharedPreferences.Editor

    /**
     * Set a ack-user update listener.
     *
     * @param msg
     * @param listener Nullable, if this is null, will remove this msg's listener from listener map.
     */
    fun setUserUpdateListener(msg: ChatMessage, listener: IAckUserUpdateListener?) {
        if (!validateMessage(msg)) {
            return
        }
        val key = msg.msgId
        if (listener == null) {
            listenerMap.remove(key)
        } else {
            listenerMap[key] = WeakReference(listener)
        }
    }

    /**
     * Contains ding msg and ding-ack msg.
     *
     * @param message
     * @return
     */
    fun isDingMessage(message: ChatMessage): Boolean {
        return message.isNeedGroupAck
    }

    /**
     * Create a ding-type message.
     */
    fun createDingMessage(to: String?, content: String?): ChatMessage {
        val message = ChatMessage.createTextSendMessage(content, to)
        message.setIsNeedGroupAck(true)
        return message
    }

    fun sendAckMessage(message: ChatMessage) {
        if (!validateMessage(message)) {
            return
        }
        if (message.isAcked) {
            return
        }

        // May a user login from multiple devices, so do not need to send the ack msg.
        if (ChatClient.getInstance().currentUser.equals(message.from, ignoreCase = true)) {
            return
        }
        try {
            if (message.isNeedGroupAck && !message.isUnread) {
                val to = message.conversationId() // do not user getFrom() here
                val msgId = message.msgId
                ChatClient.getInstance().chatManager()
                    .ackGroupMessageRead(to, msgId, (message.body as ChatTextMessageBody).message)
                message.isUnread = false
                ChatLog.i(TAG, "Send the group ack cmd-type message.")
            }
        } catch (e: Exception) {
            ChatLog.d(TAG, e.message)
        }
    }

    fun fetchGroupReadAck(msg: ChatMessage) {
        // fetch from server
        val msgId = msg.msgId
        ChatClient.getInstance().chatManager().asyncFetchGroupReadAcks(
            msgId,
            20,
            "",
            object : ChatValueCallback<ChatCursorResult<ChatGroupReadAck>> {
                override fun onSuccess(value: ChatCursorResult<ChatGroupReadAck>) {
                    ChatLog.d(TAG, "asyncFetchGroupReadAcks success")
                    if (value.data != null && value.data.size > 0) {
                        val acks = value.data
                        for (c in acks) {
                            handleGroupReadAck(c)
                        }
                    } else {
                        ChatLog.d(TAG, "no data")
                    }
                }

                override fun onError(error: Int, errorMsg: String) {
                    ChatLog.d(TAG, "asyncFetchGroupReadAcks fail: $error")
                }
            })
    }

    /**
     * To handle ding-type ack msg.
     * Need store native for reload when app restart.
     *
     * @param ack The ding-type message.
     */
    fun handleGroupReadAck(ack: ChatGroupReadAck?) {
        if (ack == null) return
        ChatLog.d(TAG, "handle group read ack: " + ack.msgId)
        val username = ack.from
        val msgId = ack.msgId
        val conversationId = ChatClient.getInstance().chatManager().getMessage(msgId).conversationId()

        // Get a message map.
        var msgCache = dataCache[conversationId]
        if (msgCache == null) {
            msgCache = createCache()
            dataCache.put(conversationId, msgCache)
        }

        // Get the msg ack-user list.
        var userList = msgCache?.get(msgId)
        if (userList == null) {
            userList = ArrayList()
            msgCache?.put(msgId, userList)
        }
        if (!userList.contains(username)) {
            userList.add(username)
        }

        // Notify ack-user list changed.
        val listenerRefs = listenerMap[msgId]
        if (listenerRefs != null) {
            listenerRefs.get()!!.onUpdate(userList)
        }

        // Store in preferences.
        val key = generateKey(conversationId, msgId)
        val set: MutableSet<String> = HashSet()
        set.addAll(userList)
        prefsEditor.putStringSet(key, set).commit()
    }

    /**
     * Delete the native stored acked users if this message deleted.
     *
     * @param message
     */
    fun delete(message: ChatMessage) {
        if (!validateMessage(message)) {
            return
        }
        val conversationId = message.to
        val msgId = message.msgId

        // Remove the memory data.
        val msgCache = dataCache[conversationId]
        msgCache?.remove(msgId)

        // Delete the data in preferences.
        val key = generateKey(conversationId, msgId)
        if (dataPrefs.contains(key)) {
            prefsEditor.remove(key).commit()
        }
    }

    /**
     * Delete the native stored acked users if this conversation deleted.
     *
     * @param conversation
     */
    fun delete(conversation: ChatConversation) {
        if (!conversation.isGroup) {
            return
        }

        // Remove the memory data.
        val conversationId = conversation.conversationId()
        dataCache.remove(conversationId)

        // Remove the preferences data.
        val keyPrefix = generateKey(conversationId, "")
        val prefsMap = dataPrefs.all
        val keySet: Set<String> = prefsMap.keys
        for (key in keySet) {
            if (key.startsWith(keyPrefix)) {
                prefsEditor.remove(key)
            }
        }
        prefsEditor.commit()
    }

    // Package level interface, for test.
    fun getListenerMap(): Map<String, WeakReference<IAckUserUpdateListener>> {
        return listenerMap
    }

    init {
//        dataCache = object : LruCache<String?, LruCache<String?, List<String?>?>?>(
//            CACHE_SIZE_CONVERSATION
//        ) {
//            override fun sizeOf(key: String?, value: LruCache<String?, List<String?>?>?): Int {
//                return 1
//            }
//        }
        listenerMap = HashMap()
        dataPrefs = context.getSharedPreferences(NAME_PREFS, Context.MODE_PRIVATE)
        prefsEditor = dataPrefs.edit()
    }

    /**
     * Generate a key for SharedPreferences to store
     *
     * @param conversationId Group chat conversation id.
     * @param originalMsgId  The id of the ding-type message.
     * @return
     */
    fun generateKey(conversationId: String, originalMsgId: String): String {
        return "$conversationId|$originalMsgId"
    }

    private fun validateMessage(message: ChatMessage?): Boolean {
        if (message == null) {
            return false
        }
        if (message.chatType != ChatType.GroupChat) {
            return false
        }
        return if (!isDingMessage(message)) {
            false
        } else true
    }

    private fun createCache(): LruCache<String, MutableList<String>>? {
        return LruCache<String, MutableList<String>>(CACHE_SIZE_MESSAGE)
    }

    companion object {
        private const val TAG = "ChatUIKitDingMessageHelper"

        // Cache 5 conversations in memory at most.
        const val CACHE_SIZE_CONVERSATION = 5

        // Cache 10 ding-type messages every conversation at most.
        const val CACHE_SIZE_MESSAGE = 10
        const val KEY_DING = "EMDingMessage"
        const val KEY_DING_ACK = "EMDingMessageAck"
        const val KEY_CONVERSATION_ID = "EMConversationID"
        private const val NAME_PREFS = "group-ack-data-prefs"
        private var instance: ChatUIKitDingMessageHelper? = null
        fun get(): ChatUIKitDingMessageHelper {
            if (instance == null) {
                synchronized(ChatUIKitDingMessageHelper::class.java) {
                    if (instance == null) {
                        instance = ChatUIKitDingMessageHelper(ChatClient.getInstance().context)
                    }
                }
            }
            return instance!!
        }
    }
}