package moe.peanutmelonseedbigalmond.push.utils

import android.os.Build

object SystemUtils {
    /**
     * 系统版本是否在Android O及以上
     */
    @JvmStatic
    fun isNewerThanO(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}