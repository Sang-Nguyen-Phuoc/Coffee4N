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
    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)

    suspend fun syncUserFromRemote(userId: Int) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val snapshot = firebaseDatabase.getReference("users").child(userId.toString()).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.let { userDao.insertUser(it) }
        }
    }

    suspend fun updateUser(user: User) {
        userDao.insertUser(user)
        firebaseDatabase.getReference("users").child(user.id.toString()).setValue(user).await()
    }

    fun getUserFlow(userId: Int): Flow<User?> = flow {
        emit(userDao.getUserById(userId))
        syncUserFromRemote(userId)
        emit(userDao.getUserById(userId))
    }

    suspend fun register(email: String, password: String, username: String): String? {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(
                    id = firebaseUser.uid.hashCode(),
                    username = username,
                    email = email,
                    phone = "",
                    name = "",
                    address = ""
                )
                firebaseDatabase.getReference("users").child(firebaseUser.uid).setValue(user).await()
                userDao.insertUser(user)
                firebaseUser.uid
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun login(email: String, password: String): String? {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.uid
        } catch (e: Exception) {
            null
        }
    }

    suspend fun sendOTP(email: String): Boolean {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun verifyOTP(token: String, newPassword: String): Boolean {
        return try {
            firebaseAuth.confirmPasswordReset(token, newPassword).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun resetPassword(email: String, newPassword: String) {
        val user = firebaseAuth.currentUser
        if (user != null && user.email == email) {
            user.updatePassword(newPassword).await()
        }
    }
}