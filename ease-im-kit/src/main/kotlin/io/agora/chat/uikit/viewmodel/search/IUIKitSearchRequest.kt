package io.agora.chat.uikit.viewmodel.search

import io.agora.chat.uikit.common.ChatSearchDirection
import io.agora.chat.uikit.common.ChatSearchScope
import io.agora.chat.uikit.viewmodel.IAttachView

interface IUIKitSearchRequest: IAttachView {

    fun searchUser(query:String)

    fun searchBlockUser(query: String)

    fun searchConversation(query:String)

    fun searchMessage(
        keywords:String,
        conversationId:String = "",
        timeStamp:Long = -1,
        from:String? = "",
        direction:ChatSearchDirection = ChatSearchDirection.UP,
        chatScope:ChatSearchScope
    )
}