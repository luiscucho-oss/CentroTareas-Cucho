package com.tecsup.centrotareas.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tecsup.centrotareas.model.CargaUi
import com.tecsup.centrotareas.model.PrimosUi
import com.tecsup.centrotareas.ui.theme.Verde
import com.tecsup.centrotareas.ui.theme.VerdeClaro
import com.tecsup.centrotareas.viewmodel.TareasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaTareas(vm: TareasViewModel) {
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()
    val progreso by vm.progreso.collectAsStateWithLifecycle()
    val corriendo by vm.corriendo.collectAsStateWithLifecycle()
    // Prueba de vida: si deja de sumar, la pantalla está congelada
    var toques by rememberSaveable { mutableIntStateOf(0) }
    val primos by vm.primos.collectAsStateWithLifecycle()
    val carga by vm.carga.collectAsStateWithLifecycle()
    val simularError by vm.simularError.collectAsStateWithLifecycle()
    var textoN by rememberSaveable { mutableStateOf("$N_POR_DEFECTO") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CentroTareas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Tarjeta("Tarea con progreso") {
                Button(onClick = { toques++ }) { Text("Tócame: $toques") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { vm.tareaLarga() }, enabled = !corriendo) {
                        Text("Iniciar")
                    }
                    OutlinedButton(onClick = { vm.cancelar() }, enabled = corriendo) {
                        Text("Cancelar")
                    }
                }
                LinearProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("${(progreso * 100).toInt()} %")
                Text(mensaje, style = MaterialTheme.typography.bodyLarge)
            }
            Tarjeta("Contar números primos") {
                val calculando = primos is PrimosUi.Calculando
                // Se ignoran textos no numéricos o menores que 2
                val n = textoN.toIntOrNull()?.takeIf { it >= 2 }
                OutlinedTextField(
                    value = textoN,
                    onValueChange = { textoN = it.filter(Char::isDigit).take(9) },
                    label = { Text("Contar primos hasta N") },
                    singleLine = true,
                    enabled = !calculando,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { n?.let(vm::contar) },
                        enabled = !calculando && n != null
                    ) { Text("Contar") }
                    OutlinedButton(onClick = { vm.cancelarPrimos() }, enabled = calculando) {
                        Text("Cancelar")
                    }
                }
                when (val p = primos) {
                    PrimosUi.Inicial -> Text("Escribe N y pulsa Contar")
                    is PrimosUi.Calculando -> {
                        LinearProgressIndicator(
                            progress = { p.progreso },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Calculando... ${(p.progreso * 100).toInt()} %")
                    }
                    PrimosUi.Cancelado -> Text("Cancelado")
                    is PrimosUi.Listo -> Recuadro(
                        "Hay ${p.cantidad} números primos\nTiempo: ${p.milisegundos} ms",
                        fondo = VerdeClaro,
                        texto = Verde
                    )
                }
            }
            Tarjeta("Dos cargas") {
                val cargando = carga is CargaUi.Cargando
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { vm.cargarUnaPorUna() }, enabled = !cargando) {
                        Text("Una por una")
                    }
                    Button(onClick = { vm.cargarALaVez() }, enabled = !cargando) {
                        Text("A la vez")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = simularError,
                        onCheckedChange = vm::cambiarSimularError,
                        enabled = !cargando
                    )
                    Text("Simular error")
                }
                when (val c = carga) {
                    CargaUi.Inicial -> Text("Elige cómo cargar el perfil y los pedidos")
                    CargaUi.Cargando -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Cargando...")
                    }
                    is CargaUi.Exito -> Recuadro(
                        "Modo: ${c.modo}\nPerfil: ${c.perfil}\n" +
                            "Pedidos: ${c.pedidos}\nTiempo: ${c.milisegundos} ms",
                        fondo = VerdeClaro,
                        texto = Verde
                    )
                    is CargaUi.Error -> Recuadro(
                        c.mensaje,
                        fondo = MaterialTheme.colorScheme.errorContainer,
                        texto = MaterialTheme.colorScheme.onErrorContainer
                    ) {
                        Button(
                            onClick = { vm.reintentar() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun Tarjeta(titulo: String, contenido: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            contenido()
        }
    }
}

// Ajustado para que el cálculo tarde entre 2 y 5 s en el emulador
private const val N_POR_DEFECTO = 500_000

@Composable
private fun Recuadro(
    mensaje: String,
    fondo: Color,
    texto: Color,
    acciones: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(fondo, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(mensaje, color = texto, fontWeight = FontWeight.Medium)
        acciones()
    }
}
