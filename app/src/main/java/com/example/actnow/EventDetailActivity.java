package com.example.actnow;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import com.example.actnow.Fragments.EventDetailFragment;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // Настройка прозрачного статус-бара
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        // Получаем данные из Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (savedInstanceState == null) {
                EventDetailFragment eventDetailFragment = new EventDetailFragment();
                eventDetailFragment.setArguments(extras); // Передаём Bundle напрямую
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, eventDetailFragment)
                        .commit();
            }
        }
    }
}