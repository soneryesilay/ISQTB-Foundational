package com.istqb.examsimulator.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.istqb.examsimulator.data.local.converters.StringListConverter
import com.istqb.examsimulator.data.local.entities.AttemptAnswerEntity
import com.istqb.examsimulator.data.local.entities.ExamAttemptEntity
import com.istqb.examsimulator.data.local.entities.OptionEntity
import com.istqb.examsimulator.data.local.entities.QuestionEntity
import com.istqb.examsimulator.data.local.entities.QuestionSetEntity

@Database(
    entities = [
        QuestionEntity::class,
        OptionEntity::class,
        QuestionSetEntity::class,
        ExamAttemptEntity::class,
        AttemptAnswerEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao
    abstract fun examDao(): ExamDao
    abstract fun questionSetDao(): QuestionSetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "istqb_exam_database"
                )
                    .fallbackToDestructiveMigration() // Versiyonu değiştirdiğimizde veritabanını sil ve yeniden oluştur
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

