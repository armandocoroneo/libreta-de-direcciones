# Libreta de Direcciones

App de Android nativa (Kotlin + Jetpack Compose + Room) para gestionar una agenda de contactos con ubicación GPS.

## Novedades de la v2

- **Llamar directo**: ícono de teléfono en la vista del contacto abre el marcador con el número ya cargado (no llama solo, vos confirmás).
- **Foto de perfil**: se elige desde la galería al crear/editar un contacto; se guarda una copia dentro de la app.
- **Exportar/Importar (backup)**: menú (⋮) en la lista → "Exportar backup" guarda un archivo `.json` con toda la libreta; "Importar backup" lo vuelve a cargar (útil al cambiar de celular).
- **Compartir contacto estructurado**: desde el menú (⋮) en la vista de un contacto → "Compartir como archivo" — envía un `.json` con los campos del contacto (no solo texto), que la otra persona puede importar con "Importar backup" y le queda cargado con sus propios campos, no como un texto suelto.
- **Recordatorios**: botón "Marcar contactado hoy"; la app muestra "Último contacto: hace X" y un ícono de aviso ⚠️ en la lista si pasaron más de ~6 meses sin marcarlo.
- **Coordenadas copiables offline**: botón "Copiar coordenadas" en la vista del contacto — funciona sin internet (a diferencia de abrir Maps, que sí necesita conexión).
- **Backup automático con tu cuenta de Google**: la app declara sus datos (incluida la base de datos y las fotos) para el backup nativo de Android — si tienes activado "Copia de seguridad" en Ajustes del sistema con tu cuenta de Google, tus contactos se restauran solos al cambiar de celular, sin configurar nada aparte.

## Funciones de la v1

- **Campos por contacto**: nombre (obligatorio), teléfono (opcional), descripción (quién es la persona), ubicación GPS.
- **Lista de contactos** ordenada (favoritos primero, luego alfabético), con búsqueda en tiempo real por nombre, descripción o teléfono.
- **Ver sin riesgo de editar**: tocar un contacto abre una vista de solo lectura. Para modificarlo hay que tocar el ícono de editar ✏️ a propósito — evita cambios accidentales si el celular está en el bolsillo.
- **Editar y eliminar** contactos, con confirmación antes de borrar. También se puede eliminar directo desde la lista (ícono de basura en cada fila).
- **Marcar como favorito** (⭐, aparecen primero en la lista).
- **Ubicación GPS clicable**: botón "Usar mi ubicación GPS actual" para capturar coordenadas del dispositivo; tocar la ubicación (en la lista o en el detalle) abre Google Maps directo en ese punto.
- **Compartir contacto**: botón que copia al portapapeles el nombre, teléfono, descripción y el link de Maps del contacto, listo para pegar donde quieras (WhatsApp, SMS, etc.).
- **Fondo oscuro fijo**, independiente del modo claro/oscuro del sistema.
- Datos guardados localmente con Room (SQLite), persisten entre sesiones — no se sincronizan a ningún servidor.

## Cómo abrir el proyecto
1. Instala **Android Studio** (versión Koala 2024.1 o más reciente).
2. Abre Android Studio → **Open** → selecciona la carpeta `LibretaDirecciones`.
3. Espera a que Gradle sincronice.
4. Conecta un dispositivo Android (o crea un emulador con AVD Manager, API 24+).
5. Presiona **Run ▶** para instalar y ejecutar la app.
6. Al usar "Usar mi ubicación GPS actual" por primera vez, la app pedirá permiso de ubicación — acéptalo.

## Compilar el APK sin Android Studio
El repositorio incluye `codemagic.yaml`, listo para compilar un APK de depuración en la nube gratis vía [Codemagic](https://codemagic.io), conectando el repositorio de GitHub y ejecutando el workflow `android-apk`.

## Estructura del proyecto
```
app/src/main/java/com/example/libretadirecciones/
├── MainActivity.kt              # Punto de entrada
├── data/
│   ├── Contacto.kt               # Entidad Room (nombre, descripción, teléfono, lat/lng, favorito)
│   ├── ContactoDao.kt            # Consultas SQL (CRUD + búsqueda)
│   ├── AppDatabase.kt            # Base de datos Room
│   └── ContactoRepositorio.kt    # Capa de repositorio
└── ui/
    ├── ContactosViewModel.kt     # Estado y lógica de la UI
    ├── PantallaLista.kt          # Lista + búsqueda + eliminar rápido + ícono de ubicación
    ├── PantallaVerContacto.kt    # Vista de solo lectura + compartir + editar + eliminar
    ├── PantallaDetalle.kt        # Alta/edición + captura GPS
    ├── AbrirMaps.kt              # Helper para lanzar Intent hacia Maps
    ├── Navegacion.kt             # Navigation Compose (lista → ver → editar)
    └── theme/Tema.kt             # Tema oscuro fijo
```

## Requisitos técnicos
- minSdk 24 (Android 7.0+)
- targetSdk / compileSdk 34
- Kotlin 2.0.21, Android Gradle Plugin 8.5.2, KSP 2.0.21-1.0.28
- Jetpack Compose (BOM 2024.09.00) con el plugin `org.jetbrains.kotlin.plugin.compose`
- Google Play Services Location (para capturar el GPS)
- Requiere que el dispositivo/emulador tenga Google Play Services y una app de mapas instalada (Google Maps u otra compatible con esquema `geo:`)

## Nota sobre Play Protect
Al instalar el APK fuera de Play Store, Android puede mostrar una advertencia de Play Protect (falso positivo típico de cualquier APK sin firma oficial que pida permiso de ubicación). La app no envía datos a ningún servidor: todo se guarda localmente en el dispositivo.

## Pendiente / fuera de alcance por ahora
- Sincronización propia vía API de Google Drive (requeriría que crees un proyecto en Google Cloud Console con credenciales OAuth) — por ahora se usa el backup automático nativo de Android en su lugar.
- Mapas 100% offline con tiles descargados (requiere una librería de mapas pesada, ej. OSMDroid) — hoy siempre se abre Google Maps, que necesita conexión.
- Elegir la ubicación tocando un punto en un mapa, en vez de solo GPS actual.
- Categorías/etiquetas con filtro.
