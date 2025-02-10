package io.agora.chat.uikit.repository

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatContactManager
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.common.suspends.acceptContactInvitation
import io.agora.chat.uikit.common.suspends.addNewContact
import io.agora.chat.uikit.common.suspends.addToBlackList
import io.agora.chat.uikit.common.suspends.declineContactInvitation
import io.agora.chat.uikit.common.suspends.deleteUserFromBlackList
import io.agora.chat.uikit.common.suspends.fetchBlackListFromServer
import io.agora.chat.uikit.common.suspends.fetchContactsFromServer
import io.agora.chat.uikit.common.suspends.removeContact
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.fetchUsersBySuspend
import io.agora.chat.uikit.provider.getSyncUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class ChatUIKitContactListRepository(
    val chatContactManager: ChatContactManager = ChatClient.getInstance().contactManager(),
) {

    companion object {
        private const val TAG = "ContactRep"
    }

    /**
     * Load server contacts.
     */
    open suspend fun loadData():List<ChatUIKitUser> =
        withContext(Dispatchers.IO){
            chatContactManager.fetchContactsFromServer()
        }

    /**
     * Load local contacts.
     */
    suspend fun loadLocalContact(): List<ChatUIKitUser> =
        withContext(Dispatchers.IO) {
            chatContactManager.contactsFromLocal.map {
                ChatUIKitClient.getUserProvider()?.getSyncUser(it)?.toUser() ?: ChatUIKitUser(it)
            }
        }

    /**
     * Add a Contact
     */
    suspend fun addContact(userName:String,reason:String?=""):String =
        withContext(Dispatchers.IO){
            chatContactManager.addNewContact(userName = userName,reason = reason)
        }

    /**
     * Delete a Contact and keepConversation
     */
    suspend fun deleteContact(userName:String,keepConversation: Boolean?):Int =
        withContext(Dispatchers.IO){
            chatContactManager.removeContact(userName, keepConversation)
        }

    /**
     * Get blocklist list from server
     */
    suspend fun getBlockListFromServer():MutableList<ChatUIKitUser> =
        withContext(Dispatchers.IO){
            chatContactManager.fetchBlackListFromServer()
        }

    /**
     * Get blocklist list from local
     */
    suspend fun getBlockListFromLocal():MutableList<ChatUIKitUser> =
        withContext(Dispatchers.IO){
            chatContactManager.blackListUsernames.map {
                ChatUIKitClient.getUserProvider()?.getSyncUser(it)?.toUser() ?: ChatUIKitUser(it)
            }.toMutableList()
        }

    /**
     * Add User to blocklist
     */
    suspend fun addUserToBlockList(userList:MutableList<String>):Int =
        withContext(Dispatchers.IO){
            chatContactManager.addToBlackList(userList)
        }

    /**
     * Remove User to blocklist
     */
    suspend fun removeUserFromBlockList(userName:String):Int =
        withContext(Dispatchers.IO){
            chatContactManager.deleteUserFromBlackList(userName)
        }

    /**
     * Agree to contact invitation
     */
    suspend fun acceptInvitation(userName:String):Int =
        withContext(Dispatchers.IO){
            chatContactManager.acceptContactInvitation(userName)
        }

    /**
     * Refuse contact invitation
     */
    suspend fun declineInvitation(userName:String):Int =
        withContext(Dispatchers.IO){
            chatContactManager.declineContactInvitation(userName)
        }

    /**
     * Fetch user information from user.
     */
    suspend fun fetchContactInfo(contactList: List<ChatUIKitUser>?) =
        withContext(Dispatchers.IO) {
            val userList = contactList?.map { it.userId }
            if (contactList.isNullOrEmpty()) {
                throw ChatException(ChatError.INVALID_PARAM, "contactList is null or empty.")
            }
            ChatUIKitClient.getUserProvider()?.fetchUsersBySuspend(userList)
        }
}