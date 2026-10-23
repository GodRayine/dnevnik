package com.nub.diary.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nub.diary.MainViewModel
import com.nub.diary.R
import com.nub.diary.Screen

@Composable
fun DiaryApp(vm: MainViewModel, onBiometric: () -> Unit, canBiometric: () -> Boolean, phone: PhoneAuth) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(vm.message) {
        vm.message?.let {
            snackbar.showSnackbar(it)
            vm.message = null
        }
    }

    // Если защиту телефона сняли, а своего PIN нет, войти иначе нечем: открываем дневник.
    val phoneLockLost = vm.deviceLock && !vm.hasPin && !phone.available()
    val phoneLockLostMessage = stringResource(R.string.phone_lock_lost)
    LaunchedEffect(phoneLockLost, vm.locked) {
        if (phoneLockLost && vm.locked) {
            vm.locked = false
            vm.message = phoneLockLostMessage
        }
    }

    val uriHandler = LocalUriHandler.current
    vm.update?.let { info ->
        AlertDialog(
            onDismissRequest = { vm.update = null },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text(stringResource(R.string.update_title)) },
            text = { Text(stringResource(R.string.update_hint, info.version)) },
            confirmButton = {
                TextButton(
                    onClick = { uriHandler.openUri(info.pageUrl); vm.update = null },
                    modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp),
                ) { Text(stringResource(R.string.open_update)) }
            },
            dismissButton = {
                TextButton(onClick = { vm.update = null }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                    Text(stringResource(R.string.update_later))
                }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            if (vm.locked) {
                val unlockTitle = stringResource(R.string.unlock_prompt_title)
                LockScreen(
                    hasPin = vm.hasPin,
                    check = vm.lock::check,
                    useBiometric = vm.hasPin && vm.biometric && canBiometric(),
                    onBiometric = onBiometric,
                    usePhoneLock = vm.deviceLock && phone.available(),
                    onPhoneLock = { phone.authenticate(unlockTitle) { vm.locked = false } },
                    onUnlock = { vm.locked = false },
                    padding = padding,
                )
            } else {
                BackHandler(enabled = vm.screen != Screen.List) { vm.screen = Screen.List }
                when (val s = vm.screen) {
                    Screen.List -> ListScreen(vm, padding)
                    is Screen.Edit -> EditScreen(vm, s.id, padding)
                    Screen.Settings -> SettingsScreen(vm, canBiometric(), phone, padding)
                }
            }
        }
    }
}
