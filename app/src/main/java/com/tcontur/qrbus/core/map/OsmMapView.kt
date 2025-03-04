package com.tcontur.qrbus.core.map

import ApiService
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tcontur.qrbus.client.ServiceClient
import com.tcontur.qrbus.client.models.Recorrido
import com.tcontur.qrbus.client.models.Ruta
import com.tcontur.qrbus.core.login.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun OsmdroidMapView() {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()




    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp, // Se oculta cuando está colapsado
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sheet content")
                RecorridosScreen()
            }
        }
    ) { innerPadding ->
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background
//        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E88E5),
                            Color(0xFF64B5F6)
                        )
                    )
                )

        ) {


//            botones para abrir y cerrar el bottom sheet
            Box(modifier = Modifier.fillMaxSize()) {

//                Box(
//                    modifier = Modifier
//                        .padding(16.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Button(
//                        onClick = {
//                            scope.launch {
//                                scaffoldState.bottomSheetState.expand()
//                            }
//                        },
//                        modifier = Modifier.align(Alignment.BottomCenter)
//                    ) {
//                        Text("Open Bottom Sheet")
//                    }
//
//                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            val mapController = controller
                            mapController.setZoom(15.0)

                            val startPoint = GeoPoint(-12.0464, -77.0428)
                            mapController.setCenter(startPoint)


                            val marker = Marker(this).apply {
                                position = startPoint
                                title = "Lima, Perú"
                            }
                            val polyline = Polyline().apply {
                                outlinePaint.color = android.graphics.Color.MAGENTA
                                outlinePaint.strokeWidth = 5f
                                addPoint(GeoPoint(-12.0453, -77.0300))
                                addPoint(GeoPoint(-12.0600, -77.0400))
                                addPoint(GeoPoint(-12.0700, -77.0500))
                                addPoint(GeoPoint(-12.0453, -77.0300)) //
                            }

                            overlays.add(polyline)


                            overlays.add(marker)


                        }

                    }

                )
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    modifier = Modifier
                        .padding(14.dp)
                        .align(Alignment.BottomStart),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Filled.DisplaySettings, contentDescription = "Agregar marcador",
                    )
                }
            }
        }

    }

}

@Composable
fun RecorridosScreen() {
    val sm = SessionManager(LocalContext.current)
    val apiService = remember { ServiceClient.getApiService(sm) }

    var rutas by remember { mutableStateOf<List<Ruta>>(emptyList()) }
    var recorridos by remember { mutableStateOf<List<Recorrido>>(emptyList()) }
    var selectedRutaId by remember { mutableStateOf<Int?>(null) }
    var selectedRecorrido by remember { mutableStateOf<Recorrido?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Carga de datos usando LaunchedEffect
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Obtén las rutas
            apiService?.getRutas(sm.getAuthToken())?.execute()?.body()?.let { rutasResponse ->
                rutas = rutasResponse
            }
            // Obtén los recorridos
            apiService?.getRecorrido(sm.getAuthToken())?.execute()?.body()
                ?.let { recorridosResponse ->
                    recorridos = recorridosResponse
                }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Selecciona una ruta:")
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = rutas.find { it.id == selectedRutaId }?.codigo ?: "Selecciona una ruta",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.clickable { dropdownExpanded = true }
                    )
                },
            )
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                rutas.forEach { ruta ->
                    DropdownMenuItem(
                        text = { Text(ruta.codigo) },
                        onClick = {
                            selectedRutaId = ruta.id
                            dropdownExpanded = false
                            selectedRecorrido = recorridos.find { it.ruta.id == ruta.id }
                        })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { dropdownExpanded = true }) {
            Text("Seleccionar ruta")
        }

        Spacer(modifier = Modifier.height(16.dp))
        selectedRecorrido?.let { recorrido ->
            Text("Recorrido seleccionado: ${recorrido.id}")
            recorrido.trayecto.forEach { punto ->
                Text("Orden: ${punto.orden} - Lat: ${punto.latitud} - Lon: ${punto.longitud}")
            }
        }
    }
}