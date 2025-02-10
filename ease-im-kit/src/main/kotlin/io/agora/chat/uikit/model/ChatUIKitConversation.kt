package io.agora.chat.uikit.model

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import java.io.Serializable

/**
 * The class is used to display the conversation information.
 * @param conversationId The conversation id.
 * @param conversationType The conversation type.
 * @param unreadMsgCount The unread message count.
 * @param lastMessage The last message.
 * @param timestamp The last message timestamp.
 * @param isPinned The conversation is pinned or not.
 * @param pinnedTime The pinned time.
 */
data class ChatUIKitConversation(
    val conversationId: String,
    val conversationType: ChatConversationType,
    val unreadMsgCount: Int,
    val lastMessage: ChatMessage?,
    val timestamp: Long,
    val isPinned: Boolean,
    val pinnedTime: Long,
): Serializable, Comparable<ChatUIKitConversation> {
    private var isSelected: Boolean = false
    private var onSelectedListener: ((Boolean) -> Unit)? = null

    /**
     * Get whether the conversation is silent.
     * @return The result of whether the conversation is silent.
     */
    fun isSilent(): Boolean {
        return ChatUIKitClient.getCache().getMutedConversationList().containsKey(conversationId)
    }

    /**
     * Set whether the conversation is silent. It will update to local db.
     * @param isSilent
     */
    fun setSilent(isSilent: Boolean) {
        if (isSilent) {
            ChatUIKitClient.getCache().setMutedConversation(conversationId)
        } else {
            ChatUIKitClient.getCache().removeMutedConversation(conversationId)
        }
    }

    fun isSelected(): Boolean {
        return isSelected
    }

    fun setSelected(isSelected: Boolean) {
        this.isSelected = isSelected
        onSelectedListener?.invoke(isSelected)
    }

    /**
     * Set the listener of conversation selected event.
     */
    internal fun setOnSelectedListener(onSelected: (Boolean) -> Unit) {
        this.onSelectedListener = onSelected
    }

    override fun compareTo(other: ChatUIKitConversation): Int {
        return if (other.isPinned && !isPinned) {
            1
        } else if (!other.isPinned && isPinned) {
            -1
        } else {
            (other.timestamp - timestamp).toInt()
        }
    }
}

/**
 * Get the bean of [ChatConversation] by conversation id.
 */
fun ChatUIKitConversation.chatConversation(): ChatConversation? = ChatClient.getInstance().chatManager().getConversation(conversationId)

/**
 * Get chat type from conversation type.
 */
fun ChatUIKitConversation.getChatType(): ChatUIKitType {
    val conversation = chatConversation()
    return if (chatConversation()?.isGroup == true) {
        if (conversation?.type === ChatConversationType.ChatRoom) {
            ChatUIKitType.CHATROOM
        } else {
            ChatUIKitType.GROUP_CHAT
        }
    } else {
        ChatUIKitType.SINGLE_CHAT
    }
}

fun ChatUIKitConversation.isGroupChat() = conversationType == ChatConversationType.GroupChat

fun ChatUIKitConversation.isChatRoom() = conversationType == ChatConversationType.ChatRoom

fun ChatUIKitConversation.isChat() = conversationType == ChatConversationType.Chat
