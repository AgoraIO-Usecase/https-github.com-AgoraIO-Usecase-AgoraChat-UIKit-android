package io.agora.chat.uikit.feature.chat.controllers

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.feature.chat.forward.ChatUIKitMessageForwardDialogFragment
import io.agora.chat.uikit.feature.chat.forward.helper.ChatUIKitMessageMultiSelectHelper
import io.agora.chat.uikit.feature.chat.forward.widgets.ChatUIKitMultipleSelectMenuView
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitLayout
import io.agora.chat.uikit.interfaces.OnForwardClickListener
import io.agora.chat.uikit.interfaces.OnMenuDismissListener
import io.agora.chat.uikit.interfaces.OnMultiSelectMenuListener
import io.agora.chat.uikit.viewmodel.messages.IChatViewRequest
import io.agora.util.EMLog

class ChatUIKitMessageMultipleSelectController(
    private val mContext: Context,
    private val chatLayout: ChatUIKitLayout,
    private val viewModel: IChatViewRequest?,
    private val conversationId: String
) {

    private val visibleMenuList = mutableListOf<Int>()
    private var isCanAutoScrollToBottom = true
    private var isNotificationViewVisible = false
    var isInMultipleSelectStyle = false

    // Show multiple select style.
    fun showMultipleSelectStyle(message: ChatMessage?) {
        ChatUIKitMultipleSelectMenuView(
            conversationId = conversationId,
            context = mContext
        ).apply {
            // Record the status of auto scroll status for chat list.
            isCanAutoScrollToBottom = chatLayout.chatMessageListLayout?.isCanAutoScrollToBottom ?: true
            // Set the chat list auto scroll status to true.
            chatLayout.chatMessageListLayout?.isCanAutoScrollToBottom = false
            // Record the visible status of notification view.
            isNotificationViewVisible = chatLayout.chatNotificationController.isNotificationViewShow()
            // dismiss the notification view.
            chatLayout.chatNotificationController.showNotificationView(false)

            isInMultipleSelectStyle = true

            setSelectedMessage(message)
            chatLayout.chatInputMenu?.let {
                it.setCustomTopExtendMenu(this)
                addEnterAnimation(this)
                it.showTopExtendMenu(true)
                it.hideInputMenu()
            }
            chatLayout.chatMessageListLayout?.notifyDataSetChanged()
            chatLayout.chatMessageListLayout?.isNeedScrollToBottomWhenViewChange(false)

            setOnMultiSelectMenuListener(object : OnMultiSelectMenuListener {

                override fun onDeleteClick(messageIdList: List<String>) {
                    if (messageIdList.isNotEmpty()){
                        val deleteDialog = CustomDialog(
                            context = mContext,
                            title = if (messageIdList.size > 1)
                                context.resources.getString(R.string.uikit_chat_dialog_delete_multi_message_title, messageIdList.size)
                            else context.resources.getString(R.string.uikit_chat_dialog_delete_one_message_title),
                            isEditTextMode = false,
                            onRightButtonClickListener = {
                                viewModel?.deleteMessages(messageIdList)
                            }
                        )
                        deleteDialog.show()
                    }
                }

                override fun onForwardClick(messageIdList: List<String>) {
                    ChatLog.e("multipleSelectController", "onForwardClick: $messageIdList")
                    if (messageIdList.isNotEmpty()){
                        ChatUIKitMessageForwardDialogFragment().apply {
                            setOnForwardClickListener(object : OnForwardClickListener {
                                override fun onForwardClick(view: View?, id: String, chatType: ChatType) {
                                    viewModel?.sendCombineMessage(id, chatType, messageIdList)
                                }
                            })
                            val mContext = this@ChatUIKitMessageMultipleSelectController.mContext
                            if (mContext is AppCompatActivity) {
                                show(mContext.supportFragmentManager, "ease_message_combine_forward_dialog")
                            }
                        }
                    }
                }
            })

            setOnMenuDismissListener(object : OnMenuDismissListener {

                override fun onDismiss() {
                    chatLayout.chatInputMenu?.showPrimaryMenu(true)
                    chatLayout.chatMessageListLayout?.notifyDataSetChanged()
                    chatLayout.chatMessageListLayout?.isNeedScrollToBottomWhenViewChange(true)
                }
            })
        }
    }

    private fun addEnterAnimation(view: View) {
        val animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0F,
            Animation.RELATIVE_TO_SELF, 0F,
            Animation.RELATIVE_TO_SELF, 1F,
            Animation.RELATIVE_TO_SELF, 0F
        )
        animation.duration = 500
        animation.fillAfter = true
        view.startAnimation(animation)
    }

    fun makeToolBarMultipleSelectStyle(toolbar: Toolbar?, cancelMultiSelectStyle: () -> Unit = {}) {
        toolbar?.let { tb ->
            makeAllMenuVisible(tb, false)
            val item = tb.menu.findItem(R.id.action_chat_multi_select_cancel)
            if (item == null) {
                tb.inflateMenu(R.menu.uikit_menu_multiple_select)
            }
            tb.menu.findItem(R.id.action_chat_multi_select_cancel)?.let {
                it.isVisible = true
                it.title?.let { tl ->
                    val spannable = SpannableString(it.toString())
                    spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.ease_color_primary))
                        , 0, tl.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    it.title = spannable
                }
            }
            tb.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when(it.itemId) {
                    R.id.action_chat_multi_select_cancel -> {
                        cancelMultiSelectStyle(tb, cancelMultiSelectStyle)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    /**
     * Clear the cache data in [ChatUIKitMessageMultiSelectHelper].
     */
    fun clearSelectedMessages() {
        ChatUIKitMessageMultiSelectHelper.getInstance().clearMessages(mContext, conversationId)
    }

    /**
     * Cancel the multiple select style.
     */
    fun cancelMultiSelectStyle(toolbar: Toolbar?, cancelMultiSelectStyle: () -> Unit = {}) {
        toolbar?.let { tb ->
            tb.menu.findItem(R.id.action_chat_multi_select_cancel)?.isVisible = false
            makeAllMenuVisible(tb, true)
            ChatUIKitMessageMultiSelectHelper.getInstance().setMultiStyle(mContext, conversationId, false)

            chatLayout.chatMessageListLayout?.post {
                // Reset the chat list auto scroll status.
                chatLayout.chatMessageListLayout?.isCanAutoScrollToBottom = isCanAutoScrollToBottom
                // Reset the visible status for the notification view.
                chatLayout.chatNotificationController.showNotificationView(isNotificationViewVisible)
                chatLayout.chatNotificationController.updateNotificationView()

                isInMultipleSelectStyle = false
            }
            cancelMultiSelectStyle.invoke()
        }
    }

    private fun makeAllMenuVisible(toolbar: Toolbar, visible: Boolean) {
        if (visible) {
            if (visibleMenuList.isNotEmpty()) {
                for (id in visibleMenuList) {
                    toolbar.menu.findItem(id)?.isVisible = true
                }
                visibleMenuList.clear()
            }
        } else {
            val size = toolbar.menu.size()
            for (i in 0 until size) {
                val item = toolbar.menu.getItem(i)
                if (item.isVisible) {
                    visibleMenuList.add(item.itemId)
                    item.isVisible = false
                }
            }
        }

    }
}