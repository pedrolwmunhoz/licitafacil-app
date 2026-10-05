package com.licitafacil.app.data.log

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.licitafacil.app.BuildConfig
import com.licitafacil.app.data.api.ApiClients
import com.licitafacil.app.data.model.AppLogCreateRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.util.concurrent.atomic.AtomicReference

object AppLogger {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val deviceId = AtomicReference<String?>(null)
    private val usuarioId = AtomicReference<String?>(null)

    fun init(context: Context) {
        val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown"
        deviceId.set(id)
    }

    fun setUsuarioId(id: String?) {
        usuarioId.set(id)
    }

    fun event(
        evento: String,
        nivel: String = "INFO",
        mensagem: String? = null,
        erro: String? = null,
        extras: Map<String, String>? = null,
    ) {
        enqueue(
            AppLogCreateRequest(
                nivel = nivel,
                evento = evento,
                usuarioId = usuarioId.get(),
                deviceId = deviceId.get(),
                appVersion = BuildConfig.VERSION_NAME,
                mensagem = mensagem,
                erro = erro,
                extras = extras?.mapValues { JsonPrimitive(it.value) },
            ),
        )
    }

    fun httpCall(
        request: Request,
        response: Response?,
        responseBody: String?,
        duracaoMs: Long,
        erro: String? = null,
    ) {
        val url = request.url
        enqueue(
            AppLogCreateRequest(
                nivel = when {
                    erro != null -> "ERROR"
                    response == null -> "ERROR"
                    response.code >= 500 -> "ERROR"
                    response.code >= 400 -> "WARN"
                    else -> "INFO"
                },
                evento = "http_call",
                usuarioId = usuarioId.get(),
                deviceId = deviceId.get(),
                appVersion = BuildConfig.VERSION_NAME,
                metodo = request.method,
                url = url.toString(),
                statusHttp = response?.code,
                duracaoMs = duracaoMs.toInt().coerceAtLeast(0),
                requestHeaders = headersToMap(request.headers),
                requestQuery = queryToMap(url),
                requestBody = readRequestBody(request),
                responseHeaders = response?.headers?.let { headersToMap(it) },
                responseBody = responseBody,
                mensagem = "${request.method} ${url.encodedPath} → ${response?.code ?: "ERR"}",
                erro = erro,
                extras = mapOf(
                    "host" to JsonPrimitive(url.host),
                    "deviceModel" to JsonPrimitive("${Build.MANUFACTURER} ${Build.MODEL}"),
                    "sdk" to JsonPrimitive(Build.VERSION.SDK_INT.toString()),
                ),
            ),
        )
    }

    private fun enqueue(payload: AppLogCreateRequest) {
        scope.launch {
            runCatching { ApiClients.licita.enviarLog(payload) }
        }
    }

    private fun headersToMap(headers: Headers): Map<String, JsonElement> {
        val out = linkedMapOf<String, JsonElement>()
        for (name in headers.names()) {
            val value = if (name.equals("Authorization", true) || name.equals("Cookie", true)) {
                "***"
            } else {
                headers.values(name).joinToString("; ")
            }
            out[name] = JsonPrimitive(value)
        }
        return out
    }

    private fun queryToMap(url: HttpUrl): Map<String, JsonElement> {
        val out = linkedMapOf<String, JsonElement>()
        for (name in url.queryParameterNames) {
            val values = url.queryParameterValues(name)
            out[name] = JsonPrimitive(values.joinToString(","))
        }
        return out
    }

    private fun readRequestBody(request: Request): String? {
        val body = request.body ?: return null
        return try {
            val buffer = Buffer()
            body.writeTo(buffer)
            redactSecrets(buffer.readUtf8())
        } catch (_: Exception) {
            null
        }
    }

    private fun redactSecrets(raw: String): String {
        var s = raw
        s = s.replace(Regex("\"senha\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE), "\"senha\":\"***\"")
        s = s.replace(Regex("\"password\"\\s*:\\s*\"[^\"]*\"", RegexOption.IGNORE_CASE), "\"password\":\"***\"")
        return s
    }
}
