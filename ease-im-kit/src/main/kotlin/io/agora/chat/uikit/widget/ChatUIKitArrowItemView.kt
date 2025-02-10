package io.agora.chat.uikit.widget

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R

class ChatUIKitArrowItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr) {
    var avatar: ChatUIKitImageView? = null
        private set
    var tvTitle: TextView? = null
        private set
    var tvSubTitle: TextView? = null
        private set
    var tvContent: TextView? = null
        private set
    var rightTitle: TextView? = null
        private set
    private var ivArrow: ImageView? = null
    private var viewDivider: View? = null
    private var title: String? = null
    private var subTitle: String? = null
    private var content: String? = null
    private var titleColor = 0
    private var subTitleColor = 0
    private var contentColor = 0
    private var titleSize = 0f
    private var subTitleSize = 0f
    private var contentSize = 0f
    private var root: View? = null

    init {
        init(context, attrs)
    }

    fun init(context: Context, attrs: AttributeSet?) {
        root = LayoutInflater.from(context).inflate(R.layout.uikit_layout_item_arrow, this)
        avatar = findViewById(R.id.avatar)
        tvTitle = findViewById(R.id.tv_title)
        tvSubTitle = findViewById(R.id.tv_subtitle)
        tvContent = findViewById(R.id.tv_content)
        ivArrow = findViewById(R.id.iv_arrow)
        viewDivider = findViewById(R.id.view_divider)
        rightTitle = findViewById(R.id.tv_right)
        val a = context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitArrowItemView)
        val titleResourceId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemTitle, -1)
        title = a.getString(R.styleable.ChatUIKitArrowItemView_arrowItemTitle)
        if (titleResourceId != -1) {
            title = getContext().getString(titleResourceId)
        }
        tvTitle?.text = title
        val titleColorId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemTitleColor, -1)
        titleColor = a.getColor(
            R.styleable.ChatUIKitArrowItemView_arrowItemTitleColor,
            ContextCompat.getColor(getContext(), R.color.ease_color_on_background_low)
        )
        if (titleColorId != -1) {
            titleColor = ContextCompat.getColor(getContext(), titleColorId)
        }
        tvTitle?.setTextColor(titleColor)
        val titleStyle = a.getInteger(R.styleable.ChatUIKitArrowItemView_arrowItemTitleStyle, -1)
        setTvStyle(titleStyle)
        val titleSizeId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemTitleSize, -1)
        titleSize =
            a.getDimension(R.styleable.ChatUIKitArrowItemView_arrowItemTitleSize, sp2px(getContext(), 14f))
        if (titleSizeId != -1) {
            titleSize = resources.getDimension(titleSizeId)
        }
        tvTitle?.paint?.textSize = titleSize


        val subTitleResourceId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemSubTitle, -1)
        subTitle = a.getString(R.styleable.ChatUIKitArrowItemView_arrowItemSubTitle)
        if (subTitleResourceId != -1) {
            subTitle = getContext().getString(subTitleResourceId)
        }
        tvSubTitle?.text = subTitle
        val subTitleColorId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemSubTitleColor, -1)
        subTitleColor = a.getColor(
            R.styleable.ChatUIKitArrowItemView_arrowItemSubTitleColor,
            ContextCompat.getColor(getContext(), R.color.ease_color_text_secondary_high)
        )
        if (subTitleColorId != -1) {
            subTitleColor = ContextCompat.getColor(getContext(), subTitleColorId)
        }
        tvSubTitle?.setTextColor(subTitleColor)
        val showSubTitle = a.getBoolean(R.styleable.ChatUIKitArrowItemView_arrowItemShowSubTitle, false)
        tvSubTitle?.visibility = if (showSubTitle) VISIBLE else GONE
        val subTitleStyle = a.getInteger(R.styleable.ChatUIKitArrowItemView_arrowItemSubTitleStyle, -1)
        setTvStyle(subTitleStyle)
        val subTitleSizeId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemSubTitleSize, -1)
        subTitleSize =
            a.getDimension(R.styleable.ChatUIKitArrowItemView_arrowItemSubTitleSize, sp2px(getContext(), 14f))
        if (titleSizeId != -1) {
            titleSize = resources.getDimension(subTitleSizeId)
        }
        tvSubTitle?.paint?.textSize = subTitleSize

        val contentResourceId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemContent, -1)
        content = a.getString(R.styleable.ChatUIKitArrowItemView_arrowItemContent)
        if (contentResourceId != -1) {
            content = getContext().getString(contentResourceId)
        }
        tvContent?.text = content
        val contentColorId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemContentColor, -1)
        contentColor = a.getColor(
            R.styleable.ChatUIKitArrowItemView_arrowItemContentColor,
            ContextCompat.getColor(getContext(), R.color.ease_color_on_background_low)
        )
        if (contentColorId != -1) {
            contentColor = ContextCompat.getColor(getContext(), contentColorId)
        }
        tvContent?.setTextColor(contentColor)
        val contentSizeId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemContentSize, -1)
        contentSize =
            a.getDimension(R.styleable.ChatUIKitArrowItemView_arrowItemContentSize, sp2px(getContext(), 14f))
        if (contentSizeId != -1) {
            contentSize = resources.getDimension(contentSizeId)
        }
        tvContent?.paint?.textSize = contentSize
        val showDivider = a.getBoolean(R.styleable.ChatUIKitArrowItemView_arrowItemShowDivider, true)
        viewDivider?.visibility = if (showDivider) VISIBLE else GONE
        val showArrow = a.getBoolean(R.styleable.ChatUIKitArrowItemView_arrowItemShowArrow, true)
        ivArrow?.visibility = if (showArrow) VISIBLE else GONE
        val showAvatar = a.getBoolean(R.styleable.ChatUIKitArrowItemView_arrowItemShowAvatar, false)
        avatar?.visibility = if (showAvatar) VISIBLE else GONE
        val arrowSrcResourceId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemArrowSrc, -1)
        if (arrowSrcResourceId != -1) {
            ivArrow?.setImageResource(arrowSrcResourceId)
        }
        val avatarSrcResourceId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemAvatarSrc, -1)
        if (avatarSrcResourceId != -1) {
            avatar?.setImageResource(avatarSrcResourceId)
        }
        val avatarHeightId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemAvatarHeight, -1)
        var height = a.getDimension(R.styleable.ChatUIKitArrowItemView_arrowItemAvatarHeight, 0f)
        if (avatarHeightId != -1) {
            height = resources.getDimension(avatarHeightId)
        }
        val avatarWidthId = a.getResourceId(R.styleable.ChatUIKitArrowItemView_arrowItemAvatarWidth, -1)
        var width = a.getDimension(R.styleable.ChatUIKitArrowItemView_arrowItemAvatarWidth, 0f)
        if (avatarWidthId != -1) {
            width = resources.getDimension(avatarWidthId)
        }
        a.recycle()
        val params = avatar?.layoutParams
        params?.height = if (height == 0f) ViewGroup.LayoutParams.WRAP_CONTENT else height.toInt()
        params?.width = if (width == 0f) ViewGroup.LayoutParams.WRAP_CONTENT else width.toInt()
    }

    fun getTitle(): String {
        return tvTitle?.text.toString().trim { it <= ' ' }
    }

    fun setTitle(title: String?) {
        tvTitle?.text = title
    }

    fun getSubTitle(): String {
        return tvSubTitle?.text.toString().trim { it <= ' ' }
    }

    fun setSubTitle(title: String?) {
        tvSubTitle?.text = title
    }

    fun setSubTitleVisibility(visibility: Int){
        tvSubTitle?.visibility = visibility
    }

    fun setContent(content: String?) {
        tvContent?.text = content
    }

    fun setArrow(resourceId: Int) {
        ivArrow?.setImageResource(resourceId)
    }

    fun setArrowVisibility(visibility: Int) {
        ivArrow?.visibility = visibility
    }

    fun setItemDividerVisibility(visibility: Int){
        viewDivider?.visibility = visibility
    }

    fun setAvatar(resourceId: Int) {
        avatar?.setImageResource(resourceId)
    }

    fun setAvatarVisibility(visibility: Int) {
        avatar?.visibility = visibility
    }

    fun setAvatarMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val params = avatar?.layoutParams as LayoutParams
        params.setMargins(left, top, right, bottom)
    }

    fun setAvatarHeight(height: Int) {
        val params = avatar?.layoutParams
        params?.height = height
        avatar?.layoutParams = params
    }

    fun setAvatarWidth(width: Int) {
        val params = avatar?.layoutParams
        params?.width = width
        avatar?.layoutParams = params
    }

     fun setTvStyle(titleStyle: Int) {
        when (titleStyle) {
            0 -> tvTitle?.setTypeface(null, Typeface.NORMAL)
            1 -> tvTitle?.setTypeface(null, Typeface.BOLD)
            2 -> tvTitle?.setTypeface(null, Typeface.ITALIC)
        }
    }

    fun setTitleColor(titleColor: Int) {
        tvTitle?.setTextColor(titleColor)
    }

    fun setSubTitleColor(titleColor: Int) {
        tvSubTitle?.setTextColor(titleColor)
    }

    fun setContentColor(contentColor: Int) {
        tvContent?.setTextColor(contentColor)
    }

    fun setTitleSize(titleSize: Float) {
        tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
    }

    fun setSubTitleSize(titleSize: Float) {
        tvTitle?.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleSize)
    }

    fun setContentSize(contentSize: Float) {
        tvContent?.textSize = contentSize
    }

    companion object {
        /**
         * sp to px
         *
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