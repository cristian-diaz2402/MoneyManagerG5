package com.example.moneymanagerg5.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneymanagerg5.ui.home.GastoItem

@Composable
fun NotificationsScreen() {
    val gastosTransporte = emptyList<GastoItem>()

    GastoFormWithGridAjustes(
        nombreCategoria = "Transporte predefinido",
        subtitulo = "Configura tus transportes más usados",
        gastos = gastosTransporte
    )
}

@Composable
fun GastoFormWithGridAjustes(nombreCategoria: String, subtitulo: String, gastos: List<GastoItem>) {
    var valor by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = nombreCategoria,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = subtitulo,
            fontSize = 16.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(text = "Ingresa el valor", fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Ingresa el valor") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )
        Text(text = "Añade una descripcion", fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 8.dp))
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
            onClick = { /* Sin funcionalidad por ahora */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = true,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.onSurface.copy(alpha = 0.08f))
        ) {
            Text("GUARDAR", color = MaterialTheme.colors.onSurface)
        }
        Spacer(modifier = Modifier.height(24.dp))
        com.example.moneymanagerg5.ui.home.GastosComidaGrid(
            gastos = gastos,
            onEditar = {},
            onEliminar = {}
        )
    }
} 