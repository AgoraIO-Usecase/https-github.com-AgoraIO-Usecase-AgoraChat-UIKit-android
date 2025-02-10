package io.agora.chat.uikit.feature.chat.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.databinding.UikitActivityShowLocalVideoBinding
import io.agora.chat.uikit.common.player.IUIKitVideoCallback
import io.agora.chat.uikit.common.player.UIKitVideoPlayer

class ChatUIKitShowLocalVideoActivity : ChatUIKitBaseActivity<UikitActivityShowLocalVideoBinding>(),
    IUIKitVideoCallback {
    private var evpPlayer: UIKitVideoPlayer? = null
    private var uri: Uri? = null
    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        initIntent(intent)
        initView()
        initListener()
        initData()
    }

    override fun getViewBinding(inflater: LayoutInflater): UikitActivityShowLocalVideoBinding {
        return UikitActivityShowLocalVideoBinding.inflate(inflater)
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(mContext, R.color.transparent), false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initIntent(intent)
        if (uri != null) {
            evpPlayer?.setSource(uri!!)
        }
    }

    private fun initIntent(intent: Intent) {
        val path: String? = intent.getStringExtra("path")
        val u: Uri? = intent.getParcelableExtra("uri")
        if (u != null) {
            uri = u
        } else if (!TextUtils.isEmpty(path)) {
            uri = Uri.parse(path)
        }
        if (uri == null) {
            finish()
        }
    }

    fun initView() {
        evpPlayer = findViewById(R.id.evp_player)
    }

    fun initListener() {
        evpPlayer?.setCallback(this)
    }

    fun initData() {
        evpPlayer?.setAutoPlay(true)
        if (uri != null) {
            evpPlayer?.setSource(uri!!)
        }
    }

    override fun onPause() {
        super.onPause()
        evpPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (evpPlayer != null) {
            evpPlayer?.release()
            evpPlayer = null
        }
    }

    override fun onStarted(player: UIKitVideoPlayer?) {}
    override fun onPaused(player: UIKitVideoPlayer?) {}
    override fun onPreparing(player: UIKitVideoPlayer?) {}
    override fun onPrepared(player: UIKitVideoPlayer?) {}
    override fun onBuffering(percent: Int) {}
    override fun onError(player: UIKitVideoPlayer?, e: Exception?) {}
    override fun onCompletion(player: UIKitVideoPlayer?) {
        finish()
    }

    override fun onClickVideoFrame(player: UIKitVideoPlayer?) {}

    companion object {
        fun actionStart(context: Context, uri: Uri?) {
            val intent = Intent(context, ChatUIKitShowLocalVideoActivity::class.java)
            intent.putExtra("uri", uri)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    context.startActivity(it)
                    return
                }
            }
            context.startActivity(intent)
        }
    }
}