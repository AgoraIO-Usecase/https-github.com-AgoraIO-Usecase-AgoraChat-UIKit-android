package io.agora.chat.uikit.common.helper

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatVoiceMessageBody
import java.io.IOException

/**
 * Created by zhangsong on 17-10-20.
 */
class ChatUIKitRowVoicePlayer private constructor(cxt: Context) {
    private val audioManager: AudioManager
    private val mediaPlayer: MediaPlayer

    /**
     * May null, please consider.
     *
     * @return
     */
    var currentPlayingId: String? = null
        private set
    private var onCompletionListener: OnCompletionListener? = null
    private val baseContext: Context
    val player: MediaPlayer
        get() = mediaPlayer
    val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    fun play(msg: ChatMessage, listener: OnCompletionListener?) {
        if (msg.body !is ChatVoiceMessageBody) return
        if (mediaPlayer.isPlaying) {
            stop()
        }
        currentPlayingId = msg.msgId
        onCompletionListener = listener
        try {
            setSpeaker()
            val voiceBody = msg.body as ChatVoiceMessageBody
            mediaPlayer.setDataSource(baseContext, voiceBody.localUri)
            mediaPlayer.prepare()
            mediaPlayer.setOnCompletionListener {
                stop()
                currentPlayingId = null
                onCompletionListener = null
            }
            mediaPlayer.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun play(voiceFilePath: String?) {
        if (voiceFilePath == null) {
            return
        }
        if (mediaPlayer.isPlaying) {
            stop()
        }
        try {
            setSpeaker()
            mediaPlayer.setDataSource(voiceFilePath)
            mediaPlayer.prepare()
            mediaPlayer.setOnCompletionListener {
                stop()
                currentPlayingId = null
                onCompletionListener = null
            }
            mediaPlayer.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        mediaPlayer.stop()
        mediaPlayer.reset()
        /**
         * This listener is to stop the voice play animation currently, considered the following 3 conditions:
         *
         * 1.A new voice item is clicked to play, to stop the previous playing voice item animation.
         * 2.The voice is play complete, to stop it's voice play animation.
         * 3.Press the voice record button will stop the voice play and must stop voice play animation.
         *
         */
        onCompletionListener?.onCompletion(mediaPlayer)
    }

    fun release() {
        onCompletionListener = null
    }

    init {
        baseContext = cxt.applicationContext
        audioManager = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mediaPlayer = MediaPlayer()
    }

    private fun setSpeaker() {
        val speakerOn: Boolean = ChatUIKitClient.getSettingsProvider()?.isSpeakerOpened ?: false
        if (speakerOn) {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = true
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING)
        } else {
            audioManager.isSpeakerphoneOn = false // Turn off speaker
            audioManager.mode = AudioManager.MODE_IN_CALL
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL)
        }
    }

    companion object {
        private val TAG = ChatUIKitRowVoicePlayer::class.java.simpleName
        private var instance: ChatUIKitRowVoicePlayer? = null
        fun getInstance(context: Context): ChatUIKitRowVoicePlayer {
            if (instance == null) {
                synchronized(ChatUIKitRowVoicePlayer::class.java) {
                    if (instance == null) {
                        instance = ChatUIKitRowVoicePlayer(context.applicationContext)
                    }
                }
            }
            return instance!!
        }
    }
}