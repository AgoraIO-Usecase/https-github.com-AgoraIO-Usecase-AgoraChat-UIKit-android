package io.agora.chat.uikit.common.helper

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.text.format.Time
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatPathUtils
import io.agora.chat.uikit.common.ChatUIKitError
import java.io.File
import java.io.IOException
import java.util.Date

class ChatUIKitVoiceRecorder(
    private val context: Context,
    private val handler: Handler
) {
    private var recorder: MediaRecorder? = null
    var isRecording = false
        private set
    private var startTime: Long = 0
    var voiceFilePath: String? = null
        private set
    private var voiceFileName: String? = null
    private var file: File? = null
    private var errorListener: ChatUIKitVoiceRecorderErrorListener? = null

    /**
     * start recording to the file
     */
    fun startRecording(appContext: Context?, conversationId: String?): String? {
        file = null
        try {
            // need to create recorder every time, otherwise, will got exception
            // from setOutputFile when try to reuse
            if (recorder != null) {
                recorder?.release()
                recorder = null
            }
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            MediaRecorder(context)
                        } else {
                            MediaRecorder()
                        }
            recorder?.run {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setAudioChannels(1) // MONO
                setAudioSamplingRate(8000) // 8000Hz
                setAudioEncodingBitRate(64) // seems if change this to
            }
            // 128, still got same file
            // size.
            // one easy way is to use temp file
            // file = File.createTempFile(PREFIX + userId, EXTENSION,
            // User.getVoicePath());
            voiceFileName = getVoiceFileName(ChatClient.getInstance().currentUser)
            voiceFilePath = getVoiceFilePath(conversationId)
            voiceFilePath?.let {
                file = File(it)
                recorder?.run {
                    setOutputFile(file?.absolutePath)
                    prepare()
                    isRecording = true
                    start()
                }
            }
        } catch (e: IOException) {
            ChatLog.e("voice", "prepare() failed ${e.message}")
            errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR, e.message)
        }
        Thread {
            try {
                while (isRecording) {
                    val msg = Message()
                    msg.what = recorder!!.maxAmplitude * 13 / 0x7FFF
                    handler.sendMessage(msg)
                    SystemClock.sleep(100)
                }
            } catch (e: Exception) {
                // from the crash report website, found one NPE crash from
                // one android 4.0.4 htc phone
                // maybe handler is null for some reason
                ChatLog.e("voice", e.toString())
                errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR, e.message)
            }
        }.start()
        startTime = Date().time
        ChatLog.d("voice", "start voice recording to file:" + file?.absolutePath)
        return if (file == null) null else file?.absolutePath
    }

    private fun getVoiceFilePath(conversationId: String?): String {
        return if (conversationId == null) {
            ChatPathUtils.getInstance().voicePath.toString() + "/" + voiceFileName
        } else {
            val conversation = ChatClient.getInstance().chatManager().getConversation(conversationId)
            if (conversation != null) {
                if (conversation.messageAttachmentPath.isNotEmpty()){
                    val file = File(conversation.messageAttachmentPath)
                    if (!file.exists()){
                        file.mkdirs()
                    }
                }
                conversation.messageAttachmentPath + "/" + voiceFileName
            } else {
                ChatPathUtils.getInstance().voicePath.toString() + "/" + voiceFileName
            }
        }
    }

    /**
     * stop the recoding
     *
     * @return seconds of the voice recorded
     */
    fun discardRecording() {
        recorder?.run {
            try {
                stop()
                release()
                recorder = null
                file?.let {
                    if (it.exists() && !it.isDirectory) {
                        it.delete()
                    }
                }
            } catch (e: IllegalStateException) {
                errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR, e.message)
            } catch (e: RuntimeException) {
                errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR, e.message)
            }
            isRecording = false
        }
    }

    fun deleteRecordingFile() {
        file?.let {
            if (it.exists() && !it.isDirectory) {
                it.delete()
            }
        }
    }

    fun stopRecoding(): Int {
        recorder?.run {
            try {
                isRecording = false
                stop()
                release()
                recorder = null
                file?.run {
                    if (!exists() || !isFile) {
                        errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR,"file not exist")
                    } else if (length() == 0L) {
                        delete()
                        errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR,"file length is 0")
                    } else {}
                } ?: {
                    errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR,"file not exist")
                }
                val seconds = (Date().time - startTime) / 1000
                ChatLog.d(
                    "voice",
                    "voice recording finished. seconds:" + seconds + " file length:" + file?.length()
                )
                return seconds.toInt()
            } catch (e: IllegalStateException) {
                errorListener?.onError(ChatUIKitError.UIKIT_RECORD_ERROR, e.message)
            }
        }
        return 0
    }

    fun release() {
        if (isRecording) {
            stopRecoding()
        } else {
            recorder?.release()
        }
    }

    private fun getVoiceFileName(uid: String): String {
        val now = Time()
        now.setToNow()
        return uid + now.toString().substring(0, 15) + EXTENSION
    }

    fun setVoiceRecorderErrorListener(listener: ChatUIKitVoiceRecorderErrorListener?) {
        this.errorListener = listener
    }

    interface ChatUIKitVoiceRecorderErrorListener {
        fun onError(error: Int, message: String?)
    }

    companion object {
        const val PREFIX = "voice"
        const val EXTENSION = ".amr"
    }
}