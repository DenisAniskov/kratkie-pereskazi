package com.plantscanner.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.plantscanner.data.model.PlantAnalysis
import com.plantscanner.ui.components.PlantCard
import com.plantscanner.viewmodel.PlantScanViewModel
import com.plantscanner.viewmodel.ScanState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlantScanViewModel,
    onNavigateToResult: (PlantAnalysis) -> Unit
) {
    val context = LocalContext.current
    val scanState by viewModel.scanState.collectAsState()
    val history by viewModel.history.collectAsState()
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            viewModel.analyzePlant(imageUri!!)
        }
    }
    
    // Camera permission launcher - launch camera AFTER permission granted
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && imageUri != null) {
            // Permission granted, now launch camera
            cameraLauncher.launch(imageUri)
        }
    }
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.analyzePlant(it)
        }
    }
    
    // Handle scan state changes
    LaunchedEffect(scanState) {
        if (scanState is ScanState.Success) {
            onNavigateToResult((scanState as ScanState.Success).analysis)
            viewModel.resetState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("AI Plants Scanner")
                        Text(
                            "Создатель: Денис Аниськов",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                FloatingActionButton(
                    onClick = {
                        val photoFile = File(
                            context.cacheDir,
                            "plant_${System.currentTimeMillis()}.jpg"
                        )
                        imageUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            photoFile
                        )
                        // Request permission first, camera will launch in callback
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.CameraAlt, "Сделать фото")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                FloatingActionButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Filled.Image, "Выбрать из галереи")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = scanState) {
                is ScanState.Analyzing -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Анализирую растение...")
                        }
                    }
                }
                is ScanState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Ошибка: ${state.message}",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Проверьте, что LM Studio запущен на http://172.16.0.1:1234",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.resetState() }) {
                                Text("Попробовать снова")
                            }
                        }
                    }
                }
                else -> {
                    // Show history
                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocalFlorist,
                                    contentDescription = null,
                                    modifier = Modifier.size(120.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = "Начните сканирование растений",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Нажмите на кнопку камеры, чтобы сделать фото",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "История сканирований",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(history) { analysis ->
                                PlantCard(
                                    analysis = analysis,
                                    onClick = { onNavigateToResult(analysis) },
                                    onDelete = { viewModel.deleteAnalysis(analysis) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
