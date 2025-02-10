package io.agora.chat.uikit.common.extensions

import io.agora.chat.uikit.common.ChatMessageReaction
import io.agora.chat.uikit.model.ChatUIKitReaction

fun ChatMessageReaction.parse(): ChatUIKitReaction {
    return ChatUIKitReaction(
        identityCode = reaction,
        emojiText = reaction,
        count = userCount,
        isAddedBySelf = isAddedBySelf
    )
}