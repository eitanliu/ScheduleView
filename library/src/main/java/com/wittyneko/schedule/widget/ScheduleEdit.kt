package com.wittyneko.schedule.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.wittyneko.schedule.R
import com.wittyneko.schedule.extensions.frameOnScreen
import org.joda.time.Period

/**
 * Desc: 创建编辑
 * <p>
 * Date: 2020-07-21
 * Copyright: Copyright (c) 2010 - 2020
 * Updater:
 * Update Time:
 * Update Comments:
 *
 * Author: wittyneko
 */
class ScheduleEdit : FrameLayout {

    // 移动Item
    var isItemTouch = false
        private set

    // 调整顶部
    var isTopTouch = false
        private set

    // 调整底部
    var isBottomTouch = false
        private set

    val isShow get() = parent != null

    val editTouchView by lazy { findViewById<View>(R.id.edit_touch) }
    val topTouchView by lazy { findViewById<View>(R.id.top_touch) }
    val bottomTouchView by lazy { findViewById<View>(R.id.bottom_touch) }
    val startTimeView by lazy { findViewById<TextView>(R.id.tv_start_time) }
    val endTimeView by lazy { findViewById<TextView>(R.id.tv_end_time) }
    val tvContent by lazy { findViewById<TextView>(R.id.tv_content) }

    private var _editStartPeriod: Period? = null
    val editStartPeriod: Period
        get() {
            if (_editStartPeriod == null) _editStartPeriod = startPeriod
            return _editStartPeriod!!
        }
    private var _editEndPeriod: Period? = null
    val editEndPeriod: Period
        get() {
            if (_editEndPeriod == null) _editEndPeriod = endPeriod
            return _editEndPeriod!!
        }

    //开始时间
    var startPeriod = Period()
        set(value) {
            field = value
        }

    //结束时间
    var endPeriod = Period()
        set(value) {
            field = value
        }

    override fun getLayoutParams(): FrameLayout.LayoutParams {
        return super.getLayoutParams() as LayoutParams
    }

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
        tvContent.apply { maxLines = measuredHeight / lineHeight }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        checkTouchLocation(ev)
        return super.dispatchTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    /**
     * Desc:  重置点击状态
     * <p>
     * Author: wittyneko
     * Date: 2020-07-22
     */
    fun resetTouchStatus() {
        isItemTouch = false
        isTopTouch = false
        isBottomTouch = false
        _editStartPeriod = null
        _editEndPeriod = null
    }

    /**
     * Desc: 检测点击位置
     * <p>
     * Author: wittyneko
     * Date: 2020-07-22
     * @param ev MotionEvent
     */
    fun checkTouchLocation(ev: MotionEvent) {
        val editFrame = editTouchView.frameOnScreen
        isItemTouch = ev.rawX.toInt() in editFrame.run { left..right }
                && ev.rawY.toInt() in editFrame.run { top..bottom }
        val topFrame = topTouchView.frameOnScreen
        isTopTouch = ev.rawX.toInt() in topFrame.run { left..right }
                && ev.rawY.toInt() in topFrame.run { top..bottom }
        val bottomFrame = bottomTouchView.frameOnScreen
        isBottomTouch = ev.rawX.toInt() in bottomFrame.run { left..right }
                && ev.rawY.toInt() in bottomFrame.run { top..bottom }
        //Log.e(ev.run { "check $rawX, $rawY;  " } + topFrame.run { "$left, $right, $top $bottom;  " } + bottomFrame.run { "$left, $right, $top $bottom;  " })
    }
}