package com.example.moneymanagerg5.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.moneymanagerg5.AuthService
import com.example.moneymanagerg5.LoginRequest
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import org.json.JSONObject
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoginScreen(
    navController: NavController? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var loginExitoso by remember { mutableStateOf(false) }
    var errorBannerMessage by remember { mutableStateOf<String?>(null) }

    if (loginExitoso) {
        LaunchedEffect(Unit) {
            navController?.navigate("home")
        }
    }

    Scaffold(
        topBar = {
            if (errorBannerMessage != null) {
                // Cerrar automáticamente después de 2 segundos
                LaunchedEffect(errorBannerMessage) {
                    delay(2000)
                    errorBannerMessage = null
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Card(
                        backgroundColor = MaterialTheme.colors.error,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorBannerMessage ?: "",
                                color = MaterialTheme.colors.onError,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Iniciar Sesión", style = MaterialTheme.typography.h5)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        Log.d("LoginScreen", "Intentando iniciar sesión con email: $email")
                        try {
                            val response = AuthService.api.login(LoginRequest(email, password))
                            Log.d("LoginScreen", "Respuesta HTTP: ${response.code()} ${response.message()}")
                            if (response.isSuccessful) {
                                val body = response.body()
                                Log.d("LoginScreen", "Cuerpo de respuesta: $body")
                                if (body?.access_token != null) {
                                    Log.d("LoginScreen", "Login exitoso, token: ${body.access_token}")
                                    loginExitoso = true
                                } else {
                                    Log.w("LoginScreen", "Login fallido, detalle: ${body?.detail}")
                                    errorBannerMessage = body?.detail ?: "Error desconocido"
                                }
                            } else {
                                val errorBody = response.errorBody()?.string()
                                Log.w("LoginScreen", "Error de autenticación: $errorBody")
                                val errorMsg = try {
                                    org.json.JSONObject(errorBody ?: "{}").optString("detail", "Email o contraseña incorrectos")
                                } catch (e: Exception) {
                                    "Email o contraseña incorrectos"
                                }
                                errorBannerMessage = errorMsg
                            }
                        } catch (e: Exception) {
                            Log.e("LoginScreen", "Excepción al iniciar sesión", e)
                            errorBannerMessage = "Error de red: ${e.localizedMessage}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }
        }
    }
} 