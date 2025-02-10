package io.agora.chat.uikit.common.player

interface IUIKitVideoCallback {
    fun onStarted(player: UIKitVideoPlayer?)
    fun onPaused(player: UIKitVideoPlayer?)
    fun onPreparing(player: UIKitVideoPlayer?)
    fun onPrepared(player: UIKitVideoPlayer?)
    fun onBuffering(percent: Int)
    fun onError(player: UIKitVideoPlayer?, e: Exception?)
    fun onCompletion(player: UIKitVideoPlayer?)
    fun onClickVideoFrame(player: UIKitVideoPlayer?)
}