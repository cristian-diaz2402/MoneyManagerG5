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
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🔔 Notificaciones",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = "${notifications.size} notificaciones • ${NotificationService.getUnreadCount()} sin leer",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para generar notificaciones de ejemplo (solo para pruebas) - OCULTO
                // if (notifications.isEmpty()) {
                //     Button(
                //         onClick = { NotificationDemoHelper.generarNotificacionesDeEjemplo() },
                //         colors = ButtonDefaults.buttonColors(
                //             backgroundColor = MaterialTheme.colors.secondary
                //         )
                //     ) {
                //         Text("📝 Generar Ejemplos")
                //     }
                // }
                
                if (notifications.isNotEmpty()) {
                    Button(
                        onClick = { NotificationService.markAllAsRead() },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Default.Done, "Marcar todo como leído", 
                             tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Leído", color = Color.White)
                    }
                    
                    Button(
                        onClick = { showClearDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(Icons.Default.Clear, "Limpiar todo", 
                             tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar", color = Color.White)
                    }
                }
            }
        }
        
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📱",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No hay notificaciones",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Las respuestas del backend aparecerán aquí\ncuando realices operaciones",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
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
            .alpha(if (notification.leida) 0.8f else 1f),
        elevation = if (notification.leida) 4.dp else 8.dp,
        backgroundColor = if (notification.leida) Color.White else Color(0xFFF8F9FA),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono con fondo circular colorido
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(getColorForType(notification.tipo).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForType(notification.tipo),
                    contentDescription = null,
                    tint = getColorForType(notification.tipo),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = if (notification.leida) Color.Gray else Color.Black
                    )
                    if (!notification.leida) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(getColorForType(notification.tipo))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = notification.mensaje,
                    fontSize = 14.sp,
                    color = if (notification.leida) Color.Gray else Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.timestamp,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Badge del tipo de notificación
                    Card(
                        backgroundColor = getColorForType(notification.tipo).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        elevation = 0.dp
                    ) {
                        Text(
                            text = getTipoDisplayName(notification.tipo),
                            fontSize = 10.sp,
                            color = getColorForType(notification.tipo),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
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
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header con icono y título
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(getColorForType(notification.tipo).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForType(notification.tipo),
                                contentDescription = null,
                                tint = getColorForType(notification.tipo),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Detalles de Respuesta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Cerrar")
                    }
                }
                
                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.Gray.copy(alpha = 0.3f)
                )
                
                // Contenido scrolleable
                LazyColumn {
                    item {
                        // Información básica con mejor diseño
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFFF8F9FA),
                            shape = RoundedCornerShape(12.dp),
                            elevation = 2.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                DetailRowStyled("📋 Tipo", getTipoDisplayName(notification.tipo), getColorForType(notification.tipo))
                                DetailRowStyled("⏰ Timestamp", notification.timestamp, Color.Gray)
                                DetailRowStyled("📌 Título", notification.titulo, Color.Black)
                                DetailRowStyled("💬 Mensaje", notification.mensaje, Color.DarkGray)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Respuesta completa del backend
                        Text(
                            text = "🔧 Respuesta Completa del Backend:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF1976D2)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFF2D2D2D),
                            shape = RoundedCornerShape(12.dp),
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "JSON Response",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Card(
                                        backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp),
                                        elevation = 0.dp
                                    ) {
                                        Text(
                                            text = "API",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = notification.respuestaCompleta,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }
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

@Composable
fun DetailRowStyled(label: String, value: String, valueColor: Color = Color.Black) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = FontWeight.Medium
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
        TipoNotificacion.SUCCESS -> Icons.Default.CheckCircle
    }
}

fun getColorForType(tipo: TipoNotificacion): Color {
    return when (tipo) {
        TipoNotificacion.VERIFICACION_CATEGORIA -> Color(0xFF2196F3) // Azul - ML
        TipoNotificacion.CREACION_GASTO -> Color(0xFF4CAF50) // Verde - Gasto creado exitosamente
        TipoNotificacion.GASTO_NORMAL -> Color(0xFFFF9800) // Naranja - Gasto normal
        TipoNotificacion.EDITAR_GASTO -> Color(0xFF9C27B0) // Púrpura - Editar
        TipoNotificacion.ELIMINAR_GASTO -> Color(0xFF757575) // Gris - Eliminar
        TipoNotificacion.ERROR -> Color(0xFFF44336) // Rojo - Errores
        TipoNotificacion.SUCCESS -> Color(0xFF4CAF50) // Verde - Éxito
    }
}

fun getTipoDisplayName(tipo: TipoNotificacion): String {
    return when (tipo) {
        TipoNotificacion.VERIFICACION_CATEGORIA -> "ML"
        TipoNotificacion.CREACION_GASTO -> "NUEVO"
        TipoNotificacion.GASTO_NORMAL -> "GASTO"
        TipoNotificacion.EDITAR_GASTO -> "EDITAR"
        TipoNotificacion.ELIMINAR_GASTO -> "ELIMINAR"
        TipoNotificacion.ERROR -> "ERROR"
        TipoNotificacion.SUCCESS -> "ÉXITO"
    }
} 