package io.agora.chat.uikit.feature.chat.forward.helper

import android.content.Context
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatCombineMessageBody
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.extensions.getMessageDigest
import io.agora.chat.uikit.common.extensions.getUserInfo
import io.agora.chat.uikit.common.extensions.plus
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.feature.chat.interfaces.OnMultipleSelectChangeListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import java.util.Collections

/**
 * It is a helper that helps us operate multi-select chat messages.
 */
class ChatUIKitMessageMultiSelectHelper private constructor() {
    private val dataMap: MutableMap<String, InnerData>
    private val listenerMap: MutableMap<String, OnMultipleSelectChangeListener?>
    private var context:Context? = null

    init {
        dataMap = HashMap()
        listenerMap = HashMap()
    }

    @Synchronized
    fun init(context: Context?, conversationId: String?) {
        if (context == null || conversationId.isNullOrEmpty()) {
            return
        }
        this.context = context
        if (dataMap.containsKey(context + conversationId)) {
            return
        }
        dataMap[context + conversationId] = InnerData()
    }

    /**
     * Add message to selectedMap.
     * @param context
     * @param message
     */
    fun addChatMessage(context: Context?, message: ChatMessage?) {
        if (message == null || context == null) {
            return
        }
        checkInnerData(context, message.conversationId())
        dataMap[context + message.conversationId()]?.addChatMessage(message)
        listenerMap[context + message.conversationId()]?.onMultipleSelectDataChange(context + message.conversationId())
    }

    private fun checkInnerData(context: Context, conversationId: String?) {
        if (conversationId.isNullOrEmpty()) {
            return
        }
        if (dataMap.containsKey(context + conversationId)) {
            return
        }
        init(context, conversationId)
    }

    private fun hasInnerData(context: Context, conversationId: String?): Boolean {
        if (conversationId.isNullOrEmpty()) {
            return false
        }
        return dataMap.containsKey(context + conversationId)
    }

    /**
     * Set the listener.
     */
    internal fun setOnMultipleSelectDataChangeListener(context: Context, conversationId: String?, listener: OnMultipleSelectChangeListener?) {
        if (conversationId.isNullOrEmpty()) {
            return
        }
        listenerMap[context + conversationId] = listener
    }

    /**
     * Remove message from selectedMap.
     * @param context
     * @param message
     */
    fun removeChatMessage(context: Context?, message: ChatMessage?) {
        if (message == null || context == null) {
            return
        }
        if (!hasInnerData(context, message.conversationId())) {
            return
        }
        dataMap[context + message.conversationId()]?.removeChatMessage(message)
        listenerMap[context + message.conversationId()]?.onMultipleSelectDataChange(context + message.conversationId())
    }

    /**
     * Determines whether the message is contained.
     *
     * @param context
     * @param message
     * @return
     */
    fun isContainsMessage(context: Context, message: ChatMessage?): Boolean {
        if (message == null) {
            return false
        }
        return if (!hasInnerData(context, message.conversationId())) {
            false
        } else dataMap[context + message.conversationId()]?.isContainsMessage(message) ?: false
    }

    /**
     * Get the sorted message id list.
     * @return
     * @param context
     */
    fun getSortedMessages(context: Context?, conversationId: String?): List<String>? {
        return if (context == null || conversationId.isNullOrEmpty() || !hasInnerData(context, conversationId)) {
            ArrayList<String>()
        } else dataMap[context + conversationId]?.sortedMessages
    }

    /**
     * Judge the target message list whether is empty.
     */
    fun isEmpty(context: Context?, conversationId: String?): Boolean {
        return if (context == null || conversationId.isNullOrEmpty() || !hasInnerData(context, conversationId)) {
            true
        } else dataMap[context + conversationId]?.isEmpty() ?: true
    }

    /**
     * Set is multi style.
     * @param context
     * @param isMultiStyle
     */
    fun setMultiStyle(context: Context?, conversationId: String?, isMultiStyle: Boolean) {
        if (context == null || conversationId.isNullOrEmpty() || !hasInnerData(context, conversationId)) {
            return
        }
        dataMap[context + conversationId]?.isMultiStyle = isMultiStyle
        listenerMap[context + conversationId]?.onMultipleSelectModelChange(context + conversationId, isMultiStyle)
    }

    /**
     * Get the multi style.
     * @return
     * @param context
     */
    fun isMultiStyle(context: Context?, conversationId: String?): Boolean {
        return if (context == null || conversationId.isNullOrEmpty() || !hasInnerData(context, conversationId)) {
            false
        } else dataMap[context + conversationId]?.isMultiStyle ?: false
    }

    /**
     * Clear the selectedMap.
     * @param context
     */
    fun clear(context: Context?, conversationId: String?) {
        if (context == null || conversationId.isNullOrEmpty() || !hasInnerData(context, conversationId)) {
            return
        }
        dataMap[context + conversationId]?.clear()
    }

    /**
     * Clear the selected messages.
     */
    fun clearMessages(context: Context?, conversationId: String?) {
        if (context == null || conversationId.isNullOrEmpty() || !hasInnerData(context, conversationId)) {
            return
        }
        dataMap[context + conversationId]?.clearSelectedMessages()
        listenerMap[context + conversationId]?.onMultipleSelectDataChange(context + conversationId)
    }

