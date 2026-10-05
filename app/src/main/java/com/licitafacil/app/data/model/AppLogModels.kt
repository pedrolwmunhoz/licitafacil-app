package com.licitafacil.app.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AppLogCreateRequest(
    val nivel: String = "INFO",
    val origem: String = "android",
    val evento: String,
    val usuarioId: String? = null,
    val deviceId: String? = null,
    val appVersion: String? = null,
    val metodo: String? = null,
    val url: String? = null,
    val statusHttp: Int? = null,
    val duracaoMs: Int? = null,
    val requestHeaders: Map<String, JsonElement>? = null,
    val requestQuery: Map<String, JsonElement>? = null,
    val requestBody: String? = null,
    val responseHeaders: Map<String, JsonElement>? = null,
    val responseBody: String? = null,
    val mensagem: String? = null,
    val erro: String? = null,
    val extras: Map<String, JsonElement>? = null,
)
