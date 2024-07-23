package moe.peanutmelonseedbigalmond.push.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import moe.peanutmelonseedbigalmond.push.BaseApp

object SystemUtils {
    /**
     * 系统版本是否在Android O及以上
     */
    @JvmStatic
    fun isNewerThanO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    @JvmStatic
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(BaseApp.context,Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}