package com.licitafacil.app.ui.util

import com.licitafacil.app.data.model.MonitorConfig
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private val money = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
private val outDateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")
private val outDateShort = DateTimeFormatter.ofPattern("dd/MM · HH:mm")
private val outDateOnly = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun formatMoney(value: Double?): String = value?.let { money.format(it) } ?: "Não informado"

/**
 * Código 1 / "sem sigilo" = público.
 * Não usar contains("sigilos") cru — "Compra sem sigilo" também contém.
 */
fun isOrcamentoSigiloso(
    flag: Boolean? = null,
    codigo: Long? = null,
    descricao: String? = null,
): Boolean {
    val d = descricao?.lowercase().orEmpty()
    if (codigo == 1L || d.contains("sem sigilo")) return false
    if (flag == true) return true
    if (codigo != null && codigo != 1L) return true
    if (d.contains("sigilos")) return true
    return false
}

/** Valor do edital: só “Orçamento sigiloso” quando for sigiloso de verdade. */
fun formatValorEdital(
    valor: Double?,
    sigiloso: Boolean? = null,
    codigo: Long? = null,
    descricao: String? = null,
): String {
    if (isOrcamentoSigiloso(sigiloso, codigo, descricao)) return "Orçamento sigiloso"
    if (valor == null) return "Não informado"
    if (valor <= 0.0) return "Orçamento sigiloso"
    return money.format(valor)
}

/** Digitação tipo caixa: só dígitos → `0,00` / `1.234,56`. Vazio se apagar tudo. */
fun moneyMaskInput(raw: String): String {
    val digits = raw.filter { it.isDigit() }.trimStart('0').take(15)
    if (digits.isEmpty()) return ""
    val padded = digits.padStart(3, '0')
    val cents = padded.takeLast(2)
    val intPart = padded.dropLast(2).ifEmpty { "0" }
    val grouped = intPart.reversed().chunked(3).joinToString(".").reversed()
    return "$grouped,$cents"
}

fun moneyMaskFromDouble(value: Double?): String {
    if (value == null) return ""
    val cents = Math.round(value * 100.0).coerceAtLeast(0L).toString()
    return moneyMaskInput(cents)
}

fun moneyMaskToDouble(masked: String): Double? {
    val digits = masked.filter { it.isDigit() }
    if (digits.isEmpty()) return null
    return digits.toLongOrNull()?.toDouble()?.div(100.0)
}

fun parseDateTime(raw: String?): LocalDateTime? {
    if (raw.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(raw).toLocalDateTime() }.getOrNull()
        ?: runCatching { LocalDateTime.parse(raw) }.getOrNull()
        ?: runCatching { LocalDate.parse(raw.take(10)).atStartOfDay() }.getOrNull()
}

fun formatPncpDate(raw: String?, short: Boolean = false): String {
    val dt = parseDateTime(raw) ?: return raw ?: "—"
    return dt.format(if (short) outDateShort else outDateTime)
}

fun daysUntil(raw: String?): Long? {
    val dt = parseDateTime(raw) ?: return null
    return ChronoUnit.DAYS.between(LocalDate.now(), dt.toLocalDate())
}

fun encerraLabel(raw: String?): String? {
    val days = daysUntil(raw) ?: return null
    return when {
        days < 0 -> "Encerrado"
        days == 0L -> "Encerra hoje"
        days == 1L -> "Encerra em 1 dia"
        else -> "Encerra em $days dias"
    }
}

fun faltamLabel(raw: String?): String? {
    val days = daysUntil(raw) ?: return null
    return when {
        days < 0 -> "Encerrado"
        days == 0L -> "Encerra hoje"
        days == 1L -> "Falta 1 dia"
        else -> "Faltam $days dias"
    }
}

fun initialsOf(nome: String?): String {
    val parts = nome.orEmpty().trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "EU"
    return parts.take(2).joinToString("") { it.first().uppercaseChar().toString() }
}

fun configSummary(c: MonitorConfig): String {
    val parts = mutableListOf<String>()
    if (c.keywords.isNotEmpty()) parts += c.keywords.joinToString(", ")
    if (c.ufs.isNotEmpty()) parts += c.ufs.joinToString(", ")
    when {
        c.valorMin != null && c.valorMax != null -> parts += "${formatMoney(c.valorMin)} – ${formatMoney(c.valorMax)}"
        c.valorMin != null -> parts += "acima de ${formatMoney(c.valorMin)}"
        c.valorMax != null -> parts += "até ${formatMoney(c.valorMax)}"
    }
    return parts.joinToString(" · ").ifBlank { "Sem filtros" }
}

fun valorResumo(c: MonitorConfig): String = when {
    c.valorMin != null && c.valorMax != null -> "${formatMoney(c.valorMin)} – ${formatMoney(c.valorMax)}"
    c.valorMin != null -> "A partir de ${formatMoney(c.valorMin)}"
    c.valorMax != null -> "Até ${formatMoney(c.valorMax)}"
    else -> "Sem limite"
}

fun formatUpdatedAt(iso: String?): String {
    if (iso.isNullOrBlank()) return "—"
    val dt = parseDateTime(iso) ?: return iso
    return "Atualizado em ${dt.format(outDateOnly)}"
}
