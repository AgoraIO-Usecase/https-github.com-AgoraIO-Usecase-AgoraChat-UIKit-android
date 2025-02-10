package io.agora.chat.uikit.feature.contact

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.databinding.UikitActivityBlockListBinding

open class ChatUIKitBlockListActivity: ChatUIKitBaseActivity<UikitActivityBlockListBinding>() {
    private var fragment: ChatUIKitBlockListFragment? = null
    override fun getViewBinding(inflater: LayoutInflater): UikitActivityBlockListBinding {
       return UikitActivityBlockListBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = ChatUIKitBlockListFragment.Builder()
            .useTitleBar(true)
            .useSearchBar(true)
            .enableTitleBarPressBack(true)
            .setEmptyLayout(R.layout.uikit_layout_no_data_show_nothing)
        setChildSettings(builder)
        fragment = builder.build()
        fragment?.let { fragment ->
            supportFragmentManager.beginTransaction().replace(binding.flFragment.id, fragment, getFragmentTag()).commit()
        }
    }

    protected open fun setChildSettings(builder: ChatUIKitBlockListFragment.Builder) {}

    protected open fun getFragmentTag(): String {
        return "ease_block_fragment"
    }

    companion object {
        fun actionStart(context: Context) {
            Intent(context, ChatUIKitBlockListActivity::class.java).apply {
                ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(this.clone() as Intent)?.let {
                    if (it.hasRoute()) {
                        context.startActivity(it)
                        return
                    }
                }
                context.startActivity(this)
            }
        }
    }
}