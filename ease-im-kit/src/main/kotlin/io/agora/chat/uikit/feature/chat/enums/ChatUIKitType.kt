package io.agora.chat.uikit.feature.chat.enums

import io.agora.chat.uikit.common.ChatConversationType

enum class ChatUIKitType {
    /**
     * Single chat type.
     */
    SINGLE_CHAT,

    /**
     * Group chat type.
     */
    GROUP_CHAT,

    /**
     * Chat room type.
     */
    CHATROOM
}

fun ChatUIKitType.getConversationType(): ChatConversationType {
    return when (this) {
        ChatUIKitType.GROUP_CHAT -> ChatConversationType.GroupChat
        ChatUIKitType.CHATROOM -> ChatConversationType.ChatRoom
        else -> ChatConversationType.Chat
    }
}