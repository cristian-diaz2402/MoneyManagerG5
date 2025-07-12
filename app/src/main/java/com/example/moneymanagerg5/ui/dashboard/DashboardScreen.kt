package com.example.moneymanagerg5.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneymanagerg5.GastoResponse
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Calcular estadísticas
    val totalGastos = uiState.gastos.sumOf { it.monto }
    val gastosPorCategoria = uiState.gastos.groupBy { it.categoria }
        .mapValues { (_, gastosCategoria) -> gastosCategoria.sumOf { it.monto } }
    val promedioGasto = if (uiState.gastos.isNotEmpty()) totalGastos / uiState.gastos.size else 0.0
    
    // Categorías disponibles
    val categorias = listOf("comida", "transporte", "varios")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header con título y controles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📊 Estadísticas",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = if (uiState.categoriaSeleccionada != null) 
                        "${uiState.gastos.size} gastos en ${uiState.categoriaSeleccionada?.uppercase()}"
                    else 
                        "${uiState.gastos.size} gastos registrados",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Row {
                if (uiState.categoriaSeleccionada != null) {
                    IconButton(onClick = { viewModel.limpiarFiltro() }) {
                        Icon(
                            Icons.Default.Clear, 
                            "Limpiar filtro",
                            tint = MaterialTheme.colors.secondary
                        )
                    }
                }
                IconButton(onClick = { viewModel.recargarDatos() }) {
                    Icon(
                        Icons.Default.Refresh, 
                        "Recargar",
                        tint = MaterialTheme.colors.primary
                    )
                }
            }
        }
        
        // Filtros por categoría
        if (uiState.categoriaSeleccionada == null) {
            Text(
                text = "🏷️ Filtrar por Categoría",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(categorias) { categoria ->
                    FilterChip(
                        categoria = categoria,
                        onClick = { viewModel.filtrarPorCategoria(categoria) }
                    )
                }
            }
        } else {
            // Mostrar filtro activo
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏷️ Filtrando por: ${uiState.categoriaSeleccionada?.uppercase()}",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colors.primary
                    )
                    TextButton(onClick = { viewModel.limpiarFiltro() }) {
                        Text("Ver Todos")
                    }
                }
            }
        }
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                backgroundColor = MaterialTheme.colors.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colors.onError,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Estadísticas resumidas
        if (uiState.gastos.isNotEmpty()) {
            // Tarjeta de presupuesto diario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "💰 Tu Presupuesto Diario",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$${String.format("%.2f", uiState.presupuestoDiario)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalGastos > uiState.presupuestoDiario) Color(0xFFE53E3E) else Color(0xFF38A169)
                    )
                    if (uiState.userData?.periodo_presupuesto != null) {
                        Text(
                            text = "Basado en presupuesto ${uiState.userData?.periodo_presupuesto?.lowercase()}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Alerta de presupuesto excedido
            if (totalGastos > uiState.presupuestoDiario && uiState.presupuestoDiario > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color(0xFFFFEBEE), // Fondo rojo claro
                    elevation = 6.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "🚨",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "¡PRESUPUESTO EXCEDIDO!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                        
                        Text(
                            text = "Has gastado $${String.format("%.2f", totalGastos)} de tu presupuesto diario de $${String.format("%.2f", uiState.presupuestoDiario)}",
                            fontSize = 14.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val exceso = totalGastos - uiState.presupuestoDiario
                        Text(
                            text = "Exceso: $${String.format("%.2f", exceso)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 Considera revisar tus gastos para el resto del día",
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            } else if (uiState.presupuestoDiario > 0 && totalGastos > (uiState.presupuestoDiario * 0.8)) {
                // Advertencia cuando están cerca del límite (80% del presupuesto)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color(0xFFFFF3E0), // Fondo naranja claro
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "¡CUIDADO CON TU PRESUPUESTO!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        
                        val porcentajeUsado = (totalGastos / uiState.presupuestoDiario * 100).toInt()
                        Text(
                            text = "Has usado el $porcentajeUsado% de tu presupuesto diario",
                            fontSize = 14.sp,
                            color = Color(0xFF795548),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        val restante = uiState.presupuestoDiario - totalGastos
                        Text(
                            text = "Te quedan: $${String.format("%.2f", restante)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💰 Piensa dos veces antes de tu próximo gasto",
                            fontSize = 12.sp,
                            color = Color(0xFF8D6E63),
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaCard(
                    titulo = "Total Gastado",
                    valor = "$${String.format("%.2f", totalGastos)}",
                    color = Color(0xFFE53E3E)
                )
                EstadisticaCard(
                    titulo = "Promedio",
                    valor = "$${String.format("%.2f", promedioGasto)}",
                    color = Color(0xFF3182CE)
                )
                EstadisticaCard(
                    titulo = "Cantidad",
                    valor = uiState.gastos.size.toString(),
                    color = Color(0xFF38A169)
                )
            }
            
            // Gastos por categoría (solo mostrar si no hay filtro activo)
            if (gastosPorCategoria.isNotEmpty() && uiState.categoriaSeleccionada == null) {
                Text(
                    text = "💰 Gastos por Categoría",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                gastosPorCategoria.entries.sortedByDescending { it.value }.forEach { (categoria, total) ->
                    GastoCategoriaItem(
                        categoria = categoria, 
                        total = total,
                        onClick = { viewModel.filtrarPorCategoria(categoria) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Lista de gastos
            Text(
                text = "📝 Historial de Gastos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn {
                items(uiState.gastos.sortedByDescending { it.fecha }) { gasto ->
                    GastoItem(gasto = gasto)
                }
            }
        } else if (!uiState.isLoading && uiState.errorMessage == null) {
            // Mostrar presupuesto diario incluso cuando no hay gastos
            if (uiState.presupuestoDiario > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = MaterialTheme.colors.surface,
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💰 Tu Presupuesto Diario",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$${String.format("%.2f", uiState.presupuestoDiario)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38A169)
                        )
                        if (uiState.userData?.periodo_presupuesto != null) {
                            Text(
                                text = "Basado en presupuesto ${uiState.userData?.periodo_presupuesto?.lowercase()}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¡Todavía no has gastado nada hoy!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF38A169)
                        )
                    }
                }
                
                // Tarjeta de estado positivo cuando no hay gastos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = Color(0xFFE8F5E8), // Fondo verde claro
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "✅",
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "¡EXCELENTE CONTROL!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        
                        Text(
                            text = "No has registrado gastos hoy. Tu presupuesto diario está intacto.",
                            fontSize = 14.sp,
                            color = Color(0xFF4CAF50),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Text(
                            text = "Presupuesto disponible: $${String.format("%.2f", uiState.presupuestoDiario)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💪 ¡Sigue así y alcanza tus metas financieras!",
                            fontSize = 12.sp,
                            color = Color(0xFF558B2F),
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📊",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.categoriaSeleccionada != null) 
                            "No hay gastos en ${uiState.categoriaSeleccionada?.uppercase()}"
                        else 
                            "No hay gastos registrados",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.categoriaSeleccionada != null) 
                            "Prueba con otra categoría o elimina el filtro"
                        else 
                            "Comienza registrando gastos en la pestaña de Inicio",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChip(categoria: String, onClick: () -> Unit) {
    val color = getCategoriaColor(categoria)
    
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = color.copy(alpha = 0.1f),
        elevation = 2.dp
    ) {
        Text(
            text = categoria.uppercase(),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EstadisticaCard(titulo: String, valor: String, color: Color) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = valor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = titulo,
                fontSize = 10.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun GastoCategoriaItem(categoria: String, total: Double, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = categoria.uppercase(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ver detalles →",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "$${String.format("%.2f", total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFE53E3E)
            )
        }
    }
}

@Composable
fun GastoItem(gasto: GastoResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = gasto.descripcion ?: "Sin descripción",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Text(
                        text = gasto.categoria.uppercase(),
                        fontSize = 12.sp,
                        color = getCategoriaColor(gasto.categoria),
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.2f", gasto.monto)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFE53E3E)
                    )
                    Text(
                        text = formatearFecha(gasto.fecha),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun getCategoriaColor(categoria: String): Color {
    return when (categoria.lowercase()) {
        "comida" -> Color(0xFFFF9800)
        "transporte" -> Color(0xFF2196F3)
        "varios" -> Color(0xFF9C27B0)
        else -> Color.Gray
    }
}

fun formatearFecha(fechaISO: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = inputFormat.parse(fechaISO)
        outputFormat.format(fecha ?: Date())
    } catch (e: Exception) {
        fechaISO.substring(0, 10) // Fallback a solo la fecha
    }
} 