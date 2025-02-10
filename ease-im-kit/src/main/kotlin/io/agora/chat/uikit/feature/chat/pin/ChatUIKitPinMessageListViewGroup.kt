package io.agora.chat.uikit.feature.chat.pin

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitPinMessageListAdapter
import io.agora.chat.uikit.interfaces.OnItemClickListener

class ChatUIKitPinMessageListViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context, attrs, defStyleAttr
){
    private var constraintLayout: ConstraintLayout? = null
    private var recyclerView: RecyclerView? = null
    private var pinAdapter: ChatUIKitPinMessageListAdapter? = null
    private var itemClickListener: OnPinItemClickListener? = null
    private var viewStatusChangeListener:OnPinViewStatusChangListener? = null
    private var tvCount: TextView? = null
    private var clBottom: View? = null
    private var itemSubViewClickListener: ChatUIKitBaseRecyclerViewAdapter.OnItemSubViewClickListener? = null

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        isClickable = true
        setBackgroundColor(Color.parseColor("#80000000"))
        constraintLayout = LayoutInflater.from(getContext())
            .inflate(R.layout.uikit_pin_message_list_view_group, this, false) as ConstraintLayout
        addView(constraintLayout)
        tvCount = findViewById<TextView>(R.id.tv_count)
        recyclerView = findViewById(R.id.rv_list)
        clBottom = findViewById<View>(R.id.cl_bottom)

        pinAdapter = ChatUIKitPinMessageListAdapter()
        val space = 8.dpToPx(context)
        val itemDecoration = ChatUIKitPinItemSpaceDecoration(space)
        recyclerView?.addItemDecoration(itemDecoration)
        recyclerView?.layoutManager = LinearLayoutManager(getContext())
        pinAdapter?.setOnItemClickListener(object: OnItemClickListener{
            override fun onItemClick(view: View?, position: Int) {
                itemClickListener?.onItemClick(pinAdapter?.getItem(position))
            }

        })
        pinAdapter?.setOnItemSubViewClickListener(object : ChatUIKitBaseRecyclerViewAdapter.OnItemSubViewClickListener{
                override fun onItemSubViewClick(view: View?, position: Int) {
                    itemSubViewClickListener?.onItemSubViewClick(view, position)
                }
            })
        recyclerView?.adapter = pinAdapter

    }

    var startY: Long = 0
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> startY = event.y.toLong()
            MotionEvent.ACTION_UP -> {
                recyclerView?.let {
                    if (!it.canScrollVertically(-1) && startY - event.y > 20) {
                        visibility = GONE
                        viewStatusChangeListener?.onHidePinView()
                        return true
                    }
                    if (event.x < 0 || event.x > width || event.y < 0 || event.y > height || event.y > clBottom?.top!!) {
                        visibility = GONE
                        viewStatusChangeListener?.onHidePinView()
                        return true
                    }
                }

            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun updatePinCount(){
        pinAdapter?.let {
            tvCount?.text = "${it.mData?.size} Pin Message"
            it.notifyDataSetChanged()
        }

    }

    private fun setData(data: MutableList<ChatMessage>?) {
        data?.let {
            pinAdapter?.setData(it)
            updatePinCount()
        }
    }

    fun setOnItemClickListener(listener: OnPinItemClickListener?) {
        itemClickListener = listener
    }

    fun setOnItemSubViewClickListener(listener: ChatUIKitBaseRecyclerViewAdapter.OnItemSubViewClickListener?) {
        itemSubViewClickListener = listener
    }

    fun setPinViewStatusChangeListener(listener:OnPinViewStatusChangListener?){
        viewStatusChangeListener = listener
    }

    fun show(messages: MutableList<ChatMessage>?) {
        visibility = VISIBLE
        setData(messages)
    }

    fun removeData(message: ChatMessage?) {
        pinAdapter?.let {
            it.mData?.map { msg->
                if (msg.msgId.equals(message?.msgId)) {
                    removeData(msg)
                }
            }
            updatePinCount()
            if (it.mData.isNullOrEmpty()) {
                visibility = GONE
                viewStatusChangeListener?.onHidePinView()
            }
        }
    }

    fun addData(message: ChatMessage){
        pinAdapter?.addData(0,message)
        updatePinCount()
    }

    fun setConstraintLayoutMaxHeight(height: Int) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)
        val recyclerViewId: Int = R.id.rv_list
        val rvHeight = height - tvCount!!.height - clBottom!!.height
        constraintSet.constrainMaxHeight(recyclerViewId, rvHeight)
        constraintSet.applyTo(constraintLayout)
    }

    interface OnPinItemClickListener {
        fun onItemClick(message: ChatMessage?)
    }

    interface OnPinViewStatusChangListener{
        fun onHidePinView(){}
    }

}