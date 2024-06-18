package com.dastanapps.visionai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject

private const val GEMINI_MODEL_FLASH="gemini-1.5-flash-latest"

private val config = generationConfig {
    temperature = 0.7f
}

val getExchangeRate = defineFunction(
    name = "getExchangeRate",
    description = "Get the exchange rate for currencies between countries",
    Schema.str("currencyFrom", "The currency to convert from."),
    Schema.str("currencyTo", "The currency to convert to.")
) { from, to ->
    makeApiRequest(from, to)
}

val saveFile = defineFunction(
    name = "saveFile",
    description = "Save file in local storage if not category avaialble then save file.txt",
    Schema.str("content", "content to save"),
    Schema.str("category", "The category of the file."),
) { content, category ->
    val _category = category.replace(" ", "_")
    val _content = content
    JSONObject().apply {
        put("path", "/sdcard/Downloads/$category.txt")
        put("filename", "$category.txt")
    }
}

val generativeModel = GenerativeModel(
    modelName = GEMINI_MODEL_FLASH,
    apiKey = BuildConfig.GoogleCloudApiKey,
    generationConfig = config,
    tools = listOf(Tool(listOf(getExchangeRate,saveFile)))
)

suspend fun makeApiRequest(
    currencyFrom: String,
    currencyTo: String
): JSONObject {
    // This hypothetical API returns a JSON such as:
    // {"base":"USD","rates":{"SEK": 0.091}}
    return JSONObject().apply {
        put("base", currencyFrom)
        put("rates", hashMapOf(currencyTo to 1))
    }
}