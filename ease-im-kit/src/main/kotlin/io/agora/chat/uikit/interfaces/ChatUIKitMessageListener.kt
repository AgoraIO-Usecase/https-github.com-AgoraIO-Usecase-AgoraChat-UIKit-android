package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.common.ChatGroupReadAck
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageListener
import io.agora.chat.uikit.common.ChatMessagePinInfo
import io.agora.chat.uikit.common.ChatMessagePinOperation
import io.agora.chat.uikit.common.ChatMessageReactionChange
import io.agora.chat.uikit.common.ChatRecallMessageInfo

open class ChatUIKitMessageListener: ChatMessageListener {

    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {}

    override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {}

    override fun onMessageRead(messages: MutableList<ChatMessage>?) {}

    override fun onGroupMessageRead(groupReadAcks: MutableList<ChatGroupReadAck>?) {}

    override fun onReadAckForGroupMessageUpdated() {}

    override fun onMessageDelivered(messages: MutableList<ChatMessage>?) {}

    override fun onMessageRecalledWithExt(recallMessageInfo: MutableList<ChatRecallMessageInfo>?) {}

    override fun onReactionChanged(messageReactionChangeList: MutableList<ChatMessageReactionChange>?) {}

    override fun onMessageContentChanged(
        messageModified: ChatMessage?,
        operatorId: String?,
        operationTime: Long
    ) {}

    override fun onMessagePinChanged(
        messageId: String?,
        conversationId: String?,
        pinOperation: ChatMessagePinOperation?,
        pinInfo: ChatMessagePinInfo?
    ) {
    }
}