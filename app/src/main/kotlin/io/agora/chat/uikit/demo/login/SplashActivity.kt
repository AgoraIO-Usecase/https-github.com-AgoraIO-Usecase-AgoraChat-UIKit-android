package io.agora.chat.uikit.demo.login

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.demo.MainActivity
import io.agora.chat.uikit.demo.R
import io.agora.chat.uikit.demo.base.BaseInitActivity
import io.agora.chat.uikit.demo.databinding.DemoSplashActivityBinding
import io.agora.chat.uikit.demo.viewmodel.SplashViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SplashActivity : BaseInitActivity<DemoSplashActivityBinding>() {
    private var ivSplash: ImageView? = null
    private var ivProduct: ImageView? = null
    private lateinit var model: SplashViewModel

    override fun getViewBinding(inflater: LayoutInflater): DemoSplashActivityBinding? {
        return DemoSplashActivityBinding.inflate(inflater)
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(this, R.color.transparent), true)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        ivSplash = findViewById(R.id.iv_splash)
        ivProduct = findViewById(R.id.iv_product)
    }

    override fun initData() {
        super.initData()
        model = ViewModelProvider(this)[SplashViewModel::class.java]
        ivSplash!!.animate()
            .alpha(1f)
            .setDuration(500)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    checkIfAgreePrivacy()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            .start()
        ivProduct!!.animate()
            .alpha(1f)
            .setDuration(500)
            .start()
    }

    private fun checkIfAgreePrivacy() {
        loginSDK()
    }

    private fun loginSDK() {
        lifecycleScope.launch {
            model.loginData()
                .catchChatException { e ->
                    ChatLog.e("TAG", "error message = " + e.description)
                    LoginActivity.startAction(mContext)
                    finish()
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), false)
                .collect {
                    if (it) {
                        startActivity(Intent(mContext, MainActivity::class.java))
                        finish()
                    }
                }
        }
    }
}