package com.yourname.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.yourname.smartpantry.model.PantryItem;
import com.yourname.smartpantry.model.Recipe;

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
}