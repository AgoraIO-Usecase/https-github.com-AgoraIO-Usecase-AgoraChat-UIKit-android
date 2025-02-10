package io.agora.chat.uikit.widget.chatrow

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatCustomMessageBody
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.widget.ChatUIKitImageView

open class ChatUIKitRowUserCard @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
    isSender: Boolean
) : ChatUIKitRow(context, attrs, defStyleAttr, isSender) {
    protected val nicknameView: TextView? by lazy { findViewById(R.id.user_nick_name) }
    protected val userIdView: TextView? by lazy { findViewById(R.id.user_id) }
    protected val headImageView: ChatUIKitImageView? by lazy { findViewById(R.id.head_Image_view) }

    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.uikit_row_received_user_card else R.layout.uikit_row_sent_user_card,
            this
        )
    }

    override fun onSetUpView() {
        ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(headImageView)
        message?.run {
            (body as? ChatCustomMessageBody)?.let {
                val params: Map<String, String> = it.params
                val uId = params[ChatUIKitConstant.USER_CARD_ID]
                userIdView?.text = uId
                val nickName = params[ChatUIKitConstant.USER_CARD_NICK]
                nicknameView?.text = nickName
                if (nickName.isNullOrEmpty()){
                    nicknameView?.visibility = GONE
                    userIdView?.visibility = VISIBLE
                }else{
                    nicknameView?.visibility = VISIBLE
                    userIdView?.visibility = GONE
                }
                val headUrl = params[ChatUIKitConstant.USER_CARD_AVATAR]
                headImageView?.load(headUrl) {
                    placeholder(R.drawable.uikit_default_avatar)
                    error(R.drawable.uikit_default_avatar)
                }
            }
        }
    }

}