package io.agora.chat.uikit.common.player

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntRange

@Suppress("unused")
internal interface IUIKitUserMethods {
    fun setSource(source: Uri)
    fun setCallback(callback: IUIKitVideoCallback)
    fun setProgressCallback(callback: IUIKitVideoProgressCallback)
    fun setPlayDrawable(drawable: Drawable?)
    fun setPlayDrawableRes(@DrawableRes res: Int)
    fun setPauseDrawable(drawable: Drawable?)
    fun setPauseDrawableRes(@DrawableRes res: Int)
    fun setThemeColor(@ColorInt color: Int)
    fun setThemeColorRes(@ColorRes colorRes: Int)
    fun setHideControlsOnPlay(hide: Boolean)
    fun setAutoPlay(autoPlay: Boolean)
    fun setInitialPosition(@IntRange(from = 0, to = Int.MAX_VALUE.toLong()) pos: Int)
    fun showControls()
    fun hideControls()

    @get:CheckResult
    val isControlsShown: Boolean
    fun toggleControls()
    fun enableControls(andShow: Boolean)
    fun disableControls()

    @get:CheckResult
    val isPrepared: Boolean

    @get:CheckResult
    val isPlaying: Boolean

    @get:CheckResult
    val currentPosition: Int

    @get:CheckResult
    val duration: Int
    fun start()
    fun seekTo(@IntRange(from = 0, to = Int.MAX_VALUE.toLong()) pos: Int)
    fun setVolume(
        @FloatRange(from = 0.0, to = 1.0) leftVolume: Float,
        @FloatRange(from = 0.0, to = 1.0) rightVolume: Float
    )

    fun pause()
    fun stop()
    fun reset()
    fun release()
    fun setAutoFullscreen(autoFullScreen: Boolean)
    fun setLoop(loop: Boolean)
}