package com.rob_uniqueword.accountability

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import java.io.Serializable

@Entity
data class ActivityGroup(
    @PrimaryKey(autoGenerate = true) override var id:Long = 0,
    var name:String = "") : Serializable, DBEntity
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

    fun save(context:Context) : Long {
        return AppDatabase.getDb(context).activityGroupDao().insertOrUpdate(this)
    }
}

@Dao
abstract class ActivityGroupDao : BaseDao<ActivityGroup>() {
    @Query("select * from ActivityGroup")
    abstract fun getAll() : LiveData<List<ActivityGroup>>
}