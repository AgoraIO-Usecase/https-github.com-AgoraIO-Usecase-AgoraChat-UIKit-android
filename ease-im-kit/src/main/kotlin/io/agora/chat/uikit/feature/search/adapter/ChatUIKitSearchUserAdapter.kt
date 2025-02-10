package io.agora.chat.uikit.feature.search.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.databinding.UikitLayoutGroupSelectContactBinding
import io.agora.chat.uikit.interfaces.OnContactSelectedListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncUser

class ChatUIKitSearchUserAdapter(
    private val isShowCheckBox:Boolean = false
): ChatUIKitBaseRecyclerViewAdapter<ChatUIKitUser>() {
    private var selectedListener:OnContactSelectedListener? = null
    private var query : String = ""

    companion object{
        private var checkedList:MutableList<String> = mutableListOf()
    }

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitUser> =
        ChatUIKitSearchUserHolder(
            isShowCheckBox,
            UikitLayoutGroupSelectContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    override fun onBindViewHolder(holder: ViewHolder<ChatUIKitUser>, position: Int) {
        if (holder is ChatUIKitSearchUserHolder){
            holder.searchText(query)
            selectedListener?.let {
                holder.setCheckBoxSelectListener(it)
            }
        }
        super.onBindViewHolder(holder, position)
    }

    fun searchText(query: String){
        this.query = query
    }

    fun setCheckBoxSelectListener(listener: OnContactSelectedListener){
        this.selectedListener = listener
    }

    fun resetSelect(){
        checkedList.clear()
        notifyDataSetChanged()
    }

    class ChatUIKitSearchUserHolder(
        private val isShowCheckBox:Boolean,
        private val viewBinding: UikitLayoutGroupSelectContactBinding,
    ): ViewHolder<ChatUIKitUser>(binding = viewBinding) {
        private var position:Int = 0
        private var user:ChatUIKitUser? = null
        private var selectedListener: OnContactSelectedListener?=null
        private var queryContent:String = ""

        override fun setData(item: ChatUIKitUser?, position: Int) {
            this.user = item
            this.position = position

            item?.run {
                with(viewBinding) {

                    if (isShowCheckBox){
                        cbSelect.visibility = View.VISIBLE

                        itemLayout.setOnClickListener{ view->
                            val isChecked = cbSelect.isChecked
                            cbSelect.isChecked = !isChecked
                        }

                        cbSelect.isChecked = checkedList.isNotEmpty() && isContains(checkedList,item.userId)

                        cbSelect.setOnCheckedChangeListener{ view,isChecked->
                            item.let { u->
                                if (isChecked) {
                                    checkedList.let { data ->
                                        if (!data.contains(u.userId)){
                                            data.add(u.userId)
                                        } else { }
                                    }
                                }else{
                                    checkedList.remove(u.userId)
                                }
                            }
                            selectedListener?.onContactSelectedChanged(view, checkedList)
                        }
                    }else{
                        cbSelect.visibility = View.GONE
                    }

                    emPresence.setUserAvatarData(item.toProfile())
                    var title  = item.nickname ?: item.userId

                    ChatUIKitClient.getUserProvider()?.getSyncUser(item.userId)?.let {
                        emPresence.setUserAvatarData(it)
                        title = it.getRemarkOrName()
                    }

                    val spannableString = SpannableString(title)
                    queryContent.let {
                        val startIndex = title.indexOf(it, ignoreCase = true)
                        if (startIndex != -1) {
                            val endIndex = startIndex + it.length
                            ChatUIKitClient.getContext()?.let {context->
                                spannableString.setSpan(
                                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.ease_color_primary)),
                                    startIndex, endIndex,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        }
                    }

                    tvName.text = spannableString

                }
            }
        }

        fun searchText(query: String){
            this.queryContent = query
        }

        fun setCheckBoxSelectListener(listener: OnContactSelectedListener){
            this.selectedListener = listener
        }

        private fun isContains(data: MutableList<String>?, username: String): Boolean {
            return data != null && data.contains(username)
        }

    }


}