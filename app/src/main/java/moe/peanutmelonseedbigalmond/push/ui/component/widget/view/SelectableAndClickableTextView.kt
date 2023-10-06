package moe.peanutmelonseedbigalmond.push.ui.component.widget.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.Selection
import android.text.Spannable
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.GestureDetectorCompat

class SelectableAndClickableTextView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val mGestureDetector: GestureDetectorCompat

    init {
        mGestureDetector = GestureDetectorCompat(context, ClickAndLongPressGestureClick())
        mGestureDetector.setIsLongpressEnabled(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (mGestureDetector.onTouchEvent(event)) {
            true
        } else {
            super.onTouchEvent(event)
        }
    }

    private inner class ClickAndLongPressGestureClick : SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            if (this@SelectableAndClickableTextView.text != null && this@SelectableAndClickableTextView.text is Spannable) {
                val buffer = this@SelectableAndClickableTextView.text as Spannable
                val x =
                    e.x - this@SelectableAndClickableTextView.totalPaddingLeft + this@SelectableAndClickableTextView.scrollX
                val y =
                    e.y.toInt() - this@SelectableAndClickableTextView.totalPaddingTop + this@SelectableAndClickableTextView.scrollY

                val layout = this@SelectableAndClickableTextView.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x)

                val link = buffer.getSpans(off, off, ClickableSpan::class.java)
                val selectionStart = Selection.getSelectionStart(buffer)
                val selectionEnd = Selection.getSelectionEnd(buffer)
                if (selectionStart > 0 && selectionEnd > 0 && selectionStart != selectionEnd) {
                    Selection.removeSelection(buffer)
                } else {
                    if (link.isNotEmpty()) {
                        link[0].onClick(this@SelectableAndClickableTextView)
                    } else {
                        performClick()
                    }
                }
                return false
            } else {
                return super.onSingleTapUp(e)
            }
        }

        override fun onLongPress(e: MotionEvent) {
            Log.i("TAG", "onLongPress: ")
            if (this@SelectableAndClickableTextView.text != null && this@SelectableAndClickableTextView.text is Spannable) {
                val buffer = this@SelectableAndClickableTextView.text as Spannable
                val x =
                    e.x - this@SelectableAndClickableTextView.totalPaddingLeft + this@SelectableAndClickableTextView.scrollX
                val y =
                    e.y.toInt() - this@SelectableAndClickableTextView.totalPaddingTop + this@SelectableAndClickableTextView.scrollY

                val layout = this@SelectableAndClickableTextView.layout
                val line = layout.getLineForVertical(y)
                val off = layout.getOffsetForHorizontal(line, x)

                val link = buffer.getSpans(off, off, ClickableSpan::class.java)
                if (link.isNotEmpty()) {
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                }
            } else {
                super.onLongPress(e)
            }
        }
    }
}