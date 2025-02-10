package io.agora.chat.uikit.repository

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatContactManager
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatManager
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatSearchDirection
import io.agora.chat.uikit.common.ChatSearchScope
import io.agora.chat.uikit.common.extensions.parse
import io.agora.chat.uikit.common.suspends.searchBlockContact
import io.agora.chat.uikit.common.suspends.searchContact
import io.agora.chat.uikit.common.suspends.searchMessage
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncProfile
import io.agora.chat.uikit.provider.getSyncUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatUIKitSearchRepository(
    private val chatManager: ChatManager = ChatClient.getInstance().chatManager(),
    private val chatContactManager: ChatContactManager = ChatClient.getInstance().contactManager(),
) {

    companion object {
        private const val TAG = "SearchRep"
    }

    /**
     * Search user from local .
     */
    suspend fun searchUser(query:String):MutableList<ChatUIKitUser> =
        withContext(Dispatchers.IO){
           chatContactManager.searchContact(query)
        }

    /**
     * Search block user from local .
     */
    suspend fun searchBlockUser(query:String):MutableList<ChatUIKitUser> =
        withContext(Dispatchers.IO){
            chatContactManager.searchBlockContact(query)
        }

    /**
     * Search conversation from local .
     */
    suspend fun searchConversation(query:String): List<ChatUIKitConversation> =
        withContext(Dispatchers.IO){
           chatManager.allConversationsBySort.filter { it ->
                   ChatUIKitClient.getGroupProfileProvider()?.getGroup(it.conversationId())?.let {
                         it.name?.contains(query)
                   } ?: kotlin.run{
                       when(it.type) {
                           ChatConversationType.GroupChat -> {
                               var name = ChatUIKitClient.getGroupProfileProvider()?.getSyncProfile(it.conversationId())?.name
                               if (name.isNullOrEmpty()) {
                                   name = ChatClient.getInstance().groupManager().getGroup(it.conversationId())?.groupName
                                   if (name.isNullOrEmpty()) {
                                       name = it.conversationId()
                                   }
                               }
                               name?.contains(query) ?: it.conversationId().contains(query)
                           }
                           ChatConversationType.ChatRoom -> {
                               ChatClient.getInstance().chatroomManager().getChatRoom(it.conversationId())?.name?.contains(query)
                                   ?: it.conversationId().contains(query)
                               it.conversationId().contains(query)
                           }
                           else -> {
                               val name = ChatUIKitClient.getUserProvider()?.getSyncUser(it.conversationId())?.getRemarkOrName() ?: it.conversationId()
                               name.contains(query)
                           }
                       }
                   }
               }
               .map { it.parse() }
        }

    /**
     * Search msg from local .
     */
    suspend fun searchMessage(
        keywords:String,
        timeStamp:Long,
        maxCount:Int,
        from:String?,
        direction:ChatSearchDirection,
        chatScope:ChatSearchScope
    ):List<ChatMessage> =
        withContext(Dispatchers.IO){
            chatManager.searchMessage(keywords, timeStamp, maxCount, from, direction,chatScope)
        }

    /**
     * Search conversation msg from local .
     */
    suspend fun searchMessageByConversation(
        conversationId:String,
        keywords:String,
        timeStamp:Long,
        maxCount:Int,
        from:String?,
        direction:ChatSearchDirection,
        chatScope:ChatSearchScope
    ):List<ChatMessage> =
        withContext(Dispatchers.IO){
            chatManager.getConversation(
                conversationId,
                ChatConversationType.Chat,
                true
            ).searchMessage(
                keywords,timeStamp,maxCount,from,direction,chatScope
            )
        }

}