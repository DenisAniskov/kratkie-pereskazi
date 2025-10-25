package com.plantscanner.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plantscanner.data.model.PlantAnalysis
import com.plantscanner.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScanState {
    object Idle : ScanState()
    object Analyzing : ScanState()
    data class Success(val analysis: PlantAnalysis) : ScanState()
    data class Error(val message: String) : ScanState()
}

class PlantScanViewModel(
    private val repository: PlantRepository
) : ViewModel() {
    
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()
    
    private val _history = MutableStateFlow<List<PlantAnalysis>>(emptyList())
    val history: StateFlow<List<PlantAnalysis>> = _history.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun analyzePlant(imageUri: Uri) {
        viewModelScope.launch {
            _scanState.value = ScanState.Analyzing
            
            val result = repository.analyzePlantImage(imageUri)
            
            _scanState.value = result.fold(
                onSuccess = { analysis -> ScanState.Success(analysis) },
                onFailure = { exception -> 
                    ScanState.Error(exception.message ?: "Неизвестная ошибка")
                }
            )
            
            // Reload history after analysis
            loadHistory()
        }
    }
    
    fun resetState() {
        _scanState.value = ScanState.Idle
    }
    
    private fun loadHistory() {
        viewModelScope.launch {
            repository.getAllAnalyses().collect { analyses ->
                _history.value = analyses
            }
        }
    }
    
    fun deleteAnalysis(analysis: PlantAnalysis) {
        viewModelScope.launch {
            repository.deleteAnalysis(analysis)
            loadHistory()
        }
    }
}
