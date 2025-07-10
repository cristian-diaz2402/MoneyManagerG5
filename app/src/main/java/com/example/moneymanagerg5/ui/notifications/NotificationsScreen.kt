package com.example.moneymanagerg5.ui.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.moneymanagerg5.ui.home.GastoItem

@Composable
fun NotificationsScreen() {
    val gastosTransporte = emptyList<GastoItem>()

    // Aquí puedes pasar la lista vacía a tu composable si lo necesitas
    // GastoFormWithGridAjustes(
    //     nombreCategoria = "Transporte predefinido",
    //     subtitulo = "Configura tus transportes más usados",
    //     gastos = gastosTransporte
    // )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Pantalla de ajustes", fontSize = 24.sp)
    }
} 