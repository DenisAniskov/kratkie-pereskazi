package com.plantscanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.plantscanner.data.repository.PlantRepository
import com.plantscanner.viewmodel.PlantScanViewModel

class PlantScanViewModelFactory(
    private val repository: PlantRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlantScanViewModel::class.java)) {
            return PlantScanViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
