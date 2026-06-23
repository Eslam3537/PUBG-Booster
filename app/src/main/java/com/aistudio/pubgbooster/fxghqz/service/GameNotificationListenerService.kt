package com.aistudio.pubgbooster.fxghqz.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.aistudio.pubgbooster.fxghqz.util.NotificationController

class GameNotificationListenerService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "GameNotification"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val context = applicationContext
        val blockAll = NotificationController.isBlockAllNotifications(context)
        if (!blockAll) {
            return
        }

        val pkgName = sbn.packageName
        if (pkgName == null || pkgName == context.packageName) {
            return
        }

        // Check if this package is whitelisted / bypassed
        val isExcludedSocial = NotificationController.isAppExcluded(context, pkgName)
        val isManualExcluded = NotificationController.isExcludeAppEnabled(context) && 
                pkgName == NotificationController.getExcludedPackage(context)

        if (isExcludedSocial || isManualExcluded) {
            Log.d(TAG, "Notification from $pkgName is whitelisted/bypassed")
            return
        }

        // Cancel/Block the notification inside the game
        try {
            cancelNotification(sbn.key)
            Log.d(TAG, "Notification blocked and dismissed from: $pkgName")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification: ${e.message}")
        }
    }
}
