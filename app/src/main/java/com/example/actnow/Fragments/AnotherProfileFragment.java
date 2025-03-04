package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Adapters.PostContentAdapter;
import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnotherProfileFragment extends Fragment {

    private static final String ARG_USER_ID = "uid";

    private String userId;

    private TextView tvUsername, tvCity, tvProfilePosts;
    private ImageView profileImageView;
    private Button btnMessage;
    private RecyclerView rvProfileContent;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference profilesCollection;
    private CollectionReference postsCollection;

    private ArrayList<PostReferenceModel> postList = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();
    private PostContentAdapter adapter;

    public AnotherProfileFragment() {
        // Required empty public constructor
    }

    public static AnotherProfileFragment newInstance(String userId) {
        AnotherProfileFragment fragment = new AnotherProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);  // Передаем userId
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);  // Извлекаем uid
            Log.d("AnotherProfileFragment", "Received userId: " + userId);
        } else {
            Log.e("AnotherProfileFragment", "No userId passed");
        }

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        profilesCollection = firebaseFirestore.collection("Profiles");
        postsCollection = firebaseFirestore.collection("Posts");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_another_profile, container, false);

        // Инициализация элементов
        tvUsername = rootView.findViewById(R.id.tv_profile_uname);
        tvCity = rootView.findViewById(R.id.tv_profile_city);
        tvProfilePosts = rootView.findViewById(R.id.tv_profile_posts);
        profileImageView = rootView.findViewById(R.id.profileImageView);
        btnMessage = rootView.findViewById(R.id.btn_message);
        rvProfileContent = rootView.findViewById(R.id.rv_profile_posts);

        loadProfileInfo();
        setupRecyclerView();
        loadUserPosts();

        btnMessage.setOnClickListener(v -> startChatWithUser());

        return rootView;
    }

    private void loadProfileInfo() {
        profilesCollection.document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String city = documentSnapshot.getString("city");
                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        Long postCount = documentSnapshot.getLong("countPost");

                        Log.d("AnotherProfileFragment", "Username: " + username);
                        Log.d("AnotherProfileFragment", "City: " + city);
                        Log.d("AnotherProfileFragment", "Profile Image URL: " + profileImageUrl);

                        tvUsername.setText(username != null && !username.isEmpty() ? username : "Unknown Username");
                        tvCity.setText(city != null && !city.isEmpty() ? city : "Unknown City");
                        tvProfilePosts.setText(String.valueOf(postCount != null ? postCount : 0));

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(getContext()).load(profileImageUrl).into(profileImageView);
                        } else {
                            profileImageView.setImageResource(R.drawable.default_profile_picture);
                        }
                    } else {
                        Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading profile", e);
                    Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupRecyclerView() {
        rvProfileContent.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostContentAdapter(postList, getContext()); // Используем конструктор с двумя параметрами
        rvProfileContent.setAdapter(adapter);
    }

    private void loadUserPosts() {
        postList.clear();
        keys.clear();
        adapter.notifyDataSetChanged();

        postsCollection.whereEqualTo("uid", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot postSnapshot : task.getResult()) {
                            PostReferenceModel model = postSnapshot.toObject(PostReferenceModel.class);
                            if (model != null) {
                                model.setId(postSnapshot.getId());

                                profilesCollection.document(userId).get().addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        model.setProfileImageUrl(documentSnapshot.getString("profileImageUrl"));
                                        model.setUsername(documentSnapshot.getString("username"));
                                        model.setCity(documentSnapshot.getString("city"));
                                    }

                                    if (!keys.contains(postSnapshot.getId())) {
                                        postList.add(model);
                                        keys.add(postSnapshot.getId());
                                        sortPostsByDate();
                                        adapter.notifyItemInserted(postList.size() - 1);
                                    }
                                });
                            }
                        }
                        tvProfilePosts.setText(String.valueOf(postList.size()));
                    } else {
                        Log.e("AnotherProfileFragment", "Error loading posts", task.getException());
                    }
                });

        postsCollection.whereEqualTo("uid", userId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("AnotherProfileFragment", "Error listening for changes", error);
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        for (DocumentChange dc : snapshot.getDocumentChanges()) {
                            PostReferenceModel updatedPost = dc.getDocument().toObject(PostReferenceModel.class);
                            String postId = dc.getDocument().getId();

                            profilesCollection.document(userId).get().addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    updatedPost.setProfileImageUrl(documentSnapshot.getString("profileImageUrl"));
                                    updatedPost.setUsername(documentSnapshot.getString("username"));
                                    updatedPost.setCity(documentSnapshot.getString("city"));
                                }

                                switch (dc.getType()) {
                                    case ADDED:
                                        if (!keys.contains(postId)) {
                                            postList.add(0, updatedPost);
                                            keys.add(0, postId);
                                            adapter.notifyItemInserted(0);
                                            rvProfileContent.scrollToPosition(0);
                                        }
                                        break;
                                    case MODIFIED:
                                        int index = keys.indexOf(postId);
                                        if (index != -1) {
                                            postList.set(index, updatedPost);
                                            adapter.notifyItemChanged(index);
                                        } else {
                                            postList.add(0, updatedPost);
                                            keys.add(0, postId);
                                            adapter.notifyItemInserted(0);
                                        }
                                        break;
                                    case REMOVED:
                                        int removeIndex = keys.indexOf(postId);
                                        if (removeIndex != -1) {
                                            postList.remove(removeIndex);
                                            keys.remove(removeIndex);
                                            adapter.notifyItemRemoved(removeIndex);
                                        }
                                        break;
                                }
                                sortPostsByDate();
                            });
                        }
                        tvProfilePosts.setText(String.valueOf(postList.size()));
                    }
                });
    }

    private void sortPostsByDate() {
        Collections.sort(postList, (post1, post2) -> {
            if (post1.getTimestamp() == null || post2.getTimestamp() == null) return 0;
            return post2.getTimestamp().compareTo(post1.getTimestamp());
        });
    }

    private void startChatWithUser() {
        if (userId == null || userId.isEmpty()) {
            Log.e("AnotherProfileFragment", "User ID is null or empty");
            Toast.makeText(getContext(), "Error: User ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatFragment chatFragment = ChatFragment.newInstance(userId);
        Bundle args = new Bundle();
        args.putString("uid", userId);
        chatFragment.setArguments(args);

        Log.d("AnotherProfileFragment", "Starting chat with userId: " + userId);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.e("AnotherProfileFragment", "Activity is null");
        }
    }
}