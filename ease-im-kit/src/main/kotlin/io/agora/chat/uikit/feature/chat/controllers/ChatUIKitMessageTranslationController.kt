package io.agora.chat.uikit.feature.chat.controllers

import android.content.Context
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatUIKitConstant
import io.agora.chat.uikit.feature.chat.enums.ChatUIKitLoadDataType
import io.agora.chat.uikit.feature.chat.widgets.ChatUIKitLayout
import io.agora.chat.uikit.viewmodel.messages.IChatViewRequest

class ChatUIKitMessageTranslationController(
    private val context: Context,
    private val chatLayout: ChatUIKitLayout,
    private val viewModel: IChatViewRequest?,
) {
    private var isThread:Boolean = false

    fun showTranslationMessage(message: ChatMessage?){
        ChatUIKitClient.getConfig()?.chatConfig?.targetTranslationLanguage?.let {
            viewModel?.translationMessage(message, mutableListOf(it))
        }
    }

    fun hideTranslationMessage(message: ChatMessage?){
        viewModel?.hideTranslationMessage(message)
    }

    fun setConversationType(isThread: ChatUIKitLoadDataType){
        this.isThread = isThread == ChatUIKitLoadDataType.THREAD
    }

    fun isShowTranslation(message: ChatMessage?):Boolean{
        val containsKey = message?.ext()?.containsKey(ChatUIKitConstant.TRANSLATION_STATUS)
        message?.let {
            if (it.body is ChatTextMessageBody){
                val body = it.body as ChatTextMessageBody
                val translations = body.translations
                containsKey?.let { hasKey->
                    if (hasKey){
                        val isTranslation = it.getBooleanAttribute(ChatUIKitConstant.TRANSLATION_STATUS)
                        isTranslation.let {
                            return it
                        }
                    }
                }
                return translations.size > 0
            }
        }
        return false
    }

}