package com.licitafacil.app.ui.lista

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.licitafacil.app.data.model.PncpItem
import com.licitafacil.app.ui.components.AppTopBar
import com.licitafacil.app.ui.components.MonitorStrip
import com.licitafacil.app.ui.components.Pill
import com.licitafacil.app.ui.components.PrimaryButton
import com.licitafacil.app.ui.components.SecondaryButton
import com.licitafacil.app.ui.components.WarnBanner
import com.licitafacil.app.ui.theme.BluePill
import com.licitafacil.app.ui.theme.BluePillInk
import com.licitafacil.app.ui.theme.Dim
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.theme.Montserrat
import com.licitafacil.app.ui.theme.Muted
import com.licitafacil.app.ui.theme.Panel
import com.licitafacil.app.ui.theme.Skeleton
import com.licitafacil.app.ui.theme.Soft
import com.licitafacil.app.ui.theme.WarnBg
import com.licitafacil.app.ui.theme.WarnInk
import com.licitafacil.app.ui.util.encerraLabel
import com.licitafacil.app.ui.util.formatValorEdital
import com.licitafacil.app.ui.util.isOrcamentoSigiloso
import com.licitafacil.app.ui.util.formatPncpDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListaScreen(
    editais: List<PncpItem>,
    loading: Boolean,
    loadingMore: Boolean = false,
    hasMore: Boolean = false,
    totalAbertos: Long = 0L,
    error: String?,
    offline: Boolean = false,
    configSummary: String,
    initials: String,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit = onRefresh,
    onOpen: (PncpItem) -> Unit,
    onConfig: () -> Unit,
    onConta: () -> Unit,
) {
    LaunchedEffect(Unit) { onRefresh() }

    Column(Modifier.fillMaxSize().background(Panel)) {
        AppTopBar(onConfig = onConfig, onConta = onConta, initials = initials)
        MonitorStrip(summary = configSummary, onAlterar = onConfig)

        when {
            offline && editais.isEmpty() -> OfflineState(onRefresh)
            loading && editais.isEmpty() -> LoadingState()
            !error.isNullOrBlank() && editais.isEmpty() && !hasMore -> ErrorState(onRefresh, detail = error)
            editais.isEmpty() && !hasMore -> EmptyState(onConfig)
            else -> {
                val navPad = WindowInsets.navigationBars.asPaddingValues()
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        top = 16.dp,
                        bottom = 16.dp + navPad.calculateBottomPadding(),
                    ),
                ) {
                    if (offline) {
                        item { OfflineBanner() }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val totalLabel = when {
                                editais.isEmpty() -> "Nenhum edital nesta combinação"
                                totalAbertos > 0L -> "$totalAbertos editais abertos"
                                else -> "${editais.size} editais abertos"
                            }
                            Text(
                                totalLabel,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                fontFamily = Montserrat,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.SwapVert, null, tint = Dim, modifier = Modifier.size(14.dp))
                                Text("Mais recentes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Dim, fontFamily = Montserrat)
                            }
                        }
                    }
                    items(items = editais, key = { it.numeroControlePNCP ?: it.hashCode().toString() }) { item ->
                        EditalCard(item, onClick = { onOpen(item) })
                    }
                    item {
                        if (hasMore) {
                            SecondaryButton(
                                text = if (loadingMore) "Carregando…" else "Carregar mais editais",
                                onClick = { if (!loadingMore) onLoadMore() },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Text(
                            if (totalAbertos > 0L) "Mostrando ${editais.size} de $totalAbertos · Fonte: PNCP"
                            else "Mostrando ${editais.size} · Fonte: PNCP",
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            fontSize = 12.sp,
                            color = Dim,
                            fontFamily = Montserrat,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditalCard(item: PncpItem, onClick: () -> Unit) {
    val prazo = encerraLabel(item.dataEncerramentoProposta)
    val urgente = (com.licitafacil.app.ui.util.daysUntil(item.dataEncerramentoProposta) ?: 99) <= 3
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(8.dp), ambientColor = Color(0x141D2125), spotColor = Color(0x0F1D2125))
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, Soft, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Pill(item.modalidadeNome ?: "Edital", BluePill, BluePillInk)
            if (prazo != null) {
                Pill(
                    text = prazo,
                    bg = if (urgente) WarnBg else Panel,
                    fg = if (urgente) WarnInk else Muted,
                    leading = {
                        Icon(
                            Icons.Outlined.AccessTime,
                            null,
                            tint = if (urgente) WarnInk else Muted,
                            modifier = Modifier.size(13.dp),
                        )
                    },
                )
            }
            if (item.srp == true || item.objetoCompra.orEmpty().contains("registro de preços", true)) {
                Pill("SRP", Panel, Muted)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(item.orgaoEntidade?.razaoSocial ?: "Órgão", fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp, fontFamily = Montserrat, color = Ink)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.LocationOn, null, tint = Dim, modifier = Modifier.size(13.dp))
                Text(
                    listOfNotNull(item.unidadeOrgao?.municipioNome, item.unidadeOrgao?.ufSigla).joinToString(" / ").ifBlank { "—" },
                    fontSize = 12.sp,
                    color = Dim,
                    fontFamily = Montserrat,
                )
            }
        }
        Text(
            item.objetoCompra.orEmpty(),
            fontSize = 14.sp,
            color = Muted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp,
            fontFamily = Montserrat,
        )
        Box(Modifier.fillMaxWidth().height(1.dp).background(Soft))
        Row(Modifier.fillMaxWidth().padding(top = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Valor estimado", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Dim, fontFamily = Montserrat)
                val valorTxt = formatValorEdital(
                    valor = item.valorTotalEstimado,
                    sigiloso = item.orcamentoSigiloso,
                    codigo = item.orcamentoSigilosoCodigo,
                    descricao = item.orcamentoSigilosoDescricao,
                )
                val valorMuted = isOrcamentoSigiloso(
                    item.orcamentoSigiloso,
                    item.orcamentoSigilosoCodigo,
                    item.orcamentoSigilosoDescricao,
                ) || item.valorTotalEstimado == null || (item.valorTotalEstimado ?: 0.0) <= 0.0
                Text(
                    valorTxt,
                    fontWeight = if (valorMuted) FontWeight.SemiBold else FontWeight.ExtraBold,
                    fontSize = if (valorMuted) 14.sp else 15.sp,
                    color = if (valorMuted) Dim else Ink,
                    fontFamily = Montserrat,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Fim da proposta", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Dim, fontFamily = Montserrat)
                Text(formatPncpDate(item.dataEncerramentoProposta, short = true), fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat)
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Buscando editais no PNCP…", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Muted, fontFamily = Montserrat)
        repeat(3) {
            Column(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Soft, RoundedCornerShape(8.dp)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(120.dp, 24.dp).background(Skeleton, CircleShape))
                    Box(Modifier.size(90.dp, 24.dp).background(Skeleton, CircleShape))
                }
                Box(Modifier.fillMaxWidth(0.7f).height(16.dp).background(Skeleton, RoundedCornerShape(4.dp)))
                Box(Modifier.fillMaxWidth().height(14.dp).background(Skeleton, RoundedCornerShape(4.dp)))
                Box(Modifier.fillMaxWidth(0.9f).height(14.dp).background(Skeleton, RoundedCornerShape(4.dp)))
            }
        }
    }
}

@Composable
private fun EmptyState(onConfig: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(28.dp, 32.dp, 28.dp, 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(80.dp).background(Color.White, CircleShape).border(1.dp, Soft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.SearchOff, null, tint = Ink, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Nada com esses filtros", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontFamily = Montserrat, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Nenhum edital aberto bateu com as palavras-chave, UFs e valores configurados.",
            color = Muted,
            fontSize = 14.sp,
            fontFamily = Montserrat,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Alterar configuração", onClick = onConfig)
    }
}

@Composable
private fun ErrorState(onRefresh: () -> Unit, detail: String? = null) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WarnBanner(
            title = "Não deu pra carregar os editais",
            body = detail?.ifBlank { null }
                ?: "A consulta pública do PNCP não respondeu ou está instável. Tente de novo em instantes.",
        )
        PrimaryButton(
            text = "Tentar de novo",
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
        )
        Text("Sua configuração continua salva.", fontSize = 12.sp, color = Dim, fontFamily = Montserrat)
    }
}

@Composable
private fun OfflineBanner() {
    WarnBanner(
        title = "Sem rede",
        body = "Você está offline. Os editais já carregados continuam visíveis. Conecte-se para atualizar.",
    )
}

@Composable
private fun OfflineState(onRefresh: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(28.dp, 32.dp, 28.dp, 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(80.dp).background(Color.White, CircleShape).border(1.dp, Soft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.CloudOff, null, tint = Ink, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Sem rede", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontFamily = Montserrat, color = Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Não há conexão com a internet. Você permanece nesta tela; ao voltar a rede, toque em tentar de novo.",
            color = Muted,
            fontSize = 14.sp,
            fontFamily = Montserrat,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Tentar de novo", onClick = onRefresh)
    }
}
