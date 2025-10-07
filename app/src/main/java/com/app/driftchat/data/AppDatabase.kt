package com.app.driftchat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.app.driftchat.data.Converters
import com.app.driftchat.data.UserDataDao
import androidx.room.TypeConverters
import com.app.driftchat.domainmodel.UserData

@Database(entities = [UserData::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "drift_chat_database"
                )
                    .fallbackToDestructiveMigration() // Use with caution in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
