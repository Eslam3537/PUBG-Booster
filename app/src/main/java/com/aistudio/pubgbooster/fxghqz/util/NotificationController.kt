package com.aistudio.pubgbooster.fxghqz.util

import android.content.Context

object NotificationController {

    fun isBlockAllNotifications(context: Context): Boolean {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("block_all_notifications", false)
    }

    fun setBlockAllNotifications(context: Context, checked: Boolean) {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("block_all_notifications", checked).apply()
    }

    fun isExcludeAppEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("exclude_app_enabled", false)
    }

    fun setExcludeAppEnabled(context: Context, checked: Boolean) {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("exclude_app_enabled", checked).apply()
    }

    fun getExcludedPackage(context: Context): String? {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        return prefs.getString("excluded_package", null)
    }

    fun setExcludedPackage(context: Context, pkg: String) {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("excluded_package", pkg).apply()
    }

    fun isAppExcluded(context: Context, pkg: String): Boolean {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("excluded_app_$pkg", false)
    }

    fun setAppExcluded(context: Context, pkg: String, nextVal: Boolean) {
        val prefs = context.getSharedPreferences("pubg_booster_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("excluded_app_$pkg", nextVal).apply()
    }
}
