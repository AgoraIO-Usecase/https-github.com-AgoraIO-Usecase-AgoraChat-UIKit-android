package io.agora.chat.uikit.common.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import io.agora.chat.uikit.R
import io.agora.chat.uikit.widget.ChatUIKitImageView

class CustomDialog(
    context: Context,
    private val title: String = "",
    private val subtitle: String = "",
    private val isEditTextMode: Boolean,
    private val inputHint: String = "",
    private val leftButtonText:String? = null,
    private val rightButtonText:String? = null,
    private var onLeftButtonClickListener: (() -> Unit)? = {},
    private var onRightButtonClickListener: (() -> Unit)? = {},
    private var onInputTextChangeListener: ((String) -> Unit)? = {},
    private var onInputModeConfirmListener: ((String) -> Unit)? = {},
) : Dialog(context) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.uikit_layout_custom_dialog, null)
        setContentView(view)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
        val subtitleTextView = view.findViewById<TextView>(R.id.subtitleTextView)
        val leftButton = view.findViewById<TextView>(R.id.leftButton)
        val rightButton = view.findViewById<TextView>(R.id.rightButton)
        val editText = view.findViewById<EditText>(R.id.editText)
        val inputClear = view.findViewById<ChatUIKitImageView>(R.id.input_clear)
        val llEdit = view.findViewById<RelativeLayout>(R.id.rl_edit)

        titleTextView.text = title
        subtitleTextView.text = subtitle
        editText.hint = inputHint

        leftButtonText?.let {
            leftButton.text = it
        }
        rightButtonText?.let {
            rightButton.text = it
        }

        if (subtitle.isEmpty()){
            subtitleTextView.visibility = View.GONE
        }else{
            subtitleTextView.visibility = View.VISIBLE
        }

        if (isEditTextMode) {
            llEdit.visibility = View.VISIBLE
            editText.requestFocus()
            inputClear.setOnClickListener{
                editText.setText("")
            }
            editText.visibility = View.VISIBLE
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s.isEmpty()){
                        inputClear.visibility = View.GONE
                        rightButton.isSelected = false
                    }else{
                        inputClear.visibility = View.VISIBLE
                        rightButton.isSelected = true
                    }
                    onInputTextChangeListener?.invoke(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        } else {
            rightButton.isSelected = true
            editText.visibility = View.GONE
            llEdit.visibility = View.GONE
        }

        leftButton.setOnClickListener {
            onLeftButtonClickListener?.invoke()
            dismiss()
        }

        rightButton.setOnClickListener {
            if (isEditTextMode){
                if (editText.text.isNotEmpty()){
                    onInputModeConfirmListener?.invoke(editText.text.toString())
                }
            }else{
                onRightButtonClickListener?.invoke()
            }
            dismiss()
        }
    }
}