package com.example.libretadirecciones.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.libretadirecciones.data.Contacto
import com.example.libretadirecciones.util.formatoTiempoTranscurrido
import com.example.libretadirecciones.util.haceMuchoQueNoContacta
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaVerContacto(
    contactoId: Long,
    viewModel: ContactosViewModel,
    alEditar: (Long) -> Unit,
    alVolver: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val alcanceCorutinas = androidx.compose.runtime.rememberCoroutineScope()
    var contacto by remember { mutableStateOf<Contacto?>(null) }
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }
    var mostrarMenu by remember { mutableStateOf(false) }
    var mensajeSnackbar by remember { mutableStateOf<String?>(null) }

    suspend fun recargar() {
        contacto = viewModel.obtenerPorId(contactoId)
    }

    LaunchedEffect(contactoId) { recargar() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contacto") },
                navigationIcon = {
                    IconButton(onClick = alVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    contacto?.let { c ->
                        if (c.telefono.isNotBlank()) {
                            IconButton(onClick = { abrirMarcador(context, c.telefono) }) {
                                Icon(Icons.Default.Call, contentDescription = "Llamar")
                            }
                        }
                        Box {
                            IconButton(onClick = { mostrarMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                                DropdownMenuItem(
                                    text = { Text("Copiar como texto") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        clipboard.setText(AnnotatedString(textoCompartir(c)))
                                        mensajeSnackbar = "Copiado al portapapeles"
                                        mostrarMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Compartir como archivo") },
                                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                    onClick = {
                                        mostrarMenu = false
                                        alcanceCorutinas.launch {
                                            val json = viewModel.exportarUnoComoJson(c.id)
                                            if (json != null) {
                                                compartirContactoComoArchivo(context, c.nombre.replace(" ", "_"), json)
                                            }
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Marcar contactado hoy") },
                                    leadingIcon = { Icon(Icons.Default.EventAvailable, contentDescription = null) },
                                    onClick = {
                                        viewModel.marcarContactadoHoy(c)
                                        mensajeSnackbar = "Marcado como contactado hoy"
                                        mostrarMenu = false
                                    }
                                )
                            }
                        }
                        IconButton(onClick = { alEditar(c.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { mostrarDialogoBorrar = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        snackbarHost = {
            mensajeSnackbar?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(1800)
                    mensajeSnackbar = null
                }
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(msg) }
            }
        }
    ) { padding ->
        val c = contacto
        if (c == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoOIniciales(c)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.nombre, style = MaterialTheme.typography.headlineSmall)
                            if (c.favorito) {
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.Star, contentDescription = "Favorito", tint = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                        if (c.ultimoContactoMillis != null) {
                            Text(
                                "Último contacto: ${formatoTiempoTranscurrido(c.ultimoContactoMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (haceMuchoQueNoContacta(c.ultimoContactoMillis))
                                    MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (c.telefono.isNotBlank()) {
                    CampoInfo(icono = Icons.Default.Phone, etiqueta = "Teléfono", valor = c.telefono)
                }

                if (c.descripcion.isNotBlank()) {
                    CampoInfo(icono = Icons.Default.Info, etiqueta = "Descripción", valor = c.descripcion)
                }

                if (c.tieneUbicacion) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    abrirEnMaps(context, c.latitud!!, c.longitud!!, c.nombre)
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (c.etiquetaUbicacion.isNotBlank()) c.etiquetaUbicacion else "Ubicación",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("Toca para abrir en Maps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Coordenadas: ${redondearTexto(c.latitud!!)}, ${redondearTexto(c.longitud!!)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString("${c.latitud},${c.longitud}"))
                                    mensajeSnackbar = "Coordenadas copiadas (funciona sin internet)"
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Copiar coordenadas", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoBorrar && contacto != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrar = false },
            title = { Text("Eliminar contacto") },
            text = { Text("¿Seguro que deseas eliminar a ${contacto!!.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminar(contacto!!)
                    mostrarDialogoBorrar = false
                    alVolver()
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoBorrar = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FotoOIniciales(c: Contacto) {
    val bitmap = remember(c.fotoUri) {
        c.fotoUri?.let { ruta ->
            try {
                val archivo = File(ruta)
                if (archivo.exists()) BitmapFactory.decodeFile(ruta) else null
            } catch (e: Exception) { null }
        }
    }
    Box(
        modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Foto de ${c.nombre}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                c.nombre.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CampoInfo(icono: androidx.compose.ui.graphics.vector.ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun redondearTexto(valor: Double): String = "%.5f".format(valor)

private fun textoCompartir(c: Contacto): String {
    val partes = mutableListOf("Contacto: ${c.nombre}")
    if (c.telefono.isNotBlank()) partes.add("Teléfono: ${c.telefono}")
    if (c.descripcion.isNotBlank()) partes.add("Descripción: ${c.descripcion}")
    if (c.tieneUbicacion) {
        partes.add("Ubicación: https://www.google.com/maps/search/?api=1&query=${c.latitud},${c.longitud}")
    }
    return partes.joinToString("\n")
}
