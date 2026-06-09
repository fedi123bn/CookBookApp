package com.example.cookbookapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        // Si déjà connecté → aller directement à la liste
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            goToRecipeList();
            return;
        }

        // Configuration Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnGoogle = findViewById(R.id.btnGoogleSignIn);

        btnLogin.setOnClickListener(v -> loginWithUsername());
        btnRegister.setOnClickListener(v -> registerWithUsername());
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    // ============ USERNAME + PASSWORD ============

    private void loginWithUsername() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Nom d'utilisateur obligatoire");
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Mot de passe obligatoire");
            return;
        }

        // Convertir username en email pour Firebase
        String email = username + "@cookbookapp.local";

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, authResult -> {
                    saveUsername(username);
                    Toast.makeText(this, "Connecté !", Toast.LENGTH_SHORT).show();
                    goToRecipeList();
                })
                .addOnFailureListener(this, e ->
                        Toast.makeText(this, "Erreur : Nom d'utilisateur ou mot de passe incorrect",
                                Toast.LENGTH_LONG).show());
    }

    private void registerWithUsername() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Nom d'utilisateur obligatoire");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mot de passe minimum 6 caractères");
            return;
        }

        // Convertir username en email pour Firebase
        String email = username + "@cookbookapp.local";

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, authResult -> {
                    saveUsername(username);
                    Toast.makeText(this, "Compte créé avec succès !", Toast.LENGTH_SHORT).show();
                    goToRecipeList();
                })
                .addOnFailureListener(this, e -> {
                    String errorMsg = e.getMessage();
                    if (errorMsg != null && errorMsg.contains("already in use")) {
                        Toast.makeText(this,
                                "Ce nom d'utilisateur existe déjà. Utilisez 'Se connecter'.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Erreur : " + errorMsg,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUsername(String username) {
        SharedPreferences prefs = getSharedPreferences("CookBookPrefs", MODE_PRIVATE);
        prefs.edit().putString("username", username).apply();
    }

    // ============ GOOGLE SIGN-IN ============

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Connexion échouée : " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {
                    Toast.makeText(this, "Connecté avec Google !", Toast.LENGTH_SHORT).show();
                    goToRecipeList();
                })
                .addOnFailureListener(this, e ->
                        Toast.makeText(this, "Erreur : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void goToRecipeList() {
        Intent intent = new Intent(this, RecipeListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}