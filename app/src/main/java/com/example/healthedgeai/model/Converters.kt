package com.example.healthedgeai.model

import androidx.room.TypeConverter
import com.example.healthedgeai.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return enumValueOf<UserRole>(value)
    }
}