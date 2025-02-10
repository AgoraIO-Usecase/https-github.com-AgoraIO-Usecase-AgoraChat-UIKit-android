package io.agora.chat.uikit.feature.chat.reaction

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.isSend
import io.agora.chat.uikit.databinding.UikitItemMessageReactionBinding
import io.agora.chat.uikit.databinding.UikitWidgetChatMessageReactionBinding
import io.agora.chat.uikit.feature.chat.reaction.interfaces.IChatReactionResultView
import io.agora.chat.uikit.feature.chat.reaction.interfaces.IMessageReaction
import io.agora.chat.uikit.feature.chat.reaction.interfaces.OnChatUIKitReactionErrorListener
import io.agora.chat.uikit.model.ChatUIKitReaction
import io.agora.chat.uikit.viewmodel.reaction.ChatUIKitReactionViewModel
import io.agora.chat.uikit.viewmodel.reaction.IChatReactionRequest
import io.agora.chat.uikit.widget.ChatUIKitFlowLayout

class ChatUIKitMessageReactionView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr), IMessageReaction, IChatReactionResultView {

    private var reactionErrorListener: OnChatUIKitReactionErrorListener? = null
    private val binding = UikitWidgetChatMessageReactionBinding.inflate(
                                LayoutInflater.from(context), this, true
                            )

    private lateinit var message: ChatMessage

    private var viewModel: IChatReactionRequest? = null
    private val adapter = ChatUIKitReactionAdapter()

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.flowReactionList.setAdapter(adapter)
        adapter.setOnItemClickListener { view, i ->
            val reaction = adapter.getData()?.get(i)
            reaction?.let {
                it.identityCode?.run {
                    if (it.isAddedBySelf) {
                        removeReaction(this)
                    } else {
                        addReaction(this)
                    }
                }
            }
        }
        binding.ivIcon.setOnClickListener {
            showMoreReactions(it)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        viewModel?.detachView(message)
    }

    override fun setupWithMessage(message: ChatMessage) {
        this.message = message
        setLayout(message.isSend())
        if (viewModel == null) {
            viewModel = if (context is AppCompatActivity) {
                ViewModelProvider(context)[ChatUIKitReactionViewModel::class.java]
            } else {
                ChatUIKitReactionViewModel()
            }
        }
        viewModel?.attachView(message,this)
    }

    private fun setLayout(isSender: Boolean) {
        val marginToParent = context.resources.getDimensionPixelSize(R.dimen.ease_chat_message_reaction_margin_parent)
        val marginToBubble = context.resources.getDimensionPixelSize(R.dimen.ease_chat_padding_bubble)
        if (isSender) {
            gravity = Gravity.END
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(marginToParent, 0, marginToBubble, 0)
            }
        } else {
            gravity = Gravity.START
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(marginToBubble, 0, marginToParent, 0)
            }
        }
    }

    override fun setViewModel(viewModel: IChatReactionRequest?) {
        this.viewModel = viewModel
        viewModel?.attachView(message, this)
    }

    override fun showReaction() {
        viewModel?.getMessageReactions(message)
    }

    override fun addReaction(reaction: String) {
        viewModel?.addReaction(message, reaction)
    }

    override fun removeReaction(reaction: String) {
        viewModel?.removeReaction(message, reaction)
    }

    override fun showMoreReactions(view: View?) {
        ChatUIKitMessageReactionsDialog(message).apply {
            show((this@ChatUIKitMessageReactionView.context as AppCompatActivity).supportFragmentManager, "ChatUIKitMessageReactionsDialog")
        }
    }

    override fun setReactionErrorListener(listener: OnChatUIKitReactionErrorListener?) {
        reactionErrorListener = listener
    }

    override fun getMessageReactionSuccess(reactions: List<ChatUIKitReaction>) {
        adapter.setData(reactions.toMutableList())
    }

    override fun addReactionSuccess(messageId: String) {
        showReaction()
    }

    override fun addReactionFail(messageId: String, errorCode: Int, errorMsg: String?) {
        ChatLog.e("ChatUIKitMessageReactionView", "addReactionFail: $errorCode $errorMsg")
        reactionErrorListener?.onError(messageId, errorCode, errorMsg)
    }

    override fun removeReactionSuccess(messageId: String) {
        showReaction()
    }

    override fun removeReactionFail(messageId: String, errorCode: Int, errorMsg: String?) {
        super.removeReactionFail(messageId, errorCode, errorMsg)
        ChatLog.e("ChatUIKitMessageReactionView", "removeReactionFail: $errorCode $errorMsg")
        reactionErrorListener?.onError(messageId, errorCode, errorMsg)
    }

    private class ChatUIKitReactionAdapter: ChatUIKitFlowLayout.ChatUIKitFlowAdapter<ChatUIKitReaction, ChatUIKitReactionAdapter.ViewHolder>() {
        class ViewHolder(val binding: UikitItemMessageReactionBinding): ChatUIKitFlowLayout.ViewHolder(binding.root)

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            return ViewHolder(UikitItemMessageReactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            val reaction = getData()?.get(position)
            reaction?.let {
                with(holder.binding) {
                    ivEmoji.setImageResource(reaction.icon)
                    tvEmojiCount.text = reaction.count.toString()
                    root.isSelected = reaction.isAddedBySelf
                }
            }
        }

    }
}