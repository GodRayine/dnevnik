package com.nub.diary.data

import com.nub.diary.BuildConfig
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val version: String, val pageUrl: String)

/**
 * Единственное обращение приложения к сети: чтение списка релизов репозитория
 * на GitHub. Ничего из приложения не отправляется, кроме самого запроса.
 */
object Updater {
    fun check(): UpdateInfo? {
        val repo = BuildConfig.UPDATE_REPO
        if (!Regex("[\\w.-]+/[\\w.-]+").matches(repo)) return null

        val conn = URL("https://api.github.com/repos/$repo/releases?per_page=1")
            .openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 8_000
            conn.readTimeout = 8_000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            if (conn.responseCode != 200) return null
            val release = JSONArray(conn.inputStream.use { it.readBytes().decodeToString() })
                .optJSONObject(0) ?: return null
            val tag = release.getString("tag_name").removePrefix("v")
            val url = release.getString("html_url")
            if (!url.startsWith("https://github.com/")) return null
            return if (isNewer(tag, BuildConfig.VERSION_NAME)) UpdateInfo(tag, url) else null
        } finally {
            conn.disconnect()
        }
    }

    /** Сравнение по числам через точку: 2026.4.5 новее 2026.4.4.1. */
    fun isNewer(candidate: String, current: String): Boolean {
        val a = candidate.split('.').map { it.toIntOrNull() ?: 0 }
        val b = current.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
