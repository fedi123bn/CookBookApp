package com.example.cookbookapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> allRecipes = new ArrayList<>();
    private List<Recipe> apiRecipes = new ArrayList<>();
    private MealApiClient apiClient;
    private DatabaseHelper dbHelper;
    private GoogleSignInClient googleSignInClient;
    private boolean showingFavorites = false;
    private EditText searchBar;
    private Button btnFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        apiClient = new MealApiClient();
        dbHelper = new DatabaseHelper(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar);
        btnFilter = findViewById(R.id.btnFilter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new RecipeAdapter(allRecipes, recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            intent.putExtra("recipe_title", recipe.getTitle());
            intent.putExtra("recipe_image", recipe.getImageUrl());
            intent.putExtra("recipe_category", recipe.getCategory());
            intent.putExtra("recipe_ingredients", recipe.getIngredients());
            intent.putExtra("recipe_instructions", recipe.getInstructions());
            intent.putExtra("recipe_area", recipe.getArea());
            startActivity(intent);
        }, this);
        recyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            showingFavorites = savedInstanceState.getBoolean("showingFavorites", false);
        }

        setupBottomNavigation();
        setupSearch();
        setupFilter();

        if (showingFavorites) {
            loadFavorites();
        } else {
            loadRecipes();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!showingFavorites) {
            loadRecipes();
        }
    }

    private void setupFilter() {
        btnFilter.setOnClickListener(v -> {
            String[] categories = {"Toutes", "Seafood", "Chicken", "Beef", "Pasta",
                    "Dessert", "Vegetarian", "Breakfast", "Side"};
            new AlertDialog.Builder(this)
                    .setTitle("Filtrer par catégorie")
                    .setItems(categories, (dialog, which) -> {
                        if (which == 0) {
                            loadRecipes();
                        } else {
                            filterByCategory(categories[which]);
                        }
                    })
                    .show();
        });
    }

    private void filterByCategory(String category) {
        apiClient.getRecipesByCategory(category, new MealApiClient.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                allRecipes.clear();
                allRecipes.addAll(recipes);
                // Filtrer aussi les recettes perso
                List<Recipe> myRecipes = dbHelper.getMyRecipes();
                for (Recipe r : myRecipes) {
                    if (r.getCategory().equalsIgnoreCase(category)) {
                        allRecipes.add(r);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RecipeListActivity.this,
                        "Erreur filtre", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadRecipes();
                } else {
                    searchRecipes(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchRecipes(String query) {
        // Chercher dans API
        apiClient.searchRecipes(query, new MealApiClient.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                allRecipes.clear();
                allRecipes.addAll(recipes);

                // Chercher aussi dans les recettes perso
                List<Recipe> myRecipes = dbHelper.getMyRecipes();
                for (Recipe r : myRecipes) {
                    if (r.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        allRecipes.add(r);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                // Si API échoue, chercher quand même dans recettes perso
                allRecipes.clear();
                List<Recipe> myRecipes = dbHelper.getMyRecipes();
                for (Recipe r : myRecipes) {
                    if (r.getTitle().toLowerCase().contains(query.toLowerCase())) {
                        allRecipes.add(r);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void loadRecipes() {
        apiClient.getRandomRecipes(new MealApiClient.RecipeCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                allRecipes.clear();
                allRecipes.addAll(recipes);
                apiRecipes.clear();
                apiRecipes.addAll(recipes);
                List<Recipe> myRecipes = dbHelper.getMyRecipes();
                allRecipes.addAll(myRecipes);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(RecipeListActivity.this,
                        "Erreur chargement", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavorites() {
        allRecipes.clear();
        List<Recipe> favorites = dbHelper.getFavorites();
        allRecipes.addAll(favorites);
        adapter.notifyDataSetChanged();
    }

    private void logout() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(RecipeListActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_all) {
                showingFavorites = false;
                searchBar.setText("");
                loadRecipes();
                return true;
            } else if (id == R.id.nav_favorites) {
                showingFavorites = true;
                loadFavorites();
                return true;
            } else if (id == R.id.nav_add) {
                startActivity(new Intent(RecipeListActivity.this, AddRecipeActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                logout();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showingFavorites", showingFavorites);
    }
}