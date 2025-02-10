package io.agora.chat.uikit.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatLog

/**
 * side bar
 */
class ChatUIKitSidebar(private val context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    private var paint: Paint? = null
    private var itemHeight = 0f
    private var mListener: OnTouchEventListener? = null
    private var sections = arrayOf<String?>("A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
    )
    private var topText: String? = ""
    private var mTextColor = 0
    private var mTextSize = 0f
    private var mBgColor = 0
    private var mWidth = 0
    private var mHeight = 0
    private var mTextCoefficient = 1f
    private var pointer = -1
    private val selectedPaint: Paint? = null
    private var mFocusBgColor = 0
    private var delayDisappearTime = 0
    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                RESET -> {
                    pointer = -1
                    invalidate()
                }
            }
        }
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : this(context, attrs, 0)

    init {
        initAttrs(attrs)
        init()
    }

    @SuppressLint("Recycle")
    private fun initAttrs(attrs: AttributeSet?) {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitSidebar)
            val topTextId = a.getResourceId(R.styleable.ChatUIKitSidebar_ease_side_bar_top_text, -1)
            topText = if (topTextId != -1) {
                context.resources.getString(topTextId)
            } else {
                a.getString(R.styleable.ChatUIKitSidebar_ease_side_bar_top_text)
            }
            val textColorId = a.getResourceId(R.styleable.ChatUIKitSidebar_ease_side_bar_text_color, -1)
            mTextColor = if (textColorId != -1) {
                ContextCompat.getColor(context, textColorId)
            } else {
                a.getColor(
                    R.styleable.ChatUIKitSidebar_ease_side_bar_text_color, Color.parseColor(
                        DEFAULT_COLOR
                    )
                )
            }
            val focusBgColor =
                a.getResourceId(R.styleable.ChatUIKitSidebar_ease_side_bar_focus_bg_color, -1)
            mFocusBgColor = if (focusBgColor != -1) {
                ContextCompat.getColor(context, focusBgColor)
            } else {
                a.getColor(R.styleable.ChatUIKitSidebar_ease_side_bar_focus_bg_color, Color.TRANSPARENT)
            }
            delayDisappearTime =
                a.getInteger(R.styleable.ChatUIKitSidebar_ease_side_bar_delay_disappear_time, 500)
            mTextSize =
                a.getDimension(R.styleable.ChatUIKitSidebar_ease_side_bar_text_size, DEFAULT_TEXT_SIZE)
            val bgId = a.getResourceId(R.styleable.ChatUIKitSidebar_ease_side_bar_background, -1)
            mBgColor = if (bgId != -1) {
                ContextCompat.getColor(context, textColorId)
            } else {
                a.getColor(R.styleable.ChatUIKitSidebar_ease_side_bar_background, Color.TRANSPARENT)
            }
            val headArrays = a.getResourceId(R.styleable.ChatUIKitSidebar_ease_side_bar_head_arrays, -1)
            sections = if (headArrays != -1) {
                resources.getStringArray(headArrays)
            } else {
                arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
                    "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
                )
            }
        }
    }

    private fun init() {
        if (sections.size > 27) {
            if (!TextUtils.isEmpty(topText)) {
                sections[0] = topText
            }
        }
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint?.color = mTextColor
        paint?.textAlign = Align.CENTER
        paint?.textSize = mTextSize
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // get the view's height
        mWidth = w
        mHeight = h
        checkTextSize()
    }

    /**
     * Verify that the text size is appropriate
     */
    private fun checkTextSize() {
        if (paint != null) {
            val metrics = paint?.fontMetrics
            metrics?.let {
                val textItemHeight = it.bottom - it.top
                if (sections.size * textItemHeight > mHeight) {
                    mTextCoefficient = mHeight / (sections.size * textItemHeight)
                    paint?.let { p->
                        p.textSize = p.textSize * mTextCoefficient
                    }
                } else {
                    paint?.textSize = mTextSize
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        ChatLog.d("ChatUIKitSidebar", "onDraw pointer: $pointer")
        if (mBgColor != Color.TRANSPARENT) {
            canvas.drawColor(mBgColor)
        }
        val center = (width / 2).toFloat()
        itemHeight = (height / sections.size).toFloat()
        for (i in sections.size - 1 downTo -1 + 1) {
            if (i == pointer && mFocusBgColor != Color.TRANSPARENT) {
                paint?.let {
                    it.color = mFocusBgColor
                    canvas.drawCircle(
                        center, itemHeight * (i + 0.75f), (mTextSize * 0.6).toFloat(), it
                    )
                    it.color = Color.WHITE
                }
            } else {
                paint?.let {
                    it.color = mTextColor
                }
            }
            sections[i]?.let { paint?.let { it1 ->
                canvas.drawText(it, center, itemHeight * (i + 1), it1) }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        pointer = sectionForPoint(event.y)
        val section = sections[pointer]
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.removeCallbacksAndMessages(null)
                invalidate()
                // Provides external interfaces for developers to operate
                if (mListener != null) {
                    mListener?.onActionDown(event, section)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                handler.removeCallbacksAndMessages(null)
                invalidate()
                // Provides external interfaces for developers to operate
                if (mListener != null) {
                    mListener?.onActionMove(event, section)
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacksAndMessages(null)
                handler.sendEmptyMessageDelayed(RESET, delayDisappearTime.toLong())
                if (mListener != null) {
                    mListener?.onActionUp(event)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Gets the character as it moves
     * @param y
     * @return
     */
    private fun sectionForPoint(y: Float): Int {
        var index = (y / itemHeight).toInt()
        if (index < 0) {
            index = 0
        }
        if (index > sections.size - 1) {
            index = sections.size - 1
        }
        return index
    }

    /**
     * Draw background
     * @param color
     */
    fun drawBackground(@ColorRes color: Int) {
        mBgColor = ContextCompat.getColor(context, color)
        postInvalidate()
    }

    fun drawBackgroundDrawable(@DrawableRes drawableId: Int) {
        background = ContextCompat.getDrawable(context, drawableId)
    }

    fun drawBackgroundDrawable(drawable: Drawable?) {
        background = drawable
    }

    /**
     * set touch event listener
     * @param listener
     */
    fun setOnTouchEventListener(listener: OnTouchEventListener?) {
        mListener = listener
    }

    interface OnTouchEventListener {
        /**
         * Down event
         * @param event
         * @param pointer
         */
        fun onActionDown(event: MotionEvent?, pointer: String?)

        /**
         * Move event
         * @param event
         * @param pointer
         */
        fun onActionMove(event: MotionEvent?, pointer: String?)

        /**
         * Up event
         * @param event
         */
        fun onActionUp(event: MotionEvent?)
    }

    companion object {
        private const val RESET = 1
        private const val DEFAULT_COLOR = "#8C8C8C"
        private const val DEFAULT_TEXT_SIZE = 10f
    }
}