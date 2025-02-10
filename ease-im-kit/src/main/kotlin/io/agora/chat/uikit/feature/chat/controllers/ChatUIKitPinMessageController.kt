package io.agora.chat.uikit.feature.chat.controllers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import io.agora.chat.uikit.base.ChatUIKitBaseRecyclerViewAdapter
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.createNotifyPinMessage
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.common.extensions.showToast
import io.agora.chat.uikit.feature.chat.pin.ChatUIKitPinMessageListViewGroup
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitLayout
import io.agora.chat.uikit.viewmodel.messages.IChatViewRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatUIKitPinMessageController(
    private val context:Context,
    private val chatLayout: ChatUIKitLayout,
    private val conversationId: String?,
    private val viewModel: IChatViewRequest?
) {
    private var pinMessageListView: ChatUIKitPinMessageListViewGroup? = null
    private var pinMessageMap: MutableMap<String,ChatMessage> = mutableMapOf()
    private val pinMessages: MutableList<ChatMessage> = mutableListOf()
    private var isShowPinView = false

    fun initPinInfoView(){
        pinMessageListView = ChatUIKitPinMessageListViewGroup(context)
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        pinMessageListView?.visibility = RelativeLayout.GONE
        chatLayout.addView(pinMessageListView, layoutParams)
        chatLayout.post {
            val maxH = (chatLayout.height * 3/4) - 100.dpToPx(context)
            pinMessageListView?.setConstraintLayoutMaxHeight( maxH )
        }

        initListener()
    }

    fun setPinInfoView(){
        pinMessageListView?.post{
            isShowPinView = !isShowPinView
            if (isShowPinView){
                showPinInfoView()
            }else{
                hidePinInfoView()
            }
        }
    }

    fun showPinInfoView(){
        pinMessageListView?.visibility = View.VISIBLE
        pinMessageListView?.show(pinMessages)
    }

    fun hidePinInfoView(){
        pinMessageListView?.visibility = View.GONE
    }

    private fun initListener(){
        pinMessageListView?.let {
            it.setOnItemClickListener(object : ChatUIKitPinMessageListViewGroup.OnPinItemClickListener{
                override fun onItemClick(message: ChatMessage?) {
                    hidePinInfoView()
                    isShowPinView = false
                    //click for pin message list
                    val messageList: List<ChatMessage>? = chatLayout.chatMessageListLayout?.getMessagesAdapter()?.data
                    var isExist = false
                    messageList?.let { data->
                        for (msg in data) {
                            if (msg.msgId == message?.msgId){
                                isExist = true
                                break
                            }
                        }
                    }
                    if (!isExist) {
                        context.showToast("message not found")
                    } else {
                        chatLayout.chatMessageListLayout?.moveToTarget(message)
                    }
                }
            })

            it.setOnItemSubViewClickListener(object : ChatUIKitBaseRecyclerViewAdapter.OnItemSubViewClickListener{
                override fun onItemSubViewClick(view: View?, position: Int) {
                    val message = pinMessages[position]
                    pinMessage(message,false)
                }

            })

            it.setPinViewStatusChangeListener(object :
                ChatUIKitPinMessageListViewGroup.OnPinViewStatusChangListener {
                    override fun onHidePinView() {
                        isShowPinView = false
                    }
            })
        }

    }

    fun updatePinMessage(message:ChatMessage?,operationUser:String?){
        CoroutineScope(Dispatchers.Main).launch {
            val isPined: Boolean = message?.pinnedInfo() == null || message.pinnedInfo().operatorId().isEmpty()
            ChatLog.d("updatePinMessage",if (isPined) "unpin success" else "pin success")
            if (isPined) {
                removeData(message)
            } else {
                addData(message)
            }
            insertNotifyMessage(message,operationUser)
            chatLayout.chatMessageListLayout?.refreshToLatest()
        }
    }

    fun pinMessage(message: ChatMessage?,isPinned:Boolean){
        if (isPinned){
            viewModel?.pinMessage(message)
        }else{
            viewModel?.unPinMessage(message)
        }
    }

    fun fetchPinnedMessagesFromServer(){
        viewModel?.fetchPinMessageFromServer(conversationId)
    }

    fun setData(messages:MutableList<ChatMessage>?){
        pinMessages.clear()
        messages?.let {
            it.map { msg->
                pinMessageMap[msg.msgId] = msg
            }
            val list = sortedByMapValue()
            pinMessages.addAll(0,list)
            pinMessageListView?.show(pinMessages)
        }
    }

    private fun sortedByMapValue():MutableList<ChatMessage>{
        val list = pinMessageMap.values.toMutableList()
        return list.filter {  it.pinnedInfo()!=null }.sortedByDescending { sortMsg-> sortMsg.pinnedInfo()?.pinTime() }.toMutableList()
    }

    fun addData(message: ChatMessage?) {
        pinMessages.clear()
        message?.let {
            pinMessageMap[message.msgId] = it
            val list = sortedByMapValue()
            pinMessages.addAll(0,list)
            pinMessageListView?.show(pinMessages)
        }
    }

    fun removeData(message: ChatMessage?) {
        pinMessages.clear()
        message?.let {
            if (pinMessageMap.containsKey(it.msgId)){
                pinMessageMap.remove(it.msgId)
            }
            val list = sortedByMapValue()
            pinMessages.addAll(0,list)
            pinMessageListView?.removeData(it)
        }
    }


    fun insertNotifyMessage(message:ChatMessage?,operationUser:String?){
        val notifyMessage = message?.createNotifyPinMessage(operationUser)
        ChatClient.getInstance().chatManager().saveMessage(notifyMessage)
    }

}