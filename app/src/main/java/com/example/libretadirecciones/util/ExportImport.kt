package com.example.libretadirecciones.util

import com.example.libretadirecciones.data.Contacto
import org.json.JSONArray
import org.json.JSONObject

/**
 * Convierte una lista de contactos a un texto JSON (para exportar backups
 * o compartir un contacto de forma estructurada, no solo como texto plano).
 */
fun contactosAJson(contactos: List<Contacto>): String {
    val arreglo = JSONArray()
    contactos.forEach { c ->
        val obj = JSONObject()
        obj.put("nombre", c.nombre)
        obj.put("descripcion", c.descripcion)
        obj.put("telefono", c.telefono)
        obj.put("latitud", c.latitud ?: JSONObject.NULL)
        obj.put("longitud", c.longitud ?: JSONObject.NULL)
        obj.put("etiquetaUbicacion", c.etiquetaUbicacion)
        obj.put("favorito", c.favorito)
        arreglo.put(obj)
    }
    val raiz = JSONObject()
    raiz.put("app", "LibretaDeDirecciones")
    raiz.put("version", 1)
    raiz.put("contactos", arreglo)
    return raiz.toString(2)
}

/**
 * Lee un texto JSON (generado por esta misma app) y devuelve la lista de
 * contactos que contiene. Ignora campos desconocidos y no falla si faltan
 * campos opcionales.
 */
fun jsonAContactos(texto: String): List<Contacto> {
    val raiz = JSONObject(texto)
    val arreglo = raiz.optJSONArray("contactos") ?: JSONArray(texto) // admite también un array suelto
    val resultado = mutableListOf<Contacto>()
    for (i in 0 until arreglo.length()) {
        val obj = arreglo.getJSONObject(i)
        val nombre = obj.optString("nombre") 
        if (nombre.isBlank()) continue
        resultado.add(
            Contacto(
                nombre = nombre,
                descripcion = obj.optString("descripcion", ""),
                telefono = obj.optString("telefono", ""),
                latitud = if (obj.isNull("latitud")) null else obj.optDouble("latitud"),
                longitud = if (obj.isNull("longitud")) null else obj.optDouble("longitud"),
                etiquetaUbicacion = obj.optString("etiquetaUbicacion", ""),
                favorito = obj.optBoolean("favorito", false)
            )
        )
    }
    return resultado
}
