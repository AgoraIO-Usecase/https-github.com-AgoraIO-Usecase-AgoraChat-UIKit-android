package io.agora.chat.uikit.viewmodel.messages

import android.net.Uri
import io.agora.chat.uikit.common.ChatCallback
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageBody
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitLoadDataType
import io.agora.chat.uikit.viewmodel.IAttachView

interface IChatViewRequest: IAttachView {

    /**
     * Bind with send id.
     * @param toChatUsername
     * @param chatType
     * @param loadDataType
     */
    fun setupWithToUser(toChatUsername: String?, chatType: ChatUIKitType, loadDataType: ChatUIKitLoadDataType)

    /**
     * Bind with the parent id for chat thread conversation.
     * @param parentId It is usually a group id which the chat thread belongs to.
     */
    fun bindParentId(parentId: String?)

    /**
     * Send Channel ack message.
     * (1) If it is a 1v1 session, the other party will receive a channel ack callback, the callback method
     * is {@link ConversationListener#onConversationRead(String, String)}
     * The SDK will set the isAcked of the message sent for this session to true.
     * (2) If it is a multi-terminal device, the other end will receive a channel ack callback, and the SDK will set the session as read.
     * (3) Not send channel ack when the conversation is thread
     */
    fun sendChannelAck()

    /**
     * Send group message read ack.
     */
    fun sendGroupMessageReadAck(messageId: String?, ext: String?)

    /**
     * Send message read ack.
     */
    fun sendMessageReadAck(messageId: String?)

    /**
     * Send text message
     * @param content
     * @param isNeedGroupAck Whether need a group receipt
     */
    fun sendTextMessage(content: String?, isNeedGroupAck: Boolean = false)

    /**
     * Send @ message
     * @param content
     */
    fun sendAtMessage(content: String?)

    /**
     * Send GIF message
     * @param name
     * @param identityCode
     */
    fun sendBigExpressionMessage(name: String?, identityCode: String?)

    /**
     * Send voice message
     * @param filePath
     * @param length
     */
    fun sendVoiceMessage(filePath: Uri?, length: Int)

    /**
     * Send image message
     * @param imageUri
     * @param sendOriginalImage
     */
    fun sendImageMessage(imageUri: Uri?, sendOriginalImage: Boolean = false)

    /**
     * Send location message
     * @param latitude
     * @param longitude
     * @param locationAddress
     */
    fun sendLocationMessage(latitude: Double, longitude: Double, locationAddress: String?)

    /**
     * Send video message
     * @param videoUri
     * @param videoLength
     */
    fun sendVideoMessage(videoUri: Uri?, videoLength: Int)

    /**
     * Send file message
     * @param fileUri
     */
    fun sendFileMessage(fileUri: Uri?)

    /**
     * Send combine message.
     * @param title
     * @param summary
     * @param compatibleText
     * @param msgIds
     */
    fun sendCombineMessage(
        title: String?,
        summary: String?,
        compatibleText: String?,
        msgIds: List<String>?
    )

    /**
     * Send combine message.
     * @param to
     * @param chatType
     * @param msgIds
     */
    fun sendCombineMessage(
        to: String?,
        chatType: ChatType?,
        msgIds: List<String>?
    )

    /**
     * Send a combine message.
     * @param message
     */
    fun sendCombineMessage(message: ChatMessage?)

    /**
     * Send cmd message
     * @param action
     */
    fun sendCmdMessage(action: String?)

    /**
     * Add extension fields to the message
     * @param message
     */
    fun addMessageAttributes(message: ChatMessage?)

    /**
     * Send message
     * @param message
     */
    fun sendMessage(message: ChatMessage?)

    /**
     * Send message
     * @param message
     * @param isCheck Whether to check the message's chatType
     * @param callback
     */
    fun sendMessage(message: ChatMessage?, isCheck: Boolean, callback: ChatCallback? = null)

    /**
     * Resend message
     * @param message
     */
    fun resendMessage(message: ChatMessage?)

    /**
     * Report message
     * @param tag
     * @param reason
     * @param msgId
     */
    fun reportMessage(tag: String, reason: String?="",msgId: String)

    /**
     * Delete local message
     * @param message
     */
    fun deleteMessage(message: ChatMessage?)

    /**
     * Delete local message list.
     * @param messages
     */
    fun deleteMessages(messages: List<String>?)

    /**
     * Withdraw message
     * @param message
     */
    fun recallMessage(message: ChatMessage?)

    /**
     * modify message
     * @param messageId
     * @param messageBodyModified
     */
    fun modifyMessage(messageId: String?, messageBodyModified: ChatMessageBody?)

    /**
     * Forward message
     * @param message   The message to be forwarded
     * @param toId      The user id or group id
     * @param chatType  The chat type
     */
    fun forwardMessage(message: ChatMessage?, toId: String, chatType: ChatType)

    /**
     * add reaction
     *
     * @param message
     * @param reaction
     */
    fun addReaction(message: ChatMessage?, reaction: String?)

    /**
     * remove reaction
     *
     * @param message
     * @param reaction
     */
    fun removeReaction(message: ChatMessage?, reaction: String?)

    /**
     * create reply message ext.
     * @param message
     */
    fun createReplyMessageExt(message: ChatMessage?)

    /**
     * translation message
     * @param message
     * @param languages
     */
    fun translationMessage(message: ChatMessage?,languages:MutableList<String>)

    /**
     * hide translation message
     * @param message
     */
    fun hideTranslationMessage(message: ChatMessage?)

    /**
     * Get in progress messages in target conversation
     */
    fun getInProgressMessages()

    /**
     * pin message
     * @param message
     */
    fun pinMessage(message:ChatMessage?)

    /**
     * pin message
     * @param message
     */
    fun unPinMessage(message:ChatMessage?)

    /**
     * fetch pin message from server
     * @param conversationId
     */
    fun fetchPinMessageFromServer(conversationId:String?)

}