package com.tcontur.qrbus.core.map

import ApiService
import android.graphics.DashPathEffect
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import android.graphics.Color as AndroidColor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tcontur.qrbus.client.ServiceClient
import com.tcontur.qrbus.client.models.Recorrido
import com.tcontur.qrbus.client.models.Ruta
import com.tcontur.qrbus.core.login.SessionManager
import com.tcontur.qrbus.core.models.AppRoute
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalMaterial3Api::class)
//@Preview(showBackground = true)
@Composable
fun OsmdroidMapView(navController: NavController) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedRecorrido by remember { mutableStateOf<Recorrido?>(null) }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RecorridosScreen(navController,
                    selectedRecorrido = selectedRecorrido,
                    onRecorridoSelected = {
                        selectedRecorrido = it
                    }
                )
            }
        }
    ) { innerPadding ->
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
            Box(modifier = Modifier.fillMaxSize()) {
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
                            overlays.add(marker)

//                            selectedRecorrido?.let { recorrido ->
//                                Log.e("Recorrido", "ID: ${recorrido.id} - Ruta: ${recorrido.ruta.codigo} - Lado: ${recorrido.lado}")
//                                val polyline = Polyline().apply {
//                                    outlinePaint.color = android.graphics.Color.BLUE
//                                    outlinePaint.strokeWidth = 5f
//                                    // Agrega cada punto del trayecto
//                                    recorrido.trayecto.forEach { punto ->
//                                        addPoint(GeoPoint(punto.latitud, punto.longitud))
//                                        Log.e("Punto", "Lat: ${punto.latitud} - Lon: ${punto.longitud}")
//                                    }
//                                }
//                                overlays.add(polyline)
//                            }
//                            overlays.add(polyline)

                            invalidate()


                        }

                    },
                    update = { mapView ->
                        // Limpia los overlays anteriores si es necesario (excepto el marcador base)
                        // Puedes hacer un filtrado o remover todos los overlays y volver a agregarlos
                        mapView.overlays.removeAll { it is Polyline }

                        // Si existe un recorrido seleccionado, dibuja el polyline
                        selectedRecorrido?.let { recorrido ->
                            // Aquí agregamos un log para ver que se actualiza
                            Log.d("MapView", "Dibujando recorrido: ${recorrido.id}")

                            val polyline = Polyline().apply {
                                outlinePaint.color =
                                    if (recorrido.lado) AndroidColor.parseColor("#0ea5e9") else AndroidColor.parseColor("#4f46e5")
                                outlinePaint.strokeWidth = 14f
                                    android.graphics.Shader.TileMode.CLAMP
                                outlinePaint.isAntiAlias = true
                                outlinePaint.alpha = 255
                                recorrido.trayecto.forEach { punto ->
                                    addPoint(GeoPoint(punto.latitud, punto.longitud))
                                }
                            }
                            mapView.overlays.add(polyline)

                            if (recorrido.trayecto.isNotEmpty()) {
                                val latitudes = recorrido.trayecto.map { it.latitud }
                                val longitudes = recorrido.trayecto.map { it.longitud }
                                val north = latitudes.maxOrNull() ?: -12.0464
                                val south = latitudes.minOrNull() ?: -12.0464
                                val east = longitudes.maxOrNull() ?: -77.0428
                                val west = longitudes.minOrNull() ?: -77.0428

                                val boundingBox = BoundingBox(north, east, south, west)
                                // Ajusta el zoom y centra el mapa para mostrar todo el recorrido
//                                mapView.controller.setCenter(
//                                    GeoPoint(
//                                        boundingBox.centerLatitude,
//                                        boundingBox.centerLongitude
//                                    )
//                                )
//                               calcular largo de la diagonal del bounding box para ajustar el zoom
                                mapView.zoomToBoundingBox(boundingBox, true, 50)
//                                mapView.controller.zoomTo(
//                                    mapView.controller.
//                                )


                                // Opcional: centra en el centro del bounding box
                                mapView.controller.animateTo(boundingBox.center)
                            }
                        }
                        mapView.invalidate()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorridosScreen(
    navController: NavController,
    selectedRecorrido: Recorrido?,
    onRecorridoSelected: (Recorrido?) -> Unit
) {
    val sm = SessionManager(LocalContext.current)
    val apiService = remember { ServiceClient.getApiService(sm) }

    var rutas by remember { mutableStateOf<List<Ruta>>(emptyList()) }
    var recorridos by remember { mutableStateOf<List<Recorrido>>(emptyList()) }
    var selectedRutaId by remember { mutableStateOf<Int?>(null) }
//    var selectedRecorrido by remember { mutableStateOf<Recorrido?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var selectedLado by remember { mutableStateOf<Boolean?>(true) }
    var dropdownLadoExpanded by remember { mutableStateOf(false) }

    // Carga de datos usando LaunchedEffect
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Obtén las rutas
            val rutasCall = apiService?.getRutas(sm.getAuthToken())
            val rutasResponse = rutasCall?.execute()
            if (rutasResponse?.code() == 401) {
                withContext(Dispatchers.Main) {
                    // Redirige al login
                    navController.navigate(AppRoute.LoginRoute)
                }
                return@withContext
            } else {
                rutasResponse?.body()?.let { rutasResponseBody ->
                    rutas = rutasResponseBody
                }
            }

            // Obtén los recorridos
            val recorridosCall = apiService?.getRecorrido(sm.getAuthToken())
            val recorridosResponse = recorridosCall?.execute()
            if (recorridosResponse?.code() == 401) {
                withContext(Dispatchers.Main) {
                    // Redirige al login
//                    navigateToLogin()
                    navController.navigate(AppRoute.LoginRoute)
                }
                return@withContext
            } else {
                recorridosResponse?.body()?.let { recorridosResponseBody ->
                    recorridos = recorridosResponseBody
                }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        selectedRecorrido?.let { recorrido ->
            Text("Recorrido seleccionado: ${recorrido.id} - ${recorrido.ruta.codigo} - ${recorrido.lado}")
//            recorrido.trayecto.forEach { punto ->
//                Text("Orden: ${punto.orden} - Lat: ${punto.latitud} - Lon: ${punto.longitud}")
//            }
        }

        Text("Selecciona una ruta:")
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it },
            ) {
                TextField(
                    value = rutas.find { it.id == selectedRutaId }?.codigo ?: "Selecciona una ruta",
                    onValueChange = {},
                    readOnly = true,
//                    modifier = Modifier.fillMaxWidth(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
//                        modifier = Modifier.clickable { dropdownExpanded = true }
                        )
                    },
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rutas.forEach { ruta ->
                        DropdownMenuItem(
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            text = { Text(ruta.codigo) },
                            onClick = {
                                selectedRutaId = ruta.id
                                dropdownExpanded = false
                                onRecorridoSelected(
                                    recorridos.find {
                                        it.ruta.id == ruta.id && (selectedLado?.let { lado -> it.lado == lado }
                                            ?: true)
                                    }
                                )
                            })
                    }
                }
            }

        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Selecciona un lado:")
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = when (selectedLado) {
                    true -> "Lado A"
                    false -> "Lado B"
                    else -> "Selecciona un lado"
                },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown lado",
                        modifier = Modifier.clickable { dropdownLadoExpanded = true }
                    )
                }
            )
            DropdownMenu(
                expanded = dropdownLadoExpanded,
                onDismissRequest = { dropdownLadoExpanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                DropdownMenuItem(
                    text = { Text("Lado A") },
                    onClick = {
                        selectedLado = true
                        dropdownLadoExpanded = false
                        // Filtra el recorrido por la ruta ya seleccionada y el lado true
                        onRecorridoSelected(
                            recorridos.find {
                                it.ruta.id == selectedRutaId && it.lado == true
                            }
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Lado B") },
                    onClick = {
                        selectedLado = false
                        dropdownLadoExpanded = false
                        // Filtra el recorrido por la ruta ya seleccionada y el lado false
                        onRecorridoSelected(
                            recorridos.find {
                                it.ruta.id == selectedRutaId && it.lado == false
                            }
                        )
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))


    }
}