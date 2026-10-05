package com.licitafacil.app.data.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.licitafacil.app.BuildConfig
import com.licitafacil.app.data.local.TokenHolder
import com.licitafacil.app.data.model.AppLogCreateRequest
import com.licitafacil.app.data.model.LoginRequest
import com.licitafacil.app.data.model.LoginResponse
import com.licitafacil.app.data.model.PncpItem
import com.licitafacil.app.data.model.PncpSearchResponse
import com.licitafacil.app.data.model.RefreshRequest
import com.licitafacil.app.data.model.RefreshResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

interface LicitaApi {
    @POST("api/pub/auth")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/pub/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @POST("api/pub/app-logs")
    suspend fun enviarLog(@Body body: AppLogCreateRequest)
}

interface PncpSearchApi {
    @GET("search/")
    suspend fun buscar(
        @Query("q") q: String?,
        @Query("tipos_documento") tiposDocumento: String,
        @Query("status") status: String,
        @Query("pagina") pagina: Int,
        @Query("tam_pagina") tamPagina: Int,
        @Query("ordenacao") ordenacao: String,
        @Query("uf") uf: String?,
    ): PncpSearchResponse
}

interface PncpConsultaApi {
    @GET("v1/orgaos/{cnpj}/compras/{ano}/{sequencial}")
    suspend fun detalhe(
        @Path("cnpj") cnpj: String,
        @Path("ano") ano: Int,
        @Path("sequencial") sequencial: Int,
    ): PncpItem
}

private class RateLimitRetryInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var last: Response? = null
        while (attempt < 4) {
            last?.close()
            val response = chain.proceed(chain.request())
            if (response.code != 429) return response
            last = response
            attempt++
            val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: (2L * attempt)
            try {
                Thread.sleep((retryAfter.coerceIn(1, 15)) * 1000)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                return response
            }
        }
        return last ?: throw IOException("PNCP rate limit")
    }
}

private class AuthHeaderInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = TokenHolder.jwt
        if (token.isNullOrBlank() || TokenHolder.isLocal()) {
            return chain.proceed(chain.request())
        }
        val path = chain.request().url.encodedPath
        if (path.endsWith("/auth") || path.contains("/auth/refresh")) {
            return chain.proceed(chain.request())
        }
        return chain.proceed(
            chain.request().newBuilder().header("Authorization", "Bearer $token").build(),
        )
    }
}

/** 401 → refresh no Redis; se refresh 401 → logout. */
private class TokenRefreshAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.contains("/auth")) return null
        if (responseCount(response) >= 2) return null

        val failedAuth = response.request.header("Authorization")
        val currentJwt = TokenHolder.jwt
        val currentRt = TokenHolder.refreshToken
        if (currentJwt.isNullOrBlank() || currentRt.isNullOrBlank() || TokenHolder.isLocal()) return null

        synchronized(TokenHolder) {
            val latestJwt = TokenHolder.jwt
            val latestRt = TokenHolder.refreshToken
            if (!latestJwt.isNullOrBlank() &&
                latestJwt != currentJwt &&
                failedAuth != "Bearer $latestJwt"
            ) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $latestJwt")
                    .build()
            }

            val refreshed = ApiClients.refreshBlocking(latestRt ?: currentRt)
            if (refreshed == null) {
                ApiClients.emitUnauthorized()
                return null
            }
            TokenHolder.set(refreshed.token, refreshed.refreshToken)
            TokenHolder.onTokensUpdated?.invoke(refreshed.token, refreshed.refreshToken)
            return response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshed.token}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}

object ApiClients {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }
    private val contentType = "application/json".toMediaType()

    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    internal fun emitUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }

    private val refreshClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val refreshApi: LicitaApiRefreshCall = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
        .client(refreshClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
        .create(LicitaApiRefreshCall::class.java)

    fun refreshBlocking(refreshToken: String): RefreshResponse? {
        return try {
            val response = refreshApi.refresh(RefreshRequest(refreshToken)).execute()
            if (response.code() == 401) return null
            if (!response.isSuccessful) return null
            response.body()
        } catch (_: Exception) {
            null
        }
    }

    private fun client(browserUa: Boolean, withAuth: Boolean): OkHttpClient {
        val b = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val ua = if (browserUa) {
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                } else {
                    "LicitaFacil/0.1 (Android)"
                }
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .header("User-Agent", ua)
                        .build(),
                )
            }
        if (withAuth) {
            b.addInterceptor(AuthHeaderInterceptor())
            b.authenticator(TokenRefreshAuthenticator())
        }
        b.addInterceptor(RateLimitRetryInterceptor())
        return b.build()
    }

    val licita: LicitaApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL.trimEnd('/') + "/")
        .client(client(browserUa = false, withAuth = true))
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
        .create(LicitaApi::class.java)

    val pncpSearch: PncpSearchApi = Retrofit.Builder()
        .baseUrl("https://pncp.gov.br/api/")
        .client(client(browserUa = true, withAuth = false))
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
        .create(PncpSearchApi::class.java)

    val pncp: PncpConsultaApi = Retrofit.Builder()
        .baseUrl("https://pncp.gov.br/api/consulta/")
        .client(client(browserUa = true, withAuth = false))
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
        .create(PncpConsultaApi::class.java)
}

private interface LicitaApiRefreshCall {
    @POST("api/pub/auth/refresh")
    fun refresh(@Body body: RefreshRequest): retrofit2.Call<RefreshResponse>
}
