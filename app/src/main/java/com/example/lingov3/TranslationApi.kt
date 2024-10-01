package com.example.lingov3


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface TranslationApi {
    @Headers(
        "Ocp-Apim-Subscription-Key: 80416eaa204c4c61b64c2af2fec871a3",
        "Content-Type: application/json"
    )
    @POST("translate")
    fun translate(
        @Query("api-version") apiVersion: String = "3.0",
        @Query("from") fromLanguage: String,
        @Query("to") toLanguage: String,
        @Body requestBody: List<TranslationRequest>
    ): Call<List<TranslationResponse>>
}

data class TranslationRequest(val text: String)

data class TranslationResponse(val translations: List<Translation>)
data class Translation(val text: String, val to: String)
