package com.rob_uniqueword.accountability

import androidx.lifecycle.LiveData
import androidx.room.*
import java.io.Serializable
import java.util.*

@Entity(
    foreignKeys = [
        ForeignKey(entity = ActivityGroup::class, parentColumns = arrayOf("id"), childColumns = arrayOf("activityGroupID"), onDelete = ForeignKey.SET_DEFAULT),
        ForeignKey(entity = ActivityChain::class, parentColumns = arrayOf("id"), childColumns = arrayOf("activityChainID"), onDelete = ForeignKey.SET_NULL)],
    indices = [
        Index(value = ["activityGroupID"]),
        Index(value = ["activityChainID"])
       ])
data class Activity(
    @PrimaryKey(autoGenerate = true) var id:Long = 0,
    @ColumnInfo(defaultValue = "1") var activityGroupID:Long = 1,
    var activityChainID:Long? = null,
    var name:String = "",
    var startDate:Date = Calendar.getInstance().time,
    var endDate:Date = Calendar.getInstance().time,
    var notes:String = "") : Serializable
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
}

@Dao
interface ActivityDao {
    @Query("select * from activity")
    fun getAll() : LiveData<List<Activity>>

    @Query("select * from activity where endDate > :cutoffDate order by startDate")
    fun getEndingAfter(cutoffDate:Long) : LiveData<List<Activity>>

    @Insert
    fun insert(activity:Activity) : Long

    @Update
    fun update(activity:Activity)
}