package com.plantscanner.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.plantscanner.data.model.PlantAnalysis
import com.plantscanner.ui.theme.DangerRed
import com.plantscanner.ui.theme.HealthyGreen
import com.plantscanner.ui.theme.WarningYellow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    analysis: PlantAnalysis,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результаты анализа") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Plant Image
            AsyncImage(
                model = File(analysis.imageUri),
                contentDescription = "Plant photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.padding(16.dp)) {
                // Plant Name
                Text(
                    text = analysis.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                if (analysis.latinName.isNotEmpty()) {
                    Text(
                        text = analysis.latinName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = dateFormat.format(Date(analysis.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Health Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = getHealthColor(analysis.healthStatus)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getHealthIcon(analysis.healthStatus),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Состояние",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = analysis.healthStatus,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            LinearProgressIndicator(
                                progress = analysis.healthScore / 100f,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Problems Section
                if (analysis.problems.isNotEmpty()) {
                    SectionCard(
                        title = "Обнаруженные проблемы",
                        icon = Icons.Filled.Warning
                    ) {
                        analysis.problems.forEach { problem ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Circle,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(8.dp)
                                        .padding(top = 6.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(problem, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Treatment Section
                if (analysis.treatment.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (analysis.healthStatus == "больно") {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.MedicalServices,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = if (analysis.healthStatus == "больно") {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (analysis.healthStatus == "больно") "План лечения" else "Профилактические меры",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (analysis.healthStatus == "больно") {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSecondaryContainer
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            analysis.treatment.forEachIndexed { index, step ->
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}.",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (analysis.healthStatus == "больно") {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (analysis.healthStatus == "больно") {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Care Instructions
                SectionCard(
                    title = "Рекомендации по уходу",
                    icon = Icons.Filled.Spa
                ) {
                    CareItem("Полив", Icons.Filled.WaterDrop, analysis.careInstructions.watering)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    CareItem("Освещение", Icons.Filled.WbSunny, analysis.careInstructions.light)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    CareItem("Температура", Icons.Filled.Thermostat, analysis.careInstructions.temperature)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    CareItem("Удобрения", Icons.Filled.Science, analysis.careInstructions.fertilizer)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Facts Section
                if (analysis.facts.isNotEmpty()) {
                    SectionCard(
                        title = "Интересные факты",
                        icon = Icons.Filled.Lightbulb
                    ) {
                        analysis.facts.forEach { fact ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(fact, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
fun CareItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String
) {
    if (description.isNotEmpty()) {
        Row {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun getHealthColor(status: String): androidx.compose.ui.graphics.Color {
    return when {
        status.contains("здоров", ignoreCase = true) -> HealthyGreen
        status.contains("внимани", ignoreCase = true) -> WarningYellow
        status.contains("больн", ignoreCase = true) -> DangerRed
        else -> MaterialTheme.colorScheme.primary
    }
}

fun getHealthIcon(status: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        status.contains("здоров", ignoreCase = true) -> Icons.Filled.CheckCircle
        status.contains("внимани", ignoreCase = true) -> Icons.Filled.Warning
        status.contains("больн", ignoreCase = true) -> Icons.Filled.Error
        else -> Icons.Filled.Info
    }
}
