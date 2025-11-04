package com.bdajaya.adminku.db.migration;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Central place for custom SQL migrations.
 *
 * MIGRATION_5_6: add FTS virtual table ProductFts and an index for name+status.
 *
 * Important:
 * - Increase AppDatabase DATABASE_VERSION accordingly.
 * - Register this migration in Room.databaseBuilder(...).addMigrations(...)
 */
public final class Migrations {

    public static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 1) Create FTS virtual table that indexes name + description.
            // Using content='Product' lets FTS reference the existing Product table rowids (docid).
            database.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS ProductFts USING fts4(name, description, content='Product');");

            // 2) Populate FTS from existing Product rows (if content option doesn't auto-populate)
            // Insert rowid/doc mapping into FTS so MATCH works immediately.
            database.execSQL("INSERT INTO ProductFts(rowid, name, description) SELECT rowid, name, description FROM Product;");

            // 3) Add composite index for common WHERE patterns (example: name + status)
            database.execSQL("CREATE INDEX IF NOT EXISTS idx_product_name_status ON Product(name, status);");

            // Note: if Product already has index for name, adding composite index is safe (AND may help certain queries).
            // If you need Porter stemming tokenization or other options, adjust FTS options accordingly.
        }
    };

    private Migrations() {}
}