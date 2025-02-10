package io.agora.chat.uikit.feature.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.databinding.UikitLayoutGroupListBinding
import io.agora.chat.uikit.feature.group.fragments.ChatUIKitGroupListFragment

class ChatUIKitGroupListActivity:ChatUIKitBaseActivity<UikitLayoutGroupListBinding>() {
    override fun getViewBinding(inflater: LayoutInflater): UikitLayoutGroupListBinding {
        return UikitLayoutGroupListBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ChatUIKitGroupListFragment().let {
            supportFragmentManager.beginTransaction().add(binding.root.id, it).commit()
        }

    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, ChatUIKitGroupListActivity::class.java)
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