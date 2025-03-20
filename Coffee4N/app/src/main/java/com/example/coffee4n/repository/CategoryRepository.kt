package com.example.coffee4n.repository

import com.google.firebase.database.FirebaseDatabase
import com.example.coffee4n.model.Category
import com.example.coffee4n.model.database.CategoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val firebaseDatabase: FirebaseDatabase
) {
    suspend fun getAllCategoriesFromLocal(): List<Category> {
        return categoryDao.getAllCategories()
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }

    private suspend fun syncCategoriesFromRemote() {
        val snapshot = firebaseDatabase.getReference("categories").get().await()
        val categories = snapshot.children.mapNotNull { it.getValue(Category::class.java) }
        categories.forEach { categoryDao.insertCategory(it) }
    }

    suspend fun addCategory(category: Category) {
        categoryDao.insertCategory(category)
        firebaseDatabase.getReference("categories").child(category.id.toString()).setValue(category).await()
    }

    suspend fun deleteCategory(id: Int) {
        categoryDao.deleteCategory(id)
        firebaseDatabase.getReference("categories").child(id.toString()).removeValue().await()
    }

    fun getCategoriesFlow(): Flow<List<Category>> = flow {
        emit(categoryDao.getAllCategories())
        syncCategoriesFromRemote()
        emit(categoryDao.getAllCategories())
    }
}