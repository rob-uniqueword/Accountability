package com.rob_uniqueword.accountability

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity
data class ActivityGroup(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val name:String
) {
    override fun toString(): String {
        return name
    }
}

@Dao
interface ActivityGroupDao {
    @Query("select * from activitygroup")
    fun getAll() : LiveData<List<ActivityGroup>>

    @Insert
    fun insert(activityGroup:ActivityGroup)
}