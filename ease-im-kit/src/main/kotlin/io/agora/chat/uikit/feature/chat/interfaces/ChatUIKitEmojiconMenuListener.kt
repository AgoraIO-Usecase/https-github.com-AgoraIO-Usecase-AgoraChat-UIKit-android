package io.agora.chat.uikit.feature.chat.interfaces

interface ChatUIKitEmojiconMenuListener {
    /**
     * on emojicon clicked
     * @param emojiIcon
     */
    fun onExpressionClicked(emojiIcon: Any?)

    /**
     * on delete image clicked
     */
    fun onDeleteImageClicked() {}

    /**
     * On send icon clicked
     */
    fun onSendIconClicked() {}
}