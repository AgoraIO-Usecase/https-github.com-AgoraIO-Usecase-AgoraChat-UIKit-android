package io.agora.chat.uikit.feature.chat.viewholders

import android.net.Uri
import android.view.View
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.feature.chat.activities.ChatUIKitShowBigImageActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatImageMessageBody
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageDirection
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils

class ChatUIKitImageViewHolder(itemView: View) : ChatUIKitRowViewHolder(itemView) {
    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        val imgBody: ChatImageMessageBody? = message?.body as? ChatImageMessageBody
        if (ChatUIKitClient.getConfig()?.chatConfig?.enableSendChannelAck == false) {
            //Here no longer send read_ack message separately, instead enter the chat page to send channel_ack
            //New messages are sent in the onReceiveMessage method of the chat page, except for video
            // , voice and file messages, and send read_ack messages
            message?.let {
                if (it.direct() === ChatMessageDirection.RECEIVE && !it.isAcked && it.chatType === ChatType.Chat) {
                    try {
                        ChatClient.getInstance().chatManager()
                            .ackMessageRead(it.from, it.msgId)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        }
        val imgUri: Uri? = imgBody?.getLocalUri()
        ChatUIKitFileUtils.takePersistableUriPermission(mContext, imgUri)
        ChatLog.e(
            "Tag",
            "big image uri: " + imgUri + "  exist: " + ChatUIKitFileUtils.isFileExistByUri(
                mContext,
                imgUri
            )
        )
        if (ChatUIKitFileUtils.isFileExistByUri(mContext, imgUri)) {
            ChatUIKitShowBigImageActivity.actionStart(mContext, imgUri)
        } else {
            // The local full size pic does not exist yet.
            // ShowBigImage needs to download it from the server
            // first
            ChatUIKitShowBigImageActivity.actionStart(mContext, message?.msgId, imgBody?.fileName)
        }
    }

    override fun handleReceiveMessage(message: ChatMessage?) {
        super.handleReceiveMessage(message)
        getChatRow()?.updateView()
    }
}