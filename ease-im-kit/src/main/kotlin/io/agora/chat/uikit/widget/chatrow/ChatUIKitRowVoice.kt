package io.agora.chat.uikit.widget.chatrow

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatDownloadStatus
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatVoiceMessageBody
import io.agora.chat.uikit.common.extensions.isSend
import io.agora.chat.uikit.common.helper.ChatUIKitRowVoicePlayer
import java.io.File

open class ChatUIKitRowVoice @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean = false
) : ChatUIKitRowFile(context, attrs, defStyleAttr, isSender) {
    protected var voiceImageView: ImageView? = null
    protected var voiceLengthView: TextView? = null
    private var readStatusView: ImageView? = null
    protected var voiceAnimation: AnimationDrawable? = null

    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.uikit_row_received_voice else R.layout.uikit_row_sent_voice,
            this
        )
    }

    override fun onFindViewById() {
        voiceImageView = findViewById<View>(R.id.iv_voice) as? ImageView
        voiceLengthView = findViewById<View>(R.id.tv_length) as? TextView
        readStatusView = findViewById<View>(R.id.iv_unread_voice) as? ImageView
    }

    override fun onSetUpView() {
        message?.run {
            val voiceBody = body as ChatVoiceMessageBody
            val len: Int = voiceBody.length
            var padding = 0
            if (len > 0) {
                padding = getVoicePadding(len)
                voiceLengthView?.text = "${voiceBody.length}\""
                voiceLengthView?.visibility = VISIBLE
            } else {
                voiceLengthView?.visibility = INVISIBLE
            }
            if (!isSender) {
                voiceImageView?.setImageResource(R.drawable.uikit_chatfrom_voice_playing)
                voiceLengthView?.setPadding(padding, 0, 0, 0)
            } else {
                voiceImageView?.setImageResource(R.drawable.uikit_chatto_voice_playing)
                voiceLengthView?.setPadding(0, 0, padding, 0)
            }
            if (!isSend()) {
                val downloadStatus = voiceBody.downloadStatus()
                if (downloadStatus === ChatDownloadStatus.PENDING &&
                    ChatClient.getInstance().options.autodownloadThumbnail
                ) {
                    if (isVoiceFileExit(voiceBody)) {
                        ChatClient.getInstance().chatManager().downloadAttachment(this)
                    }
                }
                if (readStatusView != null) {
                    if (isListened) {
                        // hide the unread icon
                        readStatusView?.visibility = INVISIBLE
                    } else {
                        readStatusView?.visibility = VISIBLE
                    }
                }
                ChatLog.d(TAG, "it is receive msg")
                if (voiceBody.downloadStatus() === ChatDownloadStatus.DOWNLOADING ||
                    voiceBody.downloadStatus() === ChatDownloadStatus.PENDING
                ) {
                    if (ChatClient.getInstance().options
                            .autodownloadThumbnail && isVoiceFileExit(voiceBody)
                    ) {
                        progressBar?.visibility = VISIBLE
                    } else {
                        progressBar?.visibility = INVISIBLE
                    }
                } else {
                    progressBar?.visibility = INVISIBLE
                }
            } else {
                // hide the unread icon
                if (readStatusView != null) {
                    readStatusView?.visibility = INVISIBLE
                }
            }

            // To avoid the item is recycled by listview and slide to this item again but the animation is stopped.
            val voicePlayer: ChatUIKitRowVoicePlayer = ChatUIKitRowVoicePlayer.getInstance(context)
            if (voicePlayer.isPlaying && msgId.equals(voicePlayer.currentPlayingId)) {
                startVoicePlayAnimation()
            }
        }
    }

    /**
     * Check if the voice file exits.
     * @param voiceBody
     * @return
     */
    protected fun isVoiceFileExit(voiceBody: ChatVoiceMessageBody): Boolean {
        return File(voiceBody.localUrl).exists()
    }

    fun getVoicePadding(voiceLen: Int): Int {
        return when(voiceLen) {
            in 1..9 -> context.resources.getDimensionPixelSize(R.dimen.ease_size_3)
            in 10 .. 19 -> context.resources.getDimensionPixelSize(R.dimen.ease_size_11)
            in 20 .. 29 -> context.resources.getDimensionPixelSize(R.dimen.ease_size_31)
            in 30 .. 39 -> context.resources.getDimensionPixelSize(R.dimen.ease_size_55)
            in 40 .. 49 -> context.resources.getDimensionPixelSize(R.dimen.ease_size_80)
            in 50 .. 59 -> context.resources.getDimensionPixelSize(R.dimen.ease_size_100)
            else -> {
                if (voiceLen >= 60) {
                    context.resources.getDimensionPixelSize(R.dimen.ease_size_150)
                } else {
                    context.resources.getDimensionPixelSize(R.dimen.ease_size_0)
                }
            }
        }
    }

    protected fun onViewUpdate(msg: ChatMessage) {

        // Only the received message has the attachment download status.
        if (msg.isSend()) {
            return
        }
        val voiceBody = msg.body as ChatVoiceMessageBody
        if (voiceBody.downloadStatus() === ChatDownloadStatus.DOWNLOADING ||
            voiceBody.downloadStatus() === ChatDownloadStatus.PENDING
        ) {
            progressBar?.visibility = VISIBLE
        } else {
            progressBar?.visibility = INVISIBLE
        }
    }

    fun startVoicePlayAnimation() {
        message?.run {
            if (!isSender) {
                voiceImageView?.setImageResource(R.drawable.voice_from_icon)
            } else {
                voiceImageView?.setImageResource(R.drawable.voice_to_icon)
            }
            voiceAnimation = voiceImageView?.drawable as AnimationDrawable
            voiceAnimation?.start()

            // Hide the voice item not listened status view.
            if (!isSend()) {
                if (readStatusView != null) {
                    readStatusView?.visibility = INVISIBLE
                }
            }
        }
    }

    fun stopVoicePlayAnimation() {
        voiceAnimation?.stop()
        message?.run {
            if (!isSender) {
                voiceImageView?.setImageResource(R.drawable.uikit_chatfrom_voice_playing)
            } else {
                voiceImageView?.setImageResource(R.drawable.uikit_chatto_voice_playing)
            }
        }
    }

    companion object {
        private val TAG = ChatUIKitRowVoice::class.java.simpleName
    }
}