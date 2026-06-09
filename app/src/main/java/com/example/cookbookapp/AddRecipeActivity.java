package com.example.cookbookapp;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class AddRecipeActivity extends AppCompatActivity {

    private EditText etTitle, etIngredients, etInstructions;
    private Spinner spinnerCategory;
    private DatabaseHelper dbHelper;
    private ImageView imgPreview;
    private Uri selectedImageUri = null;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        String fileName = "recipe_" + System.currentTimeMillis() + ".jpg";
                        File file = new File(getFilesDir(), fileName);
                        FileOutputStream outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            outputStream.write(buffer, 0, length);
                        }
                        outputStream.close();
                        inputStream.close();
                        selectedImageUri = Uri.fromFile(file);
                        imgPreview.setImageURI(selectedImageUri);
                    } catch (Exception e) {
                        selectedImageUri = uri;
                        imgPreview.setImageURI(uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbHelper = new DatabaseHelper(this);

        etTitle = findViewById(R.id.etTitle);
        etIngredients = findViewById(R.id.etIngredients);
        etInstructions = findViewById(R.id.etInstructions);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imgPreview = findViewById(R.id.imgPreview);
        Button btnChooseImage = findViewById(R.id.btnChooseImage);
        Button btnSave = findViewById(R.id.btnSave);

        if (savedInstanceState != null) {
            etTitle.setText(savedInstanceState.getString("title", ""));
            etIngredients.setText(savedInstanceState.getString("ingredients", ""));
            etInstructions.setText(savedInstanceState.getString("instructions", ""));
            String uriStr = savedInstanceState.getString("imageUri", "");
            if (!uriStr.isEmpty()) {
                selectedImageUri = Uri.parse(uriStr);
                imgPreview.setImageURI(selectedImageUri);
            }
        }

        // Catégories réelles de TheMealDB
        String[] categories = {
                "Seafood", "Chicken", "Beef", "Pasta",
                "Dessert", "Vegetarian", "Breakfast", "Side"
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        btnChooseImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*"));

        btnSave.setOnClickListener(v -> saveRecipe());
    }

    private void saveRecipe() {
        String title = etTitle.getText().toString().trim();
        String ingredients = etIngredients.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : "";

        if (title.isEmpty()) {
            etTitle.setError("Le titre est obligatoire");
            return;
        }
        if (ingredients.isEmpty()) {
            etIngredients.setError("Les ingrédients sont obligatoires");
            return;
        }
        if (instructions.isEmpty()) {
            etInstructions.setError("Les instructions sont obligatoires");
            return;
        }

        Recipe recipe = new Recipe(
                null, title, category,
                imageUrl, instructions, ingredients, ""
        );

        dbHelper.addMyRecipe(recipe);
        Toast.makeText(this, "Recette ajoutée !", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", etTitle.getText().toString());
        outState.putString("ingredients", etIngredients.getText().toString());
        outState.putString("instructions", etInstructions.getText().toString());
        if (selectedImageUri != null) {
            outState.putString("imageUri", selectedImageUri.toString());
        }
    }
}