package com.example.expensetracker.repo

import com.example.expensetracker.dao.ExpenseDao
import com.example.expensetracker.entities.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override suspend fun create(
        title: String,
        category: String,
        cost: Long,
        store: String,
        date: String,
        currency: String
    ) {
        withContext(Dispatchers.IO) {
            val expenseEntity = ExpenseEntity(
                title = title,
                category = category,
                cost = cost,
                store = store,
                date = date,
                currency = currency,
            )
            dao.createExpense(expenseEntity)
        }
    }

    override suspend fun update(expenseEntity: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            dao.updateExpense(expenseEntity)
        }
    }

    override suspend fun delete(expenseEntity: ExpenseEntity) {
        withContext(Dispatchers.IO) {
            dao.deleteExpense(expenseEntity)
        }
    }

    override fun findExpenseByTitle(title: String) : Flow<List<ExpenseEntity>> {
        return dao.findExpenseByTitle(title)
    }

    override fun observeAll(): Flow<List<ExpenseEntity>> {
        return dao.observeAll()
    }

    override fun getAllCategories(): Flow<List<String>> {
        return dao.getAllCategories()
    }


    override fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>> {
        return dao.getExpensesByCategory(category)
    }



}