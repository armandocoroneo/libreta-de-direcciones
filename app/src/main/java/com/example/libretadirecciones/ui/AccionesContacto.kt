package com.example.libretadirecciones.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun abrirMarcador(context: Context, telefono: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(telefono)}"))
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    }
}

/**
 * Guarda el JSON en un archivo temporal y abre el selector de apps de
 * Android para compartirlo (WhatsApp, Gmail, Bluetooth, etc.). El archivo
 * recibido por la otra persona se puede volver a importar en la app con
 * los mismos campos (no es solo texto plano).
 */
fun compartirContactoComoArchivo(context: Context, nombreArchivo: String, json: String) {
    val carpeta = File(context.cacheDir, "compartidos")
    if (!carpeta.exists()) carpeta.mkdirs()
    val archivo = File(carpeta, "$nombreArchivo.json")
    archivo.writeText(json)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir contacto"))
}
