package com.example.moneymanagerg5.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.moneymanagerg5.*

@Composable
fun NotificationsScreen() {
    val notifications by NotificationService.notifications.collectAsState()
    var selectedNotification by remember { mutableStateOf<BackendNotification?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Respuestas del Backend",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${notifications.size} notificaciones • ${NotificationService.getUnreadCount()} sin leer",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Row {
                // Botón para generar notificaciones de ejemplo (solo para pruebas)
                if (notifications.isEmpty()) {
                    Button(
                        onClick = { NotificationDemoHelper.generarNotificacionesDeEjemplo() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Generar Ejemplos")
                    }
                }
                
                IconButton(onClick = { NotificationService.markAllAsRead() }) {
                    Icon(Icons.Default.Done, "Marcar todo como leído")
                }
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.Clear, "Limpiar todo")
                }
            }
        }
        
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay notificaciones",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Las respuestas del backend aparecerán aquí",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = { 
                            selectedNotification = notification
                            if (!notification.leida) {
                                NotificationService.markAsRead(notification.id)
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Dialog para mostrar detalles completos
    selectedNotification?.let { notification ->
        NotificationDetailDialog(
            notification = notification,
            onDismiss = { selectedNotification = null }
        )
    }
    
    // Dialog de confirmación para limpiar
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpiar notificaciones") },
            text = { Text("¿Estás seguro de que quieres eliminar todas las notificaciones?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        NotificationService.clearAllNotifications()
                        showClearDialog = false
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun NotificationCard(
    notification: BackendNotification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .alpha(if (notification.leida) 0.7f else 1f),
        elevation = if (notification.leida) 2.dp else 4.dp,
        backgroundColor = if (notification.leida) Color.White else Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = getIconForType(notification.tipo),
                contentDescription = null,
                tint = getColorForType(notification.tipo),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.titulo,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.leida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colors.primary)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.mensaje,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = notification.timestamp,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun NotificationDetailDialog(
    notification: BackendNotification,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalles de Respuesta",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Cerrar")
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Contenido scrolleable
                LazyColumn {
                    item {
                        // Información básica
                        DetailRow("Tipo", notification.tipo.name)
                        DetailRow("Timestamp", notification.timestamp)
                        DetailRow("Título", notification.titulo)
                        DetailRow("Mensaje", notification.mensaje)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Respuesta completa del backend
                        Text(
                            text = "Respuesta Completa del Backend:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFFF5F5F5)
                        ) {
                            Text(
                                text = notification.respuestaCompleta,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp
        )
    }
}

fun getIconForType(tipo: TipoNotificacion): ImageVector {
    return when (tipo) {
        TipoNotificacion.VERIFICACION_CATEGORIA -> Icons.Default.Star
        TipoNotificacion.CREACION_GASTO -> Icons.Default.AddCircle
        TipoNotificacion.GASTO_NORMAL -> Icons.Default.ShoppingCart
        TipoNotificacion.EDITAR_GASTO -> Icons.Default.Edit
        TipoNotificacion.ELIMINAR_GASTO -> Icons.Default.Delete
        TipoNotificacion.ERROR -> Icons.Default.Warning
    }
}

fun getColorForType(tipo: TipoNotificacion): Color {
    return when (tipo) {
        TipoNotificacion.VERIFICACION_CATEGORIA -> Color(0xFF2196F3) // Azul
        TipoNotificacion.CREACION_GASTO -> Color(0xFF4CAF50) // Verde
        TipoNotificacion.GASTO_NORMAL -> Color(0xFFFF9800) // Naranja
        TipoNotificacion.EDITAR_GASTO -> Color(0xFF9C27B0) // Púrpura
        TipoNotificacion.ELIMINAR_GASTO -> Color(0xFF757575) // Gris
        TipoNotificacion.ERROR -> Color(0xFFF44336) // Rojo
    }
} 