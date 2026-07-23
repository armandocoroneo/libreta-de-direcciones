package com.example.libretadirecciones.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.libretadirecciones.data.Contacto
import com.example.libretadirecciones.util.haceMuchoQueNoContacta
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    viewModel: ContactosViewModel,
    alSeleccionarContacto: (Long) -> Unit,
    alPresionarNuevo: () -> Unit
) {
    val context = LocalContext.current
    val alcanceCorutinas = rememberCoroutineScope()
    val contactos by viewModel.contactos.collectAsState()
    var texto by viewModel.textoBusqueda
    var mensajeSnackbar by remember { mutableStateOf<String?>(null) }
    var mostrarMenu by remember { mutableStateOf(false) }

    val exportadorArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            alcanceCorutinas.launch {
                val json = viewModel.exportarTodoComoJson()
                try {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                    mensajeSnackbar = "Backup guardado correctamente"
                } catch (e: Exception) {
                    mensajeSnackbar = "No se pudo guardar el archivo"
                }
            }
        }
    }

    val importadorArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            alcanceCorutinas.launch {
                try {
                    val texto = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    if (texto != null) {
                        viewModel.importarDesdeJson(
                            texto,
                            alTerminar = { cantidad -> mensajeSnackbar = "Se importaron $cantidad contacto(s)" },
                            alError = { error -> mensajeSnackbar = error }
                        )
                    }
                } catch (e: Exception) {
                    mensajeSnackbar = "No se pudo leer el archivo"
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Libreta de Direcciones") },
                actions = {
                    Box {
                        IconButton(onClick = { mostrarMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Exportar backup") },
                                leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null) },
                                onClick = {
                                    mostrarMenu = false
                                    exportadorArchivo.launch("libreta_backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Importar backup") },
                                leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                                onClick = {
                                    mostrarMenu = false
                                    importadorArchivo.launch(arrayOf("application/json", "text/plain", "*/*"))
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = alPresionarNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo contacto")
            }
        },
        snackbarHost = {
            mensajeSnackbar?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2200)
                    mensajeSnackbar = null
                }
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = texto,
                onValueChange = { texto = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar contacto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (texto.isNotEmpty()) {
                        IconButton(onClick = { texto = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true
            )

            if (contactos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (texto.isBlank()) "No hay contactos todavía.\nToca + para agregar uno."
                        else "Sin resultados para \"$texto\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn {
                    items(contactos, key = { it.id }) { contacto ->
                        FilaContacto(
                            contacto = contacto,
                            onClick = { alSeleccionarContacto(contacto.id) },
                            onEliminar = { viewModel.eliminar(contacto) }
                        )
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaContacto(contacto: Contacto, onClick: () -> Unit, onEliminar: () -> Unit) {
    val context = LocalContext.current
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val bitmap = remember(contacto.fotoUri) {
            contacto.fotoUri?.let { ruta ->
                try {
                    val archivo = File(ruta)
                    if (archivo.exists()) BitmapFactory.decodeFile(ruta) else null
                } catch (e: Exception) { null }
            }
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Foto de ${contacto.nombre}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (contacto.nombre.firstOrNull()?.uppercase() ?: "?"),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = contacto.nombre, fontWeight = FontWeight.Medium)
                if (haceMuchoQueNoContacta(contacto.ultimoContactoMillis)) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.NotificationImportant,
                        contentDescription = "Hace mucho no hablas",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (contacto.telefono.isNotBlank()) {
                Text(
                    text = contacto.telefono,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (contacto.descripcion.isNotBlank()) {
                Text(
                    text = contacto.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (contacto.tieneUbicacion) {
            IconButton(onClick = {
                abrirEnMaps(context, contacto.latitud!!, contacto.longitud!!, contacto.nombre)
            }) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Abrir ubicación en Maps",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
        if (contacto.favorito) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Favorito",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        IconButton(onClick = { mostrarDialogoBorrar = true }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar contacto",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }

    if (mostrarDialogoBorrar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrar = false },
            title = { Text("Eliminar contacto") },
            text = { Text("¿Seguro que deseas eliminar a ${contacto.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar()
                    mostrarDialogoBorrar = false
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrar = false }) { Text("Cancelar") }
            }
        )
    }
}
