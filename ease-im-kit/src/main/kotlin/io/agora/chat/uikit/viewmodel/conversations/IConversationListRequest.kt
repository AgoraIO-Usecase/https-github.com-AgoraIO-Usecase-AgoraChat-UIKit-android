package io.agora.chat.uikit.viewmodel.conversations

import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.viewmodel.IAttachView

/**
 * Interface that [ChatUIKitConversationListViewModel] needs to implement
 */
interface IConversationListRequest: IAttachView {

    /**
     * load conversations from local or server.
     */
    fun loadData()

    /**
     * Sort conversations.
     */
    fun sortConversationList(conversations: List<ChatUIKitConversation>)

    /**
     * Mark conversation read
     * @param position
     * @param conversation
     */
    fun makeConversionRead(position: Int, conversation: ChatUIKitConversation)

    /**
     * Make conversations interruption-free
     */
    fun makeSilentForConversation(position: Int, conversation: ChatUIKitConversation)

    /**
     * Cancel conversation do not disturb
     */
    fun cancelSilentForConversation(position: Int, conversation: ChatUIKitConversation)

    /**
     * Pin conversation
     * @param position
     * @param conversation
     */
    fun pinConversation(position: Int, conversation: ChatUIKitConversation)

    /**
     * Unpin conversation
     * @param position
     * @param conversation
     */
    fun unpinConversation(position: Int, conversation: ChatUIKitConversation)

    /**
     * Delete conversation
     * @param position
     * @param conversation
     */
    fun deleteConversation(position: Int, conversation: ChatUIKitConversation)

    /**
     * Fetch group info.
     */
    fun fetchConvGroupInfo(conversationList: List<ChatUIKitConversation>)

    /**
     * Fetch user info.
     */
    fun fetchConvUserInfo(conversationList: List<ChatUIKitConversation>)
}