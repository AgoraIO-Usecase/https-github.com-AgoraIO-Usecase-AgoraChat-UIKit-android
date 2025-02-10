package io.agora.chat.uikit.widget

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.Toolbar
import java.util.Collections
import java.util.Collections.max

internal object ToolbarUtils {
    private val VIEW_TOP_COMPARATOR =
        java.util.Comparator<View> { view1, view2 -> view1.top - view2.top }

    fun getTitleTextView(toolbar: Toolbar): TextView? {
        if (TextUtils.isEmpty(toolbar.title)) {
            return null
        }
        val textViews = getTextViewsWithText(toolbar, toolbar.title)
        return if (textViews.isEmpty()) null else Collections.min(textViews, VIEW_TOP_COMPARATOR)
    }

    fun getSubtitleTextView(toolbar: Toolbar): TextView? {
        if (TextUtils.isEmpty(toolbar.subtitle)) {
            return null
        }
        val textViews = getTextViewsWithText(toolbar, toolbar.subtitle)
        return if (textViews.isEmpty()) null else max(textViews, VIEW_TOP_COMPARATOR)
    }

    private fun getTextViewsWithText(toolbar: Toolbar, text: CharSequence): List<TextView> {
        val textViews: MutableList<TextView> = ArrayList()
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is TextView) {
                val textView = child
                if (TextUtils.equals(textView.text, text)) {
                    textViews.add(textView)
                }
            }
        }
        return textViews
    }

    fun getLogoImageView(toolbar: Toolbar): ImageView? {
        return getImageView(toolbar, toolbar.logo)
    }

    private fun getImageView(toolbar: Toolbar, content: Drawable?): ImageView? {
        if (content == null) {
            return null
        }
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is ImageView) {
                val imageView = child
                val drawable = imageView.drawable
                if (drawable != null && drawable.constantState != null && drawable.constantState == content.constantState) {
                    return imageView
                }
            }
        }
        return null
    }

    fun getSecondaryActionMenuItemView(toolbar: Toolbar): View? {
        val actionMenuView = getActionMenuView(toolbar)
        if (actionMenuView != null) {
            // Only return the first child of the ActionMenuView if there is more than one child
            if (actionMenuView.childCount > 1) {
                return actionMenuView.getChildAt(0)
            }
        }
        return null
    }

    fun getActionMenuView(toolbar: Toolbar): ActionMenuView? {
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is ActionMenuView) {
                return child
            }
        }
        return null
    }

    fun getNavigationIconButton(toolbar: Toolbar): ImageButton? {
        val navigationIcon = toolbar.navigationIcon ?: return null
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is ImageButton) {
                val imageButton = child
                if (imageButton.drawable === navigationIcon) {
                    return imageButton
                }
            }
        }
        return null
    }

    @SuppressLint("RestrictedApi")
    fun getActionMenuItemView(
        toolbar: Toolbar, @IdRes menuItemId: Int
    ): ActionMenuItemView? {
        val actionMenuView = getActionMenuView(toolbar)
        if (actionMenuView != null) {
            for (i in 0 until actionMenuView.childCount) {
                val child = actionMenuView.getChildAt(i)
                if (child is ActionMenuItemView) {
                    val actionMenuItemView = child
                    if (actionMenuItemView.itemData.itemId == menuItemId) {
                        return actionMenuItemView
                    }
                }
            }
        }
        return null
    }
}