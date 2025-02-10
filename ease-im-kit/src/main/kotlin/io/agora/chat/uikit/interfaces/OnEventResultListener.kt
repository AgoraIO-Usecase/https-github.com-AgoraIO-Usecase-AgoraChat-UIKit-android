package io.agora.chat.uikit.interfaces

interface OnEventResultListener {
    fun onEventResult(function:String, errorCode: Int, errorMessage: String?)
}