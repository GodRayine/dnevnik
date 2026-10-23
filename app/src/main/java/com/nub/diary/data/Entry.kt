package com.nub.diary.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import android.content.Context
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val body: String,
    val isDiary: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

@Dao
interface EntryDao {
    @Query(
        "SELECT * FROM entries WHERE (:diary IS NULL OR isDiary = :diary) " +
            "AND (title LIKE '%' || :q || '%' OR body LIKE '%' || :q || '%') " +
            "ORDER BY createdAt DESC"
    )
    fun observe(q: String, diary: Boolean?): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun get(id: Long): Entry?

    @Query("SELECT * FROM entries ORDER BY createdAt")
    suspend fun all(): List<Entry>

    @Upsert
    suspend fun upsert(entry: Entry): Long

    @Delete
    suspend fun delete(entry: Entry)

    @Insert
    suspend fun insertAll(entries: List<Entry>)
}

@Database(entities = [Entry::class], version = 1, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun dao(): EntryDao

    companion object {
        @Volatile private var instance: DiaryDatabase? = null

        fun get(context: Context): DiaryDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, DiaryDatabase::class.java, "diary.db"
            ).build().also { instance = it }
        }
    }
}
