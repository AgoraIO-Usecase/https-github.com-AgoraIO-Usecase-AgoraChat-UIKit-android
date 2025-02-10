package io.agora.chat.uikit.demo

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.feature.chat.activities.UIKitChatActivity
import io.agora.chat.uikit.common.ChatConnectionListener
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatOptions
import io.agora.chat.uikit.common.impl.OnValueSuccess
import io.agora.chat.uikit.common.extensions.showToast
import io.agora.chat.uikit.common.helper.ChatUIKitPreferenceManager
import io.agora.chat.uikit.demo.base.UserActivityLifecycleCallbacks
import io.agora.chat.uikit.demo.login.LoginActivity
import io.agora.chat.uikit.feature.thread.ChatUIKitThreadActivity
import io.agora.chat.uikit.interfaces.ChatUIKitConnectionListener
import io.agora.chat.uikit.provider.ChatUIKitCustomActivityRoute
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.provider.ChatUIKitUserProfileProvider
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout

class DemoApplication: Application() {
    private val mLifecycleCallbacks = UserActivityLifecycleCallbacks()
    override fun onCreate() {
        super.onCreate()
        instance = this
        registerActivityLifecycleCallbacks()

        val appkey = BuildConfig.APPKEY
        if (appkey.isNullOrEmpty()) {
            showToast("APPKEY is null or empty")
            Log.e("app","APPKEY is null or empty")
            return
        }
        val options = ChatOptions()
        options.appKey = appkey
        options.acceptInvitationAlways = false
        options.requireDeliveryAck = true
        ChatUIKitClient.init(this, options)

        ChatUIKitClient.setCustomActivityRoute(object : ChatUIKitCustomActivityRoute {
            override fun getActivityRoute(intent: Intent): Intent {
                if (intent.component?.className == UIKitChatActivity::class.java.name) {
                    intent.setClass(this@DemoApplication, ChatActivity::class.java)
                }else if (intent.component?.className == ChatUIKitThreadActivity::class.java.name){
                    intent.setClass(this@DemoApplication, ChatThreadActivity::class.java)
                }
                return intent
            }

        })

        ChatUIKitClient.setUserProfileProvider(object : ChatUIKitUserProfileProvider{
            override fun getUser(userId: String?): ChatUIKitProfile? {
                return getLocalGroupMemberInfo(userId)
            }

            override fun fetchUsers(
                userIds: List<String>,
                onValueSuccess: OnValueSuccess<List<ChatUIKitProfile>>
            ) {

            }

        })

        ChatUIKitClient.addConnectionListener(object : ChatUIKitConnectionListener() {
            override fun onConnected() {

            }

            override fun onDisconnected(errorCode: Int) {
                ChatLog.e("app","onDisconnected: $errorCode")
            }

            override fun onLogout(errorCode: Int, info: String?) {
                super.onLogout(errorCode, info)
                ChatLog.e("app","onLogout: $errorCode")
                mLifecycleCallbacks.activityList.forEach {
                    it.finish()
                }
                LoginActivity.startAction(instance)
            }

        })

        // Call this method after ChatUIKitClient#init
        val isBlack = ChatUIKitPreferenceManager.getInstance().getBoolean("isBlack")
        AppCompatDelegate.setDefaultNightMode(if (isBlack) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
    }

    private fun registerActivityLifecycleCallbacks() {
        this.registerActivityLifecycleCallbacks(mLifecycleCallbacks)
    }

    fun getLifecycleCallbacks(): UserActivityLifecycleCallbacks? {
        return mLifecycleCallbacks
    }

    /**
     * Set default settings for SmartRefreshLayout
     */

    init {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            ClassicsHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, layout ->
            ClassicsFooter(context)
        }
    }

    companion object {
        private lateinit var instance: DemoApplication
        fun getInstance(): DemoApplication {
            return instance
        }
    }


    fun getLocalGroupMemberInfo(username:String?): ChatUIKitProfile?{
        var profile:ChatUIKitProfile? = null
        when(username){
            "apex" -> {
                profile =  ChatUIKitProfile(
                    id = "apex",
                    name = "房主Host",
                    avatar = "https://a1.easemob.com/easemob/chatroom-uikit/chatfiles/b837f7b0-79f8-11ee-b817-23850e48ca47"
                )
            }
            "apex1" -> {
                profile =  ChatUIKitProfile(
                    id = "apex1",
                    name = "测试昵称",
                    avatar = "https://a1.easemob.com/easemob/chatroom-uikit/chatfiles/99296020-79f8-11ee-8475-c7a7b59db79f"
                )
            }
            "lxm" -> {
                profile =  ChatUIKitProfile(
                    id = "lxm",
                    name = "大威天龙",
                    avatar = "https://a1.easemob.com/easemob/chatroom-uikit/chatfiles/16bc4980-79f9-11ee-b272-3568dd301252"
                )
            }
            else -> { }
        }
        return profile
    }
}