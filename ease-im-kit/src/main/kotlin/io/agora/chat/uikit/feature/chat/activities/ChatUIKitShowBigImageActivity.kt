/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agora.chat.uikit.feature.chat.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatDownloadStatus
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatImageMessageBody
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.common.impl.CallbackImpl
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils
import io.agora.chat.uikit.databinding.UikitActivityShowBigImageBinding
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.widget.photoview.OnPhotoTapListener
import io.agora.chat.uikit.widget.photoview.OnViewTapListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * download and show original image
 *
 */
class ChatUIKitShowBigImageActivity : ChatUIKitBaseActivity<UikitActivityShowBigImageBinding>() {
    private var default_res: Int = R.drawable.uikit_default_image
    private var filename: String? = null
    private var isNeedDownload = false
    private var msgId: String? = null
    private var mProgress: Int = 0

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        default_res = intent.getIntExtra("default_image", R.drawable.uikit_default_image)
        val uri: Uri? = intent.getParcelableExtra("uri")
        filename = intent.extras?.getString("filename")
        msgId = intent.extras?.getString("messageId")
        val emojiIconId: String? =
            intent.extras?.getString(ChatUIKitConstant.MESSAGE_ATTR_EXPRESSION_ID)
        ChatLog.d(TAG, "show big msgId:$msgId")

        //show the image if it exist in local path
        if (ChatUIKitFileUtils.isFileExistByUri(this, uri)) {
            binding.image.load(uri)
        } else if (!TextUtils.isEmpty(emojiIconId)) {
            showBigExpression(emojiIconId)
        } else if (msgId != null) {
            var msg: ChatMessage? = ChatClient.getInstance().chatManager().getMessage(msgId)
            if (msg == null) {
                msg = intent.getParcelableExtra("msg")
                if (msg == null) {
                    ChatLog.e(TAG, "message is null, messageId: $msgId")
                    finish()
                    return
                }
            }
            val body: ChatImageMessageBody = msg.body as ChatImageMessageBody
            if (ChatUIKitFileUtils.isFileExistByUri(this, body.localUri)) {
                binding.image.load(body.localUri)
            } else {
                downloadImage(msg)
            }
        } else {
            binding.image.setImageResource(default_res)
        }
        binding.image.setOnViewTapListener(object : OnViewTapListener {
            override fun onViewTap(view: View?, x: Float, y: Float) {
                finish()
            }
        })
        binding.image.setOnPhotoTapListener(object : OnPhotoTapListener {
            override fun onPhotoTap(view: View?, x: Float, y: Float) {
                finish()
            }
        })
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(true, ContextCompat.getColor(mContext, R.color.black), false)
    }

    override fun getViewBinding(inflater: LayoutInflater): UikitActivityShowBigImageBinding? {
        return UikitActivityShowBigImageBinding.inflate(inflater)
    }

    /**
     * Show custom emoji icon
     * @param emojiIconId emoji id
     */
    private fun showBigExpression(emojiIconId: String?) {
        ChatUIKitClient.getEmojiconInfoProvider()?.getEmojiconInfo(emojiIconId)?.let {
            if (it.bigIcon != -1) {
                binding.image.load(it.bigIcon) {
                    placeholder(R.drawable.uikit_default_expression)
                }
            } else if (!it.bigIconPath.isNullOrEmpty()) {
                binding.image.load(it.bigIconPath) {
                    placeholder(R.drawable.uikit_default_expression)
                }
            } else {
                binding.image.setImageResource(R.drawable.uikit_default_expression)
            }
        }
    }

    /**
     * download image
     *
     * @param msg
     */
    @SuppressLint("NewApi")
    private fun downloadImage(msg: ChatMessage?) {
        if (msg == null) {
            ChatLog.e(TAG, "download image with empty message!")
            return
        }
        isNeedDownload = true
        ChatLog.e(TAG, "download with messageId: " + msg.getMsgId())
        val str1: String = resources.getString(R.string.uikit_download_the_pictures)
        val pd = ProgressDialog(this)
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        pd.setCanceledOnTouchOutside(false)
        pd.setMessage(str1)
        pd.show()
        msg.setMessageStatusCallback(CallbackImpl(
            onSuccess = {
                mContext.mainScope().launch {
                    checkDownloadStatus(msg.body as ChatImageMessageBody,
                        onSuccess = {
                            pd?.dismiss()
                            val localUrlUri: Uri = (msg.body as ChatImageMessageBody).localUri
                            binding.image.load(localUrlUri) {
                                error(R.drawable.uikit_default_image)
                            }
                        },
                        onError = {
                            pd?.dismiss()
                            binding.image.setImageResource(default_res)
                        })
                }
            },
            onError = { code, error ->
                mContext.mainScope().launch {
                    pd?.dismiss()
                    binding.image.setImageResource(default_res)
                    if (code == ChatError.FILE_NOT_FOUND) {
                        Toast.makeText(
                            mContext,
                            R.string.uikit_image_expired,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            onProgress = { progress ->
                if (progress > mProgress) {
                    mProgress = progress
                    mContext.mainScope().launch {
                        val str2: String = resources.getString(R.string.uikit_download_the_pictures_new)
                        pd.setMessage("$str2$progress%")
                    }
                }
            }
        ))
        ChatClient.getInstance().chatManager().downloadAttachment(msg)
    }

    private suspend fun checkDownloadStatus(imageBody: ChatImageMessageBody, onSuccess: () -> Unit, onError: () -> Unit = {}) {
        if (imageBody.downloadStatus() == ChatDownloadStatus.DOWNLOADING) {
            delay(1000)
            checkDownloadStatus(imageBody, onSuccess, onError)
        } else if (imageBody.downloadStatus() == ChatDownloadStatus.SUCCESSED) {
            onSuccess.invoke()
        } else {
            onError.invoke()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isNeedDownload && !msgId.isNullOrEmpty()) {
            ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.UPDATE.name).post(lifecycleScope,
                ChatUIKitEvent(ChatUIKitEvent.EVENT.UPDATE.name, ChatUIKitEvent.TYPE.MESSAGE, msgId)
            )
        }
    }

    companion object {
        private const val TAG = "ShowBigImage"
        fun actionStart(context: Context, imageUri: Uri?) {
            val intent = Intent(context, ChatUIKitShowBigImageActivity::class.java)
            intent.putExtra("uri", imageUri)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    context.startActivity(it)
                    return
                }
            }
            context.startActivity(intent)
        }

        fun actionStart(context: Context, messageId: String?, filename: String?) {
            val intent = Intent(context, ChatUIKitShowBigImageActivity::class.java)
            intent.putExtra("messageId", messageId)
            intent.putExtra("filename", filename)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    context.startActivity(it)
                    return
                }
            }
            context.startActivity(intent)
        }

        fun actionStart(context: Context, message: ChatMessage?) {
            val intent = Intent(context, ChatUIKitShowBigImageActivity::class.java)
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