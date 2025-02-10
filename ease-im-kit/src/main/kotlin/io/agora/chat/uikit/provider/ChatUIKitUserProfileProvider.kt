package io.agora.chat.uikit.provider

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.impl.OnValueSuccess
import io.agora.chat.uikit.model.ChatUIKitProfile
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface ChatUIKitUserProfileProvider {
    /**
     * Get [ChatUIKitProfile] by userId from user.
     * @param userId
     * @return  The object provider by user.
     */
    fun getUser(userId: String?): ChatUIKitProfile?

    /**
     * Fetch users info from server and callback to UI SDK.
     * @param userIds  The user list stop scrolling,
     *              and the visible items which do not have profile will be fetched.
     * @param onValueSuccess The callback of success called by user.
     */
    fun fetchUsers(userIds: List<String>, onValueSuccess: OnValueSuccess<List<ChatUIKitProfile>>)
}

/**
 * Suspended function for fetching user information.
 */
suspend fun ChatUIKitUserProfileProvider.fetchUsersBySuspend(userIds: List<String>?): List<ChatUIKitProfile> {
    return suspendCoroutine { continuation ->
        userIds?.let {
            fetchUsers(it, onValueSuccess = { map ->
                continuation.resume(map)
            })
        }
    }
}

/**
 * Get user info by cache or sync method provided by user.
 */
fun ChatUIKitUserProfileProvider.getSyncUser(userId: String?): ChatUIKitProfile? {
    var user = ChatUIKitClient.getCache().getUser(userId)
    if (user == null) {
        user = getUser(userId)
        if (user != null && !userId.isNullOrEmpty()) {
            ChatUIKitClient.getCache().insertUser(user)
        }
    }
    return user
}