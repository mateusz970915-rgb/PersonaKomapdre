package com.example.network

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface OpenRouterApiService {
    @GET("v1/models")
    suspend fun getModels(): OpenRouterModelsResponse

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") bearerToken: String,
        @Header("HTTP-Referer") referer: String = "https://aistudio.google.com/build",
        @Header("X-Title") title: String = "PersonaMesh Colony",
        @Body request: OpenRouterChatRequest
    ): OpenRouterChatResponse
}

object OpenRouterClient {
    private const val BASE_URL = "https://openrouter.ai/api/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: OpenRouterApiService by lazy {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(OpenRouterApiService::class.java)
    }

    // Comprehensive fallback list of known free models on OpenRouter
    val FALLBACK_FREE_MODELS = listOf(
        OpenRouterModel(
            id = "meta-llama/llama-3.3-70b-instruct:free",
            name = "Meta: Llama 3.3 70B Instruct (Free)",
            description = "State-of-the-art 70B parameter open weights model by Meta.",
            context_length = 131072,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "google/gemini-2.0-flash-exp:free",
            name = "Google: Gemini Flash 2.0 Experimental (Free)",
            description = "Next-generation multimodal model from Google.",
            context_length = 1048576,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "deepseek/deepseek-r1:free",
            name = "DeepSeek: R1 (Free)",
            description = "First-generation reasoning model trained with reinforcement learning.",
            context_length = 16384,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "qwen/qwen-2.5-coder-32b-instruct:free",
            name = "Qwen: Qwen 2.5 Coder 32B Instruct (Free)",
            description = "High-efficiency 32B code generation model by Alibaba.",
            context_length = 32768,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "mistralai/mistral-7b-instruct:free",
            name = "Mistral: Mistral 7B Instruct (Free)",
            description = "Fast and high-performance 7B instruct model.",
            context_length = 32768,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "google/gemma-2-9b-it:free",
            name = "Google: Gemma 2 9B Instruct (Free)",
            description = "Lightweight state-of-the-art open model built from Gemini technology.",
            context_length = 8192,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "meta-llama/llama-3.2-11b-vision-instruct:free",
            name = "Meta: Llama 3.2 11B Vision Instruct (Free)",
            description = "Multimodal vision-language model by Meta.",
            context_length = 131072,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "nousresearch/hermes-3-llama-3.1-405b:free",
            name = "Nous: Hermes 3 Llama 3.1 405B (Free)",
            description = "Flagship 405B model fine-tuned for agentic capabilities.",
            context_length = 131072,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "openchat/openchat-7b:free",
            name = "OpenChat: OpenChat 7B (Free)",
            description = "Open source conversation model fine-tuned on C-RLFT.",
            context_length = 8192,
            pricing = OpenRouterPricing("0", "0")
        ),
        OpenRouterModel(
            id = "gryphe/mythomax-l2-13b:free",
            name = "Gryphe: MythoMax L2 13B (Free)",
            description = "Creative storytelling model combining Llama-2 variants.",
            context_length = 4096,
            pricing = OpenRouterPricing("0", "0")
        )
    )
}
