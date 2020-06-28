package com.rob_uniqueword.accountability

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

const val EXTRA_ACTIVITY_GROUP_RESULT_ID = "com.rob_uniqueword.accountability.ACTIVITY_GROUP_RESULT_ID"

class EditActivityGroup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_activity_group)
    }

    fun saveActivityGroup(view:View) {
        val activityGroupName = findViewById<EditText>(R.id.editTextActivityGroupName)

        val activityGroup = ActivityGroup()
        activityGroup.name = activityGroupName.text.toString()

        Thread {
            activityGroup.id = AppDatabase.getDb(view.context).activityGroupDao().insert(activityGroup)

            val resultIntent = Intent()
            resultIntent.putExtra(EXTRA_ACTIVITY_GROUP_RESULT_ID, activityGroup.id)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }.start()
    }
}