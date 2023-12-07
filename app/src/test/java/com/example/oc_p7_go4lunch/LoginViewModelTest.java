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

@RunWith(RobolectricTestRunner.class)
public class LoginViewModelTest {

    @Mock
    private FirebaseAuth mockFirebaseAuth;
    @Mock
    private FirebaseUser mockFirebaseUser;

    private LoginViewModel loginViewModel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // Simuler FirebaseAuth et FirebaseUser
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);


        loginViewModel = new LoginViewModel() {
            @Override
            protected FirebaseAuth getFirebaseAuth() {
                return mockFirebaseAuth;
            }
        };
    }

    @Test
    public void authenticate_UserSignedIn_UpdatesLiveData() {
        // Observer pour LiveData
        Observer<FirebaseUser> observer = mock(Observer.class);
        loginViewModel.getUserLiveData().observeForever(observer);

        // Appeler authenticate
        loginViewModel.authenticate();

        // Vérifier si userLiveData a été mis à jour
        verify(observer).onChanged(mockFirebaseUser);
    }
}
