package io.agora.chat.uikit.feature.chat.interfaces

interface OnEditTextChangeListener {
    /**
     * when send button clicked
     * @param content
     */
    fun onClickKeyboardSendBtn(content: String?)

    /**
     * if edit text has focus
     */
    fun onEditTextHasFocus(hasFocus: Boolean)
}