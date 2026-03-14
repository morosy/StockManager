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
    version = 7,
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
                recreateStockItemsTable(
                    db = db,
                    statusExpression = buildStatusExpression(hasStatus = false, hasLegacyInStock = true)
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val hasStatus = db.hasColumn(tableName = "stock_items", columnName = "status")
                val hasLegacyInStock = db.hasColumn(tableName = "stock_items", columnName = "in_stock")

                if (hasLegacyInStock || !hasStatus) {
                    recreateStockItemsTable(
                        db = db,
                        statusExpression = buildStatusExpression(
                            hasStatus = hasStatus,
                            hasLegacyInStock = hasLegacyInStock
                        )
                    )
                    return
                }

                db.execSQL(
                    """
                    UPDATE stock_items
                    SET status = CASE
                        WHEN status IN (0, 1, 2) THEN status
                        ELSE 0
                    END
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE settings ADD COLUMN tutorial_seen INTEGER NOT NULL DEFAULT 0")
            }
        }

        private fun recreateStockItemsTable(
            db: SupportSQLiteDatabase,
            statusExpression: String
        ) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS stock_items_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    board_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    status INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    export_id TEXT,
                    FOREIGN KEY(board_id) REFERENCES boards(id) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO stock_items_new (id, board_id, name, status, created_at, updated_at, export_id)
                SELECT
                    id,
                    board_id,
                    name,
                    $statusExpression,
                    created_at,
                    updated_at,
                    export_id
                FROM stock_items
                """.trimIndent()
            )
            db.execSQL("DROP TABLE stock_items")
            db.execSQL("ALTER TABLE stock_items_new RENAME TO stock_items")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_items_board_id ON stock_items(board_id)")
        }

        private fun buildStatusExpression(
            hasStatus: Boolean,
            hasLegacyInStock: Boolean
        ): String {
            return when {
                hasStatus && hasLegacyInStock -> {
                    """
                    CASE
                        WHEN status IN (0, 1, 2) THEN status
                        WHEN in_stock = 1 THEN 0
                        ELSE 2
                    END
                    """.trimIndent()
                }

                hasStatus -> {
                    """
                    CASE
                        WHEN status IN (0, 1, 2) THEN status
                        ELSE 0
                    END
                    """.trimIndent()
                }

                hasLegacyInStock -> {
                    """
                    CASE
                        WHEN in_stock = 1 THEN 0
                        ELSE 2
                    END
                    """.trimIndent()
                }

                else -> "0"
            }
        }

        private fun SupportSQLiteDatabase.hasColumn(
            tableName: String,
            columnName: String
        ): Boolean {
            query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == columnName) {
                        return true
                    }
                }
            }
            return false
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockmanager.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
