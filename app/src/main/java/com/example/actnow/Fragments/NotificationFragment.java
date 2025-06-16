package com.example.actnow.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.actnow.Adapters.NotificationAdapter;
import com.example.actnow.Models.NotificationModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationListener {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<NotificationModel> notifications = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        rvNotifications = view.findViewById(R.id.rv_notifications);
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new NotificationAdapter(getContext(), notifications, this);
        rvNotifications.setAdapter(adapter);

        checkAndSendWelcomeNotification();
        loadNotifications();
    }

    private void checkAndSendWelcomeNotification() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("Profiles").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Проверяем, является ли пользователь новым (по полю firstLogin)
                            Boolean firstLogin = document.getBoolean("firstLogin");
                            if (firstLogin != null && firstLogin) {
                                // Отправляем приветственное уведомление
                                sendWelcomeNotification();

                                // Обновляем флаг, чтобы уведомление не отправлялось повторно
                                db.collection("Profiles").document(userId)
                                        .update("firstLogin", false);
                            }
                        }
                    }
                });
    }

    private void sendWelcomeNotification() {
        String welcomeMessage = "Мы рады видеть вас в нашем приложении! Начните исследовать мероприятия вокруг вас.";

        NotificationModel welcomeNotification = new NotificationModel(
                "welcome_" + System.currentTimeMillis(),
                "system",
                "system",
                "Добро пожаловать!",
                welcomeMessage
        );

        // Добавляем в локальный список
        notifications.add(0, welcomeNotification);
        adapter.notifyItemInserted(0);

        // Сохраняем в Firebase (если нужно хранить историю уведомлений)
        saveNotificationToFirebase(welcomeNotification);
    }

    private void saveNotificationToFirebase(NotificationModel notification) {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();
        db.collection("Profiles").document(userId)
                .collection("notifications")
                .document(notification.getNotificationId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            db.collection("Profiles").document(userId)
                                    .collection("notifications")
                                    .document(notification.getNotificationId())
                                    .set(notification.toMap())
                                    .addOnSuccessListener(aVoid -> {
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        }
                    }
                });
    }

    private void loadNotifications() {
        if (auth.getCurrentUser() == null) return;

        String userId = auth.getCurrentUser().getUid();

        db.collection("Profiles").document(userId)
                .collection("notifications")
                .orderBy("createdAt")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notifications.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            NotificationModel notification = document.toObject(NotificationModel.class);
                            if (notification != null) {
                                notifications.add(0, notification);
                            }
                        }

                        if (notifications.isEmpty()) {
                            sendWelcomeNotification();
                        } else {
                            adapter.notifyDataSetChanged();
                        }
                    } else {

                        loadSampleNotifications();
                    }
                });
    }

    private void loadSampleNotifications() {
        notifications.clear();

        NotificationModel notification1 = new NotificationModel(
                "1",
                "user1",
                "event_update",
                "Vinyasa yoga flow and breathwork (English) by the sea",
                "just announced Vinyasa Yoga and Breath..."
        );
        notification1.setCreatedAt(new Timestamp(new Date(System.currentTimeMillis() - 5 * 60 * 60 * 1000)));
        notifications.add(notification1);

        NotificationModel notification2 = new NotificationModel(
                "2",
                "user1",
                "event_update",
                "Vinyasa yoga flow and breathwork (English) by the sea",
                "just announced Tone & Flow: Vinyasa & Br..."
        );
        notification2.setCreatedAt(new Timestamp(new Date(System.currentTimeMillis() - 18 * 60 * 60 * 1000)));
        notifications.add(notification2);

        NotificationModel notification3 = new NotificationModel(
                "3",
                "user1",
                "event_update",
                "Vinyasa yoga flow and breathwork (English) by the sea",
                "just announced Sunrise Sessions: Vinyas..."
        );
        notification3.setCreatedAt(new Timestamp(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)));
        notifications.add(notification3);

        NotificationModel notification4 = new NotificationModel(
                "4",
                "user1",
                "event_update",
                "Vinyasa yoga flow and breathwork (English) by the sea",
                "just announced Breathe to balance: Begi..."
        );
        notification4.setCreatedAt(new Timestamp(new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)));
        notifications.add(notification4);

        NotificationModel notification5 = new NotificationModel(
                "5",
                "user1",
                "discussion",
                "A discussion started in Vinyasa yoga flow and breathwork (English)",
                ""
        );
        notification5.setCreatedAt(new Timestamp(new Date(System.currentTimeMillis() - 48 * 60 * 60 * 1000)));
        notifications.add(notification5);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onNotificationClick(NotificationModel notification) {
        if ("new_event".equals(notification.getType()) || "event_update".equals(notification.getType())) {
            String eventId = notification.getData() != null ? notification.getData().get("eventId") : null;
            if (eventId != null) {
                Bundle args = new Bundle();
                args.putString("eventId", eventId);
                EventDetailFragment fragment = new EventDetailFragment();
                fragment.setArguments(args);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    @Override
    public void onNotificationLongClick(NotificationModel notification, int position) {
        adapter.showDeleteButton(position);
    }

    @Override
    public void onDeleteClick(NotificationModel notification, int position) {
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            db.collection("Profiles").document(userId)
                    .collection("notifications")
                    .document(notification.getNotificationId())
                    .delete();
        }

        adapter.removeNotification(position);
    }
}