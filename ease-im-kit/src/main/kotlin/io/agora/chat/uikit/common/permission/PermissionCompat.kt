package io.agora.chat.uikit.common.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import java.util.Arrays

object PermissionCompat {
    /**
     * Check media permission, such as READ_EXTERNAL_STORAGE, READ_MEDIA_IMAGES, READ_MEDIA_VIDEO.
     * Note: Do not check other permissions via this method.
     * @param context
     * @param launcher
     * @param permissions
     * @return
     */
    fun checkMediaPermission(
        context: Context?,
        launcher: ActivityResultLauncher<Array<String>>,
        vararg permissions: String
    ): Boolean {
        if (context == null || permissions == null) {
            return false
        }
        val permissionList: MutableList<String> = ArrayList()
        permissionList.addAll(Arrays.asList(*permissions))
        val permissionArray: Array<String>
        permissionArray = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            permissionList.toTypedArray()
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            permissionList.toTypedArray()
        } else {
            permissionList.clear()
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionList.toTypedArray()
        }
        if (!PermissionsManager.getInstance().hasAllPermissions(context, permissionArray)) {
            launcher?.launch(permissionArray)
            return false
        }
        return true
    }

    /**
     * Check camera permission
     * @param context
     * @param launcher
     * @param permissions
     * @return
     */
    fun checkPermission(
        context: Context?,
        launcher: ActivityResultLauncher<Array<String>>,
        vararg permissions: String
    ): Boolean{
        if (context == null) {
            return false
        }

        val mPermissions = permissions.filterNot {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (mPermissions.isNotEmpty()) {
            launcher.launch(mPermissions)
            return false
        }

        return true

    }

    /**
     * Get media access status.
     * @param context
     * @return
     */
    fun getMediaAccess(context: Context?): StorageAccess {
        return if (PermissionsManager.getInstance()
                .hasPermission(context, Manifest.permission.READ_MEDIA_IMAGES) ||
            PermissionsManager.getInstance()
                .hasPermission(context, Manifest.permission.READ_MEDIA_VIDEO)
        ) {
            // Full access on Android 13+
            StorageAccess.Full
        } else if (PermissionsManager.getInstance()
                .hasPermission(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
        ) {
            // Partial access on Android 13+
            StorageAccess.Partial
        } else if (PermissionsManager.getInstance()
                .hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            // Full access up to Android 12
            StorageAccess.Full
        } else {
            StorageAccess.Denied
        }
    }

    enum class StorageAccess {
        Full, Partial, Denied
    }
}