package io.agora.chat.uikit.feature.chat.controllers

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentManager
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatPathUtils
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatCustomMessageBody
import io.agora.chat.uikit.common.ChatImageUtils
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatVersionUtils
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.common.dialog.CustomDialog
import io.agora.chat.uikit.common.extensions.getUserInfo
import io.agora.chat.uikit.common.extensions.isSdcardExist
import io.agora.chat.uikit.common.utils.ChatUIKitCompat
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils
import io.agora.chat.uikit.common.dialog.ChatUIKitContactBottomSheetFragment
import io.agora.chat.uikit.feature.chat.UIKitChatFragment
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitLayout
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitType
import io.agora.chat.uikit.feature.chat.enums.getConversationType
import io.agora.chat.uikit.common.helper.ChatUIKitDingMessageHelper
import io.agora.chat.uikit.interfaces.OnUserListItemClickListener
import io.agora.chat.uikit.model.ChatUIKitUser
import io.agora.chat.uikit.model.getNickname
import io.agora.chat.uikit.provider.getSyncProfile
import io.agora.chat.uikit.provider.getSyncUser
import java.io.File
import java.io.IOException

open class ChatUIKitAttachmentController(
    private val mContext: Activity,
    private val layoutChat: ChatUIKitLayout?,
    private val conversationId: String?,
    private val sendOriginalImage: Boolean,
) {

    private var cameraFile: File? = null


    /**
     * select picture from camera
     */
    fun selectPicFromCamera(launcher: ActivityResultLauncher<Intent>?) {
        if (!isSdcardExist()) {
            return
        }
        cameraFile = File(
            ChatPathUtils.getInstance().imagePath, (ChatClient.getInstance().currentUser
                    + System.currentTimeMillis()) + ".jpg"
        )
        cameraFile?.let {
            it.parentFile?.mkdirs()
            launcher?.launch(
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(
                    MediaStore.EXTRA_OUTPUT, ChatUIKitCompat.getUriForFile(
                        mContext, it
                    )
                )
            )
        }

    }

    /**
     * select local image
     */
    fun selectPicFromLocal(launcher: ActivityResultLauncher<Intent>?) {
        ChatUIKitCompat.openImageByLauncher(launcher, mContext)
    }

    /**
     * select local video
     */
    fun selectVideoFromLocal(launcher: ActivityResultLauncher<Intent>?) {
        val intent = Intent()
        if (ChatVersionUtils.isTargetQ(mContext)) {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.action = Intent.ACTION_GET_CONTENT
            } else {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "video/*"
        launcher?.launch(intent)
    }

    /**
     * select local file
     */
    fun selectFileFromLocal(launcher: ActivityResultLauncher<Intent>?) {
        val intent = Intent()
        if (ChatVersionUtils.isTargetQ(mContext)) {
            intent.action = Intent.ACTION_OPEN_DOCUMENT
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.action = Intent.ACTION_GET_CONTENT
            } else {
                intent.action = Intent.ACTION_OPEN_DOCUMENT
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        launcher?.launch(intent)
    }

    /**
     * Select contact from contact list fragment.
     */
    fun selectContact(fragmentManager: FragmentManager, chatType: ChatUIKitType) {
        val fragment = ChatUIKitContactBottomSheetFragment()
        fragment.setOnUserListItemClickListener(object : OnUserListItemClickListener {

            override fun onUserListItemClick(v: View?, position: Int, user: ChatUIKitUser?) {
                super.onUserListItemClick(v, position, user)
                var showName = when (chatType) {
                    ChatUIKitType.GROUP_CHAT -> {
                        val groupName = ChatUIKitClient.getGroupProfileProvider()?.getSyncProfile(conversationId)?.name
                        if (groupName.isNullOrEmpty().not()) {
                            groupName
                        } else {
                            ChatClient.getInstance().groupManager().getGroup(conversationId)?.groupName
                                ?: conversationId
                        }
                    }
                    ChatUIKitType.CHATROOM -> {
                        ChatClient.getInstance().chatroomManager().getChatRoom(conversationId)?.name
                            ?: conversationId
                    }
                    else -> {
                        val nickname = ChatUIKitClient.getUserProvider()?.getSyncUser(conversationId)?.getRemarkOrName()
                        if (nickname.isNullOrEmpty() || nickname == conversationId) {
                            ChatClient.getInstance().chatManager().getConversation(conversationId)?.latestMessageFromOthers?.getUserInfo()?.name
                                ?: conversationId
                        } else {
                            nickname
                        }
                    }
                }
                CustomDialog(mContext
                    , mContext.getString(R.string.uikit_chat_message_user_card_select_title)
                    , mContext.getString(R.string.uikit_chat_message_user_card_share_content, user?.getNickname(), showName)
                    , false
                    , onRightButtonClickListener = {
                        val message = ChatMessage.createSendMessage(ChatMessageType.CUSTOM)
                        val body = ChatCustomMessageBody(ChatUIKitConstant.USER_CARD_EVENT)
                        val params: MutableMap<String, String> = HashMap()
                        params[ChatUIKitConstant.USER_CARD_ID] = user?.userId ?: ""
                        params[ChatUIKitConstant.USER_CARD_NICK] = user?.nickname ?: ""
                        params[ChatUIKitConstant.USER_CARD_AVATAR] = user?.avatar ?: ""
                        body.params = params
                        message.body = body
                        message.to = conversationId
                        layoutChat?.sendMessage(message)
                        fragment.hide()
                    }).show()
            }
        })
        fragment.show(fragmentManager, "selectContact")
    }


    /**
     * It's the result from ActivityResultLauncher.
     * @param result
     * @param requestCode
     */
    fun onActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            layoutChat?.chatInputMenu?.hideExtendContainer()
            if (requestCode == UIKitChatFragment.REQUEST_CODE_CAMERA) { // capture new image
                onActivityResultForCamera(data)
            } else if (requestCode == UIKitChatFragment.REQUEST_CODE_LOCAL) { // send local image
                onActivityResultForLocalPhotos(data)
            } else if (requestCode == UIKitChatFragment.REQUEST_CODE_DING_MSG) { // To send the ding-type msg.
                onActivityResultForDingMsg(data)
            } else if (requestCode == UIKitChatFragment.REQUEST_CODE_SELECT_FILE) {
                onActivityResultForLocalFiles(data)
            } else if (UIKitChatFragment.REQUEST_CODE_SELECT_VIDEO == requestCode) {
                onActivityResultForLocalVideos(data)
            }
        }
    }

    private fun onActivityResultForCamera(data: Intent?) {
        if (cameraFile != null && cameraFile!!.exists()) {
            var uri = Uri.parse(cameraFile!!.getAbsolutePath())
            // Check if the image is rotated and restore it
            if (sendOriginalImage) {
                uri = ChatImageUtils.checkDegreeAndRestoreImage(mContext, uri)
            }
            layoutChat?.sendImageMessage(uri, sendOriginalImage)
        }
    }

    private fun onActivityResultForLocalPhotos(data: Intent?) {
        if (data != null) {
            val selectedImage = data.data
            if (selectedImage != null) {
                val filePath: String = ChatUIKitFileUtils.getFilePath(mContext, selectedImage)
                if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                    layoutChat?.sendImageMessage(Uri.parse(filePath), sendOriginalImage)
                } else {
                    ChatUIKitFileUtils.saveUriPermission(mContext, selectedImage, data)
                    layoutChat?.sendImageMessage(selectedImage, sendOriginalImage)
                }
            }
        }
    }

    private fun onActivityResultForDingMsg(data: Intent?) {
        if (data != null) {
            val msgContent = data.getStringExtra("msg")
            ChatLog.i(UIKitChatFragment.TAG, "To send the ding-type msg, content: $msgContent")
            // Send the ding-type msg.
            val dingMsg: ChatMessage =
                ChatUIKitDingMessageHelper.get().createDingMessage(conversationId, msgContent)
            layoutChat?.sendMessage(dingMsg)
        }
    }

    private fun onActivityResultForLocalFiles(data: Intent?) {
        if (data != null) {
            val uri = data.data
            if (uri != null) {
                val filePath: String = ChatUIKitFileUtils.getFilePath(mContext, uri)
                if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                    layoutChat?.sendFileMessage(Uri.parse(filePath))
                } else {
                    ChatUIKitFileUtils.saveUriPermission(mContext, uri, data)
                    layoutChat?.sendFileMessage(uri)
                }
            }
        }
    }

    private fun onActivityResultForLocalVideos(data: Intent?) {
        if (data != null) {
            val uri = data.data
            val mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(mContext, uri!!)
                mediaPlayer.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val duration = mediaPlayer.duration
            ChatLog.d(UIKitChatFragment.TAG, "path = " + uri!!.path + ",duration=" + duration)
            ChatUIKitFileUtils.saveUriPermission(mContext, uri, data)
            layoutChat?.sendVideoMessage(uri, duration)
        }
    }

}