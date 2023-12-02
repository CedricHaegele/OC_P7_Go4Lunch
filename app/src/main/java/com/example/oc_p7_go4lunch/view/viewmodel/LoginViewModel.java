package com.example.oc_p7_go4lunch.view.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginViewModel extends ViewModel {
    // MutableLiveData est un type de données observable. Ici, il détient et gère les données de l'utilisateur Firebase.
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    // Cette méthode publique retourne l'objet LiveData. LiveData est un conteneur de données observable pour la classe FirebaseUser.
    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    // Cette méthode est appelée pour déclencher le processus d'authentification.
    public void authenticate() {
        // Obtient l'instance de FirebaseAuth.
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Obtient l'utilisateur actuellement connecté.
        FirebaseUser currentUser = auth.getCurrentUser();

        // Met à jour l'objet MutableLiveData avec l'utilisateur actuel. Cela va notifier tous les observateurs de ce LiveData.
        userLiveData.setValue(currentUser);
    }
}

