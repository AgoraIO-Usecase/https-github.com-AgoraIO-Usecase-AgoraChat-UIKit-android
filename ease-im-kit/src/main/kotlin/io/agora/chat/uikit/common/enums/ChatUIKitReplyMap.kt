package io.agora.chat.uikit.common.enums

/**
 * Map the message type to [io.agora.chat.ChatMessage.Type]
 */
enum class ChatUIKitReplyMap {
    /**
     * Map to [io.agora.chat.ChatMessage.Type.TXT]
     */
    txt,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.IMAGE]
     */
    img,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.VIDEO]
     */
    video,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.LOCATION]
     */
    location,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.VOICE]
     */
    audio,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.FILE]
     */
    file,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.CMD]
     */
    cmd,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.CUSTOM]
     */
    custom,

    /**
     * Map to [io.agora.chat.ChatMessage.Type.COMBINE]
     */
    combine,
    unknown
}