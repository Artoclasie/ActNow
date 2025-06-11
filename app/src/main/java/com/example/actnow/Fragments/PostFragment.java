package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class PostFragment extends Fragment {
    private static final String TAG = "PostFragment";
    private static final int POST_LIMIT = 10;

    private RecyclerView rvPosts;
    private SwipeRefreshLayout swipeRefreshLayout;
    private PostContentAdapter postContentAdapter;
    private FirebaseFirestore db;
    private ArrayList<PostReferenceModel> postList = new ArrayList<>();
    private ImageView ivAdd;
    private boolean isLoading = false;
    private DocumentSnapshot lastVisible;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        rvPosts = view.findViewById(R.id.rvPosts);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        ivAdd = view.findViewById(R.id.ivAdd);

        setupRecyclerView();
        setupSwipeRefresh();
        setupCreatePostButton();
        loadInitialPosts();
        setupScrollListener();
    }

    private void setupRecyclerView() {
        postContentAdapter = new PostContentAdapter(postList, getContext()) {
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                super.onBindViewHolder(holder, position);
                Log.d("PostAdapter", "Binding position: " + position +
                        ", username: " + postList.get(position).getUsername());
            }
        };
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPosts.setAdapter(postContentAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d(TAG, "Swipe refresh triggered");
            if (isAdded()) {
                postList.clear();
                lastVisible = null;
                loadInitialPosts();
            }
        });
    }

    private void setupCreatePostButton() {
        ivAdd.setOnClickListener(v -> {
            if (isAdded()) {
                CreatePostFragment createPostFragment = new CreatePostFragment();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, createPostFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    private void loadInitialPosts() {
        if (!isAdded()) return;

        isLoading = true;
        swipeRefreshLayout.setRefreshing(true);

        db.collection("Posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(POST_LIMIT)
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        isLoading = false;
                        swipeRefreshLayout.setRefreshing(false);

                        if (task.isSuccessful()) {
                            postList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    PostReferenceModel post = document.toObject(PostReferenceModel.class);
                                    post.setId(document.getId());
                                    postList.add(post);
                                    if (!task.getResult().isEmpty()) {
                                        lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing post", e);
                                }
                            }
                            postContentAdapter.updatePosts(postList);
                            loadUserDataForPosts();
                        } else {
                            Log.e(TAG, "Error loading initial posts", task.getException());
                            Toast.makeText(requireContext(), "Ошибка загрузки постов", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadMorePosts() {
        if (!isAdded() || isLoading || lastVisible == null) return;

        isLoading = true;
        postContentAdapter.showLoading(true);
        Log.d(TAG, "Loading more posts from Firestore");

        db.collection("Posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(POST_LIMIT)
                .get()
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        isLoading = false;
                        postContentAdapter.showLoading(false);

                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);

                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    PostReferenceModel post = document.toObject(PostReferenceModel.class);
                                    post.setId(document.getId());
                                    postList.add(post);
                                    Log.d(TAG, "Added more post: " + post.getContent());
                                }

                                postContentAdapter.notifyDataSetChanged();
                                loadUserDataForPosts();
                            }
                        } else {
                            Log.e(TAG, "Error loading more posts", task.getException());
                            Toast.makeText(requireContext(), "Ошибка загрузки постов", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadUserDataForPosts() {
        if (postList == null || postList.isEmpty()) {
            Log.d(TAG, "No posts to load user data for");
            return;
        }

        for (int i = 0; i < postList.size(); i++) {
            PostReferenceModel post = postList.get(i);
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
                            requireActivity().runOnUiThread(() -> {
                                postContentAdapter.notifyItemChanged(position);
                                Log.d(TAG, "Notified adapter for position: " + position);
                            });
                            Log.d(TAG, "Loaded user data for post " + position +
                                    ": " + post.getUsername() +
                                    " from " + post.getCity());
                        } else {
                            Log.w(TAG, "Profile not found for: " + post.getAuthorId());
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error loading user data for post " + position, e));
        }
    }

    private void setupScrollListener() {
        rvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
    public void onDestroyView() {
        super.onDestroyView();
        // Clear adapter to prevent memory leaks
        rvPosts.setAdapter(null);
    }
}