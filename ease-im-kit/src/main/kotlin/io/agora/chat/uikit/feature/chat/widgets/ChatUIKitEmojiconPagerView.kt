package io.agora.chat.uikit.feature.chat.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import io.agora.chat.uikit.R
import io.agora.chat.uikit.databinding.UikitChatEmojiExpressionGridviewBinding
import io.agora.chat.uikit.feature.chat.adapter.ChatUIKitEmojiGridAdapter
import io.agora.chat.uikit.feature.chat.adapter.EmojiconPagerAdapter
import io.agora.chat.uikit.common.helper.ChatUIKitEmojiHelper
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.model.ChatUIKitEmojicon
import io.agora.chat.uikit.model.ChatUIKitEmojiconGroupEntity
import io.agora.chat.uikit.model.ChatUIKitEmojicon.Type
import io.agora.chat.uikit.widget.ChatUIKitDividerGridItemDecoration

class ChatUIKitEmojiconPagerView @JvmOverloads constructor(
    private val context: Context,
    attrs: AttributeSet? = null
) : ViewPager(
    context, attrs
) {
    private var groupEntities: List<ChatUIKitEmojiconGroupEntity>? = null
    private var pagerAdapter: PagerAdapter? = null
    private val emojiconRows = 3
    private var emojiconColumns = 7
    private val bigEmojiconRows = 2
    private var bigEmojiconColumns = 4
    private var firstGroupPageSize = 0
    private var maxPageCount = 0
    private var previousPagerPosition = 0
    private var pagerViewListener: ChatUIKitEmojiconPagerViewListener? = null
    private val viewpages: MutableList<View> by lazy { mutableListOf<View>() }
    fun init(
        emojiconGroupList: List<ChatUIKitEmojiconGroupEntity>?,
        emijiconColumns: Int,
        bigEmojiconColumns: Int
    ) {
        if (emojiconGroupList == null) {
            throw RuntimeException("emojiconGroupList is null")
        }
        groupEntities = emojiconGroupList
        emojiconColumns = emijiconColumns
        this.bigEmojiconColumns = bigEmojiconColumns
        for (i in groupEntities!!.indices) {
            val group = groupEntities!![i]
            val groupEmojicons = group.emojiconList
            val gridViews = getGroupGridViews(group)
            if (i == 0) {
                firstGroupPageSize = gridViews.size
            }
            maxPageCount = Math.max(gridViews.size, maxPageCount)
            viewpages.addAll(gridViews)
        }
        pagerAdapter = EmojiconPagerAdapter(viewpages)
        adapter = pagerAdapter
        setOnPageChangeListener(EmojiPagerChangeListener())
        if (pagerViewListener != null) {
            pagerViewListener!!.onPagerViewInited(maxPageCount, firstGroupPageSize)
        }
    }

    fun setPagerViewListener(pagerViewListener: ChatUIKitEmojiconPagerViewListener?) {
        this.pagerViewListener = pagerViewListener
    }

    /**
     * set emojicon group position
     * @param position
     */
    fun setGroupPosition(position: Int) {
        if (adapter != null && position >= 0 && position < groupEntities!!.size) {
            var count = 0
            for (i in 0 until position) {
                count += getPageSize(groupEntities!![i])
            }
            currentItem = count
        }
    }

    /**
     * get emojicon group gridview list
     * @param groupEntity
     * @return
     */
    fun getGroupGridViews(groupEntity: ChatUIKitEmojiconGroupEntity): List<View> {
        var emojiconList: List<ChatUIKitEmojicon> = groupEntity.emojiconList?.filterNotNull() ?: mutableListOf()
        val emojiType = groupEntity.type
        val views: MutableList<View> = ArrayList()

        // Set viewPager's item view
        val pageViewBinding = UikitChatEmojiExpressionGridviewBinding.inflate(LayoutInflater.from(context))

        var columns = if (emojiType === Type.BIG_EXPRESSION) {
            bigEmojiconColumns
        } else {
            emojiconColumns
        }
        pageViewBinding.gridview.layoutManager = GridLayoutManager(context, columns)
        val gridAdapter = ChatUIKitEmojiGridAdapter()
        pageViewBinding.gridview.adapter = gridAdapter
        pageViewBinding.gridview.addItemDecoration(ChatUIKitDividerGridItemDecoration(context, R.drawable.uikit_chat_emoji_expression_gridview_divider))
        // To prevent the emoji from being obscured
        val addItems =
            if (emojiconList.size % columns == 0) columns + 1 else columns * 2 - emojiconList.size % columns + 1
        val list: MutableList<ChatUIKitEmojicon> = ArrayList()
        list.addAll(emojiconList)
        for (i in 0 until addItems) {
            val icon = ChatUIKitEmojicon()
            icon.enableClick = false
            list.add(icon)
        }
        gridAdapter.setData(list)
        if (emojiType === Type.BIG_EXPRESSION) {
            pageViewBinding.llAction.visibility = GONE
        } else {
            pageViewBinding.llAction.visibility = VISIBLE
            val isShowSendButton = context.resources.getBoolean(R.bool.ease_input_show_send_button)
            // When show send button, dismiss the emoji send button
            if (isShowSendButton) pageViewBinding.btnEmojiSend.visibility = GONE
        }
        pageViewBinding.btnEmojiDelete.setOnClickListener { pagerViewListener?.onDeleteImageClicked() }
        pageViewBinding.btnEmojiSend.setOnClickListener { pagerViewListener?.onSendIconClicked() }
        gridAdapter.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(view: View?, position: Int) {
                val emojicon = gridAdapter.getItem(position)
                emojicon?.run {
                    if (emojiText == ChatUIKitEmojiHelper.DELETE_KEY) {
                        pagerViewListener?.onDeleteImageClicked()
                    } else {
                        pagerViewListener?.onExpressionClicked(emojicon)
                    }
                }
            }

        })
        views.add(pageViewBinding.root)
        return views
    }

    /**
     * add emojicon group
     * @param groupEntity
     */
    fun addEmojiconGroup(groupEntity: ChatUIKitEmojiconGroupEntity, notifyDataChange: Boolean) {
        val pageSize = getPageSize(groupEntity)
        if (pageSize > maxPageCount) {
            maxPageCount = pageSize
            if (pagerViewListener != null && pagerAdapter != null) {
                pagerViewListener!!.onGroupMaxPageSizeChanged(maxPageCount)
            }
        }
        viewpages!!.addAll(getGroupGridViews(groupEntity))
        if (pagerAdapter != null && notifyDataChange) {
            pagerAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * remove emojicon group
     * @param position
     */
    fun removeEmojiconGroup(position: Int) {
        if (position > groupEntities!!.size - 1) {
            return
        }
        if (pagerAdapter != null) {
            pagerAdapter!!.notifyDataSetChanged()
        }
    }

    /**
     * get size of pages
     * @param groupEntity
     * @return
     */
    private fun getPageSize(groupEntity: ChatUIKitEmojiconGroupEntity): Int {
        val emojiconList = groupEntity.emojiconList
        var itemSize = emojiconColumns * emojiconRows - 1
        val totalSize = emojiconList?.size ?: 0
        val emojiType: Type? = groupEntity.type
        if (emojiType === Type.BIG_EXPRESSION) {
            itemSize = bigEmojiconColumns * bigEmojiconRows
        }
        return if (totalSize % itemSize == 0) totalSize / itemSize else totalSize / itemSize + 1
    }

    private inner class EmojiPagerChangeListener : OnPageChangeListener {
        override fun onPageSelected(position: Int) {
            var endSize = 0
            var groupPosition = 0
            for (groupEntity in groupEntities!!) {
                val groupPageSize = getPageSize(groupEntity)
                //if the position is in current group
                if (endSize + groupPageSize > position) {
                    //this is means user swipe to here from previous page
                    if (previousPagerPosition - endSize < 0) {
                        if (pagerViewListener != null) {
                            pagerViewListener!!.onGroupPositionChanged(groupPosition, groupPageSize)
                            pagerViewListener!!.onGroupPagePostionChangedTo(0)
                        }
                        break
                    }
                    //this is means user swipe to here from back page
                    if (previousPagerPosition - endSize >= groupPageSize) {
                        if (pagerViewListener != null) {
                            pagerViewListener!!.onGroupPositionChanged(groupPosition, groupPageSize)
                            pagerViewListener!!.onGroupPagePostionChangedTo(position - endSize)
                        }
                        break
                    }

                    //page changed
                    if (pagerViewListener != null) {
                        pagerViewListener!!.onGroupInnerPagePostionChanged(
                            previousPagerPosition - endSize,
                            position - endSize
                        )
                    }
                    break
                }
                groupPosition++
                endSize += groupPageSize
            }
            previousPagerPosition = position
        }

        override fun onPageScrollStateChanged(arg0: Int) {}
        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
    }

    interface ChatUIKitEmojiconPagerViewListener {
        /**
         * pagerview initialized
         * @param groupMaxPageSize --max pages size
         * @param firstGroupPageSize-- size of first group pages
         */
        fun onPagerViewInited(groupMaxPageSize: Int, firstGroupPageSize: Int)

        /**
         * group position changed
         * @param groupPosition--group position
         * @param pagerSizeOfGroup--page size of group
         */
        fun onGroupPositionChanged(groupPosition: Int, pagerSizeOfGroup: Int)

        /**
         * page position changed
         * @param oldPosition
         * @param newPosition
         */
        fun onGroupInnerPagePostionChanged(oldPosition: Int, newPosition: Int)

        /**
         * group page position changed
         * @param position
         */
        fun onGroupPagePostionChangedTo(position: Int)

        /**
         * max page size changed
         * @param maxCount
         */
        fun onGroupMaxPageSizeChanged(maxCount: Int)
        fun onDeleteImageClicked()
        fun onExpressionClicked(emojicon: ChatUIKitEmojicon?)

        /**
         * Click send icon which you can send your emoji in editText
         */
        fun onSendIconClicked()
    }
}