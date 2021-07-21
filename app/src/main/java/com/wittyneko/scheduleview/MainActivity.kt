package com.wittyneko.scheduleview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.wittyneko.schedule.extensions.asView
import com.wittyneko.schedule.widget.ScheduleEdit
import com.wittyneko.schedule.widget.ScheduleItem
import com.wittyneko.schedule.widget.ScheduleView
import com.wittyneko.scheduleview.adapter.ScheduleAdapter

class MainActivity : AppCompatActivity() {

    private val scheduleView by lazy { findViewById<ScheduleView>(R.id.schedule_view) }

    private val adapter by lazy { ScheduleAdapter(scheduleView) }

    // 日程创建
    private val onCreateClickListener = View.OnClickListener {
        val edit = it.asView<ScheduleEdit>()
        adapter.list.apply {
            add(Triple(edit.startPeriod, edit.endPeriod, "计划${size + 1}"))
        }
        adapter.notifyAllChange()
    }

    // 日程点击监听
    private val onItemClickListener = { view: ScheduleItem, position: Int ->
        Toast.makeText(this, "${view.tvContent.text}", Toast.LENGTH_SHORT).show()
    }

    // 日程修改监听
    private val onItemChangeListener = listener@{ view: ScheduleItem, position: Int ->
        val item = adapter.list[position]
        adapter.list[position] = item.copy(view.startPeriod, view.endPeriod)
        adapter.notifyAllChange()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initScheduleView()
    }

    private fun initScheduleView() {
        scheduleView.onCreateClickListener = onCreateClickListener
        scheduleView.onItemClickListener = onItemClickListener
        scheduleView.onItemChangeListener = onItemChangeListener
        scheduleView.setAdapter(adapter)
        adapter.notifyAllChange()
    }
}