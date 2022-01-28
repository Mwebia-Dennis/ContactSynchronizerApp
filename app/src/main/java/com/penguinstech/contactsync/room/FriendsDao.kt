package com.penguinstech.contactsync.room

import androidx.room.*


@Dao
interface FriendsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(friend: Friend)

    @Update
    fun updateData(friend: Friend)


    @get:Query("SELECT * FROM friends ORDER BY id DESC")
    val getFriends: List<Friend>

    @Query("SELECT COUNT(id) FROM friends where friend_id = :friend_id")
    fun getFriendById(friend_id: String?): Int

    @Delete
    fun delete(friend: Friend)
}