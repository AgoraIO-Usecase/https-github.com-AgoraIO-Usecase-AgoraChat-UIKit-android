package io.agora.chat.uikit.viewmodel.thread

import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.IAttachView

interface IChatThreadRequest: IAttachView {

    fun setupWithToConversation(parentId:String,messageId:String){}

    /**
     *  Get thread parent
     */
    fun setGroupInfo(parentId:String)

    /**
     *  Creates a message thread
     */
    fun createChatThread(chatThreadName:String,message:ChatMessage){}

    /**
     *  Gets the details of the message thread from the server.
     */
    fun fetchChatThreadFromServer(chatThreadId:String){}

    /**
     *  Gets all the topics in the parentId from the server.
     */
    fun fetchChatThreadsFromServer(parentId:String,limit:Int,cursor:String){}

    /**
     *  Joins a message thread.
     */
    fun joinChatThread(chatThreadId:String){}

    /**
     *  Destroys the message thread.
     */
    fun destroyChatThread(chatThreadId:String){}

    /**
     *  Leaves a message thread.
     */
    fun leaveChatThread(chatThreadId:String){}

    /**
     *  Changes the name of the message thread.
     */
    fun updateChatThreadName(chatThreadId:String,chatThreadName:String){}

    /**
     *  Gets a list of members in the message thread with pagination.
     */
    fun getChatThreadMembers(chatThreadId:String,limit:Int,cursor:String){}

    /**
     * Removes a member from the message thread.
     */
    fun removeMemberFromChatThread(chatThreadId:String,member:String){}

    /**
     *  if parentId == null
     *      Uses the pagination to get the list of message threads that the current user has joined.
     *  if parentId != null
     *      Use the pagination to get the list of message threads that the current user has joined in the specified group.
     */
    fun getJoinedChatThreadsFromServer(parentId:String?=null,limit: Int,cursor: String){}

    /**
     * Gets the last reply in the specified message threads from the server.
     */
    fun getChatThreadLatestMessage(chatThreadIds:List<String>){}

    /**
     * Check the integrity of the conversation object.
     */
    fun checkoutConvScope(){}

    /**
     * Check the integrity of the group object.
     */
    fun checkoutGroupScope(group:ChatGroup?){}
}