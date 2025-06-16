package com.example.actnow.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.example.actnow.MainActivity;
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        // Установка слушателя касаний на корневую разметку
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // Проверяем, произошло ли касание вне CardView
                float x = event.getX();
                float y = event.getY();
                View cardView = view.findViewById(R.id.cv_settings); // Предполагаем, что CardView имеет ID
                if (cardView != null) {
                    int[] location = new int[2];
                    cardView.getLocationOnScreen(location);
                    int cardX = location[0];
                    int cardY = location[1];
                    int cardWidth = cardView.getWidth();
                    int cardHeight = cardView.getHeight();

                    if (x < cardX || x > cardX + cardWidth || y < cardY || y > cardY + cardHeight) {
                        // Касание вне CardView, возвращаемся в ProfileFragment
                        getParentFragmentManager().popBackStack();
                        return true; // Потребляем событие
                    }
                }
            }
            return false; // Продолжаем обработку касаний внутри фрагмента
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupFirebase();
        setupPreferences();
        checkUserAccountType();
        setupListeners();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (switchNotifications != null) {
            outState.putBoolean("notifications_enabled", switchNotifications.isChecked());
        }
        if (rgTheme != null) {
            outState.putInt("theme_checked_id", rgTheme.getCheckedRadioButtonId());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            if (switchNotifications != null) {
                switchNotifications.setChecked(savedInstanceState.getBoolean("notifications_enabled", false));
            }
            if (rgTheme != null && savedInstanceState.containsKey("theme_checked_id")) {
                int checkedId = savedInstanceState.getInt("theme_checked_id");
                rgTheme.check(checkedId);
            }
        }
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
                        Log.d("SettingsFragment", "User accountType: " + (accountType != null ? accountType : "null"));
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
                    btnBecomeOrganizer.setVisibility(View.VISIBLE);
                    btnBecomeOrganizer.setText("Стать организатором");
                });
    }

    private void setupThemeSettings() {
        PreferencesManager preferencesManager = new PreferencesManager(requireContext());
        int currentTheme = preferencesManager.getThemeMode();

        switch (currentTheme) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                rbLight.setChecked(true);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                rbDark.setChecked(true);
                break;
            default:
                currentTheme = AppCompatDelegate.getDefaultNightMode();
                if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) {
                    rbLight.setChecked(true);
                } else {
                    rbDark.setChecked(true);
                }
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

            // Обновляем тему в Firestore и применяем через MainActivity
            if (currentUserId != null && isAdded() && !requireActivity().isFinishing() && !requireActivity().isDestroyed()) {
                ((MainActivity) requireActivity()).toggleTheme(); // Вызываем метод активности
            } else {
                AppCompatDelegate.setDefaultNightMode(mode); // Fallback для неавторизованных
                if (isAdded() && !requireActivity().isFinishing() && !requireActivity().isDestroyed()) {
                    new Handler().postDelayed(() -> requireActivity().recreate(), 500);
                }
            }
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

        db.collection("Profiles").document(user.getUid())
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