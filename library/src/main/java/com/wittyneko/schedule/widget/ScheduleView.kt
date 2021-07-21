package com.wittyneko.schedule.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.wittyneko.schedule.R
import com.wittyneko.schedule.extensions.*
import org.joda.time.Duration
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.Period

/**
 * Desc: 日程时间轴
 * <p>
 * Date: 2020-07-28
 * Copyright: Copyright (c) 2010 - 2020
 * Updater:
 * Update Time:
 * Update Comments:
 * @property itemLongClickListener OnLongClickListener
 * @property itemClickListener OnClickListener
 * @property editClickListener OnClickListener
 * @property cancelClickListener OnClickListener
 *
 * Author: wittyneko
 */
class ScheduleView : FrameLayout {

    private var mLastFocusX = 0f
    private var mLastFocusY = 0f
    private var mDownFocusX = 0f
    private var mDownFocusY = 0f

    private var mLastFocusRawX = 0f
    private var mLastFocusRawY = 0f
    private var mDownFocusRawX = 0f
    private var mDownFocusRawY = 0f

    private var isCheckTouch = false
    private var isRemoveItem = false
    private var isFirstLayout = true

    // 自动滚动状态
    var autoScroll = AutoScroll.IDLE

    enum class AutoScroll { IDLE, TOP, BOTTOM }

    var onlyTodayShowCurrentTime = false //只在今天显示当前时间
    var timeOffset = 0L //服务器时间偏移量
    val serviceTime get() = System.currentTimeMillis() - timeOffset //服务器时间
    val zeroTime = LocalTime.MIDNIGHT //开始时间
    val durationItemMin = Duration.standardMinutes(30) //item最小时间
    val duration24 = Duration.standardHours(24)
    val durationMin = Duration(0) // 可调整最小时间
    val durationMax = duration24 // 可调整最大时间
    val timePattern = "HH:mm"
    val time24String = "24:00"

    val layoutInterface by lazy { LayoutInflater.from(context) }
    val longPressTimeout = ViewConfiguration.getLongPressTimeout()
    val viewConfiguration by lazy { ViewConfiguration.get(context) }

    val topSpace = resources.idp(8)
    val bottomSpace = resources.idp(8)
    val itemSpace = resources.idp(45)
    val itemMarginStart = resources.idp(56)
    val itemMarginEnd = resources.idp(15)
    val columnMargin = resources.idp(1)
    val itemWidth get() = measuredWidth - itemMarginStart - itemMarginEnd + columnMargin
    val smoothScrollSpace = resources.idp(8) //每次滑动距离
    val timeTextoffset = resources.idp(7) // 隐藏时间距离偏移

    val currentTimeColor = resources.color(R.color.color_F55656)
    val bgTimeColor = resources.color(R.color.color_BCC1CD)


    val lineViewList = arrayListOf<View>()
    val timeViewList = arrayListOf<TextView>()
    val itemViewList = arrayListOf<ScheduleItem>()
    var itemEditView: ScheduleItem? = null
        private set

    val scrollView by lazy { parentAsView<NestedScrollView>() }
    val editView by lazy {
        val view = layoutInterface.inflate(
            R.layout.layout_schedule_item_edit, this, false
        ) as ScheduleEdit
        view.asLayoutParams<MarginLayoutParams>().bottomMargin = bottomSpace
        view.editTouchView.asLayoutParams<MarginLayoutParams>().apply {
            marginStart = itemMarginStart.toInt()
            marginEnd = itemMarginEnd.toInt()
        }
        view
    }
    val currentTime by lazy {
        layoutInterface.inflate(
            R.layout.layout_schedule_current_time, this, false
        ) as ScheduleCurrentTime
    }

    var selectedDate = LocalDate.now() //当前选中日期
    private var mAdapter: Adapter<Any>? = null

    var itemLongClickListener = OnLongClickListener {
        //编辑Item
        editItem(it.asView())
        true
    }

