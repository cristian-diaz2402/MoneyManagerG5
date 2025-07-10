package com.example.moneymanagerg5

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GastoService {
    private const val PREFS_NAME = "MoneyManagerPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    
    private lateinit var prefs: SharedPreferences
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveAuthData(accessToken: String, userId: Int) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putInt(KEY_USER_ID, userId)
            .apply()
    }
    
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }
    
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, 1) // Default a 1 si no hay usuario guardado
    }
    
    fun clearAuthData() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    suspend fun registrarGasto(
        descripcion: String,
        monto: String,
        categoria: String
    ): Result<GastoResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            
            val userId = getUserId().toString()
            val request = GastoRequest(
                descripcion = descripcion,
                monto = monto,
                categoria = categoria,
                usuario_id = userId
            )
            
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.registrarGasto(authorization, request)
            
            if (response.isSuccessful) {
                val gastoResponse = response.body()
                if (gastoResponse != null) {
                    Log.d("GastoService", "Gasto registrado exitosamente: ${gastoResponse.id}")
                    Result.success(gastoResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al registrar gasto: $errorBody")
                Result.failure(Exception("Error al registrar gasto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al registrar gasto", e)
            Result.failure(e)
        }
    }
} 