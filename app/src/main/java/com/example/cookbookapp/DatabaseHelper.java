package com.example.cookbookapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cookbook.db";
    private static final int DB_VERSION = 2;

    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_MY_RECIPES = "my_recipes";

    private static final String COL_USER_ID = "user_id";
    private static final String COL_ID = "id";
    private static final String COL_TITLE = "title";
    private static final String COL_CATEGORY = "category";
    private static final String COL_IMAGE = "image_url";
    private static final String COL_INSTRUCTIONS = "instructions";
    private static final String COL_INGREDIENTS = "ingredients";
    private static final String COL_AREA = "area";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    private String getUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FAVORITES + " (" +
                COL_USER_ID + " TEXT, " +
                COL_ID + " TEXT, " +
                COL_TITLE + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_IMAGE + " TEXT, " +
                COL_INSTRUCTIONS + " TEXT, " +
                COL_INGREDIENTS + " TEXT, " +
                COL_AREA + " TEXT, " +
                "PRIMARY KEY(" + COL_USER_ID + ", " + COL_ID + "))");

        db.execSQL("CREATE TABLE " + TABLE_MY_RECIPES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_ID + " TEXT, " +
                COL_TITLE + " TEXT, " +
                COL_CATEGORY + " TEXT, " +
                COL_IMAGE + " TEXT, " +
                COL_INSTRUCTIONS + " TEXT, " +
                COL_INGREDIENTS + " TEXT, " +
                COL_AREA + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_RECIPES);
        onCreate(db);
    }

    // ============ FAVORIS ============

    public void addFavorite(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, getUserId());
        values.put(COL_ID, recipe.getId());
        values.put(COL_TITLE, recipe.getTitle());
        values.put(COL_CATEGORY, recipe.getCategory());
        values.put(COL_IMAGE, recipe.getImageUrl());
        values.put(COL_INSTRUCTIONS, recipe.getInstructions());
        values.put(COL_INGREDIENTS, recipe.getIngredients());
        values.put(COL_AREA, recipe.getArea());
        db.insertOrThrow(TABLE_FAVORITES, null, values);
        db.close();
    }

    public void removeFavorite(String recipeId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FAVORITES,
                COL_USER_ID + "=? AND " + COL_ID + "=?",
                new String[]{getUserId(), recipeId});
        db.close();
    }

    public boolean isFavorite(String recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null,
                COL_USER_ID + "=? AND " + COL_ID + "=?",
                new String[]{getUserId(), recipeId},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public List<Recipe> getFavorites() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null,
                COL_USER_ID + "=?", new String[]{getUserId()},
                null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursorToFavorite(cursor));
        }
        cursor.close();
        db.close();
        return list;
    }

    // ============ MES RECETTES ============

    public void addMyRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, getUserId());
        values.put(COL_TITLE, recipe.getTitle());
        values.put(COL_CATEGORY, recipe.getCategory());
        values.put(COL_IMAGE, recipe.getImageUrl());
        values.put(COL_INSTRUCTIONS, recipe.getInstructions());
        values.put(COL_INGREDIENTS, recipe.getIngredients());
        values.put(COL_AREA, "");
        db.insertOrThrow(TABLE_MY_RECIPES, null, values);
        db.close();
    }

    public List<Recipe> getMyRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MY_RECIPES, null,
                COL_USER_ID + "=?", new String[]{getUserId()},
                null, null, null);
        while (cursor.moveToNext()) {
            Recipe r = new Recipe(
                    String.valueOf(cursor.getInt(0)),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7)
            );
            list.add(r);
        }
        cursor.close();
        db.close();
        return list;
    }

    public void removeMyRecipe(String recipeId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_MY_RECIPES,
                COL_USER_ID + "=? AND " + COL_ID + "=?",
                new String[]{getUserId(), recipeId});
        db.close();
    }

    private Recipe cursorToFavorite(Cursor cursor) {
        return new Recipe(
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getString(6),
                cursor.getString(7)
        );
    }
}