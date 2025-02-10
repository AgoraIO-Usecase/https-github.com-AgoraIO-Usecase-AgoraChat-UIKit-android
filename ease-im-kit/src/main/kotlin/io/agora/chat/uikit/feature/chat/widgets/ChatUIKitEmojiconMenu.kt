package io.agora.chat.uikit.feature.chat.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import io.agora.chat.uikit.R
import io.agora.chat.uikit.databinding.UikitWidgetChatEmojiconBinding
import io.agora.chat.uikit.feature.chat.interfaces.ChatUIKitEmojiconMenuListener
import io.agora.chat.uikit.feature.chat.interfaces.IChatEmojiconMenu
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitEmojiconPagerView.ChatUIKitEmojiconPagerViewListener
import io.agora.chat.uikit.model.ChatUIKitDefaultEmojiIconData
import io.agora.chat.uikit.model.ChatUIKitEmojicon
import io.agora.chat.uikit.model.ChatUIKitEmojiconGroupEntity

class ChatUIKitEmojiconMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), IChatEmojiconMenu {

    private val binding: UikitWidgetChatEmojiconBinding by lazy {
        UikitWidgetChatEmojiconBinding.inflate(LayoutInflater.from(context), this, true) }
    private var emojiconColumns = 0
    private var bigEmojiconColumns = 0
    private val emojiconGroupList: MutableList<ChatUIKitEmojiconGroupEntity> = ArrayList()
    private var listener: ChatUIKitEmojiconMenuListener? = null

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitEmojiconMenu)
        emojiconColumns = ta.getInt(R.styleable.ChatUIKitEmojiconMenu_emojiconColumns, defaultColumns)
        bigEmojiconColumns =
            ta.getInt(R.styleable.ChatUIKitEmojiconMenu_bigEmojiconRows, defaultBigColumns)
        ta.recycle()
    }

    @JvmOverloads
    fun init(groupEntities: MutableList<ChatUIKitEmojiconGroupEntity>? = null) {
        val entities = mutableListOf<ChatUIKitEmojiconGroupEntity>()
        if (groupEntities == null || groupEntities.size == 0) {
            entities.add(
                ChatUIKitEmojiconGroupEntity(
                    R.drawable.emoji_1,
                    ChatUIKitDefaultEmojiIconData.data.toList()
                )
            )
        } else {
            entities.addAll(groupEntities)
        }
        entities.forEach { groupEntity ->
            emojiconGroupList.add(groupEntity)
            binding.tabBar.addTab(groupEntity.icon)
        }
        binding.pagerView.setPagerViewListener(EmojiconPagerViewListener())
        binding.pagerView.init(emojiconGroupList, emojiconColumns, bigEmojiconColumns)
        binding.tabBar.setTabBarItemClickListener(object : ChatUIKitEmojiScrollTabBar.ChatUIKitScrollTabBarItemClickListener {
            override fun onItemClick(position: Int) {
                binding.pagerView.setGroupPosition(position)
            }
        })
    }

    /**
     * add emojicon group
     * @param groupEntity
     */
    override fun addEmojiconGroup(groupEntity: ChatUIKitEmojiconGroupEntity) {
        emojiconGroupList.add(groupEntity)
        binding.pagerView.addEmojiconGroup(groupEntity, true)
        binding.tabBar.addTab(groupEntity.icon)
    }

    /**
     * add emojicon group list
     * @param groupEntitieList
     */
    override fun addEmojiconGroup(groupEntitieList: List<ChatUIKitEmojiconGroupEntity>?) {
        if (groupEntitieList.isNullOrEmpty()) {
            return
        }
        val list = arrayListOf<ChatUIKitEmojiconGroupEntity>()
        list.addAll(groupEntitieList)
        list.forEachIndexed { i, groupEntity ->
            emojiconGroupList.add(groupEntity)
            binding.pagerView.addEmojiconGroup(
                groupEntity,
                i == groupEntitieList.size - 1
            )
            binding.tabBar.addTab(groupEntity.icon)
        }
    }

    /**
     * remove emojicon group
     * @param position
     */
    override fun removeEmojiconGroup(position: Int) {
        emojiconGroupList.removeAt(position)
        binding.pagerView.removeEmojiconGroup(position)
        binding.tabBar.removeTab(position)
    }

    override fun setTabBarVisibility(isVisible: Boolean) {
        if (!isVisible) {
            binding.tabBar.visibility = GONE
        } else {
            binding.tabBar.visibility = VISIBLE
        }
    }

    override fun setEmojiconMenuListener(listener: ChatUIKitEmojiconMenuListener?) {
        this.listener = listener
    }

    private inner class EmojiconPagerViewListener : ChatUIKitEmojiconPagerViewListener {
        override fun onPagerViewInited(groupMaxPageSize: Int, firstGroupPageSize: Int) {
            binding.indicatorView.init(groupMaxPageSize)
            binding.indicatorView.updateIndicator(firstGroupPageSize)
            binding.tabBar.selectedTo(0)
        }

        override fun onGroupPositionChanged(groupPosition: Int, pagerSizeOfGroup: Int) {
            binding.indicatorView.updateIndicator(pagerSizeOfGroup)
            binding.tabBar.selectedTo(groupPosition)
        }

        override fun onGroupInnerPagePostionChanged(oldPosition: Int, newPosition: Int) {
            binding.indicatorView.selectTo(oldPosition, newPosition)
        }

        override fun onGroupPagePostionChangedTo(position: Int) {
            binding.indicatorView.selectTo(position)
        }

        override fun onGroupMaxPageSizeChanged(maxCount: Int) {
            binding.indicatorView.updateIndicator(maxCount)
        }

        override fun onDeleteImageClicked() {
            listener?.onDeleteImageClicked()
        }

        override fun onExpressionClicked(emojicon: ChatUIKitEmojicon?) {
            listener?.onExpressionClicked(emojicon)
        }

        override fun onSendIconClicked() {
            listener?.onSendIconClicked()
        }
    }

    companion object {
        private const val defaultColumns = 7
        private const val defaultBigColumns = 4
    }
}