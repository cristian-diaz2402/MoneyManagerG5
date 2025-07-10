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

    // Cargar gastos al iniciar si la categoría es comida
    LaunchedEffect(nombreCategoria) {
        if (nombreCategoria.lowercase() == "comida") {
            isLoading = true
            showErrorMessage = null
            val result = GastoService.obtenerGastosPorCategoria("comida")
            result.fold(
                onSuccess = { lista ->
                    gastos = lista.map {
                        GastoItem(
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
                        
                        try {
                            val categoria = when (nombreCategoria) {
                                "Comida" -> "comida"
                                "Transporte" -> "transporte"
                                "Varios" -> "varios"
                                else -> "varios"
                            }
                            
                            val result = GastoService.registrarGasto(
                                descripcion = descripcion.ifBlank { "Sin descripción" },
                                monto = valor,
                                categoria = categoria
                            )
                            
                            result.fold(
                                onSuccess = { gastoResponse ->
                                    Log.d("HomeScreen", "Gasto registrado exitosamente: ${gastoResponse.id}")
                                    showSuccessMessage = true
                                    valor = ""
                                    descripcion = ""
                                    // Agregar el nuevo gasto a la lista
                                    gastos = gastos + GastoItem(
                                        descripcion = gastoResponse.descripcion,
                                        valor = gastoResponse.monto.toString()
                                    )
                                },
                                onFailure = { exception ->
                                    Log.e("HomeScreen", "Error al registrar gasto", exception)
                                    showErrorMessage = "Error al registrar gasto: ${exception.message}"
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Excepción al registrar gasto", e)
                            showErrorMessage = "Error inesperado: ${e.message}"
                        } finally {
                            isLoading = false
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
                Text("GUARDAR", color = MaterialTheme.colors.onSurface)
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
                    text = "¡Gasto registrado exitosamente!",
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
        GastosComidaGrid(gastos)
    }
}

// Modelo de dato para la tabla
data class GastoItem(val descripcion: String, val valor: String)

@Composable
fun GastosComidaGrid(gastos: List<GastoItem>) {
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
                            IconButton(onClick = { /* Editar */ }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { /* Eliminar */ }) {
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