package com.example.expensetracker.di

import com.example.expensetracker.repo.ExpenseRepository
import com.example.expensetracker.repo.ExpenseRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEntryRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

}