package io.agora.chat.uikit.interfaces

import android.view.View

/**
 * The interface is used for RecyclerView which LinearLayoutManager is vertical.
 */
interface OnRecyclerViewTouchListener {
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
     * RecyclerView scroll to the top.
     */
    fun onReachingTop()

    /**
     * RecyclerView scroll to the bottom.
     */
    fun onReachBottom()
}