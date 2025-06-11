package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {
    private static final String ARG_USER_ID = "userId";
    private static final String TAG = "ReportFragment";
    private String userId;
    private TextView tvReportDate, tvReportSummary, tvPostsCount, tvLikesCount, tvCommentsCount, tvActivityDetails, tvAccountType, tvAvatarUrl;
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
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Инициализация View
        tvReportDate = view.findViewById(R.id.tv_report_date);
        tvReportSummary = view.findViewById(R.id.tv_report_summary);
        tvPostsCount = view.findViewById(R.id.tv_posts_count);
        tvLikesCount = view.findViewById(R.id.tv_likes_count);
        tvCommentsCount = view.findViewById(R.id.tv_comments_count);
        tvActivityDetails = view.findViewById(R.id.tv_activity_details);
        tvAccountType = view.findViewById(R.id.tv_account_type);
        tvAvatarUrl = view.findViewById(R.id.tv_avatar_url);
        star1 = view.findViewById(R.id.star1);
        star2 = view.findViewById(R.id.star2);
        star3 = view.findViewById(R.id.star3);
        star4 = view.findViewById(R.id.star4);
        star5 = view.findViewById(R.id.star5);
        llStars = view.findViewById(R.id.ll_stars);

        // Установка текущей даты
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
        String currentDate = "Дата: " + dateFormat.format(new Date());
        if (tvReportDate != null) {
            tvReportDate.setText(currentDate);
        }

        if (userId != null) {
            loadUserProfileName();
        } else {
            Log.e(TAG, "userId is null");
            if (tvReportSummary != null) tvReportSummary.setText("Ошибка: ID пользователя отсутствует");
            if (tvPostsCount != null) tvPostsCount.setText("N/A");
            if (tvLikesCount != null) tvLikesCount.setText("N/A");
            if (tvCommentsCount != null) tvCommentsCount.setText("N/A");
            if (tvActivityDetails != null) tvActivityDetails.setText("Детали активности: отсутствуют");
            if (tvAccountType != null) tvAccountType.setText("N/A");
            if (tvAvatarUrl != null) tvAvatarUrl.setText("N/A");
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
                        String name = documentSnapshot.getString("username");
                        String accountType = documentSnapshot.getString("accountType");
                        String avatarUrl = documentSnapshot.getString("profileImage");
                        if (tvReportSummary != null) {
                            tvReportSummary.setText("Общая статистика пользователя (" + (name != null ? name : userId) + "):");
                        }
                        if (tvAccountType != null) {
                            tvAccountType.setText(accountType != null ? accountType : "N/A");
                        }
                        if (tvAvatarUrl != null) {
                            tvAvatarUrl.setText(avatarUrl != null ? avatarUrl : "null");
                        }
                        if (accountType != null && ("individual_organizer".equals(accountType) || "legal_entity".equals(accountType))) {
                            loadAverageRating();
                        } else {
                            hideStars();
                        }
                        loadUserStats();
                    } else {
                        Log.w(TAG, "Profile document does not exist for userId: " + userId);
                        if (tvReportSummary != null) {
                            tvReportSummary.setText("Общая статистика пользователя (ID: " + userId + "):");
                        }
                        if (tvAccountType != null) tvAccountType.setText("N/A");
                        if (tvAvatarUrl != null) tvAvatarUrl.setText("null");
                        hideStars();
                        loadUserStats();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading profile name for userId: " + userId, e);
                    if (tvReportSummary != null) {
                        tvReportSummary.setText("Общая статистика пользователя (ID: " + userId + "):");
                    }
                    if (tvAccountType != null) tvAccountType.setText("N/A");
                    if (tvAvatarUrl != null) tvAvatarUrl.setText("null");
                    hideStars();
                    loadUserStats();
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

    private void loadUserStats() {
        if (tvPostsCount != null) tvPostsCount.setText("Загрузка...");
        if (tvLikesCount != null) tvLikesCount.setText("Загрузка...");
        if (tvCommentsCount != null) tvCommentsCount.setText("Загрузка...");
        if (tvActivityDetails != null) {
            tvActivityDetails.setText("Детали активности:\n- Зарегистрирован: Загрузка...\n- Последний пост: Загрузка...");
        }

        db.collection("events")
                .whereEqualTo("authorId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded() || getView() == null) return;
                    int postsCount = queryDocumentSnapshots.size();
                    long totalLikes = 0;
                    long latestPostTimestamp = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Long likes = document.getLong("likesCount");
                        if (likes != null) {
                            totalLikes += likes;
                        }
                        Long createdAt = document.getLong("createdAt");
                        if (createdAt != null && createdAt > latestPostTimestamp) {
                            latestPostTimestamp = createdAt;
                        }
                    }

                    if (tvPostsCount != null) tvPostsCount.setText(String.valueOf(postsCount));
                    if (tvLikesCount != null) tvLikesCount.setText(String.valueOf(totalLikes));

                    String lastPostDate = latestPostTimestamp > 0
                            ? new SimpleDateFormat("dd MMMM yyyy", new Locale("ru")).format(new Date(latestPostTimestamp))
                            : "Нет постов";
                    updateActivityDetails(lastPostDate);

                    List<Task<QuerySnapshot>> commentTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot post : queryDocumentSnapshots) {
                        Task<QuerySnapshot> commentTask = db.collection("events")
                                .document(post.getId())
                                .collection("Comments")
                                .get();
                        commentTasks.add(commentTask);
                    }

                    Tasks.whenAllSuccess(commentTasks)
                            .addOnSuccessListener(results -> {
                                if (!isAdded() || getView() == null) return;
                                int totalComments = 0;
                                for (Object result : results) {
                                    QuerySnapshot commentSnapshot = (QuerySnapshot) result;
                                    totalComments += commentSnapshot.size();
                                }
                                if (tvCommentsCount != null) {
                                    tvCommentsCount.setText(String.valueOf(totalComments));
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                if (tvCommentsCount != null) tvCommentsCount.setText("Ошибка");
                                Log.e(TAG, "Error loading comments for userId: " + userId, e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getView() == null) return;
                    if (tvPostsCount != null) tvPostsCount.setText("Ошибка");
                    if (tvLikesCount != null) tvLikesCount.setText("Ошибка");
                    if (tvCommentsCount != null) tvCommentsCount.setText("Ошибка");
                    Log.e(TAG, "Error loading user stats for userId: " + userId, e);
                });

        db.collection("Profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getView() == null) return;
                    String registrationDate = documentSnapshot.getString("registrationDate");
                    if (registrationDate != null) {
                        updateActivityDetails(null);
                    } else {
                        if (tvActivityDetails != null) {
                            tvActivityDetails.setText("Детали активности:\n- Зарегистрирован: Неизвестно\n- Последний пост: -");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getView() == null) return;
                    if (tvActivityDetails != null) {
                        tvActivityDetails.setText("Детали активности:\n- Зарегистрирован: Ошибка\n- Последний пост: -");
                    }
                    Log.e(TAG, "Error loading registration date for userId: " + userId, e);
                });
    }

    private void updateActivityDetails(String lastPostDate) {
        if (!isAdded() || getView() == null || tvActivityDetails == null) return;

        // Определяем finalLastPostDate
        String finalLastPostDate;
        if (lastPostDate != null) {
            finalLastPostDate = lastPostDate;
        } else {
            String currentText = tvActivityDetails.getText().toString();
            if (currentText.contains("Последний пост: ")) {
                String[] parts = currentText.split("Последний пост: ");
                finalLastPostDate = parts.length > 1 ? parts[1].trim() : "-";
            } else {
                finalLastPostDate = "-";
            }
        }

        db.collection("Profiles")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getView() == null || tvActivityDetails == null) return;
                    String registrationDate = documentSnapshot.getString("registrationDate");
                    tvActivityDetails.setText(String.format("Детали активности:\n- Зарегистрирован: %s\n- Последний пост: %s",
                            registrationDate != null ? registrationDate : "Неизвестно",
                            finalLastPostDate));
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getView() == null || tvActivityDetails == null) return;
                    tvActivityDetails.setText(String.format("Детали активности:\n- Зарегистрирован: Ошибка\n- Последний пост: %s", finalLastPostDate));
                    Log.e(TAG, "Error updating activity details for userId: " + userId, e);
                });
    }
}