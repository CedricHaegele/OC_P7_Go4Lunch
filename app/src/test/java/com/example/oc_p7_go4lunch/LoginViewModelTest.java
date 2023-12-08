package com.example.oc_p7_go4lunch;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import androidx.lifecycle.Observer;

import com.example.oc_p7_go4lunch.view.viewmodel.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

// Utilisation de Robolectric pour le test unitaire
@RunWith(RobolectricTestRunner.class)
public class LoginViewModelTest {

    @Mock // Simule l'authentification Firebase
    private FirebaseAuth mockFirebaseAuth;
    @Mock // Simule un utilisateur Firebase
    private FirebaseUser mockFirebaseUser;

    // Objet ViewModel à tester
    private LoginViewModel loginViewModel;

    // Configuration initiale avant chaque test
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); // Initialise les objets simulés

        // Simule le retour de l'utilisateur courant Firebase
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        // Crée une instance de LoginViewModel pour le test
        loginViewModel = new LoginViewModel() {
            @Override
            protected FirebaseAuth getFirebaseAuth() {
                return mockFirebaseAuth;
            }
        };
    }

    // Test vérifiant la mise à jour de LiveData lors de l'authentification
    @Test
    public void authenticate_UserSignedIn_UpdatesLiveData() {
        // Simule un observer pour LiveData
        Observer<FirebaseUser> observer = mock(Observer.class);
        // Attache l'observer au LiveData de loginViewModel
        loginViewModel.getUserLiveData().observeForever(observer);

        // Exécute la méthode à tester
        loginViewModel.authenticate();

        // Vérifie si le LiveData a été mis à jour avec l'utilisateur simulé
        verify(observer).onChanged(mockFirebaseUser);
    }
}
