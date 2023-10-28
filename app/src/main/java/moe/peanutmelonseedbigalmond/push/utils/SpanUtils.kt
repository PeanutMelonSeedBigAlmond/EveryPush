package moe.peanutmelonseedbigalmond.push.utils

import android.text.Spanned
import androidx.core.text.getSpans
import io.noties.markwon.image.AsyncDrawableSpan

object SpanUtils {
    @JvmStatic
    fun findImageUrlFromSpan(spanned: Spanned): List<String> {
        return spanned.getSpans<AsyncDrawableSpan>(0, spanned.length)
            .map { it.drawable.destination }
    }
}