package com.tecsup.centrotareas.model

sealed interface PrimosUi {
    data object Inicial : PrimosUi
    // Mejora IA: el estado lleva el avance del cálculo (0f a 1f)
    data class Calculando(val progreso: Float = 0f) : PrimosUi
    data class Listo(val cantidad: Int, val milisegundos: Long) : PrimosUi
    data object Cancelado : PrimosUi
}

sealed interface CargaUi {
    data object Inicial : CargaUi
    data object Cargando : CargaUi
    data class Exito(
        val modo: String,
        val perfil: String,
        val pedidos: Int,
        val milisegundos: Long
    ) : CargaUi
    data class Error(val mensaje: String) : CargaUi
}

// Mejora IA: tiempos acumulados de cada modo de carga para compararlos
data class ComparacionTiempos(
    val unaPorUna: List<Long> = emptyList(),
    val aLaVez: List<Long> = emptyList()
) {
    val promedioUnaPorUna: Long? get() = unaPorUna.promedio()
    val promedioALaVez: Long? get() = aLaVez.promedio()

    /** Milisegundos que ahorra A la vez en promedio; null si falta algún modo. */
    val ahorro: Long?
        get() {
            val a = promedioUnaPorUna ?: return null
            val b = promedioALaVez ?: return null
            return a - b
        }

    private fun List<Long>.promedio(): Long? = if (isEmpty()) null else sum() / size
}
