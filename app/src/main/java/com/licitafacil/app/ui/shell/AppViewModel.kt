package com.licitafacil.app.ui.shell

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.licitafacil.app.data.api.ApiClients
import com.licitafacil.app.data.local.NetworkMonitor
import com.licitafacil.app.data.model.MonitorConfig
import com.licitafacil.app.data.model.PncpItem
import com.licitafacil.app.data.model.Session
import com.licitafacil.app.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** keyword × UF, ordem fixa. */
private data class SearchCombo(val q: String?, val uf: String?, var total: Long? = null)

class AppViewModel(
    app: Application,
    private val repo: AppRepository,
) : AndroidViewModel(app) {
    val session: StateFlow<Session?> = repo.session.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val config: StateFlow<MonitorConfig> = repo.config.stateIn(viewModelScope, SharingStarted.Eagerly, MonitorConfig())

    var loading by mutableStateOf(false)
        private set
    var loadingMore by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    /** Popup de login quando o servidor está indisponível (não banner). */
    var serverErrorPopup by mutableStateOf(false)
        private set
    var offline by mutableStateOf(false)
        private set
    var editais by mutableStateOf<List<PncpItem>>(emptyList())
        private set
    var hasMore by mutableStateOf(false)
        private set
    /** Soma dos `total` do PNCP por combinação (não o tamanho da lista carregada). */
    var totalAbertos by mutableStateOf(0L)
        private set
    var detalhe by mutableStateOf<PncpItem?>(null)
        private set

    private var combos: List<SearchCombo> = emptyList()
    private var comboIndex = 0
    private var paginaCombo = 1
    private val tamPagina = 10

    init {
        viewModelScope.launch {
            ApiClients.unauthorized.collect {
                repo.forceLogoutUnauthorized()
            }
        }
    }

    fun dismissServerError() {
        serverErrorPopup = false
    }

    suspend fun login(email: String, senha: String): Boolean {
        loading = true
        error = null
        serverErrorPopup = false
        return try {
            repo.login(email, senha)
            true
        } catch (e: retrofit2.HttpException) {
            when (e.code()) {
                401 -> error = "E-mail ou senha inválidos"
                403 -> error = "INACTIVE:Conta inativa|Seu acesso está desativado. Fale com o suporte para reativar."
                in 500..599 -> serverErrorPopup = true
                else -> serverErrorPopup = true
            }
            false
        } catch (e: Exception) {
            if (isNetworkFailure(e)) {
                serverErrorPopup = true
            } else {
                serverErrorPopup = true
            }
            false
        } finally {
            loading = false
        }
    }

    suspend fun logout() = repo.logout()

    suspend fun saveConfig(c: MonitorConfig) = repo.saveConfig(c)

    /** Abre lista: 1 request = 1ª combinação, página 1. */
    suspend fun carregarEditais() {
        if (!NetworkMonitor.isOnline(getApplication())) {
            offline = true
            error = null
            loading = false
            return
        }
        loading = true
        error = null
        offline = false
        editais = emptyList()
        totalAbertos = 0L
        combos = buildCombos(config.value)
        comboIndex = 0
        paginaCombo = 1
        try {
            fetchCurrentCombo(append = false)
        } catch (e: Exception) {
            applyListError(e, clear = true)
        } finally {
            loading = false
        }
    }

    /**
     * Carregar mais: próxima combinação na mesma página;
     * acabou a rodada → página 2 na mesma ordem; depois 3…
     */
    suspend fun carregarMaisEditais() {
        if (loading || loadingMore || !hasMore) return
        if (!NetworkMonitor.isOnline(getApplication())) {
            offline = true
            error = null
            return
        }
        loadingMore = true
        error = null
        offline = false
        try {
            fetchCurrentCombo(append = true)
        } catch (e: Exception) {
            applyListError(e, clear = false)
        } finally {
            loadingMore = false
        }
    }

    private suspend fun fetchCurrentCombo(append: Boolean) {
        if (combos.isEmpty()) {
            editais = emptyList()
            hasMore = false
            return
        }
        if (!positionOnNextAvailable()) {
            hasMore = false
            return
        }

        val combo = combos[comboIndex]
        val page = repo.listarEditaisCombo(
            q = combo.q,
            uf = combo.uf,
            pagina = paginaCombo,
            tamPagina = tamPagina,
        )
        combo.total = page.total
        totalAbertos = combos.mapNotNull { it.total }.sum()

        val seen = if (append) {
            editais.mapNotNull { it.numeroControlePNCP }.toHashSet()
        } else {
            hashSetOf()
        }
        val novos = page.items.filter { it.numeroControlePNCP == null || it.numeroControlePNCP !in seen }
        editais = if (append) editais + novos else novos

        hasMore = advanceCursor()
    }

    private fun buildCombos(c: MonitorConfig): List<SearchCombo> {
        val keywords = c.keywords.map { it.trim() }.filter { it.isNotEmpty() }
            .ifEmpty { listOf(null) }
        val ufs: List<String?> =
            if (c.ufs.isEmpty()) listOf(null) else c.ufs.map { it.uppercase() }
        return keywords.flatMap { kw -> ufs.map { uf -> SearchCombo(q = kw, uf = uf) } }
    }

    private fun isExhausted(combo: SearchCombo, page: Int): Boolean {
        val total = combo.total ?: return false
        return (page.toLong() - 1) * tamPagina >= total
    }

    private fun positionOnNextAvailable(): Boolean {
        val guardMax = (combos.size * 50).coerceAtLeast(1)
        repeat(guardMax) {
            if (!isExhausted(combos[comboIndex], paginaCombo)) return true
            if (!advanceCursor()) return false
        }
        return false
    }

    private fun advanceCursor(): Boolean {
        if (combos.isEmpty()) return false
        var i = comboIndex
        var p = paginaCombo
        repeat(combos.size * 50) {
            i++
            if (i >= combos.size) {
                i = 0
                p++
            }
            if (!isExhausted(combos[i], p)) {
                comboIndex = i
                paginaCombo = p
                return true
            }
        }
        return false
    }

    private fun applyListError(e: Exception, clear: Boolean) {
        if (isNetworkFailure(e)) {
            offline = true
            error = null
            // Mantém a tela / dados já carregados
            if (clear && editais.isEmpty()) {
                hasMore = false
            }
            return
        }
        offline = false
        error = when (e) {
            is retrofit2.HttpException -> when (e.code()) {
                400 -> "Consulta PNCP inválida (${e.code()})"
                404 -> "Endpoint PNCP não encontrado (${e.code()})"
                429 -> "PNCP pediu pra esperar. Tente de novo em instantes."
                else -> "PNCP respondeu ${e.code()}. Tente de novo."
            }
            else -> {
                val msg = e.message.orEmpty()
                when {
                    msg.contains("Json", true) || msg.contains("Serializer", true) ->
                        "Resposta do PNCP em formato inesperado."
                    else -> msg.ifBlank { "Falha ao carregar editais" }
                }
            }
        }
        if (clear) {
            editais = emptyList()
            totalAbertos = 0L
            hasMore = false
        }
    }

    suspend fun carregarDetalhe(cnpj: String, ano: Int, seq: Int) {
        if (!NetworkMonitor.isOnline(getApplication())) {
            offline = true
            error = null
            loading = false
            return
        }
        loading = true
        error = null
        offline = false
        detalhe = null
        try {
            detalhe = repo.detalhe(cnpj, ano, seq)
        } catch (e: Exception) {
            if (isNetworkFailure(e)) {
                offline = true
                error = null
            } else {
                error = e.message ?: "Falha ao carregar detalhe"
            }
        } finally {
            loading = false
        }
    }

    private fun isNetworkFailure(e: Throwable): Boolean {
        if (e is UnknownHostException || e is ConnectException || e is SocketTimeoutException) return true
        if (e is IOException) {
            val msg = e.message.orEmpty()
            return msg.contains("Unable to resolve host", true) ||
                msg.contains("Failed to connect", true) ||
                msg.contains("timeout", true) ||
                msg.contains("Network is unreachable", true) ||
                msg.contains("Software caused connection abort", true)
        }
        val cause = e.cause
        return cause != null && cause !== e && isNetworkFailure(cause)
    }
}

class AppViewModelFactory(private val repo: AppRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Application vem via AndroidViewModelFactory se usarmos Application; aqui injetamos via repo.context
        throw IllegalStateException("Use create(modelClass, extras)")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: androidx.lifecycle.viewmodel.CreationExtras): T {
        val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        return AppViewModel(app, repo) as T
    }
}
