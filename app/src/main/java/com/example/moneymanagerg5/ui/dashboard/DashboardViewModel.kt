package com.example.moneymanagerg5.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.moneymanagerg5.GastoService
import com.example.moneymanagerg5.GastoResponse
import com.example.moneymanagerg5.UserData

data class EstadisticasUiState(
    val gastos: List<GastoResponse> = emptyList(),
    val userData: UserData? = null,
    val presupuestoDiario: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val categoriaSeleccionada: String? = null
)

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EstadisticasUiState())
    val uiState: StateFlow<EstadisticasUiState> = _uiState.asStateFlow()

    init {
        cargarEstadisticas()
        cargarDatosUsuario()
    }
    
    private fun cargarDatosUsuario() {
        viewModelScope.launch {
            try {
                val userData = GastoService.getUserData()
                val presupuestoDiario = calcularPresupuestoDiario(userData)
                
                _uiState.value = _uiState.value.copy(
                    userData = userData,
                    presupuestoDiario = presupuestoDiario
                )
            } catch (e: Exception) {
                // Si hay error al cargar datos del usuario, mantener valores por defecto
                _uiState.value = _uiState.value.copy(
                    userData = null,
                    presupuestoDiario = 0.0
                )
            }
        }
    }
    
    private fun calcularPresupuestoDiario(userData: UserData?): Double {
        return when (userData?.periodo_presupuesto?.lowercase()) {
            "diario" -> userData.presupuesto ?: 0.0
            "semanal" -> (userData.presupuesto ?: 0.0) / 7.0
            "mensual" -> (userData.presupuesto ?: 0.0) / 30.0
            else -> 0.0
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = GastoService.obtenerTodosLosGastos()
            result.fold(
                onSuccess = { gastos ->
                    _uiState.value = _uiState.value.copy(
                        gastos = gastos,
                        isLoading = false,
                        errorMessage = null,
                        categoriaSeleccionada = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al cargar estadísticas: ${exception.message}"
                    )
                }
            )
        }
    }
    
    fun filtrarPorCategoria(categoria: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            val result = GastoService.obtenerGastosPorCategoriaAuth(categoria)
            result.fold(
                onSuccess = { gastos ->
                    _uiState.value = _uiState.value.copy(
                        gastos = gastos,
                        isLoading = false,
                        errorMessage = null,
                        categoriaSeleccionada = categoria
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al filtrar por categoría: ${exception.message}"
                    )
                }
            )
        }
    }
    
    fun limpiarFiltro() {
        cargarEstadisticas()
    }
    
    fun recargarDatos() {
        val categoriaActual = _uiState.value.categoriaSeleccionada
        cargarDatosUsuario() // Recargar datos del usuario
        if (categoriaActual != null) {
            filtrarPorCategoria(categoriaActual)
        } else {
            cargarEstadisticas()
        }
    }
}