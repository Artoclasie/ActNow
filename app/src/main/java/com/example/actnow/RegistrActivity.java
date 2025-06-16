package com.example.actnow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.actnow.Models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.util.Calendar;

public class RegistrActivity extends AppCompatActivity {

    private EditText emailEt, usernameEt, passwordEt, cityEt, birthDateEt;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registr);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Находим все View элементы
        emailEt = findViewById(R.id.email_et);
        usernameEt = findViewById(R.id.username_et);
        passwordEt = findViewById(R.id.password_et);
        cityEt = findViewById(R.id.city_et);
        birthDateEt = findViewById(R.id.birth_date_et);
        Button regBtn = findViewById(R.id.reg_btn);
        TextView goAuth = findViewById(R.id.go_auth);

        // Добавляем обработчик для автоматической расстановки точек в дате рождения
        birthDateEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2 && before == 0) {
                    birthDateEt.setText(s + ".");
                    birthDateEt.setSelection(3);
                } else if (s.length() == 5 && before == 0) {
                    birthDateEt.setText(s + ".");
                    birthDateEt.setSelection(6);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Обработчик кнопки регистрации
        regBtn.setOnClickListener(v -> registerUser());

        // Обработчик перехода к авторизации
        goAuth.setOnClickListener(v -> {
            startActivity(new Intent(RegistrActivity.this, AuthorizActivity.class));
            finish();
        });

        // Устанавливаем прозрачный статус-бар для устройств с API 21+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void registerUser() {
        String email = emailEt.getText().toString().trim();
        String username = usernameEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();
        String city = cityEt.getText().toString().trim();
        String birthDateStr = birthDateEt.getText().toString().trim();

        // Валидация полей
        if (!validateInputs(email, username, password, city, birthDateStr)) {
            return;
        }

        // Проверка возраста (минимум 15 лет)
        if (!validateAge(birthDateStr)) {
            Toast.makeText(this, "Регистрация доступна только для пользователей старше 15 лет",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Проверка, что email не занят
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().getSignInMethods().isEmpty()) {
                            // Если email не занят, продолжаем регистрацию
                            createUserInFirebase(email, password, username, city, birthDateStr);
                        } else {
                            Toast.makeText(this, "Этот email уже зарегистрирован", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Ошибка проверки email", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String email, String username, String password,
                                   String city, String birthDate) {
        if (email.isEmpty()) {
            emailEt.setError("Введите Email");
            emailEt.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEt.setError("Введите корректный email");
            emailEt.requestFocus();
            return false;
        }

        if (username.isEmpty()) {
            usernameEt.setError("Введите имя пользователя");
            usernameEt.requestFocus();
            return false;
        }

        if (username.matches(".*\\d.*")) {
            usernameEt.setError("Имя пользователя не должно содержать цифры");
            usernameEt.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEt.setError("Введите пароль");
            passwordEt.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEt.setError("Пароль должен содержать минимум 6 символов");
            passwordEt.requestFocus();
            return false;
        }

        if (city.isEmpty()) {
            cityEt.setError("Введите город");
            cityEt.requestFocus();
            return false;
        }

        if (birthDate.isEmpty()) {
            birthDateEt.setError("Введите дату рождения");
            birthDateEt.requestFocus();
            return false;
        }

        if (!birthDate.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            birthDateEt.setError("Неверный формат даты (используйте ДД.ММ.ГГГГ)");
            birthDateEt.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateAge(String birthDateStr) {
        try {
            String[] parts = birthDateStr.split("\\.");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1; // Месяцы в Calendar начинаются с 0
            int year = Integer.parseInt(parts[2]);

            Calendar birthDate = Calendar.getInstance();
            birthDate.set(year, month, day);

            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

            if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age >= 15;
        } catch (Exception e) {
            return false;
        }
    }

    private void createUserInFirebase(String email, String password,
                                      String username, String city, String birthDate) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserProfile(firebaseUser.getUid(), email, username, city, birthDate);
                        }
                    } else {
                        Toast.makeText(this, "Ошибка регистрации: " +
                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserProfile(String userId, String email, String username,
                                 String city, String birthDateStr) {
        try {
            // Парсим дату рождения
            String[] parts = birthDateStr.split("\\.");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]) - 1;
            int year = Integer.parseInt(parts[2]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            Timestamp birthDate = new Timestamp(calendar.getTime());

            // Создаем профиль пользователя
            UserProfile userProfile = new UserProfile(userId, email, username, "volunteer");
            userProfile.setCity(city);
            userProfile.setBirthDate(birthDate);
            userProfile.setBio("");
            userProfile.setProfileImage("");
            userProfile.setBackgroundImageUrl("");
            userProfile.setThemeMode(AppCompatDelegate.MODE_NIGHT_NO);

            // Сохраняем в Firestore
            db.collection("Profiles").document(userId)
                    .set(userProfile.toMap())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Ошибка сохранения профиля", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка обработки даты рождения", Toast.LENGTH_SHORT).show();
        }
    }
}