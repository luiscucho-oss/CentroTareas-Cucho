package com.tecsup.centrotareas.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class TareasRepositoryLocal : TareasRepository {

    // Mejora IA: el trabajo simulado vive en el Repository y el ViewModel solo lo recolecta.
    // flowOn cambia el hilo de todo lo que está arriba (el bloque flow) a Dispatchers.Default;
    // el collect sigue en el hilo de quien recolecta (main).
    override fun tareaConProgreso(): Flow<Float> = flow {
        Log.d("HILO", "Dentro: ${Thread.currentThread().name}")
        for (i in 1..100) {
            delay(40) // delay revisa la cancelación en cada vuelta
            emit(i / 100f)
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }

    // Paso 5: cálculo pesado en Dispatchers.Default (hilos pensados para CPU)
    override suspend fun contarPrimos(
        hasta: Int,
        alAvanzar: (Float) -> Unit
    ): Int = withContext(Dispatchers.Default) {
        var cantidad = 0
        // Mejora IA: se avisa cada 1 % para no saturar la UI con millones de actualizaciones
        val paso = maxOf(1, hasta / 100)
        for (n in 2..hasta) {
            ensureActive() // si cancelaron el Job, se detiene aquí
            if (esPrimo(n)) cantidad++
            if (n % paso == 0) alAvanzar(n.toFloat() / hasta)
        }
        alAvanzar(1f)
        cantidad
    }

    private fun esPrimo(n: Int): Boolean {
        if (n < 2) return false
        var d = 2
        // d * d <= n en Long para evitar desbordes cerca de Int.MAX_VALUE
        while (d.toLong() * d <= n) {
            if (n % d == 0) return false
            d++
        }
        return true
    }
}
