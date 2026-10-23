package com.nub.diary.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nub.diary.MainViewModel
import com.nub.diary.R
import com.nub.diary.Screen
import com.nub.diary.data.Entry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru"))
fun formatDate(ms: Long): String = dateFormat.format(Date(ms))

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListScreen(vm: MainViewModel, padding: PaddingValues) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val filter by vm.filter.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding(), contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = 720.dp).fillMaxSize()) {
            // Шапка тоже прокручивается: в альбомной ориентации остаётся место для записей.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "header") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge,
                                modifier = Modifier.semantics { heading() },
                            )
                            Text(
                                stringResource(R.string.list_subtitle), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        IconButton(
                            onClick = { vm.screen = Screen.Settings },
                            modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                        ) { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings)) }
                    }
                }
                item(key = "search") {
                    OutlinedTextField(
                        value = query, onValueChange = { vm.query.value = it },
                        label = { Text(stringResource(R.string.search_label)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = if (query.isNotEmpty()) ({
                            IconButton(
                                onClick = { vm.query.value = "" },
                                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                            ) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search)) }
                        }) else null,
                        singleLine = true, shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item(key = "filters") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaperChoice(filter == null, { vm.filter.value = null }, stringResource(R.string.filter_all))
                        PaperChoice(filter == false, { vm.filter.value = false }, stringResource(R.string.filter_notes))
                        PaperChoice(filter == true, { vm.filter.value = true }, stringResource(R.string.filter_diary))
                    }
                }
                if (entries.isEmpty()) {
                    item(key = "empty") {
                        PaperCard {
                            PaperEmblem()
                            Text(
                                stringResource(if (query.isNotBlank()) R.string.search_empty_title else R.string.empty_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                stringResource(
                                    when {
                                        query.isNotBlank() -> R.string.search_empty_hint
                                        filter != null -> R.string.filter_empty_hint
                                        else -> R.string.empty_hint
                                    }
                                ),
                                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (query.isNotEmpty() || filter != null) {
                                TextButton(
                                    onClick = { vm.query.value = ""; vm.filter.value = null },
                                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                                ) { Text(stringResource(R.string.reset_filters)) }
                            }
                        }
                    }
                } else {
                    item(key = "count") {
                        Text(
                            pluralStringResource(R.plurals.entry_count, entries.size, entries.size),
                            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    items(entries, key = { it.id }) { entry ->
                        EntryCard(entry) { vm.screen = Screen.Edit(entry.id) }
                    }
                }
            }
            ExtendedFloatingActionButton(
                onClick = { vm.screen = Screen.Edit(null) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.write_entry)) },
                containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).sizeIn(minWidth = 48.dp, minHeight = 56.dp),
            )
        }
    }
}

@Composable
private fun EntryCard(entry: Entry, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, colors.outlineVariant),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                color = if (entry.isDiary) colors.primaryContainer else colors.secondaryContainer,
                contentColor = if (entry.isDiary) colors.onPrimaryContainer else colors.onSecondaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    stringResource(if (entry.isDiary) R.string.filter_diary else R.string.entry_note),
                    style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Text(
                entry.title.ifBlank { stringResource(R.string.untitled) },
                style = MaterialTheme.typography.titleLarge, maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
            if (entry.body.isNotBlank()) {
                Text(
                    entry.body, maxLines = 3, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant,
                )
            }
            Text(
                formatDate(entry.createdAt), style = MaterialTheme.typography.labelMedium,
                color = colors.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
