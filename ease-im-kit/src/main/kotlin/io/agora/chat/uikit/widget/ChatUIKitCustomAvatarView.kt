package io.agora.chat.uikit.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import coil.imageLoader
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.dpToPx
import io.agora.chat.uikit.common.extensions.loadAvatar
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitPresenceViewBinding
import io.agora.chat.uikit.model.ChatUIKitProfile

class ChatUIKitCustomAvatarView : ConstraintLayout{
    private val mViewBinding = UikitPresenceViewBinding.inflate(LayoutInflater.from(context))

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mViewBinding.root)
        init()
    }
    private fun init() {
        setOnClickListener { v ->
            if (listener != null) {
                listener?.onPresenceClick(v)
            }
        }
        mViewBinding.ivUserAvatar.setOnClickListener {
            listener?.onPresenceAvatarClick(it)
        }

        ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(mViewBinding.ivUserAvatar)
    }

    fun setPresenceStatusSize(size: Int) {
        mViewBinding.ivPresence.let {
            it.layoutParams.width = size
            it.layoutParams.height = size
        }
    }

    fun setPresenceStatusMargin(
        start:Int? = 0,
        top:Int? = 0,
        end:Int? = 0,
        bottom:Int? = 0
    ){
        val layoutParams = mViewBinding.ivPresence.layoutParams as MarginLayoutParams
        start?.let {
            layoutParams.marginStart = it.dpToPx(context)
        }
        top?.let {
            layoutParams.topMargin = it.dpToPx(context)
        }
        end?.let {
            layoutParams.marginEnd = it.dpToPx(context)
        }
        bottom?.let {
            layoutParams.bottomMargin = it.dpToPx(context)
        }
        mViewBinding.ivPresence.layoutParams = layoutParams
    }

    fun getStatusView(): ChatUIKitImageView {
        return mViewBinding.ivPresence
    }

    fun setUserAvatarData(profile: ChatUIKitProfile?) {
        mViewBinding.ivUserAvatar.loadAvatar(profile)
    }

    fun setUserStatusData(@DrawableRes icon: Int? = null){
        icon?.let {
            mViewBinding.ivPresence.setImageResource(icon)
        }
    }

    fun setUserAvatarData(profile: ChatUIKitProfile?, icon: String?) {
        mViewBinding.ivUserAvatar.loadAvatar(profile)
        icon?.let {
            mViewBinding.ivPresence.load(data=icon){
                this.listener(
                    onError = { _,_->  mViewBinding.ivPresence.visibility = GONE},
                    onCancel = { mViewBinding.ivPresence.visibility = GONE }
                )
            }
        }
    }

    fun setUserAvatarData(@DrawableRes avatar: Int?, nickname: String?, @DrawableRes icon: Int? = null){
        mViewBinding.ivUserAvatar.load(avatar) {
            placeholder(R.drawable.uikit_default_avatar)
            error(R.drawable.uikit_default_avatar)
        }
        icon?.let {
            mViewBinding.ivPresence.setImageResource(icon)
        }
    }

    interface OnPresenceClickListener {
        fun onPresenceClick(v: View?){}
        fun onPresenceAvatarClick(v: View){}
    }

    private var listener: OnPresenceClickListener? = null

    fun setOnPresenceClickListener(listener: OnPresenceClickListener?) {
        this.listener = listener
    }

    fun getUserAvatar():ChatUIKitImageView{
        return mViewBinding.ivUserAvatar
    }
}