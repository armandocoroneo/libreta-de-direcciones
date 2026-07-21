package com.example.libretadirecciones.ui

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.libretadirecciones.data.Contacto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaLista(
    viewModel: ContactosViewModel,
    alSeleccionarContacto: (Long) -> Unit,
    alPresionarNuevo: () -> Unit
) {
    val contactos by viewModel.contactos.collectAsState()
    var texto by viewModel.textoBusqueda

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Libreta de Direcciones") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = alPresionarNuevo) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo contacto")
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
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (contacto.nombre.firstOrNull()?.uppercase() ?: "?"),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = contacto.nombre, fontWeight = FontWeight.Medium)
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
