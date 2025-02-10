package io.agora.chat.uikit.feature.chat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.common.extensions.showToast
import io.agora.chat.uikit.databinding.UikitActivityChatBinding
import io.agora.chat.uikit.feature.chat.chathistory.ChatUIKitHistoryFragment
import io.agora.chat.uikit.feature.chat.interfaces.OnCombineMessageDownloadCallback
import io.agora.chat.uikit.feature.chat.interfaces.OnSendCombineMessageCallback

open class ChatUIKitHistoryActivity: ChatUIKitBaseActivity<UikitActivityChatBinding>() {
    override fun getViewBinding(inflater: LayoutInflater): UikitActivityChatBinding? {
        return UikitActivityChatBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val message = intent.getParcelableExtra(ChatUIKitConstant.EXTRA_CHAT_COMBINE_MESSAGE) as? ChatMessage
        if (message == null) {
            mContext.showToast("Message is null.")
            finish()
            return
        }
        var fragment = supportFragmentManager.findFragmentByTag("uikit_chat_history")
        if (fragment == null) {
            val builder = ChatUIKitHistoryFragment.Builder(message)
                .useTitleBar(true)
                .setTitleBarTitle(getString(R.string.uikit_combine_default))
                .enableTitleBarPressBack(true)
                .setTitleBarBackPressListener { finish() }
                .setEmptyLayout(R.layout.uikit_layout_no_data_show_nothing)
                .setOnSendCombineMessageCallback(object : OnSendCombineMessageCallback {

                    override fun onSendCombineError(
                        message: ChatMessage?,
                        code: Int,
                        errorMsg: String?
                    ) {
                        // Handle the error message
                    }
                })
                .setOnCombineMessageDownloadCallback(object: OnCombineMessageDownloadCallback {
                    override fun onDownloadSuccess(messages: List<ChatMessage>) {

                    }

                    override fun onDownloadFail(error: Int, errorMsg: String?) {
                        errorMsg?.apply {
                            mContext.showToast(this)
                        }
                    }
                })
            setChildSettings(builder)
            fragment = builder.build()
        }
        fragment?.let { fragment ->
            supportFragmentManager.beginTransaction().replace(binding.flFragment.id, fragment).commit()
        }
    }

    protected open fun setChildSettings(builder: ChatUIKitHistoryFragment.Builder) {}

    companion object {
        fun actionStart(context: Context, combineMessage: ChatMessage?) {
            Intent(context, ChatUIKitHistoryActivity::class.java).apply {
                putExtra(ChatUIKitConstant.EXTRA_CHAT_COMBINE_MESSAGE, combineMessage)
                ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(this.clone() as Intent)?.let {
                    if (it.hasRoute()) {
                        context.startActivity(it)
                        return
                    }
                }
                context.startActivity(this)
            }
        }
    }
}