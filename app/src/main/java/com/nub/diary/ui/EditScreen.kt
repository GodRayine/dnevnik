package com.nub.diary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.nub.diary.MainViewModel
import com.nub.diary.Screen
import com.nub.diary.data.Entry

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
    androidx.activity.compose.BackHandler { close() }

    Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = ::close) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад и сохранить")
            }
            Text(
                original?.let(::formatDate) ?: "Новая запись",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (existing != null && savedId == id) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(!diary, { diary = false }, label = { Text("Заметка") })
            FilterChip(diary, { diary = true }, label = { Text("Дневник") })
        }
        val transparent = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedIndicatorColor = MaterialTheme.colorScheme.background,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.background,
        )
        TextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("Заголовок") },
            textStyle = MaterialTheme.typography.titleLarge,
            singleLine = true,
            colors = transparent,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            value = body,
            onValueChange = { body = it },
            placeholder = { Text(if (diary) "Как прошёл день?" else "Текст заметки") },
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = transparent,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить запись?") },
            text = { Text("Восстановить её не получится.") },
            confirmButton = {
                TextButton(onClick = {
                    existing?.let(vm::delete)
                    vm.screen = Screen.List
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Отмена") } },
        )
    }
}
