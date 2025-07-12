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
    private const val KEY_USER_DATA = "user_data"
    
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        NotificationService.initialize(context)
    }
    
    fun saveAuthData(accessToken: String, userId: Int, userData: UserData? = null) {
        val editor = prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putInt(KEY_USER_ID, userId)
        
        userData?.let {
            editor.putString(KEY_USER_DATA, gson.toJson(it))
        }
        
        editor.apply()
    }
    
    fun getUserData(): UserData? {
        val userDataJson = prefs.getString(KEY_USER_DATA, null)
        return userDataJson?.let {
            try {
                gson.fromJson(it, UserData::class.java)
            } catch (e: Exception) {
                null
            }
        }
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
            .remove(KEY_USER_DATA)
            .remove(KEY_USER_ID)
            .apply()
    }
    
    suspend fun registrarUsuario(
        nombre: String,
        email: String,
        password: String,
        telefono: String? = null,
        presupuesto: Double? = null,
        periodoPrefijo: String? = null
    ): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                nombre = nombre,
                email = email,
                password = password,
                telefono = telefono,
                presupuesto = presupuesto,
                periodo_presupuesto = periodoPrefijo
            )
            
            val response = AuthService.api.register(request)
            
            if (response.isSuccessful) {
                val userData = response.body()
                if (userData != null) {
                    Log.d("GastoService", "Usuario registrado exitosamente: ${userData.id}")
                    
                    // Crear notificación de éxito
                    val respuestaCompleta = gson.toJson(userData)
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.SUCCESS,
                        titulo = "Usuario Registrado",
                        mensaje = "Usuario ${userData.nombre} registrado correctamente",
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(userData)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al registrar usuario: $errorBody")
                
                // Notificación de error
                val mensajeError = when (response.code()) {
                    400 -> "El email ya está registrado"
                    422 -> "Error de validación en los datos"
                    else -> "Error al registrar usuario: ${response.code()}"
                }
                
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Registrar Usuario",
                    mensaje = mensajeError,
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception(mensajeError))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al registrar usuario", e)
            
            // Notificación de error de red
            NotificationService.addNotification(
                tipo = TipoNotificacion.ERROR,
                titulo = "Error de Red",
                mensaje = "No se pudo conectar al servidor: ${e.localizedMessage}",
                respuestaCompleta = e.stackTraceToString()
            )
            
            Result.failure(e)
        }
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

    suspend fun obtenerTodosLosGastos(): Result<List<GastoResponse>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.obtenerTodosLosGastos(authorization)
            
            if (response.isSuccessful) {
                val gastos = response.body()
                if (gastos != null) {
                    Log.d("GastoService", "Obtenidos ${gastos.size} gastos del usuario")
                    
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(gastos)
                    val mensaje = "Se obtuvieron ${gastos.size} gastos del usuario autenticado"
                    
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.GASTO_NORMAL,
                        titulo = "Estadísticas Cargadas",
                        mensaje = mensaje,
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(gastos)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al obtener todos los gastos: $errorBody")
                
                // Notificación de error
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Cargar Estadísticas",
                    mensaje = "Error ${response.code()}: ${response.message()}",
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception("Error al obtener todos los gastos: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al obtener todos los gastos", e)
            Result.failure(e)
        }
    }

    suspend fun obtenerGastosPorCategoriaAuth(
        categoria: String
    ): Result<List<GastoResponse>> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.obtenerGastosPorCategoriaAuth(authorization, categoria)
            
            if (response.isSuccessful) {
                val gastos = response.body()
                if (gastos != null) {
                    Log.d("GastoService", "Obtenidos ${gastos.size} gastos de categoría '$categoria' del usuario autenticado")
                    
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(gastos)
                    val mensaje = "Se obtuvieron ${gastos.size} gastos de categoría '$categoria'"
                    
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.GASTO_NORMAL,
                        titulo = "Gastos por Categoría - ${categoria.uppercase()}",
                        mensaje = mensaje,
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(gastos)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al obtener gastos por categoría: $errorBody")
                
                // Notificación de error
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Filtrar por Categoría",
                    mensaje = "Error ${response.code()}: ${response.message()}",
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception("Error al obtener gastos por categoría: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al obtener gastos por categoría", e)
            Result.failure(e)
        }
    }
    
    suspend fun actualizarPerfil(
        nombre: String? = null,
        telefono: String? = null,
        presupuesto: Double? = null,
        periodoPrefijo: String? = null
    ): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            
            val request = ActualizarPerfilRequest(
                nombre = nombre,
                telefono = telefono,
                presupuesto = presupuesto,
                periodo_presupuesto = periodoPrefijo
            )
            
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.actualizarPerfil(authorization, request)
            
            if (response.isSuccessful) {
                val userData = response.body()
                if (userData != null) {
                    Log.d("GastoService", "Perfil actualizado exitosamente")
                    
                    // Actualizar los datos guardados localmente
                    saveAuthData(accessToken, userData.id, userData)
                    
                    // Crear notificación de éxito
                    val respuestaCompleta = gson.toJson(userData)
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.SUCCESS,
                        titulo = "Perfil Actualizado",
                        mensaje = "Tu perfil se ha actualizado correctamente",
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(userData)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al actualizar perfil: $errorBody")
                
                // Notificación de error
                val mensajeError = when (response.code()) {
                    400 -> "Datos inválidos"
                    422 -> "Error de validación en los datos"
                    else -> "Error al actualizar perfil: ${response.code()}"
                }
                
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Actualizar Perfil",
                    mensaje = mensajeError,
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception(mensajeError))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al actualizar perfil", e)
            
            // Notificación de error de red
            NotificationService.addNotification(
                tipo = TipoNotificacion.ERROR,
                titulo = "Error de Red",
                mensaje = "No se pudo conectar al servidor: ${e.localizedMessage}",
                respuestaCompleta = e.stackTraceToString()
            )
            
            Result.failure(e)
        }
    }
    
    suspend fun actualizarPerfilPost(
        nombre: String? = null,
        telefono: String? = null,
        presupuesto: Double? = null,
        periodoPrefijo: String? = null
    ): Result<UserData> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            
            val request = ActualizarPerfilRequest(
                nombre = nombre,
                telefono = telefono,
                presupuesto = presupuesto,
                periodo_presupuesto = periodoPrefijo
            )
            
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.actualizarPerfilPost(authorization, request)
            
            if (response.isSuccessful) {
                val userData = response.body()
                if (userData != null) {
                    Log.d("GastoService", "Perfil actualizado exitosamente con nuevo endpoint")
                    
                    // Actualizar los datos guardados localmente
                    saveAuthData(accessToken, userData.id, userData)
                    
                    // Crear notificación de éxito
                    val respuestaCompleta = gson.toJson(userData)
                    NotificationService.addNotification(
                        tipo = TipoNotificacion.SUCCESS,
                        titulo = "Perfil Actualizado (Nuevo Endpoint)",
                        mensaje = "Tu perfil se ha actualizado correctamente usando el nuevo endpoint",
                        respuestaCompleta = respuestaCompleta
                    )
                    
                    Result.success(userData)
                } else {
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("GastoService", "Error al actualizar perfil con nuevo endpoint: $errorBody")
                
                // Notificación de error
                val mensajeError = when (response.code()) {
                    400 -> "Datos inválidos"
                    422 -> "Error de validación en los datos"
                    else -> "Error al actualizar perfil: ${response.code()}"
                }
                
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Actualizar Perfil (Nuevo Endpoint)",
                    mensaje = mensajeError,
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception(mensajeError))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al actualizar perfil con nuevo endpoint", e)
            
            // Notificación de error de red
            NotificationService.addNotification(
                tipo = TipoNotificacion.ERROR,
                titulo = "Error de Red (Nuevo Endpoint)",
                mensaje = "No se pudo conectar al servidor: ${e.localizedMessage}",
                respuestaCompleta = e.stackTraceToString()
            )
            
            Result.failure(e)
        }
    }
    
    suspend fun editarGastoUsuario(
        gastoId: Int,
        descripcion: String? = null,
        monto: Double? = null,
        categoria: String? = null,
        fecha: String? = null
    ): Result<GastoResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            
            val gastoUpdate = GastoUpdate(
                descripcion = descripcion,
                monto = monto,
                categoria = categoria,
                fecha = fecha
            )
            
            val request = EditarGastoUsuarioRequest(
                gasto_id = gastoId,
                gasto_update = gastoUpdate
            )
            
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.editarGastoUsuario(authorization, request)
            
            if (response.isSuccessful) {
                val gastoResponse = response.body()
                if (gastoResponse != null) {
                    Log.d("GastoService", "Gasto editado exitosamente con nuevo endpoint: ${gastoResponse.id}")
                    
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
                Log.e("GastoService", "Error al editar gasto con nuevo endpoint: $errorBody")
                
                // Notificación de error
                val mensajeError = when (response.code()) {
                    404 -> "Gasto no encontrado"
                    400 -> "Datos inválidos para editar"
                    else -> "Error al editar gasto: ${response.code()}"
                }
                
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Editar Gasto",
                    mensaje = mensajeError,
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception(mensajeError))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al editar gasto con nuevo endpoint", e)
            
            // Notificación de error de red
            NotificationService.addNotification(
                tipo = TipoNotificacion.ERROR,
                titulo = "Error de Red",
                mensaje = "No se pudo conectar al servidor: ${e.localizedMessage}",
                respuestaCompleta = e.stackTraceToString()
            )
            
            Result.failure(e)
        }
    }
    
    suspend fun eliminarGastoUsuario(
        gastoId: Int
    ): Result<EliminarGastoUsuarioResponse> = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()
            if (accessToken == null) {
                return@withContext Result.failure(Exception("No hay token de autenticación"))
            }
            
            val request = EliminarGastoUsuarioRequest(gasto_id = gastoId)
            val authorization = "Bearer $accessToken"
            val response = AuthService.api.eliminarGastoUsuario(authorization, request)
            
            if (response.isSuccessful) {
                val eliminarResponse = response.body()
                if (eliminarResponse != null) {
                    Log.d("GastoService", "Gasto eliminado exitosamente con nuevo endpoint: ${eliminarResponse.id}")
                    
                    // Crear notificación con la respuesta completa
                    val respuestaCompleta = gson.toJson(eliminarResponse)
                    val mensaje = "Gasto eliminado exitosamente"
                    
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
                Log.e("GastoService", "Error al eliminar gasto con nuevo endpoint: $errorBody")
                
                // Notificación de error
                val mensajeError = when (response.code()) {
                    404 -> "Gasto no encontrado"
                    400 -> "Error en la solicitud"
                    else -> "Error al eliminar gasto: ${response.code()}"
                }
                
                NotificationService.addNotification(
                    tipo = TipoNotificacion.ERROR,
                    titulo = "Error al Eliminar Gasto",
                    mensaje = mensajeError,
                    respuestaCompleta = errorBody ?: "Error sin detalles"
                )
                
                Result.failure(Exception(mensajeError))
            }
        } catch (e: Exception) {
            Log.e("GastoService", "Excepción al eliminar gasto con nuevo endpoint", e)
            
            // Notificación de error de red
            NotificationService.addNotification(
                tipo = TipoNotificacion.ERROR,
                titulo = "Error de Red",
                mensaje = "No se pudo conectar al servidor: ${e.localizedMessage}",
                respuestaCompleta = e.stackTraceToString()
            )
            
            Result.failure(e)
        }
    }
}