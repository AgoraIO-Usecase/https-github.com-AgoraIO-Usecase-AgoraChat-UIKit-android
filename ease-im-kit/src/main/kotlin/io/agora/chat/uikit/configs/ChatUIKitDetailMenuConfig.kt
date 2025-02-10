package io.agora.chat.uikit.configs

import android.content.Context
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import java.util.Collections

class ChatUIKitDetailMenuConfig(
    val context:Context,
    private var contactItemModels: MutableList<ChatUIKitMenuItem>? = null,
    private var groupItemModels: MutableList<ChatUIKitMenuItem>? = null,
) {
    init {
        if (contactItemModels == null){
            contactItemModels = mutableListOf()
            for (i in defaultContactItemString.indices) {
                registerContactMenuItem(
                    defaultContactItemString[i],
                    defaultContactItemDrawables[i],
                    defaultContactItemIds[i],
                    defaultContactItemColor[i],
                    defaultContactItemVisible[i],
                    defaultContactItemOrder[i]
                )
            }
        }

        if (groupItemModels == null){
            groupItemModels = mutableListOf()
            for (i in defaultGroupItemString.indices) {
                registerGroupMenuItem(
                    defaultGroupItemString[i],
                    defaultGroupItemDrawables[i],
                    defaultGroupItemIds[i],
                    defaultGroupItemColor[i],
                    defaultGroupItemVisible[i],
                    defaultGroupItemOrder[i]
                )
            }
        }

    }

    fun getDefaultContactDetailMenu(): MutableList<ChatUIKitMenuItem>?{
        return contactItemModels
    }

    fun getDefaultGroupDetailMenu(): MutableList<ChatUIKitMenuItem>?{
        return groupItemModels
    }

    private fun registerContactMenuItem(nameRes: Int, drawableRes: Int, itemId: Int, itemColor: Int, itemVisible: Boolean, order: Int = 0) {
        registerContactMenuItem(context.getString(nameRes), drawableRes, itemId, itemColor, itemVisible, order)
    }

    private fun registerContactMenuItem(name: String?, drawableRes: Int, itemId: Int, itemColor: Int, itemVisible: Boolean, order: Int) {
        val item = ChatUIKitMenuItem(
            title = name ?: "",
            resourceId = drawableRes,
            menuId = itemId,
            titleColor = ContextCompat.getColor(context, itemColor),
            isVisible = itemVisible,
            order = order
        )
        contactItemModels?.let {
            it.add(item)
            sortByOrder(it)
        }

    }

    private fun registerGroupMenuItem(nameRes: Int, drawableRes: Int, itemId: Int, itemColor: Int, itemVisible: Boolean, order: Int = 0) {
        registerGroupMenuItem(context.getString(nameRes), drawableRes, itemId, itemColor, itemVisible, order)
    }

    private fun registerGroupMenuItem(name: String?, drawableRes: Int, itemId: Int, itemColor: Int, itemVisible: Boolean, order: Int) {
        val item = ChatUIKitMenuItem(
            title = name ?: "",
            resourceId = drawableRes,
            menuId = itemId,
            titleColor = ContextCompat.getColor(context, itemColor),
            isVisible = itemVisible,
            order = order
        )
        groupItemModels?.let {
            it.add(item)
            sortByOrder(it)
        }
    }

    fun sortByOrder(itemModels: List<ChatUIKitMenuItem>) {
        Collections.sort(itemModels) { o1, o2 ->
            val `val` = o1.order - o2.order
            if (`val` > 0) {
                1
            } else if (`val` == 0) {
                0
            } else {
                -1
            }
        }
    }

}

val defaultGroupItemIds = intArrayOf(
    R.id.extend_item_message,
    R.id.extend_item_search,
)

val defaultGroupItemString = intArrayOf(
    R.string.uikit_detail_item_message,R.string.uikit_detail_item_search_msg,
)

val defaultGroupItemDrawables = intArrayOf(
    R.drawable.uikit_bubble_msg,R.drawable.uikit_search_msg,
)

val defaultGroupItemColor = intArrayOf(
    R.color.ease_group_detail_custom_layout_item_title_color,
    R.color.ease_group_detail_custom_layout_item_title_color,
)

val defaultGroupItemVisible = booleanArrayOf(
    true,true
)

val defaultGroupItemOrder = intArrayOf(
    1,20
)

val defaultContactItemIds = intArrayOf(
    R.id.extend_item_message,
    R.id.extend_item_search,
)

val defaultContactItemString = intArrayOf(
    R.string.uikit_detail_item_message, R.string.uikit_detail_item_search_msg
)

val defaultContactItemDrawables = intArrayOf(
    R.drawable.uikit_bubble_msg, R.drawable.uikit_search_msg,
)

val defaultContactItemColor = intArrayOf(
    R.color.ease_group_detail_custom_layout_item_title_color,
    R.color.ease_group_detail_custom_layout_item_title_color,
)

val defaultContactItemVisible = booleanArrayOf(
    true,true
)

val defaultContactItemOrder = intArrayOf(
    1,20
)

