package io.agora.chat.uikit.feature.invitation.interfaces

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.interfaces.IControlDataView
import io.agora.chat.uikit.model.ChatUIKitProfile

interface IUIKitNotificationResultView: IControlDataView {

    /**
     * Get all message successfully.
     */
    fun getLocalMessageSuccess(msgList:List<ChatMessage>)

    /**
     * Failed to get all message.
     */
    fun getLocalMessageFail(code: Int, error: String){}

    /**
     * load more message successfully.
     */
    fun loadMoreMessageSuccess(msgList:List<ChatMessage>)

    /**
     * Failed to load more message.
     */
    fun loadMoreMessageFail(code: Int, error: String){}

    /**
     * Fetch profile successfully.
     */
    fun fetchProfileSuccess(members:Map<String, ChatUIKitProfile>?){}

    /**
     * Failed to fetch profile.
     */
    fun fetchProfileFail(code: Int, error: String){}

    /**
     * Agree invite successfully.
     */
    fun agreeInviteSuccess(userId:String,msg:ChatMessage) {}

    /**
     * Failed agree invite.
     */
    fun agreeInviteFail(code: Int, error: String){}

    /**
     * Refuse invite successfully.
     */
    fun refuseInviteSuccess(){}

    /**
     * Failed to Refuse invite.
     */
    fun refuseInviteFail(code: Int, error: String){}
}