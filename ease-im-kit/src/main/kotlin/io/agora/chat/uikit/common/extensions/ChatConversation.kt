package io.agora.chat.uikit.common.extensions

import io.agora.chat.uikit.common.ChatConversation
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.model.ChatUIKitConversation

/**
 * Convert [ChatConversation] to [ChatUIKitConversation].
 */
fun ChatConversation.parse() = ChatUIKitConversation(
    conversationId = conversationId(),
    conversationType = type,
    unreadMsgCount = unreadMsgCount,
    lastMessage = lastMessage,
    timestamp = lastMessage?.msgTime ?: 0,
    isPinned = isPinned,
    pinnedTime = pinnedTime
)

/**
 * Whether the conversation is group chat.
 */
val ChatConversation.isGroupChat:Boolean
    get() = type == ChatConversationType.GroupChat

/**
 * Whether the conversation is chat room.
 */
val ChatConversation.isChatroom:Boolean
    get() = type == ChatConversationType.ChatRoom