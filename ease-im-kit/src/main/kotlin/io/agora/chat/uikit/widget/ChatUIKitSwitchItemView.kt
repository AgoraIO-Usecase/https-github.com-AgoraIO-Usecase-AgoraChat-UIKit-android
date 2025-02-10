package io.agora.chat.uikit.widget

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R

class ChatUIKitSwitchItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    var tvTitle: TextView? = null
        private set
    var tvHint: TextView? = null
        private set
    private var viewDivider: View? = null
    private var title: String? = null
    private val content: String? = null
    private var titleColor = 0
    private val contentColor = 0
    private var titleSize = 0f
    private val contentSize = 0f
    private var hint: String? = null
    private var root: View? = null
    var switch: SwitchCompat? = null
        private set
    private var listener: OnCheckedChangeListener? = null
    var avatar: ChatUIKitImageView? = null
        private set

    init {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        root = LayoutInflater.from(context).inflate(R.layout.uikit_layout_item_switch, this)
        avatar = findViewById(R.id.avatar)
        tvTitle = findViewById<TextView>(R.id.tv_title)
        tvHint = findViewById<TextView>(R.id.tv_hint)
        viewDivider = findViewById<View>(R.id.view_divider)
        switch = findViewById<SwitchCompat>(R.id.switch_item)
        val a = context.obtainStyledAttributes(attrs, R.styleable.SwitchItemView)
        val titleResourceId = a.getResourceId(R.styleable.SwitchItemView_switchItemTitle, -1)
        title = a.getString(R.styleable.SwitchItemView_switchItemTitle)
        if (titleResourceId != -1) {
            title = getContext().getString(titleResourceId)
        }
        tvTitle?.text = title
        val titleColorId = a.getResourceId(R.styleable.SwitchItemView_switchItemTitleColor, -1)
        titleColor = a.getColor(
            R.styleable.SwitchItemView_switchItemTitleColor,
            ContextCompat.getColor(getContext(), R.color.ease_neutral_10)
        )
        if (titleColorId != -1) {
            titleColor = ContextCompat.getColor(getContext(), titleColorId)
        }
        tvTitle?.setTextColor(titleColor)
        val titleSizeId = a.getResourceId(R.styleable.SwitchItemView_switchItemTitleSize, -1)
        titleSize =
            a.getDimension(R.styleable.SwitchItemView_switchItemTitleSize, sp2px(getContext(), 14f))
        if (titleSizeId != -1) {
            titleSize = resources.getDimension(titleSizeId)
        }
        tvTitle?.paint?.textSize = titleSize
        val titleStyle = a.getInteger(R.styleable.SwitchItemView_switchItemTitleStyle, -1)
        setTvStyle(titleStyle)
        val showDivider = a.getBoolean(R.styleable.SwitchItemView_switchItemShowDivider, true)
        viewDivider?.visibility = if (showDivider) VISIBLE else GONE
        val hintResourceId = a.getResourceId(R.styleable.SwitchItemView_switchItemHint, -1)
        hint = a.getString(R.styleable.SwitchItemView_switchItemHint)
        if (hintResourceId != -1) {
            hint = getContext().getString(hintResourceId)
        }
        tvHint?.text = hint
        val checkEnable = a.getBoolean(R.styleable.SwitchItemView_switchItemCheckEnable, true)
        switch?.isEnabled = checkEnable
        val clickable = a.getBoolean(R.styleable.SwitchItemView_switchItemClickable, true)
        switch?.isClickable = clickable
        val showAvatar = a.getBoolean(R.styleable.SwitchItemView_switchItemShowAvatar, false)
        avatar?.visibility = if (showAvatar) VISIBLE else GONE
        val avatarSrcResourceId =
            a.getResourceId(R.styleable.SwitchItemView_switchItemAvatarSrc, -1)
        if (avatarSrcResourceId != -1) {
            avatar?.setImageResource(avatarSrcResourceId)
        }
        val avatarHeightId = a.getResourceId(R.styleable.SwitchItemView_switchItemAvatarHeight, -1)
        var height = a.getDimension(R.styleable.SwitchItemView_switchItemAvatarHeight, 0f)
        if (avatarHeightId != -1) {
            height = resources.getDimension(avatarHeightId)
        }
        val avatarWidthId = a.getResourceId(R.styleable.SwitchItemView_switchItemAvatarWidth, -1)
        var width = a.getDimension(R.styleable.SwitchItemView_switchItemAvatarWidth, 0f)
        if (avatarWidthId != -1) {
            width = resources.getDimension(avatarWidthId)
        }
        a.recycle()
        val params = avatar?.layoutParams
        params?.height = if (height == 0f) ViewGroup.LayoutParams.WRAP_CONTENT else height.toInt()
        params?.width = if (width == 0f) ViewGroup.LayoutParams.WRAP_CONTENT else width.toInt()
        setListener()
        tvHint?.visibility = if (TextUtils.isEmpty(hint)) GONE else VISIBLE
    }

    private fun setListener() {
        switch?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (listener != null) {
                listener?.onCheckedChanged(this@ChatUIKitSwitchItemView, isChecked)
            }
        }
    }

    fun setChecked(checked: Boolean) {
        switch?.isChecked = checked
    }

    fun setSwitchTarckDrawable(@DrawableRes resourceId: Int){
        switch?.trackDrawable = context.getDrawable(resourceId)
    }

    fun setSwitchThumbDrawable(@DrawableRes resourceId: Int){
        switch?.thumbDrawable = context.getDrawable(resourceId)
    }

    private fun setTvStyle(titleStyle: Int) {
        when (titleStyle) {
            0 -> tvTitle?.setTypeface(null, Typeface.NORMAL)
            1 -> tvTitle?.setTypeface(null, Typeface.BOLD)
            2 -> tvTitle?.setTypeface(null, Typeface.ITALIC)
        }
    }

    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        this.listener = listener
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a compound button changed.
     */
    interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param buttonView The compound button view whose state has changed.
         * @param isChecked  The new checked state of buttonView.
         */
        fun onCheckedChanged(buttonView: ChatUIKitSwitchItemView?, isChecked: Boolean)
    }

    companion object {
        /**
         * sp to px
         * @param context
         * @param value
         * @return
         */
        fun sp2px(context: Context, value: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                value,
                context.resources.displayMetrics
            )
        }
    }
}