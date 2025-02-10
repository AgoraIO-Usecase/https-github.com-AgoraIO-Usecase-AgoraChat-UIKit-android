package io.agora.chat.uikit.menu

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.interfaces.OnMenuDismissListener
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * It is the helper to manage the menus in UIKit.
 */
open class ChatUIKitMenuHelper {
    private val menuItems by lazy { mutableListOf<ChatUIKitMenuItem>() }
    private var itemClickListener: OnMenuItemClickListener? = null
    private var menuView: IMenu? = null
    protected var view: View? = null
    private var context: Context? = null
    private var enableWxStyle: Boolean? = false
    private var isChat:Boolean = false

    open fun initMenu(view: View?,message: ChatMessage? = null,isChat:Boolean = false) {
        this.view = view
        this.isChat = isChat
        view?.let {
            enableWxStyle = ChatUIKitClient.getConfig()?.chatConfig?.enableWxMessageStyle
            context = it.context
            if (enableWxStyle == true && isChat){
                menuView = ChatUIKitMenuPopupWindow(context,view,message)
            }else{
                if (menuView == null) {
                    menuView = ChatUIKitMenuDialog()
                }
            }
        }
    }

    private fun addMenuItem() {
        if (menuItems.isEmpty()) {
            return
        }
        menuView?.registerMenus(menuItems)
    }

    /**
     * Add menu item.
     */
    fun addItemMenu(item: ChatUIKitMenuItem): ChatUIKitMenuHelper {
        if (!menuItems.contains(item)) {
            menuItems.add(item)
        }
        return this
    }

    /**
     * Add menu item.
     */
    fun addItemMenu(
        menuId: Int,
        order: Int,
        name: String,
        groupId: Int = 0,
        isVisible: Boolean = true,
        @DrawableRes resourceId: Int = -1,
        @ColorInt titleColor : Int = -1
    ): ChatUIKitMenuHelper {
        return addItemMenu(ChatUIKitMenuItem(menuId = menuId,
            order = order,
            title = name,
            groupId = groupId,
            isVisible = isVisible,
            resourceId = resourceId,
            titleColor = titleColor
        ))
    }

    /**
     * Clear menu items.
     */
    fun clear() {
        menuItems.clear()
        menuView?.clear()
    }

    open fun release(){
        menuView = null
    }

    /**
     * Dismiss menu.
     */
    fun dismiss() {
        menuView?.dismissMenu()
    }

    /**
     * Find the target menu.
     */
    fun findItem(menuId: Int): ChatUIKitMenuItem? {
        return menuItems.firstOrNull { it.menuId == menuId }
    }

    /**
     * Find the target menu item and set the visibility.
     */
    fun findItemVisible(menuId: Int, visible: Boolean) {
        menuItems.forEach {
            if (it.menuId == menuId) {
                it.isVisible = visible
            }
        }
    }

    /**
     * Set all menu items visibility.
     */
    fun setAllItemsVisible(visible: Boolean) {
        menuItems.forEach {
            it.isVisible = visible
        }
    }

    fun show() {
        addMenuItem()
        if (enableWxStyle == true && isChat){
            if (menuView is ChatUIKitMenuPopupWindow){
                CoroutineScope(Dispatchers.Main).launch {
                    (menuView as ChatUIKitMenuPopupWindow).show()
                }
            }
        }else{
            menuView?.let { menu ->
                if (menu is BottomSheetDialogFragment) {
                    view?.run {
                        if (context is Activity) {
                            menu.show((context as AppCompatActivity).supportFragmentManager, "EaseConvMenuHelper")
                        } else {
                            throw IllegalArgumentException("Context must be Activity")
                        }
                    } ?: throw IllegalArgumentException("View is null")
                }
            }
        }
    }

    /**
     * Set menu cancelable.
     */
    fun setDialogCancelable(cancelable: Boolean) {
        if (enableWxStyle == true && isChat){
            (menuView as? ChatUIKitMenuPopupWindow)?.isOutsideTouchable = cancelable
        }else{
            (menuView as? DialogFragment)?.isCancelable = cancelable
        }
    }

    /**
     * Set menu item click listener.
     */
    open fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
        itemClickListener = listener
        menuView?.setOnMenuItemClickListener(listener)
    }

    /**
     * Set menu dismiss listener.
     */
    open fun setOnMenuDismissListener(listener: OnMenuDismissListener?) {
        menuView?.setOnMenuDismissListener(listener)
    }

    fun getContext(): Context? {
        return context
    }

    /**
     * Set the orientation of the menu.
     * After setting the orientation, you need to call [notifyDataSetChanged] to take effect.
     * @param orientation [ChatUIKitMenuItemView.MenuOrientation]
     */
    fun setMenuOrientation(orientation: ChatUIKitMenuItemView.MenuOrientation) {
        menuView?.let {
            (it as? ChatUIKitMenuDialog)?.getMenuAdapter()?.let { adapter->
                adapter.setMenuOrientation(orientation)
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Set the gravity of the menu.
     * After setting the gravity, you need to call [notifyDataSetChanged] to take effect.
     * @param gravity [ChatUIKitMenuItemView.MenuGravity]
     */
    fun setMenuGravity(gravity: ChatUIKitMenuItemView.MenuGravity) {
        menuView?.let {
            (it as? ChatUIKitMenuDialog)?.getMenuAdapter()?.let { adapter->
                adapter.setMenuGravity(gravity)
                adapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * Set whether to show cancel button.
     */
    fun showCancel(show: Boolean) {
        menuView?.let {
            (it as? ChatUIKitMenuDialog)?.showCancel(show)
        }
    }

    /**
     * Add top view for ChatUIKitMenuDialog.
     */
    fun addTopView(view: View) {
        this.view?.context?.mainScope()?.launch {
            delay(100)
            if (enableWxStyle == true && isChat){
                menuView?.let {
                    (it as? ChatUIKitMenuPopupWindow)?.addReactionView(view)
                }
            }else{
                menuView?.let {
                    (it as? ChatUIKitMenuDialog)?.addTopView(view)
                }
            }
        }
    }

    /**
     * Clear top view for ChatUIKitMenuDialog.
     */
    fun clearTopView(){
        this.view?.context?.mainScope()?.launch {
            delay(100)
            if (enableWxStyle == true && isChat){
                menuView?.let {
                    (it as? ChatUIKitMenuPopupWindow)?.clearReactionView()
                }
            }else{
                menuView?.let {
                    (it as? ChatUIKitMenuDialog)?.clearTopView()
                }
            }
        }
    }

}