package io.agora.chat.uikit.feature.chat.widgets

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatCmdMessageBody
import io.agora.chat.uikit.common.ChatConversationType
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageBody
import io.agora.chat.uikit.common.ChatMessageDirection
import io.agora.chat.uikit.common.ChatMessagePinInfo
import io.agora.chat.uikit.common.ChatMessagePinOperation
import io.agora.chat.uikit.common.ChatMessageReactionChange
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatRecallMessageInfo
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatThread
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.enums.ChatUIKitFinishReason
import io.agora.chat.uikit.common.extensions.isSuccess
import io.agora.chat.uikit.common.extensions.lifecycleScope
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.common.extensions.showToast
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.common.helper.ChatUIKitAtMessageHelper
import io.agora.chat.uikit.databinding.UikitLayoutChatBinding
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitLoadDataType
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitDialogController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitMessageEditController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitMessageMultipleSelectController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitMessageReplyController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitMessageReportController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitMessageTranslationController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitNotificationController
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitPinMessageController
import io.agora.chat.uikit.feature.chat.enums.getConversationType
import io.agora.chat.uikit.feature.chat.forward.ChatUIKitMessageForwardDialogFragment
import io.agora.chat.uikit.feature.chat.interfaces.ChatInputMenuListener
import io.agora.chat.uikit.feature.chat.interfaces.IChatLayout
import io.agora.chat.uikit.feature.chat.interfaces.IChatMenu
import io.agora.chat.uikit.feature.chat.interfaces.IHandleChatResultView
import io.agora.chat.uikit.feature.chat.interfaces.OnWillSendMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatErrorListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatFinishListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatLayoutListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatRecordTouchListener
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageAckSendCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageListItemClickListener
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageListTouchListener
import io.agora.chat.uikit.feature.chat.interfaces.OnModifyMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnMultipleSelectRemoveMsgListener
import io.agora.chat.uikit.feature.chat.interfaces.OnReactionMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnRecallMessageResultListener
import io.agora.chat.uikit.feature.chat.interfaces.OnReportMessageListener
import io.agora.chat.uikit.feature.chat.interfaces.OnTranslationMessageListener
import io.agora.chat.uikit.feature.chat.reply.interfaces.OnMessageReplyViewClickListener
import io.agora.chat.uikit.feature.thread.ChatUIKitCreateThreadActivity
import io.agora.chat.uikit.feature.thread.interfaces.OnMessageChatThreadClickListener
import io.agora.chat.uikit.interfaces.UIKitChatRoomListener
import io.agora.chat.uikit.interfaces.ChatUIKitConversationListener
import io.agora.chat.uikit.interfaces.ChatUIKitGroupListener
import io.agora.chat.uikit.interfaces.ChatUIKitMessageListener
import io.agora.chat.uikit.interfaces.OnForwardClickListener
import io.agora.chat.uikit.interfaces.OnMenuChangeListener
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.interfaces.OnVoiceRecorderClickListener
import io.agora.chat.uikit.menu.ChatUIKitMenuHelper
import io.agora.chat.uikit.menu.chat.ChatUIKitChatMenuHelper
import io.agora.chat.uikit.model.ChatUIKitEmojicon
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.viewmodel.messages.ChatUIKitViewModel
import io.agora.chat.uikit.viewmodel.messages.IChatViewRequest
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChatUIKitLayout @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), IChatLayout, IHandleChatResultView, IChatMenu{

    private val mContext = context

    /**
     * The parent id of chat thread.
     * When the chat type is [ChatUIKitLoadDataType.THREAD], it is usually a group id which the chat thread belongs to.
     */
    private var parentId: String? = null
    /**
     * The view model of the chat view.
     */
    private var viewModel: IChatViewRequest? = null
    protected val chatBinding: UikitLayoutChatBinding by lazy { UikitLayoutChatBinding.inflate(
        LayoutInflater.from(mContext), this, true) }

    /**
     * Use to control the logic of chat message replying.
     */
    private val chatMessageReplyController: ChatUIKitMessageReplyController by lazy {
        ChatUIKitMessageReplyController(mContext, this, viewModel) }

    /**
     * Use to control the logic of chat message translating.
     */
    private val chatMessageTranslationController: ChatUIKitMessageTranslationController by lazy {
        ChatUIKitMessageTranslationController(mContext, this, viewModel) }

    /**
     * Use to control the logic of chat message editing.
     */
    private val chatMessageEditController: ChatUIKitMessageEditController by lazy {
        ChatUIKitMessageEditController(mContext, this)
    }

    /**
     * Use to control the logic of chat message reporting.
     */
    private val chatMessageReportController: ChatUIKitMessageReportController by lazy {
        ChatUIKitMessageReportController(mContext, this)
    }

    /**
     * Use to control the logic of chat dialog.
     */
    private val chatDialogController: ChatUIKitDialogController by lazy {
        ChatUIKitDialogController(mContext, this)
    }

    /**
     * Use to control the logic of chat message multiple select logic.
     */
    val messageMultipleSelectController: ChatUIKitMessageMultipleSelectController by lazy {
        ChatUIKitMessageMultipleSelectController(mContext, this, viewModel, conversationId!!)
    }

    /**
     * Use to control the logic of chat message notification.
     */
    val chatNotificationController: ChatUIKitNotificationController by lazy {
        ChatUIKitNotificationController(chatBinding, conversationId, viewModel)
    }

    val chatPinMessageController:ChatUIKitPinMessageController by lazy {
        ChatUIKitPinMessageController(mContext,this@ChatUIKitLayout, conversationId, viewModel)
    }

    /**
     * Current conversation id.
     */
    private var conversationId: String? = null

    /**
     * Current chat type.
     */
    private var chatType: ChatUIKitType? = null

    /**
     * current load data type.
     */
    private var loadDataType: ChatUIKitLoadDataType? = null

    /**
     * The switch of the "inputting" function.
     * When it is turned on, the device will continue to send cmd type messages
     * to notify the other party of "inputting" when sending messages
     */
    private var turnOnTyping = false

    /**
     * Whether it is the first time to send, the default is true
     */
    private var isNotFirstSend = false

    /**
     * The clipboard manager.
     */
    private val clipboard by lazy { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    /**
     * Chat menu helper.
     */
    private val menuHelper: ChatUIKitChatMenuHelper by lazy { ChatUIKitChatMenuHelper() }

    /**
     * The typing handler.
     */
    private var typingHandler: Handler? = null

    /**
     * The listener of the group. When the chat type is [ChatType.GroupChat]
     */
    private var groupChangeListener: ChatUIKitGroupListener? = null

    /**
     * The listener of the chat room. When the chat type is [ChatType.ChatRoom]
     */
    private var chatRoomListener: UIKitChatRoomListener? = null

    /**
     * Used to monitor changes in messages
     */
    private var listener: OnChatLayoutListener? = null

    /**
     * Add a message attribute event before sending a message
     */
    private var onWillSendMessageListener: OnWillSendMessageListener? = null

    private var reactionMessageListener: OnReactionMessageListener? = null

    /**
     * Withdraw monitoring
     */
    private var recallMessageListener: OnRecallMessageResultListener? = null

    /**
     * listener for modify message
     */
    private var modifyMessageListener: OnModifyMessageListener? = null

    /**
     * listener for report message
     */
    private var reportMessageListener: OnReportMessageListener? = null

    /**
     * Used to monitor touch events for sending voice
     */
    private var recordTouchListener: OnChatRecordTouchListener? = null

    /**
     * Used to monitor whether need to finish current activity.
     */
    private var finishListener: OnChatFinishListener? = null

    /**
     * listener for translation message
     */
    private var translationMessageListener: OnTranslationMessageListener? = null

    /**
     * listener for chat thread view click listener
     */
    private var threadViewClickListener: OnMessageChatThreadClickListener? = null

    /**
     * listener for multiple select view remove message listener
     */
    private var multipleSelectRemoveMsgListener: OnMultipleSelectRemoveMsgListener? = null

    private val chatMessageListener = object : ChatUIKitMessageListener() {
        override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
            var refresh = false
            if (messages != null) {
                for (message in messages) {
                    sendGroupReadAck(message)
                    sendReadAck(message)
                    // group message
                    val username: String? =
                        if (message.chatType === ChatType.GroupChat || message.chatType === ChatType.ChatRoom) {
                            message.to
                        } else {
                            // single chat message
                            message.from
                        }
                    // if the message is for current conversation
                    if (username == conversationId || message.to.equals(conversationId) || message.conversationId()
                            .equals(conversationId)
                    ) {
                        refresh = true
                    }
                    if (ChatUIKitAtMessageHelper.get().isAtMeMsg(message) && message.conversationId()
                            .equals(conversationId)
                    ) {
                        ChatUIKitAtMessageHelper.get().removeAtMeGroup(conversationId!!)
                    }
                }
                if (refresh && messages.isNotEmpty()) {
                    chatNotificationController.updateNotificationView()
                    //getChatMessageListLayout().setSendOrReceiveMessage(messages[0])
                    chatBinding.layoutChatMessage.refreshToLatest()
                }
            }
        }

        override fun onCmdMessageReceived(messages: MutableList<ChatMessage>?) {
            super.onCmdMessageReceived(messages)
            if (!turnOnTyping) {
                return
            }
            if (messages != null) {
                for (msg in messages) {
                    val body = msg.body as ChatCmdMessageBody
                    ChatLog.i(
                        TAG,
                        "Receive cmd message: " + body.action() + " - " + body.isDeliverOnlineOnly
                    )
                    context.mainScope().launch {
                        if (TextUtils.equals(msg.from, conversationId)) {
                            listener?.onPeerTyping(body.action())
                            typingHandler?.let {
                                it.removeMessages(MSG_OTHER_TYPING_END)
                                it.sendEmptyMessageDelayed(MSG_OTHER_TYPING_END, OTHER_TYPING_SHOW_TIME.toLong())
                            }
                        }
                    }
                }
            }
        }

        override fun onMessageRead(messages: MutableList<ChatMessage>?) {
            super.onMessageRead(messages)
            refreshMessages(messages)
        }

        override fun onMessageDelivered(messages: MutableList<ChatMessage>?) {
            super.onMessageDelivered(messages)
            refreshMessages(messages)
        }

        override fun onMessageRecalledWithExt(recallMessageInfo: MutableList<ChatRecallMessageInfo>?) {
            super.onMessageRecalledWithExt(recallMessageInfo)
            post {
                var isRefresh = false
                if (recallMessageInfo != null && recallMessageInfo.size > 0) {
                    for (message in recallMessageInfo) {
                        message.recallMessage?.let {
                            if (TextUtils.equals(it.conversationId(), conversationId)) {
                                isRefresh = true
                            }
                            removePinMessage(it)
                        }
                    }
                }
                if (isRefresh) {
                    chatBinding.layoutChatMessage.refreshMessages()
                }
            }
        }

        override fun onReactionChanged(messageReactionChangeList: MutableList<ChatMessageReactionChange>?) {
            super.onReactionChanged(messageReactionChangeList)
            ChatUIKitClient.getConfig()?.chatConfig?.enableMessageReaction?.let {
                if (!it) {
                    return
                }
            }
            if (messageReactionChangeList != null) {
                for (reactionChange in messageReactionChangeList) {
                    if (conversationId == reactionChange.conversionID) {
                        chatBinding.layoutChatMessage.refreshMessage(reactionChange.messageId)
                    }
                }
            }
        }

        override fun onMessageChanged(message: ChatMessage?, change: Any?) {
            super.onMessageChanged(message, change)
            refresh(message)
        }

        override fun onMessageContentChanged(
            messageModified: ChatMessage?,
            operatorId: String?,
            operationTime: Long
        ) {
            messageModified?.let {
                if (it.conversationId() == conversationId) {
                    ChatUIKitClient.getCache().cleanUrlPreviewInfo(it.msgId)
                    chatBinding.layoutChatMessage.refreshMessage(it)
                }
            }
        }

        override fun onMessagePinChanged(
            messageId: String?,
            conversationId: String?,
            pinOperation: ChatMessagePinOperation?,
            pinInfo: ChatMessagePinInfo?
        ) {
            ChatLog.d(TAG, "onMessagePinChanged: messageId:$messageId, conversationId:$conversationId, pinOperation:$pinOperation, pinInfo:$pinInfo")
            val message = ChatClient.getInstance().chatManager().getMessage(messageId)
            message?.let{
                chatPinMessageController.updatePinMessage(it,pinInfo?.operatorId())
            }?:kotlin.run{
                chatPinMessageController.fetchPinnedMessagesFromServer()
            }
        }
    }

    private val conversationListener = object : ChatUIKitConversationListener() {

        override fun onConversationRead(from: String?, to: String?) {
            chatBinding.layoutChatMessage.notifyDataSetChanged()
        }
    }

    init {
        initView()
        initListener()
    }

    private fun initView() {
        viewModel = if (context is AppCompatActivity) {
            ViewModelProvider(context)[ChatUIKitViewModel::class.java]
        } else {
            ChatUIKitViewModel()
        }
        viewModel?.attachView(this)
    }

    private fun initListener() {
        chatBinding.layoutChatMessage.setOnMessageListItemClickListener(object : OnMessageListItemClickListener {
            override fun onBubbleClick(message: ChatMessage?): Boolean {
                return listener?.onBubbleClick(message) == true
            }

            override fun onBubbleLongClick(v: View?, message: ChatMessage?): Boolean {
                if (listener?.onBubbleLongClick(v, message) == true) return true
                val enableWxStyle = ChatUIKitClient.getConfig()?.chatConfig?.enableWxMessageStyle
                if (enableWxStyle != true){
                    chatBinding.layoutMenu.hideSoftKeyboard()
                }
                menuHelper.initMenu(v,message,true)
                menuHelper.initMenuWithMessage(message)
                setMenuItemClickListener(message)
                menuHelper.show()
                return true
            }

            override fun onResendClick(message: ChatMessage?): Boolean {
                if (listener?.onResendClick(message) == true) {
                    return true
                }
                chatDialogController.showResendDialog(context) {
                    resendMessage(message)
                }
                return true
            }

            override fun onUserAvatarClick(userId: String?) {
                listener?.onUserAvatarClick(userId)
            }

            override fun onUserAvatarLongClick(userId: String?) {
                inputAtUsername(userId, true)
                listener?.onUserAvatarLongClick(userId)
            }

        })
        chatBinding.layoutChatMessage.setOnMessageListTouchListener(object : OnMessageListTouchListener {
            override fun onTouchItemOutside(v: View?, position: Int) {
                chatBinding.layoutMenu.hideSoftKeyboard()
                chatBinding.layoutMenu.showExtendMenu(false)
            }

            override fun onViewDragging() {
                chatBinding.layoutMenu.hideSoftKeyboard()
                chatBinding.layoutMenu.showExtendMenu(false)

                if (!messageMultipleSelectController.isInMultipleSelectStyle) {
                    chatNotificationController.showNotificationView(!chatBinding.layoutChatMessage.isCanAutoScrollToBottom)
                }

            }

            override fun onReachBottom() {

            }

            override fun onFinishScroll() {
                if (!messageMultipleSelectController.isInMultipleSelectStyle) {
                    chatNotificationController.showNotificationView(!chatBinding.layoutChatMessage.isCanAutoScrollToBottom)
                }
            }

        })
        chatBinding.layoutChatMessage.setOnMessageReplyViewClickListener(object :
            OnMessageReplyViewClickListener {
            override fun onReplyViewClick(message: ChatMessage?) {
                if (message == null) {
                    ChatLog.e(TAG, "onReplyViewClick： message is null")
                    return
                }
                chatMessageReplyController.dealWithReplyViewClick(message)
            }
        })
        chatBinding.layoutChatMessage.setOnMessageThreadViewClickListener(object : OnMessageChatThreadClickListener{
            override fun onThreadViewItemClick(
                view: View,
                thread: ChatThread?,
                topicMsg: ChatMessage
            ) {
                threadViewClickListener?.onThreadViewItemClick(view, thread, topicMsg)
            }
        })
        chatBinding.layoutChatMessage.setOnChatErrorListener(object : OnChatErrorListener {
            override fun onChatError(code: Int, errorMsg: String?) {
                listener?.onError(code, errorMsg)
            }
        })
        chatBinding.layoutMenu.setChatInputMenuListener(object: ChatInputMenuListener {
            override fun afterTextChanged(s: Editable?) {
                listener?.afterTextChanged(s)
            }

            override fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (listener != null){
                    return listener?.editTextOnKeyListener(v, keyCode, event)?:true
                }
                return false
            }

            override fun onTyping(s: CharSequence?, start: Int, before: Int, count: Int) {
                listener?.onTextChanged(s, start, before, count)
                if (turnOnTyping) {
                    typingHandler?.let {
                        if (!isNotFirstSend) {
                            isNotFirstSend = true
                            typingHandler?.sendEmptyMessage(MSG_TYPING_HEARTBEAT)
                        }
                        it.removeMessages(MSG_TYPING_END)
                        it.sendEmptyMessageDelayed(MSG_TYPING_END, TYPING_SHOW_TIME.toLong())
                    }
                }
            }

            override fun onSendMessage(content: String?) {
                viewModel?.sendTextMessage(content)
            }

            override fun onExpressionClicked(emojiIcon: Any?) {
                if (emojiIcon is ChatUIKitEmojicon)
                    viewModel?.sendBigExpressionMessage(emojiIcon.name, emojiIcon.identityCode)
            }

            override fun onPressToSpeakBtnTouch(v: View?, event: MotionEvent?): Boolean {
                return false
            }

            override fun onToggleVoiceBtnClicked() {
                ChatUIKitVoiceRecorderDialog(mContext, conversationId).apply {
                    if (mContext is AppCompatActivity) {
                        show(mContext.supportFragmentManager, "ease_chat_voice_recorder_dialog")
                        setOnVoiceRecorderClickListener(object : OnVoiceRecorderClickListener {
                            override fun onClick(filePath: String?, length: Int) {
                                chatBinding.layoutMenu.chatPrimaryMenu?.showTextStatus()
                                viewModel?.sendVoiceMessage(if (filePath.isNullOrEmpty()) null else Uri.parse(filePath), length)
                            }
                        })
                        setOnRecordTouchListener(recordTouchListener)
                        setOnDismissListener { chatBinding.layoutMenu.chatPrimaryMenu?.showTextStatus() }
                    }
                }
            }

            override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
                listener?.onChatExtendMenuItemClick(view, itemId)
            }

        })
        chatBinding.layoutChatMessage.setOnMessageAckSendCallback(object :
            OnMessageAckSendCallback {

            override fun onSendAckSuccess(message: ChatMessage?) {
                super.onSendAckSuccess(message)
                sendChatUpdateEvent()
                listener?.onSendAckSuccess(message)
            }

            override fun onSendAckError(message: ChatMessage?, code: Int, errorMsg: String?) {
                listener?.onSendAckError(message, code, errorMsg)
            }
        })
        ChatUIKitClient.addChatMessageListener(chatMessageListener)
        ChatUIKitClient.addConversationListener(conversationListener)
    }

    private fun setMenuItemClickListener(message: ChatMessage?) {
        menuHelper.setOnMenuItemClickListener(object : OnMenuItemClickListener {
            override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
                item?.let {
                    when(it.menuId) {
                        R.id.action_chat_copy -> {
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText(
                                    null,
                                    (message?.body as? ChatTextMessageBody)?.message
                                )
                            )
                            context.showToast(R.string.uikit_chat_copy_success)
                        }
                        R.id.action_chat_delete -> {
                            chatDialogController.showDeleteDialog(mContext, message?.isSuccess() == true) {
                                deleteMessage(message)
                            }
                        }
                        R.id.action_chat_recall -> {
                            chatDialogController.showRecallDialog(mContext) {
                                recallMessage(message)
                            }
                        }
                        R.id.action_chat_report -> {
                            chatMessageReportController.showReportDialog(message)
                        }
                        R.id.action_chat_reply -> {
                            chatMessageReplyController.showExtendMessageReplyView(message)
                        }
                        R.id.action_chat_edit -> {
                            chatMessageEditController.showEditMessageDialog(message!!)
                        }
                        R.id.action_chat_translation -> {
                            if (chatMessageTranslationController.isShowTranslation(message)){
                                chatMessageTranslationController.hideTranslationMessage(message)
                            }else{
                                chatMessageTranslationController.showTranslationMessage(message)
                            }
                        }
                        R.id.action_chat_forward -> {
                            ChatUIKitMessageForwardDialogFragment().apply {
                                setOnForwardClickListener(object : OnForwardClickListener {
                                    override fun onForwardClick(view: View?, id: String, chatType: ChatType) {
                                        viewModel?.forwardMessage(message, id, chatType)
                                    }
                                })
                                if (mContext is AppCompatActivity) {
                                    show(mContext.supportFragmentManager, "ease_message_forward_dialog")
                                }
                            }
                        }
                        R.id.action_chat_thread -> {
                            message?.let {msg->
                                if (msg.chatThread == null){
                                    ChatUIKitCreateThreadActivity.actionStart(mContext,conversationId,msg.msgId)
                                }
                            }
                        }
                        R.id.action_chat_multi_select -> {
                            messageMultipleSelectController.showMultipleSelectStyle(message)
                        }
                        R.id.action_chat_pin_message -> {
                            chatPinMessageController.pinMessage(message,true)
                        }

                        else -> {}
                    }
                }
                return true
            }

        })
    }

    override fun onDetachedFromWindow() {
        ChatUIKitClient.removeChatMessageListener(chatMessageListener)
        ChatUIKitClient.removeConversationListener(conversationListener)
        groupChangeListener?.let { ChatUIKitClient.removeGroupChangeListener(it) }
        chatRoomListener?.let { ChatUIKitClient.removeChatRoomChangeListener(it) }
        typingHandler?.removeCallbacksAndMessages(null)
        menuHelper.setOnMenuChangeListener(null)
        menuHelper.setOnMenuItemClickListener(null)
        menuHelper.dismiss()
        menuHelper.release()
        if (isGroupConv()) {
            ChatUIKitAtMessageHelper.get().removeAtMeGroup(conversationId)
            ChatUIKitAtMessageHelper.get().cleanToAtUserList()
        }
        super.onDetachedFromWindow()
    }

    fun init(conversationId: String?, chatType: ChatUIKitType, loadDataType: ChatUIKitLoadDataType = ChatUIKitLoadDataType.LOCAL) {
        this.conversationId = conversationId
        this.loadDataType = loadDataType
        this.chatType = chatType
        chatBinding.layoutChatMessage.init(conversationId, chatType, loadDataType)
        chatMessageTranslationController.setConversationType(loadDataType)
        viewModel?.setupWithToUser(conversationId, chatType, loadDataType)
        if (loadDataType != ChatUIKitLoadDataType.THREAD) {
            if (isChatroomConv()) {
                chatRoomListener = ChatRoomListener()
                ChatUIKitClient.addChatRoomChangeListener(chatRoomListener!!)
            } else if (isGroupConv()) {
                ChatUIKitAtMessageHelper.get().removeAtMeGroup(conversationId)
                groupChangeListener = GroupListener()
                ChatUIKitClient.addGroupChangeListener(groupChangeListener!!)
            }
        } else {
            viewModel?.bindParentId(parentId)
            // Not show notification view in chat thread
            chatNotificationController.setShouldDismiss(true)
        }
        ChatUIKitAtMessageHelper.get().setupWithConversation(conversationId)
        initTypingHandler()
        chatPinMessageController.fetchPinnedMessagesFromServer()
        chatPinMessageController.initPinInfoView()
    }

    fun loadData(msgId: String? = "", pageSize: Int = 10) {
        sendChannelAck()
        chatBinding.layoutChatMessage.loadData(msgId, pageSize)
        getInProgressMessages()
    }

    private fun sendChannelAck() {
        if (loadDataType == ChatUIKitLoadDataType.LOCAL) {
            ChatClient.getInstance().chatManager().getConversation(conversationId)?.let {
                if (it.unreadMsgCount > 0) {
                    viewModel?.sendChannelAck()
                }
            }
        }
    }

    private fun getInProgressMessages() {
        if (loadDataType == ChatUIKitLoadDataType.LOCAL) {
            viewModel?.getInProgressMessages()
        }
    }

    private fun sendMessageAddEvent(messageId: String?) {
        if (loadDataType != ChatUIKitLoadDataType.SEARCH) return
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name).post(lifecycleScope
        , ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD.name, ChatUIKitEvent.TYPE.MESSAGE, messageId)
        )
    }

    private fun sendChatUpdateEvent() {
        if (loadDataType != ChatUIKitLoadDataType.SEARCH) return
        ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).post(lifecycleScope
        , ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.CONVERSATION, conversationId)
        )
    }

    private fun initTypingHandler() {
        typingHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_TYPING_HEARTBEAT -> setTypingBeginMsg(this)
                    MSG_TYPING_END -> setTypingEndMsg(this)
                    MSG_OTHER_TYPING_END -> setOtherTypingEnd(this)
                }
            }
        }
        if (!turnOnTyping) {
            typingHandler?.removeCallbacksAndMessages(null)
        }
    }

    /**
     * The other party's input status is aborted
     * @param handler
     */
    private fun setOtherTypingEnd(handler: Handler) {
        // Only support single-chat type conversation.
        if (chatType !== ChatUIKitType.SINGLE_CHAT) return
        handler.removeMessages(MSG_OTHER_TYPING_END)
        listener?.onPeerTyping(ACTION_TYPING_END)
    }

    /**
     * Processing "input in progress" begins
     * @param handler
     */
    private fun setTypingBeginMsg(handler: Handler) {
        if (!turnOnTyping) return
        // Only support single-chat type conversation.
        if (chatType !== ChatUIKitType.SINGLE_CHAT) return
        // Send TYPING-BEGIN cmd msg
        viewModel?.sendCmdMessage(ACTION_TYPING_BEGIN)
        handler.sendEmptyMessageDelayed(MSG_TYPING_HEARTBEAT, TYPING_SHOW_TIME.toLong())
    }

    /**
     * End of processing "input in progress"
     * @param handler
     */
    private fun setTypingEndMsg(handler: Handler) {
        if (!turnOnTyping) return

        // Only support single-chat type conversation.
        if (chatType !== ChatUIKitType.SINGLE_CHAT) return
        isNotFirstSend = false
        handler.removeMessages(MSG_TYPING_HEARTBEAT)
        handler.removeMessages(MSG_TYPING_END)
        // Send TYPING-END cmd msg
        //presenter.sendCmdMessage(ACTION_TYPING_END);
    }

    override fun setParentId(parentId: String?) {
        this.parentId = parentId
    }

    override fun setViewModel(viewModel: IChatViewRequest?) {
        this.viewModel = viewModel
        viewModel?.attachView(this)
    }

    override val chatMessageListLayout: ChatUIKitMessageListLayout?
        get() = chatBinding.layoutChatMessage
    override val chatInputMenu: ChatUIKitInputMenu?
        get() = chatBinding.layoutMenu
    override val chatNotificationView: ChatUIKitNotificationView?
        get() = chatBinding.layoutNotification
    override val inputContent: String?
        get() = chatBinding.layoutMenu.chatPrimaryMenu?.editText?.text?.toString()?.trim()

    override fun turnOnTypingMonitor(turnOn: Boolean) {
        turnOnTyping = turnOn
        if (!turnOn) {
            isNotFirstSend = false
        }
    }

    override fun dismissNotificationView(dismiss: Boolean) {
        chatNotificationController.setShouldDismiss(dismiss)
    }

    fun inputAtUsername(username: String?, autoAddAtSymbol: Boolean) {
        var atUser = username
        val currentConversation = chatMessageListLayout?.currentConversation
        currentConversation?.let { cv->
            chatMessageListLayout?.isGroupChat(cv)?.let { isGroupChat->
                if (!isGroupChat){ return }
            }
        }
        if (ChatClient.getInstance().currentUser.equals(username)) {
            return
        }
        username?.let {
            ChatUIKitAtMessageHelper.get().addAtUser(it)
            val user: ChatUIKitUser? = ChatUIKitProfile.getGroupMember(conversationId!!, it)?.toUser()
            atUser = user?.getRemarkOrName()
            atUser = atUser?:it
            val editText: EditText? = chatInputMenu?.chatPrimaryMenu?.editText
            if (autoAddAtSymbol) insertText(editText, AT_PREFIX + atUser + AT_SUFFIX)
            else insertText(editText, atUser + AT_SUFFIX)
        }
    }

    /**
     * insert text to EditText
     * @param edit
     * @param text
     */
    private fun insertText(edit: EditText?, text: String) {
        edit?.let {
            if (edit.isFocused) {
                edit.text.insert(edit.selectionStart, text)
            } else {
                edit.text.insert( if (edit.text.isNullOrEmpty()) 0 else edit.text.length - 1, text)
            }
        }
    }

    private fun removePinMessage(message:ChatMessage?){
        val pinMessage:ChatMessage? = ChatClient.getInstance().chatManager().getMessage(message?.msgId)
        val isPined: Boolean = pinMessage?.pinnedInfo() == null || pinMessage.pinnedInfo().operatorId().isEmpty()
        if (isPined){
            chatPinMessageController.removeData(pinMessage)
        }
    }

    override fun sendTextMessage(content: String?, isNeedGroupAck: Boolean) {
        viewModel?.sendTextMessage(content, isNeedGroupAck)
    }

    override fun sendAtMessage(content: String?) {
        viewModel?.sendAtMessage(content)
    }

    override fun sendBigExpressionMessage(name: String?, identityCode: String?) {
        viewModel?.sendBigExpressionMessage(name, identityCode)
    }

    override fun sendVoiceMessage(filePath: String?, length: Int) {
        viewModel?.sendVoiceMessage(if (filePath.isNullOrEmpty()) Uri.parse(filePath) else null, length)
    }

    override fun sendVoiceMessage(filePath: Uri?, length: Int) {
        viewModel?.sendVoiceMessage(filePath, length)
    }

    override fun sendImageMessage(imageUri: Uri?, sendOriginalImage: Boolean) {
        viewModel?.sendImageMessage(imageUri, sendOriginalImage)
    }

    override fun sendLocationMessage(
        latitude: Double,
        longitude: Double,
        locationAddress: String?
    ) {
        viewModel?.sendLocationMessage(latitude, longitude, locationAddress)
    }

    override fun sendVideoMessage(videoUri: Uri?, videoLength: Int) {
        viewModel?.sendVideoMessage(videoUri, videoLength)
    }

    override fun sendCombineMessage(message: ChatMessage?) {
        viewModel?.sendCombineMessage(message)
    }

    override fun sendFileMessage(fileUri: Uri?) {
        viewModel?.sendFileMessage(fileUri)
    }

    override fun addMessageAttributes(message: ChatMessage?) {
        viewModel?.addMessageAttributes(message)
    }

    override fun sendMessage(message: ChatMessage?) {
        viewModel?.sendMessage(message)
    }

    override fun resendMessage(message: ChatMessage?) {
        viewModel?.resendMessage(message)
    }

    override fun deleteMessage(message: ChatMessage?) {
        chatBinding.layoutChatMessage.removeMessage(message)
    }

    override fun deleteMessages(messages: List<String>?) {
        viewModel?.deleteMessages(messages)
    }

    override fun recallMessage(message: ChatMessage?) {
        viewModel?.recallMessage(message)
    }

    override fun reportMessage(tag: String, reason: String, message: ChatMessage?) {
        message?.msgId?.let { viewModel?.reportMessage(tag,reason, it) }
    }

    override fun modifyMessage(messageId: String?, messageBodyModified: ChatMessageBody?) {
        viewModel?.modifyMessage(messageId, messageBodyModified)
    }

    override fun setOnEditMessageListener(listener: OnModifyMessageListener?) {
        this.modifyMessageListener = listener
    }

    override fun setOnReportMessageListener(listener: OnReportMessageListener?) {
        this.reportMessageListener = listener
    }

    override fun setOnTranslationMessageListener(listener: OnTranslationMessageListener?) {
        this.translationMessageListener = listener
    }

    override fun setOnMessageThreadViewClickListener(listener: OnMessageChatThreadClickListener?) {
        this.threadViewClickListener = listener
    }

    override fun setOnChatLayoutListener(listener: OnChatLayoutListener?) {
        this.listener = listener
    }

    override fun setOnChatRecordTouchListener(voiceTouchListener: OnChatRecordTouchListener?) {
        this.recordTouchListener = voiceTouchListener
    }

    override fun setOnRecallMessageResultListener(listener: OnRecallMessageResultListener?) {
        this.recallMessageListener = listener
    }

    override fun setOnWillSendMessageListener(onWillSendMessageListener: OnWillSendMessageListener?) {
        this.onWillSendMessageListener = onWillSendMessageListener
    }

    override fun setOnReactionListener(reactionListener: OnReactionMessageListener?) {
        this.reactionMessageListener = reactionListener
    }

    override fun setOnChatFinishListener(listener: OnChatFinishListener?) {
        this.finishListener = listener
    }

    override fun setMultipleSelectRemoveMsgListener(listener: OnMultipleSelectRemoveMsgListener) {
        this.multipleSelectRemoveMsgListener = listener
    }

    override fun ackConversationReadSuccess() {

    }

    override fun ackConversationReadFail(code: Int, message: String?) {
        listener?.onError(code, message)
    }

    override fun ackGroupMessageReadSuccess() {

    }

    override fun ackGroupMessageReadFail(code: Int, message: String?) {
        listener?.onError(code, message)
    }

    override fun ackMessageReadSuccess() {

    }

    override fun ackMessageReadFail(code: Int, message: String?) {
        listener?.onError(code, message)
    }

    override fun createThumbFileFail(message: String?) {
        listener?.onError(ChatError.GENERAL_ERROR, message)
    }

    override fun addMsgAttrBeforeSend(message: ChatMessage?) {
        chatMessageReplyController.addReplyExtToMessage(message)
        onWillSendMessageListener?.onWillSendMessage(message)
    }

    override fun onErrorBeforeSending(code: Int, message: String?) {
        listener?.onError(code, message)
    }

    override fun deleteMessageSuccess(message: ChatMessage?) {
        chatBinding.layoutChatMessage.removeMessage(message)
        sendChatUpdateEvent()
    }

    override fun deleteMessageFail(message: ChatMessage?, code: Int, errorMsg: String?) {

    }

    override fun deleteMessageListSuccess() {
        multipleSelectRemoveMsgListener?.multipleSelectRemoveMsgSuccess()
        messageMultipleSelectController.clearSelectedMessages()
        chatBinding.layoutChatMessage.refreshMessages()
        sendChatUpdateEvent()
    }

    override fun deleteMessageListFail(code: Int, errorMsg: String?) {
        multipleSelectRemoveMsgListener?.multipleSelectRemoveMsgFail(code, errorMsg)
    }

    override fun recallMessageFinish(originalMessage: ChatMessage?, notification: ChatMessage?) {
        recallMessageListener?.recallSuccess(originalMessage, notification)
        removePinMessage(originalMessage)
        chatBinding.layoutChatMessage.refreshMessages()
        sendChatUpdateEvent()
    }

    override fun recallMessageFail(code: Int, message: String?) {
        listener?.onError(code, message)
    }

    override fun onSendMessageSuccess(message: ChatMessage?) {
        // If the sent message not be included in the message listener, then refresh the message list.
        if (ChatClient.getInstance().options.isIncludeSendMessageInMessageListener.not()) {
            message?.let {
                if (it.conversationId() == conversationId) {
                    chatBinding.layoutChatMessage.refreshToLatest()
                }
            }
        }
        listener?.onSuccess(message)
    }

    override fun onSendMessageError(message: ChatMessage?, code: Int, error: String?) {
        message?.let {
            if (it.conversationId() == conversationId) {
                refresh(message)
            }
        }
        listener?.onError(code, error)
    }

    override fun onSendMessageInProgress(message: ChatMessage?, progress: Int) {
        lifecycleScope.launch {
            message?.let {
                if (it.conversationId() == conversationId) {
                    refresh(message)
                }
            }
        }
    }

    override fun sendMessageFinish(message: ChatMessage?) {
        message?.let {
            if (it.conversationId() == conversationId) {
                chatNotificationController.dismissNotificationView()
                chatMessageReplyController.clearReplyMessageExt(it)
                if (loadDataType == ChatUIKitLoadDataType.THREAD){
                    chatMessageListLayout?.isNeedScrollToBottomWhenViewChange(true)
                }
                chatBinding.layoutChatMessage.refreshToLatest()
                sendMessageAddEvent(it.msgId)
            }
        }
    }

    override fun addReactionMessageSuccess(message: ChatMessage?) {
        refresh(message)
        reactionMessageListener?.addReactionMessageSuccess(message)
    }

    override fun addReactionMessageFail(message: ChatMessage?, code: Int, error: String?) {
        reactionMessageListener?.addReactionMessageFail(message, code, error)
    }

    override fun removeReactionMessageSuccess(message: ChatMessage?) {
        refresh(message)
        reactionMessageListener?.removeReactionMessageSuccess(message)
    }

    override fun removeReactionMessageFail(message: ChatMessage?, code: Int, error: String?) {
        reactionMessageListener?.removeReactionMessageFail(message, code, error)
    }

    override fun onModifyMessageSuccess(messageModified: ChatMessage?) {
        refresh(messageModified)
        modifyMessageListener?.onModifyMessageSuccess(messageModified)
    }

    override fun onModifyMessageFailure(messageId: String?, code: Int, error: String?) {
        modifyMessageListener?.onModifyMessageFailure(messageId, code, error)
    }

    override fun createReplyMessageExtSuccess(extObject: JSONObject?) {
        chatMessageReplyController.updateReplyMessageExt(true, extObject)
    }

    override fun createReplyMessageExtFail(code: Int, error: String?) {
        listener?.onError(code, error)
    }

    override fun onReportMessageSuccess(msgId: String) {
        reportMessageListener?.onReportMessageSuccess(msgId)
    }

    override fun onReportMessageFail(msgId: String, code: Int, error: String) {
        reportMessageListener?.onReportMessageFailure(msgId,code,error)
    }

    override fun onTranslationMessageSuccess(message: ChatMessage?) {
        refresh(message)
        translationMessageListener?.onTranslationMessageSuccess(message)
    }

    override fun onHideTranslationMessage(message: ChatMessage?) {
        refresh(message)
        translationMessageListener?.onHideTranslationMessage(message)
    }

    override fun onTranslationMessageFail(code: Int, error: String) {
        translationMessageListener?.onTranslationMessageFailure(code, error)
    }

    override fun onForwardMessageSuccess(message: ChatMessage?) {
        message?.let {
            if (it.conversationId() == conversationId) {
                chatBinding.layoutChatMessage.refreshToLatest()
            }
        }
        listener?.onForwardSuccess(message)
    }

    override fun onForwardMessageFail(message: ChatMessage?, code: Int, error: String?) {
        message?.let {
            if (it.conversationId() == conversationId) {
                refresh(message)
            }
        }
        listener?.onForwardError(code, error)
    }

    override fun onSendCombineMessageSuccess(message: ChatMessage?) {
        message?.let {
            if (it.conversationId() == conversationId) {
                chatBinding.layoutChatMessage.refreshToLatest()
            }
        }
        listener?.onSendCombineSuccess(message)
    }

    override fun onSendCombineMessageFail(message: ChatMessage?, code: Int, error: String?) {
        message?.let {
            if (it.conversationId() == conversationId) {
                refresh(message)
            }
        }
        listener?.onSendCombineError(message, code, error)
        listener?.onError(code, error)
    }

    override fun onPinMessageSuccess(message:ChatMessage?) {
        chatPinMessageController.updatePinMessage(message,ChatUIKitClient.getCurrentUser()?.id)
    }

    override fun onPinMessageFail(code: Int, error: String?) {
        ChatLog.e(TAG,"onPinMessageFail $code $error")
    }

    override fun onUnPinMessageSuccess(message: ChatMessage?) {
        chatPinMessageController.updatePinMessage(message,ChatUIKitClient.getCurrentUser()?.id)
    }

    override fun onUnPinMessageFail(code: Int, error: String?) {
        ChatLog.e(TAG,"onUnPinMessageFail $code $error")
    }

    override fun onFetchPinMessageFromServerSuccess(value: MutableList<ChatMessage>?) {
        if (value.isNullOrEmpty()){
            chatPinMessageController.hidePinInfoView()
        }else{
            chatPinMessageController.setData(value)
        }
    }

    fun initPinView(){
        chatPinMessageController.setPinInfoView()
    }

    override fun onFetchPinMessageFromServerFail(code: Int, error: String?) {
        ChatLog.e(TAG,"onFetchPinMessageFromServerFail $code $error")
    }

    override fun clearMenu() {
        menuHelper.clear()
    }

    override fun addItemMenu(itemId: Int, order: Int, title: String, groupId: Int) {
        menuHelper.addItemMenu(itemId, order, title, groupId)
    }

    override fun findItemVisible(id: Int, visible: Boolean) {
        menuHelper.findItemVisible(id, visible)
    }

    override fun setOnMenuChangeListener(listener: OnMenuChangeListener?) {
        menuHelper.setOnMenuChangeListener(listener)
    }

    override fun getChatMenuHelper(): ChatUIKitMenuHelper? {
        return menuHelper
    }

    private inner class ChatRoomListener: UIKitChatRoomListener() {
        override fun onChatRoomDestroyed(roomId: String?, roomName: String?) {
            //finishCurrent(ChatUIKitFinishReason.onChatRoomDestroyed, roomId)
        }

        override fun onRemovedFromChatRoom(
            reason: Int,
            roomId: String?,
            roomName: String?,
            participant: String?
        ) {
            if (!TextUtils.equals(roomId, conversationId)) {
                return
            }
            if (reason == 0) {
                //finishCurrent(ChatUIKitFinishReason.onChatRoomUserRemoved, roomId)
            }
        }

        override fun onMemberJoined(roomId: String?, participant: String?) {

        }

        override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {

        }

    }

    private inner class GroupListener: ChatUIKitGroupListener() {
        override fun onUserRemoved(groupId: String?, groupName: String?) {
            finishCurrent(ChatUIKitFinishReason.onGroupUserRemoved, groupId)
        }

        override fun onGroupDestroyed(groupId: String?, groupName: String?) {
            finishCurrent(ChatUIKitFinishReason.onGroupDestroyed, groupId)
        }

    }

    /**
     * finish current activity
     *
     * @param reason
     * @param id
     */
    private fun finishCurrent(reason: ChatUIKitFinishReason, id: String?) {
        finishListener?.onChatFinish(reason, id)
    }

    private fun sendReadAck(message: ChatMessage) {
        // enable send channel ack and should not show unread notification in chat
        if (ChatUIKitClient.getConfig()?.chatConfig?.enableSendChannelAck == true && ChatUIKitClient.getConfig()?.chatConfig?.showUnreadNotificationInChat == false) {
            //It is a received message, a read ack message has not been sent and it is a single chat
            if (message.direct() === ChatMessageDirection.RECEIVE && !message.isAcked && message.chatType === ChatType.Chat) {
                val type = message.type
                //Video, voice and files need to be clicked before sending
                if (type === ChatMessageType.VIDEO || type === ChatMessageType.VOICE || type === ChatMessageType.FILE) {
                    return
                }
                // If not the same conversation, do not send read ack.
                if (!TextUtils.equals(message.conversationId(), conversationId)) {
                    return
                }
                viewModel?.sendMessageReadAck(message.msgId)
            }
        }
    }

    private fun sendGroupReadAck(message: ChatMessage) {
        if (message.isNeedGroupAck && message.isUnread && TextUtils.equals(
                message.conversationId(),
                conversationId
            )
        ) {
            viewModel?.sendGroupMessageReadAck(message.msgId, "")
        }
    }

    private fun refreshMessages(messages: List<ChatMessage>?) {
        if (messages != null) {
            for (msg in messages) {
                chatBinding.layoutChatMessage.refreshMessage(msg)
            }
        }
    }

    private fun refresh(message: ChatMessage?) {
        chatBinding.layoutChatMessage.refreshMessage(message)
    }

    private fun isGroupConv(): Boolean {
        return chatType?.getConversationType() == ChatConversationType.GroupChat
    }

    private fun isChatroomConv(): Boolean {
        return chatType?.getConversationType() == ChatConversationType.ChatRoom
    }

    companion object {
        private val TAG: String = ChatUIKitLayout::class.java.simpleName.toString()
        private const val MSG_TYPING_HEARTBEAT = 0
        private const val MSG_TYPING_END = 1
        private const val MSG_OTHER_TYPING_END = 2

        const val ACTION_TYPING_BEGIN = "TypingBegin"
        const val ACTION_TYPING_END = "TypingEnd"
        protected const val TYPING_SHOW_TIME = 10000
        protected const val OTHER_TYPING_SHOW_TIME = 5000

        const val AT_PREFIX = "@"
        const val AT_SUFFIX = " "
    }

}