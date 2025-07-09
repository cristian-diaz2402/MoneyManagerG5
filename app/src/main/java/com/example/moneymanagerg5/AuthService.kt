package com.example.moneymanagerg5

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// Data classes para request y response

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val access_token: String?,
    val token_type: String?,
    val user_id: Int?,
    val expires_in: Int?,
    val detail: String? = null
)

interface AuthApi {
    @POST("/auth/login-json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

object AuthService {
    private const val BASE_URL = "https://backendcd.onrender.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val api: AuthApi = retrofit.create(AuthApi::class.java)
} 