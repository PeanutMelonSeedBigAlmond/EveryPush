package moe.peanutmelonseedbigalmond.push.utils

import android.text.Spanned
import androidx.core.text.getSpans
import io.noties.markwon.image.AsyncDrawableSpan

object SpanUtils {
    /**
     * 获取 AsyncDrawableSpan
     * @param spanned Spanned
     * @return List<String>
     * @see <a href="https://www.jianshu.com/p/b188e150b8bb">解决span倒序问题</a>
     */
    @JvmStatic
    fun findImageUrlFromSpan(spanned: Spanned): List<String> {
        val drawSpan = spanned.getSpans<AsyncDrawableSpan>(0, spanned.length)
        drawSpan.sortBy(spanned::getSpanStart)
        return drawSpan.map { it.drawable.destination }
    }
}