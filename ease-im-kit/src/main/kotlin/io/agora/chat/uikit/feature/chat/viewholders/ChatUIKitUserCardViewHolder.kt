package io.agora.chat.uikit.feature.chat.viewholders

import android.view.View
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.getUserCardInfo
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.feature.contact.ChatUIKitContactCheckActivity

class ChatUIKitUserCardViewHolder(itemView: View) : ChatUIKitRowViewHolder(itemView) {

    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        message?.getUserCardInfo()?.let {
            mContext.startActivity(ChatUIKitContactCheckActivity.createIntent(mContext, it.toUser()))
        }
    }
}