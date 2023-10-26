package moe.peanutmelonseedbigalmond.push

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log

class App : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }

    override fun onCreate() {
        Log.i("App", "onCreate")
        super.onCreate()
        context = applicationContext
    }

    override fun onTrimMemory(level: Int) {
        Log.i("App", "onTrimMemory")
        super.onTrimMemory(level)
    }
}