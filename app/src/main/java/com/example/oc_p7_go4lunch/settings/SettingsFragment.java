package com.example.oc_p7_go4lunch.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.oc_p7_go4lunch.R;

public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private SharedPreferences sharedPreferences;
    private MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings, container, false);

        // Initialise le ViewModel
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        // Initialise les SharedPreferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode_enabled", false);

        // Met à jour la LiveData avec la valeur lue depuis les SharedPreferences
        darkModeEnabled.setValue(isDarkModeEnabled);

        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new PreferencesFragment())
                    .commit();
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        return view;
    }

    public static class PreferencesFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}
