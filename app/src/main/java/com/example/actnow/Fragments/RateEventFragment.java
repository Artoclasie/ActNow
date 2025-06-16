package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.actnow.Models.ReviewModel;
import com.example.actnow.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;

public class RateEventFragment extends Fragment {

    private static final String TAG = "RateEventFragment";
    private static final String ARG_EVENT_ID = "eventId";
    private static final String ARG_ORGANIZER_ID = "organizerId";

    private String eventId;
    private String organizerId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvEventTitle;
    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmit;

    public static RateEventFragment newInstance(String eventId, String organizerId) {
        RateEventFragment fragment = new RateEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ORGANIZER_ID, organizerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            organizerId = getArguments().getString(ARG_ORGANIZER_ID);
            Log.d(TAG, "onCreate: Received eventId = " + eventId + ", organizerId = " + organizerId);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return null;
        }

        db.collection("Reviews")
                .whereEqualTo("reviewerId", currentUserId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        Toast.makeText(getContext(), "Вы уже оставили отзыв на это мероприятие", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    } else {

                        initializeView(inflater, container);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка проверки отзыва", e);
                    Toast.makeText(getContext(), "Ошибка проверки отзыва", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });

        return null;
    }

    private void initializeView(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_rate_event, container, false);

        tvEventTitle = view.findViewById(R.id.tv_event_title);
        ratingBar = view.findViewById(R.id.rating_bar);
        etReview = view.findViewById(R.id.et_review);
        btnSubmit = view.findViewById(R.id.btn_submit);

        ratingBar.setStepSize(1.0f);

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        tvEventTitle.setText(title != null ? "Мероприятие окончено! " + title : "Мероприятие");
                    }
                });

        btnSubmit.setOnClickListener(v -> submitRating());

        container.addView(view);
    }

    private void submitRating() {
        String currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show();
            return;
        }

        float rating = ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(getContext(), "Пожалуйста, укажите рейтинг", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = etReview.getText().toString().trim();
        ReviewModel review = new ReviewModel();
        review.setUserId(organizerId);
        review.setReviewerId(currentUserId);
        review.setEventId(eventId);
        review.setRating(rating);
        review.setContent(reviewText.isEmpty() ? null : reviewText);
        review.setCreatedAt(Timestamp.now());

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        review.setEventTitle(title != null ? title : "Без названия");
                    } else {
                        review.setEventTitle("Мероприятие не найдено");
                    }

                    // Сохраняем отзыв
                    db.collection("Reviews")
                            .add(review)
                            .addOnSuccessListener(documentReference -> {
                                updateAverageRating(organizerId, eventId, rating);
                                Toast.makeText(getContext(), "Отзыв отправлен", Toast.LENGTH_SHORT).show();
                                getParentFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Ошибка отправки отзыва", e);
                                Toast.makeText(getContext(), "Ошибка отправки отзыва", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка получения названия мероприятия", e);
                    Toast.makeText(getContext(), "Ошибка получения данных мероприятия", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAverageRating(String organizerId, String eventId, float newRating) {
        db.collection("Reviews")
                .whereEqualTo("userId", organizerId)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRating = newRating;
                    int reviewCount = 1;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ReviewModel review = doc.toObject(ReviewModel.class);
                        totalRating += review.getRating();
                        reviewCount++;
                    }
                    double averageRating = reviewCount > 0 ? totalRating / reviewCount : newRating;

                    db.collection("events").document(eventId)
                            .update("averageRating", averageRating);

                    db.collection("Profiles").document(organizerId)
                            .update("stats.rating", averageRating);
                });
    }
}