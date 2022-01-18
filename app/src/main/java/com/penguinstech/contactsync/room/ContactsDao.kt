package com.penguinstech.contactsync.room

import androidx.room.*
import java.text.SimpleDateFormat
import java.util.*

@Dao
interface ContactsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(contactsData: Contacts?)
    fun insert(contactsData: Contacts?) {
        contactsData!!.updated_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(Date())
        insertData(contactsData)
    }

    @get:Query("SELECT * FROM contactsdata ORDER BY name ASC")
    val all: List<Contacts>


    @get:Query("SELECT * FROM contactsdata ORDER BY updated_at DESC")
    val allByDate: List<Contacts>

    @Query("SELECT * FROM contactsdata ORDER BY name ASC")
    fun getContacts(): List<Contacts>

    @Update
    fun updateData(contactsData: Contacts?)

    fun update(contactsData: Contacts?) {
        contactsData!!.updated_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(Date())
        updateData(contactsData)
    }

    @Query("SELECT * FROM contactsdata WHERE id = :id")
    fun getContactInfo(id: String?): Contacts

    @Query("SELECT * FROM contactsdata WHERE name LIKE :search ORDER BY name ASC")
    fun getBySearch(search: String): List<Contacts>

    @Query("UPDATE contactsdata SET favorite = :fav WHERE id = :id")
    fun updatefav(id: Int?, fav: Boolean)

    @Query("UPDATE contactsdata SET label = :label WHERE id = :id")
    fun updateContact(id: Int?, label: String)

    @Query("UPDATE contactsdata SET label = :label WHERE raw_id = :id")
    fun updateNewContact(id: Int?, label: String)

    @Query("SELECT * FROM contactsdata WHERE label IS NULL OR label != :salesTag ORDER BY name ASC")
    fun getDisplayContacts(salesTag: String): List<Contacts>

    @Query("SELECT * FROM contactsdata WHERE label = :salesTag ORDER BY name ASC")
    fun getFilterContacts(salesTag: String): List<Contacts>


    @Query("SELECT * FROM contactsdata WHERE updated_at >= :date ORDER BY name ASC")
    fun filterContactsByDate(date: String?): List<Contacts>

    @Query("SELECT COUNT(label) FROM contactsdata WHERE label = :salesTag")
    fun getPipeCount(salesTag: String): Int

    @Query("SELECT * FROM contactsdata WHERE id = :id")
    fun getSingleContact(id: Int): Contacts

}