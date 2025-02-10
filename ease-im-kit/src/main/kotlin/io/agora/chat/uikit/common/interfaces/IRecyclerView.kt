package io.agora.chat.uikit.common.interfaces

import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.interfaces.OnItemLongClickListener
import io.agora.chat.uikit.interfaces.OnRecyclerViewTouchListener

/**
 * Define the common methods of RecyclerView.
 */
interface IRecyclerView {
    /**
     * Add header adapter
     * @param adapter
     */
    fun addHeaderAdapter(adapter: RecyclerView.Adapter<*>?)

    /**
     * Add footer adapter
     * @param adapter
     */
    fun addFooterAdapter(adapter: RecyclerView.Adapter<*>?)

    /**
     * Remove adapter.
     * @param adapter
     */
    fun removeAdapter(adapter: RecyclerView.Adapter<*>?)

    /**
     * Add item decoration.
     * @param decor
     */
    fun addItemDecoration(decor: RecyclerView.ItemDecoration)

    /**
     * Remove item decoration.
     * @param decor
     */
    fun removeItemDecoration(decor: RecyclerView.ItemDecoration)

    /**
     * Set item click listener.
     */
    fun setOnItemClickListener(listener: OnItemClickListener?) {}

    /**
     * Set item long click listener.
     */
    fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {}

    /**
     * Set recyclerView touch listener.
     */
    fun setOnRecyclerViewTouchListener(listener: OnRecyclerViewTouchListener?) {}

    /**
     * Refresh message list.
     */
    fun notifyDataSetChanged() {}

}