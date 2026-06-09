package com.example.cookbookapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Recipe currentRecipe;
    private Button btnFavorite;
    private Button btnMap;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        dbHelper = new DatabaseHelper(this);

        String id = getIntent().getStringExtra("recipe_id");
        String title = getIntent().getStringExtra("recipe_title");
        String image = getIntent().getStringExtra("recipe_image");
        String category = getIntent().getStringExtra("recipe_category");
        String ingredients = getIntent().getStringExtra("recipe_ingredients");
        String instructions = getIntent().getStringExtra("recipe_instructions");
        String area = getIntent().getStringExtra("recipe_area");

        currentRecipe = new Recipe(id, title, category, image,
                instructions, ingredients, area);

        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvCategory = findViewById(R.id.tvCategory);
        TextView tvArea = findViewById(R.id.tvArea);
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        TextView tvInstructions = findViewById(R.id.tvInstructions);
        ImageView imgRecipe = findViewById(R.id.imgRecipe);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnMap = findViewById(R.id.btnMap);

        tvTitle.setText(title);
        tvCategory.setText("Catégorie : " + category);
        tvArea.setText("Origine : " + area);
        tvIngredients.setText(ingredients);
        tvInstructions.setText(instructions);

        Glide.with(this).load(image).into(imgRecipe);

        isFavorite = dbHelper.isFavorite(id);
        updateFavoriteButton();

        if (savedInstanceState != null) {
            isFavorite = savedInstanceState.getBoolean("isFavorite", isFavorite);
            ratingBar.setRating(savedInstanceState.getFloat("rating", 0f));
            updateFavoriteButton();
        }

        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Bouton Google Maps
        btnMap.setOnClickListener(v -> openNearbyRestaurants());

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                Toast.makeText(this, "Note : " + rating + " ⭐", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openNearbyRestaurants() {

        String recipeName = currentRecipe.getTitle();

        // Exemple : "restaurants pizza"
        String searchQuery = "restaurants " + recipeName.toLowerCase();

        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(searchQuery));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Toast.makeText(this, "Google Maps n'est pas installé", Toast.LENGTH_SHORT).show();
        }
    }
    private void toggleFavorite() {
        if (!isFavorite) {
            dbHelper.addFavorite(currentRecipe);
            isFavorite = true;
            Toast.makeText(this, "Ajouté aux favoris !", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.removeFavorite(currentRecipe.getId());
            isFavorite = false;
            Toast.makeText(this, "Retiré des favoris !", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (isFavorite) {
            btnFavorite.setText("❤️ Retirer des favoris");
        } else {
            btnFavorite.setText("🤍 Ajouter aux favoris");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isFavorite", isFavorite);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        outState.putFloat("rating", ratingBar.getRating());
    }
}