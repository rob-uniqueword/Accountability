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

const val EXTRA_ACTIVITY_MESSAGE = "com.rob_uniqueword.accountability.ACTIVITY"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front_page)

        val currentActivityTextView = findViewById<TextView>(R.id.currentActivity)
        currentActivityTextView.text = getCurrentActivity()

        val activityList = findViewById<RecyclerView>(R.id.activityList)
        activityList.layoutManager = LinearLayoutManager(this)
        getActivities(activityList)
    }

    private fun getCurrentActivity() : String {
        return "Making Accountability"
    }

    private fun getActivities(activityList:RecyclerView) {
        val activities = AppDatabase.getDb(this).activityDao().getAll()
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
        holder.textView?.text = values[position].name
        holder.layout?.setOnClickListener { v -> editActivity(v, values[position]) }
    }

    class ActivityListViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        var textView:TextView? = null
        var layout:ViewGroup? = null
        init {
            textView = itemView.findViewById(R.id.activityListText)
            layout = itemView.findViewById(R.id.activityListLayout)
        }
    }

    private fun editActivity(view:View, activity:Activity) {
        val intent = Intent(view.context, EditActivity::class.java).apply {
            putExtra(EXTRA_ACTIVITY_MESSAGE, activity)
        }
        view.context.startActivity(intent)
    }
}