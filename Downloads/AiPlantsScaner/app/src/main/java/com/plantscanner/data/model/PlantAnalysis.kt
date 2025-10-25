package com.plantscanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "plant_analyses")
data class PlantAnalysis(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latinName: String,
    val healthStatus: String,
    val healthScore: Int,
    val problems: List<String>,
    val treatment: List<String> = emptyList(),
    val careInstructions: CareInstructions,
    val facts: List<String>,
    val imageUri: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CareInstructions(
    val watering: String,
    val light: String,
    val temperature: String,
    val fertilizer: String
)

// API Response models
data class LMStudioRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 2000,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: Any // Can be string or array of content items
)

data class ContentItem(
    val type: String, // "text" or "image_url"
    val text: String? = null,
    val image_url: ImageUrl? = null
)

data class ImageUrl(
    val url: String // Base64 encoded image with data URL prefix
)

data class LMStudioResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val message: MessageResponse,
    val finish_reason: String
)

data class MessageResponse(
    val role: String,
    val content: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

// Type converters for Room
class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromCareInstructions(value: CareInstructions): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toCareInstructions(value: String): CareInstructions {
        return gson.fromJson(value, CareInstructions::class.java)
    }
}
