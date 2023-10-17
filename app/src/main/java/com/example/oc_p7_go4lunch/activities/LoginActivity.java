package com.example.oc_p7_go4lunch.activities;

// Importing the necessary Android and Firebase classes
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
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// LoginActivity class extends AppCompatActivity, which is the base class for activities in Android
public class LoginActivity extends AppCompatActivity {

    // Variable to handle the sign-in result
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult // Callback method to handle sign-in result
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setting the background drawable for the activity
        getWindow().setBackgroundDrawableResource(R.drawable.lunch_time);

        // Initialize the sign-in process
        init();
    }

    // Method to initialize the sign-in process
    private void init() {
        // Define list of identity providers like Email and Google
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Build the sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)  // Set authentication providers
                .setTheme(R.style.MyFirebaseUIToolbar)  // Set custom theme
                .build();

        // Launch sign-in intent
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
            FirebaseHelper.getUsersCollection().document(firebaseUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // User already exists
                                Log.d("Firestore", "User already exists");
                            } else {
                                // Create new user
                                Log.d("Firestore", "Creating new user");
                                FirebaseHelper.createUser(firebaseUser.getUid(), user);
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
