<div align="center">
  <img src="app_icon.webp" alt="GernikApp Icon" width="150" height="150" />

  # GerniBide 🏛️

  [![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
  [![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
  [![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
  [![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

  **Aplicación Android educativa e interactiva sobre Gernika (Euskadi, España)**

  <p>Una experiencia multimedia que combina historia, cultura vasca y valores de paz a través de 5 módulos temáticos con mini-juegos y actividades interactivas.</p>
</div>

---

<p align="justify">
GernikApp es una herramienta pedagógica diseñada para fomentar el aprendizaje sobre la historia de Gernika, sus tradiciones culturales y el mensaje de paz que representa a nivel mundial. La aplicación integra contenido multimedia, gamificación y sincronización en la nube para ofrecer una experiencia educativa completa.
</p>

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Módulos](#-módulos)
- [Tecnologías](#️-tecnologías)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Sincronización de Progreso](#-sincronización-de-progreso)
- [Instalación](#-instalación)
- [Uso](#-uso)
- [Cloudinary Integration](#️-cloudinary-integration)
- [Características Especiales](#-características-especiales)
- [API Endpoints](#-api-endpoints)
- [Troubleshooting](#-troubleshooting)
- [Contribuir](#-contribuir)
- [Desarrolladores](#-desarrolladores)

## 📱 Características

- **5 módulos temáticos** con actividades interactivas
- **Integración con Google Maps** para navegación por ubicaciones históricas
- **Sistema de progreso y puntuación** sincronizado con API backend
- **Sincronización automática** de progreso entre dispositivos
- **Soporte multiidioma** (Euskera y Español)
- **Actividades multimedia**: pintura, puzzles, quizzes de audio, AR interactivo
- **Sistema de autenticación** con JWT
- **Almacenamiento seguro** con EncryptedSharedPreferences
- **Mensajes públicos** entre usuarios (actividad "Mi Mensaje")
- **Gestión de imágenes** con Cloudinary para fotos de misiones

## 🎯 Módulos

### 🎨 Picasso - Guernica
Explora la obra maestra de Picasso a través de:
- **Color Peace**: Colorea el Guernica con zoom y borrador interactivo
- **View & Interpret**: Quiz sobre elementos de la obra
- **My Message**: Escribe mensajes de paz y visualiza mensajes de otros usuarios en tiempo real

### 🌳 Árbol de la Paz
Actividades sobre el símbolo de paz de Gernika:
- **Interactive**: Experiencia AR interactiva
- **Audio Quiz**: Preguntas con respuestas de audio
- **Puzzle**: Rompecabezas del árbol

### 🏛️ Plaza de Gernika
Descubre la plaza histórica:
- **Video**: Material audiovisual educativo
- **Drag Products**: Arrastra productos al mercado
- **Verse Game**: Juego de versos tradicionales
- **Photo Mission**: Misiones fotográficas

### 🏉 Fronton - Pelota Vasca
Aprende sobre el deporte tradicional vasco:
- **Info**: Información sobre pelota vasca
- **Dancing Ball**: Juego de ritmo
- **Cesta Tip**: Actividad de cesta punta
- **Values Group**: Agrupa valores

### 🏚️ Bunkers - Refugios
Reflexiona sobre la Guerra Civil:
- **Sound Game**: Juego de sonidos históricos
- **Peace Mural**: Crea un mural de paz
- **Reflection**: Actividad de reflexión

## 🛠️ Tecnologías

- **Kotlin** - Lenguaje principal
- **Coroutines** - Programación asíncrona
- **Retrofit 2** + **OkHttp** - Networking con interceptores
- **Moshi** - Parsing JSON con adaptadores generados
- **ViewBinding** - Binding de vistas type-safe
- **EncryptedSharedPreferences** - Almacenamiento seguro de tokens
- **Google Maps SDK** - Integración de mapas interactivos
- **Material Design 3** - UI/UX moderna
- **Cloudinary SDK** - Gestión de imágenes en la nube
- **JWT** - Autenticación con tokens Bearer

## 📁 Estructura del Proyecto

```
app/src/main/java/es/didaktikapp/gernikapp/
├── arbol/                    # Módulo: Árbol de la Paz
├── bunkers/                  # Módulo: Refugios
├── picasso/                  # Módulo: Guernica
│   ├── ColorPeaceActivity.kt
│   ├── MyMessageActivity.kt  # Mensajes de paz con API
│   ├── PaintCanvasView.kt    # Canvas con zoom y borrador
│   └── ResultActivity.kt
├── plaza/                    # Módulo: Plaza de Gernika
│   └── PhotoMissionActivity.kt  # Cloudinary integration
├── fronton/                  # Módulo: Pelota Vasca
├── data/
│   ├── models/               # Data classes
│   │   ├── PerfilProgresoResponse.kt  # Perfil y progreso completo
│   │   ├── RespuestasPublicasResponse.kt  # Mensajes públicos
│   │   └── ...
│   ├── repository/           # Repositorios
│   │   ├── AuthRepository.kt
│   │   ├── UserRepository.kt
│   │   ├── GameRepository.kt
│   │   └── BaseRepository.kt
│   └── local/
│       └── TokenManager.kt   # Gestión JWT
├── network/
│   ├── ApiService.kt         # Endpoints Retrofit
│   ├── RetrofitClient.kt     # Cliente singleton
│   └── AuthInterceptor.kt    # Auto-inyección de JWT
└── utils/
    ├── Constants.kt          # IDs de actividades y configuración
    ├── SyncManager.kt        # Sincronización de progreso
    ├── Resource.kt           # Wrapper de resultados
    └── BitmapUtils.kt        # Utilidades para imágenes
```

## 🔄 Sincronización de Progreso

GernikApp sincroniza automáticamente el progreso del usuario entre dispositivos:

### Cómo Funciona

1. **Al hacer login**: Se descarga el progreso completo del servidor
2. **Al completar actividad**: Se sube automáticamente al servidor
3. **Cambio de dispositivo**: El progreso se restaura automáticamente

### SyncManager

Gestiona la sincronización bidireccional entre servidor y SharedPreferences locales:

```kotlin
// Sincronizar datos del servidor (automático al login)
SyncManager.syncPerfilProgreso(context, perfilProgreso)

// Limpiar progreso local (al logout)
SyncManager.clearAllProgress(context)
```

### Datos Sincronizados

- ✅ Actividades completadas por módulo
- ✅ Puntuaciones máximas (top score)
- ✅ Racha de días
- ✅ Estadísticas globales
- ✅ Estado de cada actividad (no_iniciada, en_progreso, completada)

## 🚀 Instalación

### Requisitos
- Android Studio Arctic Fox o superior
- JDK 11 o superior
- Android SDK 24 o superior (Nougat 7.0+)

### Configuración

1. **Clonar el repositorio**
```bash
git clone https://github.com/Gernika-DidaktikApp/GernikApp.git
cd GernikApp
```

2. **Configurar variables de entorno**

   **a) Crear archivo `local.properties`**

   Copiar el archivo de ejemplo y completar con tus credenciales:
   ```bash
   cp local.properties.example local.properties
   ```

   **b) Editar `local.properties`** con tus credenciales:

   ```properties
   # Backend API
   API_BASE_URL=https://gernibide.up.railway.app

   # Cloudinary (para Photo Mission)
   CLOUDINARY_CLOUD_NAME=tu_cloud_name_aqui
   CLOUDINARY_API_KEY=tu_api_key_aqui
   CLOUDINARY_API_SECRET=tu_api_secret_aqui
   ```

   > **Nota para desarrollo local**: Si usas el emulador de Android y el backend corre en localhost, usa:
   > ```properties
   > API_BASE_URL=http://10.0.2.2:8000
   > ```

3. **Configurar Google Maps API Key**

   **a) Obtener API Key**
   - Ve a [Google Cloud Console](https://console.cloud.google.com/)
   - Crea un proyecto o selecciona uno existente
   - Habilita "Maps SDK for Android"
   - En "Credenciales", crea una API Key
   - Restringe la key a tu aplicación (opcional pero recomendado)

   **b) Añadir la key al proyecto**

   Editar `app/src/main/res/values/google_maps_api.xml`:
   ```xml
   <resources>
       <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
           TU_GOOGLE_MAPS_API_KEY_AQUI
       </string>
   </resources>
   ```

4. **Configurar Cloudinary** (Opcional - solo para Photo Mission)

   Si vas a usar la funcionalidad de Photo Mission:
   - Crea una cuenta en [Cloudinary](https://cloudinary.com/)
   - En el Dashboard, obtén:
     - Cloud Name
     - API Key
     - API Secret
   - Añádelos en `local.properties` (paso 2b)

   > Ver [CLOUDINARY_SETUP.md](CLOUDINARY_SETUP.md) para más detalles

5. **Build y Run**
   ```bash
   # Compilar debug
   ./gradlew assembleDebug

   # Instalar en dispositivo/emulador
   ./gradlew installDebug

   # O simplemente ejecutar desde Android Studio
   ```

### Archivos de Configuración

| Archivo | Propósito | En Git |
|---------|-----------|--------|
| `local.properties.example` | Plantilla de configuración | ✅ Sí |
| `local.properties` | Configuración real con credenciales | ❌ No (en .gitignore) |


## 🎮 Uso

### Flujo de Juego

1. **Login/Registro** → Autenticación con JWT
2. **Sincronización automática** → Descarga progreso del servidor
3. **Crear Partida** → Obtención de `juegoId`
4. **Mapa** → Selección de ubicación en Google Maps
5. **Módulo Principal** → Inicio automático de actividad
6. **Sub-actividades** → Completar mini-juegos
7. **Sincronización continua** → Progreso se guarda automáticamente en servidor

### Gestión de Sesión

```kotlin
// Guardar token
tokenManager.saveToken(token)
tokenManager.saveJuegoId(juegoId)

// Verificar sesión
if (tokenManager.hasActiveSession()) {
    // Usuario autenticado
}

// Cerrar sesión
tokenManager.clearSession()
```

### Llamadas a API

```kotlin
lifecycleScope.launch {
    when (val result = gameRepository.iniciarActividad(juegoId, actividadId, eventoId)) {
        is Resource.Success -> { /* Éxito */ }
        is Resource.Error -> { /* Error: result.message */ }
        is Resource.Loading -> { /* Cargando */ }
    }
}
```

## ☁️ Cloudinary Integration

La app utiliza Cloudinary para gestionar las imágenes de las misiones fotográficas:

### Configuración

1. Crear cuenta en [Cloudinary](https://cloudinary.com/)
2. Añadir credenciales en `local.properties`:

```properties
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret
CLOUDINARY_UPLOAD_PRESET=tu_upload_preset
```

3. Las imágenes se suben automáticamente al completar Photo Mission
4. Las URLs se guardan en el servidor como `respuesta_contenido`

> 📝 Para más detalles, consulta [CLOUDINARY_SETUP.md](CLOUDINARY_SETUP.md)

## 🎨 Características Especiales

### PaintCanvasView
Custom View para pintar con:
- ✅ Zoom con pellizco (pinch-to-zoom)
- ✅ Trazos suaves con curvas cuadráticas
- ✅ Paleta de 5 colores
- ✅ Borrador (color gris claro)
- ✅ Guardado/carga de imágenes
- ✅ Área pintable delimitada

```kotlin
// Configuración del canvas
binding.paintCanvas.currentColor = Color.parseColor("#4FC3F7")
binding.paintCanvas.setPaintableBounds(left, top, right, bottom)
binding.paintCanvas.saveToInternalStorage(context)
```

### Mensajes Públicos (My Message Activity)

Sistema de mensajes compartidos entre usuarios:

```kotlin
// Obtener mensajes de otros usuarios
lifecycleScope.launch {
    when (val result = userRepository.getRespuestasPublicas(actividadId, limit = 5)) {
        is Resource.Success -> {
            // Mostrar mensajes de paz de otros usuarios
            result.data.respuestas.forEach { respuesta ->
                addMessageView(respuesta.mensaje, respuesta.usuario)
            }
        }
        is Resource.Error -> { /* Manejar error */ }
    }
}
```

### Progreso Local
Estado de actividades guardado en `SharedPreferences`:
- Persistencia local con sincronización automática
- Marcado de completadas
- Sincronización bidireccional con API
- Prevención de doble completado
- Recuperación de progreso entre dispositivos

## 📡 API Endpoints

### Autenticación
- `POST /api/v1/auth/login-app` - Login con JWT
- `POST /api/v1/usuarios` - Registro de usuario

### Usuario
- `GET /api/v1/usuarios/{usuario_id}` - Obtener perfil
- `PUT /api/v1/usuarios/{usuario_id}` - Actualizar perfil
- `GET /api/v1/usuarios/{usuario_id}/estadisticas` - Estadísticas del usuario
- `GET /api/v1/usuarios/{usuario_id}/perfil-progreso` - **Perfil completo con progreso detallado**

### Partidas
- `POST /api/v1/partidas` - Crear partida
- `GET /api/v1/partidas/{id}` - Obtener partida
- `POST /api/v1/partidas/activa/usuario/{usuario_id}/obtener-o-crear` - Obtener o crear partida activa

### Progreso de Actividades
- `POST /api/v1/actividad-progreso/iniciar` - Iniciar actividad
- `PUT /api/v1/actividad-progreso/{progreso_id}/completar` - Completar actividad
- `PUT /api/v1/actividad-progreso/{progreso_id}` - Actualizar progreso
- `GET /api/v1/actividad-progreso/{progreso_id}` - Obtener progreso

### Actividades
- `GET /api/v1/actividades/{actividad_id}/respuestas-publicas` - **Obtener mensajes públicos de otros usuarios**

> 📝 Para más detalles sobre los endpoints, consulta [API_ENDPOINTS.md](API_ENDPOINTS.md)

## 🔒 Permisos

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 📝 Constantes

IDs de actividades definidos en `Constants.kt`:

```kotlin
// Picasso
Puntos.Picasso.ID
Puntos.Picasso.COLOR_PEACE
Puntos.Picasso.VIEW_INTERPRET
Puntos.Picasso.MY_MESSAGE

// Árbol
Puntos.Arbol.ID
Puntos.Arbol.INTERACTIVE
Puntos.Arbol.AUDIO_QUIZ
Puntos.Arbol.PUZZLE

// Plaza, Fronton, Bunkers...
```

## 🧪 Testing

```bash
# Ejecutar tests unitarios
./gradlew test

# Ejecutar tests instrumentados
./gradlew connectedAndroidTest
```

## 📚 Documentación Adicional

- [API_ENDPOINTS.md](API_ENDPOINTS.md) - Documentación completa de endpoints de la API
- [CLOUDINARY_SETUP.md](CLOUDINARY_SETUP.md) - Guía detallada de configuración de Cloudinary
- [CLAUDE.md](CLAUDE.md) - Contexto del proyecto para desarrollo con IA
- [local.properties.example](local.properties.example) - Plantilla de configuración local

## 🐛 Troubleshooting

### Errores de Configuración

#### BuildConfig no se genera
Si ves errores como `Unresolved reference: BuildConfig`:
1. Verifica que `local.properties` existe en la raíz del proyecto
2. Ejecuta `Build > Clean Project` y luego `Build > Rebuild Project`
3. Sincroniza Gradle: `File > Sync Project with Gradle Files`

#### Google Maps no se muestra
Si el mapa aparece en blanco:
1. Verifica que `google_maps_key` esté correctamente configurada en `app/src/main/res/values/google_maps_api.xml`
2. Asegúrate de que la API Key esté habilitada para "Maps SDK for Android"
3. Revisa que la restricción de la API Key incluya el SHA-1 de tu aplicación
4. Espera unos minutos después de crear la key (puede tardar en propagarse)

#### Error de conexión a la API
Si recibes errores de conexión:
1. **Emulador Android**: Usa `http://10.0.2.2:8000` en lugar de `localhost:8000`
2. **Dispositivo físico**: Asegúrate de que tu servidor sea accesible desde la red local
3. Verifica que `API_BASE_URL` en `local.properties` sea correcta
4. Revisa los logs de Retrofit en Logcat para ver la URL completa que se está usando

#### Error de Cloudinary
Si falla la subida de fotos en Photo Mission:
1. Verifica que todas las credenciales de Cloudinary estén en `local.properties`
2. Asegúrate de que no hay espacios extra en los valores
3. Confirma que el upload preset esté configurado como "unsigned" en Cloudinary
4. Revisa [CLOUDINARY_SETUP.md](CLOUDINARY_SETUP.md) para más detalles

### Errores de API

#### Error 404 en /respuestas-publicas
Si recibes 404 al cargar mensajes públicos:
1. Verifica que el token JWT no haya expirado (hacer logout/login)
2. Revisa logs de `AuthInterceptor` para confirmar que el token se está enviando
3. Verifica que `API_BASE_URL` apunte a la URL correcta en `local.properties`

#### Error 401 Unauthorized
Si recibes errores de autenticación:
1. El token JWT puede haber expirado - cierra sesión y vuelve a iniciar
2. Verifica que el `AuthInterceptor` esté añadiendo el header correctamente
3. Revisa que el token se esté guardando correctamente en `TokenManager`

### Errores de Sincronización

#### Progreso no sincroniza
1. Asegúrate de tener conexión a internet al hacer login
2. Verifica logs de `SyncManager` para errores de sincronización
3. Si falla la sincronización, la app continúa con datos locales
4. Intenta cerrar sesión y volver a iniciar para forzar la sincronización

#### Puntuaciones no se guardan
1. Verifica que tengas una partida activa (`juegoId` guardado)
2. Revisa que las llamadas a `completarEvento()` se estén haciendo correctamente
3. Comprueba los logs de `GameRepository` para ver si hay errores en la API

## 👥 Desarrolladores

| Nombre             | GitHub                                         |
|--------------------|------------------------------------------------|
| **Arantxa Main**   | [@arantxaMain](https://github.com/arantxaMain) |
| **Erlantz Garcia** | [@Erlantz50](https://github.com/Erlnatz50)     |
| **Telmo Castillo** | [@telca5](https://github.com/telcas5)          |
| **Wara Pacheco**   | [@warayasy](https://github.com/warayasy)       |

---

## 📊 Información del Proyecto

| Propiedad | Valor |
|-----------|-------|
| **Versión** | 1.0 |
| **Última actualización** | Febrero 2026 |
| **SDK mínimo** | Android 7.0 (API 24) |
| **SDK compilación** | Android 16 (API 36) |
| **SDK objetivo** | Android 16 (API 36) |
| **Lenguaje** | Kotlin |
| **JDK** | 11 |
| **Backend** | FastAPI + PostgreSQL |
| **Base URL Producción** | https://gernibide.up.railway.app |
| **Repositorio** | https://github.com/Gernika-DidaktikApp/GernikApp |

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

<div align="center">
  <p>Gernibide - Aprendiendo sobre paz, historia y cultura vasca</p>
</div>