/************************************************************
 * * Hyphenate CONFIDENTIAL
 * __________________
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Hyphenate Inc.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Hyphenate Inc.
 */
package io.agora.chat.uikit.common.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Vibrator
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.R
import io.agora.chat.uikit.common.ChatLog
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.extensions.isSilentMessage
import io.agora.chat.uikit.provider.ChatUIKitSettingsProvider
import java.util.Locale

/**
 * new message notifier class
 *
 * this class is subject to be inherited and implement the relative APIs
 *
 * On devices prior to Android 8.0:
 * The sound and vibration of notifications in the notification bar can be controlled by the'sound' and'vibration' switches in the demo settings
 * On Android 8.0 devices:
 * The sound and vibration of notifications in the notification bar are not controlled by the'sound' and'vibration' switches in the demo settings
 */
open class ChatUIKitNotifier(context: Context) {
    protected val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
    protected var fromUsers = HashSet<String>()
    protected var notificationNum = 0
    protected var appContext: Context
    protected var packageName: String
    protected var msg: String
    protected var lastNotifyTime: Long = 0
    protected var ringtone: Ringtone? = null
    protected var audioManager: AudioManager
    protected var vibrator: Vibrator
    private var _notificationInfoProvider: ChatUIKitNotificationInfoProvider? = null

    init {
        appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= 26) {
            // Create the notification channel for Android 8.0
            val channel = NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.uikit_notifier_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.vibrationPattern = VIBRATION_PATTERN
            notificationManager.createNotificationChannel(channel)
        }
        packageName = appContext.applicationInfo.packageName
        msg = appContext.getString(R.string.uikit_notifier_default_content)
        audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    /**
     * this function can be override
     */
    fun reset() {
        resetNotificationCount()
        cancelNotification()
    }

    fun resetNotificationCount() {
        notificationNum = 0
        fromUsers.clear()
    }

    fun cancelNotification() {
        notificationManager.cancel(NOTIFY_ID)
    }

    /**
     * handle the new message
     * this function can be override
     *
     * @param message
     */
    @Synchronized
    fun notify(message: ChatMessage) {
        if (message.isSilentMessage()) {
            return
        }
        val settingsProvider: ChatUIKitSettingsProvider? = ChatUIKitClient.getSettingsProvider()
        if (settingsProvider == null || !settingsProvider.isMsgNotifyAllowed(message)) {
            return
        }

        notificationNum++
        fromUsers.add(message.from)
        handleMessage(message)
    }

    @Synchronized
    fun notify(messages: List<ChatMessage?>) {
        if (messages.isNullOrEmpty()) return
        if (messages[messages.size - 1]?.isSilentMessage() == true) {
            return
        }
        val settingsProvider: ChatUIKitSettingsProvider? = ChatUIKitClient.getSettingsProvider()
        if (settingsProvider?.isMsgNotifyAllowed(null) == false) {
            return
        }

        for (message in messages) {
            notificationNum++
            fromUsers.add(message!!.from)
        }
        handleMessage(messages[messages.size - 1])
    }

