package io.agora.chat.uikit.common.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatPathUtils
import io.agora.chat.uikit.common.ChatVersionUtils
import io.agora.chat.uikit.common.extensions.isSdcardExist
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * Created by zhangsong on 18-6-6.
 */
object ChatUIKitCompat {
    private const val TAG = "ChatUIKitCompat"
    fun openImage(context: Activity, requestCode: Int) {
        val intent: Intent? = getOpenImageIntent(context)
        context.startActivityForResult(intent, requestCode)
    }

    /**
     * Open the system album.
     * @param context
     * @param requestCode
     */
    @Deprecated("Use {@link #openImage(ActivityResultLauncher, Context)} instead.")
    fun openImage(context: Fragment, requestCode: Int) {
        val intent: Intent = getOpenImageIntent(context.activity)
        context.startActivityForResult(intent, requestCode)
    }

    /**
     * Open the system album.
     * @param launcher
     * @param context
     */
    fun openImageByLauncher(launcher: ActivityResultLauncher<Intent>?, context: Context) {
        launcher?.launch(getOpenImageIntent(context))
    }

    private fun getOpenImageIntent(context: Context?): Intent {
        var intent: Intent? = null
        if (ChatVersionUtils.isTargetQ(context)) {
            intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
        } else {
            intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.type = "image/*"
        return intent
    }

    /**
     * take picture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    fun takePicture(context: Activity, requestCode: Int): File? {
        if (!isSdcardExist()) {
            return null
        }
        val cameraFile = cameraFile
        val intent: Intent = getCameraIntent(context, cameraFile)
        context.startActivityForResult(intent, requestCode)
        return cameraFile
    }

    /**
     * take picture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    fun takePicture(context: Fragment, requestCode: Int): File? {
        if (!isSdcardExist()) {
            return null
        }
        val cameraFile = cameraFile
        val intent: Intent = getCameraIntent(context.context, cameraFile)
        context.startActivityForResult(intent, requestCode)
        return cameraFile
    }

    /**
     * take video capture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    fun takeVideo(context: Activity, requestCode: Int): File? {
        if (!isSdcardExist()) {
            return null
        }
        val videoFile = videoFile
        val intent: Intent = getVideoIntent(context, videoFile)
        context.startActivityForResult(intent, requestCode)
        return videoFile
    }

    /**
     * take video capture by set file path
     * @param context
     * @param requestCode
     * @return
     */
    fun takeVideo(context: Fragment, requestCode: Int): File? {
        if (!isSdcardExist()) {
            return null
        }
        val videoFile = videoFile
        val intent: Intent = getVideoIntent(context.context, videoFile)
        context.startActivityForResult(intent, requestCode)
        return videoFile
    }

    private fun getCameraIntent(context: Context?, cameraFile: File): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(context, cameraFile))
        return intent
    }

    private val cameraFile: File
        private get() {
            val cameraFile: File = File(
                ChatPathUtils.getInstance().getImagePath(),
                (ChatClient.getInstance()
                    .getCurrentUser() + System.currentTimeMillis()).toString() + ".jpg"
            )
            cameraFile.parentFile.mkdirs()
            return cameraFile
        }

    private fun getVideoIntent(context: Context?, videoFile: File): Intent {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getUriForFile(context, videoFile))
        return intent
    }

    private val videoFile: File
        private get() {
            val videoFile: File = File(
                ChatPathUtils.getInstance().getVideoPath(),
                System.currentTimeMillis().toString() + ".mp4"
            )
            videoFile.parentFile.mkdirs()
            return videoFile
        }

    /**
     * Open file
     *
     * @param f
     * @param context
     */
    fun openFile(f: File?, context: Activity) {
        openFile(context, f)
    }

    /**
     * Open file
     * @param context
     * @param filePath
     */
    fun openFile(context: Context, filePath: String?) {
        if (TextUtils.isEmpty(filePath) || !File(filePath).exists()) {
            ChatLog.e(TAG, "File does not exist！")
            return
        }
        openFile(context, File(filePath))
    }

    /**
     * Open file
     * @param context
     * @param file
     */
    fun openFile(context: Context, file: File?) {
        if (file == null || !file.exists()) {
            ChatLog.e(TAG, "Cannot open the file, because the file is not exit, file: $file")
            return
        }
        val filename = file.name
        val mimeType = getMimeType(context, file)
        /* get uri */
        val uri = getUriForFile(context, file)
        //To solve the local video file can not open the problem
//        if(isVideoFile(context, filename)) {
//            uri = Uri.parse(file.getAbsolutePath());
//        }
        openFile(context, uri, filename, mimeType)
    }

    /**
     * Open file
     * @param context
     * @param uri
     */
    fun openFile(context: Context, uri: Uri) {
        if (!ChatUIKitFileUtils.isFileExistByUri(context, uri)) {
            ChatLog.e(TAG, "Cannot open the file, because the file is not exit, uri: $uri")
            return
        }
        val filePath = ChatUIKitFileUtils.getFilePath(context, uri)
        if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
            openFile(context, File(filePath))
            return
        }
        val filename = getFileNameByUri(context, uri)
        val mimeType = getMimeType(context, filename)
        openFile(context, uri, filename, mimeType)
    }

    /**
     * Open file
     * @param context
     * @param uri
     * @param filename
     * @param mimeType
     */
    private fun openFile(context: Context, uri: Uri?, filename: String, mimeType: String) {
        if (openApk(context, uri, filename)) {
            return
        }
        ChatLog.d(TAG, "openFile filename = $filename mimeType = $mimeType")
        ChatLog.d(TAG, "openFile uri = " + (uri?.toString() ?: "uri is null"))
        val intent = Intent(Intent.ACTION_VIEW)
        setIntentByType(context, filename, intent)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        /* set intent's file and MimeType */intent.setDataAndType(uri, mimeType)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            ChatLog.e(TAG, e.message)
            Toast.makeText(context, "Can't find proper app to open this file", Toast.LENGTH_LONG)
                .show()
        }
    }

    /**
     * Delete file
     * @param context
     * @param uri
     */
    fun deleteFile(context: Context?, uri: Uri?) {
        ChatUIKitFileUtils.deleteFile(context!!, uri)
    }

    fun getUriForFile(context: Context?, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context!!,
                context.packageName + ".fileProvider",
                file
            )
        } else {
            Uri.fromFile(file)
        }
    }

    val supportedWindowType: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

    /**
     * Gets the first frame of the video
     * @param context
     * @param videoUri
     * @return
     */
    fun getVideoThumbnail(context: Context?, videoUri: Uri): String? {
        val file: File =
            File(ChatPathUtils.getInstance().getVideoPath(), "thvideo" + System.currentTimeMillis())
        try {
            val fos = FileOutputStream(file)
            val media = MediaMetadataRetriever()
            media.setDataSource(context, videoUri)
            val frameAtTime: Bitmap? = media.getFrameAtTime()
            frameAtTime?.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return file.absolutePath
    }

    /**
     * Used to check whether a file obtained from multimedia is a video
     * @param context
     * @param uri
     * @return
     */
    fun isVideoType(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return if (TextUtils.isEmpty(mimeType)) {
            false
        } else mimeType!!.startsWith("video")
    }

    /**
     * Used to check whether a file obtained from multimedia is a picture
     * @param context
     * @param uri
     * @return
     */
    fun isImageType(context: Context, uri: Uri): Boolean {
        val mimeType = getMimeType(context, uri)
        return if (TextUtils.isEmpty(mimeType)) {
            false
        } else mimeType!!.startsWith("image")
    }

    /**
     * Get mime type
     * @param context
     * @param uri
     * @return
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }

    fun getMimeType(context: Context, file: File): String {
        return getMimeType(context, file.name)
    }

    fun getMimeType(context: Context, filename: String): String {
        var mimeType: String? = null
        val resources = context.resources
        mimeType = if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_image_file_suffix)
            )
        ) {
            "image/*"
        } else if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_video_file_suffix)
            )
        ) {
            "video/*"
        } else if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_audio_file_suffix)
            )
        ) {
            "audio/*"
        } else if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_file_file_suffix)
            )
        ) {
            "text/plain"
        } else if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_word_file_suffix)
            )
        ) {
            "application/msword"
        } else if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_excel_file_suffix)
            )
        ) {
            "application/vnd.ms-excel"
        } else if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_pdf_file_suffix)
            )
        ) {
            "application/pdf"
        } else if (checkSuffix(
                filename,
                resources.getStringArray(R.array.ease_apk_file_suffix)
            )
        ) {
            "application/vnd.android.package-archive"
        } else {
            "application/octet-stream"
        }
        return mimeType
    }

    /**
     * Check whether it is a video file
     * @param context
     * @param filename
     * @return
     */
    fun isVideoFile(context: Context, filename: String): Boolean {
        return checkSuffix(
            filename,
            context.resources.getStringArray(R.array.ease_video_file_suffix)
        )
    }

    fun setIntentByType(context: Context, filename: String, intent: Intent) {
        val rs = context.resources
        if (checkSuffix(filename, rs.getStringArray(R.array.ease_audio_file_suffix))
            || checkSuffix(filename, rs.getStringArray(R.array.ease_video_file_suffix))
        ) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra("oneshot", 0)
            intent.putExtra("configchange", 0)
        } else if (checkSuffix(filename, rs.getStringArray(R.array.ease_image_file_suffix))) {
            intent.addCategory("android.intent.category.DEFAULT")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else if (checkSuffix(filename, rs.getStringArray(R.array.ease_excel_file_suffix))
            || checkSuffix(filename, rs.getStringArray(R.array.ease_word_file_suffix))
            || checkSuffix(filename, rs.getStringArray(R.array.ease_pdf_file_suffix))
        ) {
            intent.addCategory("android.intent.category.DEFAULT")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            intent.addCategory("android.intent.category.DEFAULT")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun openApk(context: Context, uri: Uri?): Boolean {
        val filename = getFileNameByUri(context, uri)
        return openApk(context, uri, filename)
    }

    private fun openApk(context: Context, uri: Uri?, filename: String): Boolean {
        if (filename.endsWith(".apk")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.setDataAndType(uri, getMimeType(context, filename))
                context.startActivity(intent)
            } else {
                val installIntent = Intent(Intent.ACTION_VIEW)
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                installIntent.setDataAndType(uri, getMimeType(context, filename))
                context.startActivity(installIntent)
            }
            return true
        }
        return false
    }

    /**
     * Check suffix
     * @param filename
     * @param fileSuffix
     * @return
     */
    fun checkSuffix(filename: String, fileSuffix: Array<String?>?): Boolean {
        if (TextUtils.isEmpty(filename) || fileSuffix == null || fileSuffix.size <= 0) {
            return false
        }
        val length = fileSuffix.size
        for (i in 0 until length) {
            val suffix = fileSuffix[i]
            if (filename.lowercase(Locale.getDefault()).endsWith(suffix!!)) {
                return true
            }
        }
        return false
    }

    /**
     * Get file name
     * @param context
     * @param fileUri
     * @return
     */
    fun getFileNameByUri(context: Context?, fileUri: Uri?): String {
        return ChatUIKitFileUtils.getFileNameByUri(context, fileUri)
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
    fun getPath(context: Context?, uri: Uri?): String {
        return ChatUIKitFileUtils.getFilePath(context, uri)
    }
}