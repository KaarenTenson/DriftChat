package com.app.driftchat.data

import retrofit2.Response
import com.app.driftchat.domainmodel.Quote
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface QuotesApi {
    @GET("/v2/randomquotes")
    suspend fun getRandomQuote(
        @Header("X-Api-Key") apiKey: String
    ): Response<List<Quote>>
}

object RetrofitInstance {
    val api: QuotesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.api-ninjas.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(QuotesApi::class.java)
    }
}