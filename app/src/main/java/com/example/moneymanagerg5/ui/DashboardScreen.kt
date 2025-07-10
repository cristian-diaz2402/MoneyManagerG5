package com.example.moneymanagerg5.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagerg5.ui.home.GastoItem

@Composable
fun DashboardScreen() {
    // Eliminar datos hardcodeados
    // val presupuesto = 10.0
    // val gastos = listOf(
    //     GastoItem(1, "Varios", "40"),
    //     ...
    // )
    // val totalGastos = gastos.sumOf { it.valor.toDoubleOrNull() ?: 0.0 }

    // Dejar variables listas para datos dinámicos
    val presupuesto = 0.0
    val gastos = emptyList<GastoItem>()
    val totalGastos = 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tus Estadisticas",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B7685),
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Tu presupuesto Diario",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Box(
            modifier = Modifier
                .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))
                .padding(horizontal = 32.dp, vertical = 8.dp)
        ) {
            Text(text = presupuesto.toInt().toString(), fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tus gastos actuales",
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Box(
            modifier = Modifier
                .background(Color(0xFFF2F2F2), RoundedCornerShape(8.dp))
                .padding(horizontal = 32.dp, vertical = 8.dp)
        ) {
            Text(text = String.format("%.2f", totalGastos), fontSize = 24.sp, color = Color.Red)
        }
        if (totalGastos > presupuesto) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "!! TE PASASTE DE TU PRESUPUESTO !!",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        EstadisticasGastosGrid(gastos)
    }
}

@Composable
fun EstadisticasGastosGrid(gastos: List<GastoItem>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(
                text = "Descripción",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Valor",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        Divider()
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
            }
            Divider()
        }
    }
} 