package com.licitafacil.app.ui.config

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.licitafacil.app.data.model.MonitorConfig
import com.licitafacil.app.ui.components.BrandMark
import com.licitafacil.app.ui.components.ErrorBanner
import com.licitafacil.app.ui.components.LfField
import com.licitafacil.app.ui.components.PrimaryButton
import com.licitafacil.app.ui.components.SecondaryButton
import com.licitafacil.app.ui.theme.Dim
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.theme.Line
import com.licitafacil.app.ui.theme.Montserrat
import com.licitafacil.app.ui.theme.Muted
import com.licitafacil.app.ui.theme.Soft
import com.licitafacil.app.ui.theme.Yellow
import com.licitafacil.app.ui.util.formatUpdatedAt
import com.licitafacil.app.ui.util.moneyMaskFromDouble
import com.licitafacil.app.ui.util.moneyMaskInput
import com.licitafacil.app.ui.util.moneyMaskToDouble
import java.time.OffsetDateTime

private val UFS = listOf(
    "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
    "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO",
)

private val MODALIDADES = listOf(
    "Pregão eletrônico", "Pregão presencial", "Concorrência eletrônica", "Concorrência presencial",
    "Dispensa de licitação", "Inexigibilidade", "Credenciamento", "Leilão eletrônico",
    "Leilão presencial", "Diálogo competitivo", "Concurso", "Manifestação de interesse", "Pré-qualificação",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfigScreen(
    initial: MonitorConfig,
    onSave: (MonitorConfig) -> Unit,
    onCancel: (() -> Unit)?,
    onLogout: (() -> Unit)? = null,
) {
    val keywords = remember(initial) { mutableStateListOf(*initial.keywords.toTypedArray()) }
    val ufs = remember(initial) { mutableStateListOf(*initial.ufs.toTypedArray()) }
    val modalidades = remember(initial) { mutableStateListOf(*initial.modalidades.toTypedArray()) }
    var kwDraft by remember { mutableStateOf("") }
    var valorMin by remember(initial) { mutableStateOf(moneyMaskFromDouble(initial.valorMin)) }
    var valorMax by remember(initial) { mutableStateOf(moneyMaskFromDouble(initial.valorMax)) }
    var showErrors by remember { mutableStateOf(false) }
    val first = initial.keywords.isEmpty()
    val kwError = showErrors && keywords.isEmpty()
    val vmin = moneyMaskToDouble(valorMin)
    val vmax = moneyMaskToDouble(valorMax)
    val rangeError = showErrors && vmin != null && vmax != null && vmin > vmax

    // Header fixo; scroll + rodapé sobem com o teclado (imePadding)
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Column(Modifier.fillMaxWidth().background(Ink).statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().height(56.dp).padding(start = 16.dp, end = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BrandMark()
                if (onLogout != null && first) {
                    Row(
                        Modifier.height(44.dp).clickable(onClick = onLogout).padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Text("Sair", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = Montserrat)
                    }
                }
            }
        }

        Column(Modifier.weight(1f).fillMaxWidth().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp, 24.dp, 20.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (first) "PRIMEIRO ACESSO" else "EDITAR MONITORAMENTO",
                    color = Dim,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.14.em,
                    fontFamily = Montserrat,
                )
                Text(
                    if (first) "O que você quer ver?" else "Editar monitoramento",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink,
                    fontFamily = Montserrat,
                    letterSpacing = (-0.02).em,
                    lineHeight = 30.sp,
                )
                Text(
                    if (first) {
                        "Escolha os filtros dos editais que vão aparecer na sua lista. Dá pra mudar quando quiser."
                    } else {
                        "Ajuste os filtros. Ao salvar, a configuração anterior é substituída e a lista atualiza na hora."
                    },
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = Montserrat,
                )
                if (!first && !initial.updatedAt.isNullOrBlank()) {
                    Text(formatUpdatedAt(initial.updatedAt).replace("Atualizado em", "Última alteração:"), color = Dim, fontSize = 12.sp, fontFamily = Montserrat)
                }
            }

            if (kwError || rangeError) {
                val msgs = buildList {
                    if (kwError) add("palavra-chave")
                    if (rangeError) add("faixa de valor")
                }
                ErrorBanner(
                    title = "Revise ${msgs.size} campo${if (msgs.size > 1) "s" else ""} antes de salvar",
                    body = when {
                        kwError && rangeError -> "Falta palavra-chave e a faixa de valor está invertida."
                        kwError -> "Adicione pelo menos 1 palavra-chave."
                        else -> "O mínimo não pode ser maior que o máximo."
                    },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Palavras-chave", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, fontFamily = Montserrat)
                    Text("obrigatório", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Muted, fontFamily = Montserrat)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LfField(
                        value = kwDraft,
                        onValueChange = { kwDraft = it },
                        placeholder = "Ex.: ar-condicionado",
                        error = kwError,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        Modifier
                            .height(48.dp)
                            .border(1.dp, Line, RoundedCornerShape(4.dp))
                            .clickable {
                                val t = kwDraft.trim()
                                if (t.isNotEmpty() && t !in keywords) keywords.add(t)
                                kwDraft = ""
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Add, null, tint = Ink, modifier = Modifier.size(18.dp))
                            Text("Adicionar", fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat)
                        }
                    }
                }
                if (keywords.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        keywords.forEach { kw ->
                            Row(
                                Modifier.height(36.dp).background(Ink, CircleShape).padding(start = 12.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(kw, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = Montserrat)
                                Box(Modifier.size(28.dp).clickable { keywords.remove(kw) }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Close, "Remover", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                Text(
                    if (kwError) "Adicione pelo menos 1 palavra-chave."
                    else "Digite um termo e toque em Adicionar. Buscamos no objeto e na informação complementar do edital.",
                    fontSize = 13.sp,
                    color = if (kwError) Color(0xFF9E3229) else Dim,
                    fontFamily = Montserrat,
                    lineHeight = 18.sp,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Estados (UF)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, fontFamily = Montserrat)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (ufs.isNotEmpty()) {
                            Text("Limpar", color = Ink, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = Montserrat, modifier = Modifier.clickable { ufs.clear() })
                        }
                        Text("opcional", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Dim, fontFamily = Montserrat)
                    }
                }
                Text(
                    if (ufs.isEmpty()) "Nenhum selecionado = Brasil todo."
                    else "${ufs.size} selecionado${if (ufs.size > 1) "s" else ""}. Nenhum selecionado = Brasil todo.",
                    fontSize = 13.sp,
                    color = Dim,
                    fontFamily = Montserrat,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UFS.chunked(6).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { uf ->
                                val selected = uf in ufs
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .background(if (selected) Yellow else Color.White, RoundedCornerShape(4.dp))
                                        .border(1.dp, if (selected) Yellow else Line, RoundedCornerShape(4.dp))
                                        .clickable {
                                            if (selected) ufs.remove(uf) else ufs.add(uf)
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        uf,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = if (selected) Ink else Muted,
                                        fontFamily = Montserrat,
                                    )
                                }
                            }
                            repeat(6 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Faixa de valor", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, fontFamily = Montserrat)
                    Text("opcional", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Dim, fontFamily = Montserrat)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Mínimo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Muted, fontFamily = Montserrat)
                        LfField(
                            value = valorMin,
                            onValueChange = { valorMin = moneyMaskInput(it) },
                            placeholder = "0,00",
                            error = rangeError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leading = { Text("R$", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Muted, fontFamily = Montserrat) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Máximo", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Muted, fontFamily = Montserrat)
                        LfField(
                            value = valorMax,
                            onValueChange = { valorMax = moneyMaskInput(it) },
                            placeholder = "Sem limite",
                            error = rangeError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leading = { Text("R$", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Muted, fontFamily = Montserrat) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                Text(
                    if (rangeError) "O mínimo não pode ser maior que o máximo."
                    else "Valor total estimado do edital. Deixe em branco pra não limitar.",
                    fontSize = 13.sp,
                    color = if (rangeError) Color(0xFF9E3229) else Dim,
                    fontFamily = Montserrat,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Modalidades", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, fontFamily = Montserrat)
                    Text("opcional", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Dim, fontFamily = Montserrat)
                }
                Text("Nenhuma selecionada = todas.", fontSize = 13.sp, color = Dim, fontFamily = Montserrat)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MODALIDADES.forEach { mod ->
                        val selected = mod in modalidades
                        Box(
                            Modifier
                                .height(44.dp)
                                .background(if (selected) Yellow else Color.White, CircleShape)
                                .border(1.dp, if (selected) Yellow else Line, CircleShape)
                                .clickable {
                                    if (selected) modalidades.remove(mod) else modalidades.add(mod)
                                }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                mod,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (selected) Ink else Muted,
                                fontFamily = Montserrat,
                            )
                        }
                    }
                }
            }
        }

        Column(Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Soft))
            Row(
                Modifier.fillMaxWidth().padding(20.dp, 12.dp, 20.dp, 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (onCancel != null) {
                    SecondaryButton("Cancelar", onClick = onCancel, modifier = Modifier.weight(1f))
                }
                PrimaryButton(
                    text = "Salvar e ver editais",
                    onClick = {
                        showErrors = true
                        if (keywords.isEmpty()) return@PrimaryButton
                        if (vmin != null && vmax != null && vmin > vmax) return@PrimaryButton
                        onSave(
                            MonitorConfig(
                                keywords = keywords.toList(),
                                ufs = ufs.toList(),
                                valorMin = vmin,
                                valorMax = vmax,
                                modalidades = modalidades.toList(),
                                updatedAt = OffsetDateTime.now().toString(),
                            ),
                        )
                    },
                    modifier = Modifier.weight(if (onCancel != null) 2f else 1f),
                )
            }
        }
        } // imePadding content
    }
}
