package io.agora.chat.uikit.widget.chatrow

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatDownloadStatus
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.common.ChatTextFormater
import io.agora.chat.uikit.common.ChatVideoMessageBody
import io.agora.chat.uikit.common.extensions.isSend
import io.agora.chat.uikit.common.extensions.isSuccess
import io.agora.chat.uikit.common.extensions.loadImageFromMessage
import io.agora.chat.uikit.common.helper.DateFormatHelper
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils

open class ChatUIKitRowVideo @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : ChatUIKitRowFile(context, attrs, defStyleAttr, isSender) {
    private val imageView: ImageView? by lazy { findViewById(R.id.chatting_content_iv) }
    private val sizeView: TextView? by lazy { findViewById(R.id.chatting_size_iv) }
    private val timeLengthView: TextView? by lazy { findViewById(R.id.chatting_length_iv) }
    private val playView: ImageView? by lazy { findViewById(R.id.chatting_status_btn) }

    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.uikit_row_received_video else R.layout.uikit_row_sent_video,
            this
        )
    }

    override fun onSetUpView() {
        bubbleLayout?.background = null
        playView?.visibility = GONE
        message?.run {
            val videoBody = body as ChatVideoMessageBody
            if (isSuccess() && videoBody.thumbnailDownloadStatus() == ChatDownloadStatus.DOWNLOADING) {
                setMessageDownloadCallback(true)
            }
            if (videoBody.duration >= 0) {
                val time = if (videoBody.duration > 1000) {
                    DateFormatHelper.toTime(videoBody.duration)
                } else {
                    DateFormatHelper.toTimeBySecond(videoBody.duration)
                }
                timeLengthView?.text = time
            }
            if (!isSend()) {
                if (videoBody.videoFileLength > 0) {
                    val size: String = ChatTextFormater.getDataSize(videoBody.videoFileLength)
                    sizeView?.text = size
                }
            } else {
                val videoFileLength: Long = videoBody.videoFileLength
                sizeView?.text = ChatTextFormater.getDataSize(videoFileLength)
            }
            ChatLog.d(TAG, "video thumbnailStatus:" + videoBody.thumbnailDownloadStatus())
            if (!isSend()) {
                if (videoBody.thumbnailDownloadStatus() === ChatDownloadStatus.DOWNLOADING) {
                    //
                } else {
                    // System.err.println("!!!! not back receive, show image directly");
                    showVideoThumbView(this)
                }
            } else {
                if (status() == ChatMessageStatus.SUCCESS) {
                    if (videoBody.thumbnailDownloadStatus() === ChatDownloadStatus.DOWNLOADING
                        || videoBody.thumbnailDownloadStatus() === ChatDownloadStatus.PENDING
                        || videoBody.thumbnailDownloadStatus() === ChatDownloadStatus.FAILED) {
                        progressBar?.visibility = INVISIBLE
                        if (videoBody.thumbnailDownloadStatus() === ChatDownloadStatus.PENDING) {
                            showVideoThumbView(this)
                        } else if (videoBody.thumbnailDownloadStatus() === ChatDownloadStatus.FAILED){
                            imageView?.setImageResource(R.drawable.uikit_default_video_thumbnail)
                        }
                    } else {
                        progressBar?.visibility = GONE
                        showVideoThumbView(this)
                    }
                } else {
                    showVideoThumbView(this)
                }
            }
            // If local thumb exists, show video icon directly
            if (ChatUIKitFileUtils.isFileExistByUri(context, videoBody.localThumbUri)) {
                playView?.visibility = VISIBLE
            }
            //setImageIncludeThread(imageView)
        }
    }

    override fun onDownloadAttachmentSuccess() {
        super.onDownloadAttachmentSuccess()
        progressBar?.visibility = GONE
        playView?.visibility = VISIBLE
        message?.run {
            showVideoThumbView(this)
        }
    }

    override fun onDownloadAttachmentProgress(progress: Int) {
        super.onDownloadAttachmentProgress(progress)
        progressBar?.visibility = visibility
    }

    override fun onDownloadAttachmentError(code: Int, error: String?) {
        super.onDownloadAttachmentError(code, error)
        progressBar?.visibility = GONE
        playView?.visibility = GONE
        imageView?.setImageResource(R.drawable.uikit_default_video_thumbnail)
    }

    /**
     * show video thumbnails
     * @param message
     */
    @SuppressLint("StaticFieldLeak")
    private fun showVideoThumbView(message: ChatMessage) {
        imageView?.loadImageFromMessage(message, onSuccess = {
            playView?.visibility = VISIBLE
        }, onError = {
            playView?.visibility = GONE
        })
    }

    companion object {
        private val TAG = ChatUIKitRowVideo::class.java.simpleName
    }
}