package com.example.coffee4n.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.User
import com.example.coffee4n.model.database.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    // Lấy thông tin người dùng từ local
    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    // Đồng bộ thông tin người dùng từ Firebase
    suspend fun syncUserFromRemote(userId: Int) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val snapshot = firebaseDatabase.getReference("users").child(userId.toString()).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.let { userDao.insertUser(it) }
        }
    }

    // Cập nhật thông tin người dùng
    suspend fun updateUser(user: User) {
        userDao.insertUser(user) // Lưu vào local
        firebaseDatabase.getReference("users").child(user.id.toString()).setValue(user).await() // Đẩy lên Firebase
    }

    // Lấy thông tin người dùng dưới dạng Flow
    fun getUserFlow(userId: Int): Flow<User?> = flow {
        emit(userDao.getUserById(userId))
        syncUserFromRemote(userId)
        emit(userDao.getUserById(userId))
    }
}