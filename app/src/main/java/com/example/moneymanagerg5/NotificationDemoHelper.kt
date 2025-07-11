package com.example.moneymanagerg5

/**
 * Funciones de utilidad para demostrar el sistema de notificaciones
 * Puedes usar estas funciones para probar el sistema de notificaciones
 */
object NotificationDemoHelper {
    
    /**
     * Genera notificaciones de ejemplo para probar el sistema
     */
    fun generarNotificacionesDeEjemplo() {
        // Ejemplo de verificación ML que coincide
        NotificationService.addNotification(
            tipo = TipoNotificacion.VERIFICACION_CATEGORIA,
            titulo = "Verificación ML - COMIDA",
            mensaje = "✅ La categoría seleccionada es apropiada",
            respuestaCompleta = """
                {
                  "exito": true,
                  "categoria_original": "COMIDA",
                  "recomendacion": {
                    "categoria_sugerida": "COMIDA",
                    "coincide": true,
                    "confianza": 0.95,
                    "mensaje": "✅ La categoría seleccionada es apropiada"
                  }
                }
            """.trimIndent()
        )
        
        // Ejemplo de verificación ML que NO coincide
        NotificationService.addNotification(
            tipo = TipoNotificacion.VERIFICACION_CATEGORIA,
            titulo = "Verificación ML - VARIOS",
            mensaje = "💡 Sugerencia: Considera cambiar de 'varios' a 'comida'",
            respuestaCompleta = """
                {
                  "exito": true,
                  "categoria_original": "VARIOS",
                  "recomendacion": {
                    "categoria_sugerida": "COMIDA",
                    "coincide": false,
                    "confianza": 0.87,
                    "mensaje": "💡 Sugerencia: Considera cambiar de 'varios' a 'comida'"
                  }
                }
            """.trimIndent()
        )
        
        // Ejemplo de gasto creado con decisión
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
                  "usuario_id": 1,
                  "fecha": "2025-07-11T14:30:00",
                  "created_at": "2025-07-11T14:30:00.123Z",
                  "updated_at": null,
                  "confianza_categoria": 0.95
                }
            """.trimIndent()
        )
        
        // Ejemplo de gasto normal
        NotificationService.addNotification(
            tipo = TipoNotificacion.GASTO_NORMAL,
            titulo = "Gasto Registrado #124",
            mensaje = "Gasto registrado: Gasolina - $45.00",
            respuestaCompleta = """
                {
                  "descripcion": "Gasolina",
                  "monto": 45.0,
                  "categoria": "TRANSPORTE",
                  "id": 124,
                  "usuario_id": 1,
                  "fecha": "2025-07-11T15:00:00",
                  "dia_semana": 4,
                  "hora_gasto": 15,
                  "es_fin_semana": false,
                  "patron_temporal": "TARDE",
                  "frecuencia_descripcion": 1,
                  "es_recurrente": false,
                  "confianza_categoria": 0.98,
                  "created_at": "2025-07-11T15:00:00.456Z",
                  "updated_at": "2025-07-11T15:00:00.456Z"
                }
            """.trimIndent()
        )
        
        // Ejemplo de error
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
