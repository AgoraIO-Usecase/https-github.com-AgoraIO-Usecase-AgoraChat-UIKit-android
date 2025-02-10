package io.agora.chat.uikit.feature.chat.internal

import android.text.SpannableStringBuilder

internal fun SpannableStringBuilder.setTargetSpan(what: Any, start: Int, end: Int, flags: Int): SpannableStringBuilder {
    setSpan(what, start, end, flags)
    return this
}