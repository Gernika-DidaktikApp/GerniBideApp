# Configuración de Cloudinary

## Pasos para configurar Cloudinary en GernikApp

### 1. Crear cuenta en Cloudinary

1. Ve a [https://cloudinary.com/](https://cloudinary.com/)
2. Regístrate o inicia sesión
3. En el Dashboard, encontrarás tus credenciales:
   - **Cloud Name**
   - **API Key**
   - **API Secret**

### 2. Configurar credenciales en el proyecto

Abre el archivo `local.properties` en la raíz del proyecto y añade las siguientes líneas:

```properties
# Configuración de la API
API_BASE_URL=http://tu-servidor:8000

# Configuración de Cloudinary
CLOUDINARY_CLOUD_NAME=tu_cloud_name
CLOUDINARY_API_KEY=tu_api_key
CLOUDINARY_API_SECRET=tu_api_secret
```

**Ejemplo:**

```properties
API_BASE_URL=http://10.0.2.2:8000

CLOUDINARY_CLOUD_NAME=dxyz123abc
CLOUDINARY_API_KEY=123456789012345
CLOUDINARY_API_SECRET=abcdefghijklmnopqrstuvwxyz123456
```

### 3. Sincronizar el proyecto

Después de añadir las credenciales:

1. En Android Studio, haz clic en **File > Sync Project with Gradle Files**
2. Espera a que la sincronización termine
3. Compila y ejecuta la app

### 4. Verificar la configuración

Cuando abras `PhotoMissionActivity`, verás en el Logcat:

- ✅ Si está configurado correctamente: `✅ Cloudinary inicializado correctamente`
- ⚠️ Si falta configuración: `⚠️ Cloudinary no configurado en local.properties`

## Flujo de subida de imágenes

1. El usuario toma una foto con la cámara
2. Selecciona una etiqueta (Tradizioa, Komunitatea, Bizikidetza)
3. Al presionar "Igo" (Subir):
   - La imagen se sube a Cloudinary (carpeta: `gernikapp/photo_missions`)
   - Cloudinary devuelve una URL segura (HTTPS)
   - La URL se envía al backend junto con la puntuación (100.0)
   - La foto se añade a la galería local

## Características de seguridad

- Las credenciales se almacenan en `local.properties` (ignorado por git)
- Las credenciales se inyectan en `BuildConfig` durante la compilación
- La subida usa HTTPS seguro
- Compresión JPEG al 80% para optimizar tamaño

## Carpeta en Cloudinary

Las imágenes se suben a: `gernikapp/photo_missions/`

Puedes cambiar la carpeta en `PhotoMissionActivity.kt`:

```kotlin
.option("folder", "gernikapp/photo_missions")
```

## Formato de respuesta del backend

El backend debe aceptar en `CompletarEventoRequest`:

```json
{
  "puntuacion": 100.0,
  "image_url": "https://res.cloudinary.com/tu_cloud/image/upload/v123456789/gernikapp/photo_missions/abc123.jpg"
}
```

El campo `image_url` es opcional. Si una actividad no envía imágenes, el campo será `null`.

## Solución de problemas

### Error: "Cloudinary no configurado"

- Verifica que las variables estén en `local.properties`
- Sincroniza el proyecto con Gradle
- Limpia y reconstruye: **Build > Clean Project** y luego **Build > Rebuild Project**

### Error durante la subida

- Verifica tu conexión a internet
- Revisa que las credenciales sean correctas en el Dashboard de Cloudinary
- Consulta los logs de Logcat filtrando por `PhotoMissionActivity`

### La imagen no se sube

- Asegúrate de que el permiso de CÁMARA esté concedido
- Verifica que tomaste una foto antes de seleccionar etiqueta
- Comprueba que seleccionaste una etiqueta antes de presionar "Igo"
