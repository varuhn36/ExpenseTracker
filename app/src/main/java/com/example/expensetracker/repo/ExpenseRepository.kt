package com.example.expensetracker.repo


import com.example.expensetracker.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    suspend fun create(title: String, category: String, cost: Long, store: String, date: String, currency: String)

    suspend fun update(expenseEntity: ExpenseEntity)

    suspend fun delete(expenseEntity: ExpenseEntity)

    fun findExpenseByTitle(title: String) : Flow<List<ExpenseEntity>>

    fun observeAll() : Flow<List<ExpenseEntity>>

    fun getAllCategories(): Flow<List<String>>

    fun getExpensesByCategory(category: String) : Flow<List<ExpenseEntity>>

}