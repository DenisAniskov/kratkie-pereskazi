package com.plantscanner.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.plantscanner.data.api.LMStudioApiService
import com.plantscanner.data.database.PlantDao
import com.plantscanner.data.model.*
import com.plantscanner.util.Constants
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class PlantRepository(
    private val plantDao: PlantDao,
    private val context: Context
) {
    private val apiService: LMStudioApiService
    
    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.LM_STUDIO_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        apiService = retrofit.create(LMStudioApiService::class.java)
    }
    
    suspend fun analyzePlantImage(imageUri: Uri): Result<PlantAnalysis> {
        return try {
            android.util.Log.d("PlantRepository", "=== Starting plant analysis ===")
            android.util.Log.d("PlantRepository", "Image URI: $imageUri")
            android.util.Log.d("PlantRepository", "LM Studio URL: ${Constants.LM_STUDIO_BASE_URL}")
            
            // Load and compress image
            android.util.Log.d("PlantRepository", "Loading image...")
            val bitmap = loadBitmapFromUri(imageUri)
            android.util.Log.d("PlantRepository", "Image size: ${bitmap.width}x${bitmap.height}")
            
            android.util.Log.d("PlantRepository", "Converting to base64...")
            val base64Image = bitmapToBase64(bitmap)
            android.util.Log.d("PlantRepository", "Base64 size: ${base64Image.length} chars")
            
            // Create request
            val request = LMStudioRequest(
                model = Constants.LM_STUDIO_MODEL,
                messages = listOf(
                    Message(
                        role = "user",
                        content = listOf(
                            ContentItem(
                                type = "text",
                                text = Constants.PLANT_ANALYSIS_PROMPT
                            ),
                            ContentItem(
                                type = "image_url",
                                image_url = ImageUrl(url = "data:image/jpeg;base64,$base64Image")
                            )
                        )
                    )
                )
            )
            
            android.util.Log.d("PlantRepository", "Sending request to LM Studio...")
            android.util.Log.d("PlantRepository", "Model: ${Constants.LM_STUDIO_MODEL}")
            
            // Call API
            val response = apiService.analyzePlant(request)
            android.util.Log.d("PlantRepository", "Got response! Tokens used: ${response.usage.total_tokens}")
            
            val analysisText = response.choices.firstOrNull()?.message?.content
                ?: throw Exception("Empty response from API")
            
            android.util.Log.d("PlantRepository", "Response text length: ${analysisText.length}")
            
            // Parse JSON response
            val analysis = parseAnalysisResponse(analysisText, imageUri)
            android.util.Log.d("PlantRepository", "Parsed plant: ${analysis.name}")
            
            // Save to database
            val id = plantDao.insertAnalysis(analysis)
            android.util.Log.d("PlantRepository", "Saved to database with ID: $id")
            
            android.util.Log.d("PlantRepository", "=== Analysis completed successfully ===")
            Result.success(analysis.copy(id = id))
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("PlantRepository", "!!! CONNECTION FAILED !!!")
            android.util.Log.e("PlantRepository", "Cannot connect to ${Constants.LM_STUDIO_BASE_URL}")
            android.util.Log.e("PlantRepository", "Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Не удалось подключиться к LM Studio на ${Constants.LM_STUDIO_BASE_URL}. Проверьте:\n1. LM Studio запущен?\n2. Телефон и ПК в одной WiFi сети?\n3. IP адрес правильный?"))
        } catch (e: java.net.SocketTimeoutException) {
            android.util.Log.e("PlantRepository", "!!! TIMEOUT !!!")
            android.util.Log.e("PlantRepository", "Request timeout to ${Constants.LM_STUDIO_BASE_URL}")
            android.util.Log.e("PlantRepository", "Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Таймаут при подключении к LM Studio. Сервер не отвечает за 120 секунд. Проверьте доступность ${Constants.LM_STUDIO_BASE_URL}"))
        } catch (e: Exception) {
            android.util.Log.e("PlantRepository", "!!! ERROR !!!")
            android.util.Log.e("PlantRepository", "Error type: ${e.javaClass.simpleName}")
            android.util.Log.e("PlantRepository", "Error message: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }
    
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open image URI")
        return BitmapFactory.decodeStream(inputStream)
    }
    
    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Resize if too large
        val maxSize = 1024
        val scaledBitmap = if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val scale = maxSize.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
        
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    
    private fun parseAnalysisResponse(jsonText: String, imageUri: Uri): PlantAnalysis {
        try {
            android.util.Log.d("PlantRepository", "Raw response text:\n$jsonText")
            
            // Try multiple strategies to extract JSON
            var cleanJson = jsonText.trim()
            
            // Strategy 1: Look for JSON in code blocks (```json or ```)
            if (cleanJson.contains("```")) {
                cleanJson = cleanJson
                    .substringAfter("```json", "")
                    .ifEmpty { cleanJson.substringAfter("```", "") }
                    .substringBefore("```")
                    .trim()
            }
            
            // Strategy 2: Find JSON by braces if no code blocks worked
            if (!cleanJson.startsWith("{")) {
                val startIndex = cleanJson.indexOf("{")
                val endIndex = cleanJson.lastIndexOf("}")
                if (startIndex >= 0 && endIndex > startIndex) {
                    cleanJson = cleanJson.substring(startIndex, endIndex + 1)
                }
            }
            
            android.util.Log.d("PlantRepository", "Extracted JSON:\n$cleanJson")
            
            val gson = Gson()
            val response = gson.fromJson(cleanJson, PlantAnalysisResponse::class.java)
            
            // Save image locally
            val savedImageUri = saveImageLocally(imageUri)
            
            return PlantAnalysis(
                name = response.name,
                latinName = response.latinName,
                healthStatus = response.healthStatus,
                healthScore = response.healthScore,
                problems = response.problems,
                treatment = response.treatment ?: emptyList(),
                careInstructions = response.careInstructions,
                facts = response.facts,
                imageUri = savedImageUri
            )
        } catch (e: Exception) {
            // Fallback: create analysis from raw text
            return PlantAnalysis(
                name = "Анализ завершен",
                latinName = "",
                healthStatus = "Информация получена",
                healthScore = 50,
                problems = emptyList(),
                careInstructions = CareInstructions(
                    watering = jsonText,
                    light = "",
                    temperature = "",
                    fertilizer = ""
                ),
                facts = emptyList(),
                imageUri = saveImageLocally(imageUri)
            )
        }
    }
    
    private fun saveImageLocally(imageUri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: return imageUri.toString()
        
        val fileName = "plant_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)
        
        FileOutputStream(file).use { output ->
            inputStream.copyTo(output)
        }
        
        return file.absolutePath
    }
    
    fun getAllAnalyses(): Flow<List<PlantAnalysis>> {
        return plantDao.getAllAnalyses()
    }
    
    suspend fun deleteAnalysis(analysis: PlantAnalysis) {
        // Delete image file
        try {
            File(analysis.imageUri).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        plantDao.deleteAnalysis(analysis)
    }
}

// Helper data class for parsing
private data class PlantAnalysisResponse(
    val name: String,
    val latinName: String,
    val healthStatus: String,
    val healthScore: Int,
    val problems: List<String>,
    val treatment: List<String>? = null,
    val careInstructions: CareInstructions,
    val facts: List<String>
)
