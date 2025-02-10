package io.agora.chat.uikit.demo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

class LoginFragmentViewModel(application: Application) : AndroidViewModel(application) {
    private val mRepository: EMClientRepository = EMClientRepository()

    /**
     * 登录环信
     * @param userName
     * @param pwd
     * @param isTokenFlag
     */
    fun login(userName: String, pwd: String, isTokenFlag: Boolean) =
        flow {
            emit(mRepository.loginToServer(userName, pwd, isTokenFlag))
        }

    /**
     * 通过AppServe授权登录
     * @param userName
     * @param userPassword
     */
    fun loginFromAppServe(userName: String, userPassword: String) =
        flow {
            emit(mRepository.loginFromServe(userName, userPassword))
        }
            .flatMapConcat { result ->
                flow { emit(mRepository.loginToServer(userName, result.token!!, true)) }
            }

}
