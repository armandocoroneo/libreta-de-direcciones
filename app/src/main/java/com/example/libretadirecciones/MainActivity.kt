package com.example.libretadirecciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.libretadirecciones.data.AppDatabase
import com.example.libretadirecciones.data.ContactoRepositorio
import com.example.libretadirecciones.ui.ContactosViewModel
import com.example.libretadirecciones.ui.NavegacionApp
import com.example.libretadirecciones.ui.theme.LibretaDireccionesTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ContactosViewModel by viewModels {
        val dao = AppDatabase.obtenerInstancia(applicationContext).contactoDao()
        ContactosViewModel.Factory(ContactoRepositorio(dao))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LibretaDireccionesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavegacionApp(viewModel = viewModel)
                }
            }
        }
    }
}
