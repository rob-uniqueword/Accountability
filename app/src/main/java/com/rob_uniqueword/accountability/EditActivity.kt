package com.rob_uniqueword.accountability

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_activity.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class EditActivity : AppCompatActivity() {
    private var activity:Activity? = null
    private var isFollowOn:Boolean = false
    private var selectedActivityGroupID : Long? = null

    private val dateTimeManager:DateTimeManager = DateTimeManager()
    private val dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    private val timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)

    private val nameText:EditText by lazy{findViewById<EditText>(R.id.editTextName)}
    private val fromDateText:EditText by lazy{findViewById<EditText>(R.id.editTextFromDate)}
    private val fromTimeText:EditText by lazy{findViewById<EditText>(R.id.editTextFromTime)}
    private val toDateText:EditText by lazy{findViewById<EditText>(R.id.editTextToDate)}
    private val toTimeText:EditText by lazy{findViewById<EditText>(R.id.editTextToTime)}
    private val durationHoursText:EditText by lazy{findViewById<EditText>(R.id.editTextDurationHours)}
    private val durationMinutesText:EditText by lazy{findViewById<EditText>(R.id.editTextDurationMinutes)}
    private val notesText:EditText by lazy{findViewById<EditText>(R.id.editTextNotes)}
    private val groupSpinner:Spinner by lazy{findViewById<Spinner>(R.id.spinnerGroup)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_activity)

        // do this first so we can use it when we set dates from the activity
        initialiseDateTimeInputs()

        activity = intent.getSerializableExtra(EXTRA_ACTIVITY_MESSAGE) as Activity?
        isFollowOn = intent.getBooleanExtra(EXTRA_CREATE_FOLLOW_ON_MESSAGE, false)

        val tmpActivity = activity
        if (tmpActivity != null) {
            selectedActivityGroupID = tmpActivity.activityGroupID
            if (isFollowOn) {
                activity = null
                dateTimeManager.setFromDttm(tmpActivity.endDate)
                dateTimeManager.setDuration(Duration.ofHours(1))
                notesText.setText(getString(R.string.edit_activity_follow_on_note_text, tmpActivity.name))
            } else {
                nameText.setText(tmpActivity.name)
                dateTimeManager.setFromDttm(tmpActivity.startDate)
                dateTimeManager.setToDttm(tmpActivity.endDate)
                notesText.setText(tmpActivity.notes)
            }
        } else {
            dateTimeManager.setFromDttm(LocalDateTime.now())
            dateTimeManager.setDuration(Duration.ofMinutes(1))
        }

        // do this last so it can use selectedActivityGroupID
        initialiseActivityGroupSpinner()
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
        val activityGroup = groupSpinner.selectedItem as ActivityGroup
        val name = nameText.text.toString()
        val startDate = dateTimeManager.fromDttm
        val endDate = dateTimeManager.toDttm
        val notes = notesText.text.toString()

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

    private fun initialiseDateTimeInputs(){
        dateTimeManager.addFromDttmChangedCallback {
            fromDateText.setText(dateFormat.format(it))
            fromTimeText.setText(timeFormat.format(it))
        }

        dateTimeManager.addToDttmChangedCallback {
            toDateText.setText(dateFormat.format(it))
            toTimeText.setText(timeFormat.format(it))
        }

        dateTimeManager.addDurationChangedCallback {
            durationHoursText.setText(it.toHours().toString())
            durationMinutesText.setText((it.toMinutes() % 60).toString())
        }

        setUpDateTimePicker(fromDateText, fromTimeText, dateTimeManager::setFromDttm, dateTimeManager::fromDttm)
        setUpDateTimePicker(toDateText, toTimeText, dateTimeManager::setToDttm, dateTimeManager::toDttm)

        durationHoursText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val minutes = (durationHoursText.text.toString().toLong() * 60) + (dateTimeManager.duration.toMinutes() % 60)
                dateTimeManager.setDuration(Duration.ofMinutes(minutes))
            }
        }

        durationMinutesText.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val minutes = (dateTimeManager.duration.toHours() * 60) + (durationMinutesText.text.toString().toLong())
                dateTimeManager.setDuration(Duration.ofMinutes(minutes))
            }
        }
    }

    private fun setUpDateTimePicker(dateText:EditText, timeText:EditText, setDate:(dttm:LocalDateTime) -> Unit, getDate:() -> LocalDateTime) {
        val date = DatePickerDialog.OnDateSetListener {_:DatePicker, year:Int, month:Int, day:Int ->
            setDate(LocalDateTime.of(LocalDate.of(year, month, day), getDate().toLocalTime()))
        }

        val time = TimePickerDialog.OnTimeSetListener { _:TimePicker, hour: Int, minute: Int ->
            setDate(LocalDateTime.of(getDate().toLocalDate(), LocalTime.of(hour, minute)))
        }

        dateText.setOnClickListener {
            DatePickerDialog(this, date, getDate().year, getDate().monthValue, getDate().dayOfMonth).show()
        }

        timeText.setOnClickListener {
            TimePickerDialog(this, time, getDate().hour, getDate().minute, true).show()
        }
    }

    private class DateTimeManager()
    {
        var fromDttm:LocalDateTime = LocalDateTime.MIN
            private set
        var toDttm:LocalDateTime = LocalDateTime.MIN
            private set
        var duration:Duration = Duration.ZERO
            private set

        private val fromDttmChangedCallbacks:MutableList<(fromDttm:LocalDateTime) -> Unit> = mutableListOf()
        private val toDttmChangedCallbacks:MutableList<(toDttm:LocalDateTime) -> Unit> = mutableListOf()
        private val durationChangedCallbacks:MutableList<(duration:Duration) -> Unit> = mutableListOf()

        fun setFromDttm(dttm:LocalDateTime) {
            fromDttm = dttm
            duration = Duration.between(fromDttm, toDttm)
            triggerOnFromDttmChangedCallbacks()
            triggerOnDurationChangedCallbacks()
        }

        fun setToDttm(dttm:LocalDateTime) {
            toDttm = dttm
            duration = Duration.between(fromDttm, toDttm)
            triggerOnToDttmChangedCallbacks()
            triggerOnDurationChangedCallbacks()
        }

        fun setDuration(duration:Duration) {
            this.duration = duration
            toDttm = fromDttm.plusSeconds(duration.seconds)
            triggerOnDurationChangedCallbacks()
            triggerOnToDttmChangedCallbacks()
        }

        fun addFromDttmChangedCallback(callback:(fromDttm:LocalDateTime) -> Unit) {
            fromDttmChangedCallbacks.add(callback)
        }

        fun addToDttmChangedCallback(callback: (toDttm:LocalDateTime) -> Unit) {
            toDttmChangedCallbacks.add(callback)
        }

        fun addDurationChangedCallback(callback: (duration:Duration) -> Unit) {
            durationChangedCallbacks.add(callback)
        }

        private fun triggerOnFromDttmChangedCallbacks() {
            for (callback in fromDttmChangedCallbacks) {
                callback(fromDttm)
            }
        }

        private fun triggerOnToDttmChangedCallbacks() {
            for (callback in toDttmChangedCallbacks) {
                callback(toDttm)
            }
        }

        private fun triggerOnDurationChangedCallbacks() {
            for (callback in durationChangedCallbacks) {
                callback(duration)
            }
        }
    }
}