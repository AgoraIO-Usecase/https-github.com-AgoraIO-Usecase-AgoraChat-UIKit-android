/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agora.chat.uikit.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import coil.load
import coil.request.CachePolicy
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatImageMessageBody
import io.agora.chat.uikit.common.ChatImageUtils
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatPathUtils
import io.agora.chat.uikit.common.ChatVideoMessageBody
import io.agora.chat.uikit.common.extensions.getScreenInfo
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils.isFileExistByUri
import io.agora.chat.uikit.common.utils.ChatUIKitFileUtils.takePersistableUriPermission
import io.agora.util.EMLog
import io.agora.util.UriUtils
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

object ChatUIKitImageUtils : ChatImageUtils() {
    private val TAG = javaClass.simpleName
    fun getImagePath(remoteUrl: String): String {
        val imageName = remoteUrl.substring(remoteUrl.lastIndexOf("/") + 1, remoteUrl.length)
        val path = ChatPathUtils.getInstance().imagePath.toString() + "/" + imageName
        ChatLog.d("msg", "image path:$path")
        return path
    }

    fun getImagePathByFileName(filename: String): String {
        val path = ChatPathUtils.getInstance().imagePath.toString() + "/" + filename
        ChatLog.d("msg", "image path:$path")
        return path
    }

    fun getThumbnailImagePath(thumbRemoteUrl: String): String {
        val thumbImageName =
            thumbRemoteUrl.substring(thumbRemoteUrl.lastIndexOf("/") + 1, thumbRemoteUrl.length)
        val path = ChatPathUtils.getInstance().imagePath.toString() + "/" + "th" + thumbImageName
        ChatLog.d("msg", "thum image path:$path")
        return path
    }

    fun getThumbnailImagePathByName(filename: String): String {
        val path = ChatPathUtils.getInstance().imagePath.toString() + "/" + "th" + filename
        ChatLog.d("msg", "thum image dgdfg path:$path")
        return path
    }

    /**
     * Get the maximum length and width of the picture
     * @param context
     */
    fun getImageMaxSize(context: Context?): IntArray {
        val screenInfo: FloatArray? = context?.getScreenInfo()
        val maxSize = IntArray(2)
        if (screenInfo != null) {
            maxSize[0] = (screenInfo[0] * (ChatUIKitClient.getConfig()?.chatConfig?.maxShowWidthRadio ?: 0.3f)).toInt()
            maxSize[1] = (screenInfo[0] * (ChatUIKitClient.getConfig()?.chatConfig?.maxShowHeightRadio ?: 0.5f)).toInt()
        }
        return maxSize
    }

