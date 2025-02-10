package io.agora.chat.uikit.feature.chat.widgets

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.extensions.showSoftKeyboard
import io.agora.chat.uikit.databinding.UikitWidgetChatPrimaryMenuBinding
import io.agora.chat.uikit.feature.chat.interfaces.ChatUIKitPrimaryMenuListener
import io.agora.chat.uikit.feature.chat.interfaces.IChatPrimaryMenu
import io.agora.chat.uikit.widget.ChatUIKitInputEditText

class ChatUIKitPrimaryMenu @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
): FrameLayout(context, attrs, defStyleAttr), IChatPrimaryMenu, View.OnClickListener,
    ChatUIKitInputEditText.OnEditTextChangeListener, TextWatcher, View.OnKeyListener {
    private val binding: UikitWidgetChatPrimaryMenuBinding by lazy { UikitWidgetChatPrimaryMenuBinding.inflate(
        LayoutInflater.from(context), this, true) }

    private var listener: ChatUIKitPrimaryMenuListener? = null

    private var inputMenuStyle: ChatUIKitInputMenuStyle? = ChatUIKitInputMenuStyle.All

    private val isShowSendButton: Boolean by lazy { context.resources.getBoolean(R.bool.ease_input_show_send_button)}

    private val inputManager: InputMethodManager by lazy { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    init {
        binding.etSendmessage.run {
            setHorizontallyScrolling(false)
            var maxLines = context.resources.getInteger(R.integer.ease_input_edit_text_max_lines)
            maxLines = if (maxLines <= 0) 4 else maxLines
            setMaxLines(maxLines)
        }
        binding.btnSend.setOnClickListener(this)
        binding.btnSetModeVoice.setOnClickListener(this)
        binding.btnSetModeKeyboard.setOnClickListener(this)
        binding.btnMore.setOnClickListener(this)
        binding.rlFace.setOnClickListener(this)
        binding.etSendmessage.setOnClickListener(this)
        binding.etSendmessage.setOnEditTextChangeListener(this)
        binding.etSendmessage.setOnKeyListener(this)
        binding.btnPressToSpeak.setOnTouchListener { v, event ->
            return@setOnTouchListener listener?.onPressToSpeakBtnTouch(v, event) == true
        }
        showNormalStatus()
    }

    private fun setInputMenuType() {
        if (inputMenuStyle === ChatUIKitInputMenuStyle.DISABLE_VOICE) {
            binding.btnSetModeVoice.visibility = GONE
            binding.btnSetModeKeyboard.visibility = GONE
            binding.btnPressToSpeak.visibility = GONE
        } else if (inputMenuStyle === ChatUIKitInputMenuStyle.DISABLE_EMOJICON) {
            binding.rlFace.visibility = GONE
        } else if (inputMenuStyle === ChatUIKitInputMenuStyle.DISABLE_VOICE_EMOJICON) {
            binding.btnSetModeVoice.visibility = GONE
            binding.btnSetModeKeyboard.visibility = GONE
            binding.btnPressToSpeak.visibility = GONE
            binding.rlFace.visibility = GONE
        } else if (inputMenuStyle === ChatUIKitInputMenuStyle.ONLY_TEXT) {
            binding.btnSetModeVoice.visibility = GONE
            binding.btnSetModeKeyboard.visibility = GONE
            binding.btnPressToSpeak.visibility = GONE
            binding.rlFace.visibility = GONE
            binding.btnMore.visibility = GONE
        }
    }

    private fun checkSendButton() {
        val content = binding.etSendmessage.text
        setSendButtonVisible(content)
    }

    private fun setSendButtonVisible(content: CharSequence?) {
        if (isShowSendButton) {
            if (TextUtils.isEmpty(content)) {
                binding.btnMore.visibility = VISIBLE
                binding.btnSend.visibility = GONE
            } else {
                binding.btnMore.visibility = GONE
                binding.btnSend.visibility = VISIBLE
            }
        } else {
            binding.btnMore.visibility = VISIBLE
            binding.btnSend.visibility = GONE
        }
    }

    /**
     * show soft keyboard
     * @param et
     */
    private fun showSoftKeyboard(et: EditText?) {
        et?.showSoftKeyboard()
    }

    private fun showNormalFaceImage() {
        binding.ivFaceNormal.visibility = VISIBLE
        binding.ivFaceChecked.visibility = INVISIBLE
    }

    private fun showSelectedFaceImage() {
        binding.ivFaceNormal.visibility = INVISIBLE
        binding.ivFaceChecked.visibility = VISIBLE
    }

    private fun hideButtons() {
        binding.btnSetModeVoice.visibility = GONE
        binding.btnSetModeKeyboard.visibility = GONE
        binding.edittextLayout.visibility = GONE
        binding.btnPressToSpeak.visibility = GONE
    }

    private fun showSendButton(s: CharSequence?) {
        setSendButtonVisible(s)
        setInputMenuType()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        binding.etSendmessage.addTextChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        binding.etSendmessage.removeTextChangedListener(this)
        super.onDetachedFromWindow()
    }

    override fun setMenuShowType(style: ChatUIKitInputMenuStyle?) {
        this.inputMenuStyle = style
        setInputMenuType()
    }

    override fun showNormalStatus() {
        hideSoftKeyboard()
        hideButtons()
        binding.btnSetModeVoice.visibility = VISIBLE
        binding.edittextLayout.visibility = VISIBLE
        hideExtendStatus()
        checkSendButton()
        setInputMenuType()
    }

    override fun showTextStatus() {
        hideButtons()
        binding.btnSetModeVoice.visibility = VISIBLE
        binding.edittextLayout.visibility = VISIBLE
        hideExtendStatus()
        showSoftKeyboard(editText)
        checkSendButton()
        setInputMenuType()
        listener?.onToggleTextBtnClicked()
        showKeyboard()
    }

    override fun showVoiceStatus() {
        hideSoftKeyboard()
        hideButtons()
        binding.btnSetModeKeyboard.visibility = VISIBLE
        binding.edittextLayout.visibility = VISIBLE
        hideExtendStatus()
        setInputMenuType()
        listener?.onToggleVoiceBtnClicked()
    }

    override fun showEmojiconStatus() {
        hideButtons()
        binding.btnSetModeVoice.visibility = VISIBLE
        binding.edittextLayout.visibility = VISIBLE
        binding.btnMore.isChecked = false
        if (binding.ivFaceNormal.visibility == VISIBLE) {
            hideSoftKeyboard()
            showSelectedFaceImage()
        } else {
            showSoftKeyboard(editText)
            showNormalFaceImage()
        }
        setInputMenuType()
        listener?.onToggleEmojiconClicked(binding.ivFaceChecked.visibility == VISIBLE)
    }

    override fun showMoreStatus() {
        if (binding.btnMore.isChecked) {
            hideSoftKeyboard()
            hideButtons()
            binding.btnSetModeVoice.visibility = VISIBLE
            binding.edittextLayout.visibility = VISIBLE
            showNormalFaceImage()
        } else {
            showTextStatus()
        }
        setInputMenuType()
        listener?.onToggleExtendClicked(binding.btnMore.isChecked)
    }

    override fun hideExtendStatus() {
        binding.btnMore.isChecked = false
        showNormalFaceImage()
    }

    override fun hideSoftKeyboard() {
        if (binding.etSendmessage == null || context !is Activity) {
            return
        }
        if (context.window.attributes.softInputMode
            != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            inputManager.hideSoftInputFromWindow(binding.etSendmessage.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
        binding.etSendmessage.isFocusableInTouchMode = false
    }

    private fun showKeyboard() {
        binding.etSendmessage.isFocusableInTouchMode = true
        binding.etSendmessage.requestFocus()
        inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onEmojiconInputEvent(emojiContent: CharSequence?) {
        binding.etSendmessage.append(emojiContent)
    }

    override fun onEmojiconDeleteEvent() {
        if (!TextUtils.isEmpty(binding.etSendmessage.text)) {
            val event =
                KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
            binding.etSendmessage.dispatchKeyEvent(event)
        }
    }

    override fun onTextInsert(text: CharSequence?) {
        val start = binding.etSendmessage.selectionStart
        val editable = binding.etSendmessage.editableText
        editable.insert(start, text)
        showTextStatus()
    }

    override val editText: EditText?
        get() = binding.etSendmessage

    override fun setMenuBackground(bg: Drawable?) {
        binding.rlBottom.background = bg
    }

    override fun setSendButtonBackground(bg: Drawable?) {
        binding.btnSend.background = bg
    }

    override fun setEaseChatPrimaryMenuListener(listener: ChatUIKitPrimaryMenuListener?) {
        this.listener = listener
    }

    override fun setVisible(visible: Int) {
        this.visibility = visible
    }

    override fun onClick(v: View?) {
        v?.run {
            when(id) {
                R.id.btn_send -> {
                    if (listener != null) {
                        val s = binding.etSendmessage.text.toString()
                        binding.etSendmessage.setText("")
                        listener?.onSendBtnClicked(s)
                    }
                }
                R.id.btn_set_mode_voice -> {
                    showVoiceStatus()
                }
                R.id.btn_set_mode_keyboard -> {
                    showTextStatus()
                }
                R.id.btn_more -> {
                    showMoreStatus()
                }
                R.id.et_sendmessage -> {
                    showTextStatus()
                }
                R.id.rl_face -> {
                    showEmojiconStatus()
                }
            }
        }
    }

    override fun onClickKeyboardSendBtn(content: String?) {
        listener?.onSendBtnClicked(content)
    }

    override fun onEditTextHasFocus(hasFocus: Boolean) {
        listener?.onEditTextHasFocus(hasFocus)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        showSendButton(s)
        if (listener != null) {
            listener?.onTyping(s, start, before, count)
        }
    }

    override fun afterTextChanged(s: Editable?) {
        if (listener != null) {
            listener?.afterTextChanged(s)
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (listener != null){
            return listener?.editTextOnKeyListener(v, keyCode, event) ?: true
        }
        return false
    }

}