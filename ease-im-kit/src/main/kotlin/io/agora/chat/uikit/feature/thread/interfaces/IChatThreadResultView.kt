package io.agora.chat.uikit.feature.thread.interfaces

import io.agora.chat.uikit.common.ChatCursorResult
import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatThread
import io.agora.chat.uikit.common.interfaces.IControlDataView

interface IChatThreadResultView: IControlDataView {
    /**
     * Get thread parent info success
     */
    fun settGroupInfoSuccess(parent:ChatGroup?){}

    /**
     * Get thread parent info fail
     */
    fun setGroupInfoFail(code: Int, message: String?){}

    /**
     * Create chat thread success
     */
    fun onCreateChatThreadSuccess(chatThread:ChatThread,message:ChatMessage){}

    /**
     * Create chat thread fail
     */
    fun onCreateChatThreadFail(code: Int,  message: String?){}

    /**
     * Fetch chat thread details from server success
     */
    fun fetchChatThreadDetailsFromServerSuccess(chatThread:ChatThread){}

    /**
     * Fetch chat thread details from server fail
     */
    fun fetchChatThreadDetailsFromServerFail(code: Int, message: String?){}

    /**
     * Fetch chat thread from server success
     */
    fun fetchChatThreadsFromServerSuccess(result:ChatCursorResult<ChatThread>){}

    /**
     * Fetch chat thread from server fail
     */
    fun fetchChatThreadsFromServerFail(code: Int, message: String?){}

    /**
     * Join chat thread success
     */
    fun joinChatThreadSuccess(chatThread:ChatThread){}

    /**
     * Join chat thread fail
     */
    fun joinChatThreadFail(code: Int,  message: String?){}

    /**
     * Destroy chat thread success
     */
    fun destroyChatThreadSuccess(){}

    /**
     * Destroy chat thread fail
     */
    fun destroyChatThreadFail(code: Int,  message: String?){}

    /**
     * Leave chat thread success
     */
    fun leaveChatThreadSuccess(){}

    /**
     * Leave chat thread fail
     */
    fun leaveChatThreadFail(code: Int,  message: String?){}

    /**
     * Update chat thread name success
     */
    fun updateChatThreadNameSuccess(){}

    /**
     * Update chat thread name fail
     */
    fun updateChatThreadNameFail(code: Int,  message: String?){}

    /**
     * Remove member from chat thread success
     */
    fun removeMemberFromChatThreadSuccess(member:String){}

    /**
     * Remove member from chat thread fail
     */
    fun removeMemberFromChatThreadFail(code: Int,  message: String?){}

    /**
     * Get chat thread member success
     */
    fun getChatThreadMembersSuccess(result:ChatCursorResult<String>){}

    /**
     * Get chat thread member fail
     */
    fun getChatThreadMembersFail(code: Int,  message: String?){}

    /**
     * Get joined chat threads from server success
     */
    fun getJoinedChatThreadsFromServerSuccess(result: ChatCursorResult<ChatThread>){}

    /**
     * Get joined chat threads from server fail
     */
    fun getJoinedChatThreadsFromServerFail(code: Int,  message: String?){}

    /**
     * Get chat thread last message success
     */
    fun getChatThreadLatestMessageSuccess(result:MutableMap<String,ChatMessage>){}

    /**
     * Get chat thread last message fail
     */
    fun getChatThreadLatestMessageFail(code: Int,  message: String?){}

    /**
     * Has a error before sending a message.
     * @param code Error code.
     * @param message Error message.
     */
    fun onThreadErrorBeforeSending(code: Int, message: String?){}

}