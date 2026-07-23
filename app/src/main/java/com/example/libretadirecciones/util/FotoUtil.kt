package com.example.libretadirecciones.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * Copia la imagen seleccionada por el usuario (desde galería o cámara) a la
 * carpeta interna de la app, para que la foto siga disponible aunque el
 * usuario borre o mueva el archivo original. Devuelve la ruta absoluta del
 * archivo copiado, o null si falló.
 */
fun copiarFotoAAlmacenamientoInterno(context: Context, uriOrigen: Uri): String? {
    return try {
        val carpeta = File(context.filesDir, "fotos_contactos")
        if (!carpeta.exists()) carpeta.mkdirs()
        val archivoDestino = File(carpeta, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uriOrigen)?.use { entrada ->
            archivoDestino.outputStream().use { salida ->
                entrada.copyTo(salida)
            }
        }
        archivoDestino.absolutePath
    } catch (e: Exception) {
        null
    }
}

fun borrarFotoSiExiste(rutaFoto: String?) {
    if (rutaFoto.isNullOrBlank()) return
    try {
        File(rutaFoto).let { if (it.exists()) it.delete() }
    } catch (_: Exception) {
        // no crítico
    }
}
