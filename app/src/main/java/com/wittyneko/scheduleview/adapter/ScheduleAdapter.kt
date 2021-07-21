package com.wittyneko.scheduleview.adapter

import com.wittyneko.schedule.widget.ScheduleEdit
import com.wittyneko.schedule.widget.ScheduleItem
import com.wittyneko.schedule.widget.ScheduleView
import org.joda.time.Period

class ScheduleAdapter(
    private val view: ScheduleView
) : ScheduleView.Adapter<Triple<Period, Period, String>>() {

    val list = mutableListOf<Triple<Period, Period, String>>()

    init {
        list.addAll(
            arrayOf(
                Triple(Period.hours(3), Period.hours(4), "日程1"),
                Triple(Period.hours(3).withMinutes(30), Period.hours(4).withMinutes(30), "日程2"),
                Triple(Period.hours(3).withMinutes(45), Period.hours(5), "日程3"),
                Triple(Period.hours(5).withMinutes(30), Period.hours(7), "日程4"),
                Triple(Period.hours(7), Period.hours(9), "日程5"),
                Triple(Period.hours(7).withMinutes(8), Period.hours(9), "日程6"),
            )
        )
    }

    override fun getItemCount(): Int = list.size

    override fun getItem(position: Int): Triple<Period, Period, String> = list[position]

    override fun bindView(item: Triple<Period, Period, String>, view: ScheduleItem) {
        view.tvContent.text = item.third
        view.startPeriod = item.first
        view.endPeriod = item.second
    }

    override fun bindEdit(item: Triple<Period, Period, String>, view: ScheduleEdit) {
        view.tvContent.text = item.third
        view.startPeriod = item.first
        view.endPeriod = item.second
    }

    override fun bindCreate(view: ScheduleEdit) {
        view.tvContent.text = "新建日程"
    }

    fun notifyAllChange() {
        // 编辑Item先退出编辑
        if (view.editView.isShow) {
            view.cancelEdit()
        }
        view.notifyAllItem()
    }
}