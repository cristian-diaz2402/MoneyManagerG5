package com.example.moneymanagerg5.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.moneymanagerg5.UserData
import com.example.moneymanagerg5.GastoService

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userData: UserData? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        cargarDatosUsuario()
    }
    
    fun cargarDatosUsuario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userData = GastoService.getUserData()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    userData = userData
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar datos del usuario: ${e.localizedMessage}"
                )
            }
        }
    }
    
    fun actualizarDatos(
        nombre: String,
        telefono: String,
        presupuesto: String,
        periodoPrefijo: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            
            try {
                // Validaciones
                if (nombre.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "El nombre no puede estar vacío"
                    )
                    return@launch
                }
                
                if (nombre.length < 2) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "El nombre debe tener al menos 2 caracteres"
                    )
                    return@launch
                }
                
                if (telefono.isNotBlank() && !telefono.matches(Regex("^\\d{10}$"))) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "El teléfono debe tener exactamente 10 dígitos"
                    )
                    return@launch
                }
                
                val presupuestoDouble = if (presupuesto.isBlank()) {
                    null
                } else {
                    presupuesto.toDoubleOrNull()?.takeIf { it >= 0 }
                        ?: run {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "El presupuesto debe ser un número positivo o cero"
                            )
                            return@launch
                        }
                }
                
                if (!listOf("diario", "semanal", "mensual").contains(periodoPrefijo)) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "El período debe ser diario, semanal o mensual"
                    )
                    return@launch
                }
                
                // Llamar al servicio para actualizar usando el nuevo endpoint
                val result = GastoService.actualizarPerfilPost(
                    nombre = nombre.takeIf { it.isNotBlank() },
                    telefono = telefono.takeIf { it.isNotBlank() },
                    presupuesto = presupuestoDouble,
                    periodoPrefijo = periodoPrefijo
                )
                
                result.fold(
                    onSuccess = { userData ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userData = userData,
                            error = null,
                            successMessage = "Perfil actualizado correctamente"
                        )
                        
                        // Limpiar mensaje de éxito después de 3 segundos
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(3000)
                            _uiState.value = _uiState.value.copy(successMessage = null)
                        }
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.localizedMessage ?: "Error al actualizar perfil"
                        )
                    }
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error inesperado: ${e.localizedMessage}"
                )
            }
        }
    }
}
