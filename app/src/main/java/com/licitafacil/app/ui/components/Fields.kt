package com.licitafacil.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.licitafacil.app.ui.theme.Danger
import com.licitafacil.app.ui.theme.DangerBg
import com.licitafacil.app.ui.theme.DangerBorder
import com.licitafacil.app.ui.theme.Dim
import com.licitafacil.app.ui.theme.Ink
import com.licitafacil.app.ui.theme.Line
import com.licitafacil.app.ui.theme.Montserrat
import com.licitafacil.app.ui.theme.Muted
import com.licitafacil.app.ui.theme.WarnBg
import com.licitafacil.app.ui.theme.WarnInk
import com.licitafacil.app.ui.theme.Yellow
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LfField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    error: Boolean = false,
) {
    val border = if (error) Danger else Line
    val bringIntoView = remember { BringIntoViewRequester() }
    val scope = rememberCoroutineScope()
    Row(
        modifier
            .height(48.dp)
            .border(2.dp, border, RoundedCornerShape(4.dp))
            .background(Color.White, RoundedCornerShape(4.dp))
            .padding(start = 12.dp, end = 4.dp)
            .bringIntoViewRequester(bringIntoView),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        leading?.invoke()
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(placeholder, color = Dim, fontSize = 15.sp, fontFamily = Montserrat)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(Ink),
                textStyle = TextStyle(color = Ink, fontSize = 15.sp, fontFamily = Montserrat),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusEvent { state ->
                        if (state.isFocused) {
                            scope.launch { bringIntoView.bringIntoView() }
                        }
                    },
            )
        }
        trailing?.invoke()
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier
            .height(48.dp)
            .background(if (enabled) Yellow else Yellow.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = Montserrat)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(48.dp)
            .border(1.dp, Line, RoundedCornerShape(4.dp))
            .background(Color.White, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = Montserrat)
    }
}

@Composable
fun ErrorBanner(title: String, body: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(DangerBg, RoundedCornerShape(4.dp))
            .border(width = 0.dp, color = Color.Transparent, shape = RoundedCornerShape(4.dp))
            .padding(top = 3.dp)
            .background(DangerBg, RoundedCornerShape(4.dp)),
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(3.dp).background(DangerBorder, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
            Row(Modifier.padding(14.dp, 14.dp, 16.dp, 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("!", color = Danger, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, color = Danger, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat)
                    Text(body, color = Muted, fontSize = 13.sp, fontFamily = Montserrat, lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
fun WarnBanner(title: String, body: String) {
    Column(Modifier.fillMaxWidth().background(WarnBg, RoundedCornerShape(4.dp))) {
        Box(Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFFFC700), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
        Row(Modifier.padding(14.dp, 14.dp, 16.dp, 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(20.dp).background(WarnInk, RoundedCornerShape(999.dp)), contentAlignment = Alignment.Center) {
                Text("!", color = WarnBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, color = WarnInk, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = Montserrat)
                Text(body, color = Muted, fontSize = 13.sp, fontFamily = Montserrat, lineHeight = 18.sp)
            }
        }
    }
}
