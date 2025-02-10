package io.agora.chat.uikit.provider

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.impl.OnValueSuccess
import io.agora.chat.uikit.model.ChatUIKitGroupProfile
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Group profile provider.
 */
interface ChatUIKitGroupProfileProvider {
    /**
     * return ChatUIKitProfile for input group id
     * @param id    The group id.
     * @return
     */
    fun getGroup(id: String?): ChatUIKitGroupProfile?

    /**
     * Fetch profiles from server and callback to UI SDK.
     * @param groupIds  The conversation list or group list stop scrolling, and the visible items which do not have profile will be called.
     * @param onValueSuccess The callback of success called by developer.
     */
    fun fetchGroups(groupIds: List<String>, onValueSuccess: OnValueSuccess<List<ChatUIKitGroupProfile>>)
}

/**
 * Suspended function for fetching profiles.
 */
suspend fun ChatUIKitGroupProfileProvider.fetchProfilesBySuspend(groupIds: List<String>): List<ChatUIKitGroupProfile> {
    return suspendCoroutine { continuation ->
        fetchGroups(groupIds, onValueSuccess = { map ->
            continuation.resume(map)
        })
    }
}

/**
 * Get profile by cache or sync method provided by developer.
 */
fun ChatUIKitGroupProfileProvider.getSyncProfile(id: String?): ChatUIKitGroupProfile? {
    var profile = ChatUIKitClient.getCache().getGroup(id)
    if (profile == null) {
        profile = getGroup(id)
        if (profile != null && !id.isNullOrEmpty()) {
            ChatUIKitClient.getCache().insertGroup(id, profile)
        }
    }
    return profile
}