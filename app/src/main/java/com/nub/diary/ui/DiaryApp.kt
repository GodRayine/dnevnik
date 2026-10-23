package com.nub.diary.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.nub.diary.MainViewModel
import com.nub.diary.Screen

@Composable
fun DiaryApp(vm: MainViewModel, onBiometric: () -> Unit, canBiometric: () -> Boolean) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.message = null
        }
    }

    val uriHandler = LocalUriHandler.current
    vm.update?.let { info ->
        AlertDialog(
            onDismissRequest = { vm.update = null },
            title = { Text("Доступно обновление") },
            text = { Text("Версия ${info.version}. Откроется страница релиза в браузере; приложение само ничего не скачивает.") },
            confirmButton = {
                TextButton(onClick = { uriHandler.openUri(info.pageUrl); vm.update = null }) { Text("Открыть") }
            },
            dismissButton = { TextButton(onClick = { vm.update = null }) { Text("Позже") } },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            if (vm.locked) {
                LockScreen(
                    check = vm.lock::check,
                    useBiometric = vm.biometric && canBiometric(),
                    onBiometric = onBiometric,
                    onUnlock = { vm.locked = false },
                    padding = padding,
                )
            } else {
                BackHandler(enabled = vm.screen != Screen.List) { vm.screen = Screen.List }
                when (val s = vm.screen) {
                    Screen.List -> ListScreen(vm, padding)
                    is Screen.Edit -> EditScreen(vm, s.id, padding)
                    Screen.Settings -> SettingsScreen(vm, canBiometric(), padding)
                }
            }
        }
    }
}
