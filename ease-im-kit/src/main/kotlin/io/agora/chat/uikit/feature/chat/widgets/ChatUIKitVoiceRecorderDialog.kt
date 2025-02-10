package io.agora.chat.uikit.feature.chat.widgets

import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseSheetFragmentDialog
import io.agora.chat.uikit.common.extensions.keepScreenOn
import io.agora.chat.uikit.databinding.UikitChatVoiceRecorderBinding
import io.agora.chat.uikit.feature.chat.interfaces.OnChatRecordTouchListener
import io.agora.chat.uikit.common.helper.ChatUIKitRowVoicePlayer
import io.agora.chat.uikit.common.helper.ChatUIKitTimerHelper
import io.agora.chat.uikit.common.helper.ChatUIKitVoiceRecorder
import io.agora.chat.uikit.interfaces.OnVoiceRecorderClickListener

class ChatUIKitVoiceRecorderDialog(
    private val context: Context,
    private val conversationId: String?,
    private val maxRecordSeconds: Int = 60
): ChatUIKitBaseSheetFragmentDialog<UikitChatVoiceRecorderBinding>() {
    private var recordTouchListener: OnChatRecordTouchListener? = null
    private var dismissListener: OnDismissListener? = null
    private var listener: OnVoiceRecorderClickListener? = null
    private var recordStatus = RecordStatus.RECORD_IDLE
    private val micImageHandler by lazy {
        val handlerThread = HandlerThread("mic-change")
        handlerThread.start()
        object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {

                val volume = msg.arg1
                if (volume > 0) {
                    //binding?.voiceWave?.setVolume(volume)
                }
            }
        }
    }
    private val voiceRecorder by lazy { ChatUIKitVoiceRecorder(context, micImageHandler) }
    private val voicePlayer by lazy { ChatUIKitRowVoicePlayer.getInstance(context) }
    private val timer by lazy { ChatUIKitTimerHelper(targetSeconds = maxRecordSeconds) }
    // The record seconds.
    private var recordSeconds: Int = 0
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitChatVoiceRecorderBinding? {
        return UikitChatVoiceRecorderBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecordIdleStatus()
        initListener()
    }

    override fun initListener() {
        binding?.run {
            layoutActionVoiceRecord.setOnClickListener {
                when (recordStatus) {
                    RecordStatus.RECORD_IDLE -> {
                        if (recordTouchListener?.onRecordTouch(null, null) == true) {
                            return@setOnClickListener
                        }
                        toStartRecordStatus()
                    }
                    RecordStatus.RECORDING -> {
                        toStopRecordStatus()
                    }
                    RecordStatus.RECORD_STOP -> {
                        toStartPlayStatus()
                    }
                    RecordStatus.RECORD_PLAYING -> {
                        toStopPlayStatus()
                    }
                    RecordStatus.RECORD_ERROR -> {
                        toIdleStatus()
                    }
                    RecordStatus.RECORD_DONE -> {
                        toStartPlayStatus()
                    }
                }
            }

            ibActionVoiceDelete.setOnClickListener {
                toIdleStatus()
            }

            ibActionVoiceSend.setOnClickListener {
                setRecordIdleStatus()
                dismiss()
                listener?.onClick(voiceRecorder.voiceFilePath, recordSeconds)
            }
        }
        timer.setOnTimerListener(object : ChatUIKitTimerHelper.OnTimerListener {

            override fun onTimer(seconds: Int, delayMillis: Long) {
                if (recordStatus == RecordStatus.RECORDING) {
                    recordSeconds = seconds
                }
                binding?.let {
                    // Switch to main thread.
                    it.root.post {
                        it.tvVoiceLength.text = context.getString(R.string.uikit_chat_record_voice_time, seconds)
                        if (maxRecordSeconds - seconds <= 10) {
                            it.tvVoiceRemainTip.text = context.getString(R.string.uikit_chat_record_voice_remain, maxRecordSeconds - seconds)
                        }
                    }
                }
            }

            override fun onFinish() {
                binding?.let {
                    it.root.post {
                        if (recordSeconds < maxRecordSeconds) {
                            when(recordStatus) {
                                RecordStatus.RECORDING -> {
                                    toStopRecordStatus()
                                }
                                RecordStatus.RECORD_PLAYING -> {
                                    toStopPlayStatus()
                                }
                                else -> {
                                    toIdleStatus()
                                }
                            }
                        } else {
                            when(recordStatus) {
                                RecordStatus.RECORDING -> {
                                    toDoneRecordStatus()
                                }
                                RecordStatus.RECORD_PLAYING -> {
                                    toStopPlayStatus()
                                }
                                else -> {
                                    toIdleStatus()
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    private fun toStartRecordStatus() {
        setRecordingStatus()
        startRecord()
        timer.startTimer()
    }

    private fun toStopRecordStatus() {
        setRecordStopStatus()
        stopRecord()
        timer.stopTimer()
        timer.resetTimer()
    }

    private fun toDoneRecordStatus() {
        setRecordDoneStatus()
        stopRecord()
        timer.stopTimer()
        timer.resetTimer()
    }

    private fun toStartPlayStatus() {
        setRecordPlayingStatus()
        startPlay()
        timer.setTargetSeconds(recordSeconds)
        timer.startTimer()
    }

    private fun toStopPlayStatus() {
        setPlayerStopStatus()
        stopPlay()
        timer.resetTimer()
    }

    private fun toIdleStatus() {
        setRecordIdleStatus()
        discardRecord()
        // Rest the timer target seconds.
        timer.setTargetSeconds(maxRecordSeconds)
        timer.resetTimer()
    }

    private fun toErrorStatus() {
        setRecordErrorStatus()
        timer.resetTimer()
    }

    private fun setViewsGone() {
        binding?.run {
            ibActionVoiceDelete.visibility = View.GONE
            ibActionVoiceSend.visibility = View.GONE
            tvVoiceTip.visibility = View.GONE
            tvVoiceRemainTip.visibility = View.GONE
            tvVoiceLength.visibility = View.GONE
            layoutActionVoiceRecord.setImageDrawable(null)
        }
    }

    private fun setRecordIdleStatus() {
        recordStatus = RecordStatus.RECORD_IDLE
        isCancelable = true
        setViewsGone()
        binding?.run {
            layoutActionVoiceRecord.setImageResource(R.drawable.uikit_chat_voice_icon)
            tvVoiceTip.visibility = View.VISIBLE
            tvVoiceTip.text= context.getString(R.string.uikit_chat_record_voice_record)
        }
    }

    private fun setRecordingStatus() {
        recordStatus = RecordStatus.RECORDING
        isCancelable = false
        setViewsGone()
        binding?.run {
            tvVoiceRemainTip.visibility = View.VISIBLE
            tvVoiceRemainTip.text = ""
            tvVoiceLength.visibility = View.VISIBLE
            tvVoiceLength.text = context.getString(R.string.uikit_chat_record_voice_time, 0)
            tvVoiceTip.visibility = View.VISIBLE
            tvVoiceTip.text = context.getString(R.string.uikit_chat_record_voice_recording)
            voiceWave.startPlay()
        }
    }

    private fun setRecordStopStatus() {
        recordStatus = RecordStatus.RECORD_STOP
        isCancelable = true
        setViewsGone()
        binding?.run {
            ibActionVoiceDelete.visibility = View.VISIBLE
            ibActionVoiceSend.visibility = View.VISIBLE
            tvVoiceLength.visibility = View.VISIBLE
            tvVoiceTip.visibility = View.VISIBLE
            tvVoiceTip.text = context.getString(R.string.uikit_chat_record_voice_play)
            voiceWave.stopPlay()
        }
    }

    private fun setRecordPlayingStatus() {
        recordStatus = RecordStatus.RECORD_PLAYING
        isCancelable = false
        setViewsGone()
        binding?.run {
            ibActionVoiceDelete.visibility = View.VISIBLE
            ibActionVoiceSend.visibility = View.VISIBLE
            tvVoiceLength.visibility = View.VISIBLE
            tvVoiceLength.text = context.getString(R.string.uikit_chat_record_voice_time, 0)
            tvVoiceTip.visibility = View.VISIBLE
            tvVoiceTip.text = context.getString(R.string.uikit_chat_record_voice_playing)
            voiceWave.startPlay()
        }
    }

    private fun setPlayerStopStatus() {
        setRecordStopStatus()
        binding?.run {
            tvVoiceLength.visibility = View.VISIBLE
            tvVoiceLength.text = context.getString(R.string.uikit_chat_record_voice_time, recordSeconds)
        }
    }

    private fun setRecordErrorStatus() {
        recordStatus = RecordStatus.RECORD_ERROR
        isCancelable = true
    }

    private fun setRecordDoneStatus() {
        recordStatus = RecordStatus.RECORD_DONE
        setRecordStopStatus()
    }

    /**
     * Start to record.
     */
    private fun startRecord() {
        // Close the voice player.
        val voicePlayer = ChatUIKitRowVoicePlayer.getInstance(context)
        if (voicePlayer.isPlaying) voicePlayer.stop()

        // Keep the screen on.
        context.keepScreenOn()
        // Start to record.
        voiceRecorder.startRecording(context, conversationId)
    }

    /**
     * Discard the record.
     */
    private fun discardRecord() {
        voiceRecorder.deleteRecordingFile()
    }

    /**
     * Stop to record.
     */
    private fun stopRecord() {
        // Stop to record.
        val length = voiceRecorder.stopRecoding()
        // Stop to keep the screen on.
        context.keepScreenOn(false)
        // Check the record length.
        if (length > 0) {
            // Set the record done status.
            setRecordDoneStatus()
        } else {
            // Set the record error status.
            setRecordErrorStatus()
        }
    }

    /**
     * Start to play the voice.
     */
    private fun startPlay() {
        voicePlayer.play(voiceRecorder.voiceFilePath)
    }

    /**
     * Stop to play the voice.
     */
    private fun stopPlay() {
        voicePlayer.stop()
    }

    private fun release() {
        voiceRecorder.release()
        voicePlayer.release()
    }

    override fun dismiss() {
        super.dismiss()
        release()
        dismissListener?.onDismiss(null)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        release()
        dismissListener?.onDismiss(dialog)
    }

    /**
     * Set the voice recorder click listener.
     */
    fun setOnVoiceRecorderClickListener(listener: OnVoiceRecorderClickListener) {
        this.listener = listener
    }

    fun setOnRecordTouchListener(listener: OnChatRecordTouchListener?) {
        this.recordTouchListener = listener
    }

    /**
     * Set the dialog dismiss listener.
     */
    fun setOnDismissListener(listener: OnDismissListener?) {
        this.dismissListener = listener
    }

    private enum class RecordStatus {
        /**
         * Recorder is idle.
         */
        RECORD_IDLE,

        /**
         * Recording.
         */
        RECORDING,

        /**
         * Stop recording.
         */
        RECORD_STOP,

        /**
         * Playing the voice.
         */
        RECORD_PLAYING,

        /**
         * Record occurs error.
         */
        RECORD_ERROR,

        /**
         * Record is done.
         */
        RECORD_DONE
    }
}