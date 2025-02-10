package io.agora.chat.uikit.feature.chat.viewholders

import android.net.Uri
import android.view.View
import io.agora.chat.uikit.feature.chat.activities.ChatUIKitShowNormalFileActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageDirection
import io.agora.chat.uikit.common.ChatNormalFileMessageBody
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.utils.ChatUIKitCompat
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils

class ChatUIKitFileViewHolder(itemView: View) : ChatUIKitRowViewHolder(itemView) {
    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        val fileMessageBody = message?.body as ChatNormalFileMessageBody
        val filePath: Uri? = fileMessageBody.localUri
        ChatUIKitFileUtils.takePersistableUriPermission(mContext, filePath)
        if (ChatUIKitFileUtils.isFileExistByUri(mContext, filePath)) {
            ChatUIKitCompat.openFile(mContext, filePath!!)
        } else {
            // download the file
            ChatUIKitShowNormalFileActivity.actionStart(mContext, message)
        }
        message?.run {
            if(direct() == ChatMessageDirection.RECEIVE && !isAcked && chatType == ChatType.Chat) {
                try {
                    ChatClient.getInstance().chatManager()
                        .ackMessageRead(from, msgId)
                } catch (e: ChatException) {
                    e.printStackTrace()
                }
            }
        }
    }
}