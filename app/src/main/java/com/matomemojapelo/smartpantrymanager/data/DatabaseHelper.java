package com.matomemojapelo.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.matomemojapelo.smartpantrymanager.model.PantryItem;
import com.matomemojapelo.smartpantrymanager.model.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite database helper for Smart Pantry Manager.
 *
 * Responsibilities:
 *  - Create the three tables on first run
 *  - Seed 15 recipes on first run
 *  - Provide full CRUD for pantry items
 *  - Provide read access to recipes + their ingredients
 *
 * Design notes:
 *  - Quantity is REAL (double) because users enter fractions like 0.5 kg.
 *  - Ingredient names are stored lowercase+trimmed so matching is fast.
 *  - recipe_ingredients uses ON DELETE CASCADE so deleting a recipe
 *    automatically removes its ingredient rows.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DB_NAME = "smart_pantry.db";
    private static final int    DB_VERSION = 1;

    // ----- pantry_items table -----
    public static final String T_PANTRY   = "pantry_items";
    public static final String C_ID       = "id";
    public static final String C_NAME     = "name";
    public static final String C_QTY      = "quantity";
    public static final String C_UNIT     = "unit";
    public static final String C_EXPIRY   = "expiry_date";

    // ----- recipes table -----
    public static final String T_RECIPES  = "recipes";
    public static final String C_R_NAME   = "name";
    public static final String C_R_STEPS  = "steps";

    // ----- recipe_ingredients table -----
    public static final String T_RECIPE_ING  = "recipe_ingredients";
    public static final String C_RI_RECIPE_ID = "recipe_id";
    public static final String C_RI_NAME      = "ingredient_name";
    public static final String C_RI_QTY       = "quantity";
    public static final String C_RI_UNIT      = "unit";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    //onCreate
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: creating tables");

        db.execSQL("CREATE TABLE " + T_PANTRY + " (" +
                C_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                C_NAME   + " TEXT NOT NULL, " +
                C_QTY    + " REAL NOT NULL, " +
                C_UNIT   + " TEXT, " +
                C_EXPIRY + " TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " (" +
                C_ID      + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                C_R_NAME  + " TEXT NOT NULL, " +
                C_R_STEPS + " TEXT)");

        db.execSQL("CREATE TABLE " + T_RECIPE_ING + " (" +
                C_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                C_RI_RECIPE_ID + " INTEGER NOT NULL, " +
                C_RI_NAME     + " TEXT NOT NULL, " +
                C_RI_QTY      + " REAL NOT NULL, " +
                C_RI_UNIT     + " TEXT, " +
                "FOREIGN KEY(" + C_RI_RECIPE_ID + ") REFERENCES " +
                T_RECIPES + "(" + C_ID + ") ON DELETE CASCADE)");

        // Seed the 15 recipes on first run
        seedRecipes(db);
    }

    //onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: " + oldVersion + " -> " + newVersion +
                " (dropping and recreating)");
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    //addPantryItem
    // ============================================================
    //                      PANTRY CRUD — CREATE
    // ============================================================

    /**
     * Inserts a new pantry item. Returns the new row id, or -1 on failure.
     * Stores the name lowercase+trimmed for consistent matching later.
     */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_NAME,   item.getName().toLowerCase().trim());
        cv.put(C_QTY,    item.getQuantity());
        cv.put(C_UNIT,   item.getUnit());
        cv.put(C_EXPIRY, item.getExpiryDate());

        long newId = db.insert(T_PANTRY, null, cv);
        Log.d(TAG, "addPantryItem: inserted id=" + newId);
        db.close();
        return newId;
    }

    //getAllPantryItems
    // ============================================================
    //                       PANTRY CRUD — READ
    // ============================================================

    /**
     * Returns ALL pantry items sorted alphabetically by name.
     * Never returns null — returns an empty list if no rows exist.
     */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(
                T_PANTRY,       // table
                null,           // all columns
                null, null,     // no WHERE
                null,           // no GROUP BY
                null,           // no HAVING
                C_NAME + " ASC" // order by name
        );

        if (c != null && c.moveToFirst()) {
            do {
                PantryItem item = new PantryItem(
                        c.getInt(c.getColumnIndexOrThrow(C_ID)),
                        c.getString(c.getColumnIndexOrThrow(C_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(C_QTY)),
                        c.getString(c.getColumnIndexOrThrow(C_UNIT)),
                        c.getString(c.getColumnIndexOrThrow(C_EXPIRY))
                );
                items.add(item);
            } while (c.moveToNext());
        }

        if (c != null) c.close();
        db.close();
        Log.d(TAG, "getAllPantryItems: " + items.size() + " rows");
        return items;
    }

    //UpdatePantryItem
    // ============================================================
    //                     PANTRY CRUD — UPDATE
    // ============================================================

    /**
     * Updates an existing pantry item by id.
     * Returns number of rows affected (1 on success, 0 if id not found).
     */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(C_NAME,   item.getName().toLowerCase().trim());
        cv.put(C_QTY,    item.getQuantity());
        cv.put(C_UNIT,   item.getUnit());
        cv.put(C_EXPIRY, item.getExpiryDate());

        int rows = db.update(
                T_PANTRY,
                cv,
                C_ID + " = ?",
                new String[]{ String.valueOf(item.getId()) }
        );
        Log.d(TAG, "updatePantryItem: id=" + item.getId() +
                " rowsAffected=" + rows);
        db.close();
        return rows;
    }
    //deletePantryItem
    // ============================================================
    //                     PANTRY CRUD — DELETE
    // ============================================================

    /**
     * Deletes a pantry item by id.
     * Returns number of rows deleted (1 on success, 0 if id not found).
     */
    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(
                T_PANTRY,
                C_ID + " = ?",
                new String[]{ String.valueOf(id) }
        );
        Log.d(TAG, "deletePantryItem: id=" + id + " rowsDeleted=" + rows);
        db.close();
        return rows;
    }

    //Recipe
    // ============================================================
    //                       RECIPE READERS
    // ============================================================

    /**
     * Returns all recipes, each with its ingredients pre-loaded.
     * Used by Suggested Recipes and Recipe Detail screens.
     */
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.query(T_RECIPES, null, null, null, null, null,
                C_R_NAME + " ASC");

        if (c != null && c.moveToFirst()) {
            do {
                int id      = c.getInt(c.getColumnIndexOrThrow(C_ID));
                String name = c.getString(c.getColumnIndexOrThrow(C_R_NAME));
                String steps = c.getString(c.getColumnIndexOrThrow(C_R_STEPS));

                List<Recipe.Ingredient> ings = getIngredientsForRecipe(db, id);
                recipes.add(new Recipe(id, name, steps, ings));
            } while (c.moveToNext());
        }

        if (c != null) c.close();
        db.close();
        Log.d(TAG, "getAllRecipes: " + recipes.size() + " recipes");
        return recipes;
    }

    /**
     * Loads all ingredient requirements for one recipe.
     * Helper — not public because callers should use getAllRecipes()
     * or getRecipeById() which return complete objects.
     */
    private List<Recipe.Ingredient> getIngredientsForRecipe(SQLiteDatabase db,
                                                             int recipeId) {
        List<Recipe.Ingredient> ings = new ArrayList<>();
        Cursor c = db.query(
                T_RECIPE_ING,
                null,
                C_RI_RECIPE_ID + " = ?",
                new String[]{ String.valueOf(recipeId) },
                null, null, null
        );

        if (c != null && c.moveToFirst()) {
            do {
                ings.add(new Recipe.Ingredient(
                        c.getString(c.getColumnIndexOrThrow(C_RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(C_RI_QTY)),
                        c.getString(c.getColumnIndexOrThrow(C_RI_UNIT))
                ));
            } while (c.moveToNext());
        }
        if (c != null) c.close();
        return ings;
    }
    //SeedRecipes
    // ============================================================
    //                       SEED DATA (15 recipes)
    // ============================================================

    /**
     * Inserts the 15 seed recipes on first run.
     * Only called from onCreate().
     */
    private void seedRecipes(SQLiteDatabase db) {
        Log.d(TAG, "seedRecipes: inserting 15 recipes");

        // 1. Tomato Pasta
        long r1 = insertRecipe(db, "Tomato Pasta",
                "1. Boil pasta in salted water.\n" +
                "2. Fry chopped tomatoes in oil.\n" +
                "3. Mix pasta with sauce and serve.");
        addIngredient(db, r1, "pasta", 200, "g");
        addIngredient(db, r1, "tomato", 3, "piece");
        addIngredient(db, r1, "oil", 2, "tbsp");
        addIngredient(db, r1, "salt", 1, "tsp");

        // 2. Cheese Omelette
        long r2 = insertRecipe(db, "Cheese Omelette",
                "1. Beat eggs with salt.\n" +
                "2. Melt cheese in pan.\n" +
                "3. Pour eggs and cook until set.");
        addIngredient(db, r2, "egg", 2, "piece");
        addIngredient(db, r2, "cheese", 50, "g");
        addIngredient(db, r2, "salt", 1, "tsp");
        addIngredient(db, r2, "oil", 1, "tbsp");

        // 3. Garlic Rice
        long r3 = insertRecipe(db, "Garlic Rice",
                "1. Cook rice.\n" +
                "2. Fry sliced garlic in oil until golden.\n" +
                "3. Toss rice with garlic and salt.");
        addIngredient(db, r3, "rice", 200, "g");
        addIngredient(db, r3, "garlic", 3, "piece");
        addIngredient(db, r3, "oil", 2, "tbsp");
        addIngredient(db, r3, "salt", 1, "tsp");

        // 4. Onion Soup
        long r4 = insertRecipe(db, "Onion Soup",
                "1. Slice onions and soften in pot.\n" +
                "2. Add water and simmer 20 mins.\n" +
                "3. Season with salt.");
        addIngredient(db, r4, "onion", 2, "piece");
        addIngredient(db, r4, "water", 500, "ml");
        addIngredient(db, r4, "salt", 1, "tsp");

        // 5. Egg Fried Rice
        long r5 = insertRecipe(db, "Egg Fried Rice",
                "1. Cook rice and cool.\n" +
                "2. Scramble eggs in oil.\n" +
                "3. Add rice, season, stir-fry.");
        addIngredient(db, r5, "rice", 200, "g");
        addIngredient(db, r5, "egg", 2, "piece");
        addIngredient(db, r5, "oil", 2, "tbsp");
        addIngredient(db, r5, "salt", 1, "tsp");

        // 6. Cheese Sandwich
        long r6 = insertRecipe(db, "Cheese Sandwich",
                "1. Butter the bread.\n" +
                "2. Add cheese slices.\n" +
                "3. Toast until golden.");
        addIngredient(db, r6, "bread", 2, "slice");
        addIngredient(db, r6, "cheese", 50, "g");
        addIngredient(db, r6, "butter", 1, "tbsp");

        // 7. Potato Curry
        long r7 = insertRecipe(db, "Potato Curry",
                "1. Boil and cube potatoes.\n" +
                "2. Fry onions in oil.\n" +
                "3. Add potatoes and salt, simmer.");
        addIngredient(db, r7, "potato", 3, "piece");
        addIngredient(db, r7, "onion", 1, "piece");
        addIngredient(db, r7, "oil", 2, "tbsp");
        addIngredient(db, r7, "salt", 1, "tsp");

        // 8. Tomato Soup
        long r8 = insertRecipe(db, "Tomato Soup",
                "1. Boil tomatoes in water.\n" +
                "2. Blend until smooth.\n" +
                "3. Season with salt.");
        addIngredient(db, r8, "tomato", 4, "piece");
        addIngredient(db, r8, "water", 300, "ml");
        addIngredient(db, r8, "salt", 1, "tsp");

        // 9. Pancakes
        long r9 = insertRecipe(db, "Pancakes",
                "1. Mix flour, milk, egg and sugar.\n" +
                "2. Whisk to smooth batter.\n" +
                "3. Fry until golden.");
        addIngredient(db, r9, "flour", 200, "g");
        addIngredient(db, r9, "milk", 200, "ml");
        addIngredient(db, r9, "egg", 1, "piece");
        addIngredient(db, r9, "sugar", 2, "tbsp");

        // 10. Garlic Bread
        long r10 = insertRecipe(db, "Garlic Bread",
                "1. Mix softened butter with minced garlic.\n" +
                "2. Spread on bread slices.\n" +
                "3. Bake until crisp.");
        addIngredient(db, r10, "bread", 4, "slice");
        addIngredient(db, r10, "butter", 2, "tbsp");
        addIngredient(db, r10, "garlic", 2, "piece");

        // 11. Veg Salad
        long r11 = insertRecipe(db, "Veg Salad",
                "1. Chop tomatoes and onions.\n" +
                "2. Toss together.\n" +
                "3. Sprinkle salt.");
        addIngredient(db, r11, "tomato", 2, "piece");
        addIngredient(db, r11, "onion", 1, "piece");
        addIngredient(db, r11, "salt", 1, "tsp");

        // 12. Rice Pudding
        long r12 = insertRecipe(db, "Rice Pudding",
                "1. Cook rice in milk.\n" +
                "2. Add sugar and stir.\n" +
                "3. Simmer until creamy.");
        addIngredient(db, r12, "rice", 100, "g");
        addIngredient(db, r12, "milk", 300, "ml");
        addIngredient(db, r12, "sugar", 3, "tbsp");

        // 13. Egg Salad
        long r13 = insertRecipe(db, "Egg Salad",
                "1. Boil eggs and chop.\n" +
                "2. Chop onion.\n" +
                "3. Mix with salt.");
        addIngredient(db, r13, "egg", 2, "piece");
        addIngredient(db, r13, "salt", 1, "tsp");
        addIngredient(db, r13, "onion", 1, "piece");

        // 14. Cheesy Pasta
        long r14 = insertRecipe(db, "Cheesy Pasta",
                "1. Boil pasta.\n" +
                "2. Heat milk and melt cheese.\n" +
                "3. Toss pasta in sauce.");
        addIngredient(db, r14, "pasta", 200, "g");
        addIngredient(db, r14, "cheese", 80, "g");
        addIngredient(db, r14, "milk", 100, "ml");

        // 15. French Toast
        long r15 = insertRecipe(db, "French Toast",
                "1. Beat egg with milk.\n" +
                "2. Dip bread slices.\n" +
                "3. Fry in butter until golden.");
        addIngredient(db, r15, "bread", 2, "slice");
        addIngredient(db, r15, "egg", 1, "piece");
        addIngredient(db, r15, "milk", 50, "ml");
        addIngredient(db, r15, "butter", 1, "tbsp");

        Log.d(TAG, "seedRecipes: done");
    }

    private long insertRecipe(SQLiteDatabase db, String name, String steps) {
        ContentValues cv = new ContentValues();
        cv.put(C_R_NAME, name);
        cv.put(C_R_STEPS, steps);
        return db.insert(T_RECIPES, null, cv);
    }

    private void addIngredient(SQLiteDatabase db, long recipeId,
                               String name, double qty, String unit) {
        ContentValues cv = new ContentValues();
        cv.put(C_RI_RECIPE_ID, recipeId);
        cv.put(C_RI_NAME, name.toLowerCase());
        cv.put(C_RI_QTY, qty);
        cv.put(C_RI_UNIT, unit);
        db.insert(T_RECIPE_ING, null, cv);
    }
}
