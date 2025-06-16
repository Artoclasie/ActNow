package com.example.actnow.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.actnow.Adapters.CommentAdapter;
import com.example.actnow.Models.CommentModel;
import com.example.actnow.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProfileCommentsFragment extends Fragment implements CommentAdapter.OnCommentInteractionListener {

    private static final String TAG = "ProfileCommentsFragment";
    private static final int COMMENT_LIMIT = 20;

    private FirebaseFirestore db;
    private String userId;
    private TextView tvEmptyState;
    private RecyclerView rvComments;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CommentAdapter commentAdapter;
    private List<CommentModel> commentList = new ArrayList<>(); // Общий список
    private Set<String> processedEventIds = new HashSet<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        Bundle args = getArguments();
        userId = args != null ? args.getString("userId") : null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_comments, container, false);
        initViews(view);
        setupRecyclerView();
        loadInitialComments();
        return view;
    }

    private void initViews(View view) {
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        rvComments = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::loadInitialComments);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(getContext(), commentList, this); // Передаем общий список
        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        rvComments.setAdapter(commentAdapter);
    }

    private void loadInitialComments() {
        if (!isAdded() || userId == null) {
            Log.e(TAG, "userId is null or fragment not added!");
            tvEmptyState.setText("Ошибка: пользователь не авторизован");
            tvEmptyState.setVisibility(View.VISIBLE);
            rvComments.setVisibility(View.GONE);
            return;
        }

        commentList.clear(); // Очищаем общий список
        processedEventIds.clear();
        swipeRefreshLayout.setRefreshing(true);
        Log.d(TAG, "Loading initial comments for userId: " + userId);

        db.collection("events")
                .limit(10)
                .get()
                .addOnCompleteListener(eventTask -> {
                    if (isAdded() && eventTask.isSuccessful()) {
                        Log.d(TAG, "Events query successful, events found: " + eventTask.getResult().size());
                        if (!eventTask.getResult().isEmpty()) {
                            for (DocumentSnapshot eventDoc : eventTask.getResult()) {
                                String eventId = eventDoc.getId();
                                if (!processedEventIds.contains(eventId)) {
                                    processedEventIds.add(eventId);
                                    loadCommentsForEvent(eventId);
                                }
                            }
                        } else {
                            Log.d(TAG, "No events found");
                            updateUI();
                        }
                    } else {
                        Log.e(TAG, "Error loading events", eventTask.getException());
                        tvEmptyState.setText("Ошибка загрузки событий");
                        tvEmptyState.setVisibility(View.VISIBLE);
                        rvComments.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void loadCommentsForEvent(String eventId) {
        db.collection("events")
                .document(eventId)
                .collection("Comments")
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(COMMENT_LIMIT)
                .get()
                .addOnCompleteListener(commentTask -> {
                    if (isAdded() && commentTask.isSuccessful()) {
                        Log.d(TAG, "Comments query for event " + eventId + " successful, comments found: " + commentTask.getResult().size());
                        for (DocumentSnapshot document : commentTask.getResult()) {
                            try {
                                CommentModel comment = document.toObject(CommentModel.class);
                                if (comment != null) {
                                    comment.setCommentId(document.getId());
                                    comment.setEventId(eventId);
                                    // Асинхронно загружаем имя автора
                                    loadAuthorDetails(comment);
                                    commentList.add(comment);
                                    Log.d(TAG, "Loaded comment: " + comment.getContent() + ", eventId: " + eventId + ", authorId: " + comment.getAuthorId());
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing comment from document: " + document.getId(), e);
                            }
                        }
                        updateUI();
                    } else {
                        Log.e(TAG, "Error loading comments for event " + eventId, commentTask.getException());
                    }
                });
    }

    private void loadAuthorDetails(CommentModel comment) {
        db.collection("Profiles")
                .document(comment.getAuthorId())
                .get()
                .addOnSuccessListener(profileSnapshot -> {
                    if (!isAdded() || profileSnapshot == null) return;
                    if (profileSnapshot.exists()) {
                        String authorName = profileSnapshot.getString("username");
                        String profileImage = profileSnapshot.getString("avatarUrl");
                        comment.setAuthorName(authorName != null ? authorName : "Аноним");
                        comment.setProfileImage(profileImage != null ? profileImage : "");
                        int index = commentList.indexOf(comment);
                        if (index != -1) {
                            commentList.set(index, comment);
                            getActivity().runOnUiThread(() -> {
                                commentAdapter.notifyItemChanged(index); // Обновляем элемент после загрузки автора
                                Log.d(TAG, "Updated author details for commentId: " + comment.getCommentId());
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    comment.setAuthorName("Аноним");
                    comment.setProfileImage("");
                    int index = commentList.indexOf(comment);
                    if (index != -1) {
                        commentList.set(index, comment);
                        getActivity().runOnUiThread(() -> commentAdapter.notifyItemChanged(index));
                        Log.w(TAG, "Failed to load author details for commentId: " + comment.getCommentId(), e);
                    }
                });
    }

    private void updateUI() {
        getActivity().runOnUiThread(() -> {
            Log.d(TAG, "Updating UI with " + commentList.size() + " comments");
            commentAdapter.notifyDataSetChanged(); // Обновляем адаптер
            if (commentList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("Нет комментариев");
                rvComments.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                rvComments.setVisibility(View.VISIBLE);
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onLikeComment(final String commentId, final boolean isLiked, final CommentModel comment, final CommentAdapter.CommentViewHolder holder) {
        if (!isAdded() || userId == null) {
            Log.e(TAG, "Fragment not added or userId is null");
            return;
        }

        // Сохраняем текущее состояние перед изменением
        final int initialLikesCount = comment.getLikesCount();
        final boolean initialLikedState = comment.isLikedBy(userId);

        // Подготовка данных для обновления
        final int newLikesCount;
        if (isLiked) {
            if (!initialLikedState) {
                newLikesCount = initialLikesCount + 1;
            } else {
                newLikesCount = initialLikesCount; // Нет изменений, если уже лайкнуто
                return; // Выходим, чтобы избежать ненужного запроса
            }
        } else {
            if (initialLikedState) {
                newLikesCount = Math.max(0, initialLikesCount - 1);
            } else {
                newLikesCount = initialLikesCount; // Нет изменений, если не лайкнуто
                return; // Выходим, чтобы избежать ненужного запроса
            }
        }

        Map<String, Object> updates = new HashMap<>();
        if (isLiked) {
            comment.addLike(userId);
            updates.put("likes", comment.getLikes());
        } else {
            comment.removeLike(userId);
            updates.put("likes", comment.getLikes());
        }
        updates.put("likesCount", newLikesCount);

        db.collection("events")
                .document(comment.getEventId())
                .collection("Comments")
                .document(commentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    // Обновляем локальную модель и UI только после успеха
                    int index = commentList.indexOf(comment);
                    if (index != -1) {
                        comment.setLikesCount(newLikesCount);
                        commentList.set(index, comment);
                        getActivity().runOnUiThread(() -> {
                            holder.likeButton.setImageResource(isLiked ? R.drawable.ic_like : R.drawable.ic_dislike); // Обновляем иконку
                            holder.likesCountText.setText(String.valueOf(newLikesCount)); // Обновляем количество
                            commentAdapter.notifyItemChanged(index); // Обновляем UI
                            Log.d(TAG, "Like updated successfully for commentId: " + commentId);
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    // Восстанавливаем предыдущее состояние в случае ошибки
                    int index = commentList.indexOf(comment);
                    if (index != -1) {
                        comment.setLikesCount(initialLikesCount);
                        if (initialLikedState) comment.addLike(userId);
                        else comment.removeLike(userId);
                        commentList.set(index, comment);
                        getActivity().runOnUiThread(() -> {
                            holder.likeButton.setImageResource(initialLikedState ? R.drawable.ic_like : R.drawable.ic_dislike); // Восстанавливаем иконку
                            holder.likesCountText.setText(String.valueOf(initialLikesCount)); // Восстанавливаем количество
                            commentAdapter.notifyItemChanged(index); // Обновляем UI
                            Toast.makeText(getContext(), "Ошибка обновления лайков", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error updating likes for commentId: " + commentId, e);
                        });
                    }
                });
    }

    @Override
    public void onDeleteComment(CommentModel comment) {
        if (!isAdded() || userId == null || !userId.equals(comment.getAuthorId())) {
            Log.e(TAG, "Cannot delete comment: user not authorized or not author");
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Удалить комментарий")
                .setMessage("Вы уверены, что хотите удалить этот комментарий?")
                .setPositiveButton("Да", (dialog, which) -> {
                    db.collection("events")
                            .document(comment.getEventId())
                            .collection("Comments")
                            .document(comment.getCommentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded()) return;
                                // Удаляем из локального списка
                                int index = commentList.indexOf(comment);
                                if (index != -1) {
                                    commentList.remove(index);
                                    getActivity().runOnUiThread(() -> {
                                        commentAdapter.notifyItemRemoved(index); // Уведомляем адаптер об удалении
                                        updateUI(); // Обновляем видимость пустого состояния
                                        Log.d(TAG, "Comment deleted successfully: " + comment.getCommentId());
                                        Toast.makeText(getContext(), "Комментарий удален", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                Log.e(TAG, "Error deleting comment: " + comment.getCommentId(), e);
                                Toast.makeText(getContext(), "Ошибка удаления комментария", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Нет", null)
                .show();
    }
}