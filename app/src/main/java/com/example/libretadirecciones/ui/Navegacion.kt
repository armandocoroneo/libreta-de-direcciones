package com.example.libretadirecciones.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

private const val RUTA_LISTA = "lista"
private const val RUTA_VER = "ver/{contactoId}"
private const val RUTA_DETALLE = "detalle/{contactoId}"

@Composable
fun NavegacionApp(viewModel: ContactosViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = RUTA_LISTA) {
        composable(RUTA_LISTA) {
            PantallaLista(
                viewModel = viewModel,
                alSeleccionarContacto = { id -> navController.navigate("ver/$id") },
                alPresionarNuevo = { navController.navigate("detalle/0") }
            )
        }
        composable(
            route = RUTA_VER,
            arguments = listOf(navArgument("contactoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val contactoId = backStackEntry.arguments?.getLong("contactoId") ?: 0L
            PantallaVerContacto(
                contactoId = contactoId,
                viewModel = viewModel,
                alEditar = { id -> navController.navigate("detalle/$id") },
                alVolver = { navController.popBackStack() }
            )
        }
        composable(
            route = RUTA_DETALLE,
            arguments = listOf(navArgument("contactoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val contactoId = backStackEntry.arguments?.getLong("contactoId") ?: 0L
            PantallaDetalle(
                contactoId = contactoId,
                viewModel = viewModel,
                alGuardar = { navController.popBackStack() },
                alCancelar = { navController.popBackStack() }
            )
        }
    }
}
