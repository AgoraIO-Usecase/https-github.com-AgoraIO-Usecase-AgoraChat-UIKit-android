package io.agora.chat.uikit.feature.chat.controllers

import android.view.View
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageDirection
import io.agora.chat.uikit.common.extensions.hasThreadChat
import io.agora.chat.uikit.common.extensions.isReplyMessage
import io.agora.chat.uikit.common.extensions.isSend
import io.agora.chat.uikit.feature.chat.reaction.ChatUIKitMessageReactionView
import io.agora.chat.uikit.feature.chat.reaction.interfaces.OnChatUIKitReactionErrorListener
import io.agora.chat.uikit.feature.chat.reply.ChatUIKitMessageReplyView
import io.agora.chat.uikit.feature.chat.reply.interfaces.OnMessageReplyViewClickListener
import io.agora.chat.uikit.feature.chat.translation.ChatUIKitMessageTranslationView
import io.agora.chat.uikit.feature.chat.urlpreview.ChatUIKitMessageUrlPreview
import io.agora.chat.uikit.feature.thread.widgets.ChatUIKitMessageThreadView
import io.agora.chat.uikit.feature.thread.interfaces.OnMessageChatThreadClickListener
import io.agora.chat.uikit.interfaces.UrlPreviewStatusCallback
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRow
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRowText

object ChatUIKitAddExtendFunctionViewController{

    /**
     * Add reply view to message.
     */
    fun addReplyViewToMessage(message: ChatMessage?, view: ChatUIKitRowText, listener: OnMessageReplyViewClickListener?) {
        val targetView = view.getTargetTypeChildView(ChatUIKitMessageReplyView::class.java)
        if (message?.isReplyMessage() == true) {
            if (targetView == null) {
                addReplyView(view, message, listener)
            } else {
                (targetView as ChatUIKitMessageReplyView).updateMessageInfo(message)
                targetView.visibility = View.VISIBLE
            }
        } else {
            targetView?.visibility = View.GONE
        }
    }

    private fun addReplyView(view: ChatUIKitRowText, message: ChatMessage?, listener: OnMessageReplyViewClickListener?) {
        val replyView = ChatUIKitMessageReplyView(view.context, isSender = message?.isSend() ?: false)
        view.addChildToTopBubbleLayout(replyView)
        replyView.setOnMessageReplyViewClickListener(listener)
        replyView.visibility = View.GONE
        val isLoaded = replyView.updateMessageInfo(message)
        if (isLoaded) {
            replyView.visibility = View.VISIBLE
        }
    }

    /**
     * Add reaction view to message.
     */
    fun addReactionViewToMessage(
        message: ChatMessage?,
        view: ChatUIKitRow,
        reactionErrorListener: OnChatUIKitReactionErrorListener?
    ) {
        ChatUIKitClient.getConfig()?.chatConfig?.enableMessageReaction?.let {
            if (!it) {
                return
            }
        }
        val reactionView = view.getTargetTypeChildView(ChatUIKitMessageReactionView::class.java)
        message?.messageReaction?.let {
            if (it.isNotEmpty()) {
                if (reactionView == null) {
                    ChatUIKitMessageReactionView(view.context).let { child ->
                        child.setupWithMessage(message)
                        child.showReaction()
                        child.setReactionErrorListener(reactionErrorListener)
                        view.addChildToBottomBubbleLayout(child)
                    }
                } else {
                    (reactionView as ChatUIKitMessageReactionView).run {
                        setupWithMessage(message)
                        showReaction()
                        setReactionErrorListener(reactionErrorListener)
                        visibility = View.VISIBLE
                    }
                }
            } else {
                reactionView?.visibility = View.GONE
            }
        } ?: run {
            reactionView?.visibility = View.GONE
        }
    }

    /**
     * Add thread view to message.
     */
    fun addThreadRegionViewToMessage(
        message: ChatMessage?,
        view: ChatUIKitRow,
        threadEventListener: OnMessageChatThreadClickListener?
    ){
        val threadView = view.getTargetTypeChildView(ChatUIKitMessageThreadView::class.java)
        message?.let {
            if (it.hasThreadChat() && !it.isChatThreadMessage) {
                if (threadView == null) {
                    ChatUIKitMessageThreadView(view.context).let { child ->
                        child.setupWithMessage(it)
                        child.showThread()
                        child.setThreadEventListener(threadEventListener)
                        view.addChildToBottomBubbleLayout(child,0)
                    }
                } else {
                    (threadView as ChatUIKitMessageThreadView).run {
                        setupWithMessage(it)
                        showThread()
                        setThreadEventListener(threadEventListener)
                        visibility = View.VISIBLE
                    }
                }
            } else {
                threadView?.visibility = View.GONE
            }
        } ?: run {
            threadView?.visibility = View.GONE
        }
    }

    fun addTranslationViewToMessage(
        view: ChatUIKitRowText,
        message: ChatMessage?
    ){
        ChatUIKitClient.getConfig()?.chatConfig?.enableTranslationMessage?.let {
            if (!it) {
                return
            }
        }
        val isSend = message?.direct() == ChatMessageDirection.SEND
        var translationView: ChatUIKitMessageTranslationView

        view.getBubbleBottom?.let {
            if (it.childCount == 0){
                translationView = ChatUIKitMessageTranslationView(isSend,false,view.context)
                translationView.id = R.id.ease_translation_view
            }else{
                translationView = it.findViewById(R.id.ease_translation_view)
            }
            view.addChildToBubbleBottomLayout(translationView)
            translationView.visibility = View.GONE
            val isLoaded = translationView.updateMessageInfo(message)
            if (isLoaded) {
                translationView.visibility = View.VISIBLE
            }
        }
    }

    fun addUrlPreviewToMessage(
        view: ChatUIKitRowText,
        message: ChatMessage?,
        callback: UrlPreviewStatusCallback?=null
    ){
        ChatUIKitClient.getConfig()?.chatConfig?.enableUrlPreview?.let {
            if (!it) {
                return
            }
        }
        val isSend = message?.direct() == ChatMessageDirection.SEND
        var urlPreview: ChatUIKitMessageUrlPreview?
        view.getBubbleBottom?.let {
            if (it.childCount == 0){
                urlPreview = ChatUIKitMessageUrlPreview(isSend,view.context)
                urlPreview?.id = R.id.ease_url_preview
            }else{
                urlPreview = it.findViewById(R.id.ease_url_preview)
                if (urlPreview == null){
                    urlPreview = ChatUIKitMessageUrlPreview(isSend,view.context)
                    urlPreview?.id = R.id.ease_url_preview
                }
            }
            view.addChildToBubbleBottomLayout(urlPreview)
            urlPreview?.checkPreview(message,callback)
        }
    }
}