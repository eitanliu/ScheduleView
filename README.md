
![效果图.gif](https://upload-images.jianshu.io/upload_images/1845254-6933bd1300558aa8.gif?imageMogr2/auto-orient/strip)  
钉钉有啥功能就有啥，小米日历和iOS日历也是都一样，懒得录屏图是我盗iOS同事的，这是Android版实现iOS出门右转[仿钉钉日历页面,日视图下的一天日程安排视图](https://www.jianshu.com/p/2f08bf612783)，原理去看iOS的文章，不过最重要排序算法不一样。
## 依赖

一、在项目根目录build.gradle添加repositories，注意是项目根目录的，不是项目的build.gradle  

```groovy
	repositories {
		//其他maven库...
		maven { url "https://jitpack.io" }
	}
```

二、在项目的build.gradle添加dependencies  

```groovy
	dependencies {
		implementation 'com.github.wittyneko:ScheduleView:1.0.0'
	}
```

## 使用

一、定义Adapter  

```kotlin
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
```

二、绑定数据和监听  

```kotlin
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

    private fun initScheduleView() {
        scheduleView.onCreateClickListener = onCreateClickListener
        scheduleView.onItemClickListener = onItemClickListener
        scheduleView.onItemChangeListener = onItemChangeListener
        scheduleView.setAdapter(adapter)
        adapter.notifyAllChange()
    }
```

三、布局必须放在`NestedScrollView`  

```xml
    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <com.wittyneko.schedule.widget.ScheduleView
            android:id="@+id/schedule_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"/>
    </androidx.core.widget.NestedScrollView>
```

## TODO
- [ ] 优化自动滚动  
- [ ] 优化`Adapter`绑定流程  
- [ ] 支持`Attribute`配置  
