package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.helper.FirebaseHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // Activity Result Launcher for handling sign-in results
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.lunch_time);
        init();
    }

    // Initialize the sign-in process
    private void init() {
        // Choose authentication providers (Email and Google)
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.MyFirebaseUIToolbar)
                .setLogo(R.drawable.lunch_time)
                .build();

        signInLauncher.launch(signInIntent);
    }

    // Handle the result of the sign-in process
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        Log.d("LoginActivity", "onSignInResult called. Result code: " + result.getResultCode());

        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            // Get user's photo URL
            Uri photoProfile = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl();

            // Create a new user model
            assert firebaseUser != null;
            UserModel user = new UserModel(firebaseUser.getEmail(), firebaseUser.getDisplayName(), String.valueOf(firebaseUser.getPhotoUrl()));

            // Save the user to Firestore
            FirebaseHelper.getUsersCollection();
            FirebaseHelper.createUser(firebaseUser.getUid(), user);

            // Navigate to Main Activity
            Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
            mainActivity.putExtra("user", firebaseUser);
            mainActivity.putExtra("photo", photoProfile);
            startActivity(mainActivity);
        } else {
            // Sign-in failed
            if (response != null) {
                finish();
            }
        }
    }
}


