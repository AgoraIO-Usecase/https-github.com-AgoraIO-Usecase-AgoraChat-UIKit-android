package io.agora.chat.uikit.common.utils

import android.os.Build
import android.os.Environment
import android.text.TextUtils
import androidx.annotation.StringDef
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Locale
import java.util.Properties

object RomUtils {
    const val ROM_MIUI = "MIUI"
    const val ROM_EMUI = "EMUI"
    const val ROM_VIVO = "VIVO"
    const val ROM_OPPO = "OPPO"
    const val ROM_FLYME = "FLYME"
    const val ROM_SMARTISAN = "SMARTISAN"
    const val ROM_QIKU = "QIKU"
    const val ROM_LETV = "LETV"
    const val ROM_LENOVO = "LENOVO"
    const val ROM_NUBIA = "NUBIA"
    const val ROM_ZTE = "ZTE"
    const val ROM_COOLPAD = "COOLPAD"
    const val ROM_UNKNOWN = "UNKNOWN"
    private const val SYSTEM_VERSION_MIUI = "ro.miui.ui.version.name"
    private const val SYSTEM_VERSION_EMUI = "ro.build.version.emui"
    private const val SYSTEM_VERSION_VIVO = "ro.vivo.os.version"
    private const val SYSTEM_VERSION_OPPO = "ro.build.version.opporom"
    private const val SYSTEM_VERSION_FLYME = "ro.build.display.id"
    private const val SYSTEM_VERSION_SMARTISAN = "ro.smartisan.version"
    private const val SYSTEM_VERSION_LETV = "ro.letv.eui"
    private const val SYSTEM_VERSION_LENOVO = "ro.lenovo.lvp.version"
    @JvmStatic
    val lightStatusBarAvailableRomType: Int
        get() {
            if (isMiUIV7OrAbove) {
                return AvailableRomType.ANDROID_NATIVE
            }
            if (isMiUIV6OrAbove) {
                return AvailableRomType.MIUI
            }
            if (isFlymeV4OrAbove) {
                return AvailableRomType.FLYME
            }
            return if (isAndroidMOrAbove) {
                AvailableRomType.ANDROID_NATIVE
            } else AvailableRomType.NA
        }
    private val isFlymeV4OrAbove: Boolean
        private get() {
            val displayId = Build.DISPLAY
            if (!TextUtils.isEmpty(displayId) && displayId.contains("Flyme")) {
                val displayIdArray =
                    displayId.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (temp in displayIdArray) {
                    if (temp.matches("^[4-9]\\.(\\d+\\.)+\\S*".toRegex())) {
                        return true
                    }
                }
            }
            return false
        }
    private val isAndroidMOrAbove: Boolean
        private get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    private const val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
    private val isMiUIV6OrAbove: Boolean
        private get() {
            var stream: FileInputStream? = null
            return try {
                val properties = Properties()
                stream = FileInputStream(File(Environment.getRootDirectory(), "build.prop"))
                properties.load(stream)
                val uiCode = properties.getProperty(KEY_MIUI_VERSION_CODE, null)
                if (uiCode != null) {
                    val code = uiCode.toInt()
                    code >= 4
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            } finally {
                if (stream != null) {
                    try {
                        stream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    @JvmStatic
    val isMiUIV7OrAbove: Boolean
        get() {
            var stream: FileInputStream? = null
            return try {
                val properties = Properties()
                stream = FileInputStream(File(Environment.getRootDirectory(), "build.prop"))
                properties.load(stream)
                val uiCode = properties.getProperty(KEY_MIUI_VERSION_CODE, null)
                if (uiCode != null) {
                    val code = uiCode.toInt()
                    code >= 5
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            } finally {
                if (stream != null) {
                    try {
                        stream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

    private fun getSystemProperty(propName: String): String {
        return ""
        //return SystemProperties.get(propName, null);
    }

    @get:RomName
    val romName: String
        get() {
            if (isMiuiRom) {
                return ROM_MIUI
            }
            if (isHuaweiRom) {
                return ROM_EMUI
            }
            if (isVivoRom) {
                return ROM_VIVO
            }
            if (isOppoRom) {
                return ROM_OPPO
            }
            if (isMeizuRom) {
                return ROM_FLYME
            }
            if (isSmartisanRom) {
                return ROM_SMARTISAN
            }
            if (is360Rom()) {
                return ROM_QIKU
            }
            if (isLetvRom) {
                return ROM_LETV
            }
            if (isLenovoRom) {
                return ROM_LENOVO
            }
            if (isZTERom) {
                return ROM_ZTE
            }
            return if (isCoolPadRom) {
                ROM_COOLPAD
            } else ROM_UNKNOWN
        }
    val deviceManufacture: String
        get() {
            if (isMiuiRom) {
                return "小米"
            }
            if (isHuaweiRom) {
                return "华为"
            }
            if (isVivoRom) {
                return ROM_VIVO
            }
            if (isOppoRom) {
                return ROM_OPPO
            }
            if (isMeizuRom) {
                return "魅族"
            }
            if (isSmartisanRom) {
                return "锤子"
            }
            if (is360Rom()) {
                return "奇酷"
            }
            if (isLetvRom) {
                return "乐视"
            }
            if (isLenovoRom) {
                return "联想"
            }
            if (isZTERom) {
                return "中兴"
            }
            return if (isCoolPadRom) {
                "酷派"
            } else Build.MANUFACTURER
        }
    val isMiuiRom: Boolean
        get() = !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_MIUI))
    val isHuaweiRom: Boolean
        get() = !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_EMUI))
    val isVivoRom: Boolean
        get() = !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_VIVO))
    val isOppoRom: Boolean
        get() = !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_OPPO))
    val isMeizuRom: Boolean
        get() {
            val meizuFlymeOSFlag = getSystemProperty(SYSTEM_VERSION_FLYME)
            return !TextUtils.isEmpty(meizuFlymeOSFlag) && meizuFlymeOSFlag.uppercase(Locale.getDefault())
                .contains(
                    ROM_FLYME
                )
        }
    val isSmartisanRom: Boolean
        get() = !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_SMARTISAN))

