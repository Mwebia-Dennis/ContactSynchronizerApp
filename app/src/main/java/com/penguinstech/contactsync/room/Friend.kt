package com.penguinstech.contactsync.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "friends", indices = [Index(value = ["id", "mobile_no"], unique = true)])
class Friend : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id = 0
    @ColumnInfo(name = "friend_id") var friend_id: String? = null
    @ColumnInfo(name = "firebase_id") var firebase_id: String? = null
    @ColumnInfo(name = "mobile_no") var mobile_no: String? = null
    @ColumnInfo(name = "name") var name: String? = null
}