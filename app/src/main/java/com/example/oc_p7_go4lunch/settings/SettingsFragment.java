package com.example.oc_p7_go4lunch.settings;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;

import com.example.oc_p7_go4lunch.databinding.SettingsBinding;

public class SettingsFragment extends Fragment {
    private SettingsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Utilisation de View Binding pour lier le fragment au fichier XML
        binding = SettingsBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialisation du ViewModel
        SettingsViewModel viewModel = initializeViewModel();
        observeViewModel(viewModel);
        updateUIFromViewModel(viewModel);
        return view;
    }

    private SettingsViewModel initializeViewModel() {
        return new ViewModelProvider(this, AndroidViewModelFactory.getInstance(requireActivity().getApplication())).get(SettingsViewModel.class);
    }

    private void observeViewModel(SettingsViewModel viewModel) {
        viewModel.getSettingsText().observe(getViewLifecycleOwner(), settingsText -> binding.settings.setText(settingsText));
        viewModel.getNotificationsTitleText().observe(getViewLifecycleOwner(), notificationsTitleText -> binding.notificationsTitle.setText(notificationsTitleText));
        viewModel.getTextColor().observe(getViewLifecycleOwner(), textColor -> {
            binding.settings.setTextColor(textColor);
            binding.notificationsTitle.setTextColor(textColor);
        });
        viewModel.getTextStyle().observe(getViewLifecycleOwner(), textStyle -> {
            if (textStyle == Typeface.BOLD) {
                binding.settings.setTypeface(null, Typeface.BOLD);
                binding.notificationsTitle.setTypeface(null, Typeface.BOLD);
            } else {
                binding.settings.setTypeface(null, Typeface.NORMAL);
                binding.notificationsTitle.setTypeface(null, Typeface.NORMAL);
            }
        });
    }

    private void updateUIFromViewModel(SettingsViewModel viewModel) {
        viewModel.updateTextColor(getResources().getColor(com.google.android.libraries.places.R.color.quantum_black_100));
        viewModel.updateTextStyle(Typeface.BOLD);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
