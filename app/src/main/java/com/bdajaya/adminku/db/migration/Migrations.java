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
            // Room will auto-create ProductFts, but we need to populate it
            database.execSQL("INSERT INTO ProductFts(rowid, name, description) SELECT rowid, name, description FROM Product;");
        }
    };

    private Migrations() {}
}