# Implementación del Nuevo Endpoint de Actualización de Perfil

## Resumen

Se ha implementado el nuevo endpoint `POST /auth/update-profile` para modificar los datos del usuario autenticado en el frontend de la aplicación Android.

## Cambios Realizados

### 1. AuthService.kt
- **Agregado**: Nuevo método `actualizarPerfilPost()` en la interfaz `AuthApi`
- **Endpoint**: `POST /auth/update-profile`
- **Parámetros**: 
  - `@Header("Authorization")`: Token de autenticación
  - `@Body request: ActualizarPerfilRequest`: Datos a actualizar

### 2. GastoService.kt
- **Agregado**: Nuevo método `actualizarPerfilPost()` que utiliza el nuevo endpoint
- **Funcionalidad**:
  - Valida el token de autenticación
  - Envía la solicitud al nuevo endpoint
  - Actualiza los datos guardados localmente
  - Crea notificaciones de éxito/error
  - Maneja errores específicos (400, 422, etc.)

### 3. ProfileViewModel.kt
- **Modificado**: El método `actualizarDatos()` ahora usa `GastoService.actualizarPerfilPost()`
- **Mantiene**: Todas las validaciones existentes
- **Mejora**: Usa el nuevo endpoint más robusto

## Estructura del Request

```kotlin
data class ActualizarPerfilRequest(
    val nombre: String? = null,
    val telefono: String? = null,
    val presupuesto: Double? = null,
    val periodo_presupuesto: String? = null
)
```

## Características del Nuevo Endpoint

### Ventajas sobre el endpoint anterior:
1. **Método POST**: Más apropiado para operaciones de actualización
2. **Mejor manejo de errores**: Respuestas más específicas
3. **Validación mejorada**: Solo modifica campos enviados
4. **Compatibilidad**: Mantiene el endpoint anterior como respaldo

### Validaciones Implementadas:
- Nombre: mínimo 2 caracteres
- Teléfono: exactamente 10 dígitos (si se proporciona)
- Presupuesto: número positivo o cero
- Período: debe ser DIARIO, SEMANAL o MENSUAL

## Flujo de Actualización

1. **Usuario ingresa datos** en ProfileScreen
2. **ProfileViewModel valida** los datos localmente
3. **GastoService.actualizarPerfilPost()** envía al nuevo endpoint
4. **Backend procesa** y actualiza solo los campos enviados
5. **Respuesta exitosa**: Actualiza datos locales y muestra notificación
6. **Respuesta de error**: Muestra mensaje específico al usuario

## Notificaciones

El sistema crea notificaciones automáticas para:
- ✅ **Éxito**: "Perfil Actualizado (Nuevo Endpoint)"
- ❌ **Error de validación**: "Datos inválidos"
- ❌ **Error de red**: "No se pudo conectar al servidor"

## Compatibilidad

- **Endpoint anterior**: `/auth/perfil` (PATCH) - Mantenido para compatibilidad
- **Nuevo endpoint**: `/auth/update-profile` (POST) - Implementado y en uso
- **Método anterior**: `GastoService.actualizarPerfil()` - Disponible como respaldo
- **Nuevo método**: `GastoService.actualizarPerfilPost()` - En uso activo

## Testing

Para probar la implementación:

1. **Abrir la app** y navegar a la pantalla de perfil
2. **Modificar algún campo** (nombre, teléfono, presupuesto, período)
3. **Presionar "GUARDAR"**
4. **Verificar** que se muestre el mensaje de éxito
5. **Revisar notificaciones** para confirmar la operación

## Logs de Debug

El sistema registra logs detallados:
- `Log.d("GastoService", "Perfil actualizado exitosamente con nuevo endpoint")`
- `Log.e("GastoService", "Error al actualizar perfil con nuevo endpoint: $errorBody")`

## Próximos Pasos

1. **Monitorear** el uso del nuevo endpoint en producción
2. **Evaluar** si se puede deprecar el endpoint anterior
3. **Considerar** agregar más validaciones según necesidades del negocio
4. **Implementar** tests unitarios para el nuevo método 