package io.agora.chat.uikit.feature.chat.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.setParentInfo
import io.agora.chat.uikit.feature.chat.config.ChatUIKitMessageItemConfig
import io.agora.chat.uikit.feature.chat.controllers.ChatUIKitAddExtendFunctionViewController
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageAckSendCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnMessageListItemClickListener
import io.agora.chat.uikit.feature.chat.reaction.interfaces.OnChatUIKitReactionErrorListener
import io.agora.chat.uikit.feature.chat.reply.interfaces.OnMessageReplyViewClickListener
import io.agora.chat.uikit.feature.chat.viewholders.ChatUIKitRowViewHolder
import io.agora.chat.uikit.feature.chat.viewholders.ChatUIKitViewHolderFactory
import io.agora.chat.uikit.feature.chat.viewholders.ChatUIKitMessageViewType
import io.agora.chat.uikit.feature.thread.interfaces.OnMessageChatThreadClickListener
import io.agora.chat.uikit.interfaces.UrlPreviewStatusCallback
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRow
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRowText

open class ChatUIKitMessagesAdapter(
    private var messageItemConfig: ChatUIKitMessageItemConfig? = null
): ChatUIKitBaseRecyclerViewAdapter<ChatMessage>() {
    private var parentId: String? = null
    private var parentMsgId: String? = null
    private var messageAckSendCallback: OnMessageAckSendCallback? = null
    private var reactionErrorListener: OnChatUIKitReactionErrorListener? = null
    private var itemClickListener: OnMessageListItemClickListener? = null
    private var replyViewClickListener: OnMessageReplyViewClickListener? = null
    private var threadViewEventListener: OnMessageChatThreadClickListener? = null
    private var urlPreviewDownLoadListener: UrlPreviewStatusCallback? = null
    private var highlightPosition = -1
    private var colorAnimation: ValueAnimator? = null

    override fun getItemNotEmptyViewType(position: Int): Int {
        return ChatUIKitViewHolderFactory.getViewType(getItem(position))
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        return ChatUIKitViewHolderFactory.createViewHolder(parent, ChatUIKitMessageViewType.from(viewType))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatMessage>, position: Int) {
        if (holder.itemView is ChatUIKitRow) {
            (holder.itemView as ChatUIKitRow).setItemConfig(messageItemConfig)
        }
        // Set message ack send callback.
        if (holder is ChatUIKitRowViewHolder) {
            holder.setOnMessageAckSendCallback(messageAckSendCallback)
        }

        super.onBindViewHolder(holder, position)

        if (holder is ChatUIKitRowViewHolder && holder.itemView is ChatUIKitRow) {
            (holder.itemView as? ChatUIKitRow)?.let {
                it.setOnMessageListItemClickListener(itemClickListener)
            }
            (holder.itemView as? ChatUIKitRowText)?.let {
                addMessageReplyView(it, getItem(position))
                addMessageTranslationView(it,getItem(position))
                addMessageUrlPreview(it,getItem(position))
            }
            addMessageReactionView(holder.itemView as ChatUIKitRow, getItem(position))
            addMessageThreadRegionView(holder.itemView as ChatUIKitRow, getItem(position))
        }

        if (position == highlightPosition) {
            val outLayout: View? = holder.itemView.findViewById(R.id.cl_bubble_out)
            outLayout?.let { startAnimator(it) } ?: startAnimator(holder.itemView)
            highlightPosition = -1
        }
    }

    override fun getItemId(position: Int): Long {
        getItem(position)?.let {
            return it.hashCode().toLong()
        }
        return super.getItemId(position)
    }

    private fun addMessageReactionView(view: ChatUIKitRow, message: ChatMessage?) {
        message?.setParentInfo(parentId, parentMsgId)
        ChatUIKitAddExtendFunctionViewController.addReactionViewToMessage(message, view, reactionErrorListener)
    }

    private fun addMessageReplyView(view: ChatUIKitRowText, message: ChatMessage?) {
        ChatUIKitAddExtendFunctionViewController.addReplyViewToMessage(message, view, replyViewClickListener)
    }

    private fun addMessageThreadRegionView(view: ChatUIKitRow, message: ChatMessage?){
        ChatUIKitAddExtendFunctionViewController.addThreadRegionViewToMessage(message, view,threadViewEventListener)
    }

    private fun addMessageTranslationView(view: ChatUIKitRowText, message: ChatMessage?){
        ChatUIKitAddExtendFunctionViewController.addTranslationViewToMessage(view, message)
    }

    private fun addMessageUrlPreview(view: ChatUIKitRowText, message: ChatMessage?){
        ChatUIKitAddExtendFunctionViewController.addUrlPreviewToMessage(view, message,urlPreviewDownLoadListener)
    }

    /**
     * Set message item config.
     */
    fun setItemConfig(config: ChatUIKitMessageItemConfig?) {
        this.messageItemConfig = config
    }

    /**
     * Set parent info for chat thread.
     * @param parentId The parent id, usually is the group that the chat thread belongs to.
     * @param parentMsgId The parent message id, usually is the group message id that created the chat thread.
     *                  It can be null if the group message was recalled.
     */
    fun setParentInfo(parentId: String?, parentMsgId: String?){
        this.parentId = parentId
        this.parentMsgId = parentMsgId
    }

    /**
     * Set message item click listener.
     */
    fun setOnMessageListItemClickListener(listener: OnMessageListItemClickListener?) {
        itemClickListener = listener
    }

    /**
     * Set message reaction error listener.
     */
    fun setOnMessageReactionErrorListener(listener: OnChatUIKitReactionErrorListener?) {
        reactionErrorListener = listener
    }

    fun setOnMessageThreadEventListener(listener: OnMessageChatThreadClickListener?){
        threadViewEventListener = listener
    }

    /**
     * Set message ack send callback.
     */
    fun setOnMessageAckSendCallback(callback: OnMessageAckSendCallback?) {
        this.messageAckSendCallback = callback
    }

    /**
     * Highlight the item view.
     * @param position
     */
    fun highlightItem(position: Int) {
        highlightPosition = position
        notifyItemChanged(position)
    }

    private fun startAnimator(view: View) {
        val background = view.background
        val darkColor = ContextCompat.getColor(mContext!!, R.color.ease_chat_item_bg_dark)
        colorAnimation?.cancel()
        colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), Color.TRANSPARENT, darkColor)
        colorAnimation?.duration = 500
        colorAnimation?.addUpdateListener { animator ->
            view.setBackgroundColor(animator.animatedValue as Int)
            if (animator.animatedValue as Int == darkColor) {
                view.background = background
            } else if (animator.animatedValue as Int == 0) {
                view.background = null
            }
        }
        colorAnimation?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                view.background = background
            }
        })
        colorAnimation?.start()
    }

    fun setOnMessageReplyViewClickListener(listener: OnMessageReplyViewClickListener?) {
        this.replyViewClickListener = listener
    }

    fun setDownLoadUrlPreviewHtmlListener(callback: UrlPreviewStatusCallback){
        this.urlPreviewDownLoadListener = callback
    }
}