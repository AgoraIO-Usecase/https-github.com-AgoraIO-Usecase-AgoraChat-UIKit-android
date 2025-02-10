package io.agora.chat.uikit.feature.thread

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.base.ChatUIKitBaseActivity
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.extensions.hasRoute
import io.agora.chat.uikit.common.permission.PermissionCompat
import io.agora.chat.uikit.common.permission.PermissionsManager
import io.agora.chat.uikit.databinding.UikitActivityChatThreadBinding
import io.agora.chat.uikit.feature.chat.interfaces.OnChatExtendMenuItemClickListener
import io.agora.chat.uikit.feature.chat.interfaces.OnChatRecordTouchListener
import io.agora.chat.uikit.feature.thread.fragment.ChatUIKitCreateThreadFragment

open class ChatUIKitCreateThreadActivity: ChatUIKitBaseActivity<UikitActivityChatThreadBinding>() {

    private var conversationId: String? = ""
    private var topicMsgId: String? = ""
    private var fragment: ChatUIKitCreateThreadFragment? = null

    private val requestImagePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_PICTURE
            )
        }
    private val requestVideoPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_VIDEO
            )
        }
    private val requestFilePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_FILE
            )
        }

    override fun getViewBinding(inflater: LayoutInflater): UikitActivityChatThreadBinding {
        return UikitActivityChatThreadBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        conversationId = intent.getStringExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID)
        topicMsgId = intent.getStringExtra(ChatUIKitConstant.THREAD_TOPIC_MESSAGE_ID)

        initData()
    }

    open fun initData(){
        val builder = ChatUIKitCreateThreadFragment.Builder(conversationId,topicMsgId)
        builder.setOnChatExtendMenuItemClickListener(object : OnChatExtendMenuItemClickListener{
            override fun onChatExtendMenuItemClick(view: View?, itemId: Int): Boolean {
                when(itemId) {
                    R.id.extend_item_take_picture -> {
                        if (!PermissionsManager.getInstance()
                                .hasPermission(mContext, Manifest.permission.CAMERA)
                        ) {
                            PermissionsManager.getInstance()
                                .requestPermissionsIfNecessaryForResult(
                                    mContext,
                                    arrayOf(Manifest.permission.CAMERA),
                                    null
                                )
                            return true
                        }
                    }
                    R.id.extend_item_picture -> {
                        if (!PermissionCompat.checkMediaPermission(
                                mContext,
                                requestImagePermission,
                                Manifest.permission.READ_MEDIA_IMAGES
                            )
                        ) {
                            return true
                        }
                    }

                    R.id.extend_item_video -> {
                        if (!PermissionCompat.checkMediaPermission(
                                mContext,
                                requestVideoPermission,
                                Manifest.permission.READ_MEDIA_VIDEO,
                                Manifest.permission.CAMERA
                            )
                        ) {
                            return true
                        }
                    }

                    R.id.extend_item_file -> {
                        if (!PermissionCompat.checkMediaPermission(
                                mContext,
                                requestFilePermission,
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        ) {
                            return true
                        }
                    }
                }
                return false
            }
        })
        .setOnChatRecordTouchListener(object : OnChatRecordTouchListener {
            override fun onRecordTouch(v: View?, event: MotionEvent?): Boolean {
                if (!PermissionsManager.getInstance()
                        .hasPermission(mContext, Manifest.permission.RECORD_AUDIO)
                ) {
                    PermissionsManager.getInstance()
                        .requestPermissionsIfNecessaryForResult(
                            mContext,
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            null
                        )
                    return true
                }
                return false
            }
        })
        setChildSettings(builder)
        fragment = builder.build()
        fragment?.let { fragment ->
            supportFragmentManager.beginTransaction().replace(binding.flFragment.id, fragment).commit()
        }
    }

    protected open fun setChildSettings(builder: ChatUIKitCreateThreadFragment.Builder) {}


    private fun onRequestResult(result: Map<String, Boolean>?, requestCode: Int) {
        if (!result.isNullOrEmpty()) {
            for ((key, value) in result) {
                ChatLog.e(TAG, "onRequestResult: $key  $value")
            }
            if (PermissionCompat.getMediaAccess(mContext) !== PermissionCompat.StorageAccess.Denied) {
                if (requestCode == REQUEST_CODE_STORAGE_PICTURE) {
                    fragment?.selectPicFromLocal()
                } else if (requestCode == REQUEST_CODE_STORAGE_VIDEO) {
                    fragment?.selectVideoFromLocal()
                } else if (requestCode == REQUEST_CODE_STORAGE_FILE) {
                    fragment?.selectFileFromLocal()
                }
            }
        }
    }

    companion object {
        private val TAG = ChatUIKitCreateThreadActivity::class.java.simpleName
        private const val REQUEST_CODE_STORAGE_PICTURE = 111
        private const val REQUEST_CODE_STORAGE_VIDEO = 112
        private const val REQUEST_CODE_STORAGE_FILE = 113
        fun actionStart(context: Context, parentId:String?, msgId:String?) {
            val intent = Intent(context, ChatUIKitCreateThreadActivity::class.java)
            parentId?.let {
                intent.putExtra(ChatUIKitConstant.EXTRA_CONVERSATION_ID,it)
            }
            msgId?.let {
                intent.putExtra(ChatUIKitConstant.THREAD_TOPIC_MESSAGE_ID,it)
            }
            ChatUIKitClient.getCustomActivityRoute()?.getActivityRoute(intent.clone() as Intent)?.let {
                if (it.hasRoute()) {
                    context.startActivity(it)
                    return
                }
            }
            context.startActivity(intent)
        }
    }
}