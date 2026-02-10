# GernikApp 🏛️

Aplicación Android educativa e interactiva sobre Gernika (Euskadi, España). Una experiencia multimedia que combina historia, cultura y valores de paz a través de 5 módulos temáticos con mini-juegos y actividades interactivas.

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
git clone https://github.com/tu-usuario/GernikApp.git
cd GernikApp
```

2. **Configurar API Base URL**

Crear `local.properties` en la raíz del proyecto:
```properties
API_BASE_URL=https://tu-api.com
```

Si no se especifica, usa `http://10.0.2.2:8000` (emulador Android).

3. **Google Maps API Key**

Añadir en `app/src/main/res/values/strings.xml`:
```xml
<string name="google_maps_key">TU_API_KEY_AQUI</string>
```

4. **Build y Run**
```bash
./gradlew assembleDebug
```

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

- [API_ENDPOINTS.md](API_ENDPOINTS.md) - Documentación completa de endpoints
- [CLOUDINARY_SETUP.md](CLOUDINARY_SETUP.md) - Configuración de Cloudinary
- [CLAUDE.md](CLAUDE.md) - Contexto del proyecto para desarrollo

## 🐛 Troubleshooting

### Error 404 en /respuestas-publicas
Si recibes 404 al cargar mensajes públicos:
1. Verifica que el token JWT no haya expirado (hacer logout/login)
2. Revisa logs de `AuthInterceptor` para confirmar que el token se está enviando
3. Verifica que `API_BASE_URL` apunte a la URL correcta en `local.properties`

### Progreso no sincroniza
1. Asegúrate de tener conexión a internet al hacer login
2. Verifica logs de `SyncManager` para errores de sincronización
3. Si falla la sincronización, la app continúa con datos locales

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## 👥 Autores

- **Wara Pacheco** - Desarrollo principal

---

**Versión**: 1.0
**Última actualización**: Febrero 2026
**SDK mínimo**: Android 7.0 (API 24)
**SDK objetivo**: Android 16 (API 35)
**Backend**: FastAPI + PostgreSQL
**Base URL**: https://gernibide.up.railway.app