package io.agora.chat.uikit.model

data class ChatUIKitReactionEmojiconEntity(
    var emojicon: ChatUIKitEmojicon? = null,
    var count: Int = 0,
    var userList: List<String>? = null,
    var isAddedBySelf: Boolean = false
)