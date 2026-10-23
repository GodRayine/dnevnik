package com.nub.diary.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.nub.diary.MainViewModel
import com.nub.diary.R
import com.nub.diary.Screen
import com.nub.diary.data.Entry

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditScreen(vm: MainViewModel, id: Long?, padding: PaddingValues) {
    var loaded by rememberSaveable { mutableStateOf(id == null) }
    var original by rememberSaveable { mutableStateOf<Long?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var diary by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var existing by remember { mutableStateOf<Entry?>(null) }

    LaunchedEffect(id) {
        if (id != null) {
            vm.load(id)?.let {
                existing = it
                if (!loaded) {
                    title = it.title; body = it.body; diary = it.isDiary; original = it.createdAt
                    loaded = true
                }
            }
        }
    }

    // Новая запись после первого автосохранения получает id, дальше обновляется она же.
    var savedId by rememberSaveable { mutableStateOf(id) }

    LaunchedEffect(vm.autosaveMinutes) {
        if (vm.autosaveMinutes == 0) return@LaunchedEffect
        while (true) {
            delay(vm.autosaveMinutes * 60_000L)
            if (!loaded) continue
            vm.saveNow(savedId, title, body, diary, original)?.let {
                savedId = it.id
                original = it.createdAt
            }
        }
    }

    // Запись сохраняется при выходе, отдельной кнопки «Сохранить» нет.
    fun close() {
        if (loaded) vm.save(savedId, title, body, diary, original)
        vm.screen = Screen.List
    }
    BackHandler { close() }

    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 720.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PageHeader(
                title = stringResource(if (id == null) R.string.new_entry else R.string.edit_entry),
                subtitle = original?.let(::formatDate) ?: stringResource(R.string.new_page),
                onBack = ::close,
                backDescription = stringResource(R.string.back_save),
                action = {
                    if (existing != null && savedId == id) {
                        IconButton(
                            onClick = { confirmDelete = true },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) {
                            Icon(
                                Icons.Default.Delete, contentDescription = stringResource(R.string.delete_entry),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
            if (!loaded) {
                PaperCard { Text(stringResource(R.string.entry_loading)) }
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaperChoice(!diary, { diary = false }, stringResource(R.string.entry_note))
                    PaperChoice(diary, { diary = true }, stringResource(R.string.filter_diary))
                }
                PaperCard {
                    val transparent = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                    TextField(
                        value = title, onValueChange = { title = it },
                        label = { Text(stringResource(R.string.entry_title)) },
                        textStyle = MaterialTheme.typography.headlineMedium,
                        colors = transparent,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    TextField(
                        value = body, onValueChange = { body = it },
                        label = { Text(stringResource(R.string.entry_body)) },
                        placeholder = { Text(stringResource(if (diary) R.string.diary_prompt else R.string.note_prompt)) },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = transparent, minLines = 8,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.save_on_exit), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        if (vm.autosaveMinutes == 0) stringResource(R.string.autosave_off_status)
                        else stringResource(R.string.autosave_status, vm.autosaveMinutes),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.delete_title)) },
            text = { Text(stringResource(R.string.delete_hint)) },
            confirmButton = {
                TextButton(
                    onClick = { existing?.let(vm::delete); vm.screen = Screen.List },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) { Text(stringResource(R.string.delete_action), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(
                    onClick = { confirmDelete = false },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}
