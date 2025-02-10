package io.agora.chat.uikit.demo

import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.dialog.SimpleListSheetDialog
import io.agora.chat.uikit.common.extensions.isAdmin
import io.agora.chat.uikit.common.extensions.isOwner
import io.agora.chat.uikit.feature.group.ChatUIKitGroupDetailEditActivity
import io.agora.chat.uikit.feature.group.EditType
import io.agora.chat.uikit.feature.thread.ChatUIKitThreadMemberActivity
import io.agora.chat.uikit.feature.thread.fragment.ChatUIKitThreadFragment
import io.agora.chat.uikit.interfaces.SimpleListSheetItemClickListener
import io.agora.chat.uikit.model.ChatUIKitMenuItem

class ChatThreadFragment:ChatUIKitThreadFragment() {
    private var moreDialog:SimpleListSheetDialog? = null

    companion object{
        private val TAG = ChatThreadFragment::class.java.simpleName
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.run {
            titleBar.inflateMenu(R.menu.demo_thread_action_more)
            titleBar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_more -> {
                        showDialog()
                    }
                }
                true
            }
        }
    }

     override fun showDialog(){
        context?.let {
            val menu = mutableListOf(
                ChatUIKitMenuItem(
                    order = 0,
                    menuId = R.id.thread_more_edit,
                    title = resources.getString(R.string.thread_edit),
                    resourceId = R.drawable.icon_thread_edit,
                    titleColor = ContextCompat.getColor(mContext, R.color.em_color_on_background),
                    isVisible = (parentInfo?.isOwner() == true || parentInfo?.isAdmin() == true || mThread?.isOwner() == true)
                ),
                ChatUIKitMenuItem(
                    order = 1*10,
                    menuId = R.id.thread_more_member,
                    title = resources.getString(R.string.thread_member),
                    resourceId = R.drawable.icon_thread_member,
                    titleColor = ContextCompat.getColor(mContext, R.color.em_color_on_background)
                ),
                ChatUIKitMenuItem(
                    order = 2*10,
                    menuId = R.id.thread_more_leave,
                    title =  resources.getString(R.string.thread_leave),
                    resourceId = R.drawable.icon_thread_leave,
                    titleColor = ContextCompat.getColor(mContext, R.color.em_color_on_background),
                ),
                ChatUIKitMenuItem(
                    order = 3*10,
                    menuId = R.id.thread_more_destroy,
                    title =  resources.getString(R.string.thread_destroy) ,
                    resourceId = R.drawable.icon_thread_destroy ,
                    titleColor = ContextCompat.getColor(mContext, R.color.em_color_error),
                    isVisible = (parentInfo?.isOwner() == true || parentInfo?.isAdmin() == true)
                )
            )
            moreDialog = SimpleListSheetDialog(
                context = it,
                itemList = menu,
                itemListener = object : SimpleListSheetItemClickListener {
                    override fun onItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
                        when(menu.menuId){
                            R.id.thread_more_edit -> {
                                startActivity(ChatUIKitGroupDetailEditActivity.createIntent(
                                    context = mContext,
                                    groupId = mThread?.parentId,
                                    type = EditType.ACTION_EDIT_THREAD_NAME,
                                    threadName = mThread?.chatThreadName,
                                    threadId = mThread?.chatThreadId
                                ))
                                moreDialog?.dismiss()
                            }
                            R.id.thread_more_member -> {
                                chatThreadId?.let { threadId ->
                                    ChatUIKitThreadMemberActivity.actionStart(mContext,parentId,threadId)
                                }
                                moreDialog?.dismiss()
                            }
                            R.id.thread_more_leave -> {
                                leaveChatThread()
                            }
                            R.id.thread_more_destroy -> {
                                showDestroyChatThreadDialog()
                            }
                            else -> {}
                        }
                    }
                })
            parentFragmentManager.let { pm-> moreDialog?.show(pm,"thread_more_dialog") }
        }

    }

    override fun showDestroyChatThreadDialog() {
        val clearDialog = CustomDialog(
            context = mContext,
            title = mContext.resources.getString(io.agora.chat.uikit.R.string.uikit_thread_delete_topic_title),
            subtitle = mContext.resources.getString(io.agora.chat.uikit.R.string.uikit_thread_delete_topic_subtitle),
            isEditTextMode = false,
            onLeftButtonClickListener = {

            },
            onRightButtonClickListener = {
                destroyChatThread()
            }
        )
        clearDialog.show()
    }

    override fun leaveChatThreadSuccess() {
        super.leaveChatThreadSuccess()
        Log.e(TAG,"leaveChatThreadSuccess")
    }

    override fun leaveChatThreadFail(code: Int, message: String?) {
        super.leaveChatThreadFail(code, message)
        Log.e(TAG,"leaveChatThreadFail $code $message")
    }

    override fun destroyChatThreadSuccess() {
        super.destroyChatThreadSuccess()
        Log.e(TAG,"destroyChatThreadSuccess")
    }

    override fun destroyChatThreadFail(code: Int, message: String?) {
        super.destroyChatThreadFail(code, message)
        Log.e(TAG,"destroyChatThreadFail $code $message")
    }
}