# GernikApp 🏛️

Aplicación Android educativa e interactiva sobre Gernika (Euskadi, España). Una experiencia multimedia que combina historia, cultura y valores de paz a través de 5 módulos temáticos con mini-juegos y actividades interactivas.

## 📱 Características

- **5 módulos temáticos** con actividades interactivas
- **Integración con Google Maps** para navegación por ubicaciones históricas
- **Sistema de progreso y puntuación** sincronizado con API backend
- **Soporte multiidioma** (Euskera y Español)
- **Actividades multimedia**: pintura, puzzles, quizzes de audio, AR interactivo
- **Sistema de autenticación** con JWT
- **Almacenamiento seguro** con EncryptedSharedPreferences

## 🎯 Módulos

### 🎨 Picasso - Guernica
Explora la obra maestra de Picasso a través de:
- **Color Peace**: Colorea el Guernica con zoom y borrador
- **View & Interpret**: Quiz sobre elementos de la obra
- **My Message**: Escribe mensajes de paz

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
- **Retrofit 2** + **OkHttp** - Networking
- **Moshi** - Parsing JSON
- **ViewBinding** - Binding de vistas
- **EncryptedSharedPreferences** - Almacenamiento seguro
- **Google Maps SDK** - Integración de mapas
- **Material Design 3** - UI/UX

## 📁 Estructura del Proyecto

```
app/src/main/java/es/didaktikapp/gernikapp/
├── arbol/                    # Módulo: Árbol de la Paz
├── bunkers/                  # Módulo: Refugios
├── picasso/                  # Módulo: Guernica
│   ├── ColorPeaceActivity.kt
│   ├── PaintCanvasView.kt    # Canvas con zoom y borrador
│   └── ResultActivity.kt
├── plaza/                    # Módulo: Plaza de Gernika
├── fronton/                  # Módulo: Pelota Vasca
├── data/
│   ├── models/               # Data classes
│   ├── repository/           # Repositorios
│   └── local/
│       └── TokenManager.kt   # Gestión JWT
├── network/
│   ├── ApiService.kt         # Endpoints
│   ├── RetrofitClient.kt
│   └── AuthInterceptor.kt    # Auto-inyección de JWT
└── utils/
    ├── Constants.kt          # Configuración
    ├── Resource.kt           # Wrapper de resultados
    └── BitmapUtils.kt
```

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
2. **Crear Partida** → Obtención de `juegoId`
3. **Mapa** → Selección de ubicación
4. **Módulo Principal** → Inicio automático de actividad
5. **Sub-actividades** → Completar mini-juegos

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

### Progreso Local
Estado de actividades guardado en `SharedPreferences`:
- Persistencia local
- Marcado de completadas
- Sincronización con API
- Prevención de doble completado

## 📡 API Endpoints

### Autenticación
- `POST /api/v1/auth/login-app` - Login
- `POST /api/v1/usuarios` - Registro

### Usuario
- `GET /api/v1/users/{id}` - Perfil
- `PUT /api/v1/users/{id}` - Actualizar

### Partidas
- `POST /api/v1/partidas` - Crear
- `GET /api/v1/partidas/{id}` - Obtener
- `POST /api/v1/partidas/usuario/{id}/obtener-o-crear` - Obtener o crear activa

### Progreso
- `POST /api/v1/actividad-progreso/iniciar` - Iniciar actividad
- `PUT /api/v1/actividad-progreso/{id}/completar` - Completar actividad
- `GET /api/v1/actividad-progreso/{id}` - Obtener progreso

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

## 🤝 Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

**Versión**: 1.0
**Última actualización**: Febrero 2026
**SDK mínimo**: Android 7.0 (API 24)
**SDK objetivo**: Android 16 (API 35)