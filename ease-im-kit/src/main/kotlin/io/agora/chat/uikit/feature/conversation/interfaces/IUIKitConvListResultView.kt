package io.agora.chat.uikit.feature.conversation.interfaces

import io.agora.chat.uikit.common.interfaces.IControlDataView
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.model.ChatUIKitProfile

/**
 * Conversation list result interface by [ChatUIKitConversationListViewModel]
 */
interface IUIKitConvListResultView: IControlDataView {

    /**
     * Load conversation list successfully.
     */
    fun loadConversationListSuccess(list: List<ChatUIKitConversation>)

    /**
     * Load conversation list failed.
     */
    fun loadConversationListFail(code: Int, error: String)

    /**
     * Sort conversation list successfully.
     */
    fun sortConversationListFinish(conversations: List<ChatUIKitConversation>)

    /**
     * Mark conversation read successfully.
     */
    fun makeConversionReadSuccess(position: Int, conversation: ChatUIKitConversation)

    /**
     * Pin conversation successfully.
     */
    fun pinConversationSuccess(position: Int, conversation: ChatUIKitConversation)

    /**
     * Pin conversation failed.
     */
    fun pinConversationFail(conversation: ChatUIKitConversation, code: Int, error: String)

    /**
     * Unpin conversation successfully.
     */
    fun unpinConversationSuccess(position: Int, conversation: ChatUIKitConversation)

    /**
     * Unpin conversation failed.
     */
    fun unpinConversationFail(conversation: ChatUIKitConversation, code: Int, error: String)

    /**
     * Delete conversation successfully.
     */
    fun deleteConversationSuccess(position: Int, conversation: ChatUIKitConversation)

    /**
     * Delete conversation failed.
     */
    fun deleteConversationFail(conversation: ChatUIKitConversation, code: Int, error: String)

    /**
     * Make conversations interruption-free successfully.
     */
    fun makeSilentForConversationSuccess(position: Int, conversation: ChatUIKitConversation)

    /**
     * Make conversations interruption-free failed.
     */
    fun makeSilentForConversationFail(
        conversation: ChatUIKitConversation,
        errorCode: Int,
        description: String?
    )

    /**
     * Cancel conversation do not disturb successfully.
     */
    fun cancelSilentForConversationSuccess(position: Int, conversation: ChatUIKitConversation)

    /**
     * Cancel conversation do not disturb failed.
     */
    fun cancelSilentForConversationFail(
        conversation: ChatUIKitConversation,
        errorCode: Int,
        description: String?
    )

    /**
     * Fetch conversation info from user successfully.
     */
    fun fetchConversationInfoByUserSuccess(profiles: List<ChatUIKitProfile>?)

}