    var itemClickListener = OnClickListener {
        val item = it.asView<ScheduleItem>()
        onItemClickListener?.invoke(item, itemViewList.indexOf(item))
        //删除Item
        //removeItem(it.asView())
    }

    var editClickListener = OnClickListener {
        hideEdit()
        itemEditView?.also { item ->
            //编辑
            moveItem(item, editView.startPeriod, editView.endPeriod)
            itemEditView = null
        } ?: run {
            //新建
            //val item = createItem(editView.startPeriod, editView.editEndPeriod)
            //addItem(item)
            onCreateClickListener?.onClick(editView)
        }
    }

    var cancelClickListener = OnClickListener {
        hideEdit()
        itemEditView?.also { item ->
            moveItem(item, editView.startPeriod, editView.endPeriod)
            itemEditView = null
        }
    }

    var onCreateClickListener: OnClickListener? = null

    var onItemClickListener: ((view: ScheduleItem, position: Int) -> Unit)? = null

    var onItemChangeListener: ((view: ScheduleItem, position: Int) -> Unit)? = null

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
        clipChildren = false
        clipToPadding = false
        val lineHeight = resources.idp(1)
        val textSize = resources.sp(12)
        val textMarginStart = resources.idp(16)
        val textMarginEnd = resources.idp(15)

        for (i in 0..24) {
            //添加时间线
            addView(View(context).also {
                lineViewList.add(i, it)
                it.setBackgroundResource(R.color.color_DCE0E8)
                it.layoutParams = generateDefaultLayoutParams().also { layoutParams ->
                    layoutParams.height = lineHeight
                    layoutParams.topMargin = itemSpace * i + topSpace
                    if (i == 24) layoutParams.bottomMargin = bottomSpace
                    layoutParams.marginStart = itemMarginStart.toInt()
                    layoutParams.marginEnd = itemMarginEnd.toInt()
                }
            })


            addView(TextView(context).also {
                timeViewList.add(i, it)
                it.text = when (i) {
                    24 -> time24String
                    else -> zeroTime.plusHours(i).toString(timePattern)
                }
                it.setTextColor(bgTimeColor)
                it.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                it.layoutParams = generateDefaultLayoutParams().also { layoutParams ->
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                    layoutParams.topMargin = itemSpace * i
                    layoutParams.marginStart = textMarginStart
                    layoutParams.marginEnd = textMarginEnd
                }
            })
        }

        addView(currentTime)

