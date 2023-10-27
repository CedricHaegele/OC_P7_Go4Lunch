package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.helper.FirestoreHelper;
import com.example.oc_p7_go4lunch.model.firestore.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// LoginActivity class extends AppCompatActivity, which is the base class for activities in Android
public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 123;
    private FirestoreHelper firestoreHelper;

    // Variable to handle the sign-in result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult // Callback method to handle sign-in result
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestoreHelper = new FirestoreHelper();

        // Setting the background drawable for the activity
        getWindow().setBackgroundDrawableResource(R.drawable.lunch_time);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(mainActivity);
            finish();
        } else {
            startSignInActivity();
        }
    }

    // Method to initialize the sign-in process
    private void startSignInActivity() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch sign-in intent using ActivityResultLauncher
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.MyFirebaseUIToolbar)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false, true)
                .build();

        signInLauncher.launch(signInIntent);
    }


    // Method to handle the result of the sign-in process
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        // Logging the result for debugging
        Log.d("LoginActivity", "onSignInResult called. Result code: " + result.getResultCode());

        // Extract the sign-in response
        IdpResponse response = result.getIdpResponse();

        if (result.getResultCode() == RESULT_OK) {
            // Successful sign-in
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            // Fetching photo URL of user
            Uri photoProfile = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl();

            // Creating a user model object
            assert firebaseUser != null;
            UserModel user = new UserModel(firebaseUser.getEmail(), firebaseUser.getDisplayName(), String.valueOf(firebaseUser.getPhotoUrl()));

            // Check if user exists in Firestore
            firestoreHelper.getUsersCollection().document(firebaseUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // User already exists
                                Log.d("Firestore", "User already exists");
                            } else {
                                // Create new user
                                Log.d("Firestore", "Creating new user");
                                firestoreHelper.addUser(firebaseUser.getUid(), firebaseUser.getDisplayName());
                            }
                        } else {
                            Log.d("Firestore", "Failed to get user", task.getException());
                        }
                    });


            // Navigate to MainActivity
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
