# Libreta de Direcciones

App de Android nativa (Kotlin + Jetpack Compose + Room) para gestionar una agenda de contactos con ubicación GPS.

## Funciones
- Lista de contactos ordenada (favoritos primero, luego alfabético).
- Búsqueda en tiempo real por nombre, descripción o teléfono.
- Agregar, editar y eliminar contactos.
- Marcar contactos como favoritos.
- Campos: **nombre**, **descripción** (quién es la persona), teléfono (opcional), **ubicación GPS**.
- **Ubicación GPS clicable**: al tocar la ubicación de un contacto (en la lista o en el detalle), se abre Google Maps en esa coordenada.
- Botón "Usar mi ubicación GPS actual" para capturar automáticamente las coordenadas del dispositivo.
- **Fondo oscuro fijo** (nunca blanco), independiente del modo del sistema.
- Datos guardados localmente con Room (SQLite), persisten entre sesiones.

## Cómo abrir el proyecto
1. Instala **Android Studio** (versión Koala 2024.1 o más reciente).
2. Abre Android Studio → **Open** → selecciona la carpeta `LibretaDirecciones`.
3. Espera a que Gradle sincronice (descargará el wrapper de Gradle 8.7 automáticamente).
4. Conecta un dispositivo Android (o crea un emulador con AVD Manager, API 24+).
5. Presiona **Run ▶** para instalar y ejecutar la app.
6. Al usar "Usar mi ubicación GPS actual" por primera vez, la app pedirá permiso de ubicación — acéptalo. En el emulador, asegúrate de fijar una ubicación simulada (Extended Controls → Location).

## Estructura del proyecto
```
app/src/main/java/com/example/libretadirecciones/
├── MainActivity.kt              # Punto de entrada
├── data/
│   ├── Contacto.kt               # Entidad Room (nombre, descripción, teléfono, lat/lng)
│   ├── ContactoDao.kt            # Consultas SQL (CRUD + búsqueda)
│   ├── AppDatabase.kt            # Base de datos Room
│   └── ContactoRepositorio.kt    # Capa de repositorio
└── ui/
    ├── ContactosViewModel.kt     # Estado y lógica de la UI
    ├── PantallaLista.kt          # Lista + búsqueda + ícono de ubicación
    ├── PantallaDetalle.kt        # Alta/edición/eliminación + captura GPS
    ├── AbrirMaps.kt              # Helper para lanzar Intent hacia Maps
    ├── Navegacion.kt             # Navigation Compose
    └── theme/Tema.kt             # Tema oscuro fijo
```

## Requisitos técnicos
- minSdk 24 (Android 7.0+)
- targetSdk / compileSdk 34
- Kotlin 1.9.24, Jetpack Compose (BOM 2024.06.00)
- Google Play Services Location (para capturar el GPS)
- Requiere que el dispositivo/emulador tenga Google Play Services y una app de mapas instalada (Google Maps u otra compatible con esquema `geo:`).

## Posibles mejoras futuras
- Foto de perfil por contacto.
- Exportar/importar contactos (vCard o CSV).
- Elegir la ubicación en un mapa en lugar de solo GPS actual.
- Marcar/llamar directamente desde la app (Intent de llamada).
