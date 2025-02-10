package io.agora.chat.uikit.feature.chat.interfaces

import android.view.MotionEvent
import android.view.View

interface OnChatRecordTouchListener {
    /**
     * Record pressing events
     * @param v
     * @param event
     * @return
     */
    fun onRecordTouch(v: View?, event: MotionEvent?): Boolean
}