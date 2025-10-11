package com.bdajaya.adminku.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.bdajaya.adminku.data.dao.CategoryDao;
import com.bdajaya.adminku.data.dao.ConfigDao;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.dao.ProductImageDao;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.entity.Config;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.entity.ProductImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        Product.class,
        ProductImage.class,
        Category.class,
        Config.class
}, version = 2, exportSchema = true)
@TypeConverters({DateConverter.class, StringListConverter.class})
public abstract class AppDatabase extends RoomDatabase  {
    private static final String DATABASE_NAME = "adminku_db";
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create Config table
            database.execSQL("CREATE TABLE IF NOT EXISTS Config (" +
                    "`key` TEXT NOT NULL, " +
                    "`value` TEXT NOT NULL, " +
                    "PRIMARY KEY(`key`))");

            // Insert default config values
            database.execSQL("INSERT OR REPLACE INTO Config (`key`, `value`) VALUES ('max_category_depth', '5')");
        }
    };

    public abstract ProductDao productDao();
    public abstract ProductImageDao productImageDao();
    public abstract CategoryDao categoryDao();
    public abstract ConfigDao configDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        // Populate the database with initial data
                                        DatabaseInitializer.populateDatabase(getInstance(context));
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
