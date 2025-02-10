package io.agora.chat.uikit.feature.thread

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.dialog.SimpleListSheetDialog
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.databinding.UikitActivityThreadMemberLayoutBinding
import io.agora.chat.uikit.feature.thread.fragment.ChatUIKitThreadMemberFragment
import io.agora.chat.uikit.feature.thread.interfaces.OnThreadMemberItemClickListener
import io.agora.chat.uikit.interfaces.SimpleListSheetItemClickListener
import io.agora.chat.uikit.model.ChatUIKitMenuItem

open class ChatUIKitThreadMemberActivity:ChatUIKitBaseActivity<UikitActivityThreadMemberLayoutBinding>() {
    private var deleteDialog:SimpleListSheetDialog? = null

    private val fragment:ChatUIKitThreadMemberFragment by lazy{
        ChatUIKitThreadMemberFragment()
    }
    override fun getViewBinding(inflater: LayoutInflater): UikitActivityThreadMemberLayoutBinding{
        return UikitActivityThreadMemberLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = Bundle()
        intent?.let {
            val threadId = it.getStringExtra(ChatUIKitConstant.THREAD_CHAT_THREAD_ID)
            val parentId = it.getStringExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID)
            bundle.putString(ChatUIKitConstant.THREAD_CHAT_THREAD_ID,threadId)
            bundle.putString(ChatUIKitConstant.EXTRA_CONVERSATION_ID,parentId)
        }
        fragment.let {
            it.arguments = bundle
            it.setOnThreadMemberItemClickListener(object : OnThreadMemberItemClickListener{
                override fun onThreadMemberItemClick(view: View?, userId: String) {
                    if (ChatUIKitClient.getCurrentUser()?.id != userId){
                        threadMemberItemClick(view,userId)
                    }
                }
            })
            supportFragmentManager.beginTransaction().add(binding.flFragment.id, it).commit()
        }
        binding.run {
            titleBar.setTitle(getString(R.string.uikit_thread_member_title))
            titleBar.setNavigationOnClickListener { finish() }
        }
    }

    open fun threadMemberItemClick(view: View?, userId:String){
        val menu = mutableListOf(ChatUIKitMenuItem(
            menuId = R.id.thread_member_remove,
            title = getString(R.string.uikit_thread_remove_from_topic),
            titleColor = ContextCompat.getColor(this@ChatUIKitThreadMemberActivity, R.color.ease_color_error),
        ))
        deleteDialog = SimpleListSheetDialog(
            context = this@ChatUIKitThreadMemberActivity,
            itemList = menu,
            itemListener = object : SimpleListSheetItemClickListener {
                override fun onItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
                    when(menu.menuId){
                        R.id.thread_member_remove -> {
                            fragment.removeMember(userId)
                            deleteDialog?.dismiss()
                        }
                        else -> {}
                    }
                }
            })
        if (fragment.isGroupOwner()){
            supportFragmentManager.let { pm-> deleteDialog?.show(pm,"thread_member_delete_dialog") }
        }
    }

    companion object {
        fun actionStart(context: Context, conversationId:String?,threadId:String) {
            val intent = Intent(context, ChatUIKitThreadMemberActivity::class.java)
            intent.putExtra(ChatUIKitConstant.THREAD_CHAT_THREAD_ID,threadId)
            conversationId?.let {
                intent.putExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID,it)
            }
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