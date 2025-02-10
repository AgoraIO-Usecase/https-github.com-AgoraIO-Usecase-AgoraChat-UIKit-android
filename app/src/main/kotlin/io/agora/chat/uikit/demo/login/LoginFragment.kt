package io.agora.chat.uikit.demo.login

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.TextView.GONE
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import io.agora.chat.uikit.base.ChatUIKitBaseFragment
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.extensions.catchChatException
import io.agora.chat.uikit.common.extensions.hideSoftKeyboard
import io.agora.chat.uikit.demo.MainActivity
import io.agora.chat.uikit.demo.R
import io.agora.chat.uikit.demo.databinding.DemoFragmentLoginBinding
import io.agora.chat.uikit.demo.utils.ChatUIKitEditTextUtils
import io.agora.chat.uikit.demo.utils.PhoneNumberUtils
import io.agora.chat.uikit.demo.utils.ToastUtils.showToast
import io.agora.chat.uikit.demo.viewmodel.LoginFragmentViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class LoginFragment : ChatUIKitBaseFragment<DemoFragmentLoginBinding>(), View.OnClickListener, TextWatcher,
    CompoundButton.OnCheckedChangeListener, OnEditorActionListener {
    private var mUserPhone: String? = null
    private var mCode: String? = null
    private var isTokenFlag = false
    private lateinit var mFragmentViewModel: LoginFragmentViewModel
    private var clear: Drawable? = null
    private var eyeOpen: Drawable? = null
    private var eyeClose: Drawable? = null
    private val COUNTS = 5
    private val DURATION = (3 * 1000).toLong()
    private val mHits = LongArray(COUNTS)
    private var isDeveloperMode = true
    private var isShowingDialog = false

    private val stopTimeoutMillis: Long = 5000


    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoFragmentLoginBinding? {
        return DemoFragmentLoginBinding.inflate(inflater)
    }

    override fun initListener() {
        super.initListener()
        binding?.run {
            etLoginPhone.addTextChangedListener(this@LoginFragment)
            etLoginCode.addTextChangedListener(this@LoginFragment)
            tvLoginRegister.setOnClickListener(this@LoginFragment)
            tvLoginToken.setOnClickListener(this@LoginFragment)
            tvVersion.setOnClickListener(this@LoginFragment)
            btnLogin.setOnClickListener(this@LoginFragment)
            tvGetCode.setOnClickListener(this@LoginFragment)
            tvLoginResetPassword.setOnClickListener(this@LoginFragment)
            tvLoginDeveloper.setOnClickListener(this@LoginFragment)
            cbSelect.setOnCheckedChangeListener(this@LoginFragment)
            etLoginCode.setOnEditorActionListener(this@LoginFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        mFragmentViewModel = ViewModelProvider(this)[LoginFragmentViewModel::class.java]
    }

    override fun initData() {
        super.initData()
        // 保证切换fragment后相关状态正确
        binding?.run {
            tvLoginToken.visibility = GONE
            etLoginPhone.setText(ChatClient.getInstance().currentUser)
            tvVersion.text = "V${ChatClient.VERSION}"
            tvAgreement.text = spannable
            tvAgreement.movementMethod = LinkMovementMethod.getInstance()
        }
        if (isTokenFlag) {
            switchLogin()
        }
        //切换密码可见不可见的两张图片
        eyeClose = ContextCompat.getDrawable(mContext, R.drawable.d_pwd_hide)
        eyeOpen = ContextCompat.getDrawable(mContext, R.drawable.d_pwd_show)
        //切换密码可见不可见的两张图片
        clear = ContextCompat.getDrawable(mContext, R.drawable.d_clear)
        ChatUIKitEditTextUtils.showRightDrawable(binding!!.etLoginPhone, clear)
        resetView(isDeveloperMode)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_login_register -> {

            }

            R.id.tv_login_token -> {
                isTokenFlag = !isTokenFlag
                switchLogin()
            }

            R.id.tv_version -> {
                System.arraycopy(mHits, 1, mHits, 0, mHits.size - 1)
                mHits[mHits.size - 1] = SystemClock.uptimeMillis()
                if (mHits[0] >= SystemClock.uptimeMillis() - DURATION && !isShowingDialog) {
                    isShowingDialog = true
                }
            }

            R.id.btn_login -> {
                mContext?.hideSoftKeyboard()
                loginToServer()
            }

            R.id.tv_login_reset_password -> {

            }

        }
    }

    /**
     * 切换登录方式
     */
    private fun switchLogin() {
        binding?.etLoginCode?.setText("")
        if (isTokenFlag) {
            binding?.etLoginCode?.setHint(R.string.em_login_token_hint)
            binding?.tvLoginToken?.setText(R.string.em_login_tv_pwd)
            binding?.etLoginCode?.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        } else {
            binding?.etLoginCode?.setHint(R.string.em_login_password_hint)
            binding?.tvLoginToken?.setText(R.string.em_login_tv_token)
            binding?.etLoginCode?.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    private fun loginToServer() {
        if (isDeveloperMode) {
            if (TextUtils.isEmpty(mUserPhone) || TextUtils.isEmpty(mCode)) {
                showToast(mContext!!.getString(R.string.em_login_btn_info_incomplete))
                return
            }
            if (binding?.cbSelect?.isChecked == false) {
                showToast(mContext!!.getString(R.string.em_login_not_select_agreement))
                return
            }
            lifecycleScope.launch {
                mFragmentViewModel.login(mUserPhone!!, mCode!!, isTokenFlag)
                    .catchChatException { e ->
                        if (e.errorCode == ChatError.USER_AUTHENTICATION_FAILED) {
                            showToast(R.string.demo_error_user_authentication_failed)
                        } else {
                            showToast(e.description)
                        }
                    }
                    .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                    .collect {
                        if (it != null) {
                            startActivity(Intent(mContext, MainActivity::class.java))
                            mContext!!.finish()
                        }
                    }
            }
        } else {
            if (TextUtils.isEmpty(mUserPhone)) {
                showToast(mContext!!.getString(R.string.em_login_phone_empty))
                return
            }
            if (!PhoneNumberUtils.isPhoneNumber(mUserPhone)) {
                showToast(mContext!!.getString(R.string.em_login_phone_illegal))
                return
            }
            if (TextUtils.isEmpty(mCode)) {
                showToast(R.string.em_login_code_empty)
                return
            }
            if (!PhoneNumberUtils.isNumber(mCode)) {
                showToast(mContext.getString(R.string.em_login_illegal_code))
                return
            }
            if (binding?.cbSelect?.isChecked == false) {
                showToast(mContext.getString(R.string.em_login_not_select_agreement))
                return
            }
            lifecycleScope.launch {
                mFragmentViewModel.loginFromAppServe(mUserPhone!!, mCode!!)
                    .catchChatException { e ->
                        if (e.errorCode == ChatError.USER_AUTHENTICATION_FAILED) {
                            showToast(R.string.demo_error_user_authentication_failed)
                        } else {
                            showToast(e.description)
                        }
                    }
                    .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(stopTimeoutMillis), null)
                    .collect {
                        if (it != null) {
                            startActivity(Intent(mContext, MainActivity::class.java))
                            mContext.finish()
                        }
                    }
            }

        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(s: Editable) {
        binding?.run {
            mUserPhone = etLoginPhone.text.toString().trim { it <= ' ' }
            mCode = etLoginCode.text.toString().trim { it <= ' ' }
            ChatUIKitEditTextUtils.showRightDrawable(etLoginPhone, clear)
            if (isDeveloperMode) {
                ChatUIKitEditTextUtils.showRightDrawable(etLoginCode, if (isTokenFlag) null else eyeClose)
            }
            setButtonEnable(!TextUtils.isEmpty(mUserPhone) && !TextUtils.isEmpty(mCode))
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.cb_select -> setButtonEnable(
                !TextUtils.isEmpty(mUserPhone) && !TextUtils.isEmpty(
                    mCode
                ) && isChecked
            )
        }
    }

    private fun setButtonEnable(enable: Boolean) {
        binding?.run {
            btnLogin.isEnabled = enable
            if (etLoginCode.hasFocus()) {
                etLoginCode.imeOptions =
                    if (enable) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_PREVIOUS
            } else if (etLoginPhone.hasFocus()) {
                etLoginCode.imeOptions =
                    if (enable) EditorInfo.IME_ACTION_DONE else EditorInfo.IME_ACTION_NEXT
            }
        }
    }

    private val spannable: SpannableString
        private get() {
            val language = Locale.getDefault().language
            val isZh = language.startsWith("zh")
            val spanStr = SpannableString(getString(R.string.em_login_agreement))
            var start1 = 29
            var end1 = 45
            var start2 = 50
            var end2 = spanStr.length
            if (isZh) {
                start1 = 5
                end1 = 13
                start2 = 14
                end2 = spanStr.length
            }
            //设置下划线
            //spanStr.setSpan(new UnderlineSpan(), 3, 7, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    jumpToAgreement()
                }
            }, start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanStr.setSpan(
                ForegroundColorSpan(Color.WHITE),
                start1,
                end1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            //spanStr.setSpan(new UnderlineSpan(), 10, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanStr.setSpan(object : MyClickableSpan() {
                override fun onClick(widget: View) {
                    jumpToProtocol()
                }
            }, start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spanStr.setSpan(
                ForegroundColorSpan(Color.WHITE),
                start2,
                end2,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            return spanStr
        }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!TextUtils.isEmpty(mUserPhone) && !TextUtils.isEmpty(mCode)) {
                mContext.hideSoftKeyboard()
                loginToServer()
                return true
            }
        }
        return false
    }

    private fun resetView(isDeveloperMode: Boolean) {
        binding?.run {
            etLoginCode.setText("")
            if (isDeveloperMode) {
                etLoginPhone.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                etLoginCode.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                etLoginCode.setHint(R.string.em_login_password_hint)
                etLoginPhone.setHint(R.string.em_login_name_hint)
                tvGetCode.visibility = View.GONE
                tvLoginDeveloper.visibility = View.VISIBLE
                ChatUIKitEditTextUtils.changePwdDrawableRight(
                    etLoginCode,
                eyeClose,
                eyeOpen,
                null,
                null,
                null
                )
            } else {
                etLoginPhone.inputType = InputType.TYPE_CLASS_PHONE
                etLoginCode.inputType = InputType.TYPE_CLASS_NUMBER
                etLoginCode.setHint(R.string.em_login_input_verification_code)
                etLoginPhone.setHint(R.string.register_phone_number)
                tvGetCode.visibility = View.VISIBLE
                tvLoginDeveloper.visibility = View.GONE
                ChatUIKitEditTextUtils.showRightDrawable(etLoginCode, null)
                ChatUIKitEditTextUtils.clearEditTextListener(etLoginCode)
            }
        }
    }

    private fun jumpToAgreement() {
        val uri = Uri.parse("http://www.easemob.com/agreement")
        val it = Intent(Intent.ACTION_VIEW, uri)
        startActivity(it)
    }

    private fun jumpToProtocol() {
        val uri = Uri.parse("http://www.easemob.com/protocol")
        val it = Intent(Intent.ACTION_VIEW, uri)
        startActivity(it)
    }

    private abstract inner class MyClickableSpan : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.bgColor = Color.TRANSPARENT
        }
    }
}