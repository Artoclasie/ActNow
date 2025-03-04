package com.example.actnow.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.actnow.Adapters.PostContentAdapter;
import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.R;
import com.example.actnow.RegistrActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private ImageView profileImageView;
    private TextView tvProfilePosts, tvProfileUname, tvProfileCity;
    private RecyclerView rvProfilePosts;
    private ImageButton menuButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private CollectionReference profilesRef;

    private ArrayList<PostReferenceModel> postList = new ArrayList<>();
    private ArrayList<String> keys = new ArrayList<>();
    private PostContentAdapter adapter;

    private Cloudinary cloudinary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View item = inflater.inflate(R.layout.fragment_profile, container, false);

        // Инициализация элементов
        profileImageView = item.findViewById(R.id.profileImageView);
        tvProfilePosts = item.findViewById(R.id.tv_profile_posts);
        tvProfileUname = item.findViewById(R.id.tv_profile_uname);
        tvProfileCity = item.findViewById(R.id.tv_profile_city);
        rvProfilePosts = item.findViewById(R.id.rv_profile_posts);
        menuButton = item.findViewById(R.id.menu_button);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        profilesRef = firebaseFirestore.collection("Profiles");

        // Инициализация Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dolnejlw3",
                "api_key", "869237653654212",
                "api_secret", "E1GjuqHeNAaEFwXIsH6BM9KRnTA"
        ));

        return item;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAuth.getCurrentUser() != null) {
            loadUserProfile();
            setupRecyclerView();
            loadUserPosts();
        }

        // Обработчик выпадающего меню
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> showPopupMenu(v));
        } else {
            Log.e("ProfileFragment", "menuButton is null, check layout");
        }
    }

    // Лаунчер выбора изображения
    ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    profileImageView.setImageURI(uri);
                    uploadImageToCloudinary(uri);
                }
            });

    private void loadUserProfile() {
        String userId = mAuth.getCurrentUser().getUid();

        profilesRef.document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e("ProfileFragment", "Ошибка при отслеживании профиля", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        tvProfileUname.setText(documentSnapshot.getString("username") != null ? documentSnapshot.getString("username") : "최무진");
                        tvProfileCity.setText(documentSnapshot.getString("city") != null ? documentSnapshot.getString("city") : "Город");

                        String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.default_profile_picture)
                                    .into(profileImageView);
                        } else {
                            Glide.with(getContext())
                                    .load(R.drawable.default_profile_picture)
                                    .into(profileImageView);
                        }

                        Long postCount = documentSnapshot.getLong("countPost");
                        tvProfilePosts.setText(String.valueOf(postCount != null ? postCount : 0));
                    } else {
                        Toast.makeText(getContext(), "Профиль не найден", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupRecyclerView() {
        rvProfilePosts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostContentAdapter(postList, getContext(), this); // Передаём this
        rvProfilePosts.setAdapter(adapter);
    }

    public void loadUserPosts() {
        String uid = mAuth.getCurrentUser().getUid();
        CollectionReference postsRef = FirebaseFirestore.getInstance().collection("Posts");

        // Загружаем данные профиля один раз
        profilesRef.document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                String username = documentSnapshot.getString("username");
                String city = documentSnapshot.getString("city");

                // Загружаем посты
                postsRef.whereEqualTo("uid", uid)
                        .addSnapshotListener((snapshot, error) -> {
                            if (error != null) {
                                Log.e("ProfileFragment", "Ошибка при отслеживании изменений", error);
                                return;
                            }

                            if (snapshot != null) {
                                // Очищаем список перед добавлением новых данных
                                postList.clear();
                                keys.clear();

                                // Загружаем все посты заново
                                for (QueryDocumentSnapshot postSnapshot : snapshot) {
                                    PostReferenceModel post = postSnapshot.toObject(PostReferenceModel.class);
                                    String postId = postSnapshot.getId();

                                    post.setProfileImageUrl(profileImageUrl);
                                    post.setUsername(username);
                                    post.setCity(city);

                                    postList.add(post); // Добавляем пост в список
                                    keys.add(postId);
                                }

                                // Сортируем посты по дате
                                sortPostsByDate();
                                adapter.notifyDataSetChanged(); // Уведомляем адаптер об изменении данных
                            }
                        });
            }
        });
    }

    // Метод для сортировки постов по дате
    private void sortPostsByDate() {
        Collections.sort(postList, (post1, post2) -> {
            // Сортируем по timestamp, чтобы последний пост был первым
            if (post1.getTimestamp() == null || post2.getTimestamp() == null) {
                return 0; // Если timestamp отсутствует, не меняем порядок
            }
            return post2.getTimestamp().compareTo(post1.getTimestamp()); // Сортировка по убыванию
        });
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_change_avatar) {
                pickVisualMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
                return true;
            } else if (item.getItemId() == R.id.action_out) {
                // Выход из аккаунта
                mAuth.signOut();

                // Переход на экран регистрации или авторизации
                Intent intent = new Intent(getActivity(), RegistrActivity.class);
                startActivity(intent);

                // Закрытие текущей активности
                if (getActivity() != null) {
                    getActivity().finish();
                }
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        if (imageUri == null) return;

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] imageData = byteArrayOutputStream.toByteArray();

            new Thread(() -> {
                try {
                    Map uploadResult = cloudinary.uploader().upload(imageData, ObjectUtils.emptyMap());
                    String uploadedImageUrl = (String) uploadResult.get("secure_url");

                    if (uploadedImageUrl != null) {
                        updateProfileImageUrl(uploadedImageUrl);
                    }
                } catch (IOException e) {
                    Log.e("ProfileFragment", "Ошибка загрузки изображения", e);
                }
            }).start();
        } catch (IOException e) {
            Log.e("ProfileFragment", "Ошибка обработки изображения", e);
        }
    }

    private void updateProfileImageUrl(String imageUrl) {
        String userId = mAuth.getCurrentUser().getUid();
        profilesRef.document(userId)
                .update("profileImageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    Glide.with(getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(profileImageView);
                })
                .addOnFailureListener(e -> Log.e("ProfileFragment", "Ошибка обновления аватарки", e));
    }

    private void showDeletePostDialog() {
        // Эта логика больше не нужна, так как удаление управляется адаптером
    }

    private void deletePost(String postId) {
        // Эта логика больше не нужна, так как удаление управляется адаптером
    }

    private void updatePostCount() {
        // Эта логика больше не нужна напрямую, обновление происходит через адаптер
    }
}