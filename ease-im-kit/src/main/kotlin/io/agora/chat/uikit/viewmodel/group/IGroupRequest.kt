package io.agora.chat.uikit.viewmodel.group

import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatGroupOptions
import io.agora.chat.uikit.viewmodel.IAttachView

interface IGroupRequest : IAttachView {
    /**
     * load group list
     */
    fun loadJoinedGroupData(page:Int)

    /**
     * load local group list
     */
    fun loadLocalJoinedGroupData()

    /**
     * Create Group
     * @param groupName
     * @param desc
     * @param members
     * @param reason
     * @param options
     */
    fun createGroup(
        groupName:String,
        desc:String,
        members:MutableList<String>,
        reason:String,
        options:ChatGroupOptions,
    )

    /**
     * Get Group Details
     * @param groupId
     */
    fun fetchGroupDetails(groupId:String)

    /**
     * Get Group Member
     * @param groupId
     */
    fun fetchGroupMemberFromService(groupId:String)

    /**
     * Load local member
     * @param groupId
     */
    fun loadLocalMember(groupId:String)

    /**
     * Add group members
     * @param groupId
     * @param members
     */
    fun addGroupMember(
        groupId: String,
        members: MutableList<String>,
    )

    /**
     * Remove group members
     * @param groupId
     * @param members
     */
    fun removeGroupMember(
        groupId: String,
        members: MutableList<String>,
    )

    /**
     * Leave Group
     * @param groupId
     */
    fun leaveChatGroup(groupId:String)

    /**
     * Destroy Group
     * @param groupId
     */
    fun destroyChatGroup(groupId:String)

    /**
     * Change Group Name
     * @param groupId
     * @param newName
     */
    fun changeChatGroupName(
        groupId: String,
        newName: String,
    )

    /**
     * Change Group Description
     * @param groupId
     * @param description
     */
    fun changeChatGroupDescription(
        groupId: String,
        description: String,
    )

    /**
     * Change Group Owner
     * @param groupId
     * @param newOwner
     */
    fun changeChatGroupOwner(
        groupId: String,
        newOwner: String,
    )

    /**
     * Get Group member attributes
     * @param groupId
     * @param userList
     * @param keyList
     */
    fun fetchGroupMemberAllAttributes(
        groupId: String,
        userList: List<String>,
        keyList: List<String>,
    )

    /**
     * set Group member attributes
     * @param groupId
     * @param userId
     * @param attribute
     */
    fun setGroupMemberAttributes(
        groupId: String,
        userId: String,
        attribute:MutableMap<String,String>
    )

    /**
     * fetch Group member info
     * @param members
     */
    fun fetchMemberInfo(
        groupId: String?,
        members: List<String>?
    )

    /**
     * Clears all the messages of the specified conversation.
     * @param conversationId The conversation ID.
     */
    fun clearConversationMessage(conversationId: String?)

    /**
     * Set the DND of the conversation.
     */
    fun makeSilentModeForConversation(conversationId: String,conversationType:ChatConversationType)

    /**
     * Cancel conversation do not disturb
     */
    fun cancelSilentForConversation(conversationId: String,conversationType:ChatConversationType)

    /**
     *  Changes the name of the message thread.
     */
    fun updateChatThreadName(chatThreadId: String, chatThreadName: String){}
}