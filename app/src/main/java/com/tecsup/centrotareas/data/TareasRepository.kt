package com.tecsup.centrotareas.data

import kotlinx.coroutines.flow.Flow

interface TareasRepository {
    /** Tarea simulada de unos 4 s que emite su avance de 0f a 1f. */
    fun tareaConProgreso(): Flow<Float>
    /** Cuenta los primos de 2 a [hasta]; avisa el avance (0f a 1f) en [alAvanzar]. */
    suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit = {}): Int
    suspend fun cargarPerfil(): String
    suspend fun cargarPedidos(simularError: Boolean): List<String>
}
