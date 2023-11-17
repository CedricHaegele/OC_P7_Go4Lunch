package com.example.oc_p7_go4lunch.factories;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.oc_p7_go4lunch.viewmodel.RestaurantDetailViewModel;
import com.example.oc_p7_go4lunch.webservices.RestaurantApiService;

// Déclaration de la classe RestaurantDetailViewModelFactory qui implémente l'interface ViewModelProvider.Factory.
// Cette interface est utilisée pour instancier des ViewModel(s) avec des paramètres personnalisés.
public class RestaurantDetailViewModelFactory implements ViewModelProvider.Factory {

    // Déclaration d'une variable privée de type RestaurantApiService.
    // Cette variable sera utilisée pour passer une instance de RestaurantApiService au ViewModel.
    private final RestaurantApiService restaurantApiService;

    // Constructeur de la classe RestaurantDetailViewModelFactory.
    // Il prend une instance de RestaurantApiService en tant que paramètre.
    public RestaurantDetailViewModelFactory(RestaurantApiService restaurantApiService) {
        // Affectation de l'instance de RestaurantApiService à la variable membre de la classe.
        this.restaurantApiService = restaurantApiService;
    }

    // Redéfinition de la méthode create de l'interface ViewModelProvider.Factory.
    // Cette méthode est appelée pour créer une instance de ViewModel.
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        // Vérification si le modelClass spécifié est assignable à RestaurantDetailViewModel.
        // Cela signifie que cette fabrique peut seulement créer des instances de RestaurantDetailViewModel ou de ses sous-classes.
        if (modelClass.isAssignableFrom(RestaurantDetailViewModel.class)) {
            // Retourne une nouvelle instance de RestaurantDetailViewModel, en lui passant l'instance de RestaurantApiService.
            // Le cast (T) est nécessaire car la méthode doit retourner un type générique T.
            return (T) new RestaurantDetailViewModel(restaurantApiService);
        }
        // Si le modelClass demandé n'est pas assignable à RestaurantDetailViewModel,
        // on lance une exception pour indiquer que le type de ViewModel demandé est inconnu.
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


