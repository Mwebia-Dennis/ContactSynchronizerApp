package com.penguinstech.contactsync.room

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    version = 5,
    entities = [Contacts::class, ContUPDt::class],
//    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                ?: synchronized(this) {
                    // Create database here
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "latomDeviceDB"
                    )
                        .allowMainThreadQueries() //allows Room to executing task in main thread
                        .fallbackToDestructiveMigration() //allows Room to recreate database if no migrations found
                        .build()

                    INSTANCE = instance
                    instance
                }
        }
    }

    abstract fun ContactDataDao(): ContactsDao
    abstract fun ContUpdateDao(): ContUpdateDao
}