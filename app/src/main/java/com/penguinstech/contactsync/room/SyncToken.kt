package com.penguinstech.contactsync.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "sync_token", indices = [Index(value = ["id"], unique = true)])
class SyncToken : Serializable {

    @PrimaryKey(autoGenerate = true)
    var id = 0
    @ColumnInfo(name = "last_sync") var lastSync: String? = null
}