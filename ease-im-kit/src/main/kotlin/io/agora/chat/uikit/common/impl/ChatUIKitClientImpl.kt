package io.agora.chat.uikit.common.impl

import android.content.Context
import io.agora.chat.uikit.ChatUIKitConfig
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatConnectionListener
import io.agora.chat.uikit.common.ChatContactListener
import io.agora.chat.uikit.common.ChatConversationListener
import io.agora.chat.uikit.common.ChatGroupChangeListener
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessageListener
import io.agora.chat.uikit.common.ChatMultiDeviceListener
import io.agora.chat.uikit.common.ChatOptions
import io.agora.chat.uikit.common.ChatPresenceListener
import io.agora.chat.uikit.common.ChatListenersWrapper
import io.agora.chat.uikit.common.ChatRoomChangeListener
import io.agora.chat.uikit.common.ChatThreadChangeListener
import io.agora.chat.uikit.common.ChatUIKitCache
import io.agora.chat.uikit.common.enums.ChatUIKitCacheType
import io.agora.chat.uikit.common.extensions.isMainProcess
import io.agora.chat.uikit.common.helper.ChatUIKitNotifier
import io.agora.chat.uikit.common.helper.ChatUIKitPreferenceManager
import io.agora.chat.uikit.interfaces.IChatUIKitClient
import io.agora.chat.uikit.interfaces.OnEventResultListener
import io.agora.chat.uikit.model.ChatUIKitGroupProfile
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.provider.ChatUIKitCustomActivityRoute
import io.agora.chat.uikit.provider.ChatUIKitGroupProfileProvider
import io.agora.chat.uikit.provider.ChatUIKitEmojiconInfoProvider
import io.agora.chat.uikit.provider.ChatUIKitSettingsProvider
import io.agora.chat.uikit.provider.ChatUIKitUserProfileProvider
import java.util.concurrent.atomic.AtomicBoolean

internal class ChatUIKitClientImpl: IChatUIKitClient {
    private var isInit: AtomicBoolean = AtomicBoolean(false)
    private var groupProfileProvider: ChatUIKitGroupProfileProvider? = null
    private var userProvider: ChatUIKitUserProfileProvider? = null
    private var emojiconProvider: ChatUIKitEmojiconInfoProvider? = null
    private var settingsProvider: ChatUIKitSettingsProvider? = null
    private var activityRoute: ChatUIKitCustomActivityRoute? = null
    private var config: ChatUIKitConfig? = ChatUIKitConfig()
    private lateinit var context: Context
    private val cache: ChatUIKitCache by lazy { ChatUIKitCache() }
    private var user: ChatUIKitProfile? = null
    private val _notifier: ChatUIKitNotifier by lazy { ChatUIKitNotifier(context) }
    companion object {
        private const val TAG = "IChatUIKitClient"
    }
    override fun init(context: Context, options: ChatOptions?) {
        ChatLog.e(TAG, "UIKIt init")
        if (isInit.get()) {
            return
        }
        if (!context.isMainProcess()) {
            ChatLog.e(TAG, "Please init ChatUIKitClient in main process")
            return
        }
        this.context = context.applicationContext
        var chatOptions: ChatOptions? = null
        if (options == null) {
            chatOptions = ChatOptions().apply {
                // change to need confirm contact invitation
                acceptInvitationAlways = false
                // set if need read ack
                requireAck = true
                // set if need delivery ack
                requireDeliveryAck = false
            }
        } else {
            chatOptions = options
        }
        ChatClient.getInstance().init(context, chatOptions)
        addChatListenersWrapper()
        isInit.set(true)
        // If auto login, should init the cache.
        if (chatOptions.autoLogin && ChatClient.getInstance().isLoggedInBefore) {
            cache.init()
        }
        ChatLog.e(TAG, "UIKIt init end")
    }

    override fun login(userId: String, password: String, onSuccess: OnSuccess, onError: OnError) {
        this.user = ChatUIKitProfile(userId)
        ChatClient.getInstance().login(userId, password, CallbackImpl(onSuccess = {
            cache.init()
            cache.insertUser(user!!)
            onSuccess.invoke()
        }, onError))
    }

    override fun login(user: ChatUIKitProfile, token: String, onSuccess: OnSuccess, onError: OnError) {
        this.user = user
        ChatClient.getInstance().loginWithToken(user.id, token, CallbackImpl(onSuccess = {
            cache.init()
            cache.insertUser(user)
            onSuccess.invoke()
        }, onError))
    }

    override fun logout(unbindDeviceToken: Boolean, onSuccess: OnSuccess, onError: OnError) {
        val oldId = getCurrentUser()?.id
        ChatClient.getInstance().logout(unbindDeviceToken, CallbackImpl(onSuccess = {
            cache.clear(ChatUIKitCacheType.ALL)
            ChatUIKitPreferenceManager.getInstance().removeLoadedContactDataStatus(oldId)
            onSuccess.invoke()
        }, onError))
    }

    override fun isInited(): Boolean {
        return isInit.get()
    }

    override fun isLoggedIn(): Boolean {
        return ChatClient.getInstance().isLoggedIn
    }

    override fun updateCurrentUser(user: ChatUIKitProfile) {
        this.user = user
        cache.insertUser(user)
    }

    override fun getCurrentUser(): ChatUIKitProfile? {
        return if (!ChatClient.getInstance().currentUser.isNullOrEmpty()) {
            cache.getUser(ChatClient.getInstance().currentUser)
                ?: ChatUIKitProfile(ChatClient.getInstance().currentUser)
        } else null
    }

    override fun setEmojiconInfoProvider(provider: ChatUIKitEmojiconInfoProvider) {
        emojiconProvider = provider
    }

