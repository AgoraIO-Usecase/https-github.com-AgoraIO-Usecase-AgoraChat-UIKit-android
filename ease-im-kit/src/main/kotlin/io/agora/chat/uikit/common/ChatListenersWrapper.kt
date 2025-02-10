package io.agora.chat.uikit.common

import io.agora.MultiDeviceListener.CONTACT_ACCEPT
import io.agora.MultiDeviceListener.CONTACT_REMOVE
import io.agora.MultiDeviceListener.GROUP_CREATE
import io.agora.MultiDeviceListener.GROUP_DESTROY
import io.agora.MultiDeviceListener.GROUP_JOIN
import io.agora.MultiDeviceListener.GROUP_LEAVE
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.createUnsentMessage
import io.agora.chat.uikit.common.extensions.getUserInfo
import io.agora.chat.uikit.common.extensions.isGroupChat
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.common.helper.ChatUIKitAtMessageHelper
import io.agora.chat.uikit.feature.invitation.enums.InviteMessageStatus
import io.agora.chat.uikit.feature.invitation.helper.ChatUIKitNotificationMsgManager
import io.agora.chat.uikit.feature.invitation.helper.RequestMsgHelper
import io.agora.chat.uikit.interfaces.OnEventResultListener
import io.agora.chat.uikit.model.ChatUIKitEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections


