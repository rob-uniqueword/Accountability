package com.rob_uniqueword.accountability

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_ACTIVITY_MESSAGE = "com.rob_uniqueword.accountability.ACTIVITY"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)

        val activityList = findViewById<RecyclerView>(R.id.activityList)
        activityList.layoutManager = LinearLayoutManager(this)
        getActivities(activityList)
    }

    private fun getActivities(activityList:RecyclerView) {
        val activities = AppDatabase.getDb(this).activityDao().getEndingAfter(Calendar.getInstance().time.time)
        activities.observe(this, androidx.lifecycle.Observer { list ->
            activityList.adapter = ActivityListAdapter(list)
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
        val format = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
        val now = Calendar.getInstance().time

        if (position == 0 && activity.startDate.before(now) )
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

        holder.layout?.setOnClickListener { v -> editActivity(v, values[position]) }
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

    private fun editActivity(view:View, activity:Activity) {
        val intent = Intent(view.context, EditActivity::class.java).apply {
            putExtra(EXTRA_ACTIVITY_MESSAGE, activity)
        }
        view.context.startActivity(intent)
    }
}