    fun is360Rom(): Boolean {
        val manufacturer = Build.MANUFACTURER
        return !TextUtils.isEmpty(manufacturer) && manufacturer.uppercase(Locale.getDefault())
            .contains(
                ROM_QIKU
            )
    }

    val isLetvRom: Boolean
        get() = !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_LETV))
    val isLenovoRom: Boolean
        get() = !TextUtils.isEmpty(getSystemProperty(SYSTEM_VERSION_LENOVO))
    val isCoolPadRom: Boolean
        get() {
            val model = Build.MODEL
            val fingerPrint = Build.FINGERPRINT
            return !TextUtils.isEmpty(model) && model.lowercase(Locale.getDefault()).contains(
                ROM_COOLPAD
            ) || !TextUtils.isEmpty(fingerPrint) && fingerPrint.lowercase(Locale.getDefault())
                .contains(
                    ROM_COOLPAD
                )
        }
    val isZTERom: Boolean
        get() {
            val manufacturer = Build.MANUFACTURER
            val fingerPrint = Build.FINGERPRINT
            return !TextUtils.isEmpty(manufacturer) && (fingerPrint.lowercase(Locale.getDefault())
                .contains(
                    ROM_NUBIA
                )
                    || fingerPrint.lowercase(Locale.getDefault())
                .contains(ROM_ZTE)) || !TextUtils.isEmpty(fingerPrint) && (fingerPrint.lowercase(
                Locale.getDefault()
            ).contains(ROM_NUBIA)
                    || fingerPrint.lowercase(Locale.getDefault()).contains(ROM_ZTE))
        }
    val isDomesticSpecialRom: Boolean
        get() = (isMiuiRom
                || isHuaweiRom
                || isMeizuRom
                || is360Rom()
                || isOppoRom
                || isVivoRom
                || isLetvRom
                || isZTERom
                || isLenovoRom
                || isCoolPadRom)

    @StringDef(*[ROM_MIUI, ROM_EMUI, ROM_VIVO, ROM_OPPO, ROM_FLYME, ROM_SMARTISAN, ROM_QIKU, ROM_LETV, ROM_LENOVO, ROM_ZTE, ROM_COOLPAD, ROM_UNKNOWN])
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(
        AnnotationRetention.SOURCE
    )
    annotation class RomName
    internal object AvailableRomType {
        const val MIUI = 1
        const val FLYME = 2
        const val ANDROID_NATIVE = 3
        const val NA = 4
    }
}