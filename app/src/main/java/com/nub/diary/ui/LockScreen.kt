package com.nub.diary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nub.diary.R

@Composable
fun LockScreen(
    hasPin: Boolean,
    check: (String) -> Boolean,
    useBiometric: Boolean,
    onBiometric: () -> Unit,
    usePhoneLock: Boolean,
    onPhoneLock: () -> Unit,
    onUnlock: () -> Unit,
    padding: PaddingValues,
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var wrong by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (usePhoneLock) onPhoneLock() else if (useBiometric) onBiometric()
    }

    fun submit() {
        if (pin.length < 4) return
        if (check(pin)) onUnlock() else { wrong = true; pin = "" }
    }

    // Минимальная высота центрирует форму; при клавиатуре и крупном шрифте
    // содержимое может вырасти и прокрутиться, без фиксированной высоты карточки.
    BoxWithConstraints(
        Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier.widthIn(max = 440.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PaperEmblem()
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.lock_title), style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() },
                )
                Text(
                    stringResource(R.string.lock_hint), style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                )
            }
            PaperCard {
                if (hasPin) OutlinedTextField(
                    value = pin,
                    onValueChange = { value ->
                        pin = value.filter(Char::isDigit).take(8)
                        wrong = false
                        if (pin.length >= 4 && check(pin)) onUnlock()
                    },
                    label = { Text(stringResource(R.string.pin_label)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    isError = wrong,
                    supportingText = { Text(stringResource(if (wrong) R.string.wrong_pin else R.string.pin_hint)) },
                    singleLine = true, shape = MaterialTheme.shapes.small,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (hasPin) Button(
                    onClick = ::submit, enabled = pin.length >= 4,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth().sizeIn(minWidth = 48.dp, minHeight = 52.dp),
                ) { Text(stringResource(R.string.unlock)) }
                if (usePhoneLock) {
                    OutlinedButton(
                        onClick = onPhoneLock, shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().sizeIn(minWidth = 48.dp, minHeight = 52.dp),
                    ) { Text(stringResource(R.string.use_phone_lock)) }
                }
                if (useBiometric) {
                    OutlinedButton(
                        onClick = onBiometric, shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().sizeIn(minWidth = 48.dp, minHeight = 52.dp),
                    ) { Text(stringResource(R.string.use_fingerprint)) }
                }
            }
        }
    }
}
