package io.agora.chat.uikit.demo

import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.showToast
import io.agora.chat.uikit.feature.chat.UIKitChatFragment
import io.agora.chat.uikit.feature.chat.activities.UIKitChatActivity
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageForwardCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnSendCombineMessageCallback

class ChatActivity: UIKitChatActivity() {

    override fun setChildSettings(builder: UIKitChatFragment.Builder) {
        super.setChildSettings(builder)
        builder.setOnMessageForwardCallback(object : OnMessageForwardCallback {
            override fun onForwardSuccess(message: ChatMessage?) {
                mContext.showToast(R.string.message_forward_success)
            }

            override fun onForwardError(code: Int, errorMsg: String?) {
                mContext.showToast(R.string.message_forward_fail)
            }
        })
        builder.setOnSendCombineMessageCallback(object : OnSendCombineMessageCallback {
            override fun onSendCombineSuccess(message: ChatMessage?) {
                mContext.showToast(R.string.message_forward_success)
            }

            override fun onSendCombineError(message: ChatMessage?, code: Int, errorMsg: String?) {
                mContext.showToast(R.string.message_forward_fail)
            }
        })
    }
}