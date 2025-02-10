package io.agora.chat.uikit.common.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.FileHelper
import io.agora.chat.uikit.common.helper.ChatUIKitPreferenceManager


/**
 * Judge whether the Uri is a content scheme
 */
fun Uri.isContentScheme(): Boolean {
    return "content".equals(scheme, ignoreCase = true)
}

private fun Uri.getLastSub(): String {
    val uri = toString()
    if (!uri.contains("/")) {
        return ""
    }
    val lastIndex = uri.lastIndexOf("/")
    return uri.substring(lastIndex + 1)
}

/**
 * Save Uri permission after Android 10.
 */
fun Uri.savePermission(context: Context?, intent: Intent?): Boolean {
    if (context == null) {
        return false
    }
    if (!isContentScheme()) {
        return false
    }
    var intentFlags = intent?.flags ?: 0
    val takeFlags =
        intentFlags and (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    var last: String? = null
    try {
        context.contentResolver.takePersistableUriPermission(this, takeFlags)
        last = getLastSub()
        ChatLog.d("Uri", "saveUriPermission last part of Uri: $last")
    } catch (e: SecurityException) {
        ChatLog.e("Uri", "saveUriPermission failed e: " + e.message)
    }
    if (!last.isNullOrEmpty()) {
        ChatUIKitPreferenceManager.getInstance().putString(last, toString())
        return true
    }
    return false
}

/**
 * Get permanent read permission for Uri.
 */
fun Uri.takePersistablePermission(context: Context?): Uri? {
    if (context == null) {
        return null
    }
    if (!isContentScheme()) {
        return null
    }
    val last = getLastSub()
    if (!last.isNullOrEmpty()) {
        val fileUri = ChatUIKitPreferenceManager.getInstance().getString(last)
        if (!fileUri.isNullOrEmpty()) {
            return try {
                context.contentResolver.takePersistableUriPermission(
                    Uri.parse(fileUri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Uri.parse(fileUri)
            } catch (e: SecurityException) {
                ChatLog.e("ChatUIKitFileUtils", "takePersistableUriPermission failed e: " + e.message)
                null
            }
        }
    }
    try {
        context.contentResolver.takePersistableUriPermission(
            this,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (e: SecurityException) {
        ChatLog.e("ChatUIKitFileUtils", "takePersistableUriPermission failed e: " + e.message)
        return null
    }
    return this
}

/**
 * Judge whether the file Uri exists.
 */
fun Uri.isFileExist(context: Context?): Boolean {
    if (context == null) {
        return false
    }
    return FileHelper.getInstance().isFileExist(context, this)
}