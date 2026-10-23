package com.nub.diary

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nub.diary.data.Backup
import com.nub.diary.data.DiaryDatabase
import com.nub.diary.data.Entry
import com.nub.diary.data.LockStore
import com.nub.diary.data.UpdateInfo
import com.nub.diary.data.Updater
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface Screen {
    data object List : Screen
    data class Edit(val id: Long?) : Screen
    data object Settings : Screen
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = DiaryDatabase.get(app).dao()
    val lock = LockStore(app)
    private val settings = app.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var screen: Screen by mutableStateOf(Screen.List)
    var locked by mutableStateOf(lock.hasPin)
    var hasPin by mutableStateOf(lock.hasPin)
        private set
    var biometric by mutableStateOf(lock.biometric)
        private set
    var message: String? by mutableStateOf(null)

    /** Системные окна выбора файла сворачивают приложение — не блокируем из-за них. */
    var skipRelock = false

    val query = MutableStateFlow("")
    /** null — всё, true — только дневник, false — только заметки. */
    val filter = MutableStateFlow<Boolean?>(null)

    val entries: StateFlow<List<Entry>> = combine(query, filter) { q, f -> q to f }
        .flatMapLatest { (q, f) -> dao.observe(q.trim(), f) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    suspend fun load(id: Long): Entry? = dao.get(id)

    suspend fun saveNow(id: Long?, title: String, body: String, isDiary: Boolean, createdAt: Long?): Entry? {
        if (title.isBlank() && body.isBlank()) return null
        val now = System.currentTimeMillis()
        val entry = Entry(
            id = id ?: 0,
            title = title.trim(),
            body = body,
            isDiary = isDiary,
            createdAt = createdAt ?: now,
            updatedAt = now,
        )
        val newId = dao.upsert(entry)
        return entry.copy(id = if (id == null) newId else id)
    }

    fun save(id: Long?, title: String, body: String, isDiary: Boolean, createdAt: Long?) {
        viewModelScope.launch { saveNow(id, title, body, isDiary, createdAt) }
    }

    /** Интервал автосохранения в минутах; 0 — выключено. */
    var autosaveMinutes by mutableIntStateOf(settings.getInt(KEY_AUTOSAVE, 5))
        private set
    private var autosaveJob: Job? = null
    private var lastBackupHash = 0

    fun setAutosave(minutes: Int) {
        settings.edit().putInt(KEY_AUTOSAVE, minutes).apply()
        autosaveMinutes = minutes
        startAutosave()
    }

    private fun startAutosave() {
        autosaveJob?.cancel()
        if (autosaveMinutes == 0) return
        autosaveJob = viewModelScope.launch {
            while (true) {
                delay(autosaveMinutes * 60_000L)
                withContext(Dispatchers.IO) { runCatching { backupToDevice() } }
            }
        }
    }

    /** Копия лежит в закрытой папке приложения, хранятся последние пять. */
    private suspend fun backupToDevice() {
        val json = Backup.toJson(dao.all())
        if (json.hashCode() == lastBackupHash) return
        val dir = File(getApplication<Application>().filesDir, "autosave").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        File(dir, "dnevnik-auto-$stamp.json").writeText(json)
        lastBackupHash = json.hashCode()
        dir.listFiles()?.sortedBy { it.name }?.dropLast(KEEP_BACKUPS)?.forEach { it.delete() }
    }

    var update: UpdateInfo? by mutableStateOf(null)

    private fun checkUpdate() {
        viewModelScope.launch {
            update = withContext(Dispatchers.IO) { runCatching { Updater.check() }.getOrNull() }
        }
    }

    init {
        startAutosave()
        checkUpdate()
    }

    fun delete(entry: Entry) {
        viewModelScope.launch { dao.delete(entry) }
    }

    fun setPin(pin: String) {
        lock.setPin(pin)
        hasPin = true
    }

    fun setBiometric(on: Boolean) {
        lock.biometric = on
        biometric = on
    }

    fun removeLock() {
        lock.clear()
        hasPin = false
        biometric = false
    }

    fun export(uri: Uri) = io("Копия сохранена") {
        val json = Backup.toJson(dao.all())
        getApplication<Application>().contentResolver.openOutputStream(uri, "wt")!!
            .use { it.write(json.toByteArray()) }
    }

    fun import(uri: Uri) {
        io(null) {
            val text = getApplication<Application>().contentResolver.openInputStream(uri)!!
                .use { it.readBytes().decodeToString() }
            val known = dao.all().map { it.createdAt to it.body }.toSet()
            val fresh = Backup.fromJson(text).filter { (it.createdAt to it.body) !in known }
            dao.insertAll(fresh)
            message = if (fresh.isEmpty()) "Новых записей нет" else "Добавлено записей: ${fresh.size}"
        }
    }

    private companion object {
        const val KEY_AUTOSAVE = "autosave_minutes"
        const val KEEP_BACKUPS = 5
    }

    private fun io(done: String?, block: suspend () -> Unit) {
        viewModelScope.launch {
            message = try {
                withContext(Dispatchers.IO) { block() }
                done ?: message
            } catch (e: Exception) {
                "Не получилось: ${e.message ?: "ошибка файла"}"
            }
        }
    }
}
