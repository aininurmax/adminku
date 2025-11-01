package com.bdajaya.adminku.data;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.entity.Unit;

import java.util.UUID;

public class DatabaseInitializer {

    public static void populateDatabase(AppDatabase db) {
        // Initialize base units
        initializeBaseUnits(db);

        // Initialize sample categories
        initializeSampleCategories(db);
    }

    private static void initializeBaseUnits(AppDatabase db) {
        long now = System.currentTimeMillis();

        // Base units
        Unit pcs = new Unit(
                UUID.randomUUID().toString(),
                "pcs",
                "pcs",
                1,
                true,
                now,
                now
        );

        Unit gram = new Unit(
                UUID.randomUUID().toString(),
                "gram",
                "gram",
                1,
                true,
                now,
                now
        );

        // Derived units
        Unit dozen = new Unit(
                UUID.randomUUID().toString(),
                "dozen",
                "pcs",
                12,
                false,
                now,
                now
        );

        Unit kg = new Unit(
                UUID.randomUUID().toString(),
                "kg",
                "gram",
                1000,
                false,
                now,
                now
        );

        db.unitDao().insert(pcs);
        db.unitDao().insert(gram);
        db.unitDao().insert(dozen);
        db.unitDao().insert(kg);
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
                true, // Fashion has children (Wanita, Pria, Anak-anak)
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
                false, // Assume no children initially
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
                false, // Assume no children initially
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
                true, // Women has children (Atasan, Bawahan)
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
                false, // Assume no children initially
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
                false, // Assume no children initially
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
                true, // Tops has children (Kaos, Blouse)
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
                false, // Assume no children initially
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
                false, // Leaf category
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
                false, // Leaf category
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
