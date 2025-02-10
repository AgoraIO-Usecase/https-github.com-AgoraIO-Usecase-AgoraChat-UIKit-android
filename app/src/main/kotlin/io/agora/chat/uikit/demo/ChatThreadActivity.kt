package io.agora.chat.uikit.demo

import io.agora.chat.uikit.feature.chat.UIKitChatFragment
import io.agora.chat.uikit.feature.thread.ChatUIKitThreadActivity

class ChatThreadActivity:ChatUIKitThreadActivity() {
    override fun setChildSettings(builder: UIKitChatFragment.Builder) {
        super.setChildSettings(builder)
    }
}