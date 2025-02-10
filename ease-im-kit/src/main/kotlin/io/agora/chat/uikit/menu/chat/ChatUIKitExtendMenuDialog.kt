package io.agora.chat.uikit.menu.chat

import android.content.Context
import io.agora.chat.uikit.R
import io.agora.chat.uikit.feature.chat.interfaces.ChatUIKitExtendMenuItemClickListener
import io.agora.chat.uikit.feature.chat.interfaces.IChatExtendMenu
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.menu.ChatUIKitMenuDialog
import io.agora.chat.uikit.menu.ChatUIKitMenuItemView
import io.agora.chat.uikit.model.ChatUIKitMenuItem

class ChatUIKitExtendMenuDialog(
    private val context: Context
): ChatUIKitMenuDialog(), IChatExtendMenu {

    private val itemStrings = intArrayOf(
        R.string.uikit_attach_take_pic,
        R.string.uikit_attach_picture,
        R.string.uikit_attach_video,
        R.string.uikit_attach_file,
        R.string.uikit_attach_contact_card
    )
    private val itemdrawables = intArrayOf(
        R.drawable.uikit_chat_takepic_selector, R.drawable.uikit_chat_image_selector,
        R.drawable.em_chat_video_selector, R.drawable.em_chat_file_selector,
        R.drawable.em_chat_card_selector
    )
    private val itemIds = intArrayOf(
        R.id.extend_item_take_picture,
        R.id.extend_item_picture,
        R.id.extend_item_video,
        R.id.extend_item_file,
        R.id.extend_item_contact_card
    )
    private var itemClickListener: ChatUIKitExtendMenuItemClickListener? = null

    fun init() {
        for (i in itemStrings.indices) {
            registerMenuItem(itemStrings[i], itemdrawables[i], itemIds[i], i * 100)
        }

        getMenuAdapter().let {
            it.setMenuGravity(ChatUIKitMenuItemView.MenuGravity.LEFT)
            it.setMenuOrientation(ChatUIKitMenuItemView.MenuOrientation.HORIZONTAL)
            it.notifyDataSetChanged()
        }

        setOnMenuItemClickListener(object : OnMenuItemClickListener {

            override fun onMenuItemClick(item: ChatUIKitMenuItem?, position: Int): Boolean {
                item?.let {
                    itemClickListener?.onChatExtendMenuItemClick(it.menuId, null)
                    return true
                }
                return false
            }
        })
    }
    override fun registerMenuItem(name: String?, drawableRes: Int, itemId: Int, order: Int,titleColor:Int,resourceTintColor:Int) {
        registerMenuItem(title = name ?: "", resourceId = drawableRes, menuId = itemId, order = order)
    }

    override fun registerMenuItem(nameRes: Int, drawableRes: Int, itemId: Int, order: Int,titleColor:Int,resourceTintColor:Int) {
        registerMenuItem(title = context.getString(nameRes), resourceId = drawableRes, menuId = itemId, order = order)
    }

    override fun setEaseChatExtendMenuItemClickListener(listener: ChatUIKitExtendMenuItemClickListener?) {
        itemClickListener = listener
    }
}