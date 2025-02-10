package io.agora.chat.uikit.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.dpToPx
import kotlin.math.min

class ChatUIKitWaveView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var showBgWave: Boolean = false
    private var mPaint: Paint? = Paint()
    private val mBgWavePaint: Paint by lazy {
        Paint().apply {
            color = bgWaveColor
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = bgWaveStrokeW.toFloat()
        }
    }
    private var waveWidth = 10.dpToPx(context)
    private var translationLength = 10.dpToPx(context)
    private var waveRectF: RectF? = null
    private var dx: Int = 0
    private var originalW: Int = 72.dpToPx(context)
    private var originalH: Int = 48.dpToPx(context)
    private var animatorSet: AnimatorSet? = null
    private var waveStartAlpha: Float = 1f
    private var waveEndAlpha: Float = 1f
    private var duration: Long = 2000

    private var bgWaveH: Int = 0
    private var bgWaveW: Int = 0
    private var bgWaveR: Int = -1
    private var bgWaveStrokeW: Int = 0
    private var bgWaveColor: Int = -1

    private var rx: Float = -1f
    private var ry: Float = -1f

    init {
        init(context)
        initAttrs(context, attrs)

        if (rx == -1f) {
            rx = min(originalW, originalH) / 2f
            ry = rx
        }

    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitWaveView)
        typedArray?.run {
            getColor(R.styleable.ChatUIKitWaveView_ease_wave_color, -1).let {
                if (it != -1) mPaint?.color = it
            }
            getResourceId(R.styleable.ChatUIKitWaveView_ease_wave_color, -1).let {
                if (it != -1) mPaint?.color = ContextCompat.getColor(context, it)
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_width, -1f).let {
                if (it != -1f) originalW = it.toInt()
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_height, -1f).let {
                if (it != -1f) originalH = it.toInt()
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_radius, -1f).let {
                if (it != -1f) {
                    rx = it
                    ry = rx
                }
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_stroke_width, -1f).let {
                if (it != -1f) waveWidth = it.toInt()
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_move_distance, -1f).let {
                if (it != -1f) translationLength = it.toInt()
            }
            getFloat(R.styleable.ChatUIKitWaveView_ease_wave_start_alpha, -1f).let {
                if (it != -1f) {
                    mPaint?.alpha = (it * 255).toInt()
                    waveStartAlpha = it
                }
            }
            getFloat(R.styleable.ChatUIKitWaveView_ease_wave_end_alpha, -1f).let {
                if (it != -1f) waveEndAlpha = it
            }
            getInt(R.styleable.ChatUIKitWaveView_ease_wave_duration, -1).let {
                if (it != -1) duration = it.toLong()
            }
            getBoolean(R.styleable.ChatUIKitWaveView_ease_wave_show_bg_wave, false).let {
                showBgWave = it
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_bg_height, -1f).let {
                if (it != -1f) bgWaveH = it.toInt()
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_bg_width, -1f).let {
                if (it != -1f) bgWaveW = it.toInt()
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_bg_radius, -1f).let {
                if (it != -1f) bgWaveR = it.toInt()
            }
            getDimension(R.styleable.ChatUIKitWaveView_ease_wave_bg_stroke_width, -1f).let {
                if (it != -1f) bgWaveStrokeW = it.toInt()
            }
            getColor(R.styleable.ChatUIKitWaveView_ease_wave_bg_color, -1).let {
                if (it != -1) bgWaveColor = it
            }
            getResourceId(R.styleable.ChatUIKitWaveView_ease_wave_bg_color, -1).let {
                if (it != -1) bgWaveColor = ContextCompat.getColor(context, it)
            }
            recycle()
        }
    }

    private fun init(context: Context) {
        mPaint?.run {
            color = ContextCompat.getColor(context, io.agora.chat.uikit.R.color.ease_color_primary)
            alpha = (waveStartAlpha * 255).toInt()
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = waveWidth.toFloat()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (showBgWave) {
            drawBgWave(canvas)
        }
        if (waveRectF == null) {
            calculateWaveRect(true)
        }
        waveRectF?.run {
            canvas.drawRoundRect(calculateRectChange(this), rx + dx, ry + dx, mPaint!!)
        }
    }

    private fun drawBgWave(canvas: Canvas) {
        if (bgWaveH == 0 || bgWaveW == 0) {
            return
        }
        val left = measuredWidth / 2 - bgWaveW / 2 + bgWaveStrokeW / 2
        val top = measuredHeight / 2 - bgWaveH / 2 + bgWaveStrokeW / 2
        val right = measuredWidth / 2 + bgWaveW / 2 - bgWaveStrokeW / 2
        val bottom = measuredHeight / 2 + bgWaveH / 2 - bgWaveStrokeW / 2
        val rect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())

        if (bgWaveR == -1) {
            bgWaveR = min(bgWaveW, bgWaveH) / 2
        }
        if (bgWaveColor == -1) {
            bgWaveColor = ContextCompat.getColor(context, io.agora.chat.uikit.R.color.ease_primary_90)
        }

        canvas.drawRoundRect(rect, bgWaveR.toFloat(), bgWaveR.toFloat(), mBgWavePaint)
    }

    private fun initAnimator() {
        if (animatorSet != null && animatorSet!!.isRunning) {
            animatorSet?.cancel()
        }
        translationLength = if (width - originalW > height - originalH) {
            if (translationLength > (height - originalH)) {
                height - originalH
            } else {
                translationLength
            }
        } else {
            if (translationLength > width - originalW) {
                width - originalW
            } else {
                translationLength
            }
        }

        val animator = ValueAnimator.ofInt(0, translationLength)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.addUpdateListener {
            dx = it.animatedValue as Int
            invalidate()
        }

        val alphaAnimator = ObjectAnimator.ofFloat(this, "alpha", waveStartAlpha, waveEndAlpha)
        alphaAnimator.duration = duration
        alphaAnimator.repeatCount = ValueAnimator.INFINITE
        alphaAnimator.repeatMode = ValueAnimator.RESTART

        animatorSet = AnimatorSet()
        animatorSet?.run {
            play(animator).with(alphaAnimator)
        }
    }

    private fun calculateRectChange(rectF: RectF): RectF {
        val left = rectF.left - dx
        val top = rectF.top - dx
        val right = rectF.right + dx
        val bottom = rectF.bottom + dx
        return RectF(left, top, right, bottom)
    }

    /**
     * Start the wave.
     */
    fun startPlay() {
        if (isPlaying()) {
            return
        }
        initAnimator()
        calculateWaveRect()
        animatorSet?.start()
    }

    /**
     * Stop the wave.
     */
    fun stopPlay() {
        animatorSet?.cancel()
        if (dx != 0) {
            dx = 0
            postInvalidate()
        }
    }

    /**
     * Whether the wave is playing.
     */
    fun isPlaying(): Boolean {
        return animatorSet?.isRunning ?: false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animatorSet?.cancel()
        animatorSet = null
    }

    private fun calculateWaveSize() {
        waveRectF?.run {
            originalW = (right - left).toInt()
            originalH = (bottom - top).toInt()
        }
    }

    private fun calculateWaveRect(isOnDraw: Boolean = false) {
        val left = (if (isOnDraw) measuredWidth else width) / 2 - originalW / 2 + waveWidth / 2
        val top = (if (isOnDraw) measuredHeight else height) / 2 - originalH / 2 + waveWidth / 2
        val right = (if (isOnDraw) measuredWidth else width) / 2 + originalW / 2 - waveWidth / 2
        val bottom = (if (isOnDraw) measuredHeight else height) / 2 + originalH / 2 - waveWidth / 2
        waveRectF = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
    }

    /**
     * Set the wave width.
     */
    fun waveWidth(width: Int) {
        waveWidth = width
    }

    /**
     * Set the wave maximum width.
     */
    fun waveTranslationLength(translationLength: Int) {
        this.translationLength = translationLength
    }

    /**
     * Set the wave color.
     */
    fun waveColor(color: Int) {
        mPaint?.color = color
    }

    /**
     * Set the wave rect.
     */
    fun waveRect(rectF: RectF?, rx: Float, ry: Float) {
        this.waveRectF = rectF
        this.rx = rx
        this.ry = ry
        calculateWaveSize()
    }

    /**
     * Set the wave size.
     * @param width The wave width.
     * @param height The wave height.
     */
    fun waveSize(width: Int, height: Int) {
        originalW = width
        originalH = height
    }

    /**
     * Set the wave paint.
     */
    fun setWavePaint(paint: Paint?) {
        mPaint = paint
    }
}