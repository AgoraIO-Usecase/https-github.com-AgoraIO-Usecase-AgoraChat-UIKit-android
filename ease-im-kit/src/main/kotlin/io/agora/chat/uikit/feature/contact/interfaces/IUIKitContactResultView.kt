package io.agora.chat.uikit.feature.contact.interfaces

import io.agora.chat.uikit.common.ChatSilentModeResult
import io.agora.chat.uikit.common.interfaces.IControlDataView
import io.agora.chat.uikit.model.ChatUIKitUser

interface IUIKitContactResultView: IControlDataView {

    /**
     * Load contact list successfully.
     */
    fun loadContactListSuccess(list: MutableList<ChatUIKitUser>){}

    /**
     * Load contact list failed.
     */
    fun loadContactListFail(code: Int, error: String){}

    /**
     * Add contact successfully.
     */
    fun addContactSuccess(userId: String) {}

    /**
     * Add contact failed.
     */
    fun addContactFail(code: Int, error: String){}

    /**
     * Delete contact successfully.
     */
    fun deleteContactSuccess(){}

    /**
     * Delete contact failed.
     */
    fun deleteContactFail(code: Int, error: String){}

    /**
     * Fetch block list from server successfully.
     */
    fun fetchBlockListFromServerSuccess(list: MutableList<ChatUIKitUser>){}

    /**
     * Fetch block list from server failed.
     */
    fun fetchBlockListFromServerFail(code: Int, error: String){}

    /**
     * Get block list from local successfully.
     */
    fun getBlockListFromLocalSuccess(list: MutableList<ChatUIKitUser>){}

    /**
     * Get block list from local failed.
     */
    fun getBlockListFromLocalFail(code: Int, error: String){}

    /**
     * Add user to blocklist successfully.
     */
    fun addUserToBlockListSuccess(){}

    /**
     * Add user to blocklist failed.
     */
    fun addUserToBlockListFail(code: Int, error: String){}

    /**
     * Remove user from blocklist successfully.
     */
    fun removeUserFromBlockListSuccess(){}

    /**
     * Remove user from blocklist failed.
     */
    fun removeUserFromBlockListFail(code: Int, error: String){}

    /**
     * Accept invitation successfully.
     */
    fun acceptInvitationSuccess(){}

    /**
     * Accept invitation failed.
     */
    fun acceptInvitationFail(code: Int, error: String){}

    /**
     * Decline invitation successfully.
     */
    fun declineInvitationSuccess(){}

    /**
     * Decline invitation failed.
     */
    fun declineInvitationFail(code: Int, error: String){}

    /**
     * Clear conversation successfully.
     */
    fun clearConversationSuccess(conversationId: String?){}

    /**
     * Clear conversation failed.
     */
    fun clearConversationFail(code: Int, error: String?){}


    /**
     * Fetch user info from user successfully.
     */
    fun fetchUserInfoByUserSuccess(users: List<ChatUIKitUser>?) {}

    /**
     * Make contact interruption-free successfully.
     */
    fun makeSilentForContactSuccess(silentResult: ChatSilentModeResult){}

    /**
     * Make contact interruption-free failed.
     */
    fun makeSilentForContactFail(code: Int, error: String?){}

    /**
     * Cancel conversation do not disturb
     */
    fun cancelSilentForContactSuccess(){}

    /**
     * Cancel conversation do not disturb
     */
    fun cancelSilentForContactFail(code: Int, error: String?){}

}