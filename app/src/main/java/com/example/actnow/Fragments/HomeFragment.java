package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Adapters.PostContentAdapter;
import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {

    private RecyclerView rv_posts;
    private PostContentAdapter postContentAdapter; // Объявляем как поле класса
    private FirebaseFirestore db;
    private ArrayList<PostReferenceModel> postList = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Инициализация Firestore
        db = FirebaseFirestore.getInstance();

        // Инициализация RecyclerView
        rv_posts = view.findViewById(R.id.rv_news);
        rv_posts.setLayoutManager(new LinearLayoutManager(getContext()));

        // Инициализация списка постов и адаптера
        postContentAdapter = new PostContentAdapter(postList, getContext()); // Используем конструктор с двумя параметрами
        rv_posts.setAdapter(postContentAdapter);

        // Загрузка постов
        loadPosts();

        return view;
    }

    private void loadPosts() {
        CollectionReference postsRef = db.collection("Posts");

        postsRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("HomeFragment", "Error: " + error.getMessage());
                return;
            }

            if (snapshot != null) {
                // Сохраняем текущую позицию прокрутки и смещение
                LinearLayoutManager layoutManager = (LinearLayoutManager) rv_posts.getLayoutManager();
                int scrollPosition = layoutManager.findFirstVisibleItemPosition();
                View firstVisibleView = layoutManager.findViewByPosition(scrollPosition);
                int offset = (firstVisibleView == null) ? 0 : (firstVisibleView.getTop() - layoutManager.getPaddingTop());

                // Очищаем список перед загрузкой новых данных
                postList.clear();
                keys.clear();

                // Загружаем все посты заново
                for (QueryDocumentSnapshot postSnapshot : snapshot) {
                    PostReferenceModel post = postSnapshot.toObject(PostReferenceModel.class);
                    String postId = postSnapshot.getId();

                    db.collection("Profiles").document(post.getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    post.setUsername(documentSnapshot.getString("username"));
                                    post.setProfileImageUrl(documentSnapshot.getString("profileImageUrl"));
                                    post.setCity(documentSnapshot.getString("city"));
                                    post.setId(postId); // Устанавливаем ID

                                    postList.add(post);
                                    keys.add(postId);
                                    Log.d("HomeFragment", "Loaded post: " + postId);
                                } else {
                                    Log.w("HomeFragment", "Profile not found for post: " + postId);
                                }

                                // Выполняем сортировку и обновление адаптера после загрузки всех данных
                                if (postList.size() == snapshot.size()) {
                                    sortPostsByDate();
                                    postContentAdapter.notifyDataSetChanged();
                                    rv_posts.post(() -> {
                                        if (scrollPosition != RecyclerView.NO_POSITION && scrollPosition < postList.size()) {
                                            layoutManager.scrollToPositionWithOffset(scrollPosition, offset);
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("HomeFragment", "Error loading user data for post " + postId + ": " + e.getMessage());
                            });
                }
            }
        });
    }

    private void sortPostsByDate() {
        Collections.sort(postList, (post1, post2) -> {
            // Сортируем по timestamp, чтобы последний пост был первым
            if (post1.getTimestamp() == null || post2.getTimestamp() == null) {
                return 0; // Если timestamp отсутствует, не меняем порядок
            }
            return post2.getTimestamp().compareTo(post1.getTimestamp()); // Сортировка по убыванию
        });
    }
}