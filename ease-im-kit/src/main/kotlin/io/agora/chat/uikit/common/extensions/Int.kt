package io.agora.chat.uikit.common.extensions

import android.content.Context
import android.util.TypedValue
import io.agora.chat.uikit.R

fun Int.dpToPx(context: Context) = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    context.resources.displayMetrics
).toInt()

/**
 * Expression to get the message unread value exceeding the maximum value.
 */
internal fun Int.maxUnreadCount(context: Context): String {
    return if (this <= 99) {
        this.toString()
    } else {
        context.getString(R.string.uikit_message_unread_count_max)
    }
}