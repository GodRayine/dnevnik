package com.nub.diary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nub.diary.MainViewModel
import com.nub.diary.Screen
import com.nub.diary.data.Entry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
fun formatDate(ms: Long): String = dateFormat.format(Date(ms))

@Composable
fun ListScreen(vm: MainViewModel, padding: PaddingValues) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(padding)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text("Дневник", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = { vm.screen = Screen.Settings }) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { vm.query.value = it },
            placeholder = { Text("Поиск") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(filter == null, { vm.filter.value = null }, label = { Text("Все") })
            FilterChip(filter == false, { vm.filter.value = false }, label = { Text("Заметки") })
            FilterChip(filter == true, { vm.filter.value = true }, label = { Text("Дневник") })
        }

        if (entries.isEmpty()) {
            Text(
                if (query.isBlank()) "Пока пусто. Нажмите «Написать»." else "Ничего не найдено.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp),
            )
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(entries, key = { it.id }) { EntryCard(it) { vm.screen = Screen.Edit(it.id) } }
        }
    }

    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.Bottom, horizontalAlignment = androidx.compose.ui.Alignment.End) {
        ExtendedFloatingActionButton(
            onClick = { vm.screen = Screen.Edit(null) },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            text = { Text("Написать") },
        )
    }
}

@Composable
private fun EntryCard(entry: Entry, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                (if (entry.isDiary) "Дневник · " else "Заметка · ") + formatDate(entry.createdAt),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (entry.title.isNotBlank()) {
                Text(entry.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            }
            if (entry.body.isNotBlank()) {
                Text(
                    entry.body,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
