package com.rob_uniqueword.accountability

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class ActivityChain {
    @PrimaryKey(autoGenerate = true) var id:Long = 1
}