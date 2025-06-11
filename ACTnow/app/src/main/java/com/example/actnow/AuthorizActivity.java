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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthorizActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEt, passwordEt;
    private Button autBtn;
    private TextView goReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authoriz);

        // Инициализация Firebase
        mAuth = FirebaseAuth.getInstance();

        // Настройка прозрачного статус-бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailEt = findViewById(R.id.email_et);
        passwordEt = findViewById(R.id.password_et);
        autBtn = findViewById(R.id.aut_btn);
        goReg = findViewById(R.id.go_reg);
    }

    private void setupListeners() {
        autBtn.setOnClickListener(v -> attemptLogin());
        goReg.setOnClickListener(v -> navigateToRegistration());
    }

    private void attemptLogin() {
        String email = emailEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        authenticateUser(email, password);
    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            emailEt.setError("Введите email");
            emailEt.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            emailEt.setError("Некорректный формат email");
            emailEt.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            passwordEt.setError("Введите пароль");
            passwordEt.requestFocus();
            return false;
        }

        if (password.length() < 5) {
            passwordEt.setError("Пароль должен содержать минимум 5 символов");
            passwordEt.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }

    private void authenticateUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                navigateToMain();
                            }
                        } else {
                            Toast.makeText(AuthorizActivity.this,
                                    "Ошибка авторизации: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(AuthorizActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegistration() {
        startActivity(new Intent(AuthorizActivity.this, RegistrActivity.class));
    }
}