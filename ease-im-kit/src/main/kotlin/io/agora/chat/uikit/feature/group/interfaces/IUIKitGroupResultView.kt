package io.agora.chat.uikit.feature.group.interfaces

import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.common.ChatSilentModeResult
import io.agora.chat.uikit.common.interfaces.IControlDataView
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser

interface IUIKitGroupResultView: IControlDataView {

    /**
     * Load group list successfully.
     */
    fun loadGroupListSuccess(list: MutableList<ChatGroup>){}

    /**
     * Failed to Load group list.
     */
    fun loadGroupListFail(code: Int, error: String){}

    /**
     * Load local group list successfully.
     */
    fun loadLocalGroupListSuccess(list: MutableList<ChatGroup>){}

    /**
     * Failed to Load local group list.
     */
    fun loadLocalGroupListFail(code: Int, error: String){}

    /**
     * Create group successfully.
     */
    fun createGroupSuccess(group: ChatGroup){}

    /**
     * Failed to Create group
     */
    fun createGroupFail(code: Int, error: String){}

    /**
     * Fetch group member list successfully.
     */
    fun fetchGroupMemberSuccess(user:List<ChatUIKitUser>){}

    /**
     * Failed to Fetch group member list
     */
    fun fetchGroupMemberFail(code: Int, error: String){}

    /**
     * Load local group member list successfully.
     */
    fun loadLocalMemberSuccess(members: List<ChatUIKitUser>){}

    /**
     * Get group details successfully
     */
    fun fetchGroupDetailSuccess(group: ChatGroup){}

    /**
     * Failed to Get group details
     */
    fun fetchGroupDetailFail(code: Int, error: String){}

    /**
     * Leave group successfully
     */
    fun leaveChatGroupSuccess(){}

    /**
     * Failed to leave group
     */
    fun leaveChatGroupFail(code: Int, error: String){}

    /**
     * Destroy group successfully
     */
    fun destroyChatGroupSuccess(){}

    /**
     * Failed to Destroy group
     */
    fun destroyChatGroupFail(code: Int, error: String){}

    /**
     * Add group member successfully
     */
    fun addGroupMemberSuccess(){}

    /**
     * Failed to Add group member
     */
    fun addGroupMemberFail(code: Int, error: String){}

    /**
     * Remove group member successfully
     */
    fun removeChatGroupMemberSuccess(){}

    /**
     * Failed to remove group member
     */
    fun removeChatGroupMemberFail(code: Int, error: String){}

    /**
     * Change group name successfully
     */
    fun changeChatGroupNameSuccess(){}

    /**
     * Failed to change group name
     */
    fun changeChatGroupNameFail(code: Int, error: String){}

    /**
     * Change group description successfully
     */
    fun changeChatGroupDescriptionSuccess(){}

    /**
     * Failed to change group description
     */
    fun changeChatGroupDescriptionFail(code: Int, error: String){}

    /**
     * Change group owner successfully
     */
    fun changeChatGroupOwnerSuccess(){}

    /**
     * Failed to change group owner
     */
    fun changeChatGroupOwnerFail(code: Int, error: String){}

    /**
     * Get group member attribute successfully
     * @param userId
     * @param attributes
     */
    fun getGroupMemberAllAttributesSuccess(userId: List<String>, attributes:MutableMap<String,MutableMap<String,String>>){}

    /**
     * Failed to Get group member attribute
     */
    fun getGroupMemberAllAttributesFail(code: Int, error: String){}

    /**
     * Set group member attribute successfully
     */
    fun setGroupMemberAttributesSuccess(){}

    /**
     * Failed to Set group member attribute
     */
    fun setGroupMemberAttributesFail(code: Int, error: String){}

    /**
     * Fetch group member info successfully
     */
    fun fetchMemberInfoSuccess(members:Map<String,ChatUIKitProfile>?){}

    /**
     * Failed to Fetch group member info
     */
    fun fetchMemberInfoFail(code: Int, error: String){}

    /**
     * Clear conversation successfully.
     */
    fun clearConversationByGroupSuccess(conversationId: String?){}

    /**
     * Clear conversation failed.
     */
    fun clearConversationByGroupFail(code: Int, error: String?){}

    /**
     * Make contact interruption-free successfully.
     */
    fun makeSilentForGroupSuccess(silentResult: ChatSilentModeResult){}

    /**
     * Make contact interruption-free failed.
     */
    fun makeSilentForGroupFail(code: Int, error: String?){}

    /**
     * Cancel conversation do not disturb
     */
    fun cancelSilentForGroupSuccess(){}

    /**
     * Cancel conversation do not disturb
     */
    fun cancelSilentForGroupFail(code: Int, error: String?){}

    /**
     * Update chat thread name success
     */
    fun changeThreadNameSuccess(){}

    /**
     * Update chat thread name fail
     */
    fun changeThreadNameFail(code: Int, error: String?){}
}