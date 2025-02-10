package io.agora.chat.uikit.feature.contact

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.common.extensions.toProfile
import io.agora.chat.uikit.common.extensions.toUser
import io.agora.chat.uikit.configs.setAvatarStyle
import io.agora.chat.uikit.databinding.UikitActivityContactAddLayoutBinding
import io.agora.chat.uikit.feature.contact.interfaces.IUIKitContactResultView
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.provider.getSyncUser
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel
import io.agora.chat.uikit.viewmodel.contacts.IContactListRequest

open class ChatUIKitContactCheckActivity:ChatUIKitBaseActivity<UikitActivityContactAddLayoutBinding>(),
    View.OnClickListener, IUIKitContactResultView {

    var user:ChatUIKitUser?=null
    private var isContact:Boolean = false

    /**
     * The clipboard manager.
     */
    private val clipboard by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    private var contactViewModel: IContactListRequest? = null

    override fun getViewBinding(inflater: LayoutInflater): UikitActivityContactAddLayoutBinding {
        return UikitActivityContactAddLayoutBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(KEY_USER_PROFILE,ChatUIKitUser::class.java)
        }else{
            intent.getSerializableExtra(KEY_USER_PROFILE) as ChatUIKitUser?
        }

        if (user == null) finish()

        initView()
        initListener()
        initData()
    }

    fun initView(){
        ChatUIKitClient.getConfig()?.avatarConfig?.setAvatarStyle(binding.ivAvatar)
        binding.tvAddContact.isSelected = true
        ChatUIKitClient.getUserProvider()?.getSyncUser(user?.userId)?.let {
            user = it.toUser()
        }
        user?.let {
            binding.tvName.text = it.toProfile().getRemarkOrName()
            binding.tvNumber.text = resources.getString(R.string.uikit_contact_number,it.userId)
        }
    }

    open fun initData(){
        contactViewModel = ViewModelProvider(this)[ChatUIKitContactListViewModel::class.java]
        contactViewModel?.attachView(this)

        val contactsFromLocal = ChatClient.getInstance().contactManager().contactsFromLocal
        if (contactsFromLocal.isEmpty()){
            contactViewModel?.loadData(true)
        }else{
            isContact = contactsFromLocal.contains(user?.userId)
        }
        updateLayout()
    }

    private fun updateLayout(){
        if (isContact){
            user?.let {
                startActivity(ChatUIKitContactDetailsActivity.createIntent(this,it))
                finish()
            }
        }else{
            binding.titleBar.visibility = View.VISIBLE
            binding.ivAvatar.visibility = View.VISIBLE
            binding.tvName.visibility = View.VISIBLE
            binding.tvNumber.visibility = View.VISIBLE
            binding.tvAddContact.visibility = View.VISIBLE
        }
    }

    fun initListener(){
        binding.tvNumber.setOnClickListener(this)
        binding.tvAddContact.setOnClickListener(this)
        binding.titleBar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.tv_number -> {
                val indexOfSpace = binding.tvNumber.text.indexOf(":")
                if (indexOfSpace != -1) {
                    val substring = binding.tvNumber.text.substring(indexOfSpace + 1)
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            null,
                            substring
                        )
                    )
                }
            }
            R.id.tv_add_contact -> {
                user?.let {
                    contactViewModel?.addContact(it.userId)
                }
            }
            else -> {}
        }
    }

    override fun addContactSuccess(userId: String) {
        binding.tvAddContact.isSelected = false
    }

    override fun addContactFail(code: Int, error: String) {
        ChatLog.e(TAG,"addContactFail $code $error")
    }

    override fun loadContactListSuccess(list: MutableList<ChatUIKitUser>) {
        list.let { data->
            isContact = data.map { it.userId }.contains(user?.userId)
            updateLayout()
        }
    }

    override fun loadContactListFail(code: Int, error: String) {
        ChatLog.e(TAG,"loadContactListFail $code $error")
        finish()
    }

    companion object {
        private const val TAG = "ChatUIKitContactCheckActivity"
        private const val KEY_USER_PROFILE = "user_profile"

        fun createIntent(
            context: Context,
            user: ChatUIKitUser
        ): Intent {
            val intent = Intent(context, ChatUIKitContactCheckActivity::class.java)
            intent.putExtra(KEY_USER_PROFILE, user)
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    return it
                }
            }
            return intent
        }
    }


}