package io.agora.chat.uikit.common.extensions

import io.agora.chat.uikit.common.ChatUserInfo
import io.agora.chat.uikit.model.ChatUIKitUser

/**
 * It is a file used to convert Chat SDK classes into easeui SDK classes.
 */

/**
 * Convert [ChatUserInfo] to [ChatUIKitUser].
 */
internal fun ChatUserInfo.parse() = ChatUIKitUser(
    userId = userId,
    nickname = nickname,
    avatar = avatarUrl,
    email = email,
    gender = gender,
    sign = signature,
    birth = birth,
    ext = ext,
)