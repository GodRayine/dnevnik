package com.nub.diary.data

import org.json.JSONArray
import org.json.JSONObject

/** Резервная копия — обычный JSON-файл, который можно открыть в любом редакторе. */
object Backup {
    private const val FORMAT = 1

    fun toJson(entries: List<Entry>): String {
        val array = JSONArray()
        entries.forEach {
            array.put(
                JSONObject()
                    .put("title", it.title)
                    .put("body", it.body)
                    .put("diary", it.isDiary)
                    .put("createdAt", it.createdAt)
                    .put("updatedAt", it.updatedAt)
            )
        }
        return JSONObject().put("format", FORMAT).put("entries", array).toString(2)
    }

    fun fromJson(text: String): List<Entry> {
        val array = JSONObject(text).getJSONArray("entries")
        return List(array.length()) {
            val o = array.getJSONObject(it)
            Entry(
                title = o.optString("title"),
                body = o.optString("body"),
                isDiary = o.optBoolean("diary"),
                createdAt = o.getLong("createdAt"),
                updatedAt = o.optLong("updatedAt", o.getLong("createdAt")),
            )
        }
    }
}
