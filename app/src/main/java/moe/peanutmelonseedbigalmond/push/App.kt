package moe.peanutmelonseedbigalmond.push

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.imageLoader
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import moe.peanutmelonseedbigalmond.push.utils.notification.NotificationHolder

class App : Application(), ImageLoaderFactory {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set

        val markwon by lazy {
            Markwon.builder(context)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TablePlugin.create(context))
                .usePlugin(TaskListPlugin.create(context))
                .usePlugin(HtmlPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(CoilImagesPlugin.create(context, context.imageLoader))
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(SimpleExtPlugin.create())
                .build()
        }

        // 已经发送的通知
        // 通知组id, 通知列表
        val notifications = mutableMapOf<String, MutableList<NotificationHolder>>()

        // 已发送的摘要消息
        // 通知组id, id
        val summaryNotifications = mutableMapOf<String, Int>()
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

    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this).newBuilder().build()
    }
}