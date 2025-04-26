package com.example.coffee4n.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.User
import com.example.coffee4n.model.database.UserDao
import com.example.coffee4n.session.LastIds
import com.example.coffee4n.session.Models
import com.example.coffee4n.session.OwnerSession
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resumeWithException

class UserRepository(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private val lastUserIdRef = firebaseDatabase.getReference(OwnerSession.getMetadataPath(lastModelId = LastIds.User))
    private val userRef = firebaseDatabase.getReference(OwnerSession.getReferencePath(model = Models.User))

    suspend fun getUserById(userId: Int): User? {
        // Try to get user from Room first
        var user = userDao.getUserById(userId)

        // If not found in Room, fetch from Firebase
        if (user == null) {
            try {
                val snapshot = userRef.child(userId.toString()).get().await()
                user = snapshot.getValue(User::class.java)
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to fetch user $userId from Firebase: ${e.message}")
            }
        }
        return user
    }

    suspend fun syncUserFromRemote(userId: Int) {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val snapshot = userRef.child(userId.toString()).get().await()
            val user = snapshot.getValue(User::class.java)
            user?.let { userDao.insertUser(it) }
        }
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
        userRef.child(user.id.toString()).setValue(user).await()
    }

    fun getUserFlow(userId: Int): Flow<User?> = flow {
        emit(userDao.getUserById(userId))
        syncUserFromRemote(userId)
        emit(userDao.getUserById(userId))
    }

    // Đăng ký user mới
    suspend fun register(email: String, password: String, username: String): Int? {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val userId = getNextUserId()
                val user = User(
                    id = userId,
                    firebaseUid = firebaseUser.uid,
                    username = username,
                    email = email,
                    phone = "",
                    name = "",
                    address = ""
                )

                // Lưu vào Firebase theo ID số nguyên
                userRef.child(userId.toString()).setValue(user).await()
                userDao.insertUser(user)
                userId
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun authenticate(email: String, password: String): FirebaseUser? {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user // Trả về FirebaseUser nếu đăng nhập thành công
        } catch (e: Exception) {
            Log.e("LoginError", "Login failed: ${e.message}")
            null
        }
    }

    suspend fun fetchRemoteUser(firebaseUid: String): User? {
        val snapshot = userRef.orderByChild("firebaseUid").equalTo(firebaseUid).get().await()
        return snapshot.children.firstOrNull()?.getValue(User::class.java)
    }

    suspend fun syncUserToLocal(user: User) {
        val localUser = userDao.getUserById(user.id)

        if (localUser != null) {
            val updatedUser = localUser.copy(
                username = user.username,
                email = user.email,
                phone = user.phone,
            )
            userDao.updateUser(updatedUser)
        } else {
            userDao.insertOrUpdateUser(user)
        }
    }

    // Đăng nhập và trả về ID số nguyên
    suspend fun loginAsCustomer(email: String, password: String): Int? {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            firebaseUser?.let { fu ->
                val snapshot = userRef.orderByChild("firebaseUid").equalTo(fu.uid).get().await()

                snapshot.children.firstOrNull()?.let { child ->
                    val remoteUser = child.getValue(User::class.java)
                    remoteUser?.let { user ->
                        // Kiểm tra user đã tồn tại trong local DB chưa
                        val localUser = userDao.getUserById(user.id)

                        if (localUser != null) {
                            // Cập nhật các trường cần thiết từ remote sang local
                            val updatedUser = localUser.copy(
                                username = user.username,
                                email = user.email,
                                phone = user.phone,
                                // ... các trường khác cần cập nhật
                            )
                            userDao.updateUser(updatedUser)
                        } else {
                            userDao.insertOrUpdateUser(user)
                        }

                        return user.id
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e("LoginError", "Login failed: ${e.message}")
            null
        }
    }

    // Get the current Firebase authentication token
    suspend fun getAuthToken(): String {
        return firebaseAuth.currentUser?.getIdToken(false)?.await()?.token ?: ""
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

    // Lấy ID tiếp theo tự động tăng
    private suspend fun getNextUserId(): Int {
        return suspendCancellableCoroutine { continuation ->
            lastUserIdRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val lastId = mutableData.getValue(Int::class.java) ?: 0
                    mutableData.value = lastId + 1
                    return Transaction.success(mutableData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null) {
                        continuation.resumeWithException(error.toException())
                    } else {
                        val nextId = currentData?.getValue(Int::class.java) ?: 1
                        continuation.resume(nextId) {}
                    }
                }
            })
        }
    }
    suspend fun syncUserByFirebaseUid(firebaseUid: String) {
        val snapshot = userRef
            .orderByChild("firebaseUid").equalTo(firebaseUid).get().await()

        snapshot.children.firstOrNull()?.let { ds ->
            val user = ds.getValue(User::class.java)
            user?.let { userDao.insertUser(it) }
        }
    }

    // Check if a token is valid
    suspend fun isTokenValid(token: String): Boolean {
        return try {
            // This will throw an exception if the token is invalid
            FirebaseAuth.getInstance().signInWithCustomToken(token).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}