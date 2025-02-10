package io.agora.chat.uikit.common.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatPathUtils
import io.agora.chat.uikit.common.FileHelper
import io.agora.chat.uikit.common.extensions.savePermission
import io.agora.chat.uikit.common.extensions.takePersistablePermission
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object ChatUIKitFileUtils {
    private val TAG = ChatUIKitFileUtils::class.java.simpleName
    private val isQ: Boolean
        private get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun isFileExistByUri(context: Context?, fileUri: Uri?): Boolean {
        return FileHelper.getInstance().isFileExist(fileUri)
    }

    /**
     * Delete file
     * @param context
     * @param uri
     */
    fun deleteFile(context: Context, uri: Uri?) {
        if (isFileExistByUri(context, uri)) {
            val filePath = getFilePath(context, uri)
            if (!TextUtils.isEmpty(filePath)) {
                val file = File(filePath)
                if (file != null && file.exists() && file.isFile) {
                    file.delete()
                }
            } else {
                try {
                    context.contentResolver.delete(uri!!, null, null)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * Get file name
     * @param context
     * @param fileUri
     * @return
     */
    fun getFileNameByUri(context: Context?, fileUri: Uri?): String {
        return FileHelper.getInstance().getFilename(fileUri)
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    fun getFilePath(context: Context?, uri: Uri?): String {
        return FileHelper.getInstance().getFilePath(uri)
    }

    /**
     * Determine whether uri starts with file
     * @param fileUri
     * @return
     */
    fun uriStartWithFile(fileUri: Uri): Boolean {
        return "file".equals(fileUri.scheme, ignoreCase = true) && fileUri.toString().length > 7
    }

    /**
     * Determine whether uri starts with content
     * @param fileUri
     * @return
     */
    fun uriStartWithContent(fileUri: Uri): Boolean {
        return "content".equals(fileUri.scheme, ignoreCase = true)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * Is it the FileProvider of this app
     * @param context
     * @param uri
     * @return
     */
    fun isFileProvider(context: Context, uri: Uri): Boolean {
        return (context.applicationInfo.packageName + ".fileProvider").equals(
            uri.authority,
            ignoreCase = true
        )
    }

    /**
     * FileProvider shared by other apps
     * @param context
     * @param uri
     * @return
     */
    fun isOtherFileProvider(context: Context, uri: Uri): Boolean {
        val scheme = uri.scheme
        val authority = uri.authority
        return if (TextUtils.isEmpty(scheme) || TextUtils.isEmpty(authority)) {
            false
        } else (!(context.applicationInfo.packageName + ".fileProvider").equals(
            uri.authority,
            ignoreCase = true
        )
                && "content".equals(uri.scheme, ignoreCase = true)
                && authority!!.contains(".fileProvider".lowercase(Locale.getDefault())))
    }

    @SuppressLint("WrongConstant")
    fun saveUriPermission(context: Context?, fileUri: Uri?, intent: Intent?): Boolean {
        return fileUri?.savePermission(context, intent) ?: false
    }

    private fun getLastSubFromUri(fileUri: Uri?): String {
        if (fileUri == null) {
            return ""
        }
        val uri = fileUri.toString()
        if (!uri.contains("/")) {
            return ""
        }
        val lastIndex = uri.lastIndexOf("/")
        return uri.substring(lastIndex + 1)
    }

    /**
     * Get permanent read permission for Uri
     * @param context
     * @param uri
     * @return
     */
    fun takePersistableUriPermission(context: Context?, uri: Uri?): Uri? {
        return uri?.takePersistablePermission(context)
    }

    fun getThumbPath(context: Context?, videoUri: Uri?): String {
        if (!isFileExistByUri(context, videoUri)) {
            return ""
        }
        val filePath = getFilePath(context, videoUri)
        val file: File = File(
            ChatPathUtils.getInstance().getVideoPath(),
            "thvideo" + System.currentTimeMillis() + ".jpeg"
        )
        var createSuccess = true
        if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
            try {
                val fos = FileOutputStream(file)
                val ThumbBitmap: Bitmap? = ThumbnailUtils.createVideoThumbnail(filePath, 3)
                ThumbBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
                ChatLog.e(TAG, e.message)
                createSuccess = false
            }
        } else {
            try {
                val fos = FileOutputStream(file)
                val media = MediaMetadataRetriever()
                media.setDataSource(context, videoUri)
                val frameAtTime: Bitmap? = media.getFrameAtTime()
                frameAtTime?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()
            } catch (e: Exception) {
                e.printStackTrace()
                ChatLog.e(TAG, e.message)
                createSuccess = false
            }
        }
        return if (createSuccess) file.absolutePath else ""
    }
}