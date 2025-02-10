package io.agora.chat.uikit.feature.chat.interfaces

import android.text.Editable
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View

interface ChatUIKitPrimaryMenuListener {
    /**
     * when send button clicked
     * @param content
     */
    fun onSendBtnClicked(content: String?)

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
     * when speak button is touched
     * @return
     */
    fun onPressToSpeakBtnTouch(v: View?, event: MotionEvent?): Boolean

    /**
     * toggle on/off voice button
     */
    fun onToggleVoiceBtnClicked()

    /**
     * toggle on/off text button
     */
    fun onToggleTextBtnClicked()

    /**
     * toggle on/off extend menu
     * @param extend
     */
    fun onToggleExtendClicked(extend: Boolean)

    /**
     * toggle on/off emoji icon
     * @param extend
     */
    fun onToggleEmojiconClicked(extend: Boolean)

    /**
     * on text input is clicked
     */
    fun onEditTextClicked()

    /**
     * if edit text has focus
     */
    fun onEditTextHasFocus(hasFocus: Boolean)
}