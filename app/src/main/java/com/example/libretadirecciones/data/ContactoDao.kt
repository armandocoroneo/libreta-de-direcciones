package com.example.libretadirecciones.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Query("SELECT * FROM contactos ORDER BY favorito DESC, nombre ASC")
    fun obtenerTodos(): Flow<List<Contacto>>

    @Query("SELECT * FROM contactos ORDER BY nombre ASC")
    suspend fun obtenerTodosUnaVez(): List<Contacto>

    @Query("""
        SELECT * FROM contactos 
        WHERE nombre LIKE '%' || :texto || '%' 
           OR descripcion LIKE '%' || :texto || '%' 
           OR telefono LIKE '%' || :texto || '%'
        ORDER BY favorito DESC, nombre ASC
    """)
    fun buscar(texto: String): Flow<List<Contacto>>

    @Query("SELECT * FROM contactos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Contacto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(contacto: Contacto): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(contactos: List<Contacto>)

    @Update
    suspend fun actualizar(contacto: Contacto)

    @Delete
    suspend fun eliminar(contacto: Contacto)
}
