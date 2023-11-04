package com.example.oc_p7_go4lunch.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.example.oc_p7_go4lunch.firestore.FirestoreHelper;
import com.example.oc_p7_go4lunch.utils.OnUserAddedListener;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.List;

// LoginActivity class extends AppCompatActivity, which is the base class for activities in Android
public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
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

            // Null check before proceeding
            if (firebaseUser == null) {
                showSignInError("User data is not available. Please try again.");
                return;
            }

            // Fetching photo URL of user and performing null check
            Uri photoProfile = firebaseUser.getPhotoUrl();
            if (photoProfile == null) {
                showSignInError("Profile photo is not available.");
                return;
            }

            // Check if user exists in Firestore
            firestoreHelper.getUsersCollection().document(firebaseUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // User already exists
                                // Correction: Provide the required second argument for getUserData
                                firestoreHelper.getUserData(firebaseUser.getUid(), new FirestoreHelper.OnUserDataReceivedListener() {
                                    @Override
                                    public void onUserDataReceived(UserModel userModel) {
                                        // Handle received user data
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        // Handle the error
                                    }
                                });
                            } else {
                                // Create new user
                                // You need to uncomment and correctly implement the addUser method call here
                                // Make sure to pass all the required parameters
                            }
                        }
                    });



            // Navigate to MainActivity
            Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
            mainActivity.putExtra("user", firebaseUser);
            mainActivity.putExtra("photo", photoProfile);
            startActivity(mainActivity);

        } else {
            // Handle the error case
            if (response == null) {
                showSignInError("Sign-in was canceled.");
            } else if (response.getError() != null) {
                Log.e("LoginActivity", "Sign-in error: ", response.getError());
                showSignInError("Failed to sign in. Please try again.");
            }
        }
    }

    private void showSignInError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
}