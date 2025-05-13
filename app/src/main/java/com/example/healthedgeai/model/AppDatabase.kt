package com.example.healthedgeai.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [User::class, Patient::class, HealthRecord::class, VitalSignsTemplate::class],
    version = 2,  // Increment version number since we're adding a new entity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun patientDao(): PatientDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun vitalSignsTemplateDao(): VitalSignsTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_edge_ai_database"
                )
                    .fallbackToDestructiveMigration()  // This will recreate the database if the version changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}