package io.agora.chat.uikit.common.helper

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * RecycleView paging rolling tool class
 * Refer to：https://blog.csdn.net/Y_sunny_U/article/details/89500464
 */
class PagingScrollHelper {
    var mRecyclerView: RecyclerView? = null
    private val mOnScrollListener = PageOnScrollListener()
    private val mOnFlingListener = PageOnFlingListener()
    private var offsetY = 0
    private var offsetX = 0
    var startY = 0
    var startX = 0
    var mAnimator: ValueAnimator? = null
    private val mOnTouchListener = PageOnTouchListener()
    private var firstTouch = true
    private var mOnPageChangeListener: OnPageChangeListener? = null
    private var lastPageIndex = 0
    private var mOrientation = ORIENTATION.HORIZONTAL
    private var currentPosition = 0

    internal enum class ORIENTATION {
        HORIZONTAL, VERTICAL, NULL
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setUpRecycleView(recycleView: RecyclerView?) {
        requireNotNull(recycleView) { "recycleView must be not null" }
        mRecyclerView = recycleView
        recycleView.onFlingListener = mOnFlingListener
        recycleView.addOnScrollListener(mOnScrollListener)
        recycleView.setOnTouchListener(mOnTouchListener)
        updateLayoutManger()
    }

    fun updateLayoutManger() {
        mRecyclerView?.layoutManager?.let {
            mOrientation = if (it.canScrollVertically()) {
                ORIENTATION.VERTICAL
            } else if (it.canScrollHorizontally()) {
                ORIENTATION.HORIZONTAL
            } else {
                ORIENTATION.NULL
            }
            mAnimator?.cancel()
            startX = 0
            startY = 0
            offsetX = 0
            offsetY = 0
        }
    }

    val pageCount: Int
        get() {
            mRecyclerView?.let {
                if (mOrientation == ORIENTATION.NULL) {
                    return 0
                }
                if (mOrientation == ORIENTATION.VERTICAL && it.computeVerticalScrollExtent() != 0) {
                    return it.computeVerticalScrollRange() / it.computeVerticalScrollExtent()
                } else if (it.computeHorizontalScrollExtent() != 0) {
                    return it.computeHorizontalScrollRange() / it.computeHorizontalScrollExtent()
                }
            }
            return 0
        }

    fun scrollToPosition(position: Int) {
        currentPosition = position
        if (mAnimator == null) {
            mOnFlingListener.onFling(0, 0)
        }
        mAnimator?.let {
            if (it.isRunning) {
                it.cancel()
            }
            val startPoint = if (mOrientation == ORIENTATION.VERTICAL) offsetY else offsetX
            var endPoint = 0
            endPoint = if (mOrientation == ORIENTATION.VERTICAL) {
                mRecyclerView!!.height * position
            } else {
                mRecyclerView!!.width * position
            }
            if (startPoint != endPoint) {
                it.setIntValues(startPoint, endPoint)
                it.start()
            }
        }
    }

    fun checkCurrentStatus() {
        if (mOrientation == ORIENTATION.VERTICAL) {
            mRecyclerView?.let {
                if (offsetY != it.height * currentPosition) {
                    offsetY = it.height * currentPosition
                    it.scrollTo(0, offsetY)
                }
            }
        } else {
            mRecyclerView?.let {
                if (offsetX != it.width * currentPosition) {
                    offsetX = it.width * currentPosition
                    it.scrollTo(offsetX, 0)
                }
            }
        }
    }

    inner class PageOnFlingListener : RecyclerView.OnFlingListener() {
        override fun onFling(velocityX: Int, velocityY: Int): Boolean {
            if (mOrientation == ORIENTATION.NULL) {
                return false
            }
            //Gets the index of the page on which scrolling begins
            var p: Int = startPageIndex

            //Record where scrolling begins and ends
            var endPoint = 0
            var startPoint = 0

            //If it's in the vertical direction
            if (mOrientation == ORIENTATION.VERTICAL) {
                startPoint = offsetY
                if (velocityY < 0) {
                    p--
                } else if (velocityY > 0) {
                    p++
                }
                endPoint = p * mRecyclerView!!.height
            } else {
                startPoint = offsetX
                if (velocityX < 0) {
                    p--
                } else if (velocityX > 0) {
                    p++
                }
                endPoint = p * mRecyclerView!!.width
            }
            if (endPoint < 0) {
                endPoint = 0
            }
            if (mAnimator == null) {
                mAnimator = ValueAnimator.ofInt(startPoint, endPoint)
            } else {
                mAnimator?.cancel()
                mAnimator?.setIntValues(startPoint, endPoint)
            }
            mAnimator?.run {
                duration = 200
                addUpdateListener { animation ->
                    val nowPoint = animation.animatedValue as Int
                    if (mOrientation == ORIENTATION.VERTICAL) {
                        val dy = nowPoint - offsetY
                        mRecyclerView!!.scrollBy(0, dy)
                    } else {
                        val dx = nowPoint - offsetX
                        mRecyclerView!!.scrollBy(dx, 0)
                    }
                }
                addListener(onEnd = {
                    val pageIndex: Int = pageIndex
                    if (lastPageIndex != pageIndex) {
                        mOnPageChangeListener?.onPageChange(pageIndex)
                        lastPageIndex = pageIndex
                    }
                    mRecyclerView?.stopScroll()
                    startY = offsetY
                    startX = offsetX
                })
                start()
            }
            return true
        }
    }

    inner class PageOnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            //newState ==0 indicates that scrolling stops and rollback needs to be processed
            if (newState == 0 && mOrientation != ORIENTATION.NULL) {
                val move: Boolean
                var vX = 0
                var vY = 0
                if (mOrientation == ORIENTATION.VERTICAL) {
                    val absY = abs(offsetY - startY)
                    move = absY > recyclerView.height / 2
                    vY = 0
                    if (move) {
                        vY = if (offsetY - startY < 0) -1000 else 1000
                    }
                } else {
                    val absX = abs(offsetX - startX)
                    move = absX > recyclerView.width / 2
                    if (move) {
                        vX = if (offsetX - startX < 0) -1000 else 1000
                    }
                }
                mOnFlingListener.onFling(vX, vY)
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            offsetY += dy
            offsetX += dx
        }
    }

    inner class PageOnTouchListener : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            if (firstTouch) {
                firstTouch = false
                startY = offsetY
                startX = offsetX
            }
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                firstTouch = true
            }
            return false
        }
    }

    private val pageIndex: Int
        private get() {
            var p = 0
            mRecyclerView?.run {
                if (height == 0 || width == 0) {
                    return p
                }
                p = if (mOrientation == ORIENTATION.VERTICAL) {
                    offsetY / height
                } else {
                    offsetX / width
                }
            }
            return p
        }
    private val startPageIndex: Int
        private get() {
            var p = 0
            mRecyclerView?.let {
                if (it.height == 0 || it.width == 0) {
                    return p
                }
                p = if (mOrientation == ORIENTATION.VERTICAL) {
                    startY / it.height
                } else {
                    startX / it.width
                }
            }
            return p
        }

    /**
     * set page change listener
     * @param listener
     */
    fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        mOnPageChangeListener = listener
    }

    interface OnPageChangeListener {
        fun onPageChange(index: Int)
    }
}