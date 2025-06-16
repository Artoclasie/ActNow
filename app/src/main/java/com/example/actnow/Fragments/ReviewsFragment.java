package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.actnow.Adapters.ReviewAdapter;
import com.example.actnow.Models.ReviewModel;
import com.example.actnow.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReviewsFragment extends Fragment {
    private static final String TAG = "ReviewsFragment";

    private RecyclerView rvReviews;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvEmptyState;
    private TextView tvTitle;
    private ReviewAdapter reviewAdapter;
    private List<ReviewModel> reviewList = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String targetUserId;
    private String accountType;
    private Button btnLeft, btnRight;
    private View underlineIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            targetUserId = getArguments().getString("userId", mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "");
        } else {
            targetUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";
        }
        Log.d(TAG, "onCreate: targetUserId=" + targetUserId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReviews = view.findViewById(R.id.rv_reviews);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tvTitle = view.findViewById(R.id.tv_title);
        btnLeft = view.findViewById(R.id.btn_left);
        btnRight = view.findViewById(R.id.btn_right);
        underlineIndicator = view.findViewById(R.id.underline_indicator);

        if (rvReviews == null || swipeRefreshLayout == null || tvEmptyState == null || tvTitle == null || btnLeft == null || btnRight == null || underlineIndicator == null) {
            Log.e(TAG, "One or more views are null!");
            return;
        }

        tvTitle.setText("Отзывы");

        setupRecyclerView();
        setupSwipeRefresh();
        setupButtons();
        loadUserAccountType();
        loadReviews(true);
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(reviewList, requireContext(), getParentFragmentManager());
        rvReviews.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvReviews.setAdapter(reviewAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe refresh triggered");
            if (isAdded()) {
                reviewList.clear();
                loadReviews(btnLeft.isSelected());
            }
        });
    }

    private void setupButtons() {
        btnLeft.setText(getString(R.string.our_reviews));
        btnRight.setText(getString(R.string.reviews_about_us));
        btnLeft.setOnClickListener(v -> {
            btnLeft.setSelected(true);
            btnRight.setSelected(false);
            underlineIndicator.setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) underlineIndicator.getLayoutParams();
            params.startToStart = R.id.btn_left;
            params.endToEnd = R.id.btn_left;
            underlineIndicator.setLayoutParams(params);
            reviewList.clear();
            loadReviews(true);
        });
        btnRight.setOnClickListener(v -> {
            btnRight.setSelected(true);
            btnLeft.setSelected(false);
            underlineIndicator.setVisibility(View.VISIBLE);
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) underlineIndicator.getLayoutParams();
            params.startToStart = R.id.btn_right;
            params.endToEnd = R.id.btn_right;
            underlineIndicator.setLayoutParams(params);
            reviewList.clear();
            loadReviews(false);
        });

        btnLeft.setSelected(true);
        underlineIndicator.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams initialParams = (ConstraintLayout.LayoutParams) underlineIndicator.getLayoutParams();
        initialParams.startToStart = R.id.btn_left;
        initialParams.endToEnd = R.id.btn_left;
        underlineIndicator.setLayoutParams(initialParams);
    }

    private void loadUserAccountType() {
        db.collection("Profiles").document(targetUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        accountType = documentSnapshot.getString("accountType");
                        Log.d(TAG, "Account type for " + targetUserId + ": " + accountType);
                        if ("individual_organizer".equals(accountType)) {
                            btnLeft.setVisibility(View.VISIBLE);
                            btnRight.setVisibility(View.VISIBLE);
                            underlineIndicator.setVisibility(View.VISIBLE);
                        } else {
                            btnLeft.setVisibility(View.GONE);
                            btnRight.setVisibility(View.GONE);
                            underlineIndicator.setVisibility(View.GONE);
                        }
                    } else {
                        Log.w(TAG, "UserProfile not found for " + targetUserId);
                        btnLeft.setVisibility(View.GONE);
                        btnRight.setVisibility(View.GONE);
                        underlineIndicator.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading account type for " + targetUserId, e);
                    btnLeft.setVisibility(View.GONE);
                    btnRight.setVisibility(View.GONE);
                    underlineIndicator.setVisibility(View.GONE);
                });
    }

    private void loadReviews(boolean isLeft) {
        Log.d(TAG, "Starting loadReviews, isLeft: " + isLeft + ", targetUserId: " + targetUserId);
        swipeRefreshLayout.setRefreshing(true);
        db.collection("Reviews")
                .whereEqualTo(isLeft ? "reviewerId" : "userId", targetUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded()) return;
                    swipeRefreshLayout.setRefreshing(false);
                    if (task.isSuccessful()) {
                        reviewList.clear();
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ReviewModel review = document.toObject(ReviewModel.class);
                            review.setId(document.getId());
                            Task<DocumentSnapshot> eventTask = db.collection("events").document(review.getEventId())
                                    .get()
                                    .addOnSuccessListener(eventDoc -> {
                                        if (eventDoc.exists()) {
                                            review.setEventTitle(eventDoc.getString("title"));
                                        } else {
                                            review.setEventTitle("Мероприятие не найдено");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        review.setEventTitle("Ошибка загрузки мероприятия");
                                    });
                            tasks.add(eventTask);

                            reviewList.add(review);
                            Log.d(TAG, "Retrieved review: eventId=" + review.getEventId() + ", reviewId=" + review.getId());
                        }
                        Log.d(TAG, "Total reviews queried: " + tasks.size());
                        Tasks.whenAll(tasks).addOnCompleteListener(t -> {
                            if (!isAdded()) return;
                            Log.d(TAG, "Total reviews loaded: " + reviewList.size());
                            reviewAdapter.notifyDataSetChanged();
                            updateVisibility();
                        });
                    } else {
                        Log.e(TAG, "Failed to load reviews", task.getException());
                        tvEmptyState.setText("Ошибка загрузки отзывов: " + task.getException().getMessage());
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvReviews.setVisibility(View.GONE);
                    }
                });
    }

    private void updateVisibility() {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (reviewList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Нет отзывов");
                rvReviews.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvReviews.setVisibility(View.VISIBLE);
            }
        });
    }

    public static ReviewsFragment newInstance(String userId) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        fragment.setArguments(args);
        return fragment;
    }
}