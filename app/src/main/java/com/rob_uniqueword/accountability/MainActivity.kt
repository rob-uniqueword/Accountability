package com.rob_uniqueword.accountability

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text

const val EXTRA_MESSAGE = "com.rob_uniqueword.accountability.MESSAGE"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentActivityTextView = findViewById<TextView>(R.id.currentActivity)
        currentActivityTextView.text = getCurrentActivity()

        val activityList = findViewById<RecyclerView>(R.id.activityList)
        activityList.layoutManager = LinearLayoutManager(this)
        activityList.adapter = ActivityListAdapter(getActivities())
    }
}

fun getCurrentActivity() : String {
    return "Making Accountability"
}

fun getActivities() : List<String> {
    val values = mutableListOf<String>()
    for (i in 0..100) {
        values.add("Fake activity $i")
    }
    return values
}

fun listItemClickHandler(view:View, text:String) {
    Toast.makeText(view.context, text, Toast.LENGTH_SHORT).show()
}

class ActivityListAdapter(private val values: List<String>) : RecyclerView.Adapter<ActivityListAdapter.ActivityListViewHolder>() {
    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityListViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_activity, parent, false)
        return ActivityListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ActivityListViewHolder, position: Int) {
        holder.textView?.text = values[position]
        holder.layout?.setOnClickListener(View.OnClickListener { v -> listItemClickHandler(v, holder.textView?.text.toString()) })
    }

    class ActivityListViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView) {
        var textView : TextView? = null
        var layout : ViewGroup? = null
        init {
            textView = itemView.findViewById(R.id.activityListText)
            layout = itemView.findViewById(R.id.activityListLayout)
        }
    }
}