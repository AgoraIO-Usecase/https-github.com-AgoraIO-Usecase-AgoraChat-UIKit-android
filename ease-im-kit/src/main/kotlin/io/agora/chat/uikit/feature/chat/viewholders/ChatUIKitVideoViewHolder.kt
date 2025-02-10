package io.agora.chat.uikit.feature.chat.viewholders

import android.view.View
import io.agora.chat.uikit.feature.chat.activities.ChatUIKitShowVideoActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatDownloadStatus
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageDirection
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.ChatVideoMessageBody

class ChatUIKitVideoViewHolder(itemView: View) : ChatUIKitRowViewHolder(itemView) {
    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        message?.run {
            if (body !is ChatVideoMessageBody) {
                ChatLog.e(TAG, "message body is not video type")
                return
            }
            if (ChatClient.getInstance().options.autodownloadThumbnail) {

            } else {
                val body: ChatVideoMessageBody = body as ChatVideoMessageBody
                if (body.thumbnailDownloadStatus() === ChatDownloadStatus.DOWNLOADING
                    || body.thumbnailDownloadStatus() === ChatDownloadStatus.PENDING
                    || body.thumbnailDownloadStatus() === ChatDownloadStatus.FAILED) {
                    // retry download with click event of user
                    ChatClient.getInstance().chatManager().downloadThumbnail(this)
                    return
                }
            }
            if (direct() === ChatMessageDirection.RECEIVE && !isAcked && chatType === ChatType.Chat) {
                try {
                    ChatClient.getInstance().chatManager()
                        .ackMessageRead(from, msgId)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            ChatUIKitShowVideoActivity.actionStart(mContext, message)
        }

    }

    companion object {
        private val TAG = ChatUIKitVideoViewHolder::class.java.simpleName
    }
}