package io.agora.chat.uikit.feature.chat.interfaces

import android.net.Uri
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageBody
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitInputMenu
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitMessageListLayout
import io.agora.chat.uikit.feature.thread.interfaces.OnMessageChatThreadClickListener
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitNotificationView
import io.agora.chat.uikit.viewmodel.messages.IChatViewRequest

interface IChatLayout {
    /**
     * Set the parent id for chat thread conversation.
     * @param parentId It is usually a group id which the chat thread belongs to.
     */
    fun setParentId(parentId: String?)

    /**
     * set custom ViewModel
     * @param viewModel
     */
    fun setViewModel(viewModel: IChatViewRequest?)

    /**
     * Get chat list
     * @return
     */
    val chatMessageListLayout: ChatUIKitMessageListLayout?

    /**
     * Get input menu
     * @return
     */
    val chatInputMenu: ChatUIKitInputMenu?

    /**
     * Get notification view
     */
    val chatNotificationView: ChatUIKitNotificationView?

    /**
     * Get input content
     * @return
     */
    val inputContent: String?

    /**
     * Whether to open the input monitoring
     * @param turnOn
     */
    fun turnOnTypingMonitor(turnOn: Boolean)

    /**
     * Dismiss the notification view.
     */
    fun dismissNotificationView(dismiss: Boolean)

    /**
     * Send text message
     * @param content
     * @param isNeedGroupAck Need group receipt
     */
    fun sendTextMessage(content: String?, isNeedGroupAck: Boolean = false)

    /**
     * send @ message
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
    fun sendVoiceMessage(filePath: String?, length: Int)

    /**
     * Send voice message
     * @param filePath
     * @param length
     */
    fun sendVoiceMessage(filePath: Uri?, length: Int)

    /**
     * Send picture message
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
     * Send a combine message.
     * @param message
     */
    fun sendCombineMessage(message: ChatMessage?)

    /**
     * Send file message
     * @param fileUri
     */
    fun sendFileMessage(fileUri: Uri?)

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
     * Resend message
     * @param message
     */
    fun resendMessage(message: ChatMessage?)

    /**
     * Report message
     * @param tag
     * @param reason
     * @param message
     */
    fun reportMessage(tag:String,reason:String,message: ChatMessage?)

    /**
     * delete local message
     * @param message
     */
    fun deleteMessage(message: ChatMessage?)

    /**
     * Delete local message list.
     * @param messages
     */
    fun deleteMessages(messages: List<String>?)

    /**
     * Recall server message
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
     * Set the edit message listening
     * @param listener
     */
    fun setOnEditMessageListener(listener: OnModifyMessageListener?)

    /**
     * Set the report message listening
     * @param listener
     */
    fun setOnReportMessageListener(listener:OnReportMessageListener?)

    /**
     * Set the translation message listening
     * @param listener
     */
    fun setOnTranslationMessageListener(listener: OnTranslationMessageListener?)

    /**
     * Set the thread view click listening
     * @param listener
     */
    fun setOnMessageThreadViewClickListener(listener: OnMessageChatThreadClickListener?)

    fun setOnChatLayoutListener(listener: OnChatLayoutListener?)

    /**
     * Used to monitor touch events for sending voice
     * @param voiceTouchListener
     */
    fun setOnChatRecordTouchListener(voiceTouchListener: OnChatRecordTouchListener?)

    /**
     * Message withdrawal monitoring
     * @param listener
     */
    fun setOnRecallMessageResultListener(listener: OnRecallMessageResultListener?)

    /**
     * Set the attribute event before sending message
     * @param onWillSendMessageListener
     */
    fun setOnWillSendMessageListener(onWillSendMessageListener: OnWillSendMessageListener?)

    /**
     * The listener of reaction
     *
     * @param reactionListener
     */
    fun setOnReactionListener(reactionListener: OnReactionMessageListener?)

    /**
     * Set layer finish listener.
     */
    fun setOnChatFinishListener(listener: OnChatFinishListener?)

    /**
     * Set multiple select remove message listener
     */
    fun setMultipleSelectRemoveMsgListener(listener: OnMultipleSelectRemoveMsgListener){}

}