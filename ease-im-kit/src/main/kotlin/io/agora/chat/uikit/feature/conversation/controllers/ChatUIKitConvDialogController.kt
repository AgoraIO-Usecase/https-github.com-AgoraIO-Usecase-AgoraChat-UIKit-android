package io.agora.chat.uikit.feature.conversation.controllers

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.dialog.ChatUIKitNewBottomSheetFragment
import io.agora.chat.uikit.common.dialog.SimpleListSheetDialog
import io.agora.chat.uikit.common.dialog.SimpleSheetType
import io.agora.chat.uikit.feature.group.ChatUIKitCreateGroupActivity
import io.agora.chat.uikit.interfaces.SimpleListSheetItemClickListener
import io.agora.chat.uikit.model.ChatUIKitMenuItem

class ChatUIKitConvDialogController(
    private val context: Context,
    private val fragment: Fragment
) {

    fun showMoreDialog(addContactAction: (String) -> Unit){
        val context = (context as FragmentActivity)
        val mutableListOf = mutableListOf(
            ChatUIKitMenuItem(
                menuId = R.id.ease_action_new_conversation,
                title = context.getString(R.string.uikit_conv_action_new_conversation),
                resourceId = R.drawable.uikit_conv_new_chat,
                titleColor = ContextCompat.getColor(context, R.color.ease_color_primary)
            ),
            ChatUIKitMenuItem(
                menuId = R.id.ease_action_add_contact,
                title = context.getString(R.string.uikit_conv_action_add_contact),
                resourceId =  R.drawable.uikit_conv_add_contact,
                titleColor = ContextCompat.getColor(context, R.color.ease_color_primary)
            ),
            ChatUIKitMenuItem(
                menuId = R.id.ease_action_create_group,
                title = context.getString(R.string.uikit_conv_action_create_group),
                resourceId =  R.drawable.uikit_conv_new_group,
                titleColor = ContextCompat.getColor(context, R.color.ease_color_primary)
            ),
        )
        val dialog = SimpleListSheetDialog(
            context = context,
            itemList = mutableListOf,
            type = SimpleSheetType.ITEM_LAYOUT_DIRECTION_START)
        dialog.setSimpleListSheetItemClickListener(object : SimpleListSheetItemClickListener {
            override fun onItemClickListener(position: Int, menu: ChatUIKitMenuItem) {
                dialog.dismiss()
                when(menu.menuId){
                    R.id.ease_action_new_conversation -> {
                        val fragment = ChatUIKitNewBottomSheetFragment()
                        fragment.show(this@ChatUIKitConvDialogController.fragment.childFragmentManager, "createNewChat")
                    }
                    R.id.ease_action_add_contact -> {
                        showAddContactDialog(addContactAction)
                    }
                    R.id.ease_action_create_group -> {
                        ChatUIKitCreateGroupActivity.actionStart(context)
                    }
                    else -> {}
                }
            }
        })
        context.supportFragmentManager.let { dialog?.show(it,"ease_conversation_action_more") }
    }

    fun showAddContactDialog(addContactAction: (String) -> Unit){
        val context = (context as FragmentActivity)
        val contactDialog = CustomDialog(
            context = context,
            title = context.getString(R.string.uikit_conv_action_add_contact),
            subtitle = context.getString(R.string.uikit_conv_dialog_add_contact),
            inputHint = context.getString(R.string.uikit_dialog_edit_input_id_hint),
            rightButtonText = context.getString(R.string.uikit_dialog_right_text),
            isEditTextMode = true,
            onInputModeConfirmListener = {
                addContactAction.invoke(it)
            }
        )
        contactDialog.show()
    }
}