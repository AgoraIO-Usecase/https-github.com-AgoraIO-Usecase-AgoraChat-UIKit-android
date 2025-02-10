package io.agora.chat.uikit.model



/**
 * The class is used to display the group information.
 * @param id The group id.
 * @param name The group name.
 * @param avatar The group avatarUrl.
 */
class ChatUIKitGroupProfile(
    id: String,
    name: String? = null,
    avatar: String? = null
) : ChatUIKitProfile(id, name, avatar)