package com.nub.diary.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nub.diary.BuildConfig
import com.nub.diary.MainViewModel
import com.nub.diary.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(vm: MainViewModel, biometricAvailable: Boolean, padding: PaddingValues) {
    var pinDialog by rememberSaveable { mutableStateOf(false) }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(vm::export)
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::import)
    }

    Column(Modifier.fillMaxSize().padding(padding)) {
        Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { vm.screen = Screen.List }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
            }
            Text("Настройки", style = MaterialTheme.typography.titleLarge)
        }

        Column(Modifier.padding(16.dp)) {
            Text("Защита", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (vm.hasPin) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Вход по отпечатку", Modifier.weight(1f))
                    Switch(
                        checked = vm.biometric,
                        onCheckedChange = vm::setBiometric,
                        enabled = biometricAvailable,
                    )
                }
                if (!biometricAvailable) {
                    Text(
                        "Отпечаток не настроен в системе телефона.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(Modifier.padding(top = 8.dp)) {
                    OutlinedButton(onClick = { pinDialog = true }) { Text("Сменить PIN") }
                    TextButton(onClick = vm::removeLock) { Text("Убрать защиту") }
                }
            } else {
                OutlinedButton(onClick = { pinDialog = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Установить PIN")
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 20.dp))

            Text("Автосохранение", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                "Открытая запись сохраняется сама, а копия всех записей кладётся в память телефона (последние 5). Работает, пока приложение открыто.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5 to "5 мин", 10 to "10 мин", 15 to "15 мин", 0 to "Выкл.").forEach { (minutes, label) ->
                    FilterChip(vm.autosaveMinutes == minutes, { vm.setAutosave(minutes) }, label = { Text(label) })
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 20.dp))

            Text("Резервная копия", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(
                "Файл JSON со всеми записями. Он не зашифрован — храните его в надёжном месте.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(Modifier.padding(top = 8.dp)) {
                OutlinedButton(onClick = {
                    vm.skipRelock = true
                    exporter.launch("dnevnik-" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + ".json")
                }) { Text("Сохранить") }
                TextButton(onClick = {
                    vm.skipRelock = true
                    importer.launch(arrayOf("application/json", "*/*"))
                }) { Text("Загрузить") }
            }

            HorizontalDivider(Modifier.padding(vertical = 20.dp))
            Text(
                "Версия ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (pinDialog) PinDialog(onDone = { vm.setPin(it); pinDialog = false }, onDismiss = { pinDialog = false })
}

@Composable
private fun PinDialog(onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var again by rememberSaveable { mutableStateOf("") }
    val valid = pin.length >= 4 && pin == again
    val keyboard = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый PIN") },
        text = {
            Column {
                OutlinedTextField(
                    pin, { pin = it.filter(Char::isDigit).take(8) },
                    label = { Text("4–8 цифр") }, singleLine = true,
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = keyboard,
                )
                OutlinedTextField(
                    again, { again = it.filter(Char::isDigit).take(8) },
                    label = { Text("Повторите PIN") }, singleLine = true,
                    isError = again.isNotEmpty() && pin != again,
                    visualTransformation = PasswordVisualTransformation(), keyboardOptions = keyboard,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        confirmButton = { TextButton(onClick = { onDone(pin) }, enabled = valid) { Text("Сохранить") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
    )
}
