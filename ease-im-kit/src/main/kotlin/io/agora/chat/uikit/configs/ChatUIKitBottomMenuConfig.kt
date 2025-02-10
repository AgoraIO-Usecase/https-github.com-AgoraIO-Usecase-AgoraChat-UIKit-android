package io.agora.chat.uikit.configs

import android.content.Context
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import java.util.Collections

class ChatUIKitBottomMenuConfig (
    private val context: Context,
    private var contactBottomSheetModels: MutableList<ChatUIKitMenuItem>? = null,
    private var groupBottomSheetModels: MutableList<ChatUIKitMenuItem>? = null,
    private var groupOwnerBottomSheetModels: MutableList<ChatUIKitMenuItem>? = null
){

    init {
        if (contactBottomSheetModels == null){
            contactBottomSheetModels = mutableListOf()
            for (i in defaultContactBottomSheetItemString.indices) {
                registerContactMenuItem(
                    nameRes = defaultContactBottomSheetItemString[i],
                    drawableRes = defaultContactBottomSheetItemDrawables[i],
                    itemId = defaultContactBottomSheetItemIds[i],
                    itemColor = defaultContactBottomSheetItemColor[i],
                    itemVisible = defaultContactBottomSheetItemVisible[i],
                    order = defaultContactBottomSheetItemOrder[i]
                )
            }
        }

        if (groupBottomSheetModels == null){
            groupBottomSheetModels = mutableListOf()
            for (i in defaultGroupBottomSheetItemString.indices) {
                registerGroupMenuItem(
                    nameRes = defaultGroupBottomSheetItemString[i],
                    drawableRes = defaultGroupBottomSheetItemDrawables[i],
                    itemId = defaultGroupBottomSheetItemIds[i],
                    itemColor = defaultGroupBottomSheetItemColor[i],
                    itemVisible = defaultGroupBottomSheetItemVisible[i],
                    order = defaultGroupBottomSheetItemOrder[i]
                )
            }
        }

        if (groupOwnerBottomSheetModels == null){
            groupOwnerBottomSheetModels = mutableListOf()
            for (i in defaultGroupOwnerBottomSheetItemString.indices) {
                registerGroupOwnerMenuItem(
                    nameRes = defaultGroupOwnerBottomSheetItemString[i],
                    drawableRes = defaultGroupOwnerBottomSheetItemDrawables[i],
                    itemId = defaultGroupOwnerBottomSheetItemIds[i],
                    itemColor = defaultGroupOwnerBottomSheetItemColor[i],
                    itemVisible = defaultGroupOwnerBottomSheetItemVisible[i],
                    order = defaultGroupOwnerBottomSheetItemOrder[i]
                )
            }
        }

    }

    fun getDefaultContactBottomSheetMenu(): MutableList<ChatUIKitMenuItem>?{
        return contactBottomSheetModels
    }

    fun getDefaultGroupBottomSheetMenu(): MutableList<ChatUIKitMenuItem>?{
        return groupBottomSheetModels
    }

    fun getDefaultGroupOwnerBottomSheetMenu(): MutableList<ChatUIKitMenuItem>?{
        return groupOwnerBottomSheetModels
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
        contactBottomSheetModels?.let {
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
        groupBottomSheetModels?.let {
            it.add(item)
            sortByOrder(it)
        }
    }

    private fun registerGroupOwnerMenuItem(nameRes: Int, drawableRes: Int, itemId: Int, itemColor: Int, itemVisible: Boolean, order: Int = 0) {
        registerGroupOwnerMenuItem(context.getString(nameRes), drawableRes, itemId, itemColor, itemVisible, order)
    }

    private fun registerGroupOwnerMenuItem(name: String?, drawableRes: Int, itemId: Int, itemColor: Int, itemVisible: Boolean, order: Int) {
        val item = ChatUIKitMenuItem(
            title = name ?: "",
            resourceId = drawableRes,
            menuId = itemId,
            titleColor = ContextCompat.getColor(context, itemColor),
            isVisible = itemVisible,
            order = order
        )
        groupOwnerBottomSheetModels?.let {
            it.add(item)
            sortByOrder(it)
        }
    }


    private fun sortByOrder(itemModels: List<ChatUIKitMenuItem>) {
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

val defaultContactBottomSheetItemDrawables = intArrayOf(-1)

val defaultContactBottomSheetItemIds = intArrayOf(R.id.bottom_sheet_item_remove_contact)

val defaultContactBottomSheetItemString = intArrayOf(R.string.uikit_delete_contact)

val defaultContactBottomSheetItemColor = intArrayOf(R.color.ease_color_error)

val defaultContactBottomSheetItemVisible = booleanArrayOf(true)

val defaultContactBottomSheetItemOrder = intArrayOf( 0 )


val defaultGroupBottomSheetItemDrawables = intArrayOf(-1)

val defaultGroupBottomSheetItemIds = intArrayOf(R.id.bottom_sheet_item_leave_group)

val defaultGroupBottomSheetItemString = intArrayOf(R.string.uikit_group_detail_leave)

val defaultGroupBottomSheetItemColor = intArrayOf(R.color.ease_color_error)

val defaultGroupBottomSheetItemVisible = booleanArrayOf(true)

val defaultGroupBottomSheetItemOrder = intArrayOf( 0 )


val defaultGroupOwnerBottomSheetItemDrawables = intArrayOf(-1,-1)

val defaultGroupOwnerBottomSheetItemIds = intArrayOf(
    R.id.bottom_sheet_item_change_owner,
    R.id.bottom_sheet_item_destroy_group,
)

val defaultGroupOwnerBottomSheetItemString = intArrayOf(
    R.string.uikit_group_detail_change_owner,
    R.string.uikit_group_detail_destroy
)

val defaultGroupOwnerBottomSheetItemColor = intArrayOf(
    R.color.ease_color_primary,
    R.color.ease_color_error
)

val defaultGroupOwnerBottomSheetItemVisible = booleanArrayOf(true,true)

val defaultGroupOwnerBottomSheetItemOrder = intArrayOf( 0,0 )