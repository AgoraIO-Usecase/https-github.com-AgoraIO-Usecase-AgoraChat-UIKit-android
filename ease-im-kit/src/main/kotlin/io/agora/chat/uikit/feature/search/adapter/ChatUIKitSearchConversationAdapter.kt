package io.agora.chat.uikit.feature.search.adapter

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.extensions.getChatroomName
import io.agora.chat.uikit.common.extensions.getGroupNameFromId
import io.agora.chat.uikit.databinding.UikitLayoutGroupSelectContactBinding
import io.agora.chat.uikit.model.ChatUIKitConversation
import io.agora.chat.uikit.model.isChatRoom
import io.agora.chat.uikit.model.isGroupChat
import io.agora.chat.uikit.provider.getSyncProfile
import io.agora.chat.uikit.provider.getSyncUser

class ChatUIKitSearchConversationAdapter: ChatUIKitBaseRecyclerViewAdapter<ChatUIKitConversation>() {
    private var query : String = ""

    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatUIKitConversation> =
        ChatUIKitSearchConversationViewHolder(
            UikitLayoutGroupSelectContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )

    fun searchText(query: String){
        this.query = query
    }

    inner class ChatUIKitSearchConversationViewHolder(private val binding: UikitLayoutGroupSelectContactBinding)
        : ViewHolder<ChatUIKitConversation>(binding = binding) {
        override fun setData(item: ChatUIKitConversation?, position: Int) {
            item?.run {
                with(binding) {
                    cbSelect.visibility = View.GONE

                    // Set conversation avatar and name.
                    if (item.isGroupChat()) {
                        emPresence.setUserAvatarData(avatar = R.drawable.uikit_default_group_avatar
                            ,nickname = null)
                        tvName.text = item.conversationId.getGroupNameFromId()
                        ChatUIKitClient.getGroupProfileProvider()?.getSyncProfile(item.conversationId)?.let { profile ->
                            if (profile.name.isNullOrEmpty().not()) {
                                tvName.text = profile.name
                            }
                            emPresence.getUserAvatar().load(profile.avatar) {
                                placeholder(R.drawable.uikit_default_group_avatar)
                                error(R.drawable.uikit_default_group_avatar)
                            }
                        }
                    } else if (item.isChatRoom()) {
                        emPresence.setUserAvatarData(avatar = R.drawable.ease_default_chatroom_avatar
                            ,nickname = null)
                        tvName.text = item.conversationId.getChatroomName()
                    } else {
                        emPresence.setUserAvatarData(avatar = R.drawable.uikit_default_avatar
                            ,nickname = item.conversationId)
                        tvName.text = item.conversationId
                        ChatUIKitClient.getUserProvider()?.getSyncUser(item.conversationId)?.let { profile ->
                            tvName.text = profile.getRemarkOrName()
                            emPresence.getUserAvatar().load(profile.avatar) {
                                placeholder(R.drawable.uikit_default_avatar)
                                error(R.drawable.uikit_default_avatar)
                            }
                        }
                    }


                    val title  = tvName.text.toString().trim()
                    val spannableString = SpannableString(title)
                    query.let {
                        val startIndex = title.indexOf(it, ignoreCase = true)
                        if (startIndex != -1) {
                            val endIndex = startIndex + it.length
                            spannableString.setSpan(
                                ForegroundColorSpan(ContextCompat.getColor(binding.root.context, R.color.ease_color_primary)),
                                startIndex, endIndex,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                            tvName.text = spannableString
                        }
                    }
                }
            }
        }
    }

}