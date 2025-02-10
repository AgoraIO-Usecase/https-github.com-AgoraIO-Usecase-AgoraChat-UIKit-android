package io.agora.chat.uikit.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.dpToPx

class ChatUIKitMenuItemView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private var root: View? = null
    private var orientation: MenuOrientation? = null
    private var gravity: MenuGravity? = null
    init {
        initAttrs(context, attrs, defStyleAttr)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitMenuItemView, defStyleAttr, 0)
        orientation = MenuOrientation.values()[a.getInt(R.styleable.ChatUIKitMenuItemView_menuOrientation, 0)]
        setMenuOrientation(orientation)
        gravity = MenuGravity.values()[a.getInt(R.styleable.ChatUIKitMenuItemView_contentGravity, 0)]
        setMenuGravity(gravity)
    }


    /**
     * Set menu orientation.
     * @param orientation [MenuOrientation]
     */
    fun setMenuOrientation(orientation: MenuOrientation?) {
        if (orientation == null) return
        root?.let {
            this@ChatUIKitMenuItemView.removeView(root)
        }
        when(orientation) {
            MenuOrientation.HORIZONTAL -> {
                root = LayoutInflater.from(context).inflate(R.layout.uikit_chat_menu_item_horizontal, this, false)
                this@ChatUIKitMenuItemView.addView(root)
            }
            MenuOrientation.VERTICAL -> {
                root = LayoutInflater.from(context).inflate(R.layout.uikit_chat_menu_item, this, false)
                this@ChatUIKitMenuItemView.addView(root)
            }
        }
        setMenuGravity(gravity)
    }

    /**
     * Set menu gravity.
     * @param gravity [MenuGravity]
     */
    fun setMenuGravity(gravity: MenuGravity?) {
        if (gravity == null) return
        when(gravity) {
            MenuGravity.LEFT -> {
                if (root is ConstraintLayout) {
                    if (orientation == MenuOrientation.HORIZONTAL) {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(root as ConstraintLayout)
                        constraintSet.clear(R.id.text, ConstraintSet.END)
                        constraintSet.clear(R.id.image, ConstraintSet.END)
                        constraintSet.setMargin(R.id.image, ConstraintSet.START, 16.dpToPx(context))
                        constraintSet.setMargin(R.id.text, ConstraintSet.START, 4.dpToPx(context))
                        constraintSet.applyTo(root as ConstraintLayout)
                    } else {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(root as ConstraintLayout)
                        constraintSet.clear(R.id.image, ConstraintSet.END)
                        constraintSet.setMargin(R.id.image, ConstraintSet.START, 16.dpToPx(context))
                        constraintSet.applyTo(root as ConstraintLayout)
                    }

                }
            }
            MenuGravity.RIGHT -> {
                if (root is ConstraintLayout) {
                    if (orientation == MenuOrientation.HORIZONTAL) {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(root as ConstraintLayout)
                        constraintSet.clear(R.id.image, ConstraintSet.START)
                        constraintSet.clear(R.id.text, ConstraintSet.START)
                        constraintSet.setMargin(R.id.text, ConstraintSet.END, 16.dpToPx(context))
                        constraintSet.applyTo(root as ConstraintLayout)
                    } else {
                        val constraintSet = ConstraintSet()
                        constraintSet.clone(root as ConstraintLayout)
                        constraintSet.clear(R.id.image, ConstraintSet.START)
                        constraintSet.setMargin(R.id.image, ConstraintSet.END, 16.dpToPx(context))
                        constraintSet.applyTo(root as ConstraintLayout)
                    }
                }
            }
            MenuGravity.CENTER -> {
                // do nothing
            }
        }
    }

    /**
     * Set whether to hide the divider.
     * @param hideDivider true to hide the divider, false otherwise.
     */
    fun hideDivider(hideDivider: Boolean) {
        if (hideDivider) {
            root?.findViewById<View>(R.id.divider)?.visibility = View.GONE
        } else {
            root?.findViewById<View>(R.id.divider)?.visibility = View.VISIBLE
        }
    }


    enum class MenuOrientation {
        HORIZONTAL, VERTICAL
    }

    enum class MenuGravity {
        CENTER, LEFT, RIGHT
    }

}