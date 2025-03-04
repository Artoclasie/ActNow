package com.example.actnow;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация Cloudinary один раз
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dolnejlw3");
        config.put("api_key", "869237653654212");
        config.put("api_secret", "E1GjuqHeNAaEFwXIsH6BM9KRnTA");
        MediaManager.init(this, config);
    }
}