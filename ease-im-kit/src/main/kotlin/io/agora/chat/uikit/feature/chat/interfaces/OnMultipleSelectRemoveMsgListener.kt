package io.agora.chat.uikit.feature.chat.interfaces

interface OnMultipleSelectRemoveMsgListener {

    /**
     * Delete message list successfully.
     */
    fun multipleSelectRemoveMsgSuccess(){}

    /**
     * Failed to delete message list.
     */
    fun multipleSelectRemoveMsgFail(code: Int, errorMsg: String?){}
}