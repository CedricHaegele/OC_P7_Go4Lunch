package com.example.oc_p7_go4lunch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.example.oc_p7_go4lunch.MVVM.repositories.RestaurantRepository;
import com.example.oc_p7_go4lunch.model.googleplaces.PlaceModel;
import com.google.android.libraries.places.api.model.Place;

public class RestaurantRepositoryTest {

    @Mock
    private Place mockPlace; // Création d'un mock (simulation) pour l'objet Place.

    private RestaurantRepository restaurantRepository;

    @Before
    public void setUp() {
        initMocks(this); // Initialisation des mocks pour cette classe de test.
        restaurantRepository = new RestaurantRepository(); // Création d'une instance de RestaurantRepository.

        // Configuration du comportement attendu du mockPlace.
        // Lorsqu'une méthode est appelée sur mockPlace, elle renvoie une valeur spécifique.
        when(mockPlace.getName()).thenReturn("Test Restaurant");
        when(mockPlace.getAddress()).thenReturn("Test Address");
        when(mockPlace.getRating()).thenReturn(4.5);
    }

    @Test
    public void testConvertPlaceToRestaurantModel() {
        // Appel de la méthode convertPlaceToRestaurantModel avec le mockPlace.
        PlaceModel result = restaurantRepository.convertPlaceToRestaurantModel(mockPlace);

        // Vérification si les valeurs retournées sont celles attendues.
        assertEquals("Test Restaurant", result.getName());
        assertEquals("Test Address", result.getVicinity());
        assertEquals(4.5, result.getRating(), 0.01); // Le troisième paramètre est la précision pour les doubles.
    }
}
