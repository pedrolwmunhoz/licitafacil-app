package com.licitafacil.app.ui.detalhe

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.licitafacil.app.data.model.PncpItem
import com.licitafacil.app.ui.components.BackHeader
import com.licitafacil.app.ui.components.Pill
import com.licitafacil.app.ui.components.PrimaryButton
import com.licitafacil.app.ui.theme.BluePill
import com.licitafacil.app.ui.theme.BluePillInk
import com.licitafacil.app.ui.theme.Dim
import com.licitafacil.app.ui.theme.GreenPill
import com.licitafacil.app.ui.theme.GreenPillInk
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.theme.Montserrat
import com.licitafacil.app.ui.theme.Muted
import com.licitafacil.app.ui.theme.Panel
import com.licitafacil.app.ui.theme.Soft
import com.licitafacil.app.ui.theme.Yellow
import com.licitafacil.app.ui.util.faltamLabel
import com.licitafacil.app.ui.util.formatValorEdital
import com.licitafacil.app.ui.util.formatPncpDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetalheScreen(
    loading: Boolean,
    error: String?,
    offline: Boolean = false,
    item: PncpItem?,
    onLoad: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { onLoad() }

    Column(Modifier.fillMaxSize().background(Panel)) {
        BackHeader("Detalhe do edital", onBack)
        when {
            offline && item == null -> Column(
                Modifier.fillMaxSize().padding(28.dp),
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
                    "Sem conexão. Você permanece nesta tela. Conecte-se e tente de novo.",
                    color = Muted,
                    fontSize = 14.sp,
                    fontFamily = Montserrat,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Tentar de novo", onClick = onLoad)
            }
            loading && item == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Ink)
            }
            !error.isNullOrBlank() && item == null -> Text(error, Modifier.padding(16.dp), color = Color(0xFF9E3229), fontFamily = Montserrat)
            item != null -> {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp, 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Pill(item.modalidadeNome ?: "Edital", BluePill, BluePillInk)
                        Pill(
                            text = item.situacaoCompraNome ?: "Divulgada no PNCP",
                            bg = GreenPill,
                            fg = GreenPillInk,
                            leading = { Box(Modifier.size(6.dp).background(Color(0xFF1F8A5B), CircleShape)) },
                        )
                        if (item.srp == true || item.objetoCompra.orEmpty().contains("registro de preços", true)) {
                            Pill("Registro de preços", Panel, Muted)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            item.orgaoEntidade?.razaoSocial ?: "Órgão",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = Montserrat,
                            letterSpacing = (-0.015).em,
                            lineHeight = 26.sp,
                        )
                        if (!item.unidadeOrgao?.nomeUnidade.isNullOrBlank()) {
                            Text(item.unidadeOrgao?.nomeUnidade ?: "", color = Muted, fontSize = 14.sp, fontFamily = Montserrat)
                        }
                        Text(
                            listOfNotNull(item.unidadeOrgao?.municipioNome, item.unidadeOrgao?.ufSigla).joinToString(" / "),
                            color = Muted,
                            fontSize = 14.sp,
                            fontFamily = Montserrat,
                        )
                    }

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Ink, RoundedCornerShape(8.dp))
                            .border(width = 0.dp, color = Color.Transparent, shape = RoundedCornerShape(8.dp)),
                    ) {
                        Box(Modifier.fillMaxWidth().height(3.dp).background(Yellow, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)))
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Fim do recebimento de propostas", color = Soft, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
                            Text(
                                formatPncpDate(item.dataEncerramentoProposta),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = Montserrat,
                                letterSpacing = (-0.015).em,
                            )
                            faltamLabel(item.dataEncerramentoProposta)?.let { label ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.AccessTime, null, tint = Yellow, modifier = Modifier.size(15.dp))
                                    Text(label, color = Yellow, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = Montserrat)
                                }
                            }
                        }
                    }

                    SectionCard("OBJETO") {
                        Text(item.objetoCompra ?: "—", fontSize = 15.sp, lineHeight = 23.sp, color = Ink, fontFamily = Montserrat)
                    }
                    if (!item.informacaoComplementar.isNullOrBlank()) {
                        SectionCard("INFORMAÇÃO COMPLEMENTAR") {
                            Text(item.informacaoComplementar, fontSize = 14.sp, lineHeight = 21.sp, color = Muted, fontFamily = Montserrat)
                        }
                    }

                    Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Soft, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text("DADOS DA CONTRATAÇÃO", Modifier.padding(vertical = 12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.14.em, color = Dim, fontFamily = Montserrat)
                        DataRow(
                            "Valor total estimado",
                            formatValorEdital(
                                valor = item.valorTotalEstimado,
                                sigiloso = item.orcamentoSigiloso,
                                codigo = item.orcamentoSigilosoCodigo,
                                descricao = item.orcamentoSigilosoDescricao,
                            ),
                        )
                        DataRow("Modalidade", item.modalidadeNome ?: "—")
                        DataRow("Modo de disputa", item.modoDisputaNome ?: "—")
                        if (item.amparoLegalTexto().isNotBlank()) DataRow("Amparo legal", item.amparoLegalTexto())
                        val compraProc = listOfNotNull(item.numeroCompra, item.processo).joinToString(" · ")
                        if (compraProc.isNotBlank()) DataRow("Nº da compra · Processo", compraProc)
                        DataRow("Publicação no PNCP", formatPncpDate(item.dataPublicacaoPncp))
                        DataRow("Abertura das propostas", formatPncpDate(item.dataAberturaProposta))
                        DataRow("Encerramento das propostas", formatPncpDate(item.dataEncerramentoProposta))
                        if (!item.dataAtualizacao.isNullOrBlank()) DataRow("Última atualização", formatPncpDate(item.dataAtualizacao))
                    }

                    Row(
                        Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Soft, RoundedCornerShape(8.dp)).padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Nº de controle PNCP", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Dim, fontFamily = Montserrat)
                            Text(item.numeroControlePNCP ?: "—", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
                        }
                        Box(
                            Modifier.size(44.dp).background(Panel, RoundedCornerShape(4.dp)).clickable {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("PNCP", item.numeroControlePNCP.orEmpty()))
                            },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.ContentCopy, "Copiar", tint = Ink, modifier = Modifier.size(18.dp))
                        }
                    }

                    Text(
                        "Dados da API pública do PNCP. Confira o edital completo e os anexos no portal antes de participar.",
                        fontSize = 12.sp,
                        color = Dim,
                        lineHeight = 18.sp,
                        fontFamily = Montserrat,
                    )
                }

                Column(Modifier.background(Color.White).windowInsetsPadding(WindowInsets.navigationBars)) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Soft))
                    Column(Modifier.padding(16.dp, 12.dp, 16.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        val link = item.linkPncp()
                        if (link.isNotBlank()) {
                            PrimaryButton(
                                text = "Abrir no PNCP",
                                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link))) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        val origem = item.linkSistemaOrigem
                        if (!origem.isNullOrBlank()) {
                            Row(
                                Modifier.fillMaxWidth().height(48.dp).clickable {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(origem)))
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Icon(Icons.Outlined.OpenInNew, null, tint = Ink, modifier = Modifier.size(18.dp).padding(end = 0.dp))
                                Text("Abrir sistema de origem", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Soft, RoundedCornerShape(8.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.14.em, color = Dim, fontFamily = Montserrat)
        content()
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth().border(width = 0.dp, Soft).padding(top = 1.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Soft))
        Column(Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Dim, fontFamily = Montserrat)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink, fontFamily = Montserrat)
        }
    }
}
