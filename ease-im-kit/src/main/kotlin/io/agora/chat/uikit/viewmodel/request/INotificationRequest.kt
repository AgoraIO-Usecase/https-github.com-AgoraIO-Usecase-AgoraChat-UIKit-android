package io.agora.chat.uikit.viewmodel.request

import android.content.Context
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.viewmodel.IAttachView

interface INotificationRequest : IAttachView {

    fun loadMoreMessage(startMsgId:String?="",limit:Int)

    fun fetchProfileInfo(members: List<String>?)

    fun agreeInvite(context: Context,msg:ChatMessage)

    fun refuseInvite(context: Context,msg:ChatMessage)

    fun removeInviteMsg(msg: ChatMessage)

    fun loadLocalData()


}