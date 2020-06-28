package com.rob_uniqueword.accountability

import androidx.lifecycle.LiveData
import androidx.room.*
import java.io.Serializable

@Entity
data class ActivityGroup(
    @PrimaryKey(autoGenerate = true) var id:Long = 0,
    var name:String = "") : Serializable
{
    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != this.javaClass) return false

        other as ActivityGroup
        return other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Dao
interface ActivityGroupDao {
    @Query("select * from ActivityGroup")
    fun getAll() : LiveData<List<ActivityGroup>>

    @Insert
    fun insert(activityGroup:ActivityGroup) : Long
}