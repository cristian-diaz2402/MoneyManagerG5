package com.example.moneymanagerg5.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.moneymanagerg5.RecomendacionML

@Composable
fun CategoriaModal(
    recomendacion: RecomendacionML,
    categoriaOriginal: String,
    onAceptar: () -> Unit,
    onIgnorar: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = 8.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono o emoji
                Text(
                    text = "💡",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Título
                Text(
                    text = "Sugerencia de Categoría",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Mensaje de la recomendación
                Text(
                    text = recomendacion.mensaje,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onIgnorar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ignorar")
                    }
                    
                    Button(
                        onClick = onAceptar,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
} 