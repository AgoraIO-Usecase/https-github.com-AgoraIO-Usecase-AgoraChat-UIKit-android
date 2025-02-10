package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.common.ChatConnectionListener
import io.agora.chat.uikit.common.ChatLoginExtensionInfo

open class ChatUIKitConnectionListener:ChatConnectionListener {

    override fun onConnected() {}

    override fun onDisconnected(p0: Int) {}

    override fun onTokenExpired() {}

    override fun onTokenWillExpire() {}

    override fun onLogout(errorCode: Int, info: ChatLoginExtensionInfo?) {}
}