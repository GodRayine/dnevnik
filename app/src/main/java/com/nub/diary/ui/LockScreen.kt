package com.nub.diary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LockScreen(
    check: (String) -> Boolean,
    useBiometric: Boolean,
    onBiometric: () -> Unit,
    onUnlock: () -> Unit,
    padding: PaddingValues,
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var wrong by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { if (useBiometric) onBiometric() }

    Column(
        Modifier.fillMaxSize().padding(padding).padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Дневник закрыт", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 16.dp))
        OutlinedTextField(
            value = pin,
            onValueChange = { v ->
                pin = v.filter(Char::isDigit).take(8)
                wrong = false
                if (pin.length >= 4 && check(pin)) onUnlock()
            },
            label = { Text("PIN") },
            isError = wrong,
            supportingText = { if (wrong) Text("Неверный PIN") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        )
        Button(
            onClick = { if (check(pin)) onUnlock() else { wrong = true; pin = "" } },
            enabled = pin.length >= 4,
            modifier = Modifier.padding(top = 8.dp),
        ) { Text("Открыть") }
        if (useBiometric) TextButton(onClick = onBiometric) { Text("Отпечаток пальца") }
    }
}
