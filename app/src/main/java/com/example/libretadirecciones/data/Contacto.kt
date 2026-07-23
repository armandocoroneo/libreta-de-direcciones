package com.example.libretadirecciones.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contactos")
data class Contacto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val descripcion: String = "",
    val telefono: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val etiquetaUbicacion: String = "", // ej: "Casa", "Oficina", texto libre opcional
    val favorito: Boolean = false,
    val fotoUri: String? = null, // ruta interna del archivo de foto, si tiene
    val ultimoContactoMillis: Long? = null // fecha (epoch millis) en que se marcó "contactado"
) {
    val tieneUbicacion: Boolean
        get() = latitud != null && longitud != null
}
