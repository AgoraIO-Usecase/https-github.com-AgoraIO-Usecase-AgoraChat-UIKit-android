package io.agora.chat.uikit.feature.chat.controllers

import android.content.Context
import androidx.fragment.app.FragmentActivity
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitLayout
import io.agora.chat.uikit.feature.chat.report.ChatUIKitReportSheetDialog

class ChatUIKitMessageReportController(
    private val context: Context,
    private val chatLayout: ChatUIKitLayout,
    private val reportTag:Array<String>? = context.resources.getStringArray(R.array.report_reason),
) {
    fun showReportDialog(message: ChatMessage?){
        val reportTagList = reportTag?.toMutableList()
        val report = ChatUIKitReportSheetDialog(
            context = context,
            itemList = reportTagList?: context.resources.getStringArray(R.array.report_reason).toMutableList(),
            onRightButtonClickListener = { position, reason ->
                val tag = context.resources.getStringArray(R.array.report_tag).let {
                    it[position]
                } ?: "unknown"
                reason?.let { chatLayout.reportMessage(tag, reason, message) }
            },
            onLeftButtonClickListener = {}
        )
        (context as FragmentActivity).supportFragmentManager.let {  report.show(it,"chat_report") }
    }
}