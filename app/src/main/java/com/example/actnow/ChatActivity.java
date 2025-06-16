package com.example.actnow;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.example.actnow.Fragments.ChatFragment;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Получаем userId из интента
        String userId = getIntent().getStringExtra("userId");
        if (userId == null || userId.isEmpty()) {
            finish(); // Закрываем активити, если userId отсутствует
            return;
        }

        // Инициализируем и добавляем ChatFragment
        ChatFragment chatFragment = ChatFragment.newInstance(userId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.chat_container, chatFragment);
        transaction.commit();

        // Настройка обработки нажатия кнопки "назад"
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Закрываем активити при нажатии "назад"
            }
        });
    }
}