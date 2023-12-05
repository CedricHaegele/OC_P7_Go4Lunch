package com.example.oc_p7_go4lunch.view.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.view.viewmodel.LoginViewModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // Declaration of the LoginViewModel to manage the logic of authentication.
    private LoginViewModel loginViewModel;

    // Managing the result of authentication.
    // Setting up an ActivityResultLauncher for handling Firebase authentication result.
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    // List of authentication providers (email, Google, and Twitter).
    private final List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build(),
            new AuthUI.IdpConfig.TwitterBuilder().build());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing the ViewModel.
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Observing changes in the user's authentication status.
        loginViewModel.getUserLiveData().observe(this, this::updateUI);

        // Triggering the authentication process.
        loginViewModel.authenticate();
    }

    // Updates the UI based on the user's login status.
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // If the user is logged in, navigate to MainActivity.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Ends the current activity to prevent returning to the login screen.
        } else {
            // If no user is logged in, launches Firebase UI login process.
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)  // Sets the authentication providers.
                    .setTheme(R.style.MyFirebaseUIToolbar)  // Sets the theme for the login UI.
                    .build();
            signInLauncher.launch(signInIntent);  // Launches the sign-in intent.
        }
    }

    // Handles the result of the sign-in process.
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            // If login is successful, re-authenticate the user.
            loginViewModel.authenticate();
        }
    }
}
