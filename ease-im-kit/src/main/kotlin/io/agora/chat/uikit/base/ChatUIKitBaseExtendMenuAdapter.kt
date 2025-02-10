package io.agora.chat.uikit.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class ChatUIKitBaseExtendMenuAdapter<VH : RecyclerView.ViewHolder?, T> :
    RecyclerView.Adapter<VH>() {
    protected var mData: List<T>? = null
    fun setData(data: List<T>?) {
        mData = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (mData == null) 0 else mData!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH & Any {
        val view = LayoutInflater.from(parent.context).inflate(itemLayoutId, parent, false)
        return easeCreateViewHolder(view)
    }

    protected abstract val itemLayoutId: Int

    /**
     * Get ViewHolder
     * @param view
     * @return
     */
    protected abstract fun easeCreateViewHolder(view: View?): VH & Any
}