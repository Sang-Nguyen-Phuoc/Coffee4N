package com.example.coffee4n.model.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.coffee4n.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>

    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM user WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Cho phép ghi đè nếu trùng ID
    suspend fun insertOrUpdateUser(user: User) // Đổi tên hàm cho rõ nghĩa

    @Update
    suspend fun updateUser(user: User)

}