    @Synchronized
    fun notify(content: String?) {
        try {
            val builder = generateBaseBuilder(content)
            val notification = builder.build()
            notificationManager.notify(NOTIFY_ID, notification)
            if (Build.VERSION.SDK_INT < 26) {
                vibrateAndPlayTone(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Applicable to Android10, the limitation of starting Activity from the background
     * @param fullScreenIntent
     * @param title
     * @param content
     */
    @Synchronized
    fun notify(fullScreenIntent: Intent, title: String?, content: String) {
        try {
            val builder = generateBaseFullIntentBuilder(fullScreenIntent, content)
            if (!TextUtils.isEmpty(title)) {
                builder.setContentTitle(title)
            }
            val notification = builder.build()
            notificationManager.notify(NOTIFY_ID, notification)
            if (Build.VERSION.SDK_INT < 26) {
                vibrateAndPlayTone(null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * send it to notification bar
     * This can be override by subclass to provide customer implementation
     *
     * @param message
     */
    protected fun handleMessage(message: ChatMessage?) {
        try {
            val fromUsersNum = fromUsers.size
            var notifyText: String? = String.format(msg, fromUsersNum, notificationNum)
            val builder = generateBaseBuilder(notifyText)
            _notificationInfoProvider?.let {
                val contentTitle = it.getTitle(message)
                if (!contentTitle.isNullOrEmpty()) {
                    builder.setContentTitle(contentTitle)
                }
                notifyText = it.getDisplayedText(message)
                if (!notifyText.isNullOrEmpty()) {
                    builder.setTicker(notifyText)
                }
                it.getLaunchIntent(message)?.let { intent ->
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(
                        appContext,
                        NOTIFY_ID,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                    builder.setContentIntent(pendingIntent)
                }
                notifyText = it.getLatestText(message, fromUsersNum, notificationNum)
                if (!notifyText.isNullOrEmpty()) {
                    builder.setContentText(notifyText)
                }
                val smallIcon = it.getSmallIcon(message)
                if (smallIcon != 0) {
                    builder.setSmallIcon(smallIcon)
                }
            }
            val notification = builder.build()
            notificationManager.notify(NOTIFY_ID, notification)
            if (Build.VERSION.SDK_INT < 26) {
                vibrateAndPlayTone(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Generate a base Notification#Builder, contains:
     * 1.Use the app icon as default icon
     * 2.Use the app name as default title
     * 3.This notification would be sent immediately
     * 4.Can be cancelled by user
     * 5.Would launch the default activity when be clicked
     *
     * @return
     */
    private fun generateBaseBuilder(content: String?): NotificationCompat.Builder {
        val pm: PackageManager = appContext.packageManager
        val title: String = pm.getApplicationLabel(appContext.applicationInfo).toString()
        val i: Intent? = appContext.packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, NOTIFY_ID, i, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(appContext.applicationInfo.icon)
            .setContentTitle(title)
            .setTicker(content)
            .setContentText(content)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    /**
     * Generate a base Notification#Builder to replace of start background activity.
     * @param fullScreenIntent
     * @param content
     * @return
     */
    private fun generateBaseFullIntentBuilder(
        fullScreenIntent: Intent,
        content: String
    ): NotificationCompat.Builder {
        val pm: PackageManager = appContext.packageManager
        val title: String = pm.getApplicationLabel(appContext.applicationInfo).toString()
        val fullScreenPendingIntent: PendingIntent = PendingIntent.getActivity(
            appContext, NOTIFY_ID,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(appContext.applicationInfo.icon)
            .setContentTitle(title)
            .setTicker(content)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
    }

    /**
     * vibrate and  play tone
     */
    fun vibrateAndPlayTone(message: ChatMessage?) {
        if (message?.isSilentMessage() == true) {
            return
        }
        val settingsProvider: ChatUIKitSettingsProvider? = ChatUIKitClient.getSettingsProvider()
        if (settingsProvider?.isMsgNotifyAllowed(null) == false) {
            return
        }
        if (System.currentTimeMillis() - lastNotifyTime < 1000) {
            // received new messages within 2 seconds, skip play ringtone
            return
        }
        try {
            lastNotifyTime = System.currentTimeMillis()

            // check if in silent mode
            if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                ChatLog.e(TAG, "in silent mode now")
                return
            }
            if (settingsProvider?.isMsgVibrateAllowed(message) == true) {
                vibrator.vibrate(VIBRATION_PATTERN, -1)
            }
            if (settingsProvider?.isMsgSoundAllowed(message) == true) {
                if (ringtone == null) {
                    val notificationUri: Uri =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ringtone = RingtoneManager.getRingtone(appContext, notificationUri)
                    if (ringtone == null) {
                        ChatLog.d(TAG, "cant find ringtone at:" + notificationUri.path)
                        return
                    }
                }
                if (ringtone?.isPlaying == false) {
                    val vendor = Build.MANUFACTURER
                    ringtone?.play()
                    // for samsung S3, we meet a bug that the phone will
                    // continue ringtone without stop
                    // so add below special handler to stop it after 3s if
                    // needed
                    if (vendor != null && vendor.lowercase(Locale.getDefault())
                            .contains("samsung")
                    ) {
                        val ctlThread: Thread = object : Thread() {
                            override fun run() {
                                try {
                                    sleep(3000)
                                    if (ringtone?.isPlaying == true) {
                                        ringtone?.stop()
                                    }
                                } catch (e: Exception) {
                                }
                            }
                        }
                        ctlThread.run()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * set notification info Provider
     *
     * @param provider
     */
    fun setNotificationInfoProvider(provider: ChatUIKitNotificationInfoProvider?) {
        _notificationInfoProvider = provider
    }

    interface ChatUIKitNotificationInfoProvider {
        /**
         * set the notification content, such as "you received a new image from xxx"
         *
         * @param message
         * @return null-will use the default text
         */
        fun getDisplayedText(message: ChatMessage?): String?

        /**
         * set the notification content: such as "you received 5 message from 2 contacts"
         *
         * @param message
         * @param fromUsersNum- number of message sender
         * @param messageNum    -number of messages
         * @return null-will use the default text
         */
        fun getLatestText(message: ChatMessage?, fromUsersNum: Int, messageNum: Int): String?

        /**
         * 设置notification标题
         *
         * @param message
         * @return null- will use the default text
         */
        fun getTitle(message: ChatMessage?): String?

        /**
         * set the small icon
         *
         * @param message
         * @return 0- will use the default icon
         */
        fun getSmallIcon(message: ChatMessage?): Int

        /**
         * set the intent when notification is pressed
         *
         * @param message
         * @return null- will use the default icon
         */
        fun getLaunchIntent(message: ChatMessage?): Intent?
    }

    companion object {
        private const val TAG = "ChatUIKitNotifier"
        protected var NOTIFY_ID = 341 // start notification id
        protected const val CHANNEL_ID = "hyphenate_chatuidemo_notification"
        protected val VIBRATION_PATTERN = longArrayOf(0, 180, 80, 120)
    }
}