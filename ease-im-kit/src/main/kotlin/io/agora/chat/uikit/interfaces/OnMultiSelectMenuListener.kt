package io.agora.chat.uikit.interfaces

interface OnMultiSelectMenuListener {
    /**
     * Callback when delete button is clicked.
     * @param messageIdList the list of message id to be deleted.
     */
    fun onDeleteClick(messageIdList: List<String>)

    /**
     * Callback when forward button is clicked.
     * @param messageIdList the list of message id to be forwarded.
     */
    fun onForwardClick(messageIdList: List<String>)
}