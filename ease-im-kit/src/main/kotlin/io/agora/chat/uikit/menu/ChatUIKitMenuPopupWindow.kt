package io.agora.chat.uikit.menu

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.interfaces.OnMenuDismissListener
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.menu.select.ChatUIKitSelectPopAdapter
import io.agora.chat.uikit.menu.select.SelectUtils
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import java.util.Collections

@SuppressLint("RestrictedApi")
class ChatUIKitMenuPopupWindow(
    private val context: Context?,
    private val msgView: View,
    private val message: ChatMessage? = null
) : PopupWindow(),IMenu, OnItemClickListener {

    companion object{
        const val mPopSpanCount = 5
        private fun dp2px(num: Int): Int {
            return (num * Resources.getSystem().displayMetrics.density + 0.5f).toInt()
        }
    }

    private var rv_content: RecyclerView? = null
    private var iv_arrow_up: AppCompatImageView? = null
    private var iv_arrow: AppCompatImageView? = null
    private var ly_top: LinearLayoutCompat? = null
    private var ly_bottom: LinearLayoutCompat? = null
    private var reactionView:View? = null
    private var v_top_divider:View? = null
    private var v_bottom_divider:View? = null
    private var popWindowView:View? = null

    private var popupWindow: PopupWindow? = null
    private val adapter: ChatUIKitSelectPopAdapter by lazy { ChatUIKitSelectPopAdapter() }

    private val itemModels = ArrayList<ChatUIKitMenuItem>()
    private val itemMap: MutableMap<Int?, ChatUIKitMenuItem?> = HashMap()

    private var itemClickListener: OnMenuItemClickListener? = null
    private var dismissListener: OnMenuDismissListener? = null

    private var mWidth = 0  // 本pop的宽
    private var mHeight = 0 // 本pop的高
    private var mLayoutHeight = 0 // layout 填充区域高度

    init {
        popWindowView = LayoutInflater.from(context).inflate(R.layout.uikit_pop_operate, null)
        popWindowView?.let {
            rv_content = it.findViewById(R.id.rv_content)
            iv_arrow_up = it.findViewById(R.id.iv_arrow_up)
            iv_arrow = it.findViewById(R.id.iv_arrow)
            ly_top = it.findViewById(R.id.ll_menu_top)
            ly_bottom = it.findViewById(R.id.ll_menu_bottom)
            v_top_divider = it.findViewById(R.id.v_top_divider)
            v_bottom_divider = it.findViewById(R.id.v_bottom_divider)
        }

        rv_content?.let {
            it.layoutManager = LinearLayoutManager(context)
            it.setHasFixedSize(true)
            it.adapter = adapter
            adapter.setOnItemClickListener(this@ChatUIKitMenuPopupWindow)
            adapter.setData(itemModels)
        }

        popupWindow = PopupWindow(
            popWindowView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        // 使其聚集
        popupWindow?.isFocusable = true
        // 设置允许在外点击消失
        popupWindow?.isOutsideTouchable = true
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow?.setBackgroundDrawable(BitmapDrawable())
        // 动画
        popupWindow?.animationStyle = androidx.appcompat.R.style.Base_Animation_AppCompat_Dialog
    }

    override fun clear() {
        itemModels.clear()
        itemMap.clear()
        adapter.notifyDataSetChanged()
    }

    override fun dismissMenu() {
        dismiss()
    }

    override fun setMenuOrder(itemId: Int, order: Int) {
        if (itemMap.containsKey(itemId)) {
            itemMap[itemId]?.run {
                this.order = order
                sortByOrder(itemModels)
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun registerMenuItem(
        menuId: Int,
        order: Int,
        title: String,
        groupId: Int,
        isVisible: Boolean,
        resourceId: Int,
        titleColor: Int
    ) {
        if (!itemMap.containsKey(menuId) && isVisible) {
            val item = ChatUIKitMenuItem(menuId = menuId, order = order,
                title = title, groupId = groupId, isVisible = isVisible,
                resourceId = resourceId, titleColor = titleColor)
            itemMap[menuId] = item
            itemModels.add(item)
            sortByOrder(itemModels)
            adapter.notifyDataSetChanged()
        }
    }

    override fun registerMenus(menuItems: List<ChatUIKitMenuItem>) {
        if (menuItems.isEmpty()) return
        menuItems.filter {
            !itemMap.containsKey(it.menuId)
        }
            .filter { it.isVisible }
            .forEach {
                itemMap[it.menuId] = it
                itemModels.add(it)
            }
        sortByOrder(itemModels)
        adapter.notifyDataSetChanged()
    }

    override fun setOnMenuItemClickListener(listener: OnMenuItemClickListener?) {
       this.itemClickListener = listener
    }

    override fun setOnMenuDismissListener(listener: OnMenuDismissListener?) {
        this.dismissListener = listener
    }

    override fun dismiss() {
        super.dismiss()
        v_top_divider?.visibility = View.GONE
        v_bottom_divider?.visibility = View.GONE
        reactionView = null
        popupWindow?.dismiss()
        dismissListener?.onDismiss()
    }

    fun show(){
        if (itemModels.size == 0) return
        val data = mutableListOf<ChatUIKitMenuItem>()
        itemModels.map {
            if (it.isVisible)  {
                data.add(it)
            }
        }
        var size = data.size
        if (size > 10){ size = 10 }
        val deviceWidth = SelectUtils.displayWidth
        val deviceHeight = SelectUtils.displayHeight
        val statusHeight = SelectUtils.statusHeight
        val enableReaction = ChatUIKitClient.getConfig()?.chatConfig?.enableMessageReaction
        mLayoutHeight = if (enableReaction == true){ 44 }else{ 0 }

        //计算箭头显示的位置
        val location = IntArray(2)
        msgView.getLocationOnScreen(location)

        val msgViewWidth = msgView.width
        val msgViewHeight = msgView.height

        // view中心坐标 = view的位置 + view的宽度 / 2
        val centerWidth = location[0] + msgViewWidth / 2

        if (size > 5) {
            mWidth = dp2px( 54 * 5)
            // size > 5  2行展示 上下padding间距为12，item上下间距为4，item 高度为58
            if (enableReaction == true && message?.status() == ChatMessageStatus.SUCCESS ){
                mHeight = dp2px(12*2 + 4 * 2 + 58 * 2 + 7*2 + 8 + mLayoutHeight)
            }else{
                mHeight = dp2px(12 + 4 * 2 + 58 * 2 + 7 )
            }
        } else {
             if (enableReaction == true && reactionView != null && message?.status() == ChatMessageStatus.SUCCESS){
                mWidth =  dp2px(54 * 5)
            }else{
                mWidth =  dp2px(54 * size)
            }
            if (enableReaction == true && reactionView != null && message?.status() == ChatMessageStatus.SUCCESS ){
                mHeight = dp2px(12*2 + 4 + 58 + 7*2 + 8  + mLayoutHeight)
            }else{
                mHeight = dp2px(12 + 4 + 58 + 7  )
            }
        }
        // topUI true pop显示在顶部
        val topUI = location[1] > mHeight + statusHeight
        val arrowView: View?
        if (topUI) {
            iv_arrow?.visibility = View.VISIBLE
            iv_arrow_up?.visibility = View.GONE
            if (enableReaction == true && message?.status() == ChatMessageStatus.SUCCESS){
                ly_top?.visibility = View.VISIBLE
                ly_bottom?.visibility = View.GONE
            }
            arrowView = iv_arrow
        } else {
            iv_arrow_up?.visibility = View.VISIBLE
            iv_arrow?.visibility = View.GONE
            if (enableReaction == true && message?.status() == ChatMessageStatus.SUCCESS){
                ly_top?.visibility = View.GONE
                ly_bottom?.visibility = View.VISIBLE
            }
            arrowView = iv_arrow_up
        }
        if (size >= 5) {
            rv_content?.layoutManager =
                GridLayoutManager(context, mPopSpanCount, GridLayoutManager.VERTICAL, false)
            // x轴 （屏幕 - mWidth）/ 2
            val posX = (deviceWidth - mWidth) / 2
            // topUI ?
            // msgView的y轴 - popupWindow的高度
            // ：msgView的y轴 + msgView高度 + 2dp间距
            var posY = if (topUI) location[1] - mHeight  else location[1] + msgViewHeight + dp2px(2)
            if (!topUI // 反向的ui
                // 底部已经超过了 屏幕高度 - （弹窗高度 + 输入框）
                && location[1] + msgView.height > deviceHeight - dp2px(52 * 2 + 60)
            ) {
                // 显示在屏幕3/4高度
                posY = deviceHeight * 3 / 4
            }
            val arrX = - posX + location[0] + (msgViewWidth - 60) / 2 - dp2px(12 + 4)
            popupWindow?.showAtLocation(msgView, Gravity.NO_GRAVITY, posX, posY)
            arrowView?.translationX = arrX.toFloat()
        }else {
            rv_content?.layoutManager =
                GridLayoutManager(context, size, GridLayoutManager.VERTICAL, false)
            if (enableReaction == true && reactionView != null && message?.status() == ChatMessageStatus.SUCCESS){
                // x轴 （屏幕 - mWidth）/ 2
                var posX = centerWidth - mWidth / 2
                // 右侧的最大宽度
                val max = centerWidth + mWidth / 2
                if (posX < 0) {
                    posX = 0
                } else if (max > deviceWidth) {
                    posX = deviceWidth - mWidth
                }
                // topUI ?
                // msgView的y轴 - popupWindow的高度
                // ：msgView的y轴 + msgView高度 + 2dp间距
                var posY = if (topUI) location[1] - mHeight else location[1] + msgViewHeight + dp2px(2)
                if (!topUI // 反向的ui
                    // 底部已经超过了 屏幕高度 - （弹窗高度 + 输入框）
                    && location[1] + msgView.height > deviceHeight - dp2px(52 * 2 + 60)
                ) {
                    // 显示在屏幕3/4高度
                    posY = deviceHeight * 3 / 4
                }

                // view中心坐标 - pop坐标 - 16dp padding
                val arrX = - posX + location[0] + (msgViewWidth - 60) / 2 - dp2px(12 + 4)
                popupWindow?.showAtLocation(msgView, Gravity.NO_GRAVITY, posX, posY)
                arrowView?.translationX = arrX.toFloat()
            }else{
                var posX = centerWidth - mWidth / 2
                // 右侧的最大宽度
                val max = centerWidth + mWidth / 2
                if (posX < 0) {
                    posX = 0
                } else if (max > deviceWidth) {
                    // 计算超出屏幕的部分
                    val exceed = max - deviceWidth
                    posX =  centerWidth - mWidth  / 2 - exceed*2 - msgViewWidth/2
                }else{
                    posX = centerWidth - mWidth / 2 - dp2px(16)
                }
                // topUI ?
                // msgView的y轴 - popupWindow的高度
                // ：msgView的y轴 + msgView高度 + 4dp间距
                var posY = if (topUI) location[1] - mHeight else location[1] + msgViewHeight - dp2px(4)
                if (!topUI // 反向的ui
                    // 底部已经超过了 屏幕高度 - （弹窗高度 + 输入框）
                    && location[1] + msgView.height > deviceHeight - dp2px(52 * 2 + 60)
                ) {
                    // 显示在屏幕3/4高度
                    posY = deviceHeight * 3 / 4
                }
                val arrX = - posX + location[0] + (msgViewWidth - 60) / 2 - dp2px(12 + 4)
                popupWindow?.showAtLocation(msgView, Gravity.NO_GRAVITY, posX, posY)
                arrowView?.translationX = arrX.toFloat()
            }
        }
    }

    fun addReactionView(view: View){
        this.reactionView = view
        val statusHeight = SelectUtils.statusHeight

        val location = IntArray(2)
        msgView.getLocationOnScreen(location)
        val topUI = location[1] > mHeight + statusHeight

        if (topUI) {
            ly_bottom?.removeAllViews()
            ly_top?.let { layout->
                layout.removeAllViews()
                layout.addView(view)
            }
            reactionView?.let {
                v_top_divider?.visibility = View.VISIBLE
                v_bottom_divider?.visibility = View.GONE
            }
        } else {
            ly_top?.removeAllViews()
            ly_bottom?.let { layout->
                layout.removeAllViews()
                layout.addView(view)
            }
            reactionView?.let {
                v_top_divider?.visibility = View.GONE
                v_bottom_divider?.visibility = View.VISIBLE
            }
        }
    }

    fun clearReactionView(){
        ly_top?.removeAllViews()
        ly_bottom?.removeAllViews()
        ly_top?.visibility = View.GONE
        ly_bottom?.visibility = View.GONE
        v_top_divider?.visibility = View.GONE
        v_bottom_divider?.visibility = View.GONE
        reactionView = null
    }

    fun unregisterListener(){
        dismissListener = null
        itemClickListener = null
    }


    private fun sortByOrder(itemModels: List<ChatUIKitMenuItem>) {
        Collections.sort(itemModels) { o1, o2 ->
            val `val`: Int = o1.order - o2.order
            if (`val` > 0) {
                1
            } else if (`val` == 0) {
                0
            } else {
                -1
            }
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        dismiss()
        val item = itemModels[position]
        itemClickListener?.onMenuItemClick(item, position)
    }


}