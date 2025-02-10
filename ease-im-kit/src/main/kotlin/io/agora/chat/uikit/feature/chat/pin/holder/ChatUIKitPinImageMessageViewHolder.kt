package io.agora.chat.uikit.feature.chat.pin.holder

import android.annotation.SuppressLint
import android.view.View
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.getMessageDigest
import io.agora.chat.uikit.databinding.UikitPinlistImageLayoutBinding
import io.agora.chat.uikit.provider.getSyncUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class ChatUIKitPinImageMessageViewHolder (
    private val mItemSubViewListener: ChatUIKitBaseRecyclerViewAdapter.OnItemSubViewClickListener?,
    val viewBinding: UikitPinlistImageLayoutBinding
): ChatUIKitBaseRecyclerViewAdapter.ViewHolder<ChatMessage>(binding = viewBinding) {
    private var isConfirm:Boolean = false
    @SuppressLint("SetTextI18n")
    override fun setData(item: ChatMessage?, position: Int) {
        viewBinding.run {
            item?.let { message->
                tvState.text = itemView.context.getString(R.string. uikit_pin_list_item_delete)
                isConfirm = false
                val operatorId = message.pinnedInfo()?.operatorId()
                val pinTime = message.pinnedInfo()?.pinTime()
                val operatorUserInfo = ChatUIKitClient.getUserProvider()?.getSyncUser(operatorId)
                val fromUserInfo = ChatUIKitClient.getUserProvider()?.getSyncUser(message.from)

                tvFrom.text = "${operatorUserInfo?.getRemarkOrName()?:kotlin.run { operatorId }} " +
                        "pinned ${fromUserInfo?.getRemarkOrName()?:kotlin.run { message.from }} 's message"

                tvContent.text = message.getMessageDigest(itemView.context)

                val sdf = SimpleDateFormat("MM-dd, HH:mm")
                sdf.timeZone = TimeZone.getTimeZone("GMT+8")
                pinTime?.let {
                    val formattedDate = sdf.format(Date(pinTime))
                    tvTime.text = formattedDate
                }

                tvState.setOnClickListener(View.OnClickListener { v ->
                    if (isConfirm){
                        mItemSubViewListener?.onItemSubViewClick(v, position)
                    }else{
                        tvState.text = itemView.context.getString(R.string.uikit_pin_list_item_confirm_delete)
                    }
                    isConfirm = !isConfirm
                })
            }
        }
    }
}