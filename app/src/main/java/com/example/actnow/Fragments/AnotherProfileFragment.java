package com.example.actnow.Fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.actnow.ChatActivity;
import com.example.actnow.Models.ProfileViewModel;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import com.example.actnow.PreferencesManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class AnotherProfileFragment extends Fragment {

    private static final String TAG = "AnotherProfileFragment";
    private static final String ARG_USER_ID = "userId";
    private static final long QUERY_TIMEOUT_MS = 10000;
    private static final long SWIPE_DEBOUNCE_MS = 2000;

    private String userId;
    private String currentUserId;
    private boolean isFollowing;
    private boolean isViewDestroyed;
    private boolean isDataLoaded = false;
    private long lastSwipeTime = 0;

    private TextView tvUsername, tvBio, tvCity, tvBirthdate, tvRegistration, tvFollowStats;
    private TextView tvOrganizedEvents, tvSubscribed, tvCompleted, tvReviews, tvComments, tvInterestsLabel;
    private ImageView profileBackImageView, profileAvatarImageView;
    private Button btnMessage, btnFollow, btnEventReport;
    private LinearLayout infoContainer, llOrganizedEvents, menuLayout;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private ChipGroup chipGroupInterests;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference profilesRef;
    private PreferencesManager preferencesManager;
    private String accountType;
    private final Gson gson = new Gson();
    private Handler handler;
    private ProfileViewModel viewModel;
    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private ListenerRegistration profileListener;

    public static AnotherProfileFragment newInstance(String userId) {
        AnotherProfileFragment fragment = new AnotherProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called, savedInstanceState: " + (savedInstanceState != null));
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            Log.d(TAG, "onCreate: Received userId from args = " + userId);
        } else if (savedInstanceState != null) {
            userId = savedInstanceState.getString(ARG_USER_ID);
            Log.d(TAG, "onCreate: Restored userId from savedInstanceState = " + userId);
        }

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "userId is null or empty in onCreate");
            if (getActivity() != null) {
                Toast.makeText(getActivity(), "Ошибка: не удалось загрузить профиль", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        }

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        firebaseFirestore = FirebaseFirestore.getInstance();
        profilesRef = firebaseFirestore.collection("Profiles");
        preferencesManager = new PreferencesManager(requireContext());
        handler = new Handler(Looper.getMainLooper());
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (userId != null) {
            outState.putString(ARG_USER_ID, userId);
            Log.d(TAG, "onSaveInstanceState: Saved userId = " + userId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_another_profile, container, false);

        tvUsername = rootView.findViewById(R.id.tv_profile_uname);
        tvBio = rootView.findViewById(R.id.tv_bio);
        tvCity = rootView.findViewById(R.id.tv_profile_city);
        tvBirthdate = rootView.findViewById(R.id.tv_birthday);
        tvRegistration = rootView.findViewById(R.id.tv_registration);
        tvFollowStats = rootView.findViewById(R.id.tv_follow_stats);
        tvOrganizedEvents = rootView.findViewById(R.id.tv_organized_events);
        tvSubscribed = rootView.findViewById(R.id.tv_subscribed);
        tvCompleted = rootView.findViewById(R.id.tv_completed);
        tvReviews = rootView.findViewById(R.id.tv_reviews);
        tvComments = rootView.findViewById(R.id.tv_comments);
        tvInterestsLabel = rootView.findViewById(R.id.tv_interests_label);
        profileBackImageView = rootView.findViewById(R.id.profileBackImageView);
        profileAvatarImageView = rootView.findViewById(R.id.imv_post_uid);
        btnMessage = rootView.findViewById(R.id.btn_message);
        btnFollow = rootView.findViewById(R.id.btn_follow);
        btnEventReport = rootView.findViewById(R.id.btn_event_report);
        infoContainer = rootView.findViewById(R.id.info_container);
        llOrganizedEvents = rootView.findViewById(R.id.ll_organized_events);
        menuLayout = rootView.findViewById(R.id.menu_layout);
        swipeRefresh = rootView.findViewById(R.id.swipe_refresh_layout);
        progressBar = rootView.findViewById(R.id.progress_bar);
        chipGroupInterests = rootView.findViewById(R.id.chip_group_interests);

        if (infoContainer != null) infoContainer.setVisibility(View.VISIBLE);
        if (tvBirthdate != null) tvBirthdate.setVisibility(View.VISIBLE);

        setupClickListeners();
        setupSwipeRefresh();

        isViewDestroyed = false;
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called, userId: " + userId);
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (isAdded() && userId != null && !isDataLoaded) {
            loadCachedProfile();
            refreshData();
            setupProfileListener();
            handler.postDelayed(() -> {
                if (isAdded() && !isViewDestroyed && progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                    Log.w(TAG, "Query response timeout after " + QUERY_TIMEOUT_MS + "ms");
                    handleLoadFailure("Время загрузки истекло");
                    requireActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
                    });
                }
            }, QUERY_TIMEOUT_MS);
            isDataLoaded = true;
        } else if (isDataLoaded) {
            requireActivity().runOnUiThread(() -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            });
        } else {
            handleLoadFailure("Ошибка: userId отсутствует");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called, userId: " + userId);
        if (isAdded() && !isDataLoaded && userId != null) {
            loadCachedProfile();
            refreshData();
            setupProfileListener();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isViewDestroyed = true;
        handler.removeCallbacksAndMessages(null);
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
            Log.d(TAG, "Profile listener removed");
        }
        tvUsername = null;
        tvBio = null;
        tvCity = null;
        tvBirthdate = null;
        tvRegistration = null;
        tvFollowStats = null;
        tvOrganizedEvents = null;
        tvSubscribed = null;
        tvCompleted = null;
        tvReviews = null;
        tvComments = null;
        tvInterestsLabel = null;
        profileBackImageView = null;
        profileAvatarImageView = null;
        btnMessage = null;
        btnFollow = null;
        btnEventReport = null;
        infoContainer = null;
        llOrganizedEvents = null;
        menuLayout = null;
        chipGroupInterests = null;
        Log.d(TAG, "Cleared view references in onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners called");
        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> startChatWithUser());
            Log.d(TAG, "btnMessage click listener set");
        }
        if (btnFollow != null) {
            btnFollow.setOnClickListener(v -> toggleFollowStatus());
            Log.d(TAG, "btnFollow click listener set");
        }
        if (btnEventReport != null) {
            btnEventReport.setOnClickListener(v -> showReportFragment());
            Log.d(TAG, "btnEventReport click listener set");
        }

        View.OnClickListener organizedListener = v -> {
            Log.d(TAG, "Clicked Organized Events");
            Toast.makeText(requireContext(), "Clicked Organized Events", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfileOrganizedFragment.class);
        };
        if (tvOrganizedEvents != null) tvOrganizedEvents.setOnClickListener(organizedListener);

        View.OnClickListener subscribedListener = v -> {
            Log.d(TAG, "Clicked Subscribed");
            Toast.makeText(requireContext(), "Clicked Subscribed", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfileRegisteredEventsFragment.class);
        };
        if (tvSubscribed != null) tvSubscribed.setOnClickListener(subscribedListener);

        View.OnClickListener completedListener = v -> {
            Log.d(TAG, "Clicked Completed");
            Toast.makeText(requireContext(), "Clicked Completed", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfileAttendedEventsFragment.class);
        };
        if (tvCompleted != null) tvCompleted.setOnClickListener(completedListener);

        View.OnClickListener reviewsListener = v -> {
            Log.d(TAG, "Clicked Reviews");
            Toast.makeText(requireContext(), "Clicked Reviews", Toast.LENGTH_SHORT).show();
            navigateToFragment(ReviewsFragment.class);
        };
        if (tvReviews != null) tvReviews.setOnClickListener(reviewsListener);

        View.OnClickListener commentsListener = v -> {
            Log.d(TAG, "Clicked Comments");
            Toast.makeText(requireContext(), "Clicked Comments", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfileCommentsFragment.class);
        };
        if (tvComments != null) tvComments.setOnClickListener(commentsListener);

        View.OnClickListener followStatsListener = v -> {
            Log.d(TAG, "Clicked Follow Stats");
            Toast.makeText(requireContext(), "Clicked Follow Stats", Toast.LENGTH_SHORT).show();
            navigateToFragment(FollowListFragment.class);
        };
        if (tvFollowStats != null) tvFollowStats.setOnClickListener(followStatsListener);
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSwipeTime < SWIPE_DEBOUNCE_MS) {
                    Log.d(TAG, "Swipe ignored due to debounce");
                    swipeRefresh.setRefreshing(false);
                    return;
                }
                lastSwipeTime = currentTime;
                Log.d(TAG, "Swipe to refresh triggered for userId: " + userId);
                isDataLoaded = false;
                refreshData();
                swipeRefresh.setRefreshing(false);
            });
            Log.d(TAG, "SwipeRefreshLayout initialized");
        } else {
            Log.w(TAG, "SwipeRefreshLayout is null, cannot set up refresh listener");
        }
    }

    private void setupProfileListener() {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot setup profile listener: userId is null or empty");
            return;
        }
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
            Log.d(TAG, "Removed existing profile listener for userId: " + userId);
        }
        profileListener = profilesRef.document(userId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed for userId: " + userId, e);
                        return;
                    }
                    if (!isAdded() || isViewDestroyed || snapshot == null) {
                        Log.d(TAG, "Ignoring snapshot update: fragment not attached or snapshot is null");
                        return;
                    }
                    if (snapshot.exists()) {
                        Log.d(TAG, "Profile updated for userId: " + userId);
                        UserProfile userProfile = snapshot.toObject(UserProfile.class);
                        if (userProfile != null) {
                            viewModel.setUserProfile(userProfile);
                            saveProfileToCache(userProfile);
                            updateFollowStats(userProfile);
                        } else {
                            Log.e(TAG, "Failed to deserialize UserProfile for userId: " + userId);
                        }
                    } else {
                        Log.w(TAG, "Profile document does not exist for userId: " + userId);
                    }
                });
        Log.d(TAG, "Profile listener setup for userId: " + userId);
    }

    private void updateFollowStats(UserProfile user) {
        if (!isAdded() || isViewDestroyed || tvFollowStats == null) {
            Log.w(TAG, "Cannot update follow stats: fragment not attached or view is null");
            return;
        }
        requireActivity().runOnUiThread(() -> {
            tvFollowStats.setText(user.getFollowingCount() + " читает  " + user.getFollowersCount() + " читателя(-ей)");
            Log.d(TAG, "Updated tv_follow_stats for userId: " + userId + " to: " + user.getFollowingCount() + " читает " + user.getFollowersCount() + " читателя(-ей)");
        });
    }

    private void refreshData() {
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "userId is null or empty, cannot refresh data");
            if (isAdded()) handleLoadFailure("Ошибка: ID пользователя отсутствует");
            return;
        }
        if (isLoading.get()) {
            Log.d(TAG, "Refresh skipped, already loading");
            return;
        }
        isLoading.set(true);
        Log.d(TAG, "Refreshing data for userId: " + userId);
        loadProfile();
    }

    private void loadCachedProfile() {
        String cachedProfileJson = preferencesManager.getString("profile_" + userId, null);
        if (cachedProfileJson != null) {
            try {
                Log.d(TAG, "Loading cached profile for userId: " + userId);
                UserProfile cachedProfile = gson.fromJson(cachedProfileJson, UserProfile.class);
                if (cachedProfile != null) {
                    viewModel.setUserProfile(cachedProfile);
                    processProfileData(cachedProfile);
                    checkFollowingStatus();
                    Log.d(TAG, "Cached profile loaded successfully for userId: " + userId);
                }
            } catch (JsonSyntaxException e) {
                Log.w(TAG, "Failed to parse cached profile JSON for userId: " + userId, e);
            } catch (Exception e) {
                Log.w(TAG, "Failed to load cached profile for userId: " + userId, e);
            }
        } else {
            Log.d(TAG, "No cached profile found for userId: " + userId);
        }
    }

    private void saveProfileToCache(UserProfile user) {
        try {
            String profileJson = gson.toJson(user);
            preferencesManager.putString("profile_" + userId, profileJson);
            Log.d(TAG, "Saved profile to cache for userId: " + userId);
        } catch (Exception e) {
            Log.w(TAG, "Failed to save profile to cache for userId: " + userId, e);
        }
    }

    private void loadProfile() {
        if (!isAdded() || isViewDestroyed) {
            isLoading.set(false);
            return;
        }
        Log.d(TAG, "Fetching profile for userId: " + userId);
        profilesRef.document(userId).get()
                .addOnCompleteListener(task -> {
                    isLoading.set(false);
                    if (!isAdded() || isViewDestroyed) return;
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Log.d(TAG, "Profile fetch successful for userId: " + userId);
                            UserProfile userProfile = document.toObject(UserProfile.class);
                            if (userProfile != null) {
                                viewModel.setUserProfile(userProfile);
                                saveProfileToCache(userProfile);
                                processProfileData(userProfile);
                                checkFollowingStatus();
                            } else {
                                Log.e(TAG, "Failed to deserialize UserProfile for userId: " + userId);
                                handleLoadFailure("Ошибка десериализации профиля");
                            }
                        } else {
                            Log.e(TAG, "Profile document does not exist for userId: " + userId);
                            handleLoadFailure("Профиль не найден");
                        }
                    } else {
                        FirebaseFirestoreException e = (FirebaseFirestoreException) task.getException();
                        String errorCode = e != null ? e.getCode().toString() : "Unknown";
                        Log.e(TAG, "Error loading profile for userId: " + userId + ", Code: " + errorCode, e);
                        handleLoadFailure("Ошибка загрузки профиля: " + errorCode);
                    }
                });
    }

    private void processProfileData(UserProfile user) {
        if (!isAdded() || isViewDestroyed || getView() == null) {
            Log.w(TAG, "Fragment not attached, view destroyed, or view is null");
            handleLoadFailure(null);
            return;
        }
        Log.d(TAG, "Processing profile data for userId: " + userId);
        if (user != null) {
            accountType = user.getAccountType();
            requireActivity().runOnUiThread(() -> {

                if (tvUsername != null) tvUsername.setText(user.getUsername() != null ? user.getUsername() : "");
                if (tvBio != null) tvBio.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "Описание не указано");
                if (tvCity != null) tvCity.setText(user.getCity() != null ? user.getCity() : "Город не указан");
                Timestamp birthDate = user.getBirthDate();
                if (tvBirthdate != null) {
                    String birthdayText = (birthDate != null && (accountType != null && (accountType.equalsIgnoreCase("legal_entity") || accountType.equalsIgnoreCase("individual_organizer"))))
                            ? "Дата создания: " + formatDate(birthDate.toDate())
                            : (birthDate != null ? "День рождения: " + formatDate(birthDate.toDate()) : "День рождения: не указан");
                    tvBirthdate.setText(birthdayText);
                    tvBirthdate.setVisibility(View.VISIBLE);
                }
                if (infoContainer != null) infoContainer.setVisibility(View.VISIBLE);
                if (llOrganizedEvents != null) llOrganizedEvents.setVisibility((accountType != null && (accountType.equalsIgnoreCase("legal_entity") || accountType.equalsIgnoreCase("individual_organizer"))) ? View.VISIBLE : View.GONE);
                if (accountType != null && accountType.equalsIgnoreCase("legal_entity")) {
                    if (tvSubscribed != null) {
                        tvSubscribed.setVisibility(View.GONE);
                        View subscribedLayout = tvSubscribed.getParent() instanceof View ? (View) tvSubscribed.getParent() : null;
                        if (subscribedLayout != null) {
                            ImageButton btnSubscribed = subscribedLayout.findViewById(R.id.btn_subscribed);
                            if (btnSubscribed != null) btnSubscribed.setVisibility(View.GONE);
                        }
                    }
                    if (tvCompleted != null) {
                        tvCompleted.setVisibility(View.GONE);
                        View completedLayout = tvCompleted.getParent() instanceof View ? (View) tvCompleted.getParent() : null;
                        if (completedLayout != null) {
                            ImageButton btnCompleted = completedLayout.findViewById(R.id.btn_completed);
                            if (btnCompleted != null) btnCompleted.setVisibility(View.GONE);
                        }
                    }
                }

                if (btnEventReport != null) {
                    btnEventReport.setVisibility((accountType != null && accountType.equalsIgnoreCase("volunteer")) ? View.GONE : View.VISIBLE);
                    Log.d(TAG, "btnEventReport visibility set to: " + (btnEventReport.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE") + " for accountType: " + accountType);
                }

                Timestamp createdAt = user.getCreatedAt();
                if (tvRegistration != null) tvRegistration.setText(createdAt != null ? "Регистрация: " + formatDate(createdAt.toDate()) : "Регистрация: не указана");
                if (tvFollowStats != null) {
                    tvFollowStats.setText(user.getFollowingCount() + " читает  " + user.getFollowersCount() + " читателя(-ей)");
                }
                if (profileAvatarImageView != null) {
                    String profileImage = user.getProfileImage();
                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImage)
                                .thumbnail(0.25f)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.default_profile_picture)
                                .error(R.drawable.default_profile_picture)
                                .circleCrop()
                                .into(profileAvatarImageView);
                    } else {
                        profileAvatarImageView.setImageResource(R.drawable.default_profile_picture);
                    }
                }
                if (profileBackImageView != null) {
                    String backgroundImage = user.getBackgroundImageUrl();
                    if (backgroundImage != null && !backgroundImage.isEmpty()) {
                        Glide.with(requireContext())
                                .load(backgroundImage)
                                .thumbnail(0.25f)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .placeholder(R.drawable.ava)
                                .error(R.drawable.ava)
                                .into(profileBackImageView);
                    } else {
                        profileBackImageView.setImageResource(R.drawable.ava);
                    }
                }

                @SuppressWarnings("unchecked")
                List<String> interests = (List<String>) user.getInterests();
                Log.d(TAG, "Account type: " + (accountType != null ? accountType : "null"));
                Log.d(TAG, "Interests retrieved: " + (interests != null ? interests.toString() : "null"));
                if (tvInterestsLabel != null && chipGroupInterests != null) {
                    if (accountType != null && !accountType.equalsIgnoreCase("legal_entity") && interests != null && !interests.isEmpty()) {
                        Log.d(TAG, "Setting interests visible");
                        tvInterestsLabel.setVisibility(View.VISIBLE);
                        chipGroupInterests.setVisibility(View.VISIBLE);
                        chipGroupInterests.removeAllViews();
                        for (String interest : interests) {
                            Chip chip = new Chip(requireContext());
                            chip.setText(interest != null ? interest : "Не указано");
                            chip.setEnabled(false);
                            chip.setTextSize(16);
                            chip.setChipBackgroundColorResource(R.color.back);
                            chip.setChipStrokeColorResource(R.color.button);
                            chip.setChipStrokeWidth(2);
                            chip.setChipCornerRadius(0);

                            Typeface typeface = ResourcesCompat.getFont(requireContext(), R.font.barkentina);
                            if (typeface != null) {
                                chip.setTypeface(typeface);
                            } else {
                                Log.w(TAG, "Font barkentina not loaded, using default");
                            }
                            chipGroupInterests.addView(chip);
                            chip.post(() -> {
                                Log.d(TAG, "Added chip: " + chip.getText() + ", visibility: " + chip.getVisibility() + ", width: " + chip.getWidth() + ", height: " + chip.getHeight());
                            });
                        }
                    } else {
                        Log.d(TAG, "Hiding interests for legal_entity or no interests");
                        tvInterestsLabel.setVisibility(View.GONE);
                        chipGroupInterests.setVisibility(View.GONE);
                        chipGroupInterests.removeAllViews();
                    }
                    Log.d(TAG, "tvInterestsLabel visibility: " + tvInterestsLabel.getVisibility());
                    Log.d(TAG, "chipGroupInterests visibility: " + chipGroupInterests.getVisibility() + ", width: " + chipGroupInterests.getWidth() + ", height: " + chipGroupInterests.getHeight());
                } else {
                    Log.e(TAG, "tvInterestsLabel or chipGroupInterests is null");
                }

                if (progressBar != null) progressBar.setVisibility(View.GONE);
            });
        } else {
            Log.e(TAG, "UserProfile is null for userId: " + userId);
            handleLoadFailure("Ошибка обработки профиля");
        }
    }

    private String formatDate(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date", e);
            return "";
        }
    }

    private void handleLoadFailure(String errorMessage) {
        if (!isAdded() || isViewDestroyed) return;
        requireActivity().runOnUiThread(() -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            if (errorMessage != null) {
                Log.w(TAG, "Load failure: " + errorMessage);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkFollowingStatus() {
        if (!isAdded() || isViewDestroyed || currentUserId == null || currentUserId.equals(userId)) {
            if (btnFollow != null) btnFollow.setVisibility(View.GONE);
            if (btnMessage != null) btnMessage.setVisibility(View.GONE);
            return;
        }
        Log.d(TAG, "Checking following status for userId: " + userId);
        profilesRef.document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    if (!isAdded() || isViewDestroyed) return;
                    if (task.isSuccessful() && task.getResult() != null) {
                        UserProfile currentUser = task.getResult().toObject(UserProfile.class);
                        if (currentUser != null && currentUser.getStats() != null && currentUser.getStats().containsKey("following")) {
                            @SuppressWarnings("unchecked")
                            List<String> following = (List<String>) currentUser.getStats().get("following");
                            isFollowing = following != null && following.contains(userId);
                            Log.d(TAG, "Following status: " + isFollowing + " for userId: " + userId);
                            updateFollowButton();
                        } else {
                            Log.w(TAG, "No following data found for currentUserId: " + currentUserId);
                            isFollowing = false;
                            updateFollowButton();
                        }
                    } else {
                        FirebaseFirestoreException e = (FirebaseFirestoreException) task.getException();
                        String errorCode = e != null ? e.getCode().toString() : "Unknown";
                        Log.e(TAG, "Error checking following status for userId: " + currentUserId + ", Code: " + errorCode, e);
                        isFollowing = false;
                        updateFollowButton();
                    }
                });
    }

    private void updateFollowButton() {
        if (!isAdded() || isViewDestroyed) return;
        requireActivity().runOnUiThread(() -> {
            if (btnFollow != null) btnFollow.setText(isFollowing ? "Отписаться" : "Подписаться");
            if (btnMessage != null) btnMessage.setVisibility(View.VISIBLE);
        });
    }

    private void toggleFollowStatus() {
        if (!isAdded() || isViewDestroyed || currentUserId == null) return;
        Log.d(TAG, "Toggling follow status for userId: " + userId + ", current state: " + isFollowing);
        DocumentReference currentUserRef = profilesRef.document(currentUserId);
        DocumentReference targetUserRef = profilesRef.document(userId);
        if (isFollowing) {
            currentUserRef.update("stats.following", FieldValue.arrayRemove(userId))
                    .addOnSuccessListener(aVoid -> {
                        targetUserRef.update("stats.followers", FieldValue.arrayRemove(currentUserId))
                                .addOnSuccessListener(aVoid2 -> {
                                    currentUserRef.update("stats.followingCount", FieldValue.increment(-1));
                                    targetUserRef.update("stats.followersCount", FieldValue.increment(-1));
                                    isFollowing = false;
                                    updateFollowButton();
                                    Log.d(TAG, "Unfollowed userId: " + userId);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating followers", e));
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error removing follow", e));
        } else {
            currentUserRef.update("stats.following", FieldValue.arrayUnion(userId))
                    .addOnSuccessListener(aVoid -> {
                        targetUserRef.update("stats.followers", FieldValue.arrayUnion(currentUserId))
                                .addOnSuccessListener(aVoid2 -> {
                                    currentUserRef.update("stats.followingCount", FieldValue.increment(1));
                                    targetUserRef.update("stats.followersCount", FieldValue.increment(1));
                                    isFollowing = true;
                                    updateFollowButton();
                                    Log.d(TAG, "Followed userId: " + userId);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Error updating followers", e));
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding follow", e));
        }
    }

    private void startChatWithUser() {
        if (!isAdded() || isViewDestroyed || userId == null || userId.isEmpty()) {
            if (isAdded()) Toast.makeText(requireContext(), "Ошибка: ID пользователя отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId.equals(currentUserId)) {
            Toast.makeText(requireContext(), "Ошибка: Невозможно начать чат с самим собой", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted to start self-chat with userId: " + userId);
            return;
        }
        Log.d(TAG, "Starting chat with userId: " + userId);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    private void showReportFragment() {
        if (!isAdded() || isViewDestroyed || userId == null || userId.isEmpty()) {
            if (isAdded()) Toast.makeText(requireContext(), "Ошибка: ID пользователя отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Showing report fragment for userId: " + userId);
        ReportFragment reportFragment = ReportFragment.newInstance(userId);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, reportFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void navigateToFragment(Class<? extends Fragment> fragmentClass) {
        if (!isAdded() || isViewDestroyed || userId == null || userId.isEmpty()) {
            Log.e(TAG, "Navigation failed: Fragment not added or userId is null/empty, isAdded=" + isAdded() + ", isViewDestroyed=" + isViewDestroyed + ", userId=" + userId);
            if (isAdded()) Toast.makeText(requireContext(), "Ошибка: ID пользователя отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }
        Fragment fragment;
        Bundle args = new Bundle();
        args.putString("userId", userId);
        Log.d(TAG, "Navigating to " + fragmentClass.getSimpleName() + " with userId: " + userId);
        try {
            fragment = fragmentClass.getDeclaredConstructor().newInstance();
            fragment.setArguments(args);
            if (getActivity() == null) {
                Log.e(TAG, "Navigation failed: getActivity() is null");
                return;
            }
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null);
            transaction.commit();
            Log.d(TAG, "Transaction committed for " + fragmentClass.getSimpleName());
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to fragment " + fragmentClass.getSimpleName() + ": " + e.getMessage(), e);
            if (isAdded()) Toast.makeText(requireContext(), "Ошибка навигации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}