        // 测试Item
//        addItems(
//            createItem(Period.hours(3), Period.hours(4)),
//            createItem(Period.hours(3).withMinutes(30), Period.hours(4).withMinutes(30)),
//            createItem(Period.hours(3).withMinutes(45), Period.hours(5)),
//            createItem(Period.hours(5).withMinutes(30), Period.hours(7)),
//            createItem(Period.hours(7), Period.hours(9)),
//            createItem(Period.hours(7).withMinutes(8), Period.hours(9))
//        )
    }

    override fun onAttachedToWindow() {
        post {
            refreshTime()
        }
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(::refreshTime)
        super.onDetachedFromWindow()
    }

    fun refreshTime() {
        currentTime.updateLayoutParams()
        postDelayed(::refreshTime, 2000)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        itemViewList.forEach {
            it.updateLayoutParams()
            it.layoutParams.resolveLayoutDirection(it.layoutDirection)
            //updateViewLayout(it, it.layoutParams)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (isFirstLayout && visibility == View.VISIBLE) {
            currentTime.updateLayoutParams()
            scrollToCurTime()
            isFirstLayout = false
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        var consume = false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownFocusX = ev.x
                mDownFocusY = ev.y
                mLastFocusX = ev.x
                mLastFocusY = ev.y
                mDownFocusRawX = ev.rawX
                mDownFocusRawY = ev.rawY
                mLastFocusRawX = ev.rawX
                mLastFocusRawY = ev.rawY
                isCheckTouch = false
                isRemoveItem = false
                autoScroll = AutoScroll.IDLE
                editView.resetTouchStatus()
            }
            MotionEvent.ACTION_MOVE -> {
            }
            MotionEvent.ACTION_UP -> {
            }
            MotionEvent.ACTION_CANCEL -> {
            }
        }
        return consume.takeIf { it } ?: super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        var consume = false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {

                if (Math.abs(ev.y - mDownFocusY) > viewConfiguration.scaledTouchSlop * 0.3) {
                    if (editView.isShow && editView.isItemTouch) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        consume = editView.isShow
                    }
                } else {
                    if (isCheckTouch) editView.checkTouchLocation(ev)
                    if (editView.isItemTouch) isCheckTouch = false
                }
            }
            MotionEvent.ACTION_UP -> {
            }
        }
        //Log.e(ev.run { "action $action, $consume, $x, $y ${editView.isItemTouch}, ${editView.isTopTouch}, ${editView.isBottomTouch}" })
        return consume.takeIf { it } ?: super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        var consume = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (editView.isShow) editView.checkTouchLocation(event)
                consume = true
            }
            MotionEvent.ACTION_MOVE -> {
                // 创建状态
                if (editView.isShow) {
                    // 触摸事件在编辑布局
                    if (editView.run { isItemTouch || isTopTouch || isBottomTouch }) {
                        parent.requestDisallowInterceptTouchEvent(true)
                        consume = true
                        val diffY = event.y - mLastFocusY
                        val betweenY = event.y - mDownFocusY
                        val betweenTime = distanceToTime(betweenY, 0)
                        //val betweenDuration = distanceToTime(betweenY, 0).toStandardDuration()
                        //val betweenMinute = betweenDuration.standardMinutes
                        //Log.e("betweenTime ${betweenMinute};  " + betweenTime.run { "$hours, $minutes" })

                        // 编辑时间
                        when {
                            // 调整开始时间
                            editView.isTopTouch -> {
                                val startPeriod = (editView.editStartPeriod + betweenTime).round()

                                if ((editView.endPeriod - startPeriod).toStandardDuration() >= durationItemMin
                                    && startPeriod.toStandardDuration() >= durationMin
                                ) {
                                    editView.startPeriod = startPeriod
                                }
                            }
                            // 调整结束时间
                            editView.isBottomTouch -> {
                                val endPeriod = (editView.editEndPeriod + betweenTime).round()

                                if ((endPeriod - editView.startPeriod).toStandardDuration() >= durationItemMin
                                    && endPeriod.toStandardDuration() <= durationMax
                                ) {
                                    editView.endPeriod = endPeriod
                                }
                            }
                            // 调整时间段
                            editView.isItemTouch -> {
                                var startPeriod = (editView.editStartPeriod + betweenTime).round()
                                val diff = startPeriod - editView.editStartPeriod
                                val endPeriod = editView.editEndPeriod + diff

                                if (startPeriod.toStandardDuration() >= durationMin
                                    && endPeriod.toStandardDuration() <= durationMax
                                ) {
                                    editView.startPeriod = startPeriod
                                    editView.endPeriod = endPeriod
                                }
                            }
                        }

                        editView.updateLayoutParams()
                        editView.requestLayout()

                        // 滑动到顶部/底部自动滚动
                        if (autoScroll != AutoScroll.IDLE || Math.abs(betweenY) > viewConfiguration.scaledTouchSlop) {

                            val scrollFrame = scrollView.visibleOnScreen
                            val outsideTop = event.rawY - scrollFrame.top < itemSpace + topSpace
                            val canScrollTop = scrollView.canScrollVertically(-1) && outsideTop
                            val outsideBottom =
                                scrollFrame.bottom - event.rawY < itemSpace + bottomSpace
                            val canScrollBottom = scrollView.canScrollVertically(1) && outsideBottom
                            autoScroll = when {
                                !outsideTop && !outsideBottom -> AutoScroll.IDLE
                                autoScroll != AutoScroll.IDLE && (canScrollTop || canScrollBottom) -> autoScroll
                                diffY < 0 && canScrollTop -> AutoScroll.TOP
                                diffY >= 0 && canScrollBottom -> AutoScroll.BOTTOM
                                else -> autoScroll
                            }

                            when (autoScroll) {
                                AutoScroll.TOP -> {
                                    scrollView.smoothScrollBy(0, -smoothScrollSpace)
                                }
                                AutoScroll.BOTTOM -> {
                                    scrollView.smoothScrollBy(0, smoothScrollSpace)
                                }
                            }
                        }
                    }
                }
                mLastFocusX = event.x
                mLastFocusY = event.y
                mLastFocusRawX = event.rawX
                mLastFocusRawY = event.rawY
            }
            MotionEvent.ACTION_UP -> {

                mLastFocusX = event.x
                mLastFocusY = event.y
                mLastFocusRawX = event.rawX
                mLastFocusRawY = event.rawY
                if (editView.isShow) {
                    if (editView.run { isItemTouch || isTopTouch || isBottomTouch }) {
//                        if (Math.abs(mLastFocusY - mDownFocusY) < viewConfiguration.scaledTouchSlop) {
//                            editClickListener.onClick(editView)
//                        }
                    } else {
                        cancelClickListener.onClick(this)
                    }
                } else {
                    val start = distanceToTime(mLastFocusY).round().takeIf { it.hours < 23 }
                        ?: Period.hours(23)
                    if (!isRemoveItem) showEdit(start)
                }
            }
        }


        //Log.e(event.run { "action $action, $consume, $x, $y ${editView.isItemTouch}, ${editView.isTopTouch}, ${editView.isBottomTouch}" })

        return consume.takeIf { it } ?: super.onTouchEvent(event)
    }

    /**
     * Desc: 滚动到当前时间前一小时
     * <p>
     * Author: wittyneko
     * Date: 2020-08-07
     */
    fun scrollToCurTime() {

        val localTime = LocalTime(serviceTime)
        val hour = Math.max(localTime.hourOfDay - 1, 0)
        val localPeriod = Period.hours(hour)
        val curY = Math.min(
            timeToDistance(localPeriod, 0).toInt(),
            measuredHeight - scrollView.measuredHeight
        )
        scrollView.scrollTo(0, curY)
    }

    /**
     * Desc: 进入创建/编辑
     * <p>
     * Author: wittyneko
     * Date: 2020-07-22
     * @param startPeriod Period
     * @param endPeriod Period
     */
    fun showEdit(startPeriod: Period, endPeriod: Period = startPeriod.plusHours(1)) {

        editView.startPeriod = startPeriod
        editView.endPeriod = endPeriod
        editView.updateLayoutParams()
        if (!editView.isShow) {
            addView(editView)
            post {
                editView.updateLayoutParams()
                requestLayout()
            }
        } else {
            requestLayout()
        }
        if (itemEditView != null) {
            mAdapter?.apply {
                val index = itemViewList.indexOf(itemEditView!!)
                if (index < getItemCount()) bindEdit(getItem(index), editView)
            }
        } else {
            mAdapter?.bindCreate(editView)
        }
        editView.editTouchView.setOnClickListener(editClickListener)
    }

    /**
     * Desc: 取消创建/编辑
     * <p>
     * Author: wittyneko
     * Date: 2020-07-22
     */
    fun hideEdit() {
        removeView(editView)
        post {
            editView.updateLayoutParams()
            requestLayout()
        }
    }


    fun cancelEdit() {
        itemEditView = null
        hideEdit()
    }

    /**
     * Desc: 移动Item
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     * @param item TimeTableItem
     * @param startPeriod Period
     * @param endPeriod Period
     */
    fun moveItem(item: ScheduleItem, startPeriod: Period, endPeriod: Period) {
        item.startPeriod = startPeriod
        item.endPeriod = endPeriod
        onItemChangeListener?.invoke(item, itemViewList.indexOf(item))
        measureItemColumn()
        item.requestLayout()
    }

    /**
     * Desc: 编辑Item
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     * @param item TimeTableItem
     */
    fun editItem(item: ScheduleItem) {
        isCheckTouch = true
        itemEditView = item
        parent.requestDisallowInterceptTouchEvent(true)
        showEdit(item.startPeriod, item.endPeriod)
    }

    /**
     * Desc: 删除Item
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     * @param item TimeTableItem
     */
    fun removeItem(item: ScheduleItem) {
        itemViewList.remove(item)
        measureItemColumn()
        removeView(item)
        isRemoveItem = true
    }

    /**
     * Desc: 删除多个Item
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     * @param item Array<out TimeTableItem>
     */
    fun removeItems(vararg item: ScheduleItem) {
        itemViewList.removeAll(item)
        measureItemColumn()
        item.forEach { removeView(it) }
        isRemoveItem = true
    }

    /**
     * Desc: 添加Item
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     * @param item TimeTableItem
     */
    fun addItem(item: ScheduleItem) {
        itemViewList.add(item)
        measureItemColumn()
        addView(item, indexOfChild(currentTime))
    }

    /**
     * Desc: 添加多个Item
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     * @param item Array<out TimeTableItem>
     */
    fun addItems(vararg item: ScheduleItem) {
        itemViewList.addAll(item)
        measureItemColumn()
        item.forEach { addView(it, indexOfChild(currentTime)) }
    }

    /**
     * Desc: 新建Item
     * <p>
     * Author: wittyneko
     * Date: 2020-07-23
     * @param startPeriod Period
     * @param endPeriod Period
     * @return TimeTableItem
     */
    fun createItem(startPeriod: Period, endPeriod: Period) = run {
        val item = layoutInterface.inflate(
            R.layout.layout_schedule_item, this, false
        ) as ScheduleItem

        item.startPeriod = startPeriod
        item.endPeriod = endPeriod
        item.setOnLongClickListener(itemLongClickListener)
        item.setOnClickListener(itemClickListener)
        //item.updateLayoutParams()
        item
    }

    /**
     * Desc: 距离换算时间
     * <p>
     * Author: wittyneko
     * Date: 2020-07-22
     * @param dist Float
     * @param offset Int
     * @return Period
     */
    fun distanceToTime(dist: Float, offset: Int = topSpace) = run {
        val time = (dist - offset) / itemSpace
        val hour = time.toInt()
        val minute = ((time - hour) * 60).toInt()
        //LocalTime(hour, minute)

        Period(hour, minute, 0, 0)

    }

    /**
     * Desc: 时间换算距离
     * <p>
     * Author: wittyneko
     * Date: 2020-07-22
     * @param time Period
     * @param offset Int
     * @return Float
     */
    fun timeToDistance(time: Period, offset: Int = topSpace) = run {
        time.hours * itemSpace + time.minutes / 60f * itemSpace + offset
    }

    /**
     * Desc: 15分钟取整
     * <p>
     * Author: wittyneko
     * Date: 2020-07-23
     * @receiver Period
     * @return Period
     */
    fun Period.round(round: Int = 15): Period {
        return withMinutes(minutes / round * round)
    }

    fun measureItemColumn() {
        val rowList = arrayListOf<Row>() // 行数据
        //val columnList = arrayListOf<Column>() // 列数据
        //val sortList = itemViewArray.sortedByDescending { (it.endPeriod - it.startPeriod).toStandardDuration() }
        val sortList = itemViewList.sortedWith(
            compareBy<ScheduleItem> { it.startPeriod.toStandardDuration() }
                .thenByDescending { (it.endPeriod - it.startPeriod).toStandardDuration() }
        )

        sortList.forEach { item ->

            /**
             * 1. 判断列是否存在不存在新建
             * 2. 比较item时间和列时间
             * 2.1 在列之内比较下一列
             * 2.2 在列之外添加到列
             */
            var rowIndex = 0
            var columnIndex = 0
            while (true) {

                // 判断行是否存在
                if (rowIndex >= rowList.size) {
                    val column = Column(item.startPeriod, item.endPeriod, arrayListOf(item))
                    val row = Row(item.startPeriod, item.endPeriod, arrayListOf(column))
                    rowList.add(row)
                    break
                }


                val row = rowList[rowIndex]
                val rowStart = row.startPeriod.toStandardDuration()
                val rowEnd = row.endPeriod.toStandardDuration()
                val rowRange = rowStart..rowEnd

                val itemStart = item.startPeriod.toStandardDuration()
                val itemEnd = item.endPeriod.toStandardDuration()
                val itemRange = itemStart..itemEnd

                val equalStartEnd = (itemStart == rowEnd || itemEnd == rowStart)
                val inRow = (itemStart in rowRange || itemEnd in rowRange)
                if ((inRow && !equalStartEnd).not()) {
                    rowIndex++
                    continue
                }

                val columnList = row.columnList
                if (columnIndex < columnList.size) {
                    val column = columnList[columnIndex]
                    val columnStart = column.startPeriod.toStandardDuration()
                    val columnEnd = column.endPeriod.toStandardDuration()
                    val columnRange = columnStart..columnEnd

                    val isBreak = run {

                        column.viewList.forEach { prev ->

                            val prevStart = prev.startPeriod.toStandardDuration()
                            val prevEnd = prev.endPeriod.toStandardDuration()
                            val prevRange = prevStart..prevEnd

                            val equalColumnStartEnd = (itemStart == prevEnd || itemEnd == prevStart)
                            val inColumn = (itemStart in prevRange || itemEnd in prevRange)

//                            Log.e(
//                                "columnRange " +
//                                        "${zeroTime.plus(item.startPeriod)}, ${zeroTime.plus(item.endPeriod)}, " +
//                                        "${zeroTime.plus(prev.startPeriod)}, ${zeroTime.plus(prev.endPeriod)}, " +
//                                        "$inColumn equalColumnStartEnd, $rowIndex $columnIndex "
//                            )
                            if (inColumn && !equalColumnStartEnd) {
                                columnIndex++
                                return@run false
                            }
                        }
                        column.viewList.add(item)

                        if (columnEnd < itemEnd) column.endPeriod = item.endPeriod
                        if (columnStart > itemStart) column.startPeriod = item.startPeriod
                        if (rowEnd < itemEnd) row.endPeriod = item.endPeriod
                        if (rowStart > itemStart) row.startPeriod = item.startPeriod
                        return@run true
                    }
                    if (isBreak) break
                } else {
                    columnList.add(Column(item.startPeriod, item.endPeriod, arrayListOf(item)))
                    if (rowEnd < itemEnd) row.endPeriod = item.endPeriod
                    if (rowStart > itemStart) row.startPeriod = item.startPeriod

                    break
                }
            }

        }

        /**
         * 1. 遍历列数据下所有item，赋值开始列和总列数
         * 2. item 和下一列起止时间比较
         * 2.1 在时间内结束列为该列
         * 2.2 不再继续比较下一列
         */
        rowList.forEach { row ->
            val columnList = row.columnList
            val columnListSize = columnList.size
            columnList.forEachIndexed { index, column ->

                column.viewList.forEach { item ->
                    item.columnStart = index
                    item.columnEnd = index
                    item.columnSize = columnListSize
                    val itemStart = item.startPeriod.toStandardDuration()
                    val itemEnd = item.endPeriod.toStandardDuration()
                    val itemRange = itemStart..itemEnd

                    for (next in (index + 1) until columnListSize) {
                        val nextColumn = columnList[next]
                        val isBreak = run {
                            nextColumn.viewList.forEach { nextItem ->

                                val columnStart = nextItem.startPeriod.toStandardDuration()
                                val columnEnd = nextItem.endPeriod.toStandardDuration()
                                val columnRange = columnStart..columnEnd

                                val equalStartEnd =
                                    (itemStart == columnEnd || itemEnd == columnStart)
                                val inColumn = itemStart in columnRange || itemEnd in columnRange
                                        || columnStart in itemRange || columnEnd in itemRange
                                if (inColumn && !equalStartEnd) {
                                    return@run true
                                }
                            }
                            return@run false
                        }
                        if (isBreak) break
                        item.columnEnd = next
                    }
                }
            }
        }

    }

    /**
     * Desc: 计算Item位置
     * <p>
     * Author: wittyneko
     * Date: 2020-07-26
     * @receiver TimeTableItem
     */
    fun ScheduleItem.updateLayoutParams() {
        //Log.e(run { "columnMeasure $columnStart, $columnEnd, $columnSize, $columnMeasure, $itemWidth" })
        layoutParams.topMargin = timeToDistance(startPeriod).toInt()
        layoutParams.height = timeToDistance(endPeriod - startPeriod, 0).toInt()
        //layoutParams.marginStart = itemMarginStart.toInt()
        //layoutParams.marginEnd = itemMarginEnd.toInt()
        val width = itemWidth * columnMeasureWidth
        layoutParams.marginStart = (itemMarginStart + itemWidth * columnMeasureStart).toInt()
        layoutParams.width = (itemWidth * columnMeasureWidth - columnMargin).toInt()
    }

    /**
     * Desc: 计算编辑位置
     * <p>
     * Author: wittyneko
     * Date: 2020-07-26
     * @receiver TimeTableEdit
     */
    fun ScheduleEdit.updateLayoutParams() {
        val startTime = zeroTime.plus(startPeriod)
        val endTime = zeroTime.plus(endPeriod)

        layoutParams.topMargin = timeToDistance(startPeriod).toInt()
        layoutParams.height = timeToDistance(endPeriod - startPeriod, 0).toInt()
        startTimeView.text = startTime.toString(timePattern)
        endTimeView.text = when {
            endPeriod.toStandardDuration() == duration24 -> time24String
            else -> endTime.toString(timePattern)
        }
        updateTimeVisible()
    }

    /**
     * Desc: 计算当前时间位置
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     * @receiver TimeTableCurrentTime
     */
    fun ScheduleCurrentTime.updateLayoutParams() {
        val localTime = LocalTime(serviceTime)
        val localPeriod = Period(localTime.hourOfDay, localTime.minuteOfHour, 0, 0)
        val local = localPeriod.toStandardDuration()
        val isToady = selectedDate == LocalDate(serviceTime)
        itemViewList.forEach { item ->
            val start = item.startPeriod.toStandardDuration()
            val end = item.endPeriod.toStandardDuration()
            val (bg, left, color) = when {
                isToady && local > end -> Triple(
                    R.drawable.bg_schedule_item_past,
                    R.drawable.bg_schedule_item_past_left, R.color.color_BCC1CD
                )
                isToady && local in item.run { start..end } -> Triple(
                    R.drawable.bg_schedule_item_now,
                    R.drawable.bg_schedule_item_now_left, R.color.color_2A2F3C
                )
                else -> Triple(
                    R.drawable.bg_schedule_item_future,
                    R.drawable.bg_schedule_item_future_left, R.color.color_2A2F3C
                )
            }
            item.setBackgroundResource(bg)
            item.ivLeft.setBackgroundResource(left)
            item.tvContent.setTextColor(context.color(color))
        }
        val curY = timeToDistance(localPeriod) - measuredHeight / 2
        if (translationY != curY) {
            translationY = curY
            currentTime.timeView.text = localTime.toString(timePattern)
            updateTimeVisible()
        } else {
            // 不是今天隐藏当前时间
            val newVisibility = if (isToady) View.VISIBLE else View.GONE
            if (onlyTodayShowCurrentTime && currentTime.visibility != newVisibility) {
                currentTime.visibility = newVisibility
                updateTimeVisible()
            }
        }
        //layoutParams.resolveLayoutDirection(layoutDirection)
    }

    /**
     * Desc: 隐藏重叠时间
     * <p>
     * Author: wittyneko
     * Date: 2020-07-27
     */
    fun updateTimeVisible() {
        val startRange = editView.startTimeView.frameOnScreen.run { top..bottom }
        val endRange = editView.endTimeView.frameOnScreen.run { top..bottom }
        val curFrame = currentTime.timeView.frameOnScreen

        val curRange = curFrame.run { top..bottom }
        val curTop = curFrame.top + timeTextoffset
        val curBottom = curFrame.bottom - timeTextoffset
        val curVisible = curTop in startRange || curBottom in startRange
                || curTop in endRange || curBottom in endRange
        //currentTime.timeView.visibility = if (curVisible) View.INVISIBLE else View.VISIBLE
        currentTime.timeView.setTextColor(if (curVisible) Color.TRANSPARENT else currentTimeColor)

        // 背景时间隐藏
        timeViewList.forEach {
            val frame = it.frameOnScreen
            frame.top += timeTextoffset
            frame.bottom -= timeTextoffset
            // 判断是否和编辑编辑区域重叠
            val inRange = frame.top in startRange || frame.bottom in startRange
                    || frame.top in endRange || frame.bottom in endRange
            val visibility = if (inRange && editView.isShow) View.INVISIBLE else {
                // 判断当前时间区域重叠
                val inCurRange = frame.top in curRange || frame.bottom in curRange
                val isCurVisible = currentTime.isShow && currentTime.visibility == View.VISIBLE
                if (inCurRange && isCurVisible) View.INVISIBLE else View.VISIBLE
            }
            it.setTextColor(if (visibility != View.VISIBLE) Color.TRANSPARENT else bgTimeColor)
        }

    }

    @Suppress("UNCHECKED_CAST")
    fun setAdapter(adapter: Adapter<*>) {
        if (mAdapter != adapter) {
            mAdapter = adapter as Adapter<Any>
        }
    }

    fun notifyItem(position: Int) {
        mAdapter?.apply {
            if (position < getItemCount() && position < itemViewList.size) {
                val view = itemViewList.get(position)
                bindView(getItem(position), view)
            }
        }
    }

    fun notifyAllItem() {
        mAdapter?.also { adapter ->

            for (position in 0 until adapter.getItemCount()) {
                val item = adapter.getItem(position)
                val view = itemViewList.getOrNull(position)
                    ?: createItem(Period.ZERO, Period.ZERO).also { itemViewList.add(it) }
                adapter.bindView(item, view)
            }

            val removeList = if (itemViewList.size > adapter.getItemCount()) {
                val count = itemViewList.size - adapter.getItemCount()
                val item = itemViewList.takeLast(count)
                itemViewList.removeAll(item)
                item
            } else emptyList()

            measureItemColumn()
            removeList.forEach { removeView(it) }
            currentTime.updateLayoutParams()
            itemViewList.forEach { if (!it.isShow) addView(it, indexOfChild(currentTime)) }
        }
    }

    fun getAdapter() = mAdapter

    class Row(
        var startPeriod: Period,
        var endPeriod: Period,
        var columnList: MutableList<Column>
    )

    class Column(
        var startPeriod: Period,
        var endPeriod: Period,
        var viewList: MutableList<ScheduleItem>
    )

    abstract class Adapter<T> {
        abstract fun getItemCount(): Int

        abstract fun getItem(position: Int): T

        abstract fun bindView(item: T, view: ScheduleItem)

        abstract fun bindEdit(item: T, view: ScheduleEdit)

        abstract fun bindCreate(view: ScheduleEdit)
    }
}