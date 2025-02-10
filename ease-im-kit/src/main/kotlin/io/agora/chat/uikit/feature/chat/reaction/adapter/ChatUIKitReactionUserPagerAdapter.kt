package io.agora.chat.uikit.feature.chat.reaction.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.agora.chat.uikit.feature.chat.reaction.ChatUIKitReactionUserListFragment
import io.agora.chat.uikit.model.ChatUIKitReaction

class ChatUIKitReactionUserPagerAdapter(
    private val messageId: String,
    private val manager: FragmentManager,
    private val lifecycle: Lifecycle
): FragmentStateAdapter(manager, lifecycle) {
    private var reactions: List<ChatUIKitReaction>? = null

    fun setData(reactions: List<ChatUIKitReaction>?) {
        this.reactions = reactions
        notifyDataSetChanged()
    }

    fun getData(): List<ChatUIKitReaction>? {
        return reactions
    }

    override fun getItemCount(): Int {
        return reactions?.size ?: 0
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = ChatUIKitReactionUserListFragment()
        val bundle = reactions?.get(position)?.let {
            val bundle = Bundle()
            bundle.putString("reaction", it.identityCode)
            bundle.putString("messageId", messageId)
            bundle
        }
        fragment.arguments = bundle
        return fragment
    }
}