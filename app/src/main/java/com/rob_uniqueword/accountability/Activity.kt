package com.rob_uniqueword.accountability

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.work.*
import java.io.Serializable
import java.time.Duration
import java.time.LocalDateTime

const val WORKER_TAG_ACTIVITY_END_NOTIFICATION = "com.rob_uniqueword.accountability.WORKER_TAG_ACTIVITY_END_NOTIFICATION_"

@Entity(
    foreignKeys = [
        ForeignKey(entity = ActivityGroup::class, parentColumns = arrayOf("id"), childColumns = arrayOf("activityGroupID"), onDelete = ForeignKey.SET_DEFAULT)
    ],
    indices = [
        Index(value = ["activityGroupID"])
    ])
data class Activity(
    @PrimaryKey(autoGenerate = true) override var id:Long = 0,
    @ColumnInfo(defaultValue = "1") var activityGroupID:Long = 1,
    var name:String = "",
    var startDate:LocalDateTime = LocalDateTime.now(),
    var endDate:LocalDateTime = LocalDateTime.now(),
    var notes:String = "") : Serializable, DBEntity
{
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != this.javaClass) return false

        other as Activity
        return other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun save(context:Context) {
        Thread {
            val db = AppDatabase.getDb(context)
            val id = db.activityDao().insertOrUpdate(this)

            val tag = WORKER_TAG_ACTIVITY_END_NOTIFICATION + id.toString()
            val workManager = WorkManager.getInstance(context)

            workManager.cancelAllWorkByTag(tag)

            if (endDate.isAfter(LocalDateTime.now())) {
                val data = Data.Builder().putLong(EXTRA_NOTIFICATION_ACTIVITY_ID, id).build()

                val request = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
                    .setInitialDelay(Duration.between(LocalDateTime.now(), endDate))
                    .setInputData(data).addTag(tag).build()

                WorkManager.getInstance(context).enqueue(request)

                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    val toastText = context.getString(R.string.toast_activity_end_reminder_set, endDate.toDateTimeString())
                    Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun getIntervalString(context:Context) : String {
        return context.getString(R.string.activity_card_dates_format, startDate.toDateTimeString(), endDate.toDateTimeString())
    }
}

@Dao
abstract class ActivityDao : BaseDao<Activity>() {
    @Query("select * from activity")
    abstract fun getAll() : LiveData<List<Activity>>

    @Query("select * from activity where endDate > :cutoffDate order by startDate")
    abstract fun getEndingAfter(cutoffDate:Long) : LiveData<List<Activity>>

    @Query("select * from activity where id = :id")
    abstract fun getStatic(id:Long) : Activity

    @Query("select * from activity where id = :id")
    abstract fun getLive(id:Long) : LiveData<Activity>
}

class NotificationWorker(private val context:Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val activityID = inputData.getLong(EXTRA_NOTIFICATION_ACTIVITY_ID, 0)
        val activity = AppDatabase.getDb(context).activityDao().getStatic(activityID)

        queueNotification(activity)

        return Result.success()
    }

    private fun queueNotification(activity:Activity) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, ActivityOptions::class.java).apply {
            putExtra(EXTRA_ACTIVITY_MESSAGE, activity)
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val notification = NotificationCompat.Builder(applicationContext, "default")
            .setContentTitle(activity.name + " Finished")
            .setContentText("Hello it's time to finish your activity " + activity.name)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)

        notificationManager.notify(1, notification.build())
    }
}