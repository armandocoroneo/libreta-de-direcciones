package com.example.libretadirecciones.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.libretadirecciones.data.Contacto
import com.example.libretadirecciones.data.ContactoRepositorio
import com.example.libretadirecciones.util.contactosAJson
import com.example.libretadirecciones.util.jsonAContactos
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ContactosViewModel(private val repositorio: ContactoRepositorio) : ViewModel() {

    val textoBusqueda = mutableStateOf("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val contactos: StateFlow<List<Contacto>> =
        snapshotFlow { textoBusqueda.value }
            .flatMapLatest { texto ->
                if (texto.isBlank()) repositorio.obtenerTodos()
                else repositorio.buscar(texto)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun guardar(contacto: Contacto, alTerminar: () -> Unit = {}) {
        viewModelScope.launch {
            if (contacto.id == 0L) repositorio.guardar(contacto)
            else repositorio.actualizar(contacto)
            alTerminar()
        }
    }

    fun eliminar(contacto: Contacto) {
        viewModelScope.launch { repositorio.eliminar(contacto) }
    }

    fun marcarContactadoHoy(contacto: Contacto) {
        viewModelScope.launch {
            repositorio.actualizar(contacto.copy(ultimoContactoMillis = System.currentTimeMillis()))
        }
    }

    suspend fun obtenerPorId(id: Long): Contacto? = repositorio.obtenerPorId(id)

    /** Devuelve el JSON de toda la libreta, para exportar como backup. */
    suspend fun exportarTodoComoJson(): String {
        return contactosAJson(repositorio.obtenerTodosUnaVez())
    }

    /** Devuelve el JSON de un solo contacto, para compartir de forma estructurada. */
    suspend fun exportarUnoComoJson(id: Long): String? {
        val contacto = repositorio.obtenerPorId(id) ?: return null
        return contactosAJson(listOf(contacto))
    }

    /** Importa contactos desde un texto JSON (backup completo o un solo contacto compartido). */
    fun importarDesdeJson(texto: String, alTerminar: (cantidad: Int) -> Unit, alError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val nuevos = jsonAContactos(texto)
                if (nuevos.isEmpty()) {
                    alError("El archivo no tiene contactos válidos.")
                    return@launch
                }
                repositorio.importar(nuevos)
                alTerminar(nuevos.size)
            } catch (e: Exception) {
                alError("El archivo no tiene un formato válido.")
            }
        }
    }

    class Factory(private val repositorio: ContactoRepositorio) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return ContactosViewModel(repositorio) as T
        }
    }
}

// Helper simple porque snapshotFlow no está en runtime por defecto en algunos setups
private fun <T> snapshotFlow(block: () -> T): Flow<T> =
    androidx.compose.runtime.snapshotFlow(block)