internal class ChatListenersWrapper : ChatConnectionListener, ChatMessageListener, ChatGroupChangeListener,
    ChatContactListener, ChatConversationListener, ChatPresenceListener,
    ChatMultiDeviceListener, ChatThreadChangeListener {

    private val chatConnectionListener = Collections.synchronizedList(mutableListOf<ChatConnectionListener>())
    private val chatMessageListener = Collections.synchronizedList(mutableListOf<ChatMessageListener>())
    private val chatGroupChangeListener = Collections.synchronizedList(mutableListOf<ChatGroupChangeListener>())
    private val chatContactListener = Collections.synchronizedList(mutableListOf<ChatContactListener>())
    private val chatConversationListener = Collections.synchronizedList(mutableListOf<ChatConversationListener>())
    private val chatPresenceListener = Collections.synchronizedList(mutableListOf<ChatPresenceListener>())
    private val chatRoomChangeListener = Collections.synchronizedList(mutableListOf<ChatRoomChangeListener>())
    private val chatMultiDeviceListener = Collections.synchronizedList(mutableListOf<ChatMultiDeviceListener>())
    private val eventResultListener = Collections.synchronizedList(mutableListOf<OnEventResultListener>())
    private val chatThreadChangeListener = Collections.synchronizedList(mutableListOf<ChatThreadChangeListener>())

    fun addListeners(){
        ChatClient.getInstance().addConnectionListener(this)
        ChatClient.getInstance().chatManager().addMessageListener(this)
        ChatClient.getInstance().chatManager().addConversationListener(this)
        ChatClient.getInstance().groupManager().addGroupChangeListener(this)
        ChatClient.getInstance().contactManager().setContactListener(this)
        ChatClient.getInstance().presenceManager().addListener(this)
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(chatroomListener)
        ChatClient.getInstance().addMultiDeviceListener(this)
        ChatClient.getInstance().chatThreadManager().addChatThreadChangeListener(this)
    }

    fun removeListeners(){
        ChatClient.getInstance().removeConnectionListener(this)
        ChatClient.getInstance().chatManager().removeMessageListener(this)
        ChatClient.getInstance().chatManager().removeConversationListener(this)
        ChatClient.getInstance().groupManager().removeGroupChangeListener(this)
        ChatClient.getInstance().contactManager().removeContactListener(this)
        ChatClient.getInstance().presenceManager().removeListener(this)
        ChatClient.getInstance().chatroomManager().removeChatRoomListener(chatroomListener)
        ChatClient.getInstance().removeMultiDeviceListener(this)
        ChatClient.getInstance().chatThreadManager().removeChatThreadChangeListener(this)

        chatConnectionListener.clear()
        chatMessageListener.clear()
        chatGroupChangeListener.clear()
        chatContactListener.clear()
        chatConversationListener.clear()
        chatPresenceListener.clear()
        chatRoomChangeListener.clear()
        chatMultiDeviceListener.clear()
        chatThreadChangeListener.clear()
    }

    fun addConnectionListener(listener: ChatConnectionListener){
        if (chatConnectionListener.contains(listener)) return
        chatConnectionListener.add(listener)
    }

    fun removeConnectionListener(listener: ChatConnectionListener){
        chatConnectionListener.remove(listener)
    }

    fun addChatMessageListener(listener:ChatMessageListener){
        if (chatMessageListener.contains(listener)) return
        chatMessageListener.add(listener)
    }

    fun removeChatMessageListener(listener:ChatMessageListener){
        chatMessageListener.remove(listener)
    }

    fun addGroupChangeListener(listener:ChatGroupChangeListener){
        if (chatGroupChangeListener.contains(listener)) return
        chatGroupChangeListener.add(listener)
    }

    fun removeGroupChangeListener(listener:ChatGroupChangeListener){
        chatGroupChangeListener.remove(listener)
    }

    fun addContactListener(listener:ChatContactListener){
        if (chatContactListener.contains(listener)) return
        chatContactListener.add(listener)
    }

    fun removeContactListener(listener:ChatContactListener){
        chatContactListener.remove(listener)
    }

    fun addConversationListener(listener:ChatConversationListener){
        if (chatConversationListener.contains(listener)) return
        chatConversationListener.add(listener)
    }

    fun removeConversationListener(listener:ChatConversationListener){
        chatConversationListener.remove(listener)
    }

    fun addPresenceListener(listener:ChatPresenceListener){
        if (chatPresenceListener.contains(listener)) return
        chatPresenceListener.add(listener)
    }

    fun removePresenceListener(listener:ChatPresenceListener){
        chatPresenceListener.remove(listener)
    }

    fun addChatRoomChangeListener(listener:ChatRoomChangeListener){
        if (chatRoomChangeListener.contains(listener)) return
        chatRoomChangeListener.add(listener)
    }

    fun removeChatRoomChangeListener(listener:ChatRoomChangeListener){
        chatRoomChangeListener.remove(listener)
    }

    fun addMultiDeviceListener(listener:ChatMultiDeviceListener){
        if (chatMultiDeviceListener.contains(listener)) return
        chatMultiDeviceListener.add(listener)
    }

    fun removeMultiDeviceListener(listener:ChatMultiDeviceListener){
        chatMultiDeviceListener.remove(listener)
    }

    fun addEventResultListener(listener:OnEventResultListener){
        if (eventResultListener.contains(listener)) return
        eventResultListener.add(listener)
    }

    fun removeEventResultListener(listener:OnEventResultListener){
        eventResultListener.remove(listener)
    }

    fun addThreadChangeListener(listener:ChatThreadChangeListener){
        chatThreadChangeListener.add(listener)
    }

    fun removeThreadChangeListener(listener:ChatThreadChangeListener){
        chatThreadChangeListener.remove(listener)
    }

    companion object {

        private var instance: ChatListenersWrapper? = null

        fun getInstance(): ChatListenersWrapper {
            if (instance == null) {
                synchronized(ChatListenersWrapper::class.java) {
                    if (instance == null) {
                        instance = ChatListenersWrapper()
                    }
                }
            }
            return instance!!
        }
    }


    /**  ChatConnectionListener  */
    override fun onConnected() {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onConnected()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDisconnected(errorCode: Int) {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onDisconnected(errorCode)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onTokenWillExpire() {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onTokenWillExpire()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onTokenExpired() {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onTokenExpired()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onLogout(errorCode: Int, info: ChatLoginExtensionInfo?) {
        chatConnectionListener.let {
            for (connectionListener in it) {
                try {
                    connectionListener.onLogout(errorCode, info)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onOfflineMessageSyncStart() {

    }

    override fun onOfflineMessageSyncFinish() {

    }

    /**  ChatConnectionListener  */
    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
        // Update message userinfo when receive messages.
        CoroutineScope(Dispatchers.IO).launch {
            messages?.forEach { msg ->
                msg.getUserInfo(true)
            }
            try {
                ChatUIKitAtMessageHelper.get().parseMessages(messages)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageReceived(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onCmdMessageReceived(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onGroupMessageRead(groupReadAcks: MutableList<ChatGroupReadAck>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onGroupMessageRead(groupReadAcks)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageChanged(message: ChatMessage?, change: Any?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageChanged(message, change)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageContentChanged(
        messageModified: ChatMessage?,
        operatorId: String?,
        operationTime: Long
    ) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageContentChanged(messageModified,operatorId,operationTime)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageDelivered(messages: MutableList<ChatMessage>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageDelivered(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageRead(messages: MutableList<ChatMessage>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageRead(messages)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageRecalledWithExt(recallMessageInfo: MutableList<ChatRecallMessageInfo>?) {
        if (recallMessageInfo != null && recallMessageInfo.size > 0) {
            for (message in recallMessageInfo) {
                message.recallMessage?.let {
                    if (it.isGroupChat() && ChatUIKitAtMessageHelper.get().isAtMeMsg(it)) {
                        ChatUIKitAtMessageHelper.get().removeAtMeGroup(it.conversationId())
                    }
                    val recallMsg = it.createUnsentMessage(true)
                    ChatClient.getInstance().chatManager().saveMessage(recallMsg)
                }
            }
        }
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessageRecalledWithExt(recallMessageInfo)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onReactionChanged(messageReactionChangeList: MutableList<ChatMessageReactionChange>?) {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onReactionChanged(messageReactionChangeList)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onReadAckForGroupMessageUpdated() {
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onReadAckForGroupMessageUpdated()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessagePinChanged(
        messageId: String?,
        conversationId: String?,
        pinOperation: ChatMessagePinOperation?,
        pinInfo: ChatMessagePinInfo?
    ) {
        ChatLog.d("ChatListenersWrapper", "onMessagePinChanged messageId:$messageId conversationId:$conversationId pinOperation:$pinOperation pinInfo:$pinInfo")
        chatMessageListener.let {
            for (messageListener in it) {
                try {
                    messageListener.onMessagePinChanged(messageId, conversationId, pinOperation, pinInfo)
                } catch (e: Exception) {
                    e.printStackTrace()
                    ChatLog.e("ChatListenersWrapper", "onMessagePinChanged error:${e.message}")
                }
            }
        }
    }

    /**  GroupChangeListener  */
    override fun onInvitationReceived(
        groupId: String?,
        groupName: String?,
        inviter: String?,
        reason: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onInvitationReceived(groupId,groupName,inviter,reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestToJoinReceived(
        groupId: String?,
        groupName: String?,
        applicant: String?,
        reason: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onRequestToJoinReceived(groupId,groupName,applicant,reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestToJoinAccepted(groupId: String?, groupName: String?, accepter: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onRequestToJoinAccepted(groupId,groupName,accepter)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestToJoinDeclined(
        groupId: String?,
        groupName: String?,
        decliner: String?,
        reason: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onRequestToJoinDeclined(groupId,groupName,decliner,reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onInvitationAccepted(groupId: String?, invitee: String?, reason: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onInvitationAccepted(groupId,invitee,reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onInvitationDeclined(groupId: String?, invitee: String?, reason: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onInvitationDeclined(groupId,invitee,reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onUserRemoved(groupId: String?, groupName: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onUserRemoved(groupId,groupName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onGroupDestroyed(groupId: String?, groupName: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onGroupDestroyed(groupId,groupName)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAutoAcceptInvitationFromGroup(
        groupId: String?,
        inviter: String?,
        inviteMessage: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAutoAcceptInvitationFromGroup(groupId,inviter,inviteMessage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMuteListAdded(groupId: String?, mutes: MutableList<String>?, muteExpire: Long) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMuteListAdded(groupId,mutes,muteExpire)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMuteListRemoved(groupId: String?, mutes: MutableList<String>?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMuteListRemoved(groupId,mutes)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onWhiteListAdded(groupId: String?, whitelist: MutableList<String>?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onWhiteListAdded(groupId,whitelist)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onWhiteListRemoved(groupId: String?, whitelist: MutableList<String>?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onWhiteListRemoved(groupId,whitelist)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAllMemberMuteStateChanged(groupId: String?, isMuted: Boolean) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAllMemberMuteStateChanged(groupId,isMuted)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAdminAdded(groupId: String?, administrator: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAdminAdded(groupId,administrator)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAdminRemoved(groupId: String?, administrator: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAdminRemoved(groupId,administrator)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onOwnerChanged(groupId: String?, newOwner: String?, oldOwner: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onOwnerChanged(groupId,newOwner,oldOwner)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMemberJoined(groupId: String?, member: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMemberJoined(groupId,member)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMemberExited(groupId: String?, member: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onMemberExited(groupId,member)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onAnnouncementChanged(groupId: String?, announcement: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onAnnouncementChanged(groupId,announcement)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSharedFileAdded(groupId: String?, sharedFile: ChatShareFile?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onSharedFileAdded(groupId,sharedFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSharedFileDeleted(groupId: String?, fileId: String?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onSharedFileDeleted(groupId,fileId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSpecificationChanged(group: ChatGroup?) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onSpecificationChanged(group)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onGroupMemberAttributeChanged(
        groupId: String?,
        userId: String?,
        attribute: MutableMap<String, String>?,
        from: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onGroupMemberAttributeChanged(groupId, userId, attribute, from)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestToJoinDeclined(
        groupId: String?,
        groupName: String?,
        decliner: String?,
        reason: String?,
        applicant: String?
    ) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onRequestToJoinDeclined(groupId, groupName, decliner, reason, applicant)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onStateChanged(group: ChatGroup?, isDisabled: Boolean) {
        chatGroupChangeListener.let {
            for (groupListener in it) {
                try {
                    groupListener.onStateChanged(group, isDisabled)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ContactListener  */
    override fun onContactAdded(username: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onContactAdded(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onContactDeleted(username: String?) {
        deleteDefaultContactAgreedMsg(username)
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onContactDeleted(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onContactInvited(username: String?, reason: String?) {
        saveDefaultContactInvitedMsg(username, reason)
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onContactInvited(username,reason)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onFriendRequestAccepted(username: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onFriendRequestAccepted(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onFriendRequestDeclined(username: String?) {
        chatContactListener.let {
            for (contactListener in it) {
                try {
                    contactListener.onFriendRequestDeclined(username)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ConversationListener  */
    override fun onConversationUpdate() {
        chatConversationListener.let {
            for (conversationListener in it) {
                try {
                    conversationListener.onConversationUpdate()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onConversationRead(from: String?, to: String?) {
        chatConversationListener.let {
            for (conversationListener in it) {
                try {
                    conversationListener.onConversationRead(from, to)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  PresenceListener  */
    override fun onPresenceUpdated(presences: MutableList<ChatPresence>?) {
        chatPresenceListener.let {
            for (conversationListener in it) {
                try {
                    conversationListener.onPresenceUpdated(presences)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ChatRoomChangeListener  */
    private val chatroomListener by lazy { object : ChatRoomChangeListener{

        override fun onChatRoomDestroyed(roomId: String?, roomName: String?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onChatRoomDestroyed(roomId,roomName)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onMemberJoined(roomId: String?, participant: String?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onMemberJoined(roomId,participant)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onMemberExited(roomId,roomName,participant)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onRemovedFromChatRoom(
            reason: Int,
            roomId: String?,
            roomName: String?,
            participant: String?
        ) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onRemovedFromChatRoom(reason,roomId,roomName,participant)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onMuteListAdded(
            chatRoomId: String?,
            mutes: MutableList<String>?,
            expireTime: Long
        ) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onMuteListAdded(chatRoomId, mutes, expireTime)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onMuteListRemoved(chatRoomId: String?, mutes: MutableList<String>?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onMuteListRemoved(chatRoomId, mutes)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onWhiteListAdded(chatRoomId: String?, whitelist: MutableList<String>?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onWhiteListAdded(chatRoomId, whitelist)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onWhiteListRemoved(chatRoomId: String?, whitelist: MutableList<String>?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onWhiteListRemoved(chatRoomId, whitelist)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onAllMemberMuteStateChanged(chatRoomId: String?, isMuted: Boolean) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onAllMemberMuteStateChanged(chatRoomId, isMuted)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onAdminAdded(chatRoomId: String?, admin: String?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onAdminAdded(chatRoomId, admin)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onAdminRemoved(chatRoomId: String?, admin: String?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onAdminRemoved(chatRoomId, admin)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onOwnerChanged(chatRoomId: String?, newOwner: String?, oldOwner: String?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onOwnerChanged(chatRoomId, newOwner, oldOwner)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        override fun onAnnouncementChanged(chatRoomId: String?, announcement: String?) {
            chatRoomChangeListener.let {
                for (chatroomListener in it) {
                    try {
                        chatroomListener.onAnnouncementChanged(chatRoomId, announcement)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }}

    /**  MultiDeviceListener  */
    override fun onContactEvent(event: Int, target: String?, ext: String?) {
        defaultMultiDeviceContactEvent(event,target,ext)
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onContactEvent(event,target,ext)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onGroupEvent(event: Int, target: String?, usernames: MutableList<String>?) {
        defaultMultiDeviceGroupEvent(event,target,usernames)
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onGroupEvent(event,target,usernames)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onConversationEvent(
        event: Int,
        conversationId: String?,
        type: ChatConversationType?
    ) {
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onConversationEvent(event, conversationId, type)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onMessageRemoved(conversationId: String?, deviceId: String?) {
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onMessageRemoved(conversationId, deviceId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadEvent(event: Int, target: String?, usernames: MutableList<String>?) {
        chatMultiDeviceListener.let {
            for (emMultiDeviceListener in it) {
                try {
                    emMultiDeviceListener.onChatThreadEvent(event, target, usernames)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**  ThreadChangeListener  */
    override fun onChatThreadCreated(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadCreated(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadUpdated(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadUpdated(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadDestroyed(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadDestroyed(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onChatThreadUserRemoved(event: ChatThreadEvent?) {
        chatThreadChangeListener.let {
            for (emChatThreadChangeListener in it) {
                try {
                    emChatThreadChangeListener.onChatThreadUserRemoved(event)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveDefaultContactInvitedMsg(username: String?, reason: String?){
        val useDefaultContactSystemMsg = ChatUIKitClient.getConfig()?.systemMsgConfig?.useDefaultContactSystemMsg ?: false
        if (useDefaultContactSystemMsg){
            var isExist = false
            val allRequestMessage = ChatUIKitNotificationMsgManager.getInstance().getAllNotifyMessage()
            val localContact = ChatClient.getInstance().contactManager().contactsFromLocal
            allRequestMessage.map {msg->
                if (msg.ext()[ChatUIKitConstant.SYSTEM_MESSAGE_FROM] == username || localContact.contains(username)){
                    isExist = true
                }
            }
            if (!isExist){
                val ext: MutableMap<String, Any> = ChatUIKitNotificationMsgManager.getInstance().createMsgExt()
                username?.let {
                    ext[ChatUIKitConstant.SYSTEM_MESSAGE_FROM] = it
                }
                reason?.let {
                    ext[ChatUIKitConstant.SYSTEM_MESSAGE_REASON] = it
                }
                ext[ChatUIKitConstant.SYSTEM_MESSAGE_STATUS] = InviteMessageStatus.BEINVITEED.name
                ChatUIKitClient.getContext()?.let {
                    ChatUIKitNotificationMsgManager.getInstance()
                        .createMessage( RequestMsgHelper.getSystemMessage(it,ext), ext)

                    ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name)
                        .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD.name, ChatUIKitEvent.TYPE.NOTIFY))

                }
            }
        }
    }

    private fun deleteDefaultContactAgreedMsg(username: String?){
        val useDefaultContactSystemMsg = ChatUIKitClient.getConfig()?.systemMsgConfig?.useDefaultContactSystemMsg ?: false
        if (useDefaultContactSystemMsg){
            val allRequestMessage = ChatUIKitNotificationMsgManager.getInstance().getAllNotifyMessage()
            allRequestMessage.map {msg->
                if (msg.ext()[ChatUIKitConstant.SYSTEM_MESSAGE_FROM] == username &&
                    msg.ext()[ChatUIKitConstant.SYSTEM_MESSAGE_STATUS] == InviteMessageStatus.AGREED.name
                ){
                    ChatUIKitNotificationMsgManager.getInstance().removeMessage(msg)
                    ChatUIKitClient.getContext()?.let {
                        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name)
                            .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.REMOVE.name, ChatUIKitEvent.TYPE.NOTIFY))

                    }
                }
            }
        }
    }

    private fun defaultMultiDeviceContactEvent(event: Int, target: String?, ext: String?){
        val useDefaultMultiDeviceContactEvent = ChatUIKitClient.getConfig()?.multiDeviceConfig?.useDefaultMultiDeviceContactEvent ?: false
        if (useDefaultMultiDeviceContactEvent){
            when(event){
                CONTACT_REMOVE -> {
                    val allRequestMessage = ChatUIKitNotificationMsgManager.getInstance().getAllNotifyMessage()
                    allRequestMessage.map {msg->
                        if (msg.ext()[ChatUIKitConstant.SYSTEM_MESSAGE_FROM] == target &&
                            msg.ext()[ChatUIKitConstant.SYSTEM_MESSAGE_STATUS] == InviteMessageStatus.AGREED.name
                        ){
                            ChatUIKitNotificationMsgManager.getInstance().removeMessage(msg)
                            ChatUIKitClient.getContext()?.let {
                                ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name)
                                    .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.REMOVE.name, ChatUIKitEvent.TYPE.CONTACT))

                                ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.REMOVE.name)
                                    .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.REMOVE.name, ChatUIKitEvent.TYPE.NOTIFY))
                            }
                        }
                    }
                }
                CONTACT_ACCEPT -> {
                    ChatUIKitClient.getContext()?.let {
                        ChatUIKitFlowBus.withStick<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name)
                            .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD.name, ChatUIKitEvent.TYPE.CONTACT))
                    }
                }
                else -> {}
            }

        }
    }

    private fun defaultMultiDeviceGroupEvent(event: Int, target: String?, usernames: MutableList<String>?){
        val useDefaultMultiDeviceGroupEvent = ChatUIKitClient.getConfig()?.multiDeviceConfig?.useDefaultMultiDeviceGroupEvent ?: false
        if (useDefaultMultiDeviceGroupEvent){
            when(event){
                GROUP_CREATE -> {
                    ChatUIKitClient.getContext()?.let {
                        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name)
                            .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD.name, ChatUIKitEvent.TYPE.GROUP, target))
                    }
                }
                GROUP_DESTROY -> {
                    ChatUIKitClient.getContext()?.let {
                        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.DESTROY.name)
                            .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.DESTROY.name, ChatUIKitEvent.TYPE.GROUP, target))
                    }
                }
                GROUP_JOIN -> {
                    ChatUIKitClient.getContext()?.let {
                        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name)
                            .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD.name, ChatUIKitEvent.TYPE.GROUP, target))
                    }
                }
                GROUP_LEAVE -> {
                    ChatUIKitClient.getContext()?.let {
                        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.LEAVE.name)
                            .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.LEAVE.name, ChatUIKitEvent.TYPE.GROUP, target))
                    }
                }
                else -> {}
            }
        }
    }



    @Synchronized
    internal fun callbackEvent(function: String, errorCode: Int, errorMessage: String?) {
        if (eventResultListener.isEmpty()) {
            return
        }
        eventResultListener.iterator().forEach { listener ->
            listener.onEventResult(function, errorCode, errorMessage)
        }
    }

}