package com.example.libretadirecciones.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

fun abrirEnMaps(context: Context, latitud: Double, longitud: Double, etiqueta: String) {
    val etiquetaCodificada = Uri.encode(etiqueta.ifBlank { "Ubicación" })
    val uri = "geo:$latitud,$longitud?q=$latitud,$longitud($etiquetaCodificada)".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        // Alternativa si no hay ninguna app que maneje geo: (poco común)
        val intentWeb = Intent(
            Intent.ACTION_VIEW,
            "https://www.google.com/maps/search/?api=1&query=$latitud,$longitud".toUri()
        )
        context.startActivity(intentWeb)
    }
}