    /**
     * Show video cover
     * @param context
     * @param imageView
     * @param message
     * @return
     */
    fun showVideoThumb(context: Context?, imageView: ImageView?, message: ChatMessage?) {
        if (context == null || imageView == null || message == null) return
        (message.body as? ChatVideoMessageBody)?.run {
            var width: Int = thumbnailWidth
            var height: Int = thumbnailHeight
            var localThumbUri: Uri? = localThumbUri
            takePersistableUriPermission(context, localThumbUri)
            val thumbnailUrl: String = thumbnailUrl
            if (!isFileExistByUri(context, localThumbUri)) {
                localThumbUri = null
            } else {
                if (width == 0 || height == 0) {
                    var options: BitmapFactory.Options? = null
                    try {
                        options = getBitmapOptions(context, localThumbUri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (options != null) {
                        width = options.outWidth
                        height = options.outHeight
                    }
                }
            }
            showImage(context, imageView, localThumbUri, thumbnailUrl, width, height, true)
        }
    }

    fun getImageShowSize(context: Context?, message: ChatMessage?): ViewGroup.LayoutParams {
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        if (context == null || message == null || message.body !is ChatImageMessageBody) return params

        (message.body as ChatImageMessageBody).run {
            var width: Int = width
            var height: Int = height
            var imageUri: Uri? = localUri
            if (!isFileExistByUri(context, imageUri)) {
                imageUri = thumbnailLocalUri()
                if (!isFileExistByUri(context, imageUri)) {
                    imageUri = null
                }
            }
            if (width == 0 || height == 0) {
                var options: BitmapFactory.Options? = null
                try {
                    options = getBitmapOptions(context, imageUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (options != null) {
                    width = options.outWidth
                    height = options.outHeight
                }
            }
            val maxSize = getImageMaxSize(context)
            val maxWidth = maxSize[0]
            val maxHeight = maxSize[1]
            val mRadio = maxWidth * 1.0f / maxHeight
            var radio = width * 1.0f / if (height == 0) 1 else height
            if (radio == 0f) {
                radio = 1f
            }
            if (maxHeight == 0 && maxWidth == 0 /*|| (width <= maxWidth && height <= maxHeight)*/) {
                return params
            }
            when(radio) {
                // If radio is less than 1/10, the middle part will be cut off to display the 1/10 part.
                in 0f..1/10f -> {
                    params.width = (maxHeight / 10f).toInt()
                    params.height = maxHeight
                }
                // If radio is more than 0.1f and less than 3/4f
                in 1/10f..3/4f -> {
                    // If the original image height is less than the maximum show height, the original image height is used
                    if (height < maxHeight) {
                        params.width = (height * radio).toInt()
                        params.height = height
                    } else { // If the original image height is greater than the maximum show height, the maximum show height is used
                        params.width = (maxHeight * radio).toInt()
                        params.height = maxHeight
                    }
                }
                in 3/4f..10f -> {
                    if (width < maxWidth) {
                        params.width = width
                        params.height = (width / radio).toInt()
                    } else {
                        params.width = maxWidth
                        params.height = (maxWidth / radio).toInt()
                    }
                }
                else -> {
                    params.width = maxWidth
                    params.height = (maxWidth / 10f).toInt()
                }
            }
        }
        return params
    }

    /**
     * Show picture
     * @param context
     * @param imageView
     * @param message
     * @return
     */
    fun showImage(context: Context?, imageView: ImageView?, message: ChatMessage?) {
        if (context == null || imageView == null || message == null) return

        if (message.body !is ChatImageMessageBody) return

        (message.body as ChatImageMessageBody).let { imageMessageBody ->
            var width: Int = imageMessageBody.width
            var height: Int = imageMessageBody.height
            var imageUri: Uri? = imageMessageBody.localUri
            takePersistableUriPermission(context, imageUri)
            ChatLog.e(
                "tag",
                "current show small view big file: uri:$imageUri exist: ${isFileExistByUri(
                    context,
                    imageUri
                )}"
            )
            // Judge whether exist local path
            if (!isFileExistByUri(context, imageUri)) {
                imageUri = imageMessageBody.thumbnailLocalUri()
                takePersistableUriPermission(context, imageUri)
                ChatLog.e(
                    "tag",
                    "current show small view thumbnail file: uri:$imageUri exist: ${isFileExistByUri(
                        context,
                        imageUri
                    )}"
                )
                if (!isFileExistByUri(context, imageUri)) {
                    //context.revokeUriPermission(imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imageUri = null
                }
            }
            // If local uri exists, reset the width and height
            if (width == 0 || height == 0) {
                var options: BitmapFactory.Options? = null
                try {
                    options = getBitmapOptions(context, imageUri)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (options != null) {
                    width = options.outWidth
                    height = options.outHeight
                }
            }
            if (imageUri != null && isFileExistByUri(context, imageUri)) {
                showImage(context, imageView, imageUri, null, width, height)
                return
            }
            var thumbnailUrl: String? = null
            // if not auto transfer image, get the remote url
            if (!ChatClient.getInstance().options.autoTransferMessageAttachments) {
                thumbnailUrl = imageMessageBody.thumbnailUrl
                if (TextUtils.isEmpty(thumbnailUrl)) {
                    thumbnailUrl = imageMessageBody.remoteUrl
                }
                showImage(context, imageView, imageUri, thumbnailUrl, width, height)
                return
            }
            // download attachment should in view or viewHolder
        }
    }

    /**
     * The logic of displaying pictures is as follows:
     * 1. The width of the picture does not exceed 1/3 of the screen width,
     * and the height does not exceed 1/2 of the screen width. In this case,
     * the aspect ratio of the picture is 3:2
     * 2. If the aspect ratio of the picture is greater than 3:2,
     * select the height direction to be consistent with the regulations,
     * and the width direction will be scaled proportionally
     * 3. If the aspect ratio of the picture is less than 3:2,
     * select the width direction to be consistent with the regulations,
     * and the height direction is scaled proportionally
     * 4. If the length and width of the picture are small,
     * just display it according to the size of the picture
     * 5. If there is no local resource, show the server address
     * @param context
     * @param imageView
     * @param imageUri Picture local resources
     * @param imageUrl Server picture address
     * @param imgWidth
     * @param imgHeight
     * @return
     */
    fun showImage(
        context: Context?,
        imageView: ImageView,
        imageUri: Uri?,
        imageUrl: String?,
        imgWidth: Int,
        imgHeight: Int,
        isVideo: Boolean = false
    ) {
        val maxSize = getImageMaxSize(context)
        val maxWidth = maxSize[0]
        val maxHeight = maxSize[1]
        val mRadio = maxWidth * 1.0f / maxHeight
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        var radio = imgWidth * 1.0f / if (imgHeight == 0) 1 else imgHeight
        if (radio == 0f) {
            radio = 1f
        }
        if (maxHeight == 0 && maxWidth == 0 /*|| (width <= maxWidth && height <= maxHeight)*/) {
            if (context is Activity && (context.isFinishing || context.isDestroyed)) {
                return
            }
            imageView.load(imageUri ?: imageUrl) {
                diskCachePolicy(CachePolicy.ENABLED)
            }
            return
        }
        val params = imageView.layoutParams

        when(radio) {
            // If radio is less than 1/10, the middle part will be cut off to display the 1/10 part.
            in 0f..1/10f -> {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                params.width = (maxHeight / 10f).toInt()
                params.height = maxHeight
            }
            // If radio is more than 0.1f and less than 3/4f
            in 1/10f..3/4f -> {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                // If the original image height is less than the maximum show height, the original image height is used
                if (imgHeight < maxHeight) {
                    params.width = (imgHeight * radio).toInt()
                    params.height = imgHeight
                } else { // If the original image height is greater than the maximum show height, the maximum show height is used
                    params.width = (maxHeight * radio).toInt()
                    params.height = maxHeight
                }
            }
            in 3/4f..10f -> {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                if (imgWidth < maxWidth) {
                    params.width = imgWidth
                    params.height = (imgWidth / radio).toInt()
                } else {
                    params.width = maxWidth
                    params.height = (maxWidth / radio).toInt()
                }
            }
            else -> {
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                params.width = maxWidth
                params.height = (maxWidth / 10f).toInt()
            }
        }
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }
        imageView.load(imageUri ?: imageUrl) {
            error(if (isVideo) R.drawable.uikit_default_video_thumbnail else R.drawable.uikit_default_image)
            diskCachePolicy(CachePolicy.ENABLED)
            if (params.width != 0 && params.height != 0) {
                size(params.width, params.height)
            }
            listener(
                onStart = {
                    ChatLog.d("msg", "start load image")
                },
                onSuccess = { _, _ ->
                    ChatLog.d("msg", "load image success")
                },
                onError = { _, throwable ->
                    ChatLog.e("msg", "load image error: ${throwable.throwable.message}")
                    imageView.context.mainScope().launch {
                        imageView.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                }
            )
        }
    }

    fun setDrawableSize(textView: TextView, defaultSize: Float) {
        val existingAbs = textView.compoundDrawables
        val left = existingAbs[0]
        val right = existingAbs[2]
        if (left != null && defaultSize != 0f && defaultSize != 0f) {
            left.setBounds(0, 0, defaultSize.toInt(), defaultSize.toInt())
        }
        if (right != null && defaultSize != 0f && defaultSize != 0f) {
            right.setBounds(0, 0, defaultSize.toInt(), defaultSize.toInt())
        }
        textView.setCompoundDrawables(
            left ?: existingAbs[0],
            existingAbs[1],
            right ?: existingAbs[2],
            existingAbs[3]
        )
    }

    /**
     * Convert Drawable to Bitmap.
     * @param drawable
     * @return
     */
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Get a drawable with corner radius.
     * @param context
     * @param bitmap
     * @param cornerRadius
     * @return
     */
    fun getRoundedCornerDrawable(
        context: Context,
        bitmap: Bitmap?,
        cornerRadius: Float
    ): Drawable? {
        if (bitmap == null) {
            return null
        }
        val roundedDrawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
        roundedDrawable.cornerRadius = cornerRadius
        roundedDrawable.setAntiAlias(true)
        return roundedDrawable
    }

    @Throws(IOException::class)
    fun imageToJpeg(context: Context?, srcImg: Uri?, destFile: File?): Uri? {
        val bitmap: Bitmap?
        val filePath = ChatUIKitFileUtils.getFilePath(context, srcImg)
        bitmap = if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
            BitmapFactory.decodeFile(filePath, null)
        } else {
            getBitmapByUri(context, srcImg, null)
        }
        if (null != bitmap && null != destFile) {
            if (destFile.exists()) {
                destFile.delete()
            }
            val out = FileOutputStream(destFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            out.flush()
            out.close()
            return Uri.fromFile(destFile)
        }
        return srcImg
    }

    fun handleImageHeifToJpeg(context: Context?, imageUri: Uri?,destPath:String): Uri? {
        try {
            val options: BitmapFactory.Options
            val filePath = ChatUIKitFileUtils.getFilePath(context, imageUri)
            options = if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                getBitmapOptions(filePath)
            } else {
                getBitmapOptions(context, imageUri)
            }
            if ("image/heif".equals(options.outMimeType, ignoreCase = true)) {
                val fileNameByUri = UriUtils.getFileNameByUri(context, imageUri)
                val nameWithoutExtension = removeLastExtension(fileNameByUri)
                return imageToJpeg(context, imageUri, File(destPath,"$nameWithoutExtension.jpeg"))
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            EMLog.e(TAG,"handleImageHeifToJpeg:" + e.message)
        }
        EMLog.d(TAG,"handleImageHeifToJpeg:imageUri=$imageUri")
        return imageUri
    }

    fun removeLastExtension(str: String): String? {
        if (TextUtils.isEmpty(str)) {
            return UUID.randomUUID().toString()
        }
        val lastDotIndex = str.lastIndexOf('.')
        return if (lastDotIndex == -1) {
            str // 如果没有找到.，返回原字符串
        } else str.substring(0, lastDotIndex)
    }
}