package com.example.moneymanagerg5

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

data class BackendNotification(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val respuestaCompleta: String,
    val leida: Boolean = false
)

enum class TipoNotificacion {
    VERIFICACION_CATEGORIA,  // Para /ml/verificar-categoria
    CREACION_GASTO,         // Para /gastos/crear-con-decision
    GASTO_NORMAL,           // Para /gastos (registro normal)
    EDITAR_GASTO,           // Para PUT /gastos/{id}
    ELIMINAR_GASTO,         // Para DELETE /gastos/{id}
    ERROR,                   // Para errores en las API calls
    SUCCESS
}

object NotificationService {
    private const val PREFS_NAME = "NotificationsPrefs"
    private const val MAX_NOTIFICATIONS = 50
    
    private lateinit var prefs: SharedPreferences
    private val _notifications = MutableStateFlow<List<BackendNotification>>(emptyList())
    val notifications: StateFlow<List<BackendNotification>> = _notifications.asStateFlow()
    
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadNotifications()
    }
    
    fun addNotification(
        tipo: TipoNotificacion,
        titulo: String,
        mensaje: String,
        respuestaCompleta: String
    ) {
        val nuevaNotificacion = BackendNotification(
            tipo = tipo,
            titulo = titulo,
            mensaje = mensaje,
            respuestaCompleta = respuestaCompleta
        )
        
        val listaActual = _notifications.value.toMutableList()
        listaActual.add(0, nuevaNotificacion) // Agregar al inicio
        
        // Mantener solo las últimas MAX_NOTIFICATIONS notificaciones
        if (listaActual.size > MAX_NOTIFICATIONS) {
            listaActual.removeAt(listaActual.size - 1)
        }
        
        _notifications.value = listaActual
        saveNotifications()
    }
    
    fun markAsRead(notificationId: String) {
        val listaActualizada = _notifications.value.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(leida = true)
            } else {
                notification
            }
        }
        _notifications.value = listaActualizada
        saveNotifications()
    }
    
    fun markAllAsRead() {
        val listaActualizada = _notifications.value.map { notification ->
            notification.copy(leida = true)
        }
        _notifications.value = listaActualizada
        saveNotifications()
    }
    
    fun clearAllNotifications() {
        _notifications.value = emptyList()
        prefs.edit().clear().apply()
    }
    
    fun getUnreadCount(): Int {
        return _notifications.value.count { !it.leida }
    }
    
    private fun saveNotifications() {
        // Guardar las notificaciones en SharedPreferences
        val notificationsJson = notifications.value.take(MAX_NOTIFICATIONS)
        // Por simplicidad, solo guardamos los últimos 10 en persistencia
        val editor = prefs.edit()
        editor.putInt("count", notificationsJson.size.coerceAtMost(10))
        
        notificationsJson.take(10).forEachIndexed { index, notification ->
            editor.putString("notification_$index", 
                "${notification.id}|${notification.timestamp}|${notification.tipo.name}|" +
                "${notification.titulo}|${notification.mensaje}|${notification.respuestaCompleta}|${notification.leida}")
        }
        editor.apply()
    }
    
    private fun loadNotifications() {
        val count = prefs.getInt("count", 0)
        val notificationsList = mutableListOf<BackendNotification>()
        
        for (i in 0 until count) {
            val notificationData = prefs.getString("notification_$i", null)
            if (notificationData != null) {
                val parts = notificationData.split("|")
                if (parts.size >= 7) {
                    try {
                        val notification = BackendNotification(
                            id = parts[0],
                            timestamp = parts[1],
                            tipo = TipoNotificacion.valueOf(parts[2]),
                            titulo = parts[3],
                            mensaje = parts[4],
                            respuestaCompleta = parts[5],
                            leida = parts[6].toBoolean()
                        )
                        notificationsList.add(notification)
                    } catch (e: Exception) {
                        // Ignorar notificaciones corruptas
                    }
                }
            }
        }
        
        _notifications.value = notificationsList
    }
}
