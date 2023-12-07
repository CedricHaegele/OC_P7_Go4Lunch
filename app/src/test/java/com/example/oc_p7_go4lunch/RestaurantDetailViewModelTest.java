package com.example.oc_p7_go4lunch;

import androidx.lifecycle.MutableLiveData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import android.net.Uri;
import com.example.oc_p7_go4lunch.view.viewmodel.RestaurantDetailViewModel;

@RunWith(RobolectricTestRunner.class) // Utilise Robolectric pour simuler l'environnement Android.
public class RestaurantDetailViewModelTest {

    private RestaurantDetailViewModel viewModel;

    @Before
    public void setUp() {
        // Initialise le ViewModel avant chaque test.
        viewModel = new RestaurantDetailViewModel(null, null, null);
    }

    @Test
    public void prepareOpenWebsite_ValidWebsiteUrl_OpenWebsiteActionSet() {
        String validWebsiteUrl = "https://www.google.com";
        viewModel.prepareOpenWebsite(validWebsiteUrl);
        // Vérifie que l'Uri correct est défini pour une URL valide.
        assertEquals(Uri.parse(validWebsiteUrl), viewModel.getOpenWebsiteAction().getValue());
    }

    @Test
    public void prepareOpenWebsite_NullWebsiteUrl_NoActionSet() {
        viewModel.prepareOpenWebsite(null);
        // Vérifie que rien n'est défini si l'URL est nulle.
        assertNull(viewModel.getOpenWebsiteAction().getValue());
    }

    @Test
    public void prepareOpenWebsite_EmptyWebsiteUrl_NoActionSet() {
        viewModel.prepareOpenWebsite("");
        // Vérifie que rien n'est défini si l'URL est vide.
        assertNull(viewModel.getOpenWebsiteAction().getValue());
    }

    @Test
    public void prepareOpenDialer_ValidPhoneNumber_DialerActionSet() {
        String validPhoneNumber = "1234567890";
        viewModel.prepareOpenDialer(validPhoneNumber);
        // Vérifie que l'Uri correct est défini pour un numéro de téléphone valide.
        assertEquals(Uri.parse("tel:" + validPhoneNumber), viewModel.getOpenDialerAction().getValue());
    }

    @Test
    public void prepareOpenDialer_NullPhoneNumber_NoActionSet() {
        viewModel.prepareOpenDialer(null);
        // Vérifie que rien n'est défini si le numéro de téléphone est nul.
        assertNull(viewModel.getOpenDialerAction().getValue());
    }

    @Test
    public void prepareOpenDialer_EmptyPhoneNumber_NoActionSet() {
        viewModel.prepareOpenDialer("");
        // Vérifie que rien n'est défini si le numéro de téléphone est vide.
        assertNull(viewModel.getOpenDialerAction().getValue());
    }
}
