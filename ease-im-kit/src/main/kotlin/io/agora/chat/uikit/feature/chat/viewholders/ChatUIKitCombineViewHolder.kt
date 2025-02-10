package io.agora.chat.uikit.feature.chat.viewholders

import android.view.View
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.feature.chat.activities.ChatUIKitHistoryActivity

class ChatUIKitCombineViewHolder(itemView: View) : ChatUIKitRowViewHolder(itemView) {
    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        skipToCombine(message)
    }

    private fun skipToCombine(message: ChatMessage?) {
        ChatUIKitHistoryActivity.actionStart(itemView.context, message)
    }

}