    private class InnerData {
        private val selectedMap: MutableMap<Long, String>
        private val toSendUserIds: MutableSet<String>
        /**
         * Get the multi style.
         * @return
         */
        /**
         * Set is multi style.
         * @param isMultiStyle
         */
        var isMultiStyle = false

        init {
            selectedMap = HashMap()
            toSendUserIds = HashSet()
        }

        /**
         * Add message to selectedMap.
         * @param message
         */
        fun addChatMessage(message: ChatMessage?) {
            if (message == null) {
                return
            }
            selectedMap[message.msgTime] = message.msgId
        }

        /**
         * Remove message from selectedMap.
         * @param message
         */
        fun removeChatMessage(message: ChatMessage?) {
            if (message == null) {
                return
            }
            selectedMap.remove(message.msgTime)
        }

        /**
         * Determines whether the message is contained.
         * @param message
         * @return
         */
        fun isContainsMessage(message: ChatMessage?): Boolean {
            return if (message == null) {
                false
            } else selectedMap.containsKey(message.msgTime)
        }

        /**
         * Judge whether the selectedMap is empty.
         */
        fun isEmpty(): Boolean {
            return selectedMap.isEmpty()
        }

        val sortedMessages: List<String>
            /**
             * Get the sorted message id list.
             * @return
             */
            get() {
                if (selectedMap.isEmpty()) {
                    return ArrayList()
                }
                val timeList: List<Long> = ArrayList(selectedMap.keys)
                Collections.sort(timeList) { o1, o2 ->
                    if (o2 - o1 > 0) {
                        -1
                    } else if (o2 == o1) {
                        0
                    } else {
                        1
                    }
                }
                val msgIdList: MutableList<String> = ArrayList()
                for (timestamp in timeList) {
                    if (selectedMap.containsKey(timestamp)) {
                        msgIdList.add(selectedMap[timestamp]!!)
                    }
                }
                return msgIdList
            }

        /**
         * Clear the selectedMap.
         */
        fun clear() {
            selectedMap.clear()
            toSendUserIds.clear()
            isMultiStyle = false
        }

        /**
         * Clear the selected messages.
         */
        fun clearSelectedMessages() {
            selectedMap.clear()
        }

        companion object {
            fun getCombineMessageSummary(messageList: List<String?>): String {
                if (messageList.isEmpty()) {
                    return ""
                }
                val subMessageList: List<String?> = if (messageList.size > 3) {
                    messageList.subList(0, 3)
                } else {
                    messageList
                }
                val summary = StringBuilder()
                var simpleName = ""
                for (i in subMessageList.indices) {
                    val msgId = subMessageList[i]
                    val message: ChatMessage =
                        ChatClient.getInstance().chatManager().getMessage(msgId)
                    val type: ChatMessageType = message.type
                    simpleName = message.body::class.java.simpleName
                    val user: ChatUIKitUser? = message.getUserInfo()?.toUser()
                    summary.append(if (user == null) message.from else user.getNickname())
                        .append(": ")
                    when (type) {
                        ChatMessageType.TXT -> summary.append((message.body as ChatTextMessageBody).message)
                        ChatMessageType.COMBINE -> summary.append((message.body as ChatCombineMessageBody).title)
                        else -> summary.append("/")
                            .append(simpleName.substring(0, simpleName.length - 4)).append("/")
                    }
                    if (i < subMessageList.size - 1) {
                        summary.append("\n")
                    }
                }
                return summary.toString()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: ChatUIKitMessageMultiSelectHelper? = null

        fun getInstance(): ChatUIKitMessageMultiSelectHelper {
            if (instance == null) {
                synchronized(ChatUIKitMessageMultiSelectHelper::class.java) {
                    if (instance == null) {
                        instance = ChatUIKitMessageMultiSelectHelper()
                    }
                }
            }
            return instance!!
        }

        fun getCombineMessageSummary(messageList: List<String>?): String {
            if (messageList.isNullOrEmpty()) {
                return ""
            }
            val subMessageList: List<String> = if (messageList.size > 3) {
                messageList.subList(0, 3)
            } else {
                messageList
            }
            val summary = StringBuilder()
            for (i in subMessageList.indices) {
                val msgId = subMessageList[i]
                val message: ChatMessage = ChatClient.getInstance().chatManager().getMessage(msgId)
                val type: ChatMessageType = message.type
                val user: ChatUIKitUser? = message.getUserInfo()?.toUser()
                summary.append(if (user == null) message.from else user.getNickname())
                    .append(": ")
                when (type) {
                    ChatMessageType.TXT -> summary.append((message.body as ChatTextMessageBody).message)
                    ChatMessageType.COMBINE -> summary.append((message.body as ChatCombineMessageBody).title)
                    else -> {
                        ChatUIKitClient.getContext()?.let {
                            summary.append(message.getMessageDigest(it))
                        }
                    }
                }
                if (i < subMessageList.size - 1) {
                    summary.append("\n")
                }
            }
            return summary.toString()
        }
    }
}
