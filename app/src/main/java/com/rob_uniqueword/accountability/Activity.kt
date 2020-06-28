package com.rob_uniqueword.accountability

import androidx.lifecycle.LiveData
import androidx.room.*
import java.io.Serializable
import java.util.*

@Entity(foreignKeys = [ForeignKey(entity = ActivityGroup::class, parentColumns = arrayOf("id"), childColumns = arrayOf("activityGroupID"), onDelete = ForeignKey.SET_DEFAULT)])
data class Activity(
    @PrimaryKey(autoGenerate = true) val id:Long,
    @ColumnInfo(defaultValue = "1") val activityGroupID:Long = 1,
    var name:String,
    var startDate:Date,
    var endDate:Date,
    var notes:String
) : Serializable

@Dao
interface ActivityDao {
    @Query("select * from activity")
    fun getAll() : LiveData<List<Activity>>

    @Insert
    fun insert(activity:Activity)
}