package com.example.actnow.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.actnow.AuthorizActivity;
import com.example.actnow.R;
import com.example.actnow.Fragments.BecomeOrganizerFragment;
import com.example.actnow.Fragments.EditProfileFragment;
import com.example.actnow.Utils.PreferencesManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsFragment extends Fragment {
    private RadioGroup rgTheme;
    private RadioButton rbLight, rbDark;
    private SwitchMaterial switchNotifications;
    private MaterialButton btnLogout, btnDeleteAccount, btnEditProfile, btnBecomeOrganizer;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private PreferencesManager preferencesManager;
    private String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupFirebase();
        setupPreferences();
        checkUserAccountType(); // Проверяем тип аккаунта пользователя
        setupListeners();
    }

    private void initializeViews(View view) {
        rgTheme = view.findViewById(R.id.rgTheme);
        rbLight = view.findViewById(R.id.rbLight);
        rbDark = view.findViewById(R.id.rbDark);
        switchNotifications = view.findViewById(R.id.switchNotifications);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnBecomeOrganizer = view.findViewById(R.id.btnBecomeOrganizer);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser != null ? currentUser.getUid() : null;
    }

    private void setupPreferences() {
        preferencesManager = new PreferencesManager(requireContext());
        setupThemeSettings();
        switchNotifications.setChecked(preferencesManager.getNotificationsEnabled());
    }

    private void checkUserAccountType() {
        if (currentUserId == null) return;

        db.collection("Profiles").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String accountType = documentSnapshot.getString("accountType");
                        // Лог для отладки
                        Log.d("SettingsFragment", "User accountType: " + (accountType != null ? accountType : "null"));
                        // Скрываем кнопку для legal_entity и individual_organizer
                        if (accountType != null && (accountType.equalsIgnoreCase("legal_entity") || accountType.equalsIgnoreCase("individual_organizer"))) {
                            btnBecomeOrganizer.setVisibility(View.GONE);
                        } else {
                            btnBecomeOrganizer.setVisibility(View.VISIBLE);
                            btnBecomeOrganizer.setText("Стать организатором");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SettingsFragment", "Error checking user accountType: " + e.getMessage());
                    btnBecomeOrganizer.setVisibility(View.VISIBLE); // Значение по умолчанию
                    btnBecomeOrganizer.setText("Стать организатором");
                });
    }

    private void setupThemeSettings() {
        int currentTheme = preferencesManager.getThemeMode();
        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                rbLight.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                rbDark.setChecked(true);
                break;
            default:
                rbLight.setChecked(true); // Значение по умолчанию
                break;
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (checkedId == R.id.rbLight) {
                mode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (checkedId == R.id.rbDark) {
                mode = AppCompatDelegate.MODE_NIGHT_YES;
            }
            preferencesManager.setThemeMode(mode);
            AppCompatDelegate.setDefaultNightMode(mode);
            requireActivity().recreate(); // Перезапуск активности для применения темы
        });
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> logout());
        btnDeleteAccount.setOnClickListener(v -> deleteAccount());
        btnEditProfile.setOnClickListener(v -> {
            if (isAdded()) {
                EditProfileFragment editProfileFragment = new EditProfileFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, editProfileFragment)
                        .addToBackStack("settings_to_edit")
                        .commit();
            }
        });
        btnBecomeOrganizer.setOnClickListener(v -> {
            if (isAdded()) {
                BecomeOrganizerFragment becomeOrganizerFragment = new BecomeOrganizerFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, becomeOrganizerFragment)
                        .addToBackStack("settings_to_become_organizer")
                        .commit();
            }
        });
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setNotificationsEnabled(isChecked);
        });
    }

    private void logout() {
        mAuth.signOut();
        navigateToLogin();
    }

    private void deleteAccount() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(aVoid ->
                        user.delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    if (isAdded()) {
                                        Toast.makeText(getContext(), "Аккаунт удален", Toast.LENGTH_SHORT).show();
                                        navigateToLogin();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isAdded()) {
                                        Toast.makeText(getContext(), "Ошибка удаления аккаунта: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                })
                )
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Ошибка удаления данных: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(getActivity(), AuthorizActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}