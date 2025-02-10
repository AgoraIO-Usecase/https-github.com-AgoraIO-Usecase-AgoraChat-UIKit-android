package io.agora.chat.uikit.demo.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.demo.R
import io.agora.chat.uikit.demo.base.BaseInitActivity
import io.agora.chat.uikit.demo.databinding.DemoActivityLoginBinding

class LoginActivity : BaseInitActivity<DemoActivityLoginBinding>() {

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(R.id.fl_fragment, LoginFragment())
            .commit()
    }

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityLoginBinding? {
        return DemoActivityLoginBinding.inflate(inflater)
    }

    override fun setActivityTheme() {
        setFitSystemForTheme(false, ContextCompat.getColor(this, R.color.transparent), true)
    }

    companion object {
        fun startAction(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }
}