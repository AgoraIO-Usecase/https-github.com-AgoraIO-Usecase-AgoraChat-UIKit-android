package io.agora.chat.uikit.feature.invitation.helper

import android.content.Context
import android.text.TextUtils
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.feature.invitation.enums.InviteMessageStatus

object RequestMsgHelper {

    /**
     * Get system message content
     * @param msg
     * @return
     */
    @Throws(NullPointerException::class)
    fun getSystemMessage(context: Context,msg: MutableMap<String, Any>): String {
        val messageStatus = msg[ChatUIKitConstant.SYSTEM_MESSAGE_STATUS] as String?
        if (TextUtils.isEmpty(messageStatus)) {
            return ""
        }
        val status = messageStatus?.let { InviteMessageStatus.valueOf(it) } ?: return ""
        val messge: String
        val builder = StringBuilder(context.getString(status.msgContent))
        messge = when (status) {
            InviteMessageStatus.BEINVITEED -> String.format(
                builder.toString(),
                msg[ChatUIKitConstant.SYSTEM_MESSAGE_FROM]
            )
            else -> ""
        }
        return messge
    }


    /**
     * Get system message content
     * @param msg
     * @return
     */
    @Throws(ChatException::class)
    fun getSystemMessage(context: Context,msg: ChatMessage): String {
        val messageStatus: String = msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_STATUS)
        if (TextUtils.isEmpty(messageStatus)) {
            return ""
        }
        val status = InviteMessageStatus.valueOf(messageStatus) ?: return ""
        val message: String
        val builder = StringBuilder(context.getString(status.msgContent))
        message = when (status) {
            InviteMessageStatus.BEINVITEED -> java.lang.String.format(
                builder.toString(),
                msg.getStringAttribute(ChatUIKitConstant.SYSTEM_MESSAGE_FROM)
            )
            else -> ""
        }
        return message
    }
}