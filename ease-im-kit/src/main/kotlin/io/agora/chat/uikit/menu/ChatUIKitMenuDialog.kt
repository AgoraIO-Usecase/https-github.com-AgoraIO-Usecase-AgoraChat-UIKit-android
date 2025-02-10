package io.agora.chat.uikit.menu

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import io.agora.chat.uikit.base.ChatUIKitBaseSheetFragmentDialog
import io.agora.chat.uikit.databinding.UikitDialogMenuBinding
import io.agora.chat.uikit.interfaces.OnItemClickListener
import io.agora.chat.uikit.interfaces.OnMenuDismissListener
import io.agora.chat.uikit.interfaces.OnMenuItemClickListener
import io.agora.chat.uikit.model.ChatUIKitMenuItem
import java.util.Collections

open class ChatUIKitMenuDialog
    : ChatUIKitBaseSheetFragmentDialog<UikitDialogMenuBinding>(),
    IMenu, OnItemClickListener {

    private var showCancelButton: Boolean = true
    private val itemModels = ArrayList<ChatUIKitMenuItem>()
    private val itemMap: MutableMap<Int?, ChatUIKitMenuItem?> = HashMap()
    private var itemClickListener: OnMenuItemClickListener? = null
    private var dismissListener: OnMenuDismissListener? = null

    private val adapter: ChatUIKitMenuAdapter by lazy { ChatUIKitMenuAdapter() }
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): UikitDialogMenuBinding? {
        return UikitDialogMenuBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.run {
            rvDialogList.layoutManager = LinearLayoutManager(context)
            rvDialogList.setHasFixedSize(true)
            rvDialogList.adapter = adapter
            adapter.setData(itemModels)
            adapter.setOnItemClickListener(this@ChatUIKitMenuDialog)
            btnCancel.setOnClickListener { dismiss() }
            btnCancel.visibility = if (showCancelButton) View.VISIBLE else View.GONE
            menuDivider.visibility = if (showCancelButton) View.VISIBLE else View.GONE
        }
    }

    fun getMenuAdapter(): ChatUIKitMenuAdapter {
        return adapter
    }

    /**
     * Set whether to show cancel button.
     */
    fun showCancel(show: Boolean) {
        this.showCancelButton = show
        binding?.let {
            it.btnCancel.visibility = if (showCancelButton) View.VISIBLE else View.GONE
            it.menuDivider.visibility = if (showCancelButton) View.VISIBLE else View.GONE
        }
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

    override fun onItemClick(view: View?, position: Int) {
        dismiss()
        val item = itemModels[position]
        itemClickListener?.onMenuItemClick(item, position)
    }

    override fun dismiss() {
        super.dismiss()
        dismissListener?.onDismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismissListener?.onDismiss()
    }

    fun unregisterListener(){
        dismissListener = null
        itemClickListener = null
    }

    /**
     * Add top view for ChatUIKitMenuDialog.
     */
    fun addTopView(view: View) {
        binding?.llMenuTop?.let {
            it.removeAllViews()
            it.addView(view)
        }
    }

    /**
     * Clear top view for ChatUIKitMenuDialog.
     */
    fun clearTopView(){
        binding?.llMenuTop?.removeAllViews()
    }

}