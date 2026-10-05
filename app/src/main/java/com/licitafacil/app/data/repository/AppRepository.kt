package com.licitafacil.app.data.repository

import android.content.Context
import com.licitafacil.app.data.api.ApiClients
import com.licitafacil.app.data.local.LocalAuth
import com.licitafacil.app.data.local.SessionStore
import com.licitafacil.app.data.local.TokenHolder
import com.licitafacil.app.data.log.AppLogger
import com.licitafacil.app.data.model.EditaisPage
import com.licitafacil.app.data.model.LoginRequest
import com.licitafacil.app.data.model.MonitorConfig
import com.licitafacil.app.data.model.PncpItem
import com.licitafacil.app.data.model.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class AppRepository(
    private val store: SessionStore,
    private val appContext: Context,
) {
    private val persistScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val session: Flow<Session?> = store.session
    val config: Flow<MonitorConfig> = store.monitorConfig

    init {
        TokenHolder.onTokensUpdated = { jwt, refresh ->
            persistScope.launch { store.updateTokens(jwt, refresh) }
        }
        persistScope.launch {
            store.session.first()?.let { TokenHolder.set(it.jwt, it.refreshToken) }
        }
    }

    suspend fun login(email: String, senha: String): Session {
        val user = email.trim()
        if (LocalAuth.matches(appContext, user, senha)) {
            val s = Session(
                jwt = "local-${UUID.randomUUID()}",
                refreshToken = null,
                usuarioId = "00000000-0000-4000-8000-000000000001",
                nome = "Admin local",
                email = user,
                tipo = "USER",
            )
            store.saveSession(s)
            AppLogger.setUsuarioId(s.usuarioId)
            AppLogger.event("login_local", mensagem = "login local $user")
            return s
        }

        val res = ApiClients.licita.login(LoginRequest(user, senha))
        if (res.tipo.equals("ADMIN", ignoreCase = true)) {
            error("Use o painel admin. Este app é para usuários.")
        }
        val s = Session(res.jwt, res.refreshToken, res.usuarioId, res.nome, res.email, res.tipo)
        store.saveSession(s)
        AppLogger.setUsuarioId(s.usuarioId)
        AppLogger.event("login_ok", mensagem = "login ${s.email}")
        return s
    }

    suspend fun logout() {
        AppLogger.event("logout")
        AppLogger.setUsuarioId(null)
        store.clearSession()
    }

    suspend fun forceLogoutUnauthorized() {
        AppLogger.event("logout_401", nivel = "WARN", mensagem = "sessão encerrada após falha no refresh")
        AppLogger.setUsuarioId(null)
        store.clearSession()
    }

    suspend fun saveConfig(c: MonitorConfig) = store.saveConfig(c)

    suspend fun listarEditaisCombo(
        q: String?,
        uf: String?,
        pagina: Int,
        tamPagina: Int = 10,
    ): EditaisPage {
        require(pagina >= 1)
        val c = store.monitorConfig.first()
        val res = ApiClients.pncpSearch.buscar(
            q = q?.ifBlank { null },
            tiposDocumento = "edital",
            status = "recebendo_proposta",
            pagina = pagina,
            tamPagina = tamPagina,
            ordenacao = "-data",
            uf = uf,
        )
        val items = res.items.map { it.toPncpItem() }.filter { matchesLocal(it, c) }
        return EditaisPage(
            items = items,
            pagina = pagina,
            hasMore = pagina.toLong() * tamPagina < res.total,
            total = res.total,
        )
    }

    suspend fun detalhe(cnpj: String, ano: Int, sequencial: Int): PncpItem =
        ApiClients.pncp.detalhe(cnpj, ano, sequencial)

    private fun matchesLocal(item: PncpItem, c: MonitorConfig): Boolean {
        val ufOk = c.ufs.isEmpty() ||
            c.ufs.any { it.equals(item.unidadeOrgao?.ufSigla, true) }
        val valor = item.valorTotalEstimado
        val minOk = c.valorMin == null || valor == null || valor >= c.valorMin
        val maxOk = c.valorMax == null || valor == null || valor <= c.valorMax
        val modOk = c.modalidades.isEmpty() ||
            c.modalidades.any { it.equals(item.modalidadeNome, ignoreCase = true) }
        return ufOk && minOk && maxOk && modOk
    }
}
