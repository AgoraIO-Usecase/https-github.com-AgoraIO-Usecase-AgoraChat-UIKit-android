package io.agora.chat.uikit.feature.chat.viewholders

import android.view.View
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.helper.ChatUIKitDingMessageHelper

class ChatUIKitTextViewHolder(itemView: View) : ChatUIKitRowViewHolder(itemView) {
    override fun handleReceiveMessage(message: ChatMessage?) {
        super.handleReceiveMessage(message)

        message?.let {
            // Send the group-ack cmd type msg if this msg is a ding-type msg.
            ChatUIKitDingMessageHelper.get().sendAckMessage(it)
        }

    }
}