package io.agora.chat.uikit.feature.chat.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.dpToPx

@SuppressLint("NewApi")
class ChatUIKitEmojiconIndicatorView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    private var selectedBitmap: Bitmap? = null
    private var unselectedBitmap: Bitmap? = null
    private val dotViews: MutableList<ImageView> by lazy { mutableListOf() }
    private var dotHeight = 12

    init {
        dotHeight = dotHeight.dpToPx(context)
        selectedBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.uikit_dot_emojicon_selected)
        unselectedBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.uikit_dot_emojicon_unselected)
        gravity = Gravity.CENTER_HORIZONTAL
    }

    fun init(count: Int) {
        for (i in 0 until count) {
            val rl = RelativeLayout(context)
            val params = LayoutParams(dotHeight, dotHeight)
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            val imageView: ImageView = EImageView(context)
            if (i == 0) {
                imageView.setImageBitmap(selectedBitmap)
                rl.addView(imageView, layoutParams)
            } else {
                imageView.setImageBitmap(unselectedBitmap)
                rl.addView(imageView, layoutParams)
            }
            this.addView(rl, params)
            dotViews.add(imageView)
        }
    }

    fun updateIndicator(count: Int) {
        for (i in dotViews.indices) {
            if (i >= count) {
                dotViews[i].visibility = GONE
                (dotViews[i].parent as View).visibility = GONE
            } else {
                dotViews[i].visibility = VISIBLE
                (dotViews[i].parent as View).visibility = VISIBLE
            }
        }
        if (count > dotViews.size) {
            val diff = count - dotViews.size
            for (i in 0 until diff) {
                val rl = RelativeLayout(context)
                val params = LayoutParams(dotHeight, dotHeight)
                val layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
                val imageView: ImageView = EImageView(context)
                imageView.setImageBitmap(unselectedBitmap)
                rl.addView(imageView, layoutParams)
                rl.visibility = GONE
                imageView.visibility = GONE
                this.addView(rl, params)
                dotViews.add(imageView)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (selectedBitmap != null) {
            selectedBitmap!!.recycle()
        }
        if (unselectedBitmap != null) {
            unselectedBitmap!!.recycle()
        }
    }

    fun selectTo(position: Int) {
        for (iv in dotViews) {
            iv.setImageBitmap(unselectedBitmap)
        }
        dotViews[position].setImageBitmap(selectedBitmap)
    }

    fun selectTo(startPosition: Int, targetPostion: Int) {
        val startView = dotViews[startPosition]
        val targetView = dotViews[targetPostion]
        startView.setImageBitmap(unselectedBitmap)
        targetView.setImageBitmap(selectedBitmap)
    }
}

internal class EImageView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
) : AppCompatImageView(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}