package io.agora.chat.uikit.common.player

import android.content.Context
import android.graphics.Color
import androidx.annotation.AttrRes
import java.util.Locale
import java.util.concurrent.TimeUnit

internal object Util {
    @JvmStatic
    fun getDurationString(durationMs: Long, negativePrefix: Boolean): String {
        return String.format(
            Locale.getDefault(),
            "%s%02d:%02d",
            if (negativePrefix) "-" else "",
            TimeUnit.MILLISECONDS.toMinutes(durationMs), TimeUnit.MILLISECONDS.toSeconds(durationMs)
                    - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs))
        )
    }

    @JvmStatic
    fun isColorDark(color: Int): Boolean {
        val darkness = (1
                - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))
                / 255)
        return darkness >= 0.5
    }

    @JvmStatic
    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    @JvmStatic
    fun resolveColor(context: Context, @AttrRes attr: Int): Int {
        return resolveColor(context, attr, 0)
    }

    private fun resolveColor(context: Context, @AttrRes attr: Int, fallback: Int): Int {
        val a = context.theme.obtainStyledAttributes(intArrayOf(attr))
        return try {
            a.getColor(0, fallback)
        } finally {
            a.recycle()
        }
    }
}