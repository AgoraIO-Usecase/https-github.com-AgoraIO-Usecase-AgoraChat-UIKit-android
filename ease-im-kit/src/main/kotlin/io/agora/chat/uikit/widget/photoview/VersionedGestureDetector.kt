package io.agora.chat.uikit.widget.photoview

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.view.VelocityTracker
import android.view.ViewConfiguration

/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
internal abstract class VersionedGestureDetector {
    var mListener: OnGestureListener? = null
    abstract fun onTouchEvent(ev: MotionEvent): Boolean
    abstract fun isScaling(): Boolean

    interface OnGestureListener {
        fun onDrag(dx: Float, dy: Float)
        fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float)
        fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)
    }

    private open class CupcakeDetector(context: Context?) : VersionedGestureDetector() {
        var mLastTouchX = 0f
        var mLastTouchY = 0f
        val mTouchSlop: Float
        val mMinimumVelocity: Float
        private var mVelocityTracker: VelocityTracker? = null
        private var mIsDragging = false

        init {
            val configuration = ViewConfiguration.get(
                context!!
            )
            mMinimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
            mTouchSlop = configuration.scaledTouchSlop.toFloat()
        }

        open fun getActiveX(ev: MotionEvent): Float {
            return ev.x
        }

        open fun getActiveY(ev: MotionEvent): Float {
            return ev.y
        }

        override fun isScaling(): Boolean {
            return false
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mVelocityTracker = VelocityTracker.obtain()
                    mVelocityTracker?.addMovement(ev)
                    mLastTouchX = getActiveX(ev)
                    mLastTouchY = getActiveY(ev)
                    mIsDragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    val x = getActiveX(ev)
                    val y = getActiveY(ev)
                    val dx = x - mLastTouchX
                    val dy = y - mLastTouchY
                    if (!mIsDragging) {
                        // Use Pythagoras to see if drag length is larger than
                        // touch slop
                        mIsDragging = Math.sqrt((dx * dx + dy * dy).toDouble()) >= mTouchSlop
                    }
                    if (mIsDragging) {
                        mListener!!.onDrag(dx, dy)
                        mLastTouchX = x
                        mLastTouchY = y
                        if (null != mVelocityTracker) {
                            mVelocityTracker!!.addMovement(ev)
                        }
                    }
                }

                MotionEvent.ACTION_CANCEL -> {

                    // Recycle Velocity Tracker
                    if (null != mVelocityTracker) {
                        mVelocityTracker!!.recycle()
                        mVelocityTracker = null
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (mIsDragging) {
                        if (null != mVelocityTracker) {
                            mLastTouchX = getActiveX(ev)
                            mLastTouchY = getActiveY(ev)

                            // Compute velocity within the last 1000ms
                            mVelocityTracker!!.addMovement(ev)
                            mVelocityTracker!!.computeCurrentVelocity(1000)
                            val vX = mVelocityTracker!!.xVelocity
                            val vY = mVelocityTracker!!.yVelocity

                            // If the velocity is greater than minVelocity, call
                            // listener
                            if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                                mListener!!.onFling(mLastTouchX, mLastTouchY, -vX, -vY)
                            }
                        }
                    }

                    // Recycle Velocity Tracker
                    if (null != mVelocityTracker) {
                        mVelocityTracker!!.recycle()
                        mVelocityTracker = null
                    }
                }
            }
            return true
        }

    }

    @TargetApi(5)
    private open class EclairDetector(context: Context?) : CupcakeDetector(context) {
        private var mActivePointerId = INVALID_POINTER_ID
        private var mActivePointerIndex = 0
        override fun getActiveX(ev: MotionEvent): Float {
            return try {
                ev.getX(mActivePointerIndex)
            } catch (e: Exception) {
                ev.x
            }
        }

        override fun getActiveY(ev: MotionEvent): Float {
            return try {
                ev.getY(mActivePointerIndex)
            } catch (e: Exception) {
                ev.y
            }
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            val action = ev.action
            when (action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> mActivePointerId = ev.getPointerId(0)
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> mActivePointerId =
                    INVALID_POINTER_ID

                MotionEvent.ACTION_POINTER_UP -> {
                    val pointerIndex =
                        ev.action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                    val pointerId = ev.getPointerId(pointerIndex)
                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        val newPointerIndex = if (pointerIndex == 0) 1 else 0
                        mActivePointerId = ev.getPointerId(newPointerIndex)
                        mLastTouchX = ev.getX(newPointerIndex)
                        mLastTouchY = ev.getY(newPointerIndex)
                    }
                }
            }
            mActivePointerIndex =
                ev.findPointerIndex(if (mActivePointerId != INVALID_POINTER_ID) mActivePointerId else 0)
            return super.onTouchEvent(ev)
        }

        companion object {
            private const val INVALID_POINTER_ID = -1
        }
    }

    @TargetApi(8)
    private class FroyoDetector(context: Context?) : EclairDetector(context) {
        private val mDetector: ScaleGestureDetector

        // Needs to be an inner class so that we don't hit
        // VerifyError's on API 4.
        private val mScaleListener: OnScaleGestureListener = object : OnScaleGestureListener {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                mListener!!.onScale(detector.scaleFactor, detector.focusX, detector.focusY)
                return true
            }

            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                // NO-OP
            }
        }

        init {
            mDetector = ScaleGestureDetector(context!!, mScaleListener)
        }

        override fun isScaling(): Boolean {
            return mDetector.isInProgress
        }

        override fun onTouchEvent(ev: MotionEvent): Boolean {
            mDetector.onTouchEvent(ev)
            return super.onTouchEvent(ev)
        }
    }

    companion object {
        const val LOG_TAG = "VersionedGestureDetector"
        fun newInstance(context: Context?, listener: OnGestureListener?): VersionedGestureDetector {
            val sdkVersion = Build.VERSION.SDK_INT
            val detector: VersionedGestureDetector = if (sdkVersion < VERSION_CODES.ECLAIR) {
                CupcakeDetector(context)
            } else if (sdkVersion < VERSION_CODES.FROYO) {
                EclairDetector(context)
            } else {
                FroyoDetector(context)
            }
            detector.mListener = listener
            return detector
        }
    }
}