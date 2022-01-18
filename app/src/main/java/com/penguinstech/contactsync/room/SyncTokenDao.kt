package com.penguinstech.contactsync.room

import androidx.room.*

@Dao
interface SyncTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(token: SyncToken)

    @Update
    fun updateData(token: SyncToken)


    @get:Query("SELECT * FROM sync_token ORDER BY last_sync DESC limit 1")
    val getToken: List<SyncToken>
}