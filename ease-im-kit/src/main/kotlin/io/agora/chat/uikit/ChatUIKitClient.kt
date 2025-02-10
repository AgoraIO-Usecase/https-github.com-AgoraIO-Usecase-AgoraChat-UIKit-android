package io.agora.chat.uikit

import android.content.Context
import io.agora.chat.uikit.common.ChatOptions
import io.agora.chat.uikit.common.ChatPresence
import io.agora.chat.uikit.common.ChatPresenceListener
import io.agora.chat.uikit.common.ChatThreadChangeListener
import io.agora.chat.uikit.common.enums.ChatUIKitCacheType
import io.agora.chat.uikit.common.impl.ChatUIKitClientImpl
import io.agora.chat.uikit.common.impl.OnError
import io.agora.chat.uikit.common.impl.OnSuccess
import io.agora.chat.uikit.common.helper.ChatUIKitNotifier
import io.agora.chat.uikit.common.helper.ChatUIKitPreferenceManager
import io.agora.chat.uikit.interfaces.UIKitChatRoomListener
import io.agora.chat.uikit.interfaces.ChatUIKitConnectionListener
import io.agora.chat.uikit.interfaces.ChatUIKitContactListener
import io.agora.chat.uikit.interfaces.ChatUIKitConversationListener
import io.agora.chat.uikit.interfaces.ChatUIKitGroupListener
import io.agora.chat.uikit.interfaces.IChatUIKitClient
import io.agora.chat.uikit.interfaces.ChatUIKitMessageListener
import io.agora.chat.uikit.interfaces.ChatUIKitMultiDeviceListener
import io.agora.chat.uikit.interfaces.OnEventResultListener
import io.agora.chat.uikit.model.ChatUIKitGroupProfile
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.ChatUIKitCustomActivityRoute
import io.agora.chat.uikit.provider.ChatUIKitGroupProfileProvider
import io.agora.chat.uikit.provider.ChatUIKitEmojiconInfoProvider
import io.agora.chat.uikit.provider.ChatUIKitSettingsProvider
import io.agora.chat.uikit.provider.ChatUIKitUserProfileProvider


/**
 * It is the main class of the Chat UIKit.
 */
object ChatUIKitClient {
    // Whether the debug mode is open in ChatUIKitClient.
    const val DEBUG: Boolean = true

    /**
     * Open Application first load block list from server.
     */
    var isLoadBlockListFromServer: Boolean? = false

    private val client: IChatUIKitClient by lazy {
        ChatUIKitClientImpl()
    }

    /**
     * Initialize the Chat UIKit.
     * @param context The application context.
     * @param options The options of the Chat SDK.
     */
    @Synchronized
    fun init(context: Context, options: ChatOptions, config: ChatUIKitConfig? = null): ChatUIKitClient {
        client.init(context, options)
        client.setConfig(config)
        return this
    }

    /**
     * Judge whether the uikit is be initialized.
     */
    fun isInited(): Boolean {
        return client.isInited()
    }

    fun releaseGlobalListener(){
        client.releaseGlobalListener()
    }

    /**
     * Temp for test.
     */
    fun login(userId: String,
              password: String,
              onSuccess: OnSuccess = {},
              onError: OnError = {_,_->}) {
        client.login(userId, password, onSuccess, onError)
    }

    /**
     * Login with user object by token.
     * @param user The user object, see [ChatUIKitUser].
     * @param token The token.
     * @param onSuccess The callback of success.
     * @param onError The callback of error.
     */
    fun login(user: ChatUIKitProfile,
              token: String,
              onSuccess: OnSuccess = {},
              onError: OnError = {_,_ ->}) {
        client.login(user, token, onSuccess, onError)
    }



    /**
     * Log out from the Chat SDK.
     * @param unbindDeviceToken Whether unbind the device token.
     * @param onSuccess The callback of success.
     * @param onError The callback of error.
     */
    fun logout(unbindDeviceToken: Boolean,
               onSuccess: OnSuccess = {},
               onError: OnError = {_,_ ->}) {
        client.logout(unbindDeviceToken, onSuccess, onError)
    }

