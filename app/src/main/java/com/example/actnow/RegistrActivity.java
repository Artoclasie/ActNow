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

import com.example.actnow.Models.ProfileModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseFirestore firebaseFirestore;
    CollectionReference collectionReference;

    EditText et_register_username, et_register_email, et_register_passwd, et_register_confpasswd, et_register_city;
    Button btn_register;
    TextView tv_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registr);

        initvar();
        checklogin();

        // Настройка прозрачного статус-бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // Переход на экран авторизации
        tv_register.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrActivity.this, AuthorizActivity.class);
            startActivity(intent);
        });

        // Регистрация нового пользователя
        btn_register.setOnClickListener(v -> {
            String username = et_register_username.getText().toString().trim();
            String email = et_register_email.getText().toString().trim();
            String password = et_register_passwd.getText().toString().trim();
            String confirmPassword = et_register_confpasswd.getText().toString().trim();
            String city = et_register_city.getText().toString().trim();

            if (username.isEmpty()) {
                et_register_username.setError("Введите имя пользователя");
                et_register_username.requestFocus();
            } else if (email.isEmpty()) {
                et_register_email.setError("Введите Email");
                et_register_email.requestFocus();
            } else if (password.isEmpty()) {
                et_register_passwd.setError("Введите пароль");
                et_register_passwd.requestFocus();
            } else if (confirmPassword.isEmpty()) {
                et_register_confpasswd.setError("Подтвердите пароль");
                et_register_confpasswd.requestFocus();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(RegistrActivity.this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String userId = mAuth.getCurrentUser().getUid();
                                Map<String, Boolean> emptySubscriptions = new HashMap<>();

                                ProfileModel model = new ProfileModel(
                                        userId,
                                        username,
                                        email,
                                        password,
                                        city,
                                        0L,
                                       0L,
                                        0L,
                                        emptySubscriptions
                                );

                                collectionReference.document(userId).set(model)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                Toast.makeText(RegistrActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegistrActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(RegistrActivity.this, "Ошибка Firestore: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                String errorMessage = task.getException().getMessage();
                                if (errorMessage.contains("The email address is already in use")) {
                                    Toast.makeText(RegistrActivity.this, "Этот email уже зарегистрирован.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RegistrActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void initvar() {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore.collection("Profiles");

        et_register_username = findViewById(R.id.userN_et);
        et_register_email = findViewById(R.id.email_et);
        et_register_passwd = findViewById(R.id.password_et);
        et_register_confpasswd = findViewById(R.id.password_et);
        et_register_city = findViewById(R.id.city_et);

        btn_register = findViewById(R.id.reg_btn);
        tv_register = findViewById(R.id.go_avt);
    }

    private void checklogin() {
        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(RegistrActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

