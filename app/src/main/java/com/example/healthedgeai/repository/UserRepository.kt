package com.example.healthedgeai.repository

import androidx.lifecycle.LiveData
import com.example.healthedgeai.model.User
import com.example.healthedgeai.model.UserDao

class UserRepository(private val userDao: UserDao) {

    suspend fun insertUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.getUserByCredentials(email, password)
    }

    fun getUserById(userId: String): LiveData<User> {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
}