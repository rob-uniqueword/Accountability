package com.rob_uniqueword.accountability

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.Executors


@Database(entities = [Activity::class, ActivityGroup::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao() : ActivityDao
    abstract fun activityGroupDao() : ActivityGroupDao

    companion object Factory {
        @Volatile private var dbInstance : AppDatabase? = null
        private var context:Context? = null

        fun getDb(context:Context) : AppDatabase =
            dbInstance ?: synchronized(this) {
                this.context = context
                dbInstance ?: buildDb(context).also { dbInstance = it }
            }

        private fun buildDb(context:Context) : AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "app_database")
                .addCallback(dbCallback)
                .build()

        private val dbCallback = object : Callback() {
            override fun onCreate(db:SupportSQLiteDatabase) {
                Executors.newSingleThreadScheduledExecutor().execute {
                    ActivityGroup(
                        1,
                        "Ungrouped")
                        .save(context!!)
                    Activity(
                        1, 1,
                        "Set Up Accountability",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(15),
                        "Welcome to Accountability! Hope you have a great time")
                        .save(context!!)
                    /*
                    dbInstance!!.activityGroupDao().insert(ActivityGroup(1, "Ungrouped"))
                    dbInstance!!.activityDao().insert(Activity(
                        1, 1,
                        "Set Up Accountability",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusMinutes(15),
                        "Welcome to Accountability! Hope you have a great time"))

                     */
                }
            }
        }
    }
}

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromDate(value: LocalDateTime?) = value?.atZone(ZoneOffset.UTC)?.toEpochSecond()

        @TypeConverter
        @JvmStatic
        fun toDate(value:Long?) = value?.let { LocalDateTime.ofInstant(Instant.ofEpochSecond(value), ZoneOffset.UTC) }
    }
}

interface DBEntity {
    val id:Long
}

@Dao
abstract class BaseDao<T:DBEntity> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insert(entity:T) : Long

    @Update
    abstract fun update(entity:T) : Int

    @Transaction
    open fun insertOrUpdate(entity:T) : Long {
        val id = insert(entity)
        if (id != -1L) { return id }

        update(entity)
        return entity.id
    }
}