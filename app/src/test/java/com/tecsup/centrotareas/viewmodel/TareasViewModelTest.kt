package com.tecsup.centrotareas.viewmodel

import com.tecsup.centrotareas.data.TareasRepository
import com.tecsup.centrotareas.data.TareasRepositoryLocal
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.PrimosUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/** Repository falso con los mismos tiempos que el real, pero en tiempo virtual. */
private class RepoFalso : TareasRepository {
    override fun tareaConProgreso(): Flow<Float> = flow {
        for (i in 1..100) {
            delay(40)
            emit(i / 100f)
        }
    }

    override suspend fun contarPrimos(hasta: Int, alAvanzar: (Float) -> Unit): Int = 0

    override suspend fun cargarPerfil(): String {
        delay(2000)
        return "Ana Torres (ficticia)"
    }

    override suspend fun cargarPedidos(simularError: Boolean): List<String> {
        delay(3000)
        if (simularError) throw IOException("Sin conexión")
        return listOf("Pedido 101", "Pedido 102", "Pedido 103")
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TareasViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun preparar() = Dispatchers.setMain(dispatcher)

    @After
    fun limpiar() = Dispatchers.resetMain()

    @Test
    fun tareaLarga_llegaAl100YTermina() = runTest(dispatcher) {
        val vm = TareasViewModel(RepoFalso())
        vm.tareaLarga()
        advanceTimeBy(2001)
        assertEquals(0.5f, vm.progreso.value, 0.01f)
        assertTrue(vm.corriendo.value)
        advanceUntilIdle()
        assertEquals(1f, vm.progreso.value)
        assertEquals("Terminó", vm.mensaje.value)
        assertFalse(vm.corriendo.value)
        assertEquals(4000, currentTime)
    }

    @Test
    fun cancelar_detieneLaBarraYPermiteIniciarOtraVez() = runTest(dispatcher) {
        val vm = TareasViewModel(RepoFalso())
        vm.tareaLarga()
        advanceTimeBy(1601)
        vm.cancelar()
        advanceUntilIdle()
        assertEquals("Cancelado", vm.mensaje.value)
        assertEquals(0.4f, vm.progreso.value, 0.01f)
        assertFalse(vm.corriendo.value)

        vm.tareaLarga()
        advanceTimeBy(1)
        assertEquals(0f, vm.progreso.value) // empieza de cero
        assertTrue(vm.corriendo.value)
    }

    @Test
    fun cargarUnaPorUna_tarda5Segundos() = runTest(dispatcher) {
        val vm = TareasViewModel(RepoFalso())
        vm.cargarUnaPorUna()
        advanceUntilIdle()
        val exito = vm.carga.value as CargaUi.Exito
        assertEquals("Una por una", exito.modo)
        assertEquals(3, exito.pedidos)
        assertEquals(5000, currentTime)
    }

    @Test
    fun cargarALaVez_tarda3Segundos() = runTest(dispatcher) {
        val vm = TareasViewModel(RepoFalso())
        vm.cargarALaVez()
        advanceUntilIdle()
        val exito = vm.carga.value as CargaUi.Exito
        assertEquals("A la vez", exito.modo)
        assertEquals("Ana Torres (ficticia)", exito.perfil)
        assertEquals(3000, currentTime)
    }

    @Test
    fun errorSimulado_yReintentarSinError_daExito() = runTest(dispatcher) {
        val vm = TareasViewModel(RepoFalso())
        vm.cambiarSimularError(true)
        vm.cargarALaVez()
        advanceUntilIdle()
        assertEquals(CargaUi.Error("No se pudo cargar: Sin conexión"), vm.carga.value)

        vm.cambiarSimularError(false)
        vm.reintentar()
        advanceUntilIdle()
        assertTrue(vm.carga.value is CargaUi.Exito)
    }

    @Test
    fun tiempoMaximo_cortaLaCargaALos2Segundos() = runTest(dispatcher) {
        val vm = TareasViewModel(RepoFalso())
        vm.cambiarLimitarTiempo(true)
        vm.cargarALaVez()
        advanceUntilIdle()
        val error = vm.carga.value as CargaUi.Error
        assertTrue(error.mensaje.startsWith("Tardó demasiado"))
        assertEquals(2000, currentTime)
    }

    @Test
    fun comparacion_acumulaLosTiemposDeCadaModo() = runTest(dispatcher) {
        val vm = TareasViewModel(RepoFalso())
        vm.cargarUnaPorUna(); advanceUntilIdle()
        vm.cargarALaVez(); advanceUntilIdle()
        vm.cargarALaVez(); advanceUntilIdle()
        val c = vm.comparacion.value
        assertEquals(1, c.unaPorUna.size)
        assertEquals(2, c.aLaVez.size)
    }

    @Test
    fun contarPrimos_realHasta1000_da168() = runTest(dispatcher) {
        val vm = TareasViewModel(TareasRepositoryLocal())
        vm.contar(1000)
        // El cálculo real corre en Dispatchers.Default: se espera en tiempo real
        withContext(Dispatchers.Default) {
            vm.primos.first { it is PrimosUi.Listo }
        }
        assertEquals(168, (vm.primos.value as PrimosUi.Listo).cantidad)
    }

    @Test
    fun cancelarPrimos_conNGrande_quedaCancelado() = runTest(dispatcher) {
        val vm = TareasViewModel(TareasRepositoryLocal())
        vm.contar(Int.MAX_VALUE)
        advanceTimeBy(1)
        assertTrue(vm.primos.value is PrimosUi.Calculando)
        vm.cancelarPrimos()
        withContext(Dispatchers.Default) {
            vm.primos.first { it == PrimosUi.Cancelado }
        }
    }
}
