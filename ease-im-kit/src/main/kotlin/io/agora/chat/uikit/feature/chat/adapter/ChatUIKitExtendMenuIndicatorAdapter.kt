package io.agora.chat.uikit.feature.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.R

class ChatUIKitExtendMenuIndicatorAdapter :
    RecyclerView.Adapter<ChatUIKitExtendMenuIndicatorAdapter.ViewHolder>() {
    private var pageCount = 0
    private var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.uikit_chat_extend_indicator_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.indicator.isChecked = selectedPosition == position
    }

    override fun getItemCount(): Int {
        return if (pageCount == 1) 0 else pageCount
    }

    fun setPageCount(pageCount: Int) {
        this.pageCount = pageCount
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val indicator: CheckBox

        init {
            indicator = itemView as CheckBox
        }
    }
}