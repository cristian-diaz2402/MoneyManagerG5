package com.example.moneymanagerg5.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Estados locales para los campos editables
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var periodo by remember { mutableStateOf("diario") }
    var presupuesto by remember { mutableStateOf("") }
    val periodos = listOf("diario", "semanal", "mensual")
    var expanded by remember { mutableStateOf(false) }
    
    // Actualizar los campos cuando los datos del usuario cambien
    LaunchedEffect(uiState.userData) {
        uiState.userData?.let { user ->
            nombre = user.nombre ?: ""
            correo = user.email ?: ""
            telefono = user.telefono ?: ""
            presupuesto = user.presupuesto?.toString() ?: ""
            periodo = user.periodo_presupuesto ?: "diario"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Editar Perfil",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Mostrar mensaje de error si existe
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.1f)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Mostrar mensaje de éxito si existe
        uiState.successMessage?.let { success ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = success,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Mostrar indicador de carga
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }
        
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            enabled = false // Email no editable
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            enabled = !uiState.isLoading
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Presupuesto",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Periodo", fontWeight = FontWeight.Medium, modifier = Modifier.align(Alignment.Start))
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text(periodo)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                periodos.forEach { option ->
                    DropdownMenuItem(onClick = {
                        periodo = option
                        expanded = false
                    }) {
                        Text(option)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = presupuesto,
            onValueChange = { presupuesto = it },
            label = { Text("Ingresa tu presupuesto") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            enabled = !uiState.isLoading
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { 
                viewModel.actualizarDatos(nombre, telefono, presupuesto, periodo)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("GUARDAR")
            }
        }
    }
}