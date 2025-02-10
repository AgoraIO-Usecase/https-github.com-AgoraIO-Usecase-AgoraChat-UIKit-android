package io.agora.chat.uikit.feature.chat.urlpreview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.style.URLSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.toSpannable
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.extensions.isUrlPreviewMessage
import io.agora.chat.uikit.common.extensions.parseUrlPreview
import io.agora.chat.uikit.common.helper.URLPreviewHelper
import io.agora.chat.uikit.interfaces.UrlPreviewStatusCallback
import io.agora.chat.uikit.model.ChatUIKitPreview
import io.agora.chat.uikit.widget.ChatUIKitImageView

class ChatUIKitMessageUrlPreview @JvmOverloads constructor(
    var isSender:Boolean? = null,
    private val context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(
    context, attrs, defStyleAttr
) {
    private val ivIcon: ChatUIKitImageView by lazy { findViewById(R.id.iv_icon) }
    private val tvPreviewTitle: TextView by lazy { findViewById(R.id.tv_title) }
    private val tvPreviewContent: TextView by lazy { findViewById(R.id.tv_describe) }
    private val tvUrl: TextView by lazy { findViewById(R.id.tv_url) }
    private val layout: ConstraintLayout by lazy { findViewById(R.id.describe_layout) }
    private val divider: View by lazy { findViewById(R.id.v_divider) }

    init {
        initAttrs(context, attrs)
    }

    private fun initAttrs(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ChatUIKitMessageUrlPreview).let { a ->
            if (this.isSender == null){
                isSender = a.getBoolean(R.styleable.ChatUIKitMessageUrlPreview_ease_chat_message_preview_is_sender, false)
            }
            a.recycle()
        }

        isSender?.let {
            if (it) {
                inflate(context, R.layout.uikit_row_sent_url_preview_layout, this)
            } else {
                inflate(context, R.layout.uikit_row_received_url_preview_layout, this)
            }
        }
    }

    fun showParsing(parsing:String?=null){
        tvPreviewTitle.text = context.getString(R.string.uikit_url_preview_parsing)
        parsing?.let {
            tvPreviewTitle.text = it
        }
        layout.visibility = VISIBLE
        tvPreviewTitle.visibility = VISIBLE
    }

    fun hideAllView(){
        layout.visibility = GONE
        tvPreviewTitle.visibility = GONE
        tvPreviewContent.visibility = GONE
        hideImage()
    }

    fun showImage(){
        ivIcon.visibility = VISIBLE
        divider.visibility = GONE
    }

    fun hideImage(){
        ivIcon.visibility = GONE
        divider.visibility = VISIBLE
    }

    fun checkPreview(message: ChatMessage?,statusCallback: UrlPreviewStatusCallback? = null){
        hideAllView()
        message?.let {
            if (it.type != ChatMessageType.TXT){
                return
            }
            val bean = ChatUIKitClient.getCache().getUrlPreviewInfo(it.msgId)
            bean?.let {
                updateView(bean,statusCallback)
            }?:kotlin.run {
                val content = (it.body as ChatTextMessageBody).message
                tvUrl.text = content
                val spannable = tvUrl.text.toSpannable()
                val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
                if ( it.isUrlPreviewMessage() ){
                    val preview = it.parseUrlPreview()
                    updateView(preview, statusCallback)
                }else if (spans.isNotEmpty() && ChatUIKitClient.getCache().isFirstLoadedUrlPreview(it.msgId)){
                    spans.let { sp->
                        statusCallback?.onParsing()?: kotlin.run { showParsing() }
                        URLPreviewHelper.downLoadHtmlByUrl(sp[0].url,object : UrlPreviewStatusCallback{
                            override fun onDownloadFinish(preview: ChatUIKitPreview?) {
                                ChatLog.e("UrlPreview","parsing url:${preview?.url} - title:${preview?.title} - description:${preview?.description} - imageURL:${preview?.imageURL}")
                                statusCallback?.onDownloadFinish(preview)?:kotlin.run {
                                    preview?.let { pv->
                                        pv.title?.let {
                                            ChatUIKitClient.getCache().saveUrlPreviewInfo(message.msgId,pv)
                                            updateView(preview,statusCallback)
                                        }
                                    }?:kotlin.run {
                                        ChatUIKitClient.getCache().checkUrlPreview(it.msgId,false)
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            hideAllView()
                                        }, 500)
                                    }
                                }
                            }
                        })
                    }
                }
            }
        }
    }

    fun updateView(preview:ChatUIKitPreview?,statusCallback:UrlPreviewStatusCallback?=null){
        preview?.let { ep->
            if (ep.title.isNullOrEmpty()){
                statusCallback?.onParseFile() ?:kotlin.run { hideAllView() }
            }else{
                ep.description?.let { des->
                    if ( des.isEmpty() ){
                        tvPreviewContent.visibility = GONE
                    }else{
                        tvPreviewContent.text = des
                        tvPreviewContent.visibility = VISIBLE
                    }
                }?:kotlin.run {
                    tvPreviewContent.visibility = GONE
                }
                ep.imageURL?.let { url->
                    ivIcon.load(url){
                        this.listener(
                            onError = { _,_-> hideImage() },
                            onCancel = { hideImage() }
                        )
                    }
                    showImage()
                }?:kotlin.run {
                    hideImage()
                }
                ep.title?.let { title->
                    tvPreviewTitle.text = title
                    tvPreviewTitle.visibility = VISIBLE
                }
                layout.visibility = VISIBLE
            }
        }?:kotlin.run {
            hideAllView()
        }
    }

}