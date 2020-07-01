package com.rob_uniqueword.accountability

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.*
import java.util.concurrent.Executors


@Database(entities = [Activity::class, ActivityGroup::class, ActivityChain::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao() : ActivityDao
    abstract fun activityGroupDao() : ActivityGroupDao

    companion object Factory {
        @Volatile private var dbInstance : AppDatabase? = null

        fun getDb(context:Context) : AppDatabase =
            dbInstance ?: synchronized(this) {
                dbInstance ?: buildDb(context).also { dbInstance = it }
            }

        private fun buildDb(context:Context) : AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .addCallback(dbCallback)
                .build()

        private val dbCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                Executors.newSingleThreadScheduledExecutor().execute {
                    dbInstance!!.activityGroupDao().insert(ActivityGroup(1, "Ungrouped"))
                    dbInstance!!.activityDao().insert(Activity(
                        1, 1, null,
                        "Set Up Accountability",
                        Calendar.getInstance().time,
                        Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }.time,
                        "Welcome to Accountability! Hope you have a great time"))
                }
            }
        }
    }
}

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromDate(value: Date?) = value?.time

        @TypeConverter
        @JvmStatic
        fun toDate(value:Long?) = value?.let { Date(it) }
    }
}