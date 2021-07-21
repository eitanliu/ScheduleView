package com.wittyneko.schedule.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.wittyneko.schedule.R
import org.joda.time.Period

/**
 * Desc: 日程条目
 * <p>
 * Date: 2020-07-21
 * Copyright: Copyright (c) 2010 - 2020
 * Updater:
 * Update Time:
 * Update Comments:
 *
 * Author: wittyneko
 */
class ScheduleItem : FrameLayout {

    val isShow get() = parent != null

    //开始时间
    var startPeriod = Period()

    //结束时间
    var endPeriod = Period()

    var columnStart = 0
    var columnEnd = 0
    var columnSize = 1

    val columnMeasureWidth get() = (columnEnd - columnStart + 1).toFloat() / columnSize
    val columnMeasureStart get() = columnStart.toFloat() / columnSize

    val tvContent by lazy { findViewById<TextView>(R.id.tv_content) }
    val ivLeft by lazy { findViewById<ImageView>(R.id.iv_left) }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs, defStyleAttr)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(context, attrs, defStyleAttr)
    }

    private fun init(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        tvContent.apply {
            //Log.e("text " + run { "$measuredHeight, ${layout.height}, ${layout.lineCount}, $lineHeight, $lineSpacingExtra $lineSpacingMultiplier \n" })
            maxLines = measuredHeight / lineHeight
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    override fun getLayoutParams(): FrameLayout.LayoutParams {
        return super.getLayoutParams() as LayoutParams
    }
}