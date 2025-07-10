package com.example.moneymanagerg5.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.moneymanagerg5.GastoService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.util.Log

@Composable
fun HomeScreen() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Comida", "Transporte", "Varios")

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        when (selectedTabIndex) {
            0 -> GastoFormWithGrid(nombreCategoria = "Comida")
            1 -> GastoFormWithGrid(nombreCategoria = "Transporte")
            2 -> GastoFormWithGrid(nombreCategoria = "Varios")
        }
    }
}

@Composable
fun GastoFormWithGrid(nombreCategoria: String) {
    var valor by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }
    var gastos by remember { mutableStateOf(listOf<GastoItem>()) }
    val coroutineScope = rememberCoroutineScope()
    var editandoId by remember { mutableStateOf<Int?>(null) }

    // Cargar gastos al iniciar o al cambiar de categoría
    LaunchedEffect(nombreCategoria) {
        isLoading = true
        showErrorMessage = null
        val categoriaApi = when (nombreCategoria.lowercase()) {
            "comida" -> "comida"
            "transporte" -> "transporte"
            "varios" -> "varios"
            else -> "varios"
        }
        val result = GastoService.obtenerGastosPorCategoria(categoriaApi)
        result.fold(
            onSuccess = { lista ->
                gastos = lista.map {
                    GastoItem(
                        id = it.id,
                        descripcion = it.descripcion,
                        valor = it.monto.toString()
                    )
                }
            },
            onFailure = { exception ->
                showErrorMessage = "Error al cargar gastos: ${exception.message}"
            }
        )
        isLoading = false
    }

    fun limpiarFormulario() {
        valor = ""
        descripcion = ""
        editandoId = null
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = "Gastos en $nombreCategoria",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(text = "Ingresa el valor", fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Ingresa el valor") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text(text = "Añade una descripcion (opcional)", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (valor.isNotBlank()) {
                    coroutineScope.launch {
                        isLoading = true
                        showErrorMessage = null
                        showSuccessMessage = false
                        val categoriaApi = when (nombreCategoria) {
                            "Comida" -> "comida"
                            "Transporte" -> "transporte"
                            "Varios" -> "varios"
                            else -> "varios"
                        }
                        if (editandoId == null) {
                            // Registrar nuevo gasto
                            try {
                                val result = GastoService.registrarGasto(
                                    descripcion = descripcion.ifBlank { "Sin descripción" },
                                    monto = valor,
                                    categoria = categoriaApi
                                )
                                result.fold(
                                    onSuccess = { gastoResponse ->
                                        showSuccessMessage = true
                                        valor = ""
                                        descripcion = ""
                                        gastos = gastos + GastoItem(
                                            id = gastoResponse.id,
                                            descripcion = gastoResponse.descripcion,
                                            valor = gastoResponse.monto.toString()
                                        )
                                    },
                                    onFailure = { exception ->
                                        showErrorMessage = "Error al registrar gasto: ${exception.message}"
                                    }
                                )
                            } catch (e: Exception) {
                                showErrorMessage = "Error inesperado: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        } else {
                            // Editar gasto existente
                            try {
                                val result = GastoService.editarGasto(
                                    gastoId = editandoId!!,
                                    descripcion = descripcion.ifBlank { "Sin descripción" },
                                    monto = valor,
                                    categoria = categoriaApi
                                )
                                result.fold(
                                    onSuccess = { gastoResponse ->
                                        showSuccessMessage = true
                                        // Actualizar el gasto en la lista
                                        gastos = gastos.map {
                                            if (it.id == gastoResponse.id) it.copy(
                                                descripcion = gastoResponse.descripcion,
                                                valor = gastoResponse.monto.toString()
                                            ) else it
                                        }
                                        limpiarFormulario()
                                    },
                                    onFailure = { exception ->
                                        showErrorMessage = "Error al actualizar gasto: ${exception.message}"
                                    }
                                )
                            } catch (e: Exception) {
                                showErrorMessage = "Error inesperado: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = valor.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colors.onSurface
                )
            } else {
                Text(if (editandoId == null) "GUARDAR" else "ACTUALIZAR", color = MaterialTheme.colors.onSurface)
            }
        }
        // Mostrar mensajes de éxito o error
        if (showSuccessMessage) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                showSuccessMessage = false
            }
            Card(
                backgroundColor = MaterialTheme.colors.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "¡Operación exitosa!",
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        if (showErrorMessage != null) {
            LaunchedEffect(showErrorMessage) {
                kotlinx.coroutines.delay(5000)
                showErrorMessage = null
            }
            Card(
                backgroundColor = MaterialTheme.colors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = showErrorMessage ?: "",
                    color = MaterialTheme.colors.onError,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        GastosComidaGrid(
            gastos,
            onEditar = { gasto ->
                valor = gasto.valor
                descripcion = gasto.descripcion
                editandoId = gasto.id
            },
            onEliminar = { gasto ->
                coroutineScope.launch {
                    isLoading = true
                    showErrorMessage = null
                    val result = GastoService.eliminarGasto(gasto.id)
                    result.fold(
                        onSuccess = { _ ->
                            showSuccessMessage = true
                            gastos = gastos.filter { it.id != gasto.id }
                        },
                        onFailure = { exception ->
                            showErrorMessage = "Error al eliminar gasto: ${exception.message}"
                        }
                    )
                    isLoading = false
                }
            }
        )
    }
}

// Modelo de dato para la tabla
data class GastoItem(val id: Int, val descripcion: String, val valor: String)

@Composable
fun GastosComidaGrid(gastos: List<GastoItem>, onEditar: (GastoItem) -> Unit, onEliminar: (GastoItem) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Encabezados
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text = "Descripcion",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Valor",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(0.5f)) // Para los iconos
        }
        Divider()
        
        // Lista con scroll usando LazyColumn
        if (gastos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay gastos registrados",
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.body2
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Altura máxima para evitar que ocupe toda la pantalla
            ) {
                items(gastos) { gasto ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = gasto.descripcion,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = gasto.valor,
                            modifier = Modifier.weight(1f)
                        )
                        Row(modifier = Modifier.weight(0.5f)) {
                            IconButton(onClick = { onEditar(gasto) }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { onEliminar(gasto) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                    Divider()
                }
            }
        }
    }
} 