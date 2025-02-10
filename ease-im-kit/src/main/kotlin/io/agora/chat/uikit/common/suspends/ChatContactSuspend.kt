package io.agora.chat.uikit.common.suspends

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatContactManager
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.common.helper.ChatUIKitPreferenceManager
import io.agora.chat.uikit.common.impl.CallbackImpl
import io.agora.chat.uikit.common.impl.ValueCallbackImpl
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncUser
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * Suspend method for [ChatContactManager.fetchContactsFromServer()]
 * @return List<ChatUIKitUser> User Information List
 */
suspend fun ChatContactManager.fetchContactsFromServer():List<ChatUIKitUser>{
    return suspendCoroutine{ continuation ->
        asyncGetAllContactsFromServer(ValueCallbackImpl(
               onSuccess = { value ->
                   value?.let {
                       val list = it.map { contact ->
                           ChatUIKitClient.getUserProvider()?.getSyncUser(contact)?.toUser() ?: ChatUIKitUser(contact)
                       }
                       continuation.resume(list)
                   }
               },
               onError = {code,message-> continuation.resumeWithException(ChatException(code, message)) }
           ))
    }
}

/**
 * Suspend method for [ChatContactManager.addNewContact(userName,reason)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.addNewContact(userName:String, reason:String?): String{
    return suspendCoroutine{ continuation ->
        asyncAddContact(userName,reason, CallbackImpl(
            function = ChatUIKitConstant.API_ASYNC_ADD_CONTACT,
            onSuccess = { continuation.resume(userName) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

/**
 * Suspend method for [ChatContactManager.removeContact(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.removeContact(userName:String, keepConversation: Boolean?): Int{
    return suspendCoroutine{ continuation ->
        if (keepConversation == null){
            asyncDeleteContact(userName, CallbackImpl(
                onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
                onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
            ))
        }else{
            try {
                deleteContact(userName,keepConversation)
                continuation.resume(ChatError.EM_NO_ERROR)
            } catch (e: ChatException) {
                continuation.resumeWithException(e)
            }
        }

    }
}

/**
 * Suspend method for [ChatContactManager.getBlackListFromServer(userName)]
 * @return List<ChatUIKitUser> User Information List
 */
suspend fun ChatContactManager.fetchBlackListFromServer(): MutableList<ChatUIKitUser>{
    return suspendCoroutine{ continuation ->
        asyncGetBlackListFromServer(ValueCallbackImpl(
            onSuccess = { value ->
                value?.let {
                    val list = it.map { id ->
                        ChatUIKitClient.getUserProvider()?.getSyncUser(id)?.toUser() ?: ChatUIKitUser(id)
                    }.toMutableList()
                    continuation.resume(list)
                }
            },
            onError = {code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}


/**
 * Suspend method for [ChatContactManager.addUserToBlackList(userName,both)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.addToBlackList(userList:MutableList<String>): Int{
    return suspendCoroutine{ continuation ->
        asyncSaveBlackList(userList, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

/**
 * Suspend method for [ChatContactManager.removeUserFromBlackList(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.deleteUserFromBlackList(userName:String): Int{
    return suspendCoroutine{ continuation ->
        asyncRemoveUserFromBlackList(userName, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

/**
 * Suspend method for [ChatContactManager.acceptInvitation(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.acceptContactInvitation(userName:String): Int{
    return suspendCoroutine{ continuation ->
        asyncAcceptInvitation(userName, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}


/**
 * Suspend method for [ChatContactManager.declineInvitation(userName)]
 * @return [ChatError] The result of the request.
 */
suspend fun ChatContactManager.declineContactInvitation(userName:String): Int{
    return suspendCoroutine{ continuation ->
        asyncDeclineInvitation(userName, CallbackImpl(
            onSuccess = { continuation.resume(ChatError.EM_NO_ERROR) },
            onError = { code,message-> continuation.resumeWithException(ChatException(code, message))}
        ))
    }
}

suspend fun ChatContactManager.searchContact(query:String):MutableList<ChatUIKitUser>{
    return suspendCoroutine{ continuation ->
        val localContact = contactsFromLocal
        val resultList = mutableListOf<ChatUIKitUser>()
        localContact.forEach{
            val userInfo = ChatUIKitClient.getCache().getUser(it)
            if (userInfo == null){
                if (it.contains(query)){
                    resultList.add(ChatUIKitUser(it))
                }
            }else{
                userInfo.let { user->
                    val nickname = user.getRemarkOrName()
                    if (nickname.contains(query)){
                        resultList.add(user.toUser())
                    }
                }
            }
        }
        continuation.resume(resultList)
    }
}

suspend fun ChatContactManager.searchBlockContact(query:String):MutableList<ChatUIKitUser>{
    return suspendCoroutine{ continuation ->
        val localBlockContact = blackListUsernames
        val resultList = mutableListOf<ChatUIKitUser>()
        localBlockContact.forEach{
            val userInfo = ChatUIKitClient.getCache().getUser(it)
            if (userInfo == null){
                if (it.contains(query)){
                    resultList.add(ChatUIKitUser(it))
                }
            }else{
                userInfo.let { user->
                    val nickname = user.getRemarkOrName()
                    if (nickname.contains(query)){
                        resultList.add(user.toUser())
                    }
                }
            }
        }
        continuation.resume(resultList)
    }
}
