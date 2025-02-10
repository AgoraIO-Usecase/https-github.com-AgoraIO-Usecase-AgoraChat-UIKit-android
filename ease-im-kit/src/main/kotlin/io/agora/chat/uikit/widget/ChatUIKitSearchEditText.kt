package io.agora.chat.uikit.widget

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.dpToIntPx
import io.agora.chat.uikit.common.extensions.dpToPx

class ChatUIKitSearchEditText @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(
    mContext, attrs, defStyleAttr
) {
    private var mLeftHeight = 0f
    private var mLeftWidth = 0f
    private var mRightHeight = 0f
    private var mRightWidth = 0f
    private val DEFAULT_SIZE = 18f.dpToPx(context)
    private val DEFAULT_DRAWABLE_PADDING = 6f.dpToIntPx(context)
    private var left: Drawable? = null
    private var top: Drawable? = null
    private var right: Drawable? = null
    private var bottom: Drawable? = null

    init {
        init(mContext, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null) {
            val ta: TypedArray =
                context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitSearchEditText)
            mLeftHeight = ta.getDimension(
                R.styleable.ChatUIKitSearchEditText_search_edit_drawable_left_height,
                DEFAULT_SIZE
            )
            mLeftWidth = ta.getDimension(
                R.styleable.ChatUIKitSearchEditText_search_edit_drawable_left_width,
                DEFAULT_SIZE
            )
            mRightHeight = ta.getDimension(
                R.styleable.ChatUIKitSearchEditText_search_edit_drawable_right_height,
                0f
            )
            mRightWidth =
                ta.getDimension(R.styleable.ChatUIKitSearchEditText_search_edit_drawable_right_width, 0f)
            ta.recycle()
        }
        gravity = Gravity.CENTER_VERTICAL
        val hint: CharSequence = hint
        if (TextUtils.isEmpty(hint)) {
            setHint(getResources().getString(R.string.uikit_search_text_hint))
        }
        setDrawable()
    }

    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (left != null && mLeftWidth != 0f && mLeftHeight != 0f) {
            left!!.setBounds(0, 0, mLeftWidth.toInt(), mLeftHeight.toInt())
        }
        if (right != null && mRightWidth != 0f && mRightHeight != 0f) {
            right!!.setBounds(0, 0, mRightWidth.toInt(), mRightHeight.toInt())
        }
    }

    protected override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setCompoundDrawables(
            left,
            top,
            right,
            bottom
        )
    }

    private fun setDrawable() {
        // If have non-compat relative drawables, then ignore leftCompat/rightCompat
        if (Build.VERSION.SDK_INT >= 17) {
            val existingRel: Array<Drawable?> = getCompoundDrawablesRelative()
            if (existingRel[0] != null || existingRel[2] != null) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(
                    existingRel[0],
                    existingRel[1],
                    existingRel[2],
                    existingRel[3]
                )
                return
            }
        }
        // No relative drawables, so just set any compat drawables
        val existingAbs: Array<Drawable> = getCompoundDrawables()
        left = existingAbs[0]
        top = existingAbs[1]
        right = existingAbs[2]
        bottom = existingAbs[3]
        if (left == null) {
            left = ContextCompat.getDrawable(mContext, R.drawable.search)
        }
        if (right == null) {
            right = ContextCompat.getDrawable(mContext, R.drawable.search_delete)
        }
        if (left != null && mLeftWidth != 0f && mLeftHeight != 0f) {
            left!!.setBounds(0, 0, mLeftWidth.toInt(), mLeftHeight.toInt())
        }
        if (right != null && mRightWidth != 0f && mRightHeight != 0f) {
            right!!.setBounds(0, 0, mRightWidth.toInt(), mRightHeight.toInt())
        }
        setCompoundDrawables(
            if (left != null) left else existingAbs[0],
            existingAbs[1],
            if (right != null) right else existingAbs[2],
            existingAbs[3]
        )
        var background: Drawable? = getBackground()
        if (background == null) {
            background = ContextCompat.getDrawable(mContext, R.drawable.uikit_search_bg)
            setBackground(background)
        }
        var drawablePadding: Int = getCompoundDrawablePadding()
        if (drawablePadding == 0) {
            drawablePadding = DEFAULT_DRAWABLE_PADDING
            setCompoundDrawablePadding(drawablePadding)
        }
        val paddingLeft: Int = getPaddingLeft()
        val paddingRight: Int = getPaddingRight()
        val paddingTop: Int = getPaddingTop()
        val paddingBottom: Int = getPaddingBottom()
        if (paddingLeft == 0 || paddingRight == 0) {
            setPadding(16f.dpToIntPx(context), paddingTop, 16f.dpToIntPx(context), paddingBottom)
        }
    }
}