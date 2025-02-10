package io.agora.chat.uikit.widget.chatrow

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.common.extensions.addChildView
import io.agora.chat.uikit.common.extensions.getTheSameTypeChild
import io.agora.chat.uikit.common.extensions.getDateFormat
import io.agora.chat.uikit.common.extensions.isFail
import io.agora.chat.uikit.common.extensions.isSend
import io.agora.chat.uikit.common.extensions.isSuccess
import io.agora.chat.uikit.common.extensions.loadAvatar
import io.agora.chat.uikit.common.extensions.loadNickname
import io.agora.chat.uikit.common.helper.DateFormatHelper
import io.agora.chat.uikit.feature.chat.config.ChatUIKitMessageItemConfig
import io.agora.chat.uikit.feature.chat.config.resetBubbleBackground
import io.agora.chat.uikit.feature.chat.config.setAvatarConfig
import io.agora.chat.uikit.feature.chat.config.setNicknameConfig
import io.agora.chat.uikit.feature.chat.config.setTextMessageMinHeight
import io.agora.chat.uikit.feature.chat.config.setTextMessageTextConfigs
import io.agora.chat.uikit.feature.chat.config.setTimeTextConfig
import io.agora.chat.uikit.feature.chat.forward.helper.ChatUIKitMessageMultiSelectHelper
import io.agora.chat.uikit.feature.chat.interfaces.OnItemBubbleClickListener
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageListItemClickListener

abstract class ChatUIKitRow @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyle: Int = 0,
    val isSender: Boolean
): FrameLayout(context, attrs, defStyle) {

    val inflater by lazy { LayoutInflater.from(context) }

    val timeStampView: TextView? by lazy { findViewById(R.id.timestamp) }
    val userAvatarView: ImageView? by lazy { findViewById(R.id.iv_userhead) }
    val bubbleLayout: ViewGroup? by lazy { findViewById(R.id.bubble) }
    val usernickView: TextView? by lazy { findViewById(R.id.tv_userid) }
    val progressBar: ProgressBar? by lazy { findViewById(R.id.progress_bar) }
    val statusView: ImageView? by lazy { findViewById(R.id.msg_status) }
    val ackedView: TextView? by lazy { findViewById(R.id.tv_ack) }
    val deliveredView: TextView? by lazy { findViewById(R.id.tv_delivered) }
    val selectRadio: RadioButton? by lazy { findViewById(R.id.rb_select) }
    val editView: TextView? by lazy { findViewById(R.id.tv_edit) }
    val llBottomBubble: LinearLayout? by lazy { findViewById(R.id.ll_bottom_bubble) }
    protected val llTopBubble: LinearLayout? by lazy { findViewById(R.id.ll_top_bubble) }
    protected val llBubbleBottom: LinearLayout? by lazy { findViewById(R.id.ll_bubble_bottom) }

    var message: ChatMessage? = null
    var position: Int = -1

    var itemClickListener: OnMessageListItemClickListener? = null
    private var onItemBubbleClickListener: OnItemBubbleClickListener? = null

    private var messageItemConfig: ChatUIKitMessageItemConfig? = null

    init {
        onInflateView()
        onFindViewById()
    }

    /**
     * Bind data to view.
     * The method is called by ViewHolder.
     */
    fun setUpView(message: ChatMessage?, position: Int) {
        this.message = message
        this.position = position
        setUpBaseView()
        onSetUpView()
    }

    fun updateView() {
        updateMessageByStatus()
    }

    /**
     * Calls by ViewHolder.
     */
    fun setTimestamp(preMessage: ChatMessage?) {
        if (position == 0) {
            preMessage?.run {
                timeStampView?.text = getDateFormat(true)
                timeStampView?.visibility = VISIBLE
            }
        } else {
            setOtherTimestamp(preMessage)
        }
    }

    /**
     * Add child view to bottom bubble layout.
     */
    fun addChildToBottomBubbleLayout(child: View?,index:Int? = null) {
        llBottomBubble?.let {
            if (index != null){
                it.addChildView(child,index)
            }else{
                it.addChildView(child)
            }
        }
    }

    /**
     * Get the same type child view.
     */
    fun <T: View> getTargetTypeChildView(type: Class<T>): View? {
        var view = llBottomBubble?.getTheSameTypeChild(type)
        if (view == null) {
            view = llBubbleBottom?.getTheSameTypeChild(type)
        }
        if (view == null) {
            view = llTopBubble?.getTheSameTypeChild(type)
        }
        return view
    }

    private fun setUpBaseView() {
        setTimestamp(message)
        setAvatarAndNickname()
        setChatRowConfig()
        updateMessageStatus()
        initView()
        initListener()
    }

    private fun initView() {
        selectRadio?.let {
            it.visibility = if (ChatUIKitMessageMultiSelectHelper.getInstance().isMultiStyle(context, message?.conversationId())) VISIBLE else GONE
            it.isChecked = ChatUIKitMessageMultiSelectHelper.getInstance().isContainsMessage(context, message)
        }
        message?.run {
            editView?.let {
                it.visibility = if (body != null && body.operationCount() > 0) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateMessageStatus() {
        updateMessageByStatus()
        updateSendMessageStatus()
    }

    private fun updateMessageByStatus() {
        message?.run {
            when(status()) {
                ChatMessageStatus.CREATE -> {
                    // When get local messages and check it's status, change the create status to fail.
                }
                ChatMessageStatus.INPROGRESS -> {
                    onMessageInProgress()
                }
                ChatMessageStatus.SUCCESS -> {
                    onMessageSuccess()
                }
                ChatMessageStatus.FAIL -> {
                    onMessageError()
                }
            }
        }
    }

    private fun updateSendMessageStatus() {
        message?.run {
            if (isSend()) {
                // update sent and delivered status
                deliveredView?.let {
                    it.visibility = View.INVISIBLE
                    if (isSuccess()) {
                        it.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(
                                context,
                                R.drawable.uikit_msg_status_sent
                            ),
                            null
                        )
                        it.visibility = VISIBLE
                    }
                    if (ChatClient.getInstance().options.requireDeliveryAck && isDelivered) {
                        it.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(
                                getContext(),
                                R.drawable.uikit_msg_status_received
                            ),
                            null
                        )
                        it.visibility = VISIBLE
                    }
                }
                // update acked status
                ackedView?.let {
                    it.visibility = View.INVISIBLE
                    if (isSuccess() && ChatClient.getInstance().options.requireAck && isAcked) {
                        deliveredView?.visibility = View.INVISIBLE
                        it.visibility = VISIBLE
                    }
                }
                if (isSuccess()) {
                    showSuccessStatus()
                }
                // update error status
                setSendMessageFailStatus()
            }
        }
    }

    private fun updateMessageErrorStatus() {
        ackedView?.visibility = View.INVISIBLE
        deliveredView?.visibility = View.INVISIBLE
        editView?.visibility = View.GONE
        showErrorStatus()
    }

    fun setSendMessageFailStatus() {
        message?.run {
            if (isSend() && isFail()) {
                statusView?.visibility = View.VISIBLE
            }
        }
    }

    fun setItemConfig(messageItemConfig: ChatUIKitMessageItemConfig?) {
        this.messageItemConfig = messageItemConfig
    }

    private fun setChatRowConfig() {
        messageItemConfig?.let { config ->
            config.resetBubbleBackground(bubbleLayout, message?.isSend() == true)
            config.setTimeTextConfig(timeStampView)
            config.setTextMessageTextConfigs(findViewById(R.id.tv_chatcontent))
            config.setAvatarConfig(userAvatarView, message?.isSend() == true)
            config.setNicknameConfig(usernickView, message?.isSend() == true)
            config.setTextMessageMinHeight(bubbleLayout)
        }
    }

    /**
     * All user info is from message ext.
     */
    private fun setAvatarAndNickname() {
        message?.run {
            userAvatarView?.loadAvatar(this)
            usernickView?.loadNickname(this)
        }
    }

    open fun setOtherTimestamp(preMessage: ChatMessage?) {
        if (ChatClient.getInstance().options.isSortMessageByServerTime) {
            message?.let {
                if (preMessage != null && DateFormatHelper.isCloseEnough(it.msgTime
                        , preMessage.msgTime)) {
                    timeStampView?.visibility = GONE
                    return
                }
            }
        } else {
            message?.let {
                if (preMessage != null && DateFormatHelper.isCloseEnough(it.localTime()
                        , preMessage.localTime())) {
                    timeStampView?.visibility = GONE
                    return
                }
            }
        }

        message?.run {
            timeStampView?.text = getDateFormat(true)
            timeStampView?.visibility = VISIBLE
        }
    }

    private fun initListener() {
        bubbleLayout?.let {
            it.setOnClickListener {
                if (itemClickListener?.onBubbleClick(message) == true) {
                    return@setOnClickListener
                }
                onItemBubbleClickListener?.onBubbleClick(message)
            }
            it.setOnLongClickListener { view ->
                return@setOnLongClickListener itemClickListener?.onBubbleLongClick(view, message) == true
            }
        }
        statusView?.let {
            it.setOnClickListener {
                itemClickListener?.onResendClick(message)
            }
        }
        userAvatarView?.let {
            it.setOnClickListener {
                message?.run {
                    itemClickListener?.onUserAvatarClick(if (isSend()) ChatClient.getInstance().currentUser else from)
                }
            }
            it.setOnLongClickListener {
                message?.run {
                    if (itemClickListener != null) {
                        itemClickListener?.onUserAvatarLongClick(if (isSend()) ChatClient.getInstance().currentUser else from)
                        return@setOnLongClickListener true
                    }
                }
                return@setOnLongClickListener false
            }
        }
        selectRadio?.let { radio ->
            if (ChatUIKitMessageMultiSelectHelper.getInstance().isMultiStyle(context, message?.conversationId())) {
                setOnClickListener {
                    val checked = radio.isChecked
                    radio.isChecked = !checked
                    if (radio.isChecked) {
                        ChatUIKitMessageMultiSelectHelper.getInstance().addChatMessage(context, message)
                    } else {
                        ChatUIKitMessageMultiSelectHelper.getInstance().removeChatMessage(context, message)
                    }
                }
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (ChatUIKitMessageMultiSelectHelper.getInstance().isMultiStyle(context, message?.conversationId())) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * Show success status.
     */
    protected open fun showSuccessStatus() {
        progressBar?.visibility = INVISIBLE
        statusView?.visibility = INVISIBLE
    }

    protected open fun showErrorStatus() {
        progressBar?.visibility = INVISIBLE
        statusView?.visibility = VISIBLE
    }

    protected open fun showInProgressStatus() {
        progressBar?.visibility = VISIBLE
        statusView?.visibility = INVISIBLE
    }
    protected open fun onMessageSuccess() {
        updateSendMessageStatus()
    }

    protected open fun onMessageError() {
        updateMessageErrorStatus()
    }

    protected open fun onMessageInProgress() {
        showInProgressStatus()
    }

    protected open fun isSend():Boolean {
        return isSender
    }

    /**
     * Set message item click listeners.
     * @param listener
     */
    fun setOnMessageListItemClickListener(listener: OnMessageListItemClickListener?) {
        itemClickListener = listener
    }

    fun setOnItemBubbleClickListener(listener: OnItemBubbleClickListener?) {
        this.onItemBubbleClickListener = listener
    }

    /**
     * Override it and inflate your view in this method.
     */
    abstract fun onInflateView()

    /**
     * Override it and find view by id in this method.
     */
    open fun onFindViewById() {}

    /**
     * Override it and set data or listener in this method.
     */
    abstract fun onSetUpView()

    companion object {
        const val TAG = "ChatUIKitRow"
    }

}