package dz.btpprodz.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Explicit user-controlled SQLite backup/restore.
 * Restore accepts only a structurally valid database from this app's current schema.
 */
object DatabaseBackupManager {
    private const val DATABASE_NAME = "btp_pro_dz.db"
    private const val DATABASE_VERSION = 5
    private val REQUIRED_TABLES = setOf(
        "app_settings", "custom_units", "materials", "material_price_history",
        "calculation_costs", "labor_rates", "labor_costs", "projects",
        "devis_documents", "devis_lines", "room_master_table"
    )

    fun backup(context: Context, database: BtpDatabase, destination: OutputStream) {
        val checkpoint = database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")
        val busy = try {
            if (checkpoint.moveToFirst()) checkpoint.getInt(0) else 1
        } finally {
            checkpoint.close()
        }
        check(busy == 0) { "Database is busy; backup was not created. Try again." }

        BtpDatabase.closeInstance()
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        check(databaseFile.isFile) { "Database file does not exist." }
        databaseFile.inputStream().use { input -> input.copyTo(destination) }
        destination.flush()
    }

    fun restore(context: Context, source: InputStream) {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        val parent = databaseFile.parentFile ?: error("Database directory is unavailable.")
        check(parent.exists() || parent.mkdirs()) { "Cannot create database directory." }
        val candidate = File(parent, "$DATABASE_NAME.restore-check")
        val previous = File(parent, "$DATABASE_NAME.pre-restore")
        try {
            candidate.outputStream().use { output -> source.copyTo(output) }
            check(candidate.length() > 0L) { "Backup file is empty." }
            validateBackup(candidate)

            BtpDatabase.closeInstance()
            if (previous.exists() && !previous.delete()) {
                error("Cannot clear previous restore staging file.")
            }
            val hadCurrentDatabase = databaseFile.exists()
            if (hadCurrentDatabase && !databaseFile.renameTo(previous)) {
                error("Cannot safely stage the current database.")
            }
            if (!candidate.renameTo(databaseFile)) {
                if (hadCurrentDatabase) previous.renameTo(databaseFile)
                error("Could not install restored database; the previous database was preserved.")
            }

            previous.delete()
            File(databaseFile.absolutePath + "-wal").delete()
            File(databaseFile.absolutePath + "-shm").delete()
        } finally {
            candidate.delete()
            // A failed install leaves the staged original available for recovery.
        }
    }

    private fun validateBackup(file: File) {
        val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        try {
            val integrityCursor = db.rawQuery("PRAGMA quick_check", null)
            val integrity = try {
                if (integrityCursor.moveToFirst()) integrityCursor.getString(0) else "No integrity result"
            } finally {
                integrityCursor.close()
            }
            check(integrity.equals("ok", ignoreCase = true)) {
                "Backup integrity check failed: $integrity"
            }

            val versionCursor = db.rawQuery("PRAGMA user_version", null)
            val version = try {
                if (versionCursor.moveToFirst()) versionCursor.getInt(0) else -1
            } finally {
                versionCursor.close()
            }
            check(version == DATABASE_VERSION) {
                "Unsupported backup schema version ($version). Expected $DATABASE_VERSION."
            }

            val tableCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null
            )
            val tables = try {
                buildSet {
                    while (tableCursor.moveToNext()) add(tableCursor.getString(0))
                }
            } finally {
                tableCursor.close()
            }
            val missing = REQUIRED_TABLES - tables
            check(missing.isEmpty()) {
                "Backup is missing required app tables: ${missing.joinToString()}."
            }
        } finally {
            db.close()
        }
    }
}
