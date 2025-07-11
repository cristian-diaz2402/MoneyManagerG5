package com.example.moneymanagerg5

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.DELETE

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

// Data classes para gastos
data class GastoRequest(
    val descripcion: String,
    val monto: String,
    val categoria: String,
    val usuario_id: String
)

data class GastoResponse(
    val descripcion: String?,
    val monto: Double,
    val categoria: String,
    val id: Int,
    val usuario_id: Int,
    val fecha: String,
    val dia_semana: Int,
    val hora_gasto: Int,
    val es_fin_semana: Boolean,
    val patron_temporal: String,
    val frecuencia_descripcion: Int,
    val es_recurrente: Boolean,
    val confianza_categoria: Double,
    val created_at: String,
    val updated_at: String
)

// Data class para la respuesta de eliminación
data class EliminarGastoResponse(
    val mensaje: String?,
    val gasto_eliminado: GastoEliminado?,
    val usuario_id: Int?
)
data class GastoEliminado(
    val id: Int,
    val descripcion: String,
    val monto: Double,
    val categoria: String,
    val fecha: String
)

// Data classes para verificación de categoría con ML
data class VerificarCategoriaRequest(
    val descripcion: String,
    val categoria_usuario: String
)

data class RecomendacionML(
    val categoria_sugerida: String,
    val categoria_original: String,
    val coincide: Boolean,
    val mensaje: String
)

data class VerificarCategoriaResponse(
    val recomendacion: RecomendacionML
)

// Data class para crear gasto con decisión
data class CrearGastoConDecisionRequest(
    val descripcion: String,
    val monto: Double,
    val categoria_original: String,
    val categoria_sugerida: String,
    val acepta_sugerencia: Boolean
)

data class GastoConDecisionResponse(
    val id: Int,
    val descripcion: String?,
    val monto: Double,
    val categoria: String,
    val fecha: String,
    val usuario_id: Int
)

interface AuthApi {
    @POST("/auth/login-json")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("/gastos")
    suspend fun registrarGasto(
        @Header("Authorization") authorization: String,
        @Body request: GastoRequest
    ): Response<GastoResponse>

    @GET("/gastos/usuario/{usuario_id}/categoria/{categoria}")
    suspend fun obtenerGastosPorCategoria(
        @Header("Authorization") authorization: String,
        @Path("usuario_id") usuarioId: Int,
        @Path("categoria") categoria: String,
        @Query("limite") limite: Int = 100
    ): Response<List<GastoResponse>>

    @PUT("/gastos/{gasto_id}")
    suspend fun editarGasto(
        @Header("Authorization") authorization: String,
        @Path("gasto_id") gastoId: Int,
        @Query("usuario_id") usuarioId: Int,
        @Body request: GastoRequest
    ): Response<GastoResponse>

    @DELETE("/gastos/{gasto_id}")
    suspend fun eliminarGasto(
        @Header("Authorization") authorization: String,
        @Path("gasto_id") gastoId: Int,
        @Query("usuario_id") usuarioId: Int
    ): Response<EliminarGastoResponse>

    @POST("/ml/verificar-categoria")
    suspend fun verificarCategoria(
        @Header("Authorization") authorization: String,
        @Body request: VerificarCategoriaRequest
    ): Response<VerificarCategoriaResponse>

    @POST("/gastos/crear-con-decision")
    suspend fun crearGastoConDecision(
        @Header("Authorization") authorization: String,
        @Body request: CrearGastoConDecisionRequest
    ): Response<GastoConDecisionResponse>
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