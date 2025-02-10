package io.agora.chat.uikit.feature.chat.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatFileMessageBody
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.common.impl.CallbackImpl
import io.agora.chat.uikit.common.utils.ChatUIKitCompat
import io.agora.chat.uikit.databinding.UikitActivityShowFileBinding
import kotlinx.coroutines.launch

class ChatUIKitShowNormalFileActivity : ChatUIKitBaseActivity<UikitActivityShowFileBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val message: ChatMessage? = intent.getParcelableExtra("msg")
        if (message?.body !is ChatFileMessageBody) {
            Toast.makeText(
                this@ChatUIKitShowNormalFileActivity,
                "Unsupported message body",
                Toast.LENGTH_SHORT
            ).show()
            finish()
            return
        }
        message.setMessageStatusCallback(CallbackImpl(
            onSuccess = {
                mainScope().launch{
                    ChatUIKitCompat.openFile(
                        mContext,
                        (message.body as ChatFileMessageBody).localUri
                    )
                    finish()
                }
            },
            onError = { code, error ->
                mainScope().launch {
                    ChatUIKitCompat.deleteFile(
                        mContext,
                        (message.body as ChatFileMessageBody).localUri
                    )
                    var str4: String =
                        resources.getString(R.string.uikit_failed_to_download_file)
                    if (code == ChatError.FILE_NOT_FOUND) {
                        str4 = resources.getString(R.string.uikit_file_expired)
                    }
                    Toast.makeText(mContext, str4 + message, Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            },
            onProgress = { progress ->
                mainScope().launch {
                    this@ChatUIKitShowNormalFileActivity.binding.progressBar.progress = progress
                }
            }
        ))
        ChatClient.getInstance().chatManager().downloadAttachment(message)
    }

    override fun getViewBinding(inflater: LayoutInflater): UikitActivityShowFileBinding {
        return UikitActivityShowFileBinding.inflate(inflater)
    }


    companion object {
        private val TAG = ChatUIKitShowNormalFileActivity::class.java.simpleName
        fun actionStart(context: Context, message: ChatMessage?) {
            val intent = Intent(context, ChatUIKitShowNormalFileActivity::class.java)
            intent.putExtra("msg", message)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    context.startActivity(it)
                    return
                }
            }
            context.startActivity(intent)
        }
    }
}