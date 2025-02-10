package io.agora.chat.uikit.feature.chat.widgets

import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.extensions.getEmojiText
import io.agora.chat.uikit.databinding.UikitWidgetChatInputMenuContainerBinding
import io.agora.chat.uikit.feature.chat.interfaces.ChatInputMenuListener
import io.agora.chat.uikit.feature.chat.interfaces.ChatUIKitExtendMenuItemClickListener
import io.agora.chat.uikit.feature.chat.interfaces.ChatUIKitPrimaryMenuListener
import io.agora.chat.uikit.feature.chat.interfaces.ChatUIKitEmojiconMenuListener
import io.agora.chat.uikit.feature.chat.interfaces.IChatEmojiconMenu
import io.agora.chat.uikit.feature.chat.interfaces.IChatExtendMenu
import io.agora.chat.uikit.feature.chat.interfaces.IChatInputMenu
import io.agora.chat.uikit.feature.chat.interfaces.IChatPrimaryMenu
import io.agora.chat.uikit.feature.chat.interfaces.IChatTopExtendMenu
import io.agora.chat.uikit.interfaces.OnMenuDismissListener
import io.agora.chat.uikit.menu.ChatUIKitMenuDialog
import io.agora.chat.uikit.model.ChatUIKitEmojicon

class ChatUIKitInputMenu @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr), IChatInputMenu, ChatUIKitPrimaryMenuListener,
    ChatUIKitExtendMenuItemClickListener, ChatUIKitEmojiconMenuListener {

    private val TAG = ChatUIKitInputMenu::class.java.simpleName

    private val binding: UikitWidgetChatInputMenuContainerBinding by lazy { UikitWidgetChatInputMenuContainerBinding.inflate(
        LayoutInflater.from(context), this, true) }

    private var primaryMenu: IChatPrimaryMenu? = null
    private var emojiMenu: IChatEmojiconMenu? = null
    private var extendMenu: IChatExtendMenu? = null
    private var topExtendMenu: IChatTopExtendMenu? = null
    private var menuListener: ChatInputMenuListener? = null

    init {
        showPrimaryMenu()
        if (extendMenu == null) {
            extendMenu = ChatUIKitExtendMenu(getContext())
            (extendMenu as ChatUIKitExtendMenu).init()
        }
    }

    private fun showPrimaryMenu() {
        if (primaryMenu == null) {
            primaryMenu = ChatUIKitPrimaryMenu(getContext())
        }
        primaryMenu?.let {
            if (it is View) {
                binding.primaryMenuContainer.visibility = VISIBLE
                binding.primaryMenuContainer.removeAllViews()
                binding.primaryMenuContainer.addView(it)
            }
            if (it is Fragment && getContext() is AppCompatActivity) {
                val manager = (getContext() as AppCompatActivity).supportFragmentManager
                manager.beginTransaction().replace(
                    R.id.primary_menu_container,
                    (it as Fragment)
                ).commitAllowingStateLoss()
            }
            it.setEaseChatPrimaryMenuListener(this)
        }
    }

    private fun showExtendMenu() {
        if (extendMenu == null) {
            extendMenu = ChatUIKitExtendMenu(getContext())
            (extendMenu as ChatUIKitExtendMenu).init()
        }
        if (extendMenu is View) {
            postDelayed({
                binding.run {
                    extendMenuContainer.visibility = VISIBLE
                    extendMenuContainer.removeAllViews()
                    extendMenuContainer.addView(extendMenu as View?)
                    extendMenu?.setEaseChatExtendMenuItemClickListener(this@ChatUIKitInputMenu)
                }
            }, 200)
        }
        if (extendMenu is Dialog) {
            binding.extendMenuContainer.visibility = GONE
            (extendMenu as Dialog).show()
            (extendMenu as Dialog).setOnDismissListener {
                primaryMenu?.hideExtendStatus()
            }
            (extendMenu as Dialog).setOnCancelListener {
                primaryMenu?.hideExtendStatus()
            }
            extendMenu?.setEaseChatExtendMenuItemClickListener(this)
        }
        if (extendMenu is DialogFragment && getContext() is AppCompatActivity) {
            binding.extendMenuContainer.visibility = GONE
            (extendMenu as DialogFragment).let {
                val manager = (getContext() as AppCompatActivity).supportFragmentManager
                it.show(manager, "ease_chat_extend_menu")
                extendMenu?.setEaseChatExtendMenuItemClickListener(this)
                if (it is ChatUIKitMenuDialog) {
                    it.setOnMenuDismissListener(object : OnMenuDismissListener {
                        override fun onDismiss() {
                            showExtendMenu(false)
                        }
                    })
                }
            }
        } else if (extendMenu is Fragment && getContext() is AppCompatActivity) {
            binding.extendMenuContainer.visibility = VISIBLE
            val manager = (getContext() as AppCompatActivity).supportFragmentManager
            manager.beginTransaction().replace(
                R.id.extend_menu_container,
                (extendMenu as Fragment?)!!
            ).commitAllowingStateLoss()
            extendMenu?.setEaseChatExtendMenuItemClickListener(this)
        }
    }

    private fun showEmojiconMenu() {
        if (emojiMenu == null) {
            emojiMenu = ChatUIKitEmojiconMenu(context)
            (emojiMenu as ChatUIKitEmojiconMenu).init()
        }
        if (emojiMenu is View) {
            postDelayed({
                binding.run {
                    extendMenuContainer.visibility = VISIBLE
                    extendMenuContainer.alpha = 0f
                    extendMenuContainer.animate().alpha(1f).setDuration(400).start()
                    extendMenuContainer.removeAllViews()
                    extendMenuContainer.addView(emojiMenu as View?)
                    emojiMenu?.setEmojiconMenuListener(this@ChatUIKitInputMenu)
                }
            }, 200)
        }
        if (emojiMenu is Fragment && context is AppCompatActivity) {
            postDelayed({
                binding.run {
                    extendMenuContainer.visibility = VISIBLE
                    extendMenuContainer.alpha = 0f
                    extendMenuContainer.animate().alpha(1f).setDuration(400).start()
                    val manager = context.supportFragmentManager
                    manager.beginTransaction().replace(
                        R.id.extend_menu_container,
                        (emojiMenu as Fragment?)!!
                    ).commitAllowingStateLoss()
                    emojiMenu?.setEmojiconMenuListener(this@ChatUIKitInputMenu)
                }
            }, 200)
        }
    }

    private fun showTopExtendMenu() {
        if (topExtendMenu is View) {
            binding.topExtendMenuContainer.visibility = VISIBLE
            binding.topExtendMenuContainer.removeAllViews()
            binding.topExtendMenuContainer.addView(topExtendMenu as View?)
        }
        if (topExtendMenu is Fragment && context is AppCompatActivity) {
            binding.topExtendMenuContainer.visibility = VISIBLE
            val manager = context.supportFragmentManager
            manager.beginTransaction().replace(
                R.id.top_extend_menu_container,
                (topExtendMenu as Fragment?)!!
            ).commitAllowingStateLoss()
        }
    }

    override fun onSendBtnClicked(content: String?) {
        menuListener?.onSendMessage(content)
    }

    override fun onTyping(s: CharSequence?, start: Int, before: Int, count: Int) {
        menuListener?.onTyping(s, start, before, count)
    }

    override fun afterTextChanged(s: Editable?) {
        menuListener?.afterTextChanged(s)
    }

    override fun editTextOnKeyListener(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (menuListener != null){
            return menuListener?.editTextOnKeyListener(v, keyCode, event)?:true
        }
        return false
    }

    override fun onPressToSpeakBtnTouch(v: View?, event: MotionEvent?): Boolean {
        return menuListener?.onPressToSpeakBtnTouch(v, event) ?: false
    }

    override fun onToggleVoiceBtnClicked() {
        showExtendMenu(false)
        menuListener?.onToggleVoiceBtnClicked()
    }

    override fun onToggleTextBtnClicked() {
        showExtendMenu(false)
    }

    override fun onToggleExtendClicked(extend: Boolean) {
        showExtendMenu(extend)
    }

    override fun onToggleEmojiconClicked(extend: Boolean) {
        showEmojiconMenu(extend)
    }

    override fun onEditTextClicked() {
        ChatLog.i(TAG, "onEditTextClicked")
    }

    override fun onEditTextHasFocus(hasFocus: Boolean) {
        ChatLog.i(TAG, "onEditTextHasFocus: hasFocus = $hasFocus")
    }

    override fun setCustomPrimaryMenu(menu: IChatPrimaryMenu?) {
        primaryMenu = menu
        showPrimaryMenu()
    }

    override fun setCustomEmojiconMenu(menu: IChatEmojiconMenu?) {
        this.emojiMenu = menu
    }

    override fun setCustomExtendMenu(menu: IChatExtendMenu?) {
        extendMenu = menu
    }

    override fun setCustomTopExtendMenu(menu: IChatTopExtendMenu?) {
        topExtendMenu = menu
    }

    override fun hideExtendContainer() {
        primaryMenu?.showNormalStatus()
        binding.extendMenuContainer.visibility = GONE
    }

    override fun hideInputMenu() {
        binding.primaryMenuContainer.visibility = GONE
        binding.extendMenuContainer.visibility = GONE
    }

    override fun showPrimaryMenu(show: Boolean) {
        if (show) {
            showPrimaryMenu()
        } else {
            binding.primaryMenuContainer.visibility = GONE
        }
    }

    override fun showEmojiconMenu(show: Boolean) {
        if (show) {
            showEmojiconMenu()
        } else {
            binding.extendMenuContainer.visibility = GONE
        }
    }

    override fun showExtendMenu(show: Boolean) {
        if (show) {
            showExtendMenu()
        } else {
            binding.extendMenuContainer.visibility = GONE
            primaryMenu?.hideExtendStatus()
        }
    }

    override fun showTopExtendMenu(isShow: Boolean) {
        if (isShow) {
            showTopExtendMenu()
        } else {
            binding.topExtendMenuContainer.visibility = GONE
        }
    }

    override fun hideSoftKeyboard() {
        primaryMenu?.hideSoftKeyboard()
    }

    override fun setChatInputMenuListener(listener: ChatInputMenuListener?) {
        menuListener = listener
    }

    override val chatPrimaryMenu: IChatPrimaryMenu?
        get() = primaryMenu
    override val chatEmojiMenu: IChatEmojiconMenu?
        get() = emojiMenu
    override val chatExtendMenu: IChatExtendMenu?
        get() = extendMenu
    override val chatTopExtendMenu: IChatTopExtendMenu?
        get() = topExtendMenu

    override fun onBackPressed(): Boolean {
        if (binding.extendMenuContainer.visibility == VISIBLE) {
            binding.extendMenuContainer.visibility = GONE
            return false
        }
        return true
    }

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
        menuListener?.onChatExtendMenuItemClick(itemId, view)
    }

    override fun onExpressionClicked(emojiIcon: Any?) {
        if (emojiIcon is ChatUIKitEmojicon) {
            val easeEmojicon: ChatUIKitEmojicon = emojiIcon
            if (easeEmojicon.type !== ChatUIKitEmojicon.Type.BIG_EXPRESSION) {
                primaryMenu?.onEmojiconInputEvent(
                    easeEmojicon.emojiText.getEmojiText(context)
                )
            } else {
                if (menuListener != null) {
                    menuListener!!.onExpressionClicked(emojiIcon)
                }
            }
        } else {
            if (menuListener != null) {
                menuListener!!.onExpressionClicked(emojiIcon)
            }
        }
    }

    override fun onDeleteImageClicked() {
        primaryMenu?.onEmojiconDeleteEvent()
    }

    override fun onSendIconClicked() {
        primaryMenu?.run {
            editText?.let {
                val content = it.text.toString().trim()
                if (content.isNotEmpty()) {
                    it.setText("")
                    menuListener?.onSendMessage(content)
                }
            }
        }
    }
}