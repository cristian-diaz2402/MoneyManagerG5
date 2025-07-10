package com.example.moneymanagerg5.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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

    // Datos hardcodeados para cada categoría
    val gastosComida = listOf(
        GastoItem("Varios", "40"),
        GastoItem("Dyspaly", "20"),
        GastoItem("Baile", "13"),
        GastoItem("Huevos", "4"),
        GastoItem("Papas y psn", "2"),
        GastoItem("Abuelita", "30")
    )
    val gastosTransporte = listOf(
        GastoItem("Bus", "10"),
        GastoItem("Taxi", "25"),
        GastoItem("Metro", "8")
    )
    val gastosVarios = listOf(
        GastoItem("Regalo", "50"),
        GastoItem("Cine", "15")
    )

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
            0 -> GastoFormWithGrid(nombreCategoria = "Comida", gastos = gastosComida)
            1 -> GastoFormWithGrid(nombreCategoria = "Transporte", gastos = gastosTransporte)
            2 -> GastoFormWithGrid(nombreCategoria = "Varios", gastos = gastosVarios)
        }
    }
}

@Composable
fun GastoFormWithGrid(nombreCategoria: String, gastos: List<GastoItem>) {
    var valor by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
        // Filas de datos
        gastos.forEach { gasto ->
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