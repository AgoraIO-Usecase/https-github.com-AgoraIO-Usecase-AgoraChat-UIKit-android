package io.agora.chat.uikit.common.enums

enum class ChatUIKitFinishReason {
    /**
     * \~english
     * Chat ended because group was destroyed.
     */
    onGroupDestroyed,

    /**
     * \~english
     * Chat ended because group was left.
     */
    onGroupLeft,

    /**
     * \~english
     * Chat ended because user was removed from group.
     */
    onGroupUserRemoved,

    /**
     * \~english
     * Chat ended because user was removed.
     */
    onContactRemoved,

}