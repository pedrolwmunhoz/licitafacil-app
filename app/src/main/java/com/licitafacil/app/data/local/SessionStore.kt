package com.licitafacil.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.licitafacil.app.data.model.MonitorConfig
import com.licitafacil.app.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("licitafacil")

class SessionStore(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    private val jwt = stringPreferencesKey("jwt")
    private val refreshToken = stringPreferencesKey("refreshToken")
    private val usuarioId = stringPreferencesKey("usuarioId")
    private val nome = stringPreferencesKey("nome")
    private val email = stringPreferencesKey("email")
    private val tipo = stringPreferencesKey("tipo")
    private val config = stringPreferencesKey("config")

    val session: Flow<Session?> = context.dataStore.data.map { p ->
        val token = p[jwt] ?: return@map null
        Session(
            jwt = token,
            refreshToken = p[refreshToken],
            usuarioId = p[usuarioId].orEmpty(),
            nome = p[nome].orEmpty(),
            email = p[email].orEmpty(),
            tipo = p[tipo].orEmpty(),
        )
    }

    val monitorConfig: Flow<MonitorConfig> = context.dataStore.data.map { p ->
        val raw = p[config] ?: return@map MonitorConfig()
        runCatching { json.decodeFromString<MonitorConfig>(raw) }.getOrElse { MonitorConfig() }
    }

    suspend fun saveSession(s: Session) {
        TokenHolder.set(s.jwt, s.refreshToken)
        context.dataStore.edit {
            it[jwt] = s.jwt
            if (s.refreshToken != null) {
                it[refreshToken] = s.refreshToken
            } else {
                it.remove(refreshToken)
            }
            it[usuarioId] = s.usuarioId
            it[nome] = s.nome
            it[email] = s.email
            it[tipo] = s.tipo
        }
    }

    suspend fun updateTokens(jwtValue: String, refreshValue: String) {
        TokenHolder.set(jwtValue, refreshValue)
        context.dataStore.edit {
            it[jwt] = jwtValue
            it[refreshToken] = refreshValue
        }
    }

    suspend fun clearSession() {
        TokenHolder.clear()
        context.dataStore.edit {
            it.remove(jwt)
            it.remove(refreshToken)
            it.remove(usuarioId)
            it.remove(nome)
            it.remove(email)
            it.remove(tipo)
        }
    }

    suspend fun saveConfig(c: MonitorConfig) {
        context.dataStore.edit { it[config] = json.encodeToString(c) }
    }
}
