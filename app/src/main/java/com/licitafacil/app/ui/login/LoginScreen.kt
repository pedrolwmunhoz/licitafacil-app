package com.licitafacil.app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.licitafacil.app.ui.components.BrandMark
import com.licitafacil.app.ui.components.ErrorBanner
import com.licitafacil.app.ui.components.LfField
import com.licitafacil.app.ui.components.PixelRadarDecor
import com.licitafacil.app.ui.components.PrimaryButton
import com.licitafacil.app.ui.theme.Dim
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.theme.Montserrat
import com.licitafacil.app.ui.theme.Muted
import com.licitafacil.app.ui.theme.Yellow

@Composable
fun LoginScreen(
    loading: Boolean,
    error: String?,
    serverErrorPopup: Boolean,
    onDismissServerError: () -> Unit,
    onLogin: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var showSenha by remember { mutableStateOf(false) }
    val inactive = error?.startsWith("INACTIVE:") == true
    val inactiveParts = error?.removePrefix("INACTIVE:")?.split("|")
    val plainError = if (!inactive && !error.isNullOrBlank()) error else null
    val scroll = rememberScrollState()

    if (serverErrorPopup) {
        AlertDialog(
            onDismissRequest = onDismissServerError,
            title = {
                Text("Erro no servidor", fontWeight = FontWeight.Bold, fontFamily = Montserrat)
            },
            text = {
                Text(
                    "Não foi possível conectar ao servidor de login. Tente novamente em instantes.",
                    fontFamily = Montserrat,
                    color = Muted,
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissServerError) {
                    Text("OK", fontFamily = Montserrat, fontWeight = FontWeight.Bold, color = Ink)
                }
            },
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(scroll),
    ) {
        Column(
            Modifier.padding(24.dp, 24.dp, 24.dp, 36.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                BrandMark(box = 32.dp, textSize = 18)
                PixelRadarDecor()
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "EDITAIS DO PNCP",
                    color = Yellow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.14.em,
                    fontFamily = Montserrat,
                )
                Text(
                    "Editais abertos, filtrados pro seu negócio.",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 32.sp,
                    letterSpacing = (-0.02).em,
                    fontFamily = Montserrat,
                )
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(24.dp, 32.dp, 24.dp, 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Entrar", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Ink, fontFamily = Montserrat, letterSpacing = (-0.015).em)
                Text(
                    "Use o e-mail e a senha que o suporte enviou pra você.",
                    color = Muted,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontFamily = Montserrat,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("E-mail", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink, fontFamily = Montserrat)
                LfField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "voce@empresa.com.br",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Senha", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink, fontFamily = Montserrat)
                LfField(
                    value = senha,
                    onValueChange = { senha = it },
                    placeholder = "Sua senha",
                    visualTransformation = if (showSenha) VisualTransformation.None else PasswordVisualTransformation(),
                    trailing = {
                        Box(Modifier.size(40.dp).clickable { showSenha = !showSenha }, contentAlignment = Alignment.Center) {
                            Icon(
                                if (showSenha) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (showSenha) "Ocultar senha" else "Mostrar senha",
                                tint = Muted,
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (inactive && inactiveParts != null && inactiveParts.size >= 2) {
                ErrorBanner(inactiveParts[0], inactiveParts[1])
            } else if (plainError != null) {
                ErrorBanner("Não foi possível entrar", plainError)
            }
            PrimaryButton(
                text = if (loading) "Entrando..." else "Entrar",
                onClick = { onLogin(email, senha) },
                enabled = !loading && email.isNotBlank() && senha.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Dim, fontSize = 13.sp, fontFamily = Montserrat)) {
                        append("Esqueceu a senha ou não tem acesso?\n")
                    }
                    withStyle(SpanStyle(color = Ink, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = Montserrat)) {
                        append("Fale com o suporte.")
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 20.sp,
            )
        }
    }
}
