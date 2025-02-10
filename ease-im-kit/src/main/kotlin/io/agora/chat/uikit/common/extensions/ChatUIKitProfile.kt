package io.agora.chat.uikit.common.extensions

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncUser

/**
 * Convert [ChatUIKitProfile] to [ChatUIKitUser].
 */
fun ChatUIKitProfile.toUser(): ChatUIKitUser {
    return ChatUIKitUser(userId = id, nickname = name, avatar = avatar, remark = remark)
}

/**
 * Get more information of the user from user provider.
 */
fun ChatUIKitProfile.getFullInfo(): ChatUIKitProfile {
    if (name.isNullOrEmpty() || avatar.isNullOrEmpty()) {
        ChatUIKitClient.getUserProvider()?.getSyncUser(id)?.let {
            if (name.isNullOrEmpty()) {
                name = it.name
            }
            if (avatar.isNullOrEmpty()) {
                avatar = it.avatar
            }
        }
    }
    return this
}