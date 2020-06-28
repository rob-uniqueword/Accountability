package com.rob_uniqueword.accountability

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_activity.*
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    private var activity:Activity? = null
    private var selectedActivityGroupID : Long? = null

    private val fromDttm: Calendar = Calendar.getInstance()
    private val toDttm: Calendar = Calendar.getInstance().apply { add(Calendar.HOUR, 1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_activity)

        activity = intent.getSerializableExtra(EXTRA_ACTIVITY_MESSAGE) as Activity?

        val tmpActivity = activity
        if (tmpActivity != null) {
            selectedActivityGroupID = tmpActivity.activityGroupID
            findViewById<EditText>(R.id.editTextName).setText(tmpActivity.name)
            fromDttm.time = tmpActivity.startDate
            toDttm.time = tmpActivity.endDate
            findViewById<EditText>(R.id.editTextNotes).setText(tmpActivity.notes)
        }

        initialiseActivityGroupSpinner()

        val fromDateText = findViewById<EditText>(R.id.editTextFromDate)
        val fromTimeText = findViewById<EditText>(R.id.editTextFromTime)
        val toDateText = findViewById<EditText>(R.id.editTextToDate)
        val toTimeText = findViewById<EditText>(R.id.editTextToTime)

        setUpDateTimePicker(fromDateText, fromTimeText, fromDttm)
        setUpDateTimePicker(toDateText, toTimeText, toDttm)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == android.app.Activity.RESULT_OK) {
            selectedActivityGroupID = data!!.getSerializableExtra(EXTRA_ACTIVITY_GROUP_RESULT_ID) as Long
        }
    }

    fun newActivityGroup(view:View) {
        val intent = Intent(this, EditActivityGroup::class.java)
        startActivityForResult(intent, 1)
    }

    fun saveActivity(view:View) {
        val activityGroup = findViewById<Spinner>(R.id.spinnerGroup).selectedItem as ActivityGroup
        val name = findViewById<EditText>(R.id.editTextName).text.toString()
        val startDate = fromDttm.time
        val endDate = toDttm.time
        val notes = findViewById<EditText>(R.id.editTextNotes).text.toString()

        val isInsert = activity == null
        val activity = activity ?: Activity()
        activity.activityGroupID = activityGroup.id
        activity.name = name
        activity.startDate = startDate
        activity.endDate = endDate
        activity.notes = notes

        Thread {
            if (isInsert) {
                AppDatabase.getDb(this).activityDao().insert(activity)
            } else {
                AppDatabase.getDb(this).activityDao().update(activity)
            }
        }.start()

        finish()
    }

    private fun initialiseActivityGroupSpinner() {
        val activityGroups = AppDatabase.getDb(this).activityGroupDao().getAll()
        val groupSpinner = findViewById<Spinner>(R.id.spinnerGroup)

        activityGroups.observe(this, androidx.lifecycle.Observer<List<ActivityGroup>> { list ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            groupSpinner.adapter = adapter

            val selectedID = selectedActivityGroupID
            if (selectedID != null) {
                spinnerGroup.setSelection(adapter.getPosition(ActivityGroup(selectedID,"")))
            }
        } )

        groupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedActivityGroupID = null
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selected = groupSpinner.selectedItem as ActivityGroup
                selectedActivityGroupID = selected.id
            }
        }
    }

    private fun setUpDateTimePicker(dateText:EditText, timeText:EditText, calendar:Calendar) {
        dateText.setText(SimpleDateFormat.getDateInstance().format(calendar.time))
        timeText.setText(SimpleDateFormat.getTimeInstance().format(calendar.time))

        val date = DatePickerDialog.OnDateSetListener {_:DatePicker, year:Int, month:Int, day:Int ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            dateText.setText(SimpleDateFormat.getDateInstance().format(calendar.time))
        }

        val time = TimePickerDialog.OnTimeSetListener { _:TimePicker, hour: Int, minute: Int ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            timeText.setText(SimpleDateFormat.getTimeInstance().format(calendar.time))
        }

        dateText.setOnClickListener {
            DatePickerDialog(
                this,
                date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        timeText.setOnClickListener {
            TimePickerDialog(
                this,
                time,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }
}