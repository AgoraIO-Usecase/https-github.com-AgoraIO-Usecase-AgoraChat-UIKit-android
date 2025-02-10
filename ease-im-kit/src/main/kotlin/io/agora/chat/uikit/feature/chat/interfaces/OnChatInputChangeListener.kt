package io.agora.chat.uikit.feature.chat.interfaces

import android.text.Editable
import android.view.KeyEvent
import android.view.View

interface OnChatInputChangeListener {
    /**
     * EditText text change monitoring
     * @param s
     * @param start
     * @param before
     * @param count
     */
    fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)

    /**
     * After typing on the editing text layout.
     */
    fun afterTextChanged(s: Editable?){}

    /**
     * Edit text layout key events.
     */
    fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?):Boolean
}