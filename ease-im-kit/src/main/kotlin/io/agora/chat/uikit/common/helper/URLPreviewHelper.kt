package io.agora.chat.uikit.common.helper

import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.interfaces.UrlPreviewStatusCallback
import io.agora.chat.uikit.model.ChatUIKitPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object URLPreviewHelper {

    fun matchHTMLContent(html: String): ChatUIKitPreview {
        val content = ChatUIKitPreview()

        // Open Graph 协议的正则表达式模式
        val titleOGPattern = ChatUIKitClient.getConfig()?.chatConfig?.titleOGPattern
        val descriptionOGPattern = ChatUIKitClient.getConfig()?.chatConfig?.descriptionOGPattern
        val imageOGPattern = ChatUIKitClient.getConfig()?.chatConfig?.imageOGPattern

        // 非 Open Graph 协议的正则表达式模式
        val titlePattern = ChatUIKitClient.getConfig()?.chatConfig?.titlePattern
        val descriptionPattern = ChatUIKitClient.getConfig()?.chatConfig?.descriptionPattern
        val imagePattern = ChatUIKitClient.getConfig()?.chatConfig?.imagePattern
        val imageSrcPattern = ChatUIKitClient.getConfig()?.chatConfig?.imageSrcPattern

        // 提取 Open Graph 协议中的 title
        val titleOGMatch = titleOGPattern?.toRegex()?.find(html)
        if (titleOGMatch != null) {
            val titleOG = titleOGMatch.groups[0]?.value
            titleOG?.let {
                val startRange = titleOG.indexOf("content=\"")
                val endRange = titleOG.indexOf("\"", startRange + 9)
                if (startRange != -1 && endRange != -1) {
                    content.title = titleOG.substring(startRange + 9, endRange)
                }
            }
        } else {
            // 如果没有找到 Open Graph 协议的内容，则提取非 OG 协议的内容
            if (content.title == null) {
                val titleMatch = titlePattern?.toRegex()?.find(html)
                if (titleMatch != null) {
                    val title = titleMatch.groups[0]?.value
                    title?.let {
                        val startRange = title.indexOf("<title>")
                        val endRange = title.indexOf("</title>")
                        if (startRange != -1 && endRange != -1) {
                            content.title = title.substring(startRange + 7, endRange)
                        }
                    }
                }
            }
        }

        // 提取 Open Graph 协议中的 description
        val descriptionOGMatch = descriptionOGPattern?.toRegex()?.find(html)
        if (descriptionOGMatch != null) {
            val descriptionOG = descriptionOGMatch.groups[0]?.value
            descriptionOG?.let {
                val startRange = descriptionOG.indexOf("content=\"")
                val endRange = descriptionOG.indexOf("\"", startRange + 9)
                if (startRange != -1 && endRange != -1) {
                    content.description = descriptionOG.substring(startRange + 9, endRange)
                }
            }
        } else {
            if (content.description == null) {
                val descriptionMatch = descriptionPattern?.toRegex()?.find(html)
                if (descriptionMatch != null) {
                    val description = descriptionMatch.groups[0]?.value
                    description?.let {
                        val startRange = description.indexOf("content=\"")
                        val endRange = description.indexOf("\"", startRange + 9)
                        if (startRange != -1 && endRange != -1) {
                            content.description = description.substring(startRange + 9, endRange)
                        }
                    }
                }
            }
        }

        // 提取 Open Graph 协议中的 image
        val imageOGMatch = imageOGPattern?.toRegex()?.find(html)
        if (imageOGMatch != null) {
            val imageOG = imageOGMatch.groups[0]?.value
            imageOG?.let {
                val startRange = imageOG.indexOf("content=\"")
                val endRange = imageOG.indexOf("\"", startRange + 9)
                if (startRange != -1 && endRange != -1) {
                    content.imageURL = imageOG.substring(startRange + 9, endRange)
                }
            }
        } else {
            if (content.imageURL == null) {
                val imageSrcMatch = imageSrcPattern?.toRegex()?.find(html)
                if (imageSrcMatch != null) {
                    val imageSrc = imageSrcMatch.groups[0]?.value
                    imageSrc?.let {
                        val startRange = imageSrc.indexOf("href=\"")
                        val endRange = imageSrc.indexOf("\"", startRange + 6)
                        if (startRange != -1 && endRange != -1) {
                            content.imageURL = imageSrc.substring(startRange + 6, endRange)
                        }
                    }
                } else {
                    val imageMatch = imagePattern?.toRegex()?.find(html)
                    if (imageMatch != null) {
                        val imageTag = imageMatch.groups[0]?.value
                        imageTag?.let {
                            var result = ""
                            val startRange = imageTag.indexOf("src=")
                            val sub = imageTag.substring(startRange+4, imageTag.length)
                            val index = sub.indexOf(" ")
                            result = if (index != -1){
                                checkUrl(sub.substring(0,index))
                            }else{
                                checkUrl(sub)
                            }
                            content.imageURL = result
                        }
                    }
                }
            }
        }

        return content
    }

    private fun checkUrl(url:String):String{
        if (url.startsWith("//")) {
            return "https:$url"
        }
        if (!url.startsWith("https://")){
            if (url.indexOf("http") != -1){
                return url.replaceFirst("http", "https")
            }
        }
        return url
    }

    fun downLoadHtmlByUrl(
        targetUrl:String,
        callback: UrlPreviewStatusCallback
    ){
        CoroutineScope(Dispatchers.Main).launch {
            callback.onDownloadStart()

            val htmlString = withContext(Dispatchers.IO) {
                var result = ""
                try {
                    val parseUrl = URL(checkUrl(targetUrl))
                    val connection = parseUrl.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    val responseCode = connection.responseCode
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    reader.close()
                    connection.disconnect()
                    ChatLog.e("URLPreviewHelper","download responseCode $responseCode")
                    if (responseCode == 200){
                        result = response.toString()
                        ChatLog.e("URLPreviewHelper","result $result")
                    }
                }catch (e:Exception){
                    callback.onDownloadFinish(null)
                }
                result
            }
            if (htmlString.isEmpty()){
                callback.onDownloadFinish(null)
            }else{
                val bean = matchHTMLContent(htmlString)
                bean.url = targetUrl
                callback.onDownloadFinish(bean)
            }
        }
    }


}
