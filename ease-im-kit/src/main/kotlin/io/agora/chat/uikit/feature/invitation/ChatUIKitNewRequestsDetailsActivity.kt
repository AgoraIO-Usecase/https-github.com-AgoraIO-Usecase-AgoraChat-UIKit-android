package io.agora.chat.uikit.feature.invitation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import coil.load
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.databinding.UikitLayoutNewRequestDetailsBinding
import io.agora.chat.uikit.feature.contact.interfaces.IUIKitContactResultView
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import io.agora.chat.uikit.viewmodel.contacts.ChatUIKitContactListViewModel
import io.agora.chat.uikit.viewmodel.contacts.IContactListRequest
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ChatUIKitNewRequestsDetailsActivity: ChatUIKitBaseActivity<UikitLayoutNewRequestDetailsBinding>(),
    IUIKitContactResultView {
    private lateinit var user: ChatUIKitUser
    private var contactViewModel: IContactListRequest? = null
    override fun getViewBinding(inflater: LayoutInflater): UikitLayoutNewRequestDetailsBinding? {
        return UikitLayoutNewRequestDetailsBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val byteArray = intent.getByteArrayExtra(KEY_USER_INFO)
        val inputStream = ByteArrayInputStream(byteArray)
        val inputStreamReader = ObjectInputStream(inputStream)
        user = inputStreamReader.readObject() as ChatUIKitUser

        initView()
        initListener()
    }

    private fun initView(){
        contactViewModel = ViewModelProvider(this)[ChatUIKitContactListViewModel::class.java]
        contactViewModel?.attachView(this)

        binding.invitationNickName.text = user.getNickname()
        binding.invitationNumber.text = user.userId
        binding.invitationAction.text = resources.getString(R.string.uikit_invitation_detail_action)
        binding.invitationAction.isSelected = true

        val builder = ImageLoader.Builder(this)
            .placeholder(R.drawable.uikit_default_avatar)
            .error(R.drawable.uikit_default_avatar)
            .build()
        binding.invitationAvatar.load(user.avatar,builder)
    }

    private fun initListener(){
        binding.invitationTitle.setNavigationOnClickListener {
            finish()
        }

        binding.invitationAction.setOnClickListener{
            contactViewModel?.addContact(user.userId)
        }
    }

    override fun addContactSuccess(userId: String) {
        finish()
    }

    override fun addContactFail(code: Int, error: String) {

    }

    companion object {
        private const val KEY_USER_INFO = "userInfo"
        fun createIntent(
            context: Context,
            user: ChatUIKitUser
        ): Intent {
            val intent = Intent(context, ChatUIKitNewRequestsDetailsActivity::class.java)
            val outputStream = ByteArrayOutputStream()
            val outputStreamWriter = ObjectOutputStream(outputStream)
            outputStreamWriter.writeObject(user)
            outputStreamWriter.close()
            val byteArray = outputStream.toByteArray()
            intent.putExtra(KEY_USER_INFO, byteArray)

            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    return it
                }
            }
            return intent
        }
    }
}