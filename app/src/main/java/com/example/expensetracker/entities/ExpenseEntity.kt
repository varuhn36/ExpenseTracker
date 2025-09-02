package com.example.expensetracker.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExpenseEntity(
    @PrimaryKey (autoGenerate = true) val id: Long = 0L,
    val title: String,
    val category: String,
    val cost: Long,
    val store: String,
    val date: String,
    val currency: String
)
