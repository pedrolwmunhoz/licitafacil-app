package com.licitafacil.app.ui.conta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.licitafacil.app.data.model.MonitorConfig
import com.licitafacil.app.data.model.Session
import com.licitafacil.app.ui.components.BackHeader
import com.licitafacil.app.ui.components.PrimaryButton
import com.licitafacil.app.ui.components.SecondaryButton
import com.licitafacil.app.ui.theme.Dim
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.theme.Montserrat
import com.licitafacil.app.ui.theme.Muted
import com.licitafacil.app.ui.theme.Panel
import com.licitafacil.app.ui.theme.Soft
import com.licitafacil.app.ui.theme.Yellow
import com.licitafacil.app.ui.util.formatUpdatedAt
import com.licitafacil.app.ui.util.initialsOf
import com.licitafacil.app.ui.util.valorResumo

@Composable
fun ContaScreen(
    session: Session?,
    config: MonitorConfig,
    onConfig: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Panel)) {
        BackHeader("Minha conta", onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp, 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Soft, RoundedCornerShape(8.dp)).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    Modifier.size(56.dp).background(Yellow, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(initialsOf(session?.nome), color = Ink, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, fontFamily = Montserrat)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(session?.nome ?: "—", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, fontFamily = Montserrat)
                    Text(session?.email ?: "—", fontSize = 14.sp, color = Muted, fontFamily = Montserrat)
                }
            }

            Column(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)).border(1.dp, Soft, RoundedCornerShape(8.dp)).padding(16.dp, 16.dp, 16.dp, 4.dp),
            ) {
                Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("MEU MONITORAMENTO", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.14.em, color = Dim, fontFamily = Montserrat)
                    Text(formatUpdatedAt(config.updatedAt), fontSize = 12.sp, color = Dim, fontFamily = Montserrat)
                }
                InfoLine("Palavras-chave", config.keywords.joinToString(", ").ifBlank { "—" })
                InfoLine("Estados", config.ufs.joinToString(", ").ifBlank { "Brasil todo" })
                InfoLine("Valor", valorResumo(config))
                InfoLine("Modalidades", config.modalidades.joinToString(", ").ifBlank { "Todas" })
            }

            PrimaryButton(
                text = "Configurar o que quero ver",
                onClick = onConfig,
                modifier = Modifier.fillMaxWidth(),
            )
            SecondaryButton(
                text = "Sair",
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Pra trocar senha ou e-mail, fale com o suporte.",
                fontSize = 13.sp,
                color = Dim,
                fontFamily = Montserrat,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Soft))
        Column(Modifier.padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Dim, fontFamily = Montserrat)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink, fontFamily = Montserrat)
        }
    }
}
