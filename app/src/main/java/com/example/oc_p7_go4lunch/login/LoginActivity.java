package com.example.oc_p7_go4lunch.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.activities.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;


public class LoginActivity extends AppCompatActivity {

    // Déclaration du ViewModel qui gère la logique de l'authentification.
    private LoginViewModel loginViewModel;

    // Gère le résultat de l'authentification.
    // Définition d'un launcher d'activité pour gérer le résultat de l'authentification Firebase.
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    // Liste des fournisseurs d'authentification (e-mail et Google).
    private final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.TwitterBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialisation du ViewModel.
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observer les changements dans l'état d'authentification de l'utilisateur.
        loginViewModel.getUserLiveData().observe(this, this::updateUI);

        // Déclenche le processus d'authentification.
        loginViewModel.authenticate();
    }

    // Met à jour l'interface utilisateur en fonction de l'état de connexion de l'utilisateur.
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Si l'utilisateur est connecté, navigue vers MainActivity.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Termine l'activité actuelle pour empêcher le retour à l'écran de connexion.
        } else {
            // Si aucun utilisateur n'est connecté, lance le processus de connexion Firebase UI.
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)  // Définit les fournisseurs d'authentification.
                    .setTheme(R.style.MyFirebaseUIToolbar)  // Définit le thème de l'interface de connexion.
                    .build();
            signInLauncher.launch(signInIntent);  // Lance l'intention de connexion.
        }
    }

    // Gère le résultat du processus de connexion.
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            // Si la connexion est réussie, réauthentifie l'utilisateur.
            loginViewModel.authenticate();
        }
    }
}
