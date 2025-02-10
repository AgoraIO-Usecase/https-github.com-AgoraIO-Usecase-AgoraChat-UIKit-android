package io.agora.chat.uikit.model

import io.agora.chat.uikit.feature.chat.enums.ChatUIKitReactionType

data class ChatUIKitReaction (
    var identityCode: String? = null,
    var icon: Int = 0,
    var emojiText: String? = null,
    var count: Int = 0,
    var isAddedBySelf: Boolean = false,
    var type: ChatUIKitReactionType = ChatUIKitReactionType.NORMAL
)
