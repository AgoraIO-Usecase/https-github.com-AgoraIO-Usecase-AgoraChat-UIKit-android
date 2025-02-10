package io.agora.chat.uikit.feature.chat.interfaces

import android.text.Editable
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View

interface ChatInputMenuListener {
    /**
     * when typing on the edit-text layout.
     */
    fun onTyping(s: CharSequence?, start: Int, before: Int, count: Int)

    /**
     * After typing on the editing text layout.
     */
    fun afterTextChanged(s: Editable?)

    /**
     * Edit text layout key events.
     */
    fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?):Boolean

    /**
     * when send message button pressed
     *
     * @param content
     * message content
     */
    fun onSendMessage(content: String?)

    /**
     * when big icon pressed
     * @param emojiIcon
     */
    fun onExpressionClicked(emojiIcon: Any?)

    /**
     * when speak button is touched
     * @param v
     * @param event
     * @return
     */
    fun onPressToSpeakBtnTouch(v: View?, event: MotionEvent?): Boolean

    /**
     * When voice button pressed.
     */
    fun onToggleVoiceBtnClicked()

    /**
     * when click the item of extend menu
     * @param itemId
     * @param view
     */
    fun onChatExtendMenuItemClick(itemId: Int, view: View?)
}