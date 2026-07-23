package com.example.libretadirecciones.util

import java.util.concurrent.TimeUnit

/** Umbral a partir del cual consideramos que "hace mucho no hablas" con alguien. */
const val UMBRAL_OLVIDADO_DIAS = 180L // ~6 meses

fun formatoTiempoTranscurrido(millis: Long): String {
    val diffMillis = System.currentTimeMillis() - millis
    val dias = TimeUnit.MILLISECONDS.toDays(diffMillis)
    return when {
        dias <= 0 -> "hoy"
        dias == 1L -> "hace 1 día"
        dias < 30 -> "hace $dias días"
        dias < 60 -> "hace 1 mes"
        dias < 365 -> "hace ${dias / 30} meses"
        dias < 730 -> "hace 1 año"
        else -> "hace ${dias / 365} años"
    }
}

fun haceMuchoQueNoContacta(ultimoContactoMillis: Long?): Boolean {
    if (ultimoContactoMillis == null) return false
    val dias = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - ultimoContactoMillis)
    return dias >= UMBRAL_OLVIDADO_DIAS
}
