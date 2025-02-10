package io.agora.chat.uikit.feature.chat.interfaces

import android.view.View

interface OnMessageListTouchListener {
    /**
     * touch event
     * @param v
     * @param position
     */
    fun onTouchItemOutside(v: View?, position: Int)

    /**
     * The control is being dragged
     */
    fun onViewDragging()

    /**
     * RecyclerView scroll to bottom
     */
    fun onReachBottom()

    /**
     * RecyclerView is not scrolling.
     */
    fun onFinishScroll()
}