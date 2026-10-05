package com.licitafacil.app.data.api

import com.licitafacil.app.data.log.AppLogger
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Captura método, URL completa, query, headers, body req/res, status e duração.
 * Não loga o próprio endpoint de ingestão (evita loop).
 */
class AppLogInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (path.contains("/app-logs")) {
            return chain.proceed(request)
        }

        val started = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            val body = response.body
            val media = body?.contentType()
            val bytes = body?.bytes() ?: ByteArray(0)
            val text = runCatching { String(bytes, Charsets.UTF_8) }.getOrNull()
            val durMs = (System.nanoTime() - started) / 1_000_000
            AppLogger.httpCall(request, response, text, durMs)
            response.newBuilder()
                .body(bytes.toResponseBody(media))
                .build()
        } catch (e: Exception) {
            val durMs = (System.nanoTime() - started) / 1_000_000
            AppLogger.httpCall(request, null, null, durMs, erro = e.message ?: e.javaClass.simpleName)
            if (e is IOException) throw e
            throw IOException(e.message, e)
        }
    }
}
