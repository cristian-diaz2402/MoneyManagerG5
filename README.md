# 📊 MoneyManagerG5

![Status](https://img.shields.io/badge/status-in%20progress-yellow)
![App](https://img.shields.io/badge/app-Android-green)
![Backend](https://img.shields.io/badge/backend-FastAPI-purple)
![Frontend](https://img.shields.io/badge/frontend-Android%20(Kotlin)-green)
![DB](https://img.shields.io/badge/database-PostgreSQL-blue)
![ML](https://img.shields.io/badge/Machine%20Learning-enabled-purple)

---

## 📖 Descripción

Moneymanager G5 es una aplicación móvil que permite a los usuarios autenticados **registrar, consultar y filtrar sus gastos** en 3 categorías: Comida, Transporte y Varios con persistencia de datos mediante una base de datos PostgresSQL que se conecta un backend con **FastAPI**, autenticación JWT, la aplicación móvil fué desarrollada en **Kotlin (Jetpack Compose)** en Android Studio.  

Se incluyen endpoints para **gestionar el perfil del usuario**, obtener **estadísticas dinámicas** y filtrar gastos por **categoría**.  
Además, la app utiliza una **arquitectura MVVM**, integra **notificaciones** y proporciona una **experiencia de usuario fluida**. 

E incluye la implementacion de un modelo de **Machine Learning** de regresión lineal preentrando y exportado en formato **pkl** y publicado en **[Hugging Face Space - Haz Clic aquí para ver como funciona el modelo](https://huggingface.co/spaces/cristiandiaz2403/MiSpace)** como servicio API y backend propio que permite consumir la API de Hugging Face

**Si necesitas ayuda para replicar o implementar este proyecto no dudes en contectarme**

---

## 🚀 Endpoints Disponibles

| Endpoint | Método | Descripción | Parámetros |
|----------|--------|-------------|------------|
| `/auth/me/gastos` | GET | Retorna todos los gastos del usuario autenticado | - |
| `/auth/me/gastos?categoria={categoria}` | GET | Filtra gastos por categoría | `categoria`: comida/transporte/varios |
| `/auth/update-profile` | POST | Actualiza los datos del perfil del usuario | Body: `nombre`, `telefono`, `presupuesto`, `periodo_presupuesto` |

---

## ✅ Funcionalidades Clave

### 1. Gestión de Gastos
- **Obtener todos los gastos** del usuario autenticado  
- **Filtrado en tiempo real por categoría** (`comida`, `transporte`, `varios`)  
- **Estadísticas dinámicas** recalculadas al aplicar filtros  
- **Historial ordenado por fecha** con detalles de cada gasto  

### 2. Interfaz de Usuario
- **Pantalla de Estadísticas** con:
  - Chips interactivos para filtrar por categoría  
  - Indicador visual de filtro activo  
  - Botón para limpiar filtros  
  - Estados vacíos personalizados  
  - Totales, promedio y cantidad de gastos  
- **Pantalla de Inicio (HomeScreen)**:
  - Carga gastos con autenticación  
  - Filtrado por categoría desde la pantalla principal  

### 3. Experiencia de Usuario
- **Loading states** con feedback visual  
- **Mensajes de error claros** en fallos de red o autenticación  
- **Recarga inteligente** que mantiene filtros activos  
- **Interfaz responsive** con Jetpack Compose  

### 4. Autenticación y Seguridad
- Uso de **JWT tokens** con autorización `Bearer`  
- Validación de token antes de cada petición  
- Manejo robusto de errores de autenticación  

### 5. Perfil del Usuario
- **Nuevo endpoint** `POST /auth/update-profile`:
  - Permite modificar nombre, teléfono, presupuesto y período  
  - Validaciones:
    - Nombre mínimo 2 caracteres  
    - Teléfono de 10 dígitos  
    - Presupuesto ≥ 0  
    - Período: DIARIO, SEMANAL o MENSUAL  
  - Solo se actualizan los campos enviados  

### 6. Arquitectura MVVM
- **ViewModel reactivo** con `StateFlow`  
- **Gestión centralizada del estado** (loading, error, datos, filtro activo)  
- Separación clara entre lógica de negocio y UI  

### 7. Sistema de Notificaciones
- Registro automático de todas las operaciones  
- Notificaciones específicas para:
  - Éxitos (perfil actualizado, gastos cargados, filtros aplicados)  
  - Errores (validación, red, backend)  
- Útiles para **debugging** con respuestas completas del servidor  

---

## 🖥️ Ejemplo de Request y Response

### Obtener todos los gastos
```

GET /auth/me/gastos
Authorization: Bearer \<token\_jwt>

````

**Ejemplo de respuesta:**
```json
[
  {
    "id": 20,
    "descripcion": "almuerzo para mi novia",
    "monto": 5.0,
    "categoria": "comida",
    "usuario_id": 1,
    "fecha": "2025-07-11T15:54:57.183935"
  }
]
````

### Actualizar perfil

```
POST /auth/update-profile
Authorization: Bearer <token_jwt>
```

**Body:**

```json
{
  "nombre": "Carlos Pérez",
  "telefono": "0987654321",
  "presupuesto": 300.0,
  "periodo_presupuesto": "MENSUAL"
}
```
 ## 📎 Anexos
 
