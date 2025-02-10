package io.agora.chat.uikit.widget.photoview

import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.View

internal object Compat {
    private const val SIXTY_FPS_INTERVAL = 1000 / 60
    fun postOnAnimation(view: View, runnable: Runnable?) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            SDK16.postOnAnimation(view, runnable)
        } else {
            view.postDelayed(runnable, SIXTY_FPS_INTERVAL.toLong())
        }
    }
}