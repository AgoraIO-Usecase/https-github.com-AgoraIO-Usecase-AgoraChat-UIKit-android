package io.agora.chat.uikit.viewmodel.contacts

import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.IAttachView

interface IContactListRequest : IAttachView {
    /**
     * load User from local or server.
     * @param fetchServerData Whether fetch contacts from server.
     */
    fun loadData(fetchServerData: Boolean = false)

    /**
     * Adds a new contact.
     * @param userName  The user ID.
     * @param reason    Reason for adding friends
     */
    fun addContact(userName:String,reason:String?="")

    /**
     * Deletes a contact.
     * @param userName  The user ID.
     * @param keepConversation  Whether to keep the associated conversation and messages.
     */
    fun deleteContact(userName:String,keepConversation:Boolean?)

    /**
     * Fetch the blocklist from the server.
     */
     fun fetchBlockListFromServer()

    /**
     * Gets the blocklist from the local.
     */
     fun getBlockListFromLocal()

    /**
     * Adds the user to blocklist.
     * You can send message to the user in the blocklist, but you can not receive the message sent by the user in the blocklist.
     * @param userList  The user list to be blocked.
     */
     fun addUserToBlockList(userList:MutableList<String>)

    /**
     * Removes the contact from the blocklist.
     * @param userName  The user to be removed from the blocklist.
     */
     fun removeUserFromBlockList(userName:String)

    /**
     * Accepts a friend invitation。
     * @param userName  The user who initiates the friend request.
     */
     fun acceptInvitation(userName:String)


    /**
     * Declines a friend invitation.
     * @param userName The user who initiates the friend request.
     */
     fun declineInvitation(userName:String)


    /**
     * Clears all the messages of the specified conversation.
     * @param conversationId The conversation ID.
     */
    fun clearConversationMessage(conversationId: String?)

    /**
     * Fetch contact information from user.
     */
    fun fetchContactInfo(contactList: List<ChatUIKitUser>?)

    /**
     * Set the DND of the conversation.
     */
    fun makeSilentModeForConversation(conversationId: String,conversationType:ChatConversationType)

    /**
     * Cancel conversation do not disturb
     */
    fun cancelSilentForConversation(conversationId: String,conversationType:ChatConversationType)

}