package io.agora.chat.uikit.widget.photoview

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.widget.OverScroller
import android.widget.Scroller

internal abstract class ScrollerProxy {
    abstract fun computeScrollOffset(): Boolean
    abstract fun fling(
        startX: Int, startY: Int, velocityX: Int, velocityY: Int, minX: Int, maxX: Int, minY: Int,
        maxY: Int, overX: Int, overY: Int
    )

    abstract fun forceFinished(finished: Boolean)
    abstract fun getCurrX(): Int
    abstract fun getCurrY(): Int

    @TargetApi(9)
    private class GingerScroller(context: Context?) : ScrollerProxy() {
        private val mScroller: OverScroller

        init {
            mScroller = OverScroller(context)
        }

        override fun computeScrollOffset(): Boolean {
            return mScroller.computeScrollOffset()
        }

        override fun fling(
            startX: Int,
            startY: Int,
            velocityX: Int,
            velocityY: Int,
            minX: Int,
            maxX: Int,
            minY: Int,
            maxY: Int,
            overX: Int,
            overY: Int
        ) {
            mScroller.fling(
                startX,
                startY,
                velocityX,
                velocityY,
                minX,
                maxX,
                minY,
                maxY,
                overX,
                overY
            )
        }

        override fun forceFinished(finished: Boolean) {
            mScroller.forceFinished(finished)
        }

        override fun getCurrX(): Int {
            return mScroller.currX
        }

        override fun getCurrY(): Int {
            return mScroller.currY
        }
    }

    private class PreGingerScroller(context: Context?) : ScrollerProxy() {
        private val mScroller: Scroller

        init {
            mScroller = Scroller(context)
        }

        override fun computeScrollOffset(): Boolean {
            return mScroller.computeScrollOffset()
        }

        override fun fling(
            startX: Int,
            startY: Int,
            velocityX: Int,
            velocityY: Int,
            minX: Int,
            maxX: Int,
            minY: Int,
            maxY: Int,
            overX: Int,
            overY: Int
        ) {
            mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
        }

        override fun forceFinished(finished: Boolean) {
            mScroller.forceFinished(finished)
        }

        override fun getCurrX(): Int {
            return mScroller.currX
        }

        override fun getCurrY(): Int {
            return mScroller.currY
        }
    }

    companion object {
        fun getScroller(context: Context?): ScrollerProxy {
            return if (Build.VERSION.SDK_INT < VERSION_CODES.GINGERBREAD) {
                PreGingerScroller(context)
            } else {
                GingerScroller(context)
            }
        }
    }
}