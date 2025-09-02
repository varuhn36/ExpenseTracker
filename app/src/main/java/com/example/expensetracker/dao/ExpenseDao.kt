package com.example.expensetracker.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.expensetracker.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert
    suspend fun createExpense(expenseEntity: ExpenseEntity)

    @Update
    suspend fun updateExpense(expenseEntity: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expenseEntity: ExpenseEntity)

    @Query("SELECT * FROM expenseentity WHERE title LIKE :title")
    fun findExpenseByTitle(title: String) : Flow<List<ExpenseEntity>>

    @Query("SELECT DISTINCT category FROM expenseentity")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM expenseentity WHERE category = :category")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenseentity")
    fun observeAll(): Flow<List<ExpenseEntity>>
}