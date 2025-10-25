package com.plantscanner.util

object Constants {
    // LM Studio API configuration
    const val LM_STUDIO_BASE_URL = "http://192.168.1.126:1234/"
    const val LM_STUDIO_MODEL = "google/gemma-3-12b"
    
    // System prompts
    const val PLANT_ANALYSIS_PROMPT = """Проанализируй это растение на фотографии и верни ТОЛЬКО JSON (без дополнительного текста, без приветствий, без объяснений).

Формат ответа (строго JSON):
{
  "name": "Название растения на русском",
  "latinName": "Латинское название",
  "healthStatus": "здорово/требует внимания/больно",
  "healthScore": 85,
  "problems": ["проблема 1", "проблема 2"],
  "treatment": ["конкретное действие 1 с препаратами", "действие 2", "действие 3"],
  "careInstructions": {
    "watering": "Рекомендации по поливу",
    "light": "Рекомендации по освещению",
    "temperature": "Оптимальная температура",
    "fertilizer": "Рекомендации по удобрениям"
  },
  "facts": ["интересный факт 1", "интересный факт 2", "интересный факт 3"]
}

ВАЖНО ПРО ЛЕЧЕНИЕ (treatment):
- Если healthStatus = "здорово" → treatment пустой массив []
- Если healthStatus = "требует внимания" → укажи профилактические меры
- Если healthStatus = "больно" → укажи конкретное лечение с названиями препаратов (фунгициды, инсектициды и т.д.)
- Примеры: "Обработать Фитоспорином 2-3 раза с интервалом 7 дней", "Удалить пораженные листья и обработать медным купоросом 1%"

ВАЖНО: Верни ТОЛЬКО валидный JSON, без markdown, без текста до или после JSON."""
    
    // Database
    const val DATABASE_NAME = "plant_scanner_db"
    
    // Timeouts (increased for slow networks)
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 120L // seconds - for AI processing
    const val WRITE_TIMEOUT = 60L // seconds
}
