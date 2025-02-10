package io.agora.chat.uikit.interfaces

import io.agora.chat.uikit.model.ChatUIKitPreview

interface UrlPreviewStatusCallback {
    fun onParsing(){}
    fun onParseFile(){}
    fun onDownloadStart(){}
    fun onDownloadFinish(preview: ChatUIKitPreview?){}
}