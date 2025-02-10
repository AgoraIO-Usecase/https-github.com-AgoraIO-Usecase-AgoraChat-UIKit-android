package io.agora.chat.uikit.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.dpToPx

class ChatUIKitSearchView @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    private var iconTint: Int? = null
    private lateinit var searchIcon: ImageView
    private lateinit var searchText: TextView
    private lateinit var searchRoot: LinearLayout

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.uikit_layout_search, this, true)
        searchIcon = findViewById(R.id.iv_search_icon)
        searchText = findViewById(R.id.tv_search_text)
        searchRoot = findViewById(R.id.search_root)
        if (attrs != null) {
            val ta: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitSearchView)

            ta.getDrawable(R.styleable.ChatUIKitSearchView_search_drawable_icon)?.let { setIcon(it) }
            ta.getColor(R.styleable.ChatUIKitSearchView_search_drawable_icon_tint, -1).let {
                if (it != -1) {
                    setIconTint(it)
                }
            }
            ta.getString(R.styleable.ChatUIKitSearchView_search_text_hint)?.let { setHint(it) }
            ta.getResourceId(R.styleable.ChatUIKitSearchView_search_text_hint, -1).let {
                if (it != -1) {
                    setHint(it)
                }
            }
            ta.getString(R.styleable.ChatUIKitSearchView_search_text_hint)?.let { setHint(it) }
            ta.getColor(R.styleable.ChatUIKitSearchView_search_text_color, -1).let {
                if (it != -1) {
                    setTextColor(it)
                }
            }
            ta.getResourceId(R.styleable.ChatUIKitSearchView_search_text_color, -1).let {
                if (it != -1) {
                    setTextColor(ContextCompat.getColor(context, it))
                }
            }
            ta.getDimension(R.styleable.ChatUIKitSearchView_search_text_size, -1f).let {
                if (it != -1f) {
                    setTextSize(it)
                }
            }
            ta.getResourceId(R.styleable.ChatUIKitSearchView_search_text_size, -1).let {
                if (it != -1) {
                    setTextSize(it)
                }
            }
            ta.getString(R.styleable.ChatUIKitSearchView_search_text)?.let { setText(it) }
            ta.getResourceId(R.styleable.ChatUIKitSearchView_search_text, -1).let {
                if (it != -1) {
                    setText(it)
                }
            }
            ta.getInteger(R.styleable.ChatUIKitSearchView_search_gravity, -1).let {
                if (it != -1) {
                    setGravity(it)
                }
            }
            ta.getDimension(R.styleable.ChatUIKitSearchView_search_drawable_padding, -1f).let {
                if (it != -1f) {
                    setDrawablePadding(it.toInt())
                }
            }
            ta.getResourceId(R.styleable.ChatUIKitSearchView_search_drawable_padding, 4).let {
                if (it != -1) {
                    setDrawablePadding(it.dpToPx(context))
                }
            }

            ta.recycle()
        }
    }

    /**
     * Set the icon to use for the search button.
     */
    fun setIcon(@DrawableRes resId: Int) {
        setIcon(AppCompatResources.getDrawable(getContext(), resId))
    }

    /**
     * Set the icon to use for the search button.
     */
    fun setIcon(icon: Drawable?) {
        searchIcon.setImageDrawable(maybeTintIcon(icon))
    }

    /**
     * Set the tint to apply to the search icon.
     */
    fun setIconTint(@ColorInt iconTint: Int) {
        this.iconTint = iconTint
        val iconDrawable = searchIcon.drawable
        iconDrawable?.let { setIcon(it) }
    }

    /**
     * Set the hint resource to display in the query text field.
     */
    fun setHint(@StringRes resId: Int) {
        setHint(context.getString(resId))
    }

    /**
     * Set the hint text to display in the query text field.
     */
    fun setHint(hint: CharSequence?) {
        searchText.hint = hint
    }

    /**
     * Set the text color to display in the query text field.
     */
    fun setTextColor(@ColorInt color: Int) {
        searchText.setTextColor(color)
    }

    /**
     * Set the text size resource to display in the query text field.
     */
    fun setTextSize(@DimenRes sizeRes: Int) {
        setTextSize(context.resources.getDimension(sizeRes))
    }

    /**
     * Set the text size to display in the query text field.
     */
    fun setTextSize(size: Float) {
        searchText.textSize = size
    }

    /**
     * Set the text resource to display in the query text field.
     */
    fun setText(@StringRes resId: Int) {
        setText(context.getString(resId))
    }

    /**
     * Set the text to display in the query text field.
     */
    fun setText(text: CharSequence?) {
        searchText.text = text
    }

    /**
     * Set the gravity for the search view.
     */
    fun setGravity(gravity: Int) {
        searchRoot.gravity = gravity
        requestLayout()
    }

    /**
     * Set the padding for the search icon.
     */
    fun setDrawablePadding(padding: Int) {
        val marginStart = searchIcon.marginStart
        val marginTop = searchIcon.marginTop
        val marginBottom = searchIcon.marginBottom
        (searchIcon.layoutParams as LinearLayout.LayoutParams).let {
            it.marginStart = marginStart
            it.topMargin = marginTop
            it.bottomMargin = marginBottom
            it.marginEnd = padding
        }
    }

    private fun maybeTintIcon(icon: Drawable?): Drawable? {
        return if (icon != null && iconTint != null) {
            val wrappedNavigationIcon = DrawableCompat.wrap(icon.mutate())
            DrawableCompat.setTint(
                wrappedNavigationIcon,
                iconTint!!
            )
            wrappedNavigationIcon
        } else {
            icon
        }
    }

}