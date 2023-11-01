package moe.peanutmelonseedbigalmond.push

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import android.util.Log
import androidx.annotation.Px
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.Transformation
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.MarkwonSpansFactory
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.ImageSize
import io.noties.markwon.image.ImageSizeResolverDef
import io.noties.markwon.image.ImagesPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin.CoilStore
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import moe.peanutmelonseedbigalmond.push.utils.notification.NotificationHolder
import org.commonmark.node.Link

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
                .usePlugin(LinkifyPlugin.create())
                .usePlugin(SimpleExtPlugin.create())
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                        val originalFactory = builder.getFactory(Link::class.java)
                        if (originalFactory != null) {
                            builder.appendFactory(Link::class.java) { _, _ ->
                                return@appendFactory object : CharacterStyle(), UpdateAppearance {
                                    override fun updateDrawState(tp: TextPaint?) {
                                        tp?.isUnderlineText = false
                                    }
                                }
                            }
                        }
                    }
                })
                .usePlugin(object : AbstractMarkwonPlugin() {
                    override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                        builder.imageSizeResolver(object : ImageSizeResolverDef() {
                            override fun resolveImageSize(drawable: AsyncDrawable): Rect {
                                return resolveImageSize(
                                    ImageSize(ImageSize.Dimension(100f, UNIT_PERCENT), null),
                                    drawable.result.bounds,
                                    drawable.lastKnownCanvasWidth,
                                    drawable.lastKnowTextSize
                                )
                            }
                        })
                    }
                })
                .usePlugin(CoilImagesPlugin.create(object : CoilStore {
                    override fun load(drawable: AsyncDrawable): ImageRequest {
                        return ImageRequest.Builder(context)
                            .data(drawable.destination)
                            .transformations(RoundedCornerWithoutCropTransformation(16f))
                            .build()
                    }

                    override fun cancel(disposable: Disposable) {
                        disposable.dispose()
                    }
                }, context.imageLoader))
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
        Firebase.crashlytics.setCustomKeys {
            key("isDebug", BuildConfig.DEBUG)
            key("buildType", BuildConfig.BUILD_TYPE)
        }
    }

    override fun onTrimMemory(level: Int) {
        Log.i("App", "onTrimMemory")
        super.onTrimMemory(level)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this).newBuilder().build()
    }

    private class RoundedCornerWithoutCropTransformation(
        @Px private val topLeft: Float = 0f,
        @Px private val topRight: Float = 0f,
        @Px private val bottomLeft: Float = 0f,
        @Px private val bottomRight: Float = 0f
    ) : Transformation {
        constructor(@Px radius: Float) : this(radius, radius, radius, radius)

        init {
            require(topLeft >= 0 && topRight >= 0 && bottomLeft >= 0 && bottomRight >= 0) {
                "All radii must be >= 0."
            }
        }

        override val cacheKey = "${javaClass.name}-$topLeft,$topRight,$bottomLeft,$bottomRight"

        override suspend fun transform(input: Bitmap, size: Size): Bitmap {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val output = createBitmap(input.width, input.height, input.config)
            output.applyCanvas {
                drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                paint.shader = BitmapShader(input, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

                val radii = floatArrayOf(
                    topLeft,
                    topLeft,
                    topRight,
                    topRight,
                    bottomRight,
                    bottomRight,
                    bottomLeft,
                    bottomLeft
                )
                val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
                val path = Path().apply { addRoundRect(rect, radii, Path.Direction.CW) }
                drawPath(path, paint)
            }

            return output
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is RoundedCornerWithoutCropTransformation &&
                    topLeft == other.topLeft &&
                    topRight == other.topRight &&
                    bottomLeft == other.bottomLeft &&
                    bottomRight == other.bottomRight
        }

        override fun hashCode(): Int {
            var result = topLeft.hashCode()
            result = 31 * result + topRight.hashCode()
            result = 31 * result + bottomLeft.hashCode()
            result = 31 * result + bottomRight.hashCode()
            return result
        }
    }
}