package com.tecsup.centrotareas.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TareasRepositoryLocalTest {

    private val repo = TareasRepositoryLocal()

    @Test
    fun contarPrimos_coincideConLosValoresDeReferencia() = runTest {
        assertEquals(25, repo.contarPrimos(100))
        assertEquals(168, repo.contarPrimos(1000))
        assertEquals(1229, repo.contarPrimos(10_000))
        assertEquals(9592, repo.contarPrimos(100_000))
        assertEquals(41_538, repo.contarPrimos(500_000))
    }
}
