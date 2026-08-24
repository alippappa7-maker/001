package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.impact.ImpactDao
import com.example.data.local.impact.ImpactEntity
import com.example.data.local.knowledge.KnowledgeDao
import com.example.data.local.knowledge.KnowledgeEntity
import com.example.data.local.studio.StudioDao
import com.example.data.local.studio.VideoProjectEntity

@Database(
    entities = [
        VideoProjectEntity::class,
        KnowledgeEntity::class,
        ImpactEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studioDao(): StudioDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun impactDao(): ImpactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qabas_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
