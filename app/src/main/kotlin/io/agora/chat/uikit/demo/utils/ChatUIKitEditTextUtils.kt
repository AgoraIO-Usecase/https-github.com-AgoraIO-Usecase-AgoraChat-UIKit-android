package io.agora.chat.uikit.demo.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.agora.chat.uikit.demo.R
import io.agora.util.EMLog

object ChatUIKitEditTextUtils {
    fun changePwdDrawableRight(
        editText: EditText,
        eyeOpen: Drawable?,
        eyeClose: Drawable?,
        left: Drawable?,
        top: Drawable?,
        bottom: Drawable?
    ) {
        //标识密码是否能被看见
        val canBeSeen = booleanArrayOf(false)
        editText.setOnTouchListener { v: View?, event: MotionEvent ->
            val drawable = editText.compoundDrawables[2] ?: return@setOnTouchListener false
            //如果右边没有图片，不再处理
            //如果不是按下事件，不再处理
            if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener false
            if (event.x > (editText.width
                        - editText.paddingRight
                        - drawable.intrinsicWidth)
            ) {
                if (canBeSeen[0]) {
                    editText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    editText.setCompoundDrawablesWithIntrinsicBounds(left, top, eyeOpen, bottom)
                    canBeSeen[0] = false
                } else {
                    editText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                    editText.setCompoundDrawablesWithIntrinsicBounds(left, top, eyeClose, bottom)
                    canBeSeen[0] = true
                }
                editText.setSelection(editText.text.toString().length)
                editText.isFocusable = true
                editText.isFocusableInTouchMode = true
                editText.requestFocus()
                return@setOnTouchListener true
            }
            false
        }
    }

    fun showRightDrawable(editText: EditText, right: Drawable?) {
        val content = editText.text.toString().trim { it <= ' ' }
        editText.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            if (TextUtils.isEmpty(content)) null else right,
            null
        )
    }

    fun clearEditTextListener(editText: EditText) {
        editText.setOnTouchListener { v: View?, event: MotionEvent ->
            val drawable = editText.compoundDrawables[2] ?: return@setOnTouchListener false
            //如果右边没有图片，不再处理
            //如果不是按下事件，不再处理
            if (event.action != MotionEvent.ACTION_UP) return@setOnTouchListener false
            if (event.x > (editText.width
                        - editText.paddingRight
                        - drawable.intrinsicWidth)
            ) {
                editText.setText("")
                return@setOnTouchListener true
            }
            false
        }
    }

    /**
     * 单行，根据关键字确定省略号的不同位置
     * @param textView
     * @param str
     * @param keyword
     * @param width
     * @return
     */
    fun ellipsizeString(textView: TextView, str: String, keyword: String, width: Int): String {
        if (TextUtils.isEmpty(keyword)) {
            return str
        }
        val paint: Paint = textView.paint
        if (paint.measureText(str) < width) {
            return str
        }
        val count = paint.breakText(str, 0, str.length, true, width.toFloat(), null)
        val index = str.indexOf(keyword)
        //如果关键字在第一行,末尾显示省略号
        if (index + keyword.length < count) {
            return str
        }
        //如果关键字在最后，则起始位置显示省略号
        if (str.length - index <= count - 3) {
            var end = str.substring(str.length - count)
            end = "..." + end.substring(3)
            return end
        }
        //如果是在中部的话，首尾显示省略号
        val subCount = (count - keyword.length) / 2
        var middle = str.substring(index - subCount, index + keyword.length + subCount)
        middle = "..." + middle.substring(3)
        middle = middle.substring(0, middle.length - 3) + "..."
        return middle
    }

    fun highLightKeyword(context: Context?, str: String, keyword: String): SpannableStringBuilder? {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(keyword) || !str.contains(keyword)) {
            return null
        }
        val builder = SpannableStringBuilder(str)
        builder.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    context!!,
                    R.color.em_color_brand
                )
            ),
            str.indexOf(keyword),
            str.indexOf(keyword) + keyword.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return builder
    }

    /**
     * 设置最多显示行数及保留末尾的文本类型
     * @param textView
     * @param str
     * @param num
     * @param width
     * @return
     */
    fun ellipsizeMiddleString(textView: TextView, str: String, num: Int, width: Int): String {
        //设置为最大显示行数及为中间省略
        var str = str
        textView.maxLines = num
        textView.ellipsize = TextUtils.TruncateAt.MIDDLE
        val paint = textView.paint
        if (TextUtils.isEmpty(str) || width <= 0 || paint.measureText(str) < width) {
            return str
        }
        //检查是否需要进行省略
        var startIndex = 0
        var maxNum = 0
        for (i in 0 until num) {
            if (startIndex < str.length) {
                maxNum += paint.breakText(str, startIndex, str.length, true, width.toFloat(), null)
                startIndex = maxNum - 1
            }
        }
        if (str.length < maxNum) {
            return str
        }
        //获取第num行占据的字符数目
        var maxCount = 0
        maxCount = try {
            textView.layout.getLineEnd(num - 1)
        } catch (e: Exception) {
            e.printStackTrace()
            return str
        }
        //如果不满num行
        if (str.length < maxCount) {
            return str
        }
        //如果文件
        if (str.contains(".")) {
            val lastIndex = str.lastIndexOf(".")
            val suffix = "..." + str.substring(lastIndex - 5)
            val requestWidth = paint.measureText(suffix)
            //对str取反
            val reverse = StringBuilder(str.substring(0, maxCount)).reverse().toString()
            var takeUpCount = paint.breakText(reverse, 0, reverse.length, true, requestWidth, null)
            takeUpCount = getTakeUpCount(paint, reverse, takeUpCount, requestWidth)
            str = str.substring(0, maxCount - takeUpCount) + suffix
            EMLog.i("ChatUIKitEditTextUtils", "last str = $str")
        }
        return str
    }

    /**
     * 检查保证可以展示文件类型
     * @param paint
     * @param reverse
     * @param takeUpCount
     * @param requestWidth
     * @return
     */
    private fun getTakeUpCount(
        paint: Paint,
        reverse: String,
        takeUpCount: Int,
        requestWidth: Float
    ): Int {
        val measureWidth = paint.measureText(reverse.substring(0, takeUpCount))
        return if (measureWidth <= requestWidth) {
            getTakeUpCount(paint, reverse, takeUpCount + 1, requestWidth)
        } else takeUpCount + 1
    }
}