package com.rob_uniqueword.accountability

import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDateTime

class ActivityOptions : AppCompatActivity() {
    lateinit var activity:Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_options)

        activity = intent.getSerializableExtra(EXTRA_ACTIVITY_MESSAGE) as Activity

        Thread {
            val activityGroup = AppDatabase.getDb(this).activityGroupDao().get(activity.activityGroupID)
            findViewById<TextView>(R.id.txtActivityGroup).text = activityGroup.name
        }.start()

        findViewById<TextView>(R.id.txtActivityName).text = activity.name
        findViewById<TextView>(R.id.txtActivityDates).text = activity.getIntervalString(this)

        val notesView = findViewById<TextView>(R.id.txtActivityNotes)
        if (activity.notes.isBlank()) {
            notesView.visibility = View.GONE
        } else {
            notesView.text = activity.notes
        }
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
    }
}