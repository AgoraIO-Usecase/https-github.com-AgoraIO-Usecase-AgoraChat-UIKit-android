package io.agora.chat.uikit.widget

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

class ChatUIKitDividerGridItemDecoration(
    private val context: Context,
    private val drawableId: Int = -1,
    private val showLastLine: Boolean = false
) : RecyclerView.ItemDecoration() {
    private var mDivider: Drawable?
    private var mShowLastLine = false

    init {
        if (drawableId == -1) {
            val a = context.obtainStyledAttributes(ATTRS)
            mDivider = a.getDrawable(0)
            a.recycle()
        } else {
            mDivider = ContextCompat.getDrawable(context, drawableId)
        }
        mShowLastLine = showLastLine
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawHorizontal(c, parent)
        drawVertical(c, parent)
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        var spanCount = -1
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            spanCount = layoutManager.spanCount
        } else if (layoutManager is StaggeredGridLayoutManager) {
            spanCount = layoutManager
                .spanCount
        }
        return spanCount
    }

    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val childCount: Int = if (mShowLastLine) {
            parent.childCount
        } else {
            val spanCount = getSpanCount(parent)
            val count = parent.childCount
            count - if (count % spanCount == 0) spanCount else count % spanCount
        }
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val right = (child.right + params.rightMargin
                    + mDivider!!.intrinsicWidth)
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }

    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                .layoutParams as RecyclerView.LayoutParams
            val top = child.top - params.topMargin
            val bottom = child.bottom + params.bottomMargin
            val left = child.right + params.rightMargin
            val right = left + mDivider!!.intrinsicWidth
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)
        }
    }

    private fun isLastColum(
        parent: RecyclerView, pos: Int, spanCount: Int,
        childCount: Int
    ): Boolean {
        var childCount = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            if ((pos + 1) % spanCount == 0) // If it's the last column, you don't need to draw the right
            {
                return true
            }
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager
                .orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                if ((pos + 1) % spanCount == 0) {
                    return true
                }
            } else {
                childCount -= childCount % spanCount
                if (pos >= childCount) return true
            }
        }
        return false
    }

    private fun isLastRaw(
        parent: RecyclerView, pos: Int, spanCount: Int,
        childCount: Int
    ): Boolean {
        var childCount = childCount
        val layoutManager = parent.layoutManager
        if (layoutManager is GridLayoutManager) {
            childCount -= childCount % spanCount
            if (pos >= childCount) return true
        } else if (layoutManager is StaggeredGridLayoutManager) {
            val orientation = layoutManager
                .orientation
            if (orientation == StaggeredGridLayoutManager.VERTICAL) {
                childCount -= childCount % spanCount
                if (pos >= childCount) return true
            } else {
                if ((pos + 1) % spanCount == 0) {
                    return true
                }
            }
        }
        return false
    }

    override fun getItemOffsets(
        outRect: Rect, itemPosition: Int,
        parent: RecyclerView
    ) {
        val spanCount = getSpanCount(parent)
        parent.adapter?.let {
            val childCount = it.itemCount
            if (isLastRaw(parent, itemPosition, spanCount, childCount)) {
                outRect[0, 0, mDivider!!.intrinsicWidth] = 0
            } else if (isLastColum(parent, itemPosition, spanCount, childCount)) {
                outRect[0, 0, 0] = mDivider!!.intrinsicHeight
            } else {
                outRect[0, 0, mDivider!!.intrinsicWidth] = mDivider!!.intrinsicHeight
            }
        }
    }

    companion object {
        private val ATTRS = intArrayOf(R.attr.listDivider)
    }
}