    /**
     * Whether the user is logged in.
     */
    fun isLoggedIn(): Boolean {
        return client.isLoggedIn()
    }

    /**
     * Update the current user.
     */
    fun updateCurrentUser(user: ChatUIKitProfile) {
        client.updateCurrentUser(user)
    }

    /**
     * Get the current user.
     */
    fun getCurrentUser(): ChatUIKitProfile? {
        return client.getCurrentUser()
    }

    /**
     * Set the provider of the emoji icon.
     * @param provider The provider of the emoji icon.
     */
    fun setEmojiconInfoProvider(provider: ChatUIKitEmojiconInfoProvider): ChatUIKitClient {
        client.setEmojiconInfoProvider(provider)
        return this
    }

    /**
     * Set the conversation information provider.
     * @param provider The provider of the conversation information.
     */
    fun setGroupProfileProvider(provider: ChatUIKitGroupProfileProvider): ChatUIKitClient {
        client.setGroupProfileProvider(provider)
        return this
    }

    /**
     * Set the userinfo provider.
     * @param provider The provider of the userinfo.
     */
    fun setUserProfileProvider(provider: ChatUIKitUserProfileProvider): ChatUIKitClient {
        client.setUserProfileProvider(provider)
        return this
    }

    /**
     * Update the UIKit group information in cache.
     * @param profiles The profiles to update.
     */
    fun updateGroupInfo(profiles: List<ChatUIKitGroupProfile>) {
        client.updateGroupProfiles(profiles)
    }

    /**
     * Update the UIKit user information in cache.
     * @param users The profiles to update.
     */
    fun updateUsersInfo(users: List<ChatUIKitProfile>) {
        client.updateUsersInfo(users)
    }

    /**
     * Set the provider of the settings.
     * @param provider The provider of the settings.
     */
    fun setSettingsProvider(provider: ChatUIKitSettingsProvider): ChatUIKitClient {
        client.setSettingsProvider(provider)
        return this
    }

    /**
     * Set the activity route in UIKit.
     * @param route The provider of the activity route.
     */
    fun setCustomActivityRoute(route: ChatUIKitCustomActivityRoute): ChatUIKitClient {
        client.setCustomActivityRoute(route)
        return this
    }

    /**
     * Get the application context.
     */
    fun getContext(): Context? {
        return client.getContext()
    }

    /**
     * Get the UIKit SDK configurations.
     */
    fun getConfig(): ChatUIKitConfig? {
        return client.getConfig()
    }

    /**
     * Clear the cache.
     */
    fun clearCache(type: ChatUIKitCacheType? = ChatUIKitCacheType.ALL) {
        client.clearKitCache(type)
    }

    /**
     * Get the cache.
     */
    internal fun getCache() = client.getKitCache()

    /**
     * Get the notification helper in EaseUIKit.
     */
    fun getNotifier(): ChatUIKitNotifier? {
        return client.getNotifier()
    }

    /**
     * Get the emojicon provider.
     */
    fun getEmojiconInfoProvider(): ChatUIKitEmojiconInfoProvider? {
        return client.getEmojiconInfoProvider()
    }

    /**
     * Get the conversation information provider.
     */
    fun getGroupProfileProvider(): ChatUIKitGroupProfileProvider? {
        return client.getGroupProfileProvider()
    }

    /**
     * Get the userinfo provider.
     */
    fun getUserProvider(): ChatUIKitUserProfileProvider? {
        return client.getUserProvider()
    }

    /**
     * Get the settings provider.
     */
    fun getSettingsProvider(): ChatUIKitSettingsProvider? {
        return client.getSettingsProvider()
    }

