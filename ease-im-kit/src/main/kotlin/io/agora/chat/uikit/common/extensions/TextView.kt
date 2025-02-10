package io.agora.chat.uikit.common.extensions

import android.widget.TextView
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.model.ChatUIKitProfile

/**
 * Set nickname from ChatUIKitProfile.
 * @param profile
 */
internal fun TextView.loadNickname(profile: ChatUIKitProfile?) {
    if (profile == null) return
    text = if (profile.getRemarkOrName().isNullOrEmpty()) profile.id else profile.getRemarkOrName()
}

/**
 * Set nickname from message ext.
 */
internal fun TextView.loadNickname(message: ChatMessage?) {
    if (message == null) return
    loadNickname(message.getUserInfo())
}

internal fun TextView.getTextHeight(): Int {
    return paint.fontMetricsInt.bottom - paint.fontMetricsInt.top
}