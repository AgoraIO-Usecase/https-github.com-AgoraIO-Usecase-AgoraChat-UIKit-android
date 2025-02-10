package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.common.ChatRoomChangeListener


abstract class UIKitChatRoomListener : ChatRoomChangeListener {
    abstract override fun onChatRoomDestroyed(roomId: String?, roomName: String?)
    abstract override fun onRemovedFromChatRoom(
        reason: Int,
        roomId: String?,
        roomName: String?,
        participant: String?
    )

    abstract override fun onMemberJoined(roomId: String?, participant: String?)
    abstract override fun onMemberExited(roomId: String?, roomName: String?, participant: String?)
    override fun onMuteListAdded(chatRoomId: String?, mutes: List<String?>?, expireTime: Long) {}
    override fun onMuteListRemoved(chatRoomId: String?, mutes: List<String?>?) {}
    override fun onAdminAdded(chatRoomId: String?, admin: String?) {}
    override fun onAdminRemoved(chatRoomId: String?, admin: String?) {}
    override fun onOwnerChanged(chatRoomId: String?, newOwner: String?, oldOwner: String?) {}
    override fun onAnnouncementChanged(chatroomId: String?, announcement: String?) {}
    override fun onWhiteListAdded(chatRoomId: String?, whitelist: List<String?>?) {}
    override fun onWhiteListRemoved(chatRoomId: String?, whitelist: List<String?>?) {}
    override fun onAllMemberMuteStateChanged(chatRoomId: String?, isMuted: Boolean) {}
}