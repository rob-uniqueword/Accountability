package com.rob_uniqueword.accountability

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {
    var activity:Activity? = null

    private val fromDttm: Calendar = Calendar.getInstance();
    private val toDttm: Calendar = Calendar.getInstance();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_activity)

        setActivityGroups()

        val fromDateText = findViewById<EditText>(R.id.editTextFromDate)
        val fromTimeText = findViewById<EditText>(R.id.editTextFromTime)
        val toDateText = findViewById<EditText>(R.id.editTextToDate)
        val toTimeText = findViewById<EditText>(R.id.editTextToTime)
        setUpDateTimePicker(fromDateText, fromTimeText, fromDttm)
        setUpDateTimePicker(toDateText, toTimeText, toDttm)

        activity = intent.getSerializableExtra(EXTRA_ACTIVITY_MESSAGE) as Activity?

        if ( activity != null ) {
            findViewById<EditText>(R.id.editTextName).setText(activity!!.name)
            findViewById<Spinner>(R.id.spinnerGroup).setSelection(0) // todo - make this good
            fromDttm.time = activity!!.startDate
            fromDateText.setText(SimpleDateFormat.getDateInstance().format(fromDttm.time))
            fromTimeText.setText(SimpleDateFormat.getTimeInstance().format(fromDttm.time))
            toDttm.time = activity!!.endDate
            toDateText.setText(SimpleDateFormat.getDateInstance().format(toDttm.time))
            toTimeText.setText(SimpleDateFormat.getTimeInstance().format(toDttm.time))
            findViewById<EditText>(R.id.editTextNotes).setText(activity!!.notes)
        }
    }

    private fun setActivityGroups() {
        val activityGroups = AppDatabase.getDb(this).activityGroupDao().getAll()

        activityGroups.observe(this, androidx.lifecycle.Observer<List<ActivityGroup>> { list ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            val groupSpinner = findViewById<Spinner>(R.id.spinnerGroup)
            groupSpinner.adapter = adapter
        } )
    }

    private fun setUpDateTimePicker(dateText:EditText, timeText:EditText, calendar:Calendar) {
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