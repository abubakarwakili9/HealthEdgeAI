package com.example.healthedgeai.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [User::class, Patient::class, HealthRecord::class, VitalSignsTemplate::class], // Add VitalSignsTemplate.class here
    version = 2, // Increment version number
    exportSchema = false
)
@TypeConverters(Converters::class) // Add type converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun patientDao(): PatientDao
    abstract fun healthRecordDao(): HealthRecordDao
    abstract fun vitalSignsTemplateDao(): VitalSignsTemplateDao // Add this line

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
                    .fallbackToDestructiveMigration() // Add this line for easy development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}