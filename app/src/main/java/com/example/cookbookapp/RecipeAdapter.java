package com.example.cookbookapp;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final List<Recipe> recipes;
    private final OnRecipeClickListener listener;
    private final DatabaseHelper dbHelper;

    public RecipeAdapter(List<Recipe> recipes, OnRecipeClickListener listener, Context context) {
        this.recipes = recipes;
        this.listener = listener;
        this.dbHelper = new DatabaseHelper(context);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.tvTitle.setText(recipe.getTitle());
        holder.tvCategory.setText(recipe.getCategory());

        String imageUrl = recipe.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("content://")) {
                Glide.with(holder.itemView.getContext())
                        .load(Uri.parse(imageUrl))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .override(200, 200)
                        .centerCrop()
                        .into(holder.imgRecipe);
            } else {
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .override(200, 200)
                        .centerCrop()
                        .into(holder.imgRecipe);
            }
        } else {
            holder.imgRecipe.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Clic normal → détail
        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));

        // Appui long → dialogue suppression
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Supprimer")
                    .setMessage("Voulez-vous supprimer \"" + recipe.getTitle() + "\" ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        // Supprimer des favoris ET des recettes perso
                        dbHelper.removeFavorite(recipe.getId());
                        dbHelper.removeMyRecipe(recipe.getId());
                        recipes.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, recipes.size());
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRecipe;
        TextView tvTitle, tvCategory;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRecipe = itemView.findViewById(R.id.imgRecipe);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}