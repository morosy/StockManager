package com.morosy.stockmanager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        BoardEntity::class,
        StockItemEntity::class,
        SettingsEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun settingsDao(): SettingsDao
    abstract fun boardDao(): BoardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE boards ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val now = System.currentTimeMillis()
                db.execSQL("ALTER TABLE boards ADD COLUMN created_at INTEGER NOT NULL DEFAULT $now")
                db.execSQL("ALTER TABLE boards ADD COLUMN export_id TEXT")
                db.execSQL("ALTER TABLE stock_items ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")
                db.execSQL("ALTER TABLE stock_items ADD COLUMN export_id TEXT")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 新しい status カラムを追加（0: 白, 1: 黄色, 2: 赤）
                // in_stock が true なら status = 0, false なら status = 2
                db.execSQL("""
                    ALTER TABLE stock_items ADD COLUMN status INTEGER NOT NULL DEFAULT 0
                """.trimIndent())
                // 既存のデータを移行: in_stock の値を status に変換
                db.execSQL("""
                    UPDATE stock_items 
                    SET status = CASE 
                        WHEN in_stock = 1 THEN 0
                        ELSE 2
                    END
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockmanager.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}

