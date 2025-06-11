package com.example.actnow.Fragments;

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
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.PreferencesManager;
import com.example.actnow.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final long QUERY_TIMEOUT_MS = 10000; // Таймаут 10 секунд
    private static final long SWIPE_DEBOUNCE_MS = 2000; // Дебouncing для свайпа

    private ImageView profileBackImageView, imvPostUid;
    private TextView tvProfileTitle, tvProfileUname, tvBio, tvBirthDate, tvRegistrationDate, tvCity, tvFollowStats;
    private TextView tvOrganizedEvents, tvPosts, tvSubscribed, tvCompleted, tvReviews;
    private ImageButton menuButton, btnOrganizedEvents, btnPosts, btnSubscribed, btnCompleted, btnReviews;
    private Button btnEventReport;
    private MaterialButton btnCreateEvent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference profilesRef;
    private String accountType;
    private PreferencesManager preferencesManager;
    private final Gson gson = new Gson();
    private Handler handler;
    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private ListenerRegistration profileListener;
    private boolean isViewDestroyed;
    private boolean isDataLoaded = false;
    private long lastSwipeTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initViews(view);
        isViewDestroyed = false;
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
        preferencesManager = new PreferencesManager(requireContext());
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        setupClickListeners();
        setupSwipeRefresh();

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (mAuth.getCurrentUser() != null) {
            loadCachedProfile();
            loadUserProfile();
            setupProfileListener();
            handler.postDelayed(() -> {
                if (isAdded() && !isViewDestroyed && progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                    Log.w(TAG, "Query response timeout after " + QUERY_TIMEOUT_MS + "ms");
                    handleLoadFailure("Время загрузки истекло");
                    requireActivity().runOnUiThread(() -> {
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                    });
                }
            }, QUERY_TIMEOUT_MS);
            isDataLoaded = true;
        } else {
            handleLoadFailure("Пользователь не авторизован");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        if (isAdded() && !isDataLoaded && mAuth.getCurrentUser() != null) {
            loadCachedProfile();
            loadUserProfile();
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
        swipeRefreshLayout = null;
        progressBar = null;
        tvFollowStats = null;
        tvOrganizedEvents = null;
        tvPosts = null;
        tvSubscribed = null;
        tvCompleted = null;
        tvReviews = null;
        profileBackImageView = null;
        imvPostUid = null;
        tvProfileTitle = null;
        tvProfileUname = null;
        tvBio = null;
        tvCity = null;
        tvBirthDate = null;
        tvRegistrationDate = null;
        menuButton = null;
        btnEventReport = null;
        btnCreateEvent = null;
        btnOrganizedEvents = null;
        btnPosts = null;
        btnSubscribed = null;
        btnCompleted = null;
        btnReviews = null;
        Log.d(TAG, "Cleared view references in onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler = null;
    }

    private void initViews(View view) {
        profileBackImageView = view.findViewById(R.id.profileBackImageView);
        imvPostUid = view.findViewById(R.id.imv_post_uid);
        tvProfileTitle = view.findViewById(R.id.tv_profile_title);
        tvProfileUname = view.findViewById(R.id.tv_profile_uname);
        tvBio = view.findViewById(R.id.tv_bio);
        tvCity = view.findViewById(R.id.tv_city);
        tvBirthDate = view.findViewById(R.id.tv_birthday);
        tvRegistrationDate = view.findViewById(R.id.tv_registration_date);
        tvFollowStats = view.findViewById(R.id.tv_follow_stats);
        tvOrganizedEvents = view.findViewById(R.id.tv_organized_events);
        tvPosts = view.findViewById(R.id.tv_posts);
        tvSubscribed = view.findViewById(R.id.tv_subscribed);
        tvCompleted = view.findViewById(R.id.tv_completed);
        tvReviews = view.findViewById(R.id.tv_reviews);
        menuButton = view.findViewById(R.id.menu_button);
        btnEventReport = view.findViewById(R.id.btn_event_report);
        btnCreateEvent = view.findViewById(R.id.btn_create_event);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progress_bar);
        btnOrganizedEvents = view.findViewById(R.id.btn_organized_events);
        btnPosts = view.findViewById(R.id.btn_posts);
        btnSubscribed = view.findViewById(R.id.btn_subscribed);
        btnCompleted = view.findViewById(R.id.btn_completed);
        btnReviews = view.findViewById(R.id.btn_reviews);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        profilesRef = firebaseFirestore.collection("Profiles");
    }

    private void setupClickListeners() {
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> navigateToSettings());
        }
        if (btnEventReport != null) {
            btnEventReport.setOnClickListener(v -> navigateToEventReport());
        }
        if (btnCreateEvent != null) {
            btnCreateEvent.setOnClickListener(v -> navigateToCreateEvent());
        }
        if (tvFollowStats != null) {
            tvFollowStats.setOnClickListener(v -> navigateToFollowList());
        }

        View.OnClickListener organizedListener = v -> {
            Log.d(TAG, "Clicked Organized Events");
            Toast.makeText(requireContext(), "Clicked Organized Events", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfileOrganizedFragment.class);
        };
        if (tvOrganizedEvents != null) tvOrganizedEvents.setOnClickListener(organizedListener);
        if (btnOrganizedEvents != null) btnOrganizedEvents.setOnClickListener(organizedListener);

        View.OnClickListener postsListener = v -> {
            Log.d(TAG, "Clicked Posts");
            Toast.makeText(requireContext(), "Clicked Posts", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfilePostsFragment.class);
        };
        if (tvPosts != null) tvPosts.setOnClickListener(postsListener);
        if (btnPosts != null) btnPosts.setOnClickListener(postsListener);

        View.OnClickListener subscribedListener = v -> {
            Log.d(TAG, "Clicked Subscribed");
            Toast.makeText(requireContext(), "Clicked Subscribed", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfileRegisteredEventsFragment.class);
        };
        if (tvSubscribed != null) tvSubscribed.setOnClickListener(subscribedListener);
        if (btnSubscribed != null) btnSubscribed.setOnClickListener(subscribedListener);

        View.OnClickListener completedListener = v -> {
            Log.d(TAG, "Clicked Completed");
            Toast.makeText(requireContext(), "Clicked Completed", Toast.LENGTH_SHORT).show();
            navigateToFragment(ProfileAttendedEventsFragment.class);
        };
        if (tvCompleted != null) tvCompleted.setOnClickListener(completedListener);
        if (btnCompleted != null) btnCompleted.setOnClickListener(completedListener);

        View.OnClickListener reviewsListener = v -> {
            Log.d(TAG, "Clicked Reviews");
            Toast.makeText(requireContext(), "Clicked Reviews", Toast.LENGTH_SHORT).show();
            navigateToFragment(ReviewsFragment.class);
        };
        if (tvReviews != null) tvReviews.setOnClickListener(reviewsListener);
        if (btnReviews != null) btnReviews.setOnClickListener(reviewsListener);
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSwipeTime < SWIPE_DEBOUNCE_MS) {
                    Log.d(TAG, "Swipe ignored due to debounce");
                    swipeRefreshLayout.setRefreshing(false);
                    return;
                }
                lastSwipeTime = currentTime;
                Log.d(TAG, "Swipe refresh triggered");
                if (isAdded()) {
                    isDataLoaded = false;
                    loadUserProfile();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
            swipeRefreshLayout.setColorSchemeResources(android.R.color.white);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.button);
            Log.d(TAG, "SwipeRefreshLayout initialized");
        } else {
            Log.w(TAG, "SwipeRefreshLayout is null, cannot set up refresh listener");
        }
    }

    private void loadCachedProfile() {
        if (mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        String cachedProfileJson = preferencesManager.getString("profile_" + userId, null);
        if (cachedProfileJson != null) {
            try {
                Log.d(TAG, "Loading cached profile for userId: " + userId);
                UserProfile cachedProfile = gson.fromJson(cachedProfileJson, UserProfile.class);
                if (cachedProfile != null) {
                    processProfileData(cachedProfile);
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
            preferencesManager.putString("profile_" + user.getUserId(), profileJson);
            Log.d(TAG, "Saved profile to cache for userId: " + user.getUserId());
        } catch (Exception e) {
            Log.w(TAG, "Failed to save profile to cache for userId: " + user.getUserId(), e);
        }
    }

    private void loadUserProfile() {
        if (!isAdded() || mAuth.getCurrentUser() == null || isViewDestroyed) {
            isLoading.set(false);
            handleLoadFailure(null);
            return;
        }
        if (isLoading.get()) {
            Log.d(TAG, "Load skipped, already loading");
            return;
        }
        isLoading.set(true);
        String userId = mAuth.getCurrentUser().getUid();
        Log.d(TAG, "Fetching profile for userId: " + userId);
        profilesRef.document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    isLoading.set(false);
                    if (!isAdded() || isViewDestroyed) return;
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Log.d(TAG, "Profile fetch successful for userId: " + userId);
                            UserProfile userProfile = document.toObject(UserProfile.class);
                            if (userProfile != null) {
                                userProfile.setUserId(userId);
                                saveProfileToCache(userProfile);
                                processProfileData(userProfile);
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

    private void setupProfileListener() {
        if (mAuth.getCurrentUser() == null) {
            Log.e(TAG, "Cannot setup profile listener: user is null");
            return;
        }
        String userId = mAuth.getCurrentUser().getUid();
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
                            userProfile.setUserId(userId);
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
            Log.d(TAG, "Updated tv_follow_stats for userId: " + user.getUserId() + " to: " + user.getFollowingCount() + " читает " + user.getFollowersCount() + " читателя(-ей)");
        });
    }

    private void processProfileData(UserProfile user) {
        if (!isAdded() || isViewDestroyed || getView() == null) {
            Log.w(TAG, "Fragment not attached, view destroyed, or view is null");
            handleLoadFailure(null);
            return;
        }
        Log.d(TAG, "Processing profile data for userId: " + user.getUserId());
        if (user != null) {
            accountType = user.getAccountType();
            requireActivity().runOnUiThread(() -> {
                if (tvProfileTitle != null) {
                    tvProfileTitle.setText("Профиль");
                }
                if (tvProfileUname != null) {
                    tvProfileUname.setText(user.getUsername() != null ? user.getUsername() : "");
                }
                if (tvCity != null) {
                    tvCity.setText(user.getCity() != null && !user.getCity().isEmpty() ? user.getCity() : "Город не указан");
                }
                if (tvBio != null) {
                    tvBio.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "Описание не указано");
                }
                if (accountType != null && (accountType.equalsIgnoreCase("legal_entity") || accountType.equalsIgnoreCase("individual_organizer"))) {
                    if (btnCreateEvent != null) {
                        btnCreateEvent.setVisibility(View.VISIBLE);
                    }
                    if (tvBirthDate != null) {
                        Timestamp birthDate = user.getBirthDate();
                        tvBirthDate.setText(birthDate != null ? "Дата создания: " + formatDate(birthDate.toDate()) : "Дата создания: не указана");
                    }
                    if (tvOrganizedEvents != null) tvOrganizedEvents.setVisibility(View.VISIBLE);
                    if (btnOrganizedEvents != null) btnOrganizedEvents.setVisibility(View.VISIBLE);
                    if (accountType.equalsIgnoreCase("legal_entity")) {
                        if (tvSubscribed != null) tvSubscribed.setVisibility(View.GONE);
                        if (btnSubscribed != null) btnSubscribed.setVisibility(View.GONE);
                        if (tvCompleted != null) tvCompleted.setVisibility(View.GONE);
                        if (btnCompleted != null) btnCompleted.setVisibility(View.GONE);
                    }
                } else {
                    if (btnCreateEvent != null) {
                        btnCreateEvent.setVisibility(View.GONE);
                    }
                    if (tvBirthDate != null) {
                        Timestamp birthDate = user.getBirthDate();
                        tvBirthDate.setText(birthDate != null ? "День рождения: " + formatDate(birthDate.toDate()) : "День рождения: не указан");
                    }
                    if (tvOrganizedEvents != null) tvOrganizedEvents.setVisibility(View.GONE);
                    if (btnOrganizedEvents != null) btnOrganizedEvents.setVisibility(View.GONE);
                }
                if (tvRegistrationDate != null) {
                    Timestamp createdAt = user.getCreatedAt();
                    tvRegistrationDate.setText(createdAt != null ? "Регистрация: " + formatDate(createdAt.toDate()) : "Регистрация: не указана");
                }
                if (tvFollowStats != null) {
                    tvFollowStats.setText(user.getFollowingCount() + " читает  " + user.getFollowersCount() + " читателя(-ей)");
                }
                loadProfileImages(user.getProfileImage(), user.getBackgroundImageUrl());
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            Log.e(TAG, "UserProfile is null");
            handleLoadFailure("Ошибка обработки профиля");
        }
    }

    private void loadProfileImages(String avatarUrl, String backgroundImageUrl) {
        if (!isAdded()) return;

        if (avatarUrl != null && !avatarUrl.isEmpty() && imvPostUid != null) {
            Glide.with(requireContext())
                    .load(avatarUrl)
                    .thumbnail(0.25f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .into(imvPostUid);
        } else if (imvPostUid != null) {
            imvPostUid.setImageResource(R.drawable.default_profile_picture);
        }

        if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty() && profileBackImageView != null) {
            Glide.with(requireContext())
                    .load(backgroundImageUrl)
                    .thumbnail(0.25f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ava)
                    .error(R.drawable.ava)
                    .into(profileBackImageView);
        } else if (profileBackImageView != null) {
            profileBackImageView.setImageResource(R.drawable.ava);
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
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            if (errorMessage != null) {
                Log.w(TAG, "Load failure: " + errorMessage);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToFragment(Class<? extends Fragment> fragmentClass) {
        if (!isAdded() || mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        Fragment fragment;
        Bundle args = new Bundle();
        args.putString("userId", userId);
        try {
            fragment = fragmentClass.getDeclaredConstructor().newInstance();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to fragment " + fragmentClass.getSimpleName() + ": " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Ошибка навигации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSettings() {
        if (!isAdded() || getActivity() == null) return;
        try {
            SettingsFragment settingsFragment = new SettingsFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, settingsFragment)
                    .addToBackStack("profile_to_settings")
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(requireContext(), "Ошибка навигации", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToEventReport() {
        if (!isAdded() || getActivity() == null || mAuth.getCurrentUser() == null) return;
        try {
            String userId = mAuth.getCurrentUser().getUid();
            ReportFragment reportFragment = ReportFragment.newInstance(userId);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, reportFragment)
                    .addToBackStack("profile_to_report")
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(requireContext(), "Ошибка навигации", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToCreateEvent() {
        if (!isAdded() || getActivity() == null) return;
        try {
            CreateEventFragment createEventFragment = new CreateEventFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, createEventFragment)
                    .addToBackStack("profile_to_create_event")
                    .commit();
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(requireContext(), "Ошибка навигации", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToFollowList() {
        if (!isAdded() || mAuth.getCurrentUser() == null) return;
        String userId = mAuth.getCurrentUser().getUid();
        FollowListFragment followListFragment = FollowListFragment.newInstance(userId);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, followListFragment)
                .addToBackStack(null)
                .commit();
    }
}