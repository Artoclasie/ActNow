package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.actnow.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportFragment extends Fragment {
    private static final String ARG_USER_ID = "userId";
    private static final String TAG = "ReportFragment";
    private String userId;
    private TextView tvReportDate;
    private ImageView star1, star2, star3, star4, star5;
    private LinearLayout llStars;
    private FirebaseFirestore db;

    public static ReportFragment newInstance(String userId) {
        ReportFragment fragment = new ReportFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            Log.d(TAG, "Received userId: " + userId);
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                View cardView = view.findViewById(R.id.cv_report);
                if (cardView != null) {
                    int[] location = new int[2];
                    cardView.getLocationOnScreen(location);
                    int cardX = location[0];
                    int cardY = location[1];
                    int cardWidth = cardView.getWidth();
                    int cardHeight = cardView.getHeight();

                    if (x < cardX || x > cardX + cardWidth || y < cardY || y > cardY + cardHeight) {
                        getParentFragmentManager().popBackStack();
                        return true;
                    }
                }
            }
            return false;
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация View
        tvReportDate = view.findViewById(R.id.tv_report_date);
        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);
        llStars = view.findViewById(R.id.ll_stars);

        // Установка текущей даты
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
        String currentDate = "Дата: " + dateFormat.format(new Date()); // Сегодняшняя дата
        if (tvReportDate != null) {
            tvReportDate.setText(currentDate);
        }

        if (userId != null) {
            loadUserProfileName();
        } else {
            Log.e(TAG, "userId is null");
            hideStars();
        }
    }

    private void loadUserProfileName() {
        db.collection("Profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getView() == null) return;
                    if (documentSnapshot.exists()) {
                        String accountType = documentSnapshot.getString("accountType");
                        if (accountType != null && ("individual_organizer".equals(accountType) || "legal_entity".equals(accountType))) {
                            loadAverageRating();
                        } else {
                            hideStars();
                        }
                    } else {
                        Log.w(TAG, "Profile document does not exist for userId: " + userId);
                        hideStars();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading profile name for userId: " + userId, e);
                    hideStars();
                });
    }

    private void hideStars() {
        if (llStars != null) {
            llStars.setVisibility(View.GONE);
        }
    }

    private void loadAverageRating() {
        db.collection("Reviews")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded() || getView() == null) return;
                    if (!querySnapshot.isEmpty()) {
                        float totalRating = 0;
                        int count = 0;
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            Double rating = doc.getDouble("rating");
                            if (rating != null) {
                                totalRating += rating.floatValue();
                                count++;
                            }
                        }
                        float averageRating = count > 0 ? totalRating / count : 0;
                        setStarRating(averageRating);
                    } else {
                        setStarRating(0);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading average rating for userId: " + userId, e);
                    setStarRating(0);
                });
    }

    private void setStarRating(float rating) {
        if (star1 == null || star2 == null || star3 == null || star4 == null || star5 == null) return;
        int stars = (int) Math.floor(rating);
        boolean hasHalfStar = rating - stars >= 0.5f;

        star1.setImageResource(stars >= 1 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star2.setImageResource(stars >= 2 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star3.setImageResource(stars >= 3 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star4.setImageResource(stars >= 4 ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
        star5.setImageResource(stars >= 5 ? R.drawable.ic_star_filled : (hasHalfStar && stars == 4 ? R.drawable.ic_star_half : R.drawable.ic_star_outline));
    }
}