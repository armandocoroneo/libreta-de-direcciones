package com.example.libretadirecciones.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.libretadirecciones.data.Contacto
import com.example.libretadirecciones.util.copiarFotoAAlmacenamientoInterno
import com.google.android.gms.location.LocationServices
import java.io.File
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaDetalle(
    contactoId: Long,
    viewModel: ContactosViewModel,
    alGuardar: () -> Unit,
    alCancelar: () -> Unit
) {
    val context = LocalContext.current

    var cargado by remember { mutableStateOf(contactoId == 0L) }
    var idActual by remember { mutableStateOf(0L) }
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var etiquetaUbicacion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var favorito by remember { mutableStateOf(false) }
    var fotoUri by remember { mutableStateOf<String?>(null) }
    var ultimoContactoMillis by remember { mutableStateOf<Long?>(null) }
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }
    var obteniendoUbicacion by remember { mutableStateOf(false) }
    var errorUbicacion by remember { mutableStateOf<String?>(null) }

    val clienteUbicacion = remember { LocationServices.getFusedLocationProviderClient(context) }

    val selectorFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val rutaCopia = copiarFotoAAlmacenamientoInterno(context, uri)
            if (rutaCopia != null) fotoUri = rutaCopia
        }
    }

    fun capturarUbicacion() {
        obteniendoUbicacion = true
        errorUbicacion = null
        clienteUbicacion.lastLocation
            .addOnSuccessListener { ubicacion ->
                obteniendoUbicacion = false
                if (ubicacion != null) {
                    latitud = ubicacion.latitude
                    longitud = ubicacion.longitude
                } else {
                    errorUbicacion = "No se pudo obtener la ubicación. Activa el GPS e intenta de nuevo."
                }
            }
            .addOnFailureListener {
                obteniendoUbicacion = false
                errorUbicacion = "Error al obtener la ubicación."
            }
    }

    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) capturarUbicacion()
        else errorUbicacion = "Se necesita permiso de ubicación para usar el GPS."
    }

    fun solicitarUbicacionActual() {
        val permisoConcedido = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (permisoConcedido) capturarUbicacion()
        else lanzadorPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(contactoId) {
        if (contactoId != 0L) {
            viewModel.obtenerPorId(contactoId)?.let {
                idActual = it.id
                nombre = it.nombre
                descripcion = it.descripcion
                telefono = it.telefono
                etiquetaUbicacion = it.etiquetaUbicacion
                latitud = it.latitud
                longitud = it.longitud
                favorito = it.favorito
                fotoUri = it.fotoUri
                ultimoContactoMillis = it.ultimoContactoMillis
            }
            cargado = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (contactoId == 0L) "Nuevo contacto" else "Editar contacto") },
                navigationIcon = {
                    IconButton(onClick = alCancelar) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    IconButton(onClick = { favorito = !favorito }) {
                        Icon(
                            imageVector = if (favorito) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorito"
                        )
                    }
                    if (contactoId != 0L) {
                        IconButton(onClick = { mostrarDialogoBorrar = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (nombre.isNotBlank()) {
                        viewModel.guardar(
                            Contacto(
                                id = idActual,
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                telefono = telefono.trim(),
                                latitud = latitud,
                                longitud = longitud,
                                etiquetaUbicacion = etiquetaUbicacion.trim(),
                                favorito = favorito,
                                fotoUri = fotoUri,
                                ultimoContactoMillis = ultimoContactoMillis
                            )
                        ) { alGuardar() }
                    }
                },
                icon = { Icon(Icons.Default.Check, contentDescription = null) },
                text = { Text("Guardar") }
            )
        }
    ) { padding ->
        if (!cargado) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Selector de foto
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { selectorFoto.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = remember(fotoUri) {
                            fotoUri?.let { ruta ->
                                try {
                                    val archivo = File(ruta)
                                    if (archivo.exists()) BitmapFactory.decodeFile(ruta) else null
                                } catch (e: Exception) { null }
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Foto del contacto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.AddAPhoto,
                                contentDescription = "Agregar foto",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                if (fotoUri != null) {
                    TextButton(
                        onClick = { fotoUri = null },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) { Text("Quitar foto") }
                }

                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = telefono, onValueChange = { telefono = it },
                    label = { Text("Teléfono (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = descripcion, onValueChange = { descripcion = it },
                    label = { Text("Descripción (quién es)") },
                    placeholder = { Text("Ej: amigo de la universidad, plomero, jefe...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                Text("Ubicación", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = etiquetaUbicacion, onValueChange = { etiquetaUbicacion = it },
                    label = { Text("Etiqueta (ej: Casa, Oficina)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )

                // Tarjeta de ubicación: muestra coordenadas y permite abrir en Maps
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .then(
                            if (latitud != null && longitud != null)
                                Modifier.clickable {
                                    abrirEnMaps(context, latitud!!, longitud!!, nombre)
                                }
                            else Modifier
                        )
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            if (latitud != null && longitud != null) {
                                Text(
                                    "Lat: ${redondear(latitud!!)}, Lon: ${redondear(longitud!!)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Toca para abrir en Maps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                Text(
                                    "Sin ubicación guardada",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = { solicitarUbicacionActual() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !obteniendoUbicacion
                ) {
                    if (obteniendoUbicacion) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Obteniendo ubicación...")
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usar mi ubicación GPS actual")
                    }
                }

                if (latitud != null && longitud != null) {
                    TextButton(onClick = { latitud = null; longitud = null }) {
                        Text("Quitar ubicación")
                    }
                }

                errorUbicacion?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(72.dp)) // espacio para el FAB
            }
        }
    }

    if (mostrarDialogoBorrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrar = false },
            title = { Text("Eliminar contacto") },
            text = { Text("¿Seguro que deseas eliminar a $nombre?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(
                        Contacto(
                            id = idActual, nombre = nombre, descripcion = descripcion,
                            telefono = telefono, latitud = latitud, longitud = longitud,
                            etiquetaUbicacion = etiquetaUbicacion, favorito = favorito,
                            fotoUri = fotoUri, ultimoContactoMillis = ultimoContactoMillis
                        )
                    )
                    mostrarDialogoBorrar = false
                    alGuardar()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrar = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun redondear(valor: Double): Double = round(valor * 10000) / 10000
