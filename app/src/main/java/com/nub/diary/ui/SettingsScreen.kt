package com.nub.diary.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.nub.diary.BuildConfig
import com.nub.diary.MainViewModel
import com.nub.diary.R
import com.nub.diary.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(vm: MainViewModel, biometricAvailable: Boolean, padding: PaddingValues) {
    var pinDialog by rememberSaveable { mutableStateOf(false) }

    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let(vm::export)
    }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::import)
    }

    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "header") {
                PageHeader(
                    title = stringResource(R.string.settings),
                    subtitle = stringResource(R.string.settings_subtitle),
                    onBack = { vm.screen = Screen.List },
                )
            }
            item(key = "security") {
                PaperCard {
                    SectionTitle(stringResource(R.string.security_title))
                    SupportingText(stringResource(if (vm.hasPin) R.string.security_on else R.string.security_off))
                    if (vm.hasPin) {
                        // Вся строка — одна доступная цель; Switch не дублирует её в TalkBack.
                        Row(
                            Modifier.fillMaxWidth().sizeIn(minHeight = 56.dp)
                                .toggleable(
                                    value = vm.biometric, enabled = biometricAvailable,
                                    role = Role.Switch, onValueChange = vm::enableBiometric,
                                ).padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(R.string.fingerprint_title), style = MaterialTheme.typography.titleSmall)
                                SupportingText(
                                    stringResource(if (biometricAvailable) R.string.fingerprint_hint else R.string.fingerprint_unavailable)
                                )
                            }
                            Switch(
                                checked = vm.biometric, onCheckedChange = null, enabled = biometricAvailable,
                                modifier = Modifier.clearAndSetSemantics { },
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { pinDialog = true }, shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) { Text(stringResource(if (vm.hasPin) R.string.change_pin else R.string.set_pin)) }
                    if (vm.hasPin) {
                        TextButton(
                            onClick = vm::removeLock,
                            modifier = Modifier.fillMaxWidth().sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) { Text(stringResource(R.string.remove_security), color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
            item(key = "autosave") {
                PaperCard {
                    SectionTitle(stringResource(R.string.autosave_title))
                    SupportingText(stringResource(R.string.autosave_hint))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(5 to R.string.interval_5, 10 to R.string.interval_10, 15 to R.string.interval_15, 0 to R.string.interval_off)
                            .forEach { (minutes, label) ->
                                PaperChoice(vm.autosaveMinutes == minutes, { vm.setAutosave(minutes) }, stringResource(label))
                            }
                    }
                }
            }
            item(key = "backup") {
                PaperCard {
                    SectionTitle(stringResource(R.string.backup_title))
                    SupportingText(stringResource(R.string.backup_hint))
                    OutlinedButton(
                        onClick = {
                            vm.skipRelock = true
                            exporter.launch("dnevnik-" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + ".json")
                        },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) { Text(stringResource(R.string.export_backup)) }
                    TextButton(
                        onClick = {
                            vm.skipRelock = true
                            importer.launch(arrayOf("application/json", "*/*"))
                        },
                        modifier = Modifier.fillMaxWidth().sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                    ) { Text(stringResource(R.string.import_backup)) }
                }
            }
            item(key = "about") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SupportingText(stringResource(R.string.local_storage_hint))
                    SupportingText(stringResource(R.string.system_theme_hint))
                    SupportingText(stringResource(R.string.version_label, BuildConfig.VERSION_NAME))
                }
            }
        }
    }

    if (pinDialog) PinDialog(onDone = { vm.setPin(it); pinDialog = false }, onDismiss = { pinDialog = false })
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun SupportingText(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun PinDialog(onDone: (String) -> Unit, onDismiss: () -> Unit) {
    var pin by rememberSaveable { mutableStateOf("") }
    var again by rememberSaveable { mutableStateOf("") }
    val valid = pin.length >= 4 && pin == again
    val mismatch = again.isNotEmpty() && pin != again
    val focus = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_pin_title)) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SupportingText(stringResource(R.string.new_pin_hint))
                OutlinedTextField(
                    value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(8) },
                    label = { Text(stringResource(R.string.pin_label)) },
                    singleLine = true, shape = MaterialTheme.shapes.small,
                    supportingText = { Text(stringResource(R.string.pin_hint)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = again, onValueChange = { again = it.filter(Char::isDigit).take(8) },
                    label = { Text(stringResource(R.string.repeat_pin)) },
                    singleLine = true, shape = MaterialTheme.shapes.small, isError = mismatch,
                    supportingText = { Text(stringResource(if (mismatch) R.string.pin_mismatch else R.string.repeat_pin_hint)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (valid) onDone(pin) }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDone(pin) }, enabled = valid,
                colors = ButtonDefaults.textButtonColors(disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
