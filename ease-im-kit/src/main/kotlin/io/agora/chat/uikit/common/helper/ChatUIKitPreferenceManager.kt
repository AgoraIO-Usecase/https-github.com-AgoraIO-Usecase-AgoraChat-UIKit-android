package io.agora.chat.uikit.common.helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import io.agora.chat.uikit.ChatUIKitClient
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class ChatUIKitPreferenceManager @SuppressLint("CommitPrefEdits") private constructor() {
    private val editor: SharedPreferences.Editor?
    private val mSharedPreferences: SharedPreferences?

    init {
        mSharedPreferences = ChatUIKitClient.getContext()
            ?.getSharedPreferences("EM_SP_AT_MESSAGE", Context.MODE_PRIVATE)
        editor = mSharedPreferences?.edit()
    }

    var atMeGroups: Set<String>?
        get() = mSharedPreferences?.getStringSet(KEY_AT_GROUPS, null)
        set(groups) {
            editor?.remove(KEY_AT_GROUPS)
            editor?.putStringSet(KEY_AT_GROUPS, groups)
            editor?.apply()
        }

    /**
     * Save unsent text message content
     * @param toChatUsername
     * @param content
     */
    fun saveUnSendMsgInfo(toChatUsername: String?, content: String?) {
        editor?.putString("UnSendMsg$toChatUsername", content)
        editor?.apply()
    }

    fun getUnSendMsgInfo(toChatUsername: String?): String? {
        return mSharedPreferences?.getString("UnSendMsg$toChatUsername", "")
    }

    fun putString(key: String?, value: String?) {
        editor?.putString(key, value)
        editor?.commit()
    }

    fun getString(key: String?): String? {
        return mSharedPreferences?.getString(key, "")
    }

    fun putBoolean(key: String?, value: Boolean) {
        editor?.putBoolean(key, value)
        editor?.commit()
    }

    fun getBoolean(key: String?): Boolean {
        return mSharedPreferences?.getBoolean(key, false) ?: false
    }

    /**
     * Set whether the conversation list has been loaded from the server
     */
    internal fun setLoadedConversationsFromServer(value: Boolean) {
        ChatUIKitClient.getCurrentUser()?.let {
            editor?.putBoolean(KEY_LOADED_CONVS_FROM_SERVER+it.id, value)
            editor?.commit()
        }
    }

    /**
     * Get whether the conversation list has been loaded from the server
     */
    internal fun isLoadedConversationsFromServer(): Boolean {
        ChatUIKitClient.getCurrentUser()?.let {
            return mSharedPreferences?.getBoolean(KEY_LOADED_CONVS_FROM_SERVER+it.id, false) ?: false
        }
        return false
    }

    /**
     * Switch account clearing load contact status
     */
    internal fun removeLoadedContactDataStatus(key: String?){
        key?.let {
            editor?.remove(it)
            editor?.apply()
        }
    }

    /**
     * Set mute map.
     * @param userId    The current user.
     * @param muteMap   The mute map.
     */
    fun setMuteMap(userId: String, muteMap: Map<String, Long>?) {
        if (muteMap.isNullOrEmpty()) {
            editor?.putString(MUTE_DATA_KEY.plus(userId), "")
            editor?.commit()
            return
        }
        val mJsonArray = JSONArray()
        val iterator: Iterator<Map.Entry<String, Long>> = muteMap.entries.iterator()
        val result = JSONObject()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next()
            try {
                result.put(key, value)
            } catch (e: JSONException) {
            }
        }
        mJsonArray.put(result)
        editor?.putString(MUTE_DATA_KEY.plus(userId), mJsonArray.toString())
        editor?.commit()
    }

    /**
     * Get mute map.
     * @param userId The current user.
     */
    fun getMuteMap(userId: String): MutableMap<String, Long> {
        val mute: MutableMap<String, Long> = HashMap()
        val result = mSharedPreferences?.getString(MUTE_DATA_KEY.plus(userId), "")
        if (TextUtils.isEmpty(result)) return mute
        try {
            val array = JSONArray(result)
            for (i in 0 until array.length()) {
                val itemObject = array.getJSONObject(i)
                val names = itemObject.names()
                if (names != null) {
                    for (j in 0 until names.length()) {
                        val name = names.getString(j)
                        val value = itemObject.getLong(name)
                        mute[name] = value
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return mute
    }

    companion object {
        private const val KEY_AT_GROUPS = "AT_GROUPS"
        private const val MUTE_DATA_KEY = "mute_data_key"
        private const val KEY_LOADED_CONVS_FROM_SERVER = "key_loaded_convs_from_server_"

        private var instance: ChatUIKitPreferenceManager? = null

        fun getInstance(): ChatUIKitPreferenceManager {
            if (instance == null) {
                synchronized(ChatUIKitPreferenceManager::class.java) {
                    if (instance == null) {
                        instance = ChatUIKitPreferenceManager()
                    }
                }
            }
            return instance!!
        }
    }
}