package io.agora.chat.uikit.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

/**
 * Menu item bean.
 * @param menuId the id of menu item.
 * @param order the order of menu item, user can sort menu items by order.
 * @param title the title of menu item.
 * @param groupId the group id of menu item, default is 0.
 * @param isVisible the visibility of menu item, default is true.
 * @param resourceId the resource id of menu item icon, default is -1.
 * @param titleColor the color of menu item title, default is -1.
 */
data class ChatUIKitMenuItem(
    val menuId: Int,
    var title: String?,
    var order: Int = 0,
    val groupId: Int = 0,
    var isVisible: Boolean = true,
    @DrawableRes var resourceId: Int = -1,
    @ColorInt var titleColor : Int = -1,
    @ColorInt var resourceTintColor: Int = -1
)