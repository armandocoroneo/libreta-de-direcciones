package com.example.libretadirecciones.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.libretadirecciones.data.Contacto

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
    var contacto by remember { mutableStateOf<Contacto?>(null) }
    var mostrarDialogoBorrar by remember { mutableStateOf(false) }
    var mostrarConfirmacionCopiado by remember { mutableStateOf(false) }

    LaunchedEffect(contactoId) {
        contacto = viewModel.obtenerPorId(contactoId)
    }

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
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(textoCompartir(c)))
                            mostrarConfirmacionCopiado = true
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
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
                    Text(c.nombre, style = MaterialTheme.typography.headlineSmall)
                    if (c.favorito) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = "Favorito", tint = MaterialTheme.colorScheme.tertiary)
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
                            .clickable { abrirEnMaps(context, c.latitud!!, c.longitud!!, c.nombre) }
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    if (c.etiquetaUbicacion.isNotBlank()) c.etiquetaUbicacion else "Ubicación",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text("Toca para abrir en Maps", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarConfirmacionCopiado) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            mostrarConfirmacionCopiado = false
        }
        Snackbar(modifier = Modifier.padding(16.dp)) { Text("Copiado al portapapeles") }
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

private fun textoCompartir(c: Contacto): String {
    val partes = mutableListOf("Contacto: ${c.nombre}")
    if (c.telefono.isNotBlank()) partes.add("Teléfono: ${c.telefono}")
    if (c.descripcion.isNotBlank()) partes.add("Descripción: ${c.descripcion}")
    if (c.tieneUbicacion) {
        partes.add("Ubicación: https://www.google.com/maps/search/?api=1&query=${c.latitud},${c.longitud}")
    }
    return partes.joinToString("\n")
}
