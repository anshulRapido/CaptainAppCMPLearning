package com.rapido.captainapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [OrderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CaptainDatabase : RoomDatabase() {
    abstract fun orderDao(): OrderDao

    companion object {
//        Thread-safe visibility across threads
//        instance to hold single instance
//        context.applicationContext -> survives activity lifecycle
        @Volatile
        private var INSTANCE: CaptainDatabase? = null

        fun getDatabase(context: Context): CaptainDatabase {
//          synchronized(this) Thread safe block only one thread enters at a time
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaptainDatabase::class.java,
                    "captain_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}