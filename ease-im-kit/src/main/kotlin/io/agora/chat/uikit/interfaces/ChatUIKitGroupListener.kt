package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.common.ChatGroupChangeListener
import io.agora.chat.uikit.common.ChatShareFile


abstract class ChatUIKitGroupListener : ChatGroupChangeListener {

    abstract override fun onGroupDestroyed(groupId: String?, groupName: String?)

    override fun onUserRemoved(groupId: String?, groupName: String?) {}

    override fun onInvitationReceived(
        groupId: String?,
        groupName: String?,
        inviter: String?,
        reason: String?
    ) {
    }

    override fun onRequestToJoinReceived(
        groupId: String?,
        groupName: String?,
        applicant: String?,
        reason: String?
    ) {
    }

    override fun onRequestToJoinAccepted(groupId: String?, groupName: String?, accepter: String?) {}
    override fun onRequestToJoinDeclined(
        groupId: String?,
        groupName: String?,
        decliner: String?,
        reason: String?
    ) {
    }

    override fun onInvitationAccepted(groupId: String?, invitee: String?, reason: String?) {}
    override fun onInvitationDeclined(groupId: String?, invitee: String?, reason: String?) {}
    override fun onAutoAcceptInvitationFromGroup(
        groupId: String?,
        inviter: String?,
        inviteMessage: String?
    ) {
    }

    override fun onMuteListAdded(groupId: String?, mutes: List<String?>?, muteExpire: Long) {}
    override fun onMuteListRemoved(groupId: String?, mutes: List<String?>?) {}
    override fun onAdminAdded(groupId: String?, administrator: String?) {}
    override fun onAdminRemoved(groupId: String?, administrator: String?) {}
    override fun onOwnerChanged(groupId: String?, newOwner: String?, oldOwner: String?) {}
    override fun onMemberJoined(groupId: String?, member: String?) {}
    override fun onMemberExited(groupId: String?, member: String?) {}
    override fun onAnnouncementChanged(groupId: String?, announcement: String?) {}
    override fun onSharedFileAdded(groupId: String?, sharedFile: ChatShareFile?) {}
    override fun onSharedFileDeleted(groupId: String?, fileId: String?) {}
    override fun onWhiteListAdded(groupId: String?, whitelist: List<String?>?) {}
    override fun onWhiteListRemoved(groupId: String?, whitelist: List<String?>?) {}
    override fun onAllMemberMuteStateChanged(groupId: String?, isMuted: Boolean) {}
}