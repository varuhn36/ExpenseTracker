package com.example.expensetracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.expensetracker.dao.ExpenseDao
import com.example.expensetracker.entities.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version =  2)
abstract class AppDataBase : RoomDatabase() {
    abstract fun expenseDao() : ExpenseDao
}