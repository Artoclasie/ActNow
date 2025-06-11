package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Adapters.FollowAdapter;
import com.example.actnow.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FollowListDetailFragment extends Fragment {

    private static final String TAG = "FollowListDetailFragment";
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_TYPE = "type"; // "following" или "followers"

    private String userId;
    private String type;
    private RecyclerView recyclerView;
    private FollowAdapter adapter;
    private List<Map<String, Object>> followList = new ArrayList<>();
    private TextView tvEmptyMessage;

    public static FollowListDetailFragment newInstance(String userId, String type) {
        FollowListDetailFragment fragment = new FollowListDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            type = getArguments().getString(ARG_TYPE);
            Log.d(TAG, "Received userId: " + userId + ", type: " + type);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_follow_list_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.rv_follow_list);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_message);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(null); // Очищаем старый адаптер
        adapter = new FollowAdapter(followList, requireContext());
        recyclerView.setAdapter(adapter);

        loadFollowData();
    }

    private void loadFollowData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Profiles").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Извлекаем stats как Map
                        @SuppressWarnings("unchecked")
                        Map<String, Object> stats = (Map<String, Object>) documentSnapshot.get("stats");
                        List<String> followIds = new ArrayList<>();
                        if (stats != null) {
                            @SuppressWarnings("unchecked")
                            List<String> ids = (List<String>) stats.get(type);
                            if (ids != null) {
                                followIds.addAll(ids);
                            }
                        }
                        Log.d(TAG, "Fetched " + type + " IDs: " + followIds);
                        if (followIds != null && !followIds.isEmpty()) {
                            followList.clear();
                            List<Task<?>> tasks = new ArrayList<>();
                            for (String id : followIds) {
                                Task<Map<String, Object>> task = db.collection("Profiles").document(id).get()
                                        .continueWith(task1 -> {
                                            if (task1.isSuccessful()) {
                                                return task1.getResult().getData();
                                            }
                                            return null;
                                        });
                                tasks.add(task);
                            }
                            Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
                                followList.clear();
                                for (Object result : results) {
                                    if (result != null) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> userData = (Map<String, Object>) result;
                                        followList.add(userData);
                                        Log.d(TAG, "Added user data: " + userData);
                                    }
                                }
                                adapter.updateData(followList);
                                tvEmptyMessage.setVisibility(View.GONE);
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Error fetching user data", e);
                                tvEmptyMessage.setText("Ошибка загрузки данных");
                                tvEmptyMessage.setVisibility(View.VISIBLE);
                            });
                        } else {
                            Log.d(TAG, "No " + type + " found for userId: " + userId);
                            tvEmptyMessage.setText(type.equals("followers") ? "Нет подписчиков" : "Нет подписок");
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e(TAG, "Document does not exist for userId: " + userId);
                        tvEmptyMessage.setText("Пользователь не найден");
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading " + type, e);
                    tvEmptyMessage.setText("Ошибка загрузки данных");
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                });
    }
}