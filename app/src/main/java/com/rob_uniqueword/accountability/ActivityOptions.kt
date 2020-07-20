package com.rob_uniqueword.accountability

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import java.time.LocalDateTime

class ActivityOptions : AppCompatActivity() {
    lateinit var activity:Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_options)

        val activityID = intent.getLongExtra(EXTRA_ACTIVITY_ID_MESSAGE, -1)
        val appDatabase = AppDatabase.getDb(this)

        val activityNameTextView = findViewById<TextView>(R.id.txtActivityName)
        val activityDatesTextView = findViewById<TextView>(R.id.txtActivityDates)
        val activityGroupTextView = findViewById<TextView>(R.id.txtActivityGroup)
        val notesView = findViewById<TextView>(R.id.txtActivityNotes)

        val liveActivity = appDatabase.activityDao().getLive(activityID)
        liveActivity.observe(this, Observer {
            activity = it
            activityNameTextView.text = it.name
            activityDatesTextView.text = it.getIntervalString(this)

            Thread {
                activityGroupTextView.text = appDatabase.activityGroupDao().getStatic(it.activityGroupID).name
            }.start()

            if (activity.notes.isBlank()) {
                notesView.visibility = View.GONE
            } else {
                notesView.visibility = View.VISIBLE
                notesView.text = activity.notes
            }
        })
    }

    // todo - make this and createFollowOnActivity aware of existing future activities
    fun completeActivity(view: View) {
        activity.endDate = LocalDateTime.now()
        activity.save(view.context)
        createFollowOnActivity(view)
    }

    fun editActivity(view: View) {
        val intent = Intent(view.context, EditActivity::class.java).apply {
            putExtra(EXTRA_ACTIVITY_MESSAGE, activity)
        }
        view.context.startActivity(intent)
    }

    fun createFollowOnActivity(view: View) {
        val intent = Intent(view.context, EditActivity::class.java).apply {
            putExtra(EXTRA_ACTIVITY_MESSAGE, activity)
            putExtra(EXTRA_CREATE_FOLLOW_ON_MESSAGE, true)
        }
        view.context.startActivity(intent)
        finish()
    }
}