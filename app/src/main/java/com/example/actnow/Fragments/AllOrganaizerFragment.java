package com.example.actnow.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.actnow.Adapters.OrganizerAdapter;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllOrganaizerFragment extends Fragment implements HomeFragment.SearchableFragment {
    private static final String TAG = "AllOrganaizerFragment";
    private RecyclerView rvOrganizers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OrganizerAdapter adapter;
    private List<UserProfile> organizers = new ArrayList<>();
    private List<UserProfile> allOrganizers = new ArrayList<>();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserId;

    public static AllOrganaizerFragment newInstance() {
        return new AllOrganaizerFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_organaizer, container, false);
        rvOrganizers = view.findViewById(R.id.rvOrganizers);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        setupRecyclerView();
        loadOrganizers();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new OrganizerAdapter(organizers, user -> {
            Fragment fragment;
            Bundle args = new Bundle();
            args.putString("userId", user.getUserId());
            if (user.getUserId().equals(currentUserId)) {
                fragment = new ProfileFragment();
            } else {
                fragment = new AnotherProfileFragment();
            }
            fragment.setArguments(args);
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        rvOrganizers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrganizers.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadOrganizers();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadOrganizers() {
        Query baseQuery = db.collection("Profiles")
                .whereIn("accountType", List.of("individual_organizer", "legal_entity", "volunteer"));

        baseQuery.get()
                .addOnSuccessListener(querySnapshot -> {
                    allOrganizers.clear();
                    Set<String> userIds = new HashSet<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        UserProfile user = doc.toObject(UserProfile.class);
                        if (user != null && !userIds.contains(user.getUserId())) {
                            user.setUserId(doc.getId());
                            userIds.add(user.getUserId());
                            String displayName = user.getUsername() != null ? user.getUsername() :
                                    (String) user.getProfile().get("name");
                            String displayCity = user.getCity() != null ? user.getCity() :
                                    (String) user.getProfile().get("city");
                            if ((displayName != null || displayCity != null) && !user.isBanned()) {
                                Log.d(TAG, "Loaded organizer: userId=" + user.getUserId() +
                                        ", name=" + displayName +
                                        ", city=" + displayCity +
                                        ", avatarUrl=" + user.getProfileImage() +
                                        ", accountType=" + user.getAccountType());
                                allOrganizers.add(user);
                            } else {
                                Log.w(TAG, "Skipped organizer with userId=" + doc.getId() +
                                        " due to missing name and city or banned");
                            }
                        } else {
                            Log.w(TAG, "Skipped organizer with userId=" + doc.getId() +
                                    " due to null user or duplicate");
                        }
                    }
                    if (currentUserId != null && !userIds.contains(currentUserId)) {
                        db.collection("Profiles").document(currentUserId).get()
                                .addOnSuccessListener(userDoc -> {
                                    UserProfile currentUser = userDoc.toObject(UserProfile.class);
                                    if (currentUser != null &&
                                            ("individual_organizer".equals(currentUser.getAccountType()) ||
                                                    "legal_entity".equals(currentUser.getAccountType()) ||
                                                    "volunteer".equals(currentUser.getAccountType())) &&
                                            !currentUser.isBanned()) {
                                        currentUser.setUserId(currentUserId);
                                        String displayName = currentUser.getUsername() != null ? currentUser.getUsername() :
                                                (String) currentUser.getProfile().get("name");
                                        String displayCity = currentUser.getCity() != null ? currentUser.getCity() :
                                                (String) currentUser.getProfile().get("city");
                                        if (displayName != null || displayCity != null) {
                                            userIds.add(currentUserId);
                                            Log.d(TAG, "Loaded current user: userId=" + currentUserId +
                                                    ", name=" + displayName +
                                                    ", city=" + displayCity +
                                                    ", avatarUrl=" + currentUser.getProfileImage());
                                            allOrganizers.add(currentUser);
                                        } else {
                                            Log.w(TAG, "Skipped current user with userId=" + currentUserId +
                                                    " due to missing name and city");
                                        }
                                    } else {
                                        Log.w(TAG, "Skipped current user with userId=" + currentUserId +
                                                " due to invalid accountType or banned");
                                    }
                                    updateOrganizersList();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to load current user profile", e);
                                    updateOrganizersList();
                                });
                    } else {
                        updateOrganizersList();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to load organizers", e));
    }

    private void updateOrganizersList() {
        sortOrganizersByRating();
        organizers.clear();
        organizers.addAll(allOrganizers);
        adapter.notifyDataSetChanged();
        Log.d(TAG, "Updated organizers list, size: " + organizers.size());
    }

    private void sortOrganizersByRating() {
        allOrganizers.sort((u1, u2) -> {
            int score1 = u1.getOrganizedEvents() + u1.getPostsCount();
            int score2 = u2.getOrganizedEvents() + u2.getPostsCount();
            return Integer.compare(score2, score1);
        });
    }

    @Override
    public void onSearch(String query, String filters) {
        Log.d(TAG, "Search query: " + query + ", filters: " + filters);
        if (query.isEmpty() && filters.isEmpty()) {
            loadOrganizers();
            return;
        }

        organizers.clear();
        for (UserProfile user : allOrganizers) {
            String name = user.getUsername() != null ? user.getUsername().toLowerCase() :
                    ((String) user.getProfile().get("name") != null ? ((String) user.getProfile().get("name")).toLowerCase() : "");
            String city = user.getCity() != null ? user.getCity().toLowerCase() :
                    ((String) user.getProfile().get("city") != null ? ((String) user.getProfile().get("city")).toLowerCase() : "");
            boolean matchesQuery = query.isEmpty() ||
                    name.contains(query.toLowerCase()) ||
                    city.contains(query.toLowerCase());

            boolean matchesFilters = filters.isEmpty();
            if (!filters.isEmpty()) {
                String[] filterArray = filters.split(",");
                for (String filter : filterArray) {
                    if ((filter.equals("Организаторы") && "individual_organizer".equals(user.getAccountType())) ||
                            (filter.equals("Организации") && "legal_entity".equals(user.getAccountType())) ||
                            (filter.equals("Волонтёры") && "volunteer".equals(user.getAccountType()))) {
                        matchesFilters = true;
                        break;
                    }
                }
            }

            if (matchesQuery && matchesFilters) {
                organizers.add(user);
            }
        }

        // Сортировка в зависимости от фильтров
        if (!filters.isEmpty()) {
            String[] filterArray = filters.split(",");
            if (filterArray.length == 1) {
                String filter = filterArray[0];
                if (filter.equals("Организаторы")) {
                    organizers.sort((u1, u2) -> Double.compare(u2.getRating(), u1.getRating()));
                } else if (filter.equals("Организации")) {
                    organizers.sort((u1, u2) -> {
                        Timestamp t1 = u1.getLastEventDate();
                        Timestamp t2 = u2.getLastEventDate();
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1);
                    });
                } else if (filter.equals("Волонтёры")) {
                    organizers.sort((u1, u2) -> Integer.compare(u2.getVolunteerHours(), u1.getVolunteerHours()));
                }
            }
        }

        adapter.notifyDataSetChanged();
        Log.d(TAG, "Search results, organizers size: " + organizers.size());
    }
}