package io.agora.chat.uikit.widget.photoview

import android.annotation.TargetApi
import android.view.View

@TargetApi(16)
internal object SDK16 {
    fun postOnAnimation(view: View, r: Runnable?) {
        view.postOnAnimation(r)
    }
}