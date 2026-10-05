package com.licitafacil.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val senha: String)

@Serializable
data class LoginResponse(
    val jwt: String,
    val jwtExpires: String,
    val refreshToken: String,
    val refreshTokenExpires: String,
    val usuarioId: String,
    val nome: String,
    val email: String,
    val tipo: String,
)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class RefreshResponse(
    val token: String,
    val expiresToken: String,
    val refreshToken: String,
    val expiresRefreshToken: String,
)

@Serializable
data class MonitorConfig(
    val keywords: List<String> = emptyList(),
    val ufs: List<String> = emptyList(),
    val valorMin: Double? = null,
    val valorMax: Double? = null,
    val modalidades: List<String> = emptyList(),
    val updatedAt: String? = null,
)

@Serializable
data class PncpPage(
    val data: List<PncpItem> = emptyList(),
    val totalRegistros: Long = 0,
    val totalPaginas: Long = 0,
    val numeroPagina: Long = 0,
    val paginasRestantes: Long = 0,
    val empty: Boolean = true,
)

/** Resposta de `GET /api/search/` (portal PNCP). */
@Serializable
data class PncpSearchResponse(
    val items: List<PncpSearchItem> = emptyList(),
    val total: Long = 0,
)

@Serializable
data class PncpSearchItem(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    @kotlinx.serialization.SerialName("document_type") val documentType: String? = null,
    @kotlinx.serialization.SerialName("item_url") val itemUrl: String? = null,
    @kotlinx.serialization.SerialName("numero_controle_pncp") val numeroControlePncp: String? = null,
    @kotlinx.serialization.SerialName("orgao_cnpj") val orgaoCnpj: String? = null,
    @kotlinx.serialization.SerialName("orgao_nome") val orgaoNome: String? = null,
    val ano: String? = null,
    @kotlinx.serialization.SerialName("numero_sequencial") val numeroSequencial: String? = null,
    val uf: String? = null,
    @kotlinx.serialization.SerialName("municipio_nome") val municipioNome: String? = null,
    @kotlinx.serialization.SerialName("modalidade_licitacao_id") val modalidadeId: String? = null,
    @kotlinx.serialization.SerialName("modalidade_licitacao_nome") val modalidadeNome: String? = null,
    @kotlinx.serialization.SerialName("situacao_nome") val situacaoNome: String? = null,
    @kotlinx.serialization.SerialName("data_publicacao_pncp") val dataPublicacaoPncp: String? = null,
    @kotlinx.serialization.SerialName("data_fim_recebimento_propostas") val dataFimRecebimentoPropostas: String? = null,
    @kotlinx.serialization.SerialName("data_inicio_recebimento_propostas") val dataInicioRecebimentoPropostas: String? = null,
    /**
     * No `/api/search/` com status=recebendo_proposta estes campos trazem
     * abertura/encerramento de proposta (iguais ao detalhe da consulta).
     * `data_*_recebimento_propostas` vem vazio nesse endpoint.
     */
    @kotlinx.serialization.SerialName("data_fim_vigencia") val dataFimVigencia: String? = null,
    @kotlinx.serialization.SerialName("data_inicio_vigencia") val dataInicioVigencia: String? = null,
    @kotlinx.serialization.SerialName("valor_global") val valorGlobal: Double? = null,
    @kotlinx.serialization.SerialName("valor_total_estimado") val valorTotalEstimado: Double? = null,
    @kotlinx.serialization.SerialName("indicador_orcamento_sigiloso") val indicadorOrcamentoSigiloso: Boolean? = null,
) {
    fun toPncpItem(): PncpItem {
        val fimProposta = dataFimRecebimentoPropostas?.takeIf { it.isNotBlank() }
            ?: dataFimVigencia?.takeIf { it.isNotBlank() }
        val iniProposta = dataInicioRecebimentoPropostas?.takeIf { it.isNotBlank() }
            ?: dataInicioVigencia?.takeIf { it.isNotBlank() }
        return PncpItem(
            numeroControlePNCP = numeroControlePncp,
            anoCompra = ano?.toIntOrNull(),
            sequencialCompra = numeroSequencial?.toIntOrNull(),
            objetoCompra = description?.ifBlank { title } ?: title,
            valorTotalEstimado = valorTotalEstimado ?: valorGlobal,
            orcamentoSigiloso = indicadorOrcamentoSigiloso == true,
            modalidadeId = modalidadeId?.toLongOrNull(),
            modalidadeNome = modalidadeNome,
            situacaoCompraNome = situacaoNome,
            dataPublicacaoPncp = dataPublicacaoPncp,
            dataAberturaProposta = iniProposta,
            dataEncerramentoProposta = fimProposta,
            orgaoEntidade = PncpOrgao(cnpj = orgaoCnpj, razaoSocial = orgaoNome),
            unidadeOrgao = PncpUnidade(ufSigla = uf, municipioNome = municipioNome),
        )
    }
}

data class EditaisPage(
    val items: List<PncpItem>,
    val pagina: Int,
    val hasMore: Boolean,
    val total: Long = 0,
)

@Serializable
data class PncpOrgao(
    val cnpj: String? = null,
    val razaoSocial: String? = null,
)

@Serializable
data class PncpUnidade(
    val ufSigla: String? = null,
    val ufNome: String? = null,
    val municipioNome: String? = null,
    val nomeUnidade: String? = null,
    val codigoUnidade: String? = null,
    val codigoIbge: String? = null,
)

@Serializable
data class PncpAmparoLegal(
    val nome: String? = null,
    val descricao: String? = null,
    val codigo: Long? = null,
)

@Serializable
data class PncpItem(
    val numeroControlePNCP: String? = null,
    val anoCompra: Int? = null,
    val sequencialCompra: Int? = null,
    val objetoCompra: String? = null,
    val informacaoComplementar: String? = null,
    val valorTotalEstimado: Double? = null,
    val orcamentoSigiloso: Boolean? = null,
    val orcamentoSigilosoCodigo: Long? = null,
    val orcamentoSigilosoDescricao: String? = null,
    val modalidadeId: Long? = null,
    val modalidadeNome: String? = null,
    val modoDisputaNome: String? = null,
    val situacaoCompraNome: String? = null,
    val dataPublicacaoPncp: String? = null,
    val dataAberturaProposta: String? = null,
    val dataEncerramentoProposta: String? = null,
    val dataAtualizacao: String? = null,
    val dataAtualizacaoGlobal: String? = null,
    val linkSistemaOrigem: String? = null,
    val srp: Boolean? = null,
    val amparoLegal: PncpAmparoLegal? = null,
    val numeroCompra: String? = null,
    val processo: String? = null,
    val orgaoEntidade: PncpOrgao? = null,
    val unidadeOrgao: PncpUnidade? = null,
) {
    fun cnpj(): String = orgaoEntidade?.cnpj.orEmpty()
    fun amparoLegalTexto(): String = amparoLegal?.nome.orEmpty()
    fun linkPncp(): String {
        val c = cnpj()
        val a = anoCompra
        val s = sequencialCompra
        return if (c.isNotBlank() && a != null && s != null) {
            "https://pncp.gov.br/app/editais/$c/$a/$s"
        } else ""
    }
}

data class Session(
    val jwt: String,
    /** Null só em login local (sem API). Login real sempre traz refresh do Redis. */
    val refreshToken: String?,
    val usuarioId: String,
    val nome: String,
    val email: String,
    val tipo: String,
)
