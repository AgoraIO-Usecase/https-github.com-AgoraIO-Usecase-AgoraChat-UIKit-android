package io.agora.chat.uikit.feature.conversation.interfaces

interface OnConversationListChangeListener {
    /**
     * Notice item change
     * @param position
     */
    fun notifyItemChange(position: Int, conversationId: String)

    /**
     * Notify all data changes
     */
    fun notifyAllChange()

    /**
     * Notice removal
     * @param position
     * @param conversationId
     */
    fun notifyItemRemove(position: Int, conversationId: String)
}