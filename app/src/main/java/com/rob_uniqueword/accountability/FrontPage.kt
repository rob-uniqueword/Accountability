package com.rob_uniqueword.accountability

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

const val EXTRA_ACTIVITY_MESSAGE = "com.rob_uniqueword.accountability.ACTIVITY"
const val EXTRA_CREATE_FOLLOW_ON_MESSAGE = "com.rob_uniqueword.accountability.CREATE_FOLLOW_ON"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)

        val activityList = findViewById<RecyclerView>(R.id.activityList)
        activityList.layoutManager = LinearLayoutManager(this)
        getActivities(activityList)
    }

    private fun getActivities(activityList:RecyclerView) {
        val activities = AppDatabase.getDb(this).activityDao().getEndingAfter(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
        activities.observe(this, androidx.lifecycle.Observer { list ->
            activityList.adapter = ActivityListAdapter(list.filter { a -> a.endDate.isAfter(LocalDateTime.now()) })
        })
    }

    fun createActivity(view:View) {
        val intent = Intent(this, EditActivity::class.java)
        this.startActivity(intent)
    }
}

class ActivityListAdapter(private val values: List<Activity>) : RecyclerView.Adapter<ActivityListAdapter.ActivityListViewHolder>() {
    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_activity, parent, false)
        return ActivityListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActivityListViewHolder, position: Int) {
        val activity = values[position]
        val context = holder.layout?.context!!
        val format = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val now = LocalDateTime.now()

        if (position == 0 && activity.startDate.isBefore(now) )
        {
            holder.activityNameText?.setBackgroundColor(context.resources.getColor(R.color.colorAccent, context.theme))
        }

        holder.activityNameText?.text = activity.name

        holder.activityDatesText?.text = holder.layout?.context?.getString(
            R.string.activity_card_dates_format,
            format.format(activity.startDate),
            format.format(activity.endDate))

        if (activity.notes.isBlank()) {
            holder.activityNotesText?.height = 0 // Gross. This should be GONE, but ConstraintLayouts are awful
        } else {
            holder.activityNotesText?.text = activity.notes
        }

        holder.layout?.setOnClickListener { v -> showActivityOptions(v, values[position]) }
    }

    class ActivityListViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        var layout:ViewGroup? = null
        var activityNameText:TextView? = null
        var activityDatesText:TextView? = null
        var activityNotesText:TextView? = null
        init {
            layout = itemView.findViewById(R.id.activityListLayout)
            activityNameText = itemView.findViewById(R.id.activityCardName)
            activityDatesText = itemView.findViewById(R.id.activityCardDates)
            activityNotesText = itemView.findViewById(R.id.activityCardNotes)
        }
    }

    private fun showActivityOptions(view:View, activity:Activity) {
        val layoutInflater = view.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popup = layoutInflater.inflate(R.layout.activity_activity_options, null)

        popup.findViewById<Button>(R.id.buttonCompleteActity).setOnClickListener { v -> completeActivity(v, activity) }
        popup.findViewById<Button>(R.id.buttonEditActivity).setOnClickListener { v -> editActivity(v, activity) }
        popup.findViewById<Button>(R.id.buttonFollowOnActivity).setOnClickListener { v -> createFollowOnActivity(v, activity) }

        val displayMetrics = DisplayMetrics()
        (view.context as android.app.Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        popup.measure(View.MeasureSpec.makeMeasureSpec(displayMetrics.widthPixels, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val popupWindow = PopupWindow(popup, displayMetrics.widthPixels, popup.measuredHeight, true)
        popupWindow.showAsDropDown(view, 0, 16)
    }

    // todo - make this and createFollowOnActivity aware of existing future activities
    private fun completeActivity(view:View, activity:Activity) {
        activity.endDate = LocalDateTime.now()
        activity.save(view.context)
        createFollowOnActivity(view, activity)
    }

    private fun editActivity(view:View, activity:Activity) {
        val intent = Intent(view.context, EditActivity::class.java).apply {
            putExtra(EXTRA_ACTIVITY_MESSAGE, activity)
        }
        view.context.startActivity(intent)
    }

    private fun createFollowOnActivity(view:View, activity:Activity) {
        val intent = Intent(view.context, EditActivity::class.java).apply {
            putExtra(EXTRA_ACTIVITY_MESSAGE, activity)
            putExtra(EXTRA_CREATE_FOLLOW_ON_MESSAGE, true)
        }
        view.context.startActivity(intent)
    }
}