    override fun setGroupProfileProvider(provider: ChatUIKitGroupProfileProvider) {
        groupProfileProvider = provider
    }

    override fun setUserProfileProvider(provider: ChatUIKitUserProfileProvider) {
        userProvider = provider
    }

    override fun updateGroupProfiles(profiles: List<ChatUIKitGroupProfile>) {
        cache.updateProfiles(profiles)
    }

    override fun updateUsersInfo(users: List<ChatUIKitProfile>) {
        cache.updateUsers(users)
    }

    override fun setSettingsProvider(provider: ChatUIKitSettingsProvider) {
        settingsProvider = provider
    }

    override fun setCustomActivityRoute(route: ChatUIKitCustomActivityRoute) {
        activityRoute = route
    }

    override fun setConfig(config: ChatUIKitConfig?) {
        if (config != null) {
            this.config = config
        }
    }

    override fun getContext(): Context? {
        if (!isInit.get()) {
            ChatLog.e(TAG, "please init UIKit SDK first!")
            return null
        }
        return context
    }

    override fun getEmojiconInfoProvider(): ChatUIKitEmojiconInfoProvider? {
        return emojiconProvider
    }

    override fun getGroupProfileProvider(): ChatUIKitGroupProfileProvider? {
        return groupProfileProvider
    }

    override fun getUserProvider(): ChatUIKitUserProfileProvider? {
        return userProvider
    }

    override fun getSettingsProvider(): ChatUIKitSettingsProvider? {
        return settingsProvider
    }

    override fun getCustomActivityRoute(): ChatUIKitCustomActivityRoute? {
        return activityRoute
    }

    override fun getConfig(): ChatUIKitConfig? {
        return config
    }

    override fun clearKitCache(type: ChatUIKitCacheType?) {
        cache.clear(type)
    }

    override fun getKitCache(): ChatUIKitCache {
        return cache
    }

    override fun getNotifier(): ChatUIKitNotifier? {
        if (!isInit.get()) {
            ChatLog.e(TAG, "please init UIKit SDK first!")
            return null
        }
        return _notifier
    }

    private fun addChatListenersWrapper() {
        ChatListenersWrapper.getInstance().addListeners()
    }

    fun removeChatListener() {
        ChatListenersWrapper.getInstance().removeListeners()
    }

    override fun addConnectionListener(listener:ChatConnectionListener) {
        ChatListenersWrapper.getInstance().addConnectionListener(listener)
    }

    override fun removeConnectionListener(listener: ChatConnectionListener) {
        ChatListenersWrapper.getInstance().removeConnectionListener(listener)
    }

    override fun addChatMessageListener(listener: ChatMessageListener) {
        ChatListenersWrapper.getInstance().addChatMessageListener(listener)
    }

    override fun removeChatMessageListener(listener: ChatMessageListener) {
        ChatListenersWrapper.getInstance().removeChatMessageListener(listener)
    }

    override fun addGroupChangeListener(listener: ChatGroupChangeListener) {
        ChatListenersWrapper.getInstance().addGroupChangeListener(listener)
    }

    override fun removeGroupChangeListener(listener: ChatGroupChangeListener) {
        ChatListenersWrapper.getInstance().removeGroupChangeListener(listener)
    }

    override fun addContactListener(listener: ChatContactListener) {
        ChatListenersWrapper.getInstance().addContactListener(listener)
    }

    override fun removeContactListener(listener: ChatContactListener) {
        ChatListenersWrapper.getInstance().removeContactListener(listener)
    }

    override fun addConversationListener(listener: ChatConversationListener) {
        ChatListenersWrapper.getInstance().addConversationListener(listener)
    }

    override fun removeConversationListener(listener: ChatConversationListener) {
        ChatListenersWrapper.getInstance().removeConversationListener(listener)
    }

    override fun addPresenceListener(listener: ChatPresenceListener) {
        ChatListenersWrapper.getInstance().addPresenceListener(listener)
    }

    override fun removePresenceListener(listener: ChatPresenceListener) {
        ChatListenersWrapper.getInstance().removePresenceListener(listener)
    }

    override fun addChatRoomChangeListener(listener: ChatRoomChangeListener) {
        ChatListenersWrapper.getInstance().addChatRoomChangeListener(listener)
    }

    override fun removeChatRoomChangeListener(listener: ChatRoomChangeListener) {
        ChatListenersWrapper.getInstance().removeChatRoomChangeListener(listener)
    }

    override fun addMultiDeviceListener(listener: ChatMultiDeviceListener) {
        ChatListenersWrapper.getInstance().addMultiDeviceListener(listener)
    }

    override fun removeMultiDeviceListener(listener: ChatMultiDeviceListener) {
        ChatListenersWrapper.getInstance().removeMultiDeviceListener(listener)
    }

    override fun addEventResultListener(listener: OnEventResultListener) {
        ChatListenersWrapper.getInstance().addEventResultListener(listener)
    }

    override fun removeEventResultListener(listener: OnEventResultListener) {
        ChatListenersWrapper.getInstance().removeEventResultListener(listener)
    }

    override fun callbackEvent(function: String, errorCode: Int, errorMessage: String?) {
        ChatListenersWrapper.getInstance().callbackEvent(function, errorCode, errorMessage)
    }

    override fun addThreadChangeListener(listener: ChatThreadChangeListener) {
        ChatListenersWrapper.getInstance().addThreadChangeListener(listener)
    }

    override fun removeThreadChangeListener(listener: ChatThreadChangeListener) {
        ChatListenersWrapper.getInstance().removeThreadChangeListener(listener)
    }

    override fun releaseGlobalListener() {
        removeChatListener()
    }

}