    /**
     * Get the activity route provider.
     */
    fun getCustomActivityRoute(): ChatUIKitCustomActivityRoute? {
        return client.getCustomActivityRoute()
    }

    fun checkMutedConversationList(userId:String):Boolean{
        return getCache().getMutedConversationList().containsKey(userId)
    }

    /**
     * Add Connection Listener
     */
    fun addConnectionListener(connectListener:ChatUIKitConnectionListener){
        client.addConnectionListener(connectListener)
    }

    /**
     * Remove Connection Listener
     */
    fun removeConnectionListener(connectListener:ChatUIKitConnectionListener){
        client.removeConnectionListener(connectListener)
    }

    /**
     * Add ChatMessage Listener
     */
    fun addChatMessageListener(listener:ChatUIKitMessageListener){
        client.addChatMessageListener(listener)
    }

    /**
     * Remove ChatMessage Listener
     */
    fun removeChatMessageListener(listener:ChatUIKitMessageListener){
        client.removeChatMessageListener(listener)
    }

    /**
     * Add GroupChange Listener
     */
    fun addGroupChangeListener(listener:ChatUIKitGroupListener){
        client.addGroupChangeListener(listener)
    }

    /**
     * Remove GroupChange Listener
     */
    fun removeGroupChangeListener(listener:ChatUIKitGroupListener){
        client.removeGroupChangeListener(listener)
    }

    /**
     * Add Contact Listener
     */
    fun addContactListener(listener:ChatUIKitContactListener){
        client.addContactListener(listener)
    }

    /**
     * Remove Contact Listener
     */
    fun removeContactListener(listener:ChatUIKitContactListener){
        client.removeContactListener(listener)
    }

    /**
     * Add Conversation Listener
     */
    fun addConversationListener(listener:ChatUIKitConversationListener){
        client.addConversationListener(listener)
    }

    /**
     * Remove Conversation Listener
     */
    fun removeConversationListener(listener:ChatUIKitConversationListener){
        client.removeConversationListener(listener)
    }

    /**
     * Add Presence Listener
     */
    fun addPresenceListener(listener:ChatPresenceListener){
        client.addPresenceListener(listener)
    }

    /**
     * Remove Presence Listener
     */
    fun removePresenceListener(listener:ChatPresenceListener){
        client.removePresenceListener(listener)
    }

    /**
     * Add ChatRoomChange Listener
     */
    fun addChatRoomChangeListener(listener:UIKitChatRoomListener){
        client.addChatRoomChangeListener(listener)
    }

    /**
     * Remove ChatRoomChange Listener
     */
    fun removeChatRoomChangeListener(listener:UIKitChatRoomListener){
        client.removeChatRoomChangeListener(listener)
    }

    /**
     * Add MultiDevice Listener
     */
    fun addMultiDeviceListener(listener:ChatUIKitMultiDeviceListener){
        client.addMultiDeviceListener(listener)
    }

    /**
     * Remove MultiDevice Listener
     */
    fun removeMultiDeviceListener(listener:ChatUIKitMultiDeviceListener){
        client.removeMultiDeviceListener(listener)
    }

    /**
     * Add Event Result Listener
     */
    fun addEventResultListener(listener: OnEventResultListener){
        client.addEventResultListener(listener)
    }

    /**
     * Remove Event Result Listener
     */
    fun removeEventResultListener(listener:OnEventResultListener){
        client.removeEventResultListener(listener)
    }

    /**
     * Set Event Result Callback
     */
    fun setEventResultCallback(function: String, errorCode: Int, errorMessage: String?){
        client.callbackEvent(function, errorCode, errorMessage)
    }

    /**
     * Add Thread Change Listener
     */
    fun addThreadChangeListener(listener: ChatThreadChangeListener){
        client.addThreadChangeListener(listener)
    }

    /**
     * Remove Thread Change Listener
     */
    fun removeThreadChangeListener(listener: ChatThreadChangeListener){
        client.removeThreadChangeListener(listener)
    }

}