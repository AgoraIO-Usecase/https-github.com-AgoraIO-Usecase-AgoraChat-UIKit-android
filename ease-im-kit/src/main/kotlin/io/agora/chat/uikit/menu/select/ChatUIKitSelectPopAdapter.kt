package io.agora.chat.uikit.menu.select

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.databinding.UikitItemSelectTextPopBinding
import io.agora.chat.uikit.model.ChatUIKitMenuItem

class ChatUIKitSelectPopAdapter : ChatUIKitBaseRecyclerViewAdapter<ChatUIKitMenuItem>(){

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitMenuItem> {
        return SelectMenuViewHolder(UikitItemSelectTextPopBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitMenuItem>, position: Int) {
        super.onBindViewHolder(holder, position)
    }

    private inner class SelectMenuViewHolder(
        private val viewBinding:UikitItemSelectTextPopBinding
    ) : ViewHolder<ChatUIKitMenuItem>(binding = viewBinding) {

        override fun initView(viewBinding: ViewBinding?) {
            super.initView(viewBinding)
        }

        @SuppressLint("RestrictedApi")
        override fun setData(item: ChatUIKitMenuItem?, position: Int) {
            item?.let { menu->
                viewBinding.run {
                    if (menu.resourceId != -1) {
                        ivPopIcon.setImageResource(menu.resourceId)
                        ivPopIcon.visibility = View.VISIBLE
                    } else {
                        ivPopIcon.visibility = View.GONE
                    }
                    tvPopFunc.text = menu.title
                    if (menu.titleColor != -1) {
                        tvPopFunc.setTextColor(menu.titleColor)
                        ivPopIcon.supportImageTintList = ColorStateList.valueOf(menu.titleColor)
                    } else {
                        ivPopIcon.supportImageTintList = ColorStateList.valueOf(
                            ContextCompat.getColor(mContext!!
                                , R.color.ease_neutral_30))
                    }
                }
            }
        }

    }
}