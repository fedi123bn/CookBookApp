package com.example.cookbookapp;

import android.os.Handler;
import android.os.Looper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MealApiClient {

    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface RecipeCallback {
        void onSuccess(List<Recipe> recipes);
        void onError(String error);
    }

    public interface SingleRecipeCallback {
        void onSuccess(Recipe recipe);
        void onError(String error);
    }

    // Recherche par nom
    public void searchRecipes(String query, RecipeCallback callback) {
        String url = BASE_URL + "search.php?s=" + query;
        makeRequest(url, callback);
    }

    // Filtrer par catégorie
    public void getRecipesByCategory(String category, RecipeCallback callback) {
        String url = BASE_URL + "filter.php?c=" + category;
        makeRequest(url, callback);
    }

    // Détail d'une recette par ID
    public void getRecipeById(String id, SingleRecipeCallback callback) {
        String url = BASE_URL + "lookup.php?i=" + id;
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                JsonArray meals = json.getAsJsonArray("meals");
                if (meals != null && meals.size() > 0) {
                    Recipe recipe = parseDetailedRecipe(meals.get(0).getAsJsonObject());
                    mainHandler.post(() -> callback.onSuccess(recipe));
                } else {
                    mainHandler.post(() -> callback.onError("Recette non trouvée"));
                }
            }
        });
    }

    // Recettes aléatoires (page d'accueil)
    public void getRandomRecipes(RecipeCallback callback) {
        searchRecipes("a", callback);
    }

    private void makeRequest(String url, RecipeCallback callback) {
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String body = response.body().string();
                List<Recipe> recipes = parseRecipes(body);
                mainHandler.post(() -> callback.onSuccess(recipes));
            }
        });
    }

    private List<Recipe> parseRecipes(String json) {
        List<Recipe> recipes = new ArrayList<>();
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            JsonArray meals = obj.getAsJsonArray("meals");
            if (meals == null) return recipes;
            for (JsonElement el : meals) {
                JsonObject m = el.getAsJsonObject();
                recipes.add(parseDetailedRecipe(m));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recipes;
    }

    private Recipe parseDetailedRecipe(JsonObject m) {
        String id = getStr(m, "idMeal");
        String title = getStr(m, "strMeal");
        String category = getStr(m, "strCategory");
        String image = getStr(m, "strMealThumb");
        String instructions = getStr(m, "strInstructions");
        String area = getStr(m, "strArea");

        // Construire la liste des ingrédients
        StringBuilder ingredients = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            String ing = getStr(m, "strIngredient" + i);
            String measure = getStr(m, "strMeasure" + i);
            if (ing != null && !ing.isEmpty()) {
                ingredients.append("• ").append(measure).append(" ").append(ing).append("\n");
            }
        }

        return new Recipe(id, title, category, image,
                instructions, ingredients.toString(), area);
    }

    private String getStr(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return "";
        return el.getAsString();
    }
}