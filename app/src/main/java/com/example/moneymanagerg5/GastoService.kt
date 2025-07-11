package com.example.moneymanagerg5

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson

object GastoService {
    private const val PREFS_NAME = "MoneyManagerPrefs"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_USER_ID = "user_id"
    
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        NotificationService.initialize(context)
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

    suspend fun verificarCategoria(
        descripcion: String,
        categoriaUsuario: String
    ): Result<VerificarCategoriaResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            val request = VerificarCategoriaRequest(
                descripcion = descripcion,
                categoria_usuario = categoriaUsuario
            )
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.verificarCategoria(authorization, request)
            if (response.isSuccessful) {
                val verificarResponse = response.body()
                if (verificarResponse != null) {
                    Log.d("GastoService", "Categoría verificada exitosamente")
                    
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(verificarResponse)
                    val mensaje = if (verificarResponse.recomendacion.coincide) {
                        "✅ ${verificarResponse.recomendacion.mensaje}"
                    } else {
                        "💡 ${verificarResponse.recomendacion.mensaje}"
                    }
                    
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.VERIFICACION_CATEGORIA,
                        titulo = "Verificación ML - ${verificarResponse.recomendacion.categoria_original}",
                        mensaje = mensaje,
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(verificarResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al verificar categoría: $errorBody")
                
                // Notificación de error
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error en Verificación ML",
                    mensaje = "Error ${response.code()}: ${response.message()}",
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception("Error al verificar categoría: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al verificar categoría", e)
            Result.failure(e)
        }
    }

    suspend fun crearGastoConDecision(
        descripcion: String,
        monto: Double,
        categoriaOriginal: String,
        categoriaSugerida: String,
        aceptaSugerencia: Boolean
    ): Result<GastoConDecisionResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            
            val request = CrearGastoConDecisionRequest(
                descripcion = descripcion,
                monto = monto,
                categoria_original = categoriaOriginal,
                categoria_sugerida = categoriaSugerida,
                acepta_sugerencia = aceptaSugerencia,
                usuario_id = 1 // TODO: Obtener del almacenamiento de sesión cuando esté implementado
            )
            
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.crearGastoConDecision(authorization, request)
            
            if (response.isSuccessful) {
                val gastoResponse = response.body()
                if (gastoResponse != null) {
                    Log.d("GastoService", "Gasto creado con decisión exitosamente: ${gastoResponse.id}")
                    
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(gastoResponse)
                    val decision = if (aceptaSugerencia) "Aceptó" else "Rechazó"
                    val mensaje = "$decision sugerencia ML: ${gastoResponse.categoria} - $${gastoResponse.monto}"
                    
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.CREACION_GASTO,
                        titulo = "Gasto Creado con ML #${gastoResponse.id}",
                        mensaje = mensaje,
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(gastoResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al crear gasto con decisión: $errorBody")
                
                // Notificación de error
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Crear Gasto",
                    mensaje = "Error ${response.code()}: ${response.message()}",
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception("Error al crear gasto con decisión: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al crear gasto con decisión", e)
            Result.failure(e)
        }
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
                    
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(gastoResponse)
                    val mensaje = "Gasto registrado: ${gastoResponse.descripcion} - $${gastoResponse.monto}"
                    
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.GASTO_NORMAL,
                        titulo = "Gasto Registrado #${gastoResponse.id}",
                        mensaje = mensaje,
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(gastoResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al registrar gasto: $errorBody")
                
                // Notificación de error
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Registrar Gasto",
                    mensaje = "Error ${response.code()}: ${response.message()}",
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
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
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(gastoResponse)
                    val mensaje = "Gasto editado: ${gastoResponse.descripcion} - $${gastoResponse.monto}"
                    
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.EDITAR_GASTO,
                        titulo = "Gasto Editado #${gastoResponse.id}",
                        mensaje = mensaje,
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(gastoResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al editar gasto: $errorBody")
                
                // Notificación de error
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Editar Gasto",
                    mensaje = "Error ${response.code()}: ${response.message()}",
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
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
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(eliminarResponse)
                    val gastoInfo = eliminarResponse.gasto_eliminado
                    val mensaje = if (gastoInfo != null) {
                        "Gasto eliminado: ${gastoInfo.descripcion} - $${gastoInfo.monto}"
                    } else {
                        eliminarResponse.mensaje ?: "Gasto eliminado exitosamente"
                    }
                    
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.ELIMINAR_GASTO,
                        titulo = "Gasto Eliminado",
                        mensaje = mensaje,
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(eliminarResponse)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al eliminar gasto: $errorBody")
                
                // Notificación de error
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Eliminar Gasto",
                    mensaje = "Error ${response.code()}: ${response.message()}",
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception("Error al eliminar gasto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al eliminar gasto", e)
            Result.failure(e)
        }
    }
}