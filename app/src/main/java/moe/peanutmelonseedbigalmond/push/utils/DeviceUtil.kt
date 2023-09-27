package moe.peanutmelonseedbigalmond.push.utils

import android.os.Build

object DeviceUtil {
    fun getDeviceName(): String {
        return Build.MODEL
    }
}