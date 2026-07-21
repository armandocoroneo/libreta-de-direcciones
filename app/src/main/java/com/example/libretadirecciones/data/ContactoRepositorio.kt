package com.example.libretadirecciones.data

import kotlinx.coroutines.flow.Flow

class ContactoRepositorio(private val dao: ContactoDao) {

    fun obtenerTodos(): Flow<List<Contacto>> = dao.obtenerTodos()

    fun buscar(texto: String): Flow<List<Contacto>> = dao.buscar(texto)

    suspend fun obtenerPorId(id: Long): Contacto? = dao.obtenerPorId(id)

    suspend fun guardar(contacto: Contacto): Long = dao.insertar(contacto)

    suspend fun actualizar(contacto: Contacto) = dao.actualizar(contacto)

    suspend fun eliminar(contacto: Contacto) = dao.eliminar(contacto)
}
