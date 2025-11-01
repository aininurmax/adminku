package com.bdajaya.adminku.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.bdajaya.adminku.data.dao.*;
import com.bdajaya.adminku.data.entity.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Product.class,
        ProductImage.class,
        Category.class,
        Unit.class,
        StockTransaction.class,
        Brand.class
}, version = 5, exportSchema = true)
@TypeConverters({DateConverter.class, StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase  {
    private static final String DATABASE_NAME = "adminku_db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract ProductDao productDao();
    public abstract ProductImageDao productImageDao();
    public abstract CategoryDao categoryDao();
    public abstract UnitDao unitDao();
    public abstract StockTransactionDao stockTransactionDao();
    public abstract BrandDao brandDao();

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Rename kolom lama dan tambahkan metadata baru
            database.execSQL("ALTER TABLE ProductImage RENAME COLUMN imageBase64 TO imagePath");
            database.execSQL("ALTER TABLE ProductImage ADD COLUMN fileSize INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE ProductImage ADD COLUMN width INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE ProductImage ADD COLUMN height INTEGER NOT NULL DEFAULT 0");

            // Note: Data lama akan invalid, perlu migrasi manual
        }
    };

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        // Populate the database with initial data
                                        DatabaseInitializer.populateDatabase(getInstance(context));
                                    });
                                }
                            })
                            .addMigrations(MIGRATION_4_5)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
