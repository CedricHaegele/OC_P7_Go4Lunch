package com.example.oc_p7_go4lunch.activities;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.oc_p7_go4lunch.R;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Déclaration de la classe BaseActivity qui est une classe abstraite et qui étend AppCompatActivity.
// En tant que classe abstraite, elle sert de base pour d'autres activités et ne peut pas être instanciée directement.
public abstract class BaseActivity extends AppCompatActivity {

    // Déclaration d'une variable statique protégée 'user' de type User.
    // Étant statique, elle est partagée par toutes les instances de BaseActivity et ses sous-classes.
    // Protégée, elle est accessible dans toutes les sous-classes de BaseActivity.
    protected static User user;

    // Méthode protégée getCurrentUser qui retourne l'utilisateur actuel connecté via Firebase Auth.
    // Cette méthode utilise FirebaseAuth pour obtenir l'utilisateur actuel et est accessible dans les sous-classes de BaseActivity.
    protected FirebaseUser getCurrentUser() { return FirebaseAuth.getInstance().getCurrentUser(); }

    // Méthode protégée isCurrentUserLogged qui retourne un booléen.
    // Cette méthode vérifie si l'utilisateur actuel (obtenu par getCurrentUser) n'est pas null, indiquant ainsi si un utilisateur est connecté.
    protected Boolean isCurrentUserLogged() { return (this.getCurrentUser() != null); }

    // Méthode protégée onFailureListener qui retourne un OnFailureListener.
    // Ce listener est utilisé pour gérer les échecs des opérations Firebase.
    // En cas d'échec, un Toast est affiché avec un message d'erreur générique.
    protected OnFailureListener onFailureListener(){
        return e -> Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

}
