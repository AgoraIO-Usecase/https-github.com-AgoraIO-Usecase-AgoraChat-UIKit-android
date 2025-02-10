package io.agora.chat.uikit.model

/**
 * An entity of group EmojiIcon
 */
data class ChatUIKitEmojiconGroupEntity(
    /**
     * Group icon
     */
    var icon: Int = 0,

    /**
     * Emojicon data
     */
    var emojiconList: List<ChatUIKitEmojicon?>? = null,

    /**
     * Group name
     */
    var name: String? = null,

    /**
     * Emojicon type
     */
    var type: ChatUIKitEmojicon.Type? = null,
)