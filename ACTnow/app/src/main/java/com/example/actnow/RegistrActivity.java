package com.example.actnow;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.actnow.Models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegistrActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference collectionReference;

    private EditText et_register_username, et_register_email, et_register_passwd,
            et_register_city, et_register_birth_date;
    private Button btn_register;
    private TextView tv_go_auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registr);

        // Настройка прозрачного статус-бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        initViews();
        setupFirebase();
        checkCurrentUser();
        setupListeners();
    }

    private void initViews() {
        et_register_username = findViewById(R.id.username_et);
        et_register_email = findViewById(R.id.email_et);
        et_register_passwd = findViewById(R.id.password_et);
        et_register_city = findViewById(R.id.city_et);
        et_register_birth_date = findViewById(R.id.birth_date_et);
        btn_register = findViewById(R.id.reg_btn);
        tv_go_auth = findViewById(R.id.go_auth);
    }

    private void setupFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("Profiles");
    }

    private void checkCurrentUser() {
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void setupListeners() {
        tv_go_auth.setOnClickListener(v -> {
            startActivity(new Intent(this, AuthorizActivity.class));
        });

        btn_register.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = et_register_username.getText().toString().trim();
        String email = et_register_email.getText().toString().trim();
        String password = et_register_passwd.getText().toString().trim();
        String city = et_register_city.getText().toString().trim();
        String birthDateStr = et_register_birth_date.getText().toString().trim();

        if (!validateInputs(username, email, password, city, birthDateStr)) {
            return;
        }

        Timestamp birthDate = validateAndParseDate(birthDateStr);
        if (birthDate == null) {
            return;
        }

        if (!validateAge(birthDate)) {
            return;
        }

        createUserInFirebase(email, password, username, city, birthDate);
    }

    private boolean validateInputs(String username, String email, String password, String city, String birthDate) {
        if (username.isEmpty()) {
            et_register_username.setError("Введите имя пользователя");
            et_register_username.requestFocus();
            return false;
        }
        if (email.isEmpty()) {
            et_register_email.setError("Введите Email");
            et_register_email.requestFocus();
            return false;
        }
        if (password.isEmpty()) {
            et_register_passwd.setError("Введите пароль");
            et_register_passwd.requestFocus();
            return false;
        }
        if (city.isEmpty()) {
            et_register_city.setError("Введите город");
            et_register_city.requestFocus();
            return false;
        }
        if (birthDate.isEmpty()) {
            et_register_birth_date.setError("Введите дату рождения");
            et_register_birth_date.requestFocus();
            return false;
        }
        return true;
    }

    private Timestamp validateAndParseDate(String birthDateStr) {
        try {
            String[] dateParts = birthDateStr.split("\\.");
            if (dateParts.length != 3) {
                throw new ParseException("Неверный формат даты", 0);
            }

            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);

            if (day < 1 || day > 31) {
                showDateError("День должен быть от 1 до 31");
                return null;
            }
            if (month < 1 || month > 12) {
                showDateError("Месяц должен быть от 1 до 12");
                return null;
            }
            if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                showDateError("Год должен быть от 1900 до " + Calendar.getInstance().get(Calendar.YEAR));
                return null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            sdf.setLenient(false);
            Date parsedDate = sdf.parse(birthDateStr);
            if (parsedDate == null) {
                throw new ParseException("Неверная дата", 0);
            }
            return new Timestamp(parsedDate);
        } catch (NumberFormatException e) {
            showDateError("Дата должна содержать только числа");
            return null;
        } catch (ParseException e) {
            showDateError("Неверная дата (например, 30 февраля не существует)");
            return null;
        }
    }

    private void showDateError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        et_register_birth_date.requestFocus();
    }

    private boolean validateAge(Timestamp birthDate) {
        Calendar birthCal = Calendar.getInstance();
        birthCal.setTime(birthDate.toDate());
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        if (age < 15) {
            Toast.makeText(this, "Регистрация доступна только для пользователей старше 15 лет", Toast.LENGTH_SHORT).show();
            et_register_birth_date.requestFocus();
            return false;
        }
        return true;
    }

    private void createUserInFirebase(String email, String password, String username, String city, Timestamp birthDate) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserProfile(username, email, city, birthDate);
                    } else {
                        handleRegistrationError(task.getException().getMessage());
                    }
                });
    }

    private void saveUserProfile(String username, String email, String city, Timestamp birthDate) {
        String userId = mAuth.getCurrentUser().getUid();
        UserProfile userProfile = new UserProfile(userId, email, username, "volunteer");
        userProfile.setCity(city);
        userProfile.setBirthDate(birthDate);
        userProfile.setCreatedAt(Timestamp.now());
        userProfile.setUpdatedAt(Timestamp.now());

        collectionReference.document(userId).set(userProfile.toMap())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Ошибка Firestore: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleRegistrationError(String errorMessage) {
        if (errorMessage.contains("The email address is already in use")) {
            Toast.makeText(this, "Этот email уже зарегистрирован.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}