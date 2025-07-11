package com.example.moneymanagerg5

/**
 * Funciones de utilidad para demostrar el sistema de notificaciones
 * Puedes usar estas funciones para probar el sistema de notificaciones
 */
object NotificationDemoHelper {
    
    /**
     * Genera notificaciones de ejemplo para probar el sistema
     * Solo incluye los endpoints que están activos en el backend
     */
    fun generarNotificacionesDeEjemplo() {
        // Ejemplo 1: Verificación ML que coincide
        NotificationService.addNotification(
            tipo = TipoNotificacion.VERIFICACION_CATEGORIA,
            titulo = "Verificación ML - COMIDA",
            mensaje = "✅ La categoría seleccionada es apropiada",
            respuestaCompleta = """
                {
                  "recomendacion": {
                    "categoria_sugerida": "COMIDA",
                    "categoria_original": "COMIDA",
                    "coincide": true,
                    "mensaje": "✅ La categoría seleccionada es apropiada"
                  }
                }
            """.trimIndent()
        )
        
        // Ejemplo 2: Verificación ML que NO coincide
        NotificationService.addNotification(
            tipo = TipoNotificacion.VERIFICACION_CATEGORIA,
            titulo = "Verificación ML - VARIOS",
            mensaje = "💡 Sugerencia: Considera cambiar de 'varios' a 'comida'",
            respuestaCompleta = """
                {
                  "recomendacion": {
                    "categoria_sugerida": "COMIDA",
                    "categoria_original": "VARIOS",
                    "coincide": false,
                    "mensaje": "💡 Sugerencia: Considera cambiar de 'varios' a 'comida'"
                  }
                }
            """.trimIndent()
        )
        
        // Ejemplo 3: Gasto creado con decisión (Aceptó sugerencia)
        NotificationService.addNotification(
            tipo = TipoNotificacion.CREACION_GASTO,
            titulo = "Gasto Creado con ML #123",
            mensaje = "Aceptó sugerencia ML: COMIDA - $25.50",
            respuestaCompleta = """
                {
                  "id": 123,
                  "descripcion": "Almuerzo McDonald's",
                  "monto": 25.50,
                  "categoria": "COMIDA",
                  "fecha": "2025-07-11T14:30:00",
                  "usuario_id": 1
                }
            """.trimIndent()
        )
        
        // Ejemplo 4: Gasto creado con decisión (Rechazó sugerencia)
        NotificationService.addNotification(
            tipo = TipoNotificacion.CREACION_GASTO,
            titulo = "Gasto Creado con ML #124",
            mensaje = "Rechazó sugerencia ML: VARIOS - $15.00",
            respuestaCompleta = """
                {
                  "id": 124,
                  "descripcion": "Compra en tienda",
                  "monto": 15.00,
                  "categoria": "VARIOS",
                  "fecha": "2025-07-11T15:00:00",
                  "usuario_id": 1
                }
            """.trimIndent()
        )
        
        // Ejemplo 5: Error en verificación ML
        NotificationService.addNotification(
            tipo = TipoNotificacion.ERROR,
            titulo = "Error en Verificación ML",
            mensaje = "Error 401: Token de autenticación inválido",
            respuestaCompleta = """
                {
                  "error": "UNAUTHORIZED",
                  "message": "Token de autenticación inválido o expirado",
                  "code": 401,
                  "timestamp": "2025-07-11T15:30:00.789Z"
                }
            """.trimIndent()
        )
        
        // Ejemplo 6: Error al crear gasto
        NotificationService.addNotification(
            tipo = TipoNotificacion.ERROR,
            titulo = "Error al Crear Gasto",
            mensaje = "Error 400: Bad Request",
            respuestaCompleta = """
                {
                  "error": "VALIDATION_ERROR",
                  "message": "El campo 'descripcion' es requerido",
                  "code": 400,
                  "timestamp": "2025-07-11T15:30:00.789Z"
                }
            """.trimIndent()
        )
    }
    
    /**
     * Limpia todas las notificaciones de ejemplo
     */
    fun limpiarNotificaciones() {
        NotificationService.clearAllNotifications()
    }
}
