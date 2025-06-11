package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.actnow.Adapters.PostContentAdapter;
import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllVolunteerFragment extends Fragment implements HomeFragment.SearchableFragment {
    private static final String TAG = "AllVolunteerFragment";
    private static final int POST_LIMIT = 10;

    private RecyclerView rvVolunteers;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostContentAdapter adapter;
    private List<PostReferenceModel> posts = new ArrayList<>();
    private List<PostReferenceModel> allPosts = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private boolean isLoading = false;
    private DocumentSnapshot lastVisible;
    private Map<String, String> authorAccountTypes = new HashMap<>();

    public static AllVolunteerFragment newInstance() {
        return new AllVolunteerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_volunteer, container, false);
        rvVolunteers = view.findViewById(R.id.rvVolunteers);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        setupRecyclerView();
        loadPosts();
        return view;
    }

    private void setupRecyclerView() {
        adapter = new PostContentAdapter(posts, getContext()) {
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Log.d(TAG, "Binding position: " + position +
                        ", username: " + posts.get(position).getUsername());
            }
        };
        rvVolunteers.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVolunteers.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe refresh triggered");
            if (isAdded()) {
                posts.clear();
                allPosts.clear();
                lastVisible = null;
                authorAccountTypes.clear();
                loadPosts();
            }
        });

        setupScrollListener();
    }

    private void loadPosts() {
        if (!isAdded() || currentUserId == null) {
            isLoading = false;
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            Log.w(TAG, "Cannot load posts: Fragment not attached or userId is null");
            return;
        }

        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);

        db.collection("Profiles").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) {
                        isLoading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        return;
                    }

                    List<String> followedUserIds = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("stats")) {
                            Map<String, Object> stats = (Map<String, Object>) documentSnapshot.get("stats");
                            if (stats != null && stats.containsKey("following")) {
                                Object followingObj = stats.get("following");
                                Log.d(TAG, "Following field value: " + followingObj + ", type: " +
                                        (followingObj != null ? followingObj.getClass().getSimpleName() : "null"));
                                if (followingObj instanceof List) {
                                    followedUserIds = (List<String>) followingObj;
                                    Log.d(TAG, "Retrieved following list from stats: " + followedUserIds);
                                } else {
                                    Log.w(TAG, "Following field in stats is not a list, value: " + followingObj);
                                }
                            } else {
                                Log.w(TAG, "Following field is missing in stats");
                            }
                        } else {
                            Log.w(TAG, "Stats field is missing in the profile document");
                        }
                    } else {
                        Log.w(TAG, "Profile document does not exist for user: " + currentUserId);
                    }

                    if (followedUserIds.isEmpty()) {
                        isLoading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.updatePosts(posts);
                        Log.d(TAG, "No followed users found");
                        return;
                    }

                    Log.d(TAG, "Fetching posts for followed users: " + followedUserIds);

                    Query query = db.collection("Posts")
                            .whereIn("authorId", followedUserIds)
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .limit(POST_LIMIT);

                    query.get()
                            .addOnSuccessListener(postSnapshot -> {
                                if (!isAdded()) {
                                    isLoading = false;
                                    swipeRefreshLayout.setRefreshing(false);
                                    return;
                                }

                                posts.clear();
                                allPosts.clear();
                                Log.d(TAG, "Post snapshot size: " + postSnapshot.size());
                                for (DocumentSnapshot doc : postSnapshot.getDocuments()) {
                                    try {
                                        PostReferenceModel post = doc.toObject(PostReferenceModel.class);
                                        if (post != null) {
                                            post.setId(doc.getId());
                                            posts.add(post);
                                            allPosts.add(post);
                                            Log.d(TAG, "Loaded post: ID=" + post.getId() +
                                                    ", content=" + post.getContent() +
                                                    ", authorId=" + post.getAuthorId());
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error parsing post", e);
                                    }
                                }
                                lastVisible = postSnapshot.getDocuments().isEmpty() ? null :
                                        postSnapshot.getDocuments().get(postSnapshot.size() - 1);
                                Log.d(TAG, "Posts list size after loading: " + posts.size());
                                adapter.updatePosts(posts);
                                loadUserDataForPosts();
                                isLoading = false;
                                swipeRefreshLayout.setRefreshing(false);
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    isLoading = false;
                                    swipeRefreshLayout.setRefreshing(false);
                                    Log.e(TAG, "Failed to load posts", e);
                                    Toast.makeText(requireContext(), "Ошибка загрузки постов", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        isLoading = false;
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, "Failed to load user profile for user: " + currentUserId, e);
                        Toast.makeText(requireContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMorePosts() {
        if (!isAdded() || isLoading || lastVisible == null || currentUserId == null) {
            Log.d(TAG, "Skipping loadMorePosts: isLoading=" + isLoading +
                    ", lastVisible=" + (lastVisible == null ? "null" : "exists") +
                    ", currentUserId=" + currentUserId);
            return;
        }

        isLoading = true;
        adapter.showLoading(true);
        Log.d(TAG, "Loading more posts from Firestore");

        db.collection("Profiles").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) {
                        isLoading = false;
                        adapter.showLoading(false);
                        return;
                    }

                    List<String> followedUserIds = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("stats")) {
                            Map<String, Object> stats = (Map<String, Object>) documentSnapshot.get("stats");
                            if (stats != null && stats.containsKey("following")) {
                                Object followingObj = stats.get("following");
                                Log.d(TAG, "Following field value (more posts): " + followingObj + ", type: " +
                                        (followingObj != null ? followingObj.getClass().getSimpleName() : "null"));
                                if (followingObj instanceof List) {
                                    followedUserIds = (List<String>) followingObj;
                                    Log.d(TAG, "Retrieved following list from stats for more posts: " + followedUserIds);
                                } else {
                                    Log.w(TAG, "Following field in stats is not a list for more posts, value: " + followingObj);
                                }
                            } else {
                                Log.w(TAG, "Following field is missing in stats for more posts");
                            }
                        } else {
                            Log.w(TAG, "Stats field is missing in the profile document for more posts");
                        }
                    } else {
                        Log.w(TAG, "Profile document does not exist for user: " + currentUserId);
                    }

                    if (followedUserIds.isEmpty()) {
                        isLoading = false;
                        adapter.showLoading(false);
                        Log.d(TAG, "No followed users found for loading more posts");
                        return;
                    }

                    Query query = db.collection("Posts")
                            .whereIn("authorId", followedUserIds)
                            .orderBy("createdAt", Query.Direction.DESCENDING)
                            .startAfter(lastVisible)
                            .limit(POST_LIMIT);

                    query.get()
                            .addOnSuccessListener(postSnapshot -> {
                                if (!isAdded()) {
                                    isLoading = false;
                                    adapter.showLoading(false);
                                    return;
                                }

                                Log.d(TAG, "Post snapshot size (more posts): " + postSnapshot.size());
                                if (!postSnapshot.isEmpty()) {
                                    lastVisible = postSnapshot.getDocuments().get(postSnapshot.size() - 1);
                                    for (DocumentSnapshot doc : postSnapshot.getDocuments()) {
                                        PostReferenceModel post = doc.toObject(PostReferenceModel.class);
                                        if (post != null) {
                                            post.setId(doc.getId());
                                            posts.add(post);
                                            allPosts.add(post);
                                            Log.d(TAG, "Loaded more post: " + post.getContent());
                                        }
                                    }
                                    Log.d(TAG, "Posts list size after loading more: " + posts.size());
                                    adapter.updatePosts(posts);
                                    loadUserDataForPosts();
                                } else {
                                    Log.d(TAG, "No more posts to load");
                                }
                                isLoading = false;
                                adapter.showLoading(false);
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    isLoading = false;
                                    adapter.showLoading(false);
                                    Log.e(TAG, "Error loading more posts", e);
                                    Toast.makeText(requireContext(), "Ошибка загрузки постов", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        isLoading = false;
                        adapter.showLoading(false);
                        Log.e(TAG, "Failed to load user profile for more posts", e);
                        Toast.makeText(requireContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadUserDataForPosts() {
        if (posts.isEmpty()) {
            Log.d(TAG, "No posts to load user data for");
            return;
        }

        for (int i = 0; i < posts.size(); i++) {
            PostReferenceModel post = posts.get(i);
            if (post.getAuthorId() == null) {
                Log.w(TAG, "Post has no authorId: " + post.getId());
                continue;
            }

            final int position = i;
            db.collection("Profiles").document(post.getAuthorId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isAdded() && documentSnapshot.exists()) {
                            post.setUsername(documentSnapshot.getString("username"));
                            post.setProfileImageUrl(documentSnapshot.getString("avatarUrl"));
                            post.setCity(documentSnapshot.getString("city"));
                            String accountType = documentSnapshot.getString("accountType");
                            authorAccountTypes.put(post.getAuthorId(), accountType);
                            requireActivity().runOnUiThread(() -> {
                                adapter.notifyItemChanged(position);
                                Log.d(TAG, "Notified adapter for position: " + position);
                            });
                            Log.d(TAG, "Loaded user data for post " + position +
                                    ": " + post.getUsername() + " from " + post.getCity() +
                                    ", accountType: " + accountType);
                        } else {
                            Log.w(TAG, "Profile not found for: " + post.getAuthorId());
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading user data for post " + position, e));
        }
    }

    private void setupScrollListener() {
        rvVolunteers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0 && totalItemCount >= POST_LIMIT) {
                        loadMorePosts();
                    }
                }
            }
        });
    }

    @Override
    public void onSearch(String query, String filters) {
        if (!isAdded()) {
            Log.w(TAG, "Fragment not attached, skipping search");
            return;
        }

        Log.d(TAG, "Search query: " + query + ", filters: " + filters);
        if (query.isEmpty() && filters.isEmpty()) {
            posts.clear();
            posts.addAll(allPosts);
            adapter.updatePosts(posts);
            loadUserDataForPosts();
            Log.d(TAG, "Reset to all posts, size: " + posts.size());
            return;
        }

        posts.clear();
        for (PostReferenceModel post : allPosts) {
            String city = post.getCity() != null ? post.getCity().toLowerCase() : "";
            String content = post.getContent() != null ? post.getContent().toLowerCase() : "";
            String username = post.getUsername() != null ? post.getUsername().toLowerCase() : "";
            boolean matchesQuery = query.isEmpty() ||
                    city.contains(query.toLowerCase()) ||
                    content.contains(query.toLowerCase()) ||
                    username.contains(query.toLowerCase());

            boolean matchesFilters = filters.isEmpty();
            if (!filters.isEmpty()) {
                String[] filterArray = filters.split(",");
                String accountType = authorAccountTypes.getOrDefault(post.getAuthorId(), "");
                for (String filter : filterArray) {
                    if ((filter.equals("Организаторы") && "individual_organizer".equals(accountType)) ||
                            (filter.equals("Организации") && "legal_entity".equals(accountType)) ||
                            (filter.equals("Волонтёры") && "volunteer".equals(accountType))) {
                        matchesFilters = true;
                        break;
                    }
                }
            }

            if (matchesQuery && matchesFilters) {
                posts.add(post);
            }
        }

        // Сортировка по Timestamp
        posts.sort((p1, p2) -> {
            if (p1.getCreatedAt() == null || p2.getCreatedAt() == null) {
                return p1.getCreatedAt() == null ? 1 : -1; // Если один из них null, другой "больше"
            }
            return p2.getCreatedAt().compareTo(p1.getCreatedAt()); // Сравниваем Timestamp
        });
        adapter.updatePosts(posts);
        Log.d(TAG, "Posts loaded: " + posts.size());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvVolunteers.setAdapter(null);
        posts.clear();
        allPosts.clear();
        authorAccountTypes.clear();
        Log.d(TAG, "Resources cleared in onDestroyView");
    }
}