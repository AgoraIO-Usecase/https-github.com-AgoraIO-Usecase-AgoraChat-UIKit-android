package io.agora.chat.uikit.common.dialog

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import io.agora.chat.uikit.R

interface ChatUIKitBottomSheetChildHelper {
    @get:StringRes
    val titleBarTitle: Int
        //more details by user choose
        get() = R.string.uikit_cancel

    @get:StringRes
    val titleBarRightText: Int
        get() = R.string.uikit_cancel

    @get:ColorRes
    val titleBarRightTextColor: Int
        get() = R.color.ease_color_primary

    fun onTitleBarRightTextViewClick(): Boolean {
        return false
    }

    val isShowTitleBarLeftLayout: Boolean
        get() = false

    fun getParentFragment(): Fragment?

    fun startFragment(fragment: Fragment?, tag: String?) {
        val parentFragment = getParentFragment()
        if (parentFragment != null && parentFragment is ChatUIKitBottomSheetContainerHelper) {
            (parentFragment as ChatUIKitBottomSheetContainerHelper).startFragment(fragment, tag)
        }
    }

    fun hide() {
        val parentFragment = getParentFragment()
        if (parentFragment != null && parentFragment is ChatUIKitBottomSheetContainerHelper) {
            (parentFragment as ChatUIKitBottomSheetContainerHelper).hide()
        }
    }

    fun back() {
        val parentFragment = getParentFragment()
        if (parentFragment != null && parentFragment is ChatUIKitBottomSheetContainerHelper) {
            (parentFragment as ChatUIKitBottomSheetContainerHelper).back()
        }
    }

    fun isChangeColor(isChange: Boolean) {
        val parentFragment = getParentFragment()
        if (parentFragment != null && parentFragment is ChatUIKitBottomSheetContainerHelper) {
            (parentFragment as ChatUIKitBottomSheetContainerHelper).changeNextColor(isChange)
        }
    }
}