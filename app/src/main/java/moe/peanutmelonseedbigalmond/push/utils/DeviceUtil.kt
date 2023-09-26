package moe.peanutmelonseedbigalmond.push.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings

object DeviceUtil {
    fun getDeviceName():String{
        return Build.MODEL
    }
}