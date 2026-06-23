package com.aistudio.pubgbooster.fxghqz.data

import android.content.Context
import android.content.pm.PackageManager

data class InstalledPubgVariant(
    val packageName: String,
    val displayName: String,
    val appName: String, // الاسم الحقيقي المسجل في الجهاز عبر PackageManager
)

object PubgVersionDetector {

    // قائمة معرفات الحزم المعروفة لنسخ PUBG/BGMI المختلفة حول العالم
    private val knownPackages = mapOf(
        "com.tencent.ig" to "PUBG Mobile (Global)",
        "com.pubg.krmobile" to "PUBG Mobile (Korea)",
        "com.pubg.imobile" to "BGMI (India)",
        "com.vng.pubgmobile" to "PUBG Mobile (Vietnam)",
        "com.rekoo.pubgm" to "PUBG Mobile (Taiwan)",
    )

    /** يفحص فعلياً عبر PackageManager أي نسخ PUBG مثبتة حقاً على الجهاز، بدون افتراض. */
    fun detectInstalledVariants(context: Context): List<InstalledPubgVariant> {
        val pm = context.packageManager
        return knownPackages.mapNotNull { (pkg, label) ->
            try {
                val appInfo = pm.getApplicationInfo(pkg, 0)
                val realName = pm.getApplicationLabel(appInfo).toString()
                InstalledPubgVariant(packageName = pkg, displayName = label, appName = realName)
            } catch (_: PackageManager.NameNotFoundException) {
                null // غير مثبتة فعلياً — لا تُضف للقائمة
            }
        }
    }
}
