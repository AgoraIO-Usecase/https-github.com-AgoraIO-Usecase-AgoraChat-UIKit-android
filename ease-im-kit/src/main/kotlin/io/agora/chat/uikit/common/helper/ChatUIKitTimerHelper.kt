package io.agora.chat.uikit.common.helper

import android.os.Handler
import android.os.HandlerThread

/**
 * This is a simple timer class.
 * The default interval is 1000ms.
 * You can change the interval by passing the delayMillis parameter.
 */
class ChatUIKitTimerHelper(
    /**
     * The target seconds of the timer.
     */
    private var targetSeconds: Int = 60,
    /**
     * The delay millis of the timer.
     */
    private val delayMillis: Long = 1000,
    /**
     * The name of the timer.
     */
    private val name: String = "ease_timer"
) {
    private var timerStatus = TimerStatus.TIMER_IDLE
    private var seconds = 0
    private var onTimerListener: OnTimerListener? = null
    private val handlerThread by lazy { HandlerThread(name) }
    private val timerHandler: Handler by lazy {
        handlerThread.start()
        object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: android.os.Message) {
                if (msg.what == SEND_INTERVAL) {
                    seconds++
                    onTimerListener?.onTimer(seconds, delayMillis)
                    if (seconds >= targetSeconds) {
                        onTimerListener?.onFinish()
                        timerHandler.sendEmptyMessage(SEND_STOP)
                        return
                    }
                    if (timerStatus == TimerStatus.TIMER_RUNNING) {
                        timerHandler.sendEmptyMessageDelayed(SEND_INTERVAL, delayMillis)
                    }
                } else if (msg.what == SEND_STOP) {
                    resetTimer()
                }
            }
        }
    }

    /**
     * Start the timer.
     */
    fun startTimer() {
        if (timerStatus == TimerStatus.TIMER_RUNNING) {
            return
        }
        timerStatus = TimerStatus.TIMER_RUNNING
        timerHandler.sendEmptyMessageDelayed(SEND_INTERVAL, delayMillis)
    }

    /**
     * Stop the timer.
     * @return The seconds of the timer.
     */
    fun stopTimer(): Int {
        if (timerStatus != TimerStatus.TIMER_RUNNING) {
            return seconds
        }
        timerStatus = TimerStatus.TIMER_STOP
        timerHandler.removeMessages(SEND_INTERVAL)
        return seconds
    }

    /**
     * Reset the timer.
     */
    fun resetTimer() {
        if (timerStatus == TimerStatus.TIMER_RUNNING) {
            stopTimer()
        }
        seconds = 0
        timerStatus = TimerStatus.TIMER_IDLE
    }

    /**
     * Release the timer.
     */
    fun release() {
        timerHandler.removeCallbacksAndMessages(null)
        handlerThread.quit()
    }

    /**
     * Whether the timer is running.
     */
    fun isTimerRunning(): Boolean {
        return timerStatus == TimerStatus.TIMER_RUNNING
    }

    /**
     * Whether the timer is stop.
     */
    fun isTimerStop(): Boolean {
        return timerStatus == TimerStatus.TIMER_STOP
    }

    /**
     * Set the target seconds of the timer.
     */
    fun setTargetSeconds(seconds: Int) {
        targetSeconds = seconds
    }

    /**
     * Set the timer listener.
     */
    fun setOnTimerListener(listener: OnTimerListener) {
        onTimerListener = listener
    }

    interface OnTimerListener {
        /**
         * The timer callback.
         * @param seconds The seconds of the timer.
         * @param delayMillis The delay millis of the timer.
         */
        fun onTimer(seconds: Int, delayMillis: Long)

        /**
         * The timer finish callback.
         */
        fun onFinish()
    }

    enum class TimerStatus {
        TIMER_IDLE,
        TIMER_RUNNING,
        TIMER_STOP
    }

    companion object {
        private const val SEND_INTERVAL = 1000
        private const val SEND_STOP = 1001
    }
}