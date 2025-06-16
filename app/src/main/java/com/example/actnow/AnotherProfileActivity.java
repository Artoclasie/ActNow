package com.example.actnow;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import com.example.actnow.Fragments.AnotherProfileFragment;
import com.example.actnow.Fragments.ChatFragment;

public class AnotherProfileActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_profile);

        // Настройка прозрачного статус-бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // Получаем userId из Intent
        String userId = getIntent().getStringExtra("uid"); // Используем "uid"
        boolean fromChatsFragment = getIntent().getBooleanExtra("fromChatsFragment", false);

        // Проверяем, что userId не null или пустой строкой
        if (userId != null && !userId.isEmpty()) {
            // Отображаем фрагмент
            if (savedInstanceState == null) {
                if (fromChatsFragment) {
                    // Если переход из ChatsFragment, открываем ChatFragment без добавления в BackStack
                    ChatFragment chatFragment = ChatFragment.newInstance(userId);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, chatFragment)
                            .commit();
                } else {
                    // Иначе открываем AnotherProfileFragment без добавления в BackStack
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, AnotherProfileFragment.newInstance(userId))
                            // .addToBackStack(null) // Убрали добавление в BackStack
                            .commit();
                }
            }
        } else {

            Log.e("AnotherProfileActivity", "userId is null or empty");
        }
    }

    @Override
    public void onBackPressed() {
        boolean fromChatsFragment = getIntent().getBooleanExtra("fromChatsFragment", false);
        if (fromChatsFragment) {
            finish();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                super.onBackPressed();
            }
        }
    }
}
