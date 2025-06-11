package com.example.actnow.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BecomeOrganizerFragment extends Fragment {
    private RadioGroup rgOrganizerType;
    private RadioButton rbIndividual, rbLegal;
    private LinearLayout llLegalFields, llIndividualFields;
    private EditText etCompanyName, etRegNumber, etFirstName, etLastName, etCity, etEmail, etPassword, etLegalCity;
    private Button btnSubmit, btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_become_organizer, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        rgOrganizerType = view.findViewById(R.id.rg_organizer_type);
        rbIndividual = view.findViewById(R.id.rb_individual);
        rbLegal = view.findViewById(R.id.rb_legal);
        llLegalFields = view.findViewById(R.id.ll_legal_fields);
        llIndividualFields = view.findViewById(R.id.ll_individual_fields);
        etCompanyName = view.findViewById(R.id.et_company_name);
        etRegNumber = view.findViewById(R.id.et_reg_number);
        etFirstName = view.findViewById(R.id.et_first_name);
        etLastName = view.findViewById(R.id.et_last_name);
        etCity = view.findViewById(R.id.et_city);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        etLegalCity = view.findViewById(R.id.et_legal_city);
        btnSubmit = view.findViewById(R.id.btn_submit);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void setupListeners() {
        rgOrganizerType.setOnCheckedChangeListener((group, checkedId) -> {
            llLegalFields.setVisibility(checkedId == R.id.rb_legal ? View.VISIBLE : View.GONE);
            llIndividualFields.setVisibility(checkedId == R.id.rb_individual ? View.VISIBLE : View.GONE);
        });

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnSubmit.setOnClickListener(v -> submitOrganizationRequest());
    }

    private void submitOrganizationRequest() {
        String accountType = rbIndividual.isChecked() ? "individual_organizer" : "legal_entity";

        if (accountType.equals("legal_entity")) {
            String companyName = etCompanyName.getText().toString().trim();
            String regNumber = etRegNumber.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String city = etLegalCity.getText().toString().trim();

            if (companyName.isEmpty() || regNumber.isEmpty() || email.isEmpty() || password.isEmpty() || city.isEmpty()) {
                Toast.makeText(getContext(), "Заполните все обязательные поля для юр. лица", Toast.LENGTH_SHORT).show();
                return;
            }

            if (regNumber.length() < 5) {
                Toast.makeText(getContext(), "Рег. номер должен содержать минимум 5 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(getContext(), "Введите действительный email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(getContext(), "Пароль должен содержать минимум 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            if (city.length() < 2) {
                Toast.makeText(getContext(), "Город должен содержать минимум 2 символа", Toast.LENGTH_SHORT).show();
                return;
            }

            createLegalEntityAccount(email, password, companyName, regNumber, city);
        } else if (accountType.equals("individual_organizer")) {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String city = etCity.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || city.isEmpty()) {
                Toast.makeText(getContext(), "Заполните обязательные поля: имя, фамилию и город", Toast.LENGTH_SHORT).show();
                return;
            }

            if (firstName.length() < 2 || lastName.length() < 2) {
                Toast.makeText(getContext(), "Имя и фамилия должны содержать минимум 2 символа", Toast.LENGTH_SHORT).show();
                return;
            }

            if (city.length() < 2) {
                Toast.makeText(getContext(), "Город должен содержать минимум 2 символа", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUserProfile(accountType, firstName, lastName, city);
        }
    }

    private void createLegalEntityAccount(String email, String password, String companyName, String regNumber, String city) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String newUserId = mAuth.getCurrentUser().getUid();
                        UserProfile userProfile = new UserProfile(newUserId, email, companyName, "legal_entity");
                        userProfile.setRole("organizer");
                        userProfile.setCity(city); // Сохраняем город в поле city

                        db.collection("Profiles").document(newUserId)
                                .set(userProfile.toMap())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Юридическое лицо успешно зарегистрировано", Toast.LENGTH_SHORT).show();
                                    navigateToProfile();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Ошибка создания профиля: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    mAuth.getCurrentUser().delete();
                                });
                    } else {
                        Toast.makeText(getContext(), "Ошибка создания аккаунта: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUserProfile(String accountType, String firstName, String lastName, String city) {
        String userId = mAuth.getCurrentUser().getUid();

        UserProfile userProfile = new UserProfile();
        userProfile.setAccountType(accountType);
        userProfile.setRole("individual_organizer");
        userProfile.setName(firstName + " " + lastName);
        userProfile.setCity(city);

        db.collection("Profiles").document(userId)
                .update(
                        "accountType", accountType,
                        "role", "individual_organizer",
                        "username", userProfile.getName(),
                        "city", userProfile.getCity()
                )
                .addOnSuccessListener(aVoid -> showSuccessMessage(accountType))
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Ошибка обновления профиля: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessMessage(String accountType) {
        Toast.makeText(getContext(),
                accountType.equals("legal_entity") ?
                        "Юридическое лицо успешно зарегистрировано" :
                        "Вы успешно зарегистрированы как физическое лицо",
                Toast.LENGTH_SHORT).show();

        if (!accountType.equals("legal_entity") && getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    private void navigateToProfile() {
        if (isAdded() && getActivity() != null) {
            ProfileFragment profileFragment = new ProfileFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, profileFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}