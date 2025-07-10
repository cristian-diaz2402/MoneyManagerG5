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

    suspend fun obtenerGastosPorCategoria(
        categoria: String,
        limite: Int = 100
    ): Result<List<GastoResponse>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            val userId = getUserId()
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.obtenerGastosPorCategoria(
                authorization = authorization,
                usuarioId = userId,
                categoria = categoria,
                limite = limite
            )
            if (response.isSuccessful) {
                val gastos = response.body()
                if (gastos != null) {
                    Result.success(gastos)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al obtener gastos: $errorBody")
                Result.failure(Exception("Error al obtener gastos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al obtener gastos", e)
            Result.failure(e)
        }
    }

    suspend fun editarGasto(
        gastoId: Int,
        descripcion: String,
        monto: String,
        categoria: String
    ): Result<GastoResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            val userId = getUserId()
            val authorization = "Bearer $accessToken"
            val request = GastoRequest(
                descripcion = descripcion,
                monto = monto,
                categoria = categoria,
                usuario_id = userId.toString()
            )
            val response = AuthService.api.editarGasto(
                authorization = authorization,
                gastoId = gastoId,
                usuarioId = userId,
                request = request
            )
            if (response.isSuccessful) {
                val gastoResponse = response.body()
                if (gastoResponse != null) {
                    Result.success(gastoResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al editar gasto: $errorBody")
                Result.failure(Exception("Error al editar gasto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al editar gasto", e)
            Result.failure(e)
        }
    }

    suspend fun eliminarGasto(
        gastoId: Int
    ): Result<EliminarGastoResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            val userId = getUserId()
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.eliminarGasto(
                authorization = authorization,
                gastoId = gastoId,
                usuarioId = userId
            )
            if (response.isSuccessful) {
                val eliminarResponse = response.body()
                if (eliminarResponse != null) {
                    Result.success(eliminarResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al eliminar gasto: $errorBody")
                Result.failure(Exception("Error al eliminar gasto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al eliminar gasto", e)
            Result.failure(e)
        }
    }
} 