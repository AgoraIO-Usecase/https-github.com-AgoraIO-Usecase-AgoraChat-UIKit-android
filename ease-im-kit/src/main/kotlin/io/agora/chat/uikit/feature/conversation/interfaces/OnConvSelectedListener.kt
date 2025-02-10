package io.agora.chat.uikit.feature.conversation.interfaces

/**
 * Used to listener the conversation item selected event.
 * Used in [ChatUIKitConversation].
 */
internal interface OnConvSelectedListener {

    /**
     * Callback when the conversation item is selected.
     * @param isSelected the conversation is selected or not.
     */
    fun onConvSelected(isSelected: Boolean)
}