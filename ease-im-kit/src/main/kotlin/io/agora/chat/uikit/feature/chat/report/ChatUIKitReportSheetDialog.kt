package io.agora.chat.uikit.feature.chat.report

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseSheetFragmentDialog
import io.agora.chat.uikit.databinding.UikitLayoutChatReportBinding
import io.agora.chat.uikit.interfaces.SimpleListSheetItemSelectListener

class ChatUIKitReportSheetDialog(
    context: Context,
    itemList: MutableList<String>,
    private var onLeftButtonClickListener: (() -> Unit)? = {},
    private var onRightButtonClickListener: ((position: Int, reason:String?) -> Unit)? = null,
): ChatUIKitBaseSheetFragmentDialog<UikitLayoutChatReportBinding>(), View.OnClickListener {
    private var tagList:MutableList<String>
    private var mContext:Context
    private var reportAdapter: ReportAdapter? = null
    private var currentTag:String?=""
    private var selectedPosition: Int = -1

    init {
        this.mContext = context
        this.tagList = itemList
    }
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitLayoutChatReportBinding {
        return UikitLayoutChatReportBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.root?.let { setOnApplyWindowInsets(it) }
    }

    override fun initView() {
        binding?.let {
            reportAdapter = ReportAdapter(tagList)
            val layoutManager = LinearLayoutManager(mContext)
            it.rlSheetList.layoutManager = layoutManager
            it.rlSheetList.adapter = this.reportAdapter
            it.rightButton.isSelected = true
        }
    }

    override fun initListener() {
        binding?.let {
            it.leftButton.setOnClickListener(this)
            it.rightButton.setOnClickListener(this)
        }
        reportAdapter?.setSimpleListSheetItemClickListener(object : SimpleListSheetItemSelectListener{
            override fun onSelectListener(position: Int,tag:String) {
                currentTag = tag
                selectedPosition = position
            }
        })
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.leftButton -> {
                onLeftButtonClickListener?.let { it() }
                dismiss()
            }
            R.id.rightButton -> {
                onRightButtonClickListener?.let {
                    it(selectedPosition, currentTag)
                }
                dismiss()
            }
            else -> { dismiss() }
        }
    }

    class ReportAdapter(
        private val dataList: MutableList<String>?,
    ) : RecyclerView.Adapter<ReportAdapter.ViewHolder>(){
        private lateinit var listener: SimpleListSheetItemSelectListener
        private var selectPosition:Int = -1

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textView: TextView = itemView.findViewById(R.id.report_tag)
            val tagCb: CheckBox = itemView.findViewById(R.id.report_cb)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.uikit_layout_report_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int {
            return dataList?.size ?: 0
        }

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            dataList?.let {
                holder.textView.text = it[position]
                holder.tagCb.isChecked = selectPosition == position

                holder.tagCb.isClickable = false

                holder.itemView.setOnClickListener {
                    if (position == selectPosition) {
                        holder.tagCb.isChecked = false
                        selectPosition = -1
                    }else{
                        holder.tagCb.isChecked = true
                        selectPosition = position
                    }
                    notifyDataSetChanged()

                    if (holder.tagCb.isChecked){
                        listener.onSelectListener(position,dataList[selectPosition])
                    }
                }
            }
        }

        fun setSimpleListSheetItemClickListener(listener: SimpleListSheetItemSelectListener){
            this.listener = listener
        }

    }

}