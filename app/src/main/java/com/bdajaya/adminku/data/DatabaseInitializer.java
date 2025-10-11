package com.bdajaya.adminku.data;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.entity.Config;
import com.bdajaya.adminku.data.entity.Unit;

import java.util.UUID;

public class DatabaseInitializer {

    public static void populateDatabase(AppDatabase db) {
        // Initialize base units
        initializeBaseUnits(db);

        // Initialize config values
        initializeConfig(db);

        // Initialize sample categories
        initializeSampleCategories(db);
    }

    private static void initializeBaseUnits(AppDatabase db) {
        long now = System.currentTimeMillis();

        // Base unit for pieces
    }

    private static void initializeConfig(AppDatabase db) {
        Config maxDepthConfig = new Config("max_category_depth", "5");
        db.configDao().insert(maxDepthConfig);
    }

    private static void initializeSampleCategories(AppDatabase db) {
        long now = System.currentTimeMillis();

        // Root categories
        String fashionId = UUID.randomUUID().toString();
        Category fashion = new Category(
                fashionId,
                null,
                0,
                "Fashion",
                null,
                now,
                now
        );

        String electronicsId = UUID.randomUUID().toString();
        Category electronics = new Category(
                electronicsId,
                null,
                0,
                "Electronics",
                null,
                now,
                now
        );

        String groceryId = UUID.randomUUID().toString();
        Category grocery = new Category(
                groceryId,
                null,
                0,
                "Grocery",
                null,
                now,
                now
        );

        // Level 1 categories under Fashion
        String womenId = UUID.randomUUID().toString();
        Category women = new Category(
                womenId,
                fashionId,
                1,
                "Wanita",
                null,
                now,
                now
        );

        String menId = UUID.randomUUID().toString();
        Category men = new Category(
                menId,
                fashionId,
                1,
                "Pria",
                null,
                now,
                now
        );

        String kidsId = UUID.randomUUID().toString();
        Category kids = new Category(
                kidsId,
                fashionId,
                1,
                "Anak-anak",
                null,
                now,
                now
        );

        // Level 2 categories under Women
        String womenTopsId = UUID.randomUUID().toString();
        Category womenTops = new Category(
                womenTopsId,
                womenId,
                2,
                "Atasan",
                null,
                now,
                now
        );

        String womenBottomsId = UUID.randomUUID().toString();
        Category womenBottoms = new Category(
                womenBottomsId,
                womenId,
                2,
                "Bawahan",
                null,
                now,
                now
        );

        // Level 3 categories under Women Tops
        String womenTShirtsId = UUID.randomUUID().toString();
        Category womenTShirts = new Category(
                womenTShirtsId,
                womenTopsId,
                3,
                "Kaos",
                null,
                now,
                now
        );

        String womenBlousesId = UUID.randomUUID().toString();
        Category womenBlouses = new Category(
                womenBlousesId,
                womenTopsId,
                3,
                "Blouse",
                null,
                now,
                now
        );

        // Insert all categories
        db.categoryDao().insert(fashion);
        db.categoryDao().insert(electronics);
        db.categoryDao().insert(grocery);
        db.categoryDao().insert(women);
        db.categoryDao().insert(men);
        db.categoryDao().insert(kids);
        db.categoryDao().insert(womenTops);
        db.categoryDao().insert(womenBottoms);
        db.categoryDao().insert(womenTShirts);
        db.categoryDao().insert(womenBlouses);
    }
}
