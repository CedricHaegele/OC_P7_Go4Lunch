package com.example.oc_p7_go4lunch.view.fragment;

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
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.oc_p7_go4lunch.R;

// Extends Fragment and implements OnSharedPreferenceChangeListener to listen for preference changes
public class SettingsFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    // Declaration of ViewModel and SharedPreferences
    private SharedPreferences sharedPreferences;
    // LiveData to track the state of dark mode
    private final MutableLiveData<Boolean> darkModeEnabled = new MutableLiveData<>();

    @Nullable
    @Override
    // onCreateView is called to draw the user interface for the fragment
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.settings, container, false);

        // Initialize the ViewModel

        // Initialize SharedPreferences to store and retrieve user preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        // Retrieve and set the current state of dark mode from SharedPreferences
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode_enabled", false);
        darkModeEnabled.setValue(isDarkModeEnabled);

        // Load PreferencesFragment only if there is no instance state saved
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new PreferencesFragment())
                    .commit();
        }

        // Set up the action bar for the fragment
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        return view;
    }

    @Override
    // onResume is called when the fragment becomes visible and active
    public void onResume() {
        super.onResume();
        // Register the listener for SharedPreferences changes
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    // onPause is called when the fragment is no longer in the foreground
    public void onPause() {
        super.onPause();
        // Unregister the listener to avoid memory leaks
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    // Called when a shared preference is changed, added, or removed
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Check if the preference changed is for notifications
        if ("notifications_enabled".equals(key)) {
            // Get the new value of the preference
            boolean enabled = sharedPreferences.getBoolean(key, false);
            // Toggle notification service based on new value
            toggleNotificationService(enabled);
        }
    }

    // Toggle the state of notification based on user preference
    private void toggleNotificationService(boolean enabled) {
        // Edit the SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Put the new value for notifications
        editor.putBoolean("notifications_enabled", enabled);
        // Apply changes asynchronously
        editor.apply();
    }

    // Inner class to handle preferences UI
    public static class PreferencesFragment extends PreferenceFragmentCompat {
        @Override
        // Create preferences UI from the XML resource
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}
