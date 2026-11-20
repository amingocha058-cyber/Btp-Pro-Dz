package dz.btpprodz.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Explicit user-controlled SQLite backup/restore.
 * Restore must only be invoked after the user selects a trusted backup file.
 */
object DatabaseBackupManager {
    private const val DATABASE_NAME = "btp_pro_dz.db"

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
        try {
            candidate.outputStream().use { output -> source.copyTo(output) }
            check(candidate.length() > 0L) { "Backup file is empty." }
            val validationDb = SQLiteDatabase.openDatabase(
                candidate.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            try {
                val cursor = validationDb.rawQuery("PRAGMA quick_check", null)
                val integrity = try {
                    if (cursor.moveToFirst()) cursor.getString(0) else "No integrity result"
                } finally {
                    cursor.close()
                }
                check(integrity.equals("ok", ignoreCase = true)) {
                    "Backup integrity check failed: $integrity"
                }
            } finally {
                validationDb.close()
            }

            BtpDatabase.closeInstance()
            File(databaseFile.absolutePath + "-wal").delete()
            File(databaseFile.absolutePath + "-shm").delete()
            if (databaseFile.exists() && !databaseFile.delete()) {
                error("Cannot replace current database file.")
            }
            check(candidate.renameTo(databaseFile)) { "Could not install restored database." }
        } finally {
            candidate.delete()
        }
    }
}
