package com.example.cookbookapp;

public class Recipe {
    private String id;
    private String title;
    private String category;
    private String imageUrl;
    private String instructions;
    private String ingredients;
    private String area;
    private boolean isFavorite;
    private String firestoreId; // ID du document Firestore

    // Constructeur vide (requis pour Firestore)
    public Recipe() {}

    public Recipe(String id, String title, String category,
                  String imageUrl, String instructions,
                  String ingredients, String area) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.ingredients = ingredients;
        this.area = area;
        this.isFavorite = false;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getInstructions() { return instructions; }
    public String getIngredients() { return ingredients; }
    public String getArea() { return area; }
    public boolean isFavorite() { return isFavorite; }
    public String getFirestoreId() { return firestoreId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }
    public void setArea(String area) { this.area = area; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }
}