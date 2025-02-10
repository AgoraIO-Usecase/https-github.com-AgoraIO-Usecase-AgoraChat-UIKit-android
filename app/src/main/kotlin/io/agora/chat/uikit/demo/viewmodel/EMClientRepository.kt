package io.agora.chat.uikit.demo.viewmodel

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatError
import io.agora.chat.uikit.common.ChatException
import io.agora.chat.uikit.common.ChatHttpClientManager
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatValueCallback
import io.agora.chat.uikit.demo.BuildConfig
import io.agora.chat.uikit.demo.DemoApplication
import io.agora.chat.uikit.demo.R
import io.agora.chat.uikit.demo.base.ErrorCode
import io.agora.chat.uikit.demo.bean.LoginResult
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.model.ChatUIKitUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 作为EMClient的repository,处理EMClient相关的逻辑
 */
class EMClientRepository {
    /**
     * 登录过后需要加载的数据
     * @return
     */
    suspend fun loadAllInfoFromHX(): Boolean =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                if (ChatClient.getInstance().isLoggedInBefore && ChatClient.getInstance().options.autoLogin) {
                    loadAllConversationsAndGroups()
                    continuation.resume(true)
                } else {
                    continuation.resumeWithException(ChatException(ErrorCode.EM_NOT_LOGIN, ""))
                }
            }
        }

    /**
     * 从本地数据库加载所有的对话及群组
     */
    private fun loadAllConversationsAndGroups() {
        // 从本地数据库加载所有的对话及群组
        ChatClient.getInstance().chatManager().loadAllConversations()
        ChatClient.getInstance().groupManager().loadAllGroups()
    }

    /**
     * 注册
     * @param userName
     * @param pwd
     * @return
     */
    suspend fun registerToHx(userName: String?, pwd: String?): String? =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                try {
                    ChatClient.getInstance().createAccount(userName, pwd)
                    continuation.resume(userName)
                } catch (e: ChatException) {
                    continuation.resumeWithException(ChatException(e.errorCode, e.message))
                }
            }
        }

    /**
     * 登录到服务器，可选择密码登录或者token登录
     * @param userName
     * @param pwd
     * @param isTokenFlag
     * @return
     */
    suspend fun loginToServer(
        userName: String,
        pwd: String,
        isTokenFlag: Boolean
    ): ChatUIKitUser =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                if (isTokenFlag) {
                    ChatUIKitClient.login(ChatUIKitProfile(userName), pwd, onSuccess = {
                        successForCallBack(continuation)
                    }, onError = { code, error ->
                        continuation.resumeWithException(ChatException(code, error))
                    })
                } else {
                    ChatUIKitClient.login(userName, pwd, onSuccess = {
                        successForCallBack(continuation)
                    }, onError = { code, error ->
                        continuation.resumeWithException(ChatException(code, error))
                    })
                }
            }
        }

    /**
     * 退出登录
     * @param unbindDeviceToken
     * @return
     */
    suspend fun logout(unbindDeviceToken: Boolean): Int =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                ChatUIKitClient.logout(unbindDeviceToken, onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                }, onError = { code, error ->
                    continuation.resumeWithException(ChatException(code, error))
                })
            }
        }

    private fun successForCallBack(continuation: Continuation<ChatUIKitUser>) {
        // get current user id
        val currentUser = ChatClient.getInstance().currentUser
        val user = ChatUIKitUser(currentUser)
        continuation.resume(user)

        // ** manually load all local groups and conversation
        loadAllConversationsAndGroups()
    }

    suspend fun loginFromServe(userName: String, userPassword: String): LoginResult =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                LoginFromAppServe(userName, userPassword, object : ChatValueCallback<LoginResult> {
                    override fun onSuccess(value: LoginResult?) {
                        continuation.resume(value!!)
                    }

                    override fun onError(code: Int, error: String?) {
                        continuation.resumeWithException(ChatException(code, error))
                    }
                })
            }
        }

    private fun LoginFromAppServe(
        userName: String,
        userPassword: String,
        callBack: ChatValueCallback<LoginResult>
    ) {
        try {
            val headers: MutableMap<String, String> = HashMap()
            headers["Content-Type"] = "application/json"
            val request = JSONObject()
            request.putOpt("phoneNumber", userName)
            request.putOpt("smsCode", userPassword)
            val url: String =
                BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_BASE_USER + BuildConfig.APP_SERVER_LOGIN
            ChatLog.d("LoginToAppServer url : ", url)
            val response = ChatHttpClientManager.httpExecute(
                url,
                headers,
                request.toString(),
                ChatHttpClientManager.Method_POST
            )
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                ChatLog.d("LoginToAppServer success : ", responseInfo)
                val `object` = JSONObject(responseInfo)
                val result = LoginResult()
                val phoneNumber = `object`.getString("phoneNumber")
                result.phone = phoneNumber
                result.token = `object`.getString("token")
                result.username = `object`.getString("chatUserName")
                result.code = code
                callBack.onSuccess(result)
            } else {
                if (responseInfo != null && responseInfo.length > 0) {
                    var errorInfo: String? = null
                    try {
                        val `object` = JSONObject(responseInfo)
                        errorInfo = `object`.getString("errorInfo")
                        if (errorInfo.contains("phone number illegal")) {
                            errorInfo = DemoApplication.getInstance().getString(R.string.em_login_phone_illegal)
                        } else if (errorInfo.contains("verification code error") || errorInfo.contains(
                                "send SMS to get mobile phone verification code"
                            )
                        ) {
                            errorInfo = DemoApplication.getInstance().getString(R.string.em_login_illegal_code)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    callBack.onError(code, errorInfo)
                } else {
                    callBack.onError(code, responseInfo)
                }
            }
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }
}