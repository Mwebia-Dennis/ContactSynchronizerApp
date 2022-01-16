package com.penguinstech.contactsync.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ContUpdateDao {
    @Query("SELECT * FROM contUPDt  WHERE contId = :contId  ORDER BY timestamp ASC")
    fun getcontUpdate(contId: Int): List<ContUPDt>

    @Query("SELECT * FROM contUPDt WHERE `update` == 'Contact created' AND contId = :id")
    fun notiExists(id: Int): Int

    @Insert
    fun addcontUpdate(insert: ContUPDt)

    @Insert
    fun addcontUpdateList(update:List<ContUPDt>)

    @Delete
    fun deletecontUpdate(update: ContUPDt)

    @Query("DELETE FROM contUPDt")
    fun deleteAllcontUpdates()

    @Query("UPDATE contUPDt SET actType = :label WHERE id = :updateId")
    fun updateType(updateId: Int?, label: String)
}