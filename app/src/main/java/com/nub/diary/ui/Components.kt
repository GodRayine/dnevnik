package com.nub.diary.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nub.diary.R

// animateColorAsState использует системный MotionDurationScale: при нулевом
// масштабе анимаций сразу показывает конечное состояние, без собственных таймеров.
private const val SelectionDurationMillis = 200

@Composable
internal fun PaperChoice(selected: Boolean, onClick: () -> Unit, label: String, enabled: Boolean = true) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (selected) colors.primaryContainer else colors.surface,
        animationSpec = tween(SelectionDurationMillis), label = "Выбор категории",
    )
    FilterChip(
        selected = selected, onClick = onClick, enabled = enabled,
        label = { Text(label) },
        leadingIcon = if (selected) ({ Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }) else null,
        modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = container, selectedContainerColor = container,
            labelColor = colors.onSurfaceVariant, selectedLabelColor = colors.onPrimaryContainer,
            selectedLeadingIconColor = colors.onPrimaryContainer,
            disabledContainerColor = colors.surfaceVariant, disabledLabelColor = colors.onSurfaceVariant,
            disabledSelectedContainerColor = colors.surfaceVariant, disabledLeadingIconColor = colors.onSurfaceVariant,
        ),
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outline),
    )
}

@Composable
internal fun PaperCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shadowElevation = 1.dp,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
internal fun PaperEmblem() {
    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.large) {
        Icon(
            painterResource(R.drawable.ic_notebook), contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(16.dp).size(32.dp),
        )
    }
}

@Composable
internal fun PageHeader(
    title: String, subtitle: String? = null, onBack: () -> Unit,
    backDescription: String = stringResource(R.string.back), action: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backDescription)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        action()
    }
}
