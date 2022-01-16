package com.penguinstech.contactsync.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "contUPDt")
data class ContUPDt(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "userId") var userId: String,
    @ColumnInfo(name = "contId") var contId: Int,
    @ColumnInfo(name = "timestamp") var timestamp: Long,
    @ColumnInfo(name = "update") var update: String,
    @ColumnInfo(name = "fileURI") val fileURI: String?,
    @ColumnInfo(name = "actType") val actType: String?,
) : Serializable {
}