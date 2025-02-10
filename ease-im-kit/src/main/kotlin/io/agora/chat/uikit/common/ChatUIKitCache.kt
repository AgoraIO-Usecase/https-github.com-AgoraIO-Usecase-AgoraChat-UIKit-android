package io.agora.chat.uikit.common

import io.agora.chat.uikit.common.enums.ChatUIKitCacheType
import io.agora.chat.uikit.common.helper.ChatUIKitPreferenceManager
import io.agora.chat.uikit.model.ChatUIKitGroupProfile
import io.agora.chat.uikit.model.ChatUIKitPreview
import io.agora.chat.uikit.model.ChatUIKitProfile
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class ChatUIKitCache {
    private val userMap: ConcurrentMap<String, ChatUIKitProfile> = ConcurrentHashMap()
    // Cache the group info. The key is the groupId, the value is the group info.
    private val groupMap: ConcurrentMap<String, ChatUIKitGroupProfile> = ConcurrentHashMap()
    // Cache the userinfo parsed by message ext. The key is the userId, the value is the userinfo.
    private val messageUserMap: ConcurrentMap<String, ChatUIKitProfile> = ConcurrentHashMap()
    private val mutedConvMap: MutableMap<String, Long> = HashMap()
    private val previewMap:ConcurrentMap<String, ChatUIKitPreview> = ConcurrentHashMap()
    private val checkPreviewMap:MutableMap<String,Boolean> = mutableMapOf()

    companion object {
        private const val TAG = "ChatUIKitCache"
    }

    fun init() {
        clear(ChatUIKitCacheType.ALL)
        // Load the muted conversation list from the local storage.
        val muteMap = ChatUIKitPreferenceManager.getInstance().getMuteMap(ChatClient.getInstance().currentUser)
        if (muteMap.isNotEmpty()) {
            mutedConvMap.putAll(muteMap)
        }
    }

    fun insertUser(user: ChatUIKitProfile) {
        userMap[user.id] = user
    }

    /**
     * Insert or update the group info to the cache.
     * @param groupId The group id.
     * @param profile The group info.
     */
    fun insertGroup(groupId: String?, profile: ChatUIKitGroupProfile?) {
        if (groupId.isNullOrEmpty()) {
            ChatLog.e(TAG, "insertGroup: groupId is null or empty")
            return
        }
        groupMap[groupId] = profile
    }

    fun getUser(userId: String?): ChatUIKitProfile? {
        if (userId.isNullOrEmpty()) {
            return null
        }
        return userMap[userId]
    }

    /**
     * Get the group info by groupId.
     * @param groupId The group id.
     * @return The group info.
     */
    fun getGroup(groupId: String?): ChatUIKitGroupProfile? {
        if (groupId.isNullOrEmpty()) {
            return null
        }
        return groupMap[groupId]
    }

    /**
     * Insert message userinfo to cache.
     */
    @Synchronized
    fun insertMessageUser(userId: String, profile: ChatUIKitProfile) {
        if (messageUserMap.containsKey(userId)) {
            if (messageUserMap[userId]!!.getTimestamp() < profile.getTimestamp()) {
                return
            }
        }
        messageUserMap[userId] = profile
    }

    /**
     * Get userinfo cache by userId.
     */
    fun getMessageUserInfo(userId: String?): ChatUIKitProfile? {
        if (userId.isNullOrEmpty() || !messageUserMap.containsKey(userId)) return null
        return messageUserMap[userId]
    }

    /**
     * Get the muted conversation list.
     */
    @Synchronized
    fun getMutedConversationList(): MutableMap<String, Long> {
        return mutedConvMap
    }

    /**
     * Add target conversation to mute map.
     */
    @Synchronized
    fun setMutedConversation(conversationId: String, mutedTime: Long = 0) {
        mutedConvMap[conversationId] = mutedTime
        ChatUIKitPreferenceManager.getInstance().setMuteMap(ChatClient.getInstance().currentUser, mutedConvMap)
    }

    /**
     * Remove target conversation from mute map.
     */
    @Synchronized
    fun removeMutedConversation(conversationId: String) {
        mutedConvMap.remove(conversationId)
        ChatUIKitPreferenceManager.getInstance().setMuteMap(ChatClient.getInstance().currentUser, mutedConvMap)
    }

    fun clear(type: ChatUIKitCacheType?) {
        if (type == null || type == ChatUIKitCacheType.ALL) {
            userMap.clear()
            groupMap.clear()
            messageUserMap.clear()
            mutedConvMap.clear()
            previewMap.clear()
        } else {
            when (type) {
                ChatUIKitCacheType.CONTACT -> userMap.clear()
                ChatUIKitCacheType.CONVERSATION_INFO -> groupMap.clear()
                else -> {
                }
            }
        }
    }

    fun updateProfiles(profiles: List<ChatUIKitGroupProfile>) {
        if (profiles.isNotEmpty()) {
            profiles.forEach {
                groupMap[it.id] = it
            }
        }
    }

    fun updateUsers(users: List<ChatUIKitProfile>) {
        if (users.isNotEmpty()) {
            users.forEach {
                userMap[it.id] = it
            }
        }
    }

    fun saveUrlPreviewInfo(msgId:String?,bean:ChatUIKitPreview){
        msgId?.let {
            if (it.isNotEmpty()){
                previewMap[msgId] = bean
            }
        }
    }

    fun getUrlPreviewInfo(msgId: String?): ChatUIKitPreview? {
        msgId?.let {
            if (previewMap.size > 0 && it.isNotEmpty()) {
                if (previewMap.containsKey(msgId)) {
                    return previewMap[msgId]
                }
            }
        }
        return null
    }

    fun cleanUrlPreviewInfo(msgId: String?){
        msgId?.let {
            if (previewMap.containsKey(it)) {
                previewMap.remove(it)
            }
            if (checkPreviewMap.containsKey(it)){
                checkPreviewMap.remove(it)
            }
        }
    }

    fun checkUrlPreview(msgId:String?,isFirst:Boolean? = true){
        msgId?.let {
            if (it.isNotEmpty()){
                checkPreviewMap[msgId] = isFirst?:true
            }
        }
    }

    fun isFirstLoadedUrlPreview(msgId:String?):Boolean{
        msgId?.let {
            if (checkPreviewMap.isNotEmpty() && it.isNotEmpty()) {
                if (checkPreviewMap.containsKey(msgId)) {
                    return checkPreviewMap[msgId]?:true
                }
            }
        }
        return true
    }

}