package com.nub.diary.ui

/**
 * Вход через защиту самого телефона: отпечаток, лицо, а при их отсутствии или отказе —
 * системный PIN, рисунок или пароль. Сам дневник эти данные не видит и не хранит.
 */
class PhoneAuth(
    val available: () -> Boolean,
    val authenticate: (title: String, onSuccess: () -> Unit) -> Unit,
)
