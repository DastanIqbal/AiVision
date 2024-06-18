package com.dastanapps.visionai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.defineFunction
import com.google.ai.client.generativeai.type.generationConfig
import org.json.JSONObject
import java.io.File

// https://ai.google.dev/gemini-api/docs/get-started/tutorial?lang=android

object GenAIConfig{
    private const val GEMINI_MODEL_FLASH = "gemini-1.5-flash-latest"

    private val config = generationConfig {
        temperature = 0.7f
    }

    val generativeModel = GenerativeModel(
        modelName = GEMINI_MODEL_FLASH,
        apiKey = BuildConfig.GoogleCloudApiKey,
        generationConfig = config,
        tools = listOf(Tool(listOf(GenAITools.saveFile)))
    )
}

object GenAITools{

    val saveFile = defineFunction(
        name = "saveFile",
        description = "Save file in local storage if not category avaialble then save file.txt",
        Schema.str("content", "content to save"),
        Schema.str("category", "The category of the file."),
    ) { content, category ->
        val _category = category.replace(" ", "_")
        val _content = content
        val path = App.INSTANCE.externalCacheDir?.path

        val file = File("$path/$_category.txt")
        file.writeText(_content)

        JSONObject().apply {
            put("path", file.path)
            put("filename", "$category.txt")
        }
    }
}