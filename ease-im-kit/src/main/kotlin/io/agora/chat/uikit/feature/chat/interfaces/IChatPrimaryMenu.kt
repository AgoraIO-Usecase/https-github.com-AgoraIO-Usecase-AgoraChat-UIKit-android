package io.agora.chat.uikit.feature.chat.interfaces

import android.graphics.drawable.Drawable
import android.widget.EditText
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitInputMenuStyle

/**
 * The base function of EasePrimaryMenu.
 */
interface IChatPrimaryMenu {
    /**
     * Set menu display type
     * @param style
     */
    fun setMenuShowType(style: ChatUIKitInputMenuStyle?)

    /**
     * Show EditText but hide soft keyboard.
     */
    fun showNormalStatus()

    /**
     * Show EditText and soft keyboard.
     */
    fun showTextStatus()

    /**
     * Show voice style and hide other status.
     */
    fun showVoiceStatus()

    /**
     * Show emoticon extend menu and EditText, hide soft keyboard and other status.
     */
    fun showEmojiconStatus()

    /**
     * Show extend menu and EditText, hide soft keyboard and other status.
     */
    fun showMoreStatus()

    /**
     * Hide extend menu.
     */
    fun hideExtendStatus()

    /**
     * Hide soft keyboard.
     */
    fun hideSoftKeyboard()

    /**
     * Enter emoticon event
     * @param emojiContent
     */
    fun onEmojiconInputEvent(emojiContent: CharSequence?)

    /**
     * Delete emoticon event
     */
    fun onEmojiconDeleteEvent()

    /**
     * Insert text
     * @param text
     */
    fun onTextInsert(text: CharSequence?)

    /**
     * Get EditText
     * @return
     */
    val editText: EditText?

    /**
     * Set the background of the input box
     * @param bg
     */
    fun setMenuBackground(bg: Drawable?)

    /**
     * Set the send button background
     * @param bg
     */
    fun setSendButtonBackground(bg: Drawable?)

    /**
     * Set up monitoring
     * @param listener
     */
    fun setEaseChatPrimaryMenuListener(listener: ChatUIKitPrimaryMenuListener?)

    /**
     * Set the menu visibility
     */
    fun setVisible(visible: Int)
}