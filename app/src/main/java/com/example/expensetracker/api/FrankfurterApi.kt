package com.example.expensetracker.api

import retrofit2.http.GET
import retrofit2.http.Query
import java.math.BigDecimal

interface FrankfurterApi {
    @GET("latest")
    suspend fun getLatest(
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("amount") amount: BigDecimal? = null
    ): RatesResponse
}
