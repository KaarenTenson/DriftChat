package com.app.driftchat.data

import retrofit2.Response
import com.app.driftchat.domainmodel.Quote
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

// API interface
interface QuotesApi {
    @GET("/v2/randomquotes") // get request to this endpoint
    suspend fun getRandomQuote(
        @Header("X-Api-Key") apiKey: String // header with api key
    ): Response<List<Quote>> // response with list of single quote data class
}

// Retrofit instance building
object RetrofitInstance {
    val api: QuotesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com")
            .addConverterFactory(GsonConverterFactory.create()) // converts JSON to data class
            .build()
            .create(QuotesApi::class.java) // implementation of QuotesApi interface
    }
}