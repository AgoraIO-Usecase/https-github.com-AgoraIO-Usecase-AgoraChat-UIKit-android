package io.agora.chat.uikit.common.extensions

import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser

/**
 * Convert [ChatUIKitUser] to [ChatUIKitProfile].
 */
fun ChatUIKitUser.toProfile(): ChatUIKitProfile {
    return ChatUIKitProfile(id = userId, name = nickname, avatar = avatar, remark = remark)
}