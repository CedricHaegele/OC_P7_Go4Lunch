package com.example.oc_p7_go4lunch.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

import com.example.oc_p7_go4lunch.R;
import com.example.oc_p7_go4lunch.activities.MainActivity;
import com.example.oc_p7_go4lunch.databinding.LoginActivityBinding;
import com.example.oc_p7_go4lunch.firebaseUser.UserModel;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private LoginActivityBinding binding;
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = LoginActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        onStart();
        setupListeners();
    }

    private void setupListeners() {
        binding.mailLoginButton.setOnClickListener(view -> startSignInActivity());
    }

    private void startSignInActivity() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setTheme(R.style.MyFirebaseUIToolbar)
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false, true)
                .build();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    handleResponseAfterSignIn(requestCode,resultCode,data);
                }
            } else {
               
            }
        }
    }

    // Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();

                    FirebaseFirestore.getInstance().collection("users")
                            .document(userId)
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null && document.exists()) {
                                        // L'utilisateur existe déjà, rediriger vers MainActivity
                                        navigateToMainActivity();
                                    } else {
                                        // L'utilisateur n'existe pas, l'ajouter à Firestore
                                        addUserToFirestore(userId);
                                    }
                                } else {
                                    showSnackBar(getString(R.string.error_unknown_error));
                                }
                            });
                }
            } else {
                if (response == null) {
                    showSnackBar(getString(R.string.error_authentication_canceled));
                } else if (response.getError() != null) {
                    if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                        showSnackBar(getString(R.string.error_no_internet));
                    } else {
                        showSnackBar(getString(R.string.error_unknown_error));
                    }
                }
            }
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void addUserToFirestore(String userId) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("name", user.getDisplayName());
            userData.put("email", user.getEmail());
            userData.put("urlPicture", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);

            FirebaseFirestore.getInstance().collection("users")
                    .document(userId)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> navigateToMainActivity())
                    .addOnFailureListener(e -> {

                    });
        }
    }

    // Show Snack Bar with a message
    private void showSnackBar(String message) {
        Snackbar.make(binding.mailLoginButton, message, Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}
