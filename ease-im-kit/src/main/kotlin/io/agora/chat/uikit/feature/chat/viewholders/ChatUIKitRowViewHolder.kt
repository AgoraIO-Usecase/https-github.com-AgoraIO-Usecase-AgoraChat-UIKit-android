package io.agora.chat.uikit.feature.chat.viewholders

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.extensions.ioScope
import io.agora.chat.uikit.common.extensions.isSend
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.feature.chat.interfaces.OnItemBubbleClickListener
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageAckSendCallback
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRow
import kotlinx.coroutines.launch

open class ChatUIKitRowViewHolder(itemView: View): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatMessage>(itemView),
    OnItemBubbleClickListener {
    private var messageAckSendCallback: OnMessageAckSendCallback? = null
    private val TAG = ChatUIKitRowViewHolder::class.java.simpleName
    protected var mContext: Context = itemView.context
    private var chatRow: ChatUIKitRow? = null
    private var message: ChatMessage? = null

    init {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        itemView.layoutParams = params
    }

    override fun initView(itemView: View?) {
        chatRow = itemView as ChatUIKitRow
        chatRow?.setOnItemBubbleClickListener(this)
    }

    override fun setData(item: ChatMessage?, position: Int) {
        message = item
        chatRow?.setUpView(item, position)
        handleMessage()
    }

    override fun setDataList(data: List<ChatMessage>?, position: Int) {
        super.setDataList(data, position)
        data?.let {
            if (position < data.size) {
                chatRow?.setTimestamp(if (position == 0) null else data[position - 1])
            }
        }
    }

    override fun onBubbleClick(message: ChatMessage?) {

    }

    open fun onDetachedFromWindow() {}

    open fun handleMessage() {
        message?.run {
            if (isSend()) {
                handleSendMessage(message)
            } else {
                handleReceiveMessage(message)
            }
        }
    }

    /**
     * send message
     * @param message
     */
    protected open fun handleSendMessage(message: ChatMessage?) {
        // Update the view according to the message current status.
        //getChatRow().updateView(message)
    }

    /**
     * receive message
     * @param message
     */
    protected open fun handleReceiveMessage(message: ChatMessage?) {
        //Here no longer send read_ack message separately, instead enter the chat page to send channel_ack
        //New messages are sent in the onReceiveMessage method of the chat page, except for video
        // , voice and file messages, and send read_ack messages
        if (ChatUIKitClient.getConfig()?.chatConfig?.enableSendChannelAck == true && ChatUIKitClient.getConfig()?.chatConfig?.showUnreadNotificationInChat == false) {
            return
        }
        message?.let { msg ->
            // make message as read
            mContext.ioScope().launch {
                ChatClient.getInstance().chatManager().getConversation(msg.conversationId())?.let {
                    it.markMessageAsRead(msg.msgId)
                }
            }
            // send message read ack
            val type = msg.type
            //Video, voice and files need to be clicked before sending
            if (type === ChatMessageType.VIDEO || type === ChatMessageType.VOICE || type === ChatMessageType.FILE) {
                return
            }
            if (!msg.isAcked && msg.chatType === ChatType.Chat) {
                mContext.ioScope().launch {
                    try {
                        ChatClient.getInstance().chatManager()
                            .ackMessageRead(msg.from, msg.msgId)
                        getContext().mainScope().launch {
                            messageAckSendCallback?.onSendAckSuccess(msg)
                        }
                    } catch (e: ChatException) {
                        e.printStackTrace()
                        getContext().mainScope().launch {
                            messageAckSendCallback?.onSendAckError(msg, e.errorCode, e.message)
                        }
                    }
                }
            }
        }
    }

    open fun getContext(): Context {
        return mContext
    }

    open fun getChatRow(): ChatUIKitRow? {
        return chatRow
    }

    /**
     * Set message ack send callback.
     */
    fun setOnMessageAckSendCallback(callback: OnMessageAckSendCallback?) {
        this.messageAckSendCallback = callback
    }
}