package com.tcontur.qrbus.client.viewmodels

import ApiService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.qrbus.client.models.Recorrido
import com.tcontur.qrbus.client.models.Ruta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
//
//
//class RutasViewModel(private val apiService: ApiService) : ViewModel() {
//
//    private val _rutas = MutableStateFlow<List<Ruta>>(emptyList())
//    val rutas: StateFlow<List<Ruta>> get() = _rutas
//
//    private val _recorridos = MutableStateFlow<List<Recorrido>>(emptyList())
//    val recorridos: StateFlow<List<Recorrido>> get() = _recorridos
//
//    private val _selectedRuta = MutableStateFlow<Ruta?>(null)
//    val selectedRuta: StateFlow<Ruta?> get() = _selectedRuta
//
//    fun fetchRutas(authToken: String) {
//        viewModelScope.launch {
//            try {
//                _rutas.value = apiService.getRutas(authToken)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    fun fetchRecorridos(authToken: String) {
//        viewModelScope.launch {
//            try {
//                _recorridos.value = apiService.getRecorridos(authToken)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    fun setSelectedRuta(ruta: Ruta) {
//        _selectedRuta.value = ruta
//    }
//}