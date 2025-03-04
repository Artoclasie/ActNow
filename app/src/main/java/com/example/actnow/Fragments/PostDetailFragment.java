package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class PostDetailFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String postId;
    private String currentUserId;
    private String authorUid; // UID автора поста
    private Button btnSignUp;
    private Button btnMessageAuthor; // Кнопка "Написать автору"
    private ArrayList<String> volunteers;

    private LinearLayout llVolunteersContainer; // Контейнер для аватарок волонтеров
    private ImageView imvAuthorAvatar; // Аватарка автора

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_detail, container, false);

        // Инициализация списка volunteers для текущего поста
        volunteers = new ArrayList<>();

        // Находим элементы интерфейса
        TextView tvTitle = rootView.findViewById(R.id.tv_post_title);
        ImageView imvPostImage = rootView.findViewById(R.id.imv_post_image);
        TextView tvGroup = rootView.findViewById(R.id.tv_post_group);
        TextView tvDate = rootView.findViewById(R.id.tv_post_date);
        TextView tvLocation = rootView.findViewById(R.id.tv_post_location);
        TextView tvAdditionalInfo = rootView.findViewById(R.id.tv_post_additional_info);
        imvAuthorAvatar = rootView.findViewById(R.id.imv_post_author);
        btnSignUp = rootView.findViewById(R.id.btn_sign_up);
        btnMessageAuthor = rootView.findViewById(R.id.btn_message_author);

        // Контейнер для аватарок волонтеров
        llVolunteersContainer = rootView.findViewById(R.id.ll_volunteers_container);

        // Получаем данные из Bundle
        Bundle args = getArguments();
        if (args != null) {
            postId = args.getString("postId");
            authorUid = args.getString("uid"); // Получаем UID автора
            tvTitle.setText(args.getString("title", "No Title"));

            String imageUrl = args.getString("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(imvPostImage);
            } else {
                imvPostImage.setImageResource(R.drawable.default_image);
            }

            tvGroup.setText(args.getString("city", "No City"));

            String timestamp = args.getString("date");
            if (timestamp != null) {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                try {
                    Date date = inputFormat.parse(timestamp);
                    String formattedDate = outputFormat.format(date);
                    tvDate.setText("Дата: " + formattedDate);
                } catch (ParseException e) {
                    tvDate.setText("Дата: " + timestamp);
                }
            } else {
                tvDate.setText("Дата: Не указана");
            }

            tvLocation.setText("Местоположение: " + args.getString("location", "No Location"));
            tvAdditionalInfo.setText(args.getString("content", "No Content"));

            String profileImageUrl = args.getString("profileImageUrl");
            Log.d("PostDetailFragment", "Profile image URL from Bundle: " + profileImageUrl); // Логирование

            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(this).load(profileImageUrl).into(imvAuthorAvatar);
            } else {
                imvAuthorAvatar.setImageResource(R.drawable.default_profile_picture);
                Log.d("PostDetailFragment", "Profile image URL is null or empty");
            }
        }

        // Проверяем, является ли текущий пользователь автором поста
        if (currentUserId.equals(authorUid)) {
            btnSignUp.setVisibility(View.GONE); // Скрываем кнопку "Записаться"
            btnMessageAuthor.setVisibility(View.GONE); // Скрываем кнопку "Написать автору"
        } else {
            // Инициализация состояния кнопки "Записаться"
            loadSubscriptionStatus();

            // Обработчики нажатий
            btnSignUp.setOnClickListener(v -> toggleSubscription());
            btnMessageAuthor.setOnClickListener(v -> startChatWithAuthor());
        }

        return rootView;
    }

    private void loadSubscriptionStatus() {
        if (postId == null) {
            Log.e("PostDetailFragment", "postId is null");
            return;
        }

        // Очищаем список волонтеров перед загрузкой новых данных
        volunteers = new ArrayList<>();

        // Загружаем данные из Firestore по postId
        db.collection("Posts").document(postId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Получаем список волонтеров для текущего поста
                        volunteers = (ArrayList<String>) documentSnapshot.get("volunteers");
                        if (volunteers == null) {
                            volunteers = new ArrayList<>(); // Инициализируем пустой список, если волонтеров нет
                        }

                        // Обновляем кнопку и аватарки
                        updateButtonState();
                        updateVolunteersAvatars();
                        Log.d("PostDetailFragment", "Loaded volunteers for post " + postId + ": " + volunteers);
                    } else {
                        Log.w("PostDetailFragment", "Post document not found for postId: " + postId);
                    }
                })
                .addOnFailureListener(e -> Log.e("PostDetailFragment", "Error loading volunteers", e));
    }

    private void toggleSubscription() {
        if (postId == null || currentUserId == null) {
            Log.e("PostDetailFragment", "postId or currentUserId is null");
            return;
        }

        if (volunteers.contains(currentUserId)) {
            // Если пользователь уже записан, отписываем его
            volunteers.remove(currentUserId);
        } else {
            // Если пользователь не записан, добавляем его
            volunteers.add(currentUserId);
        }

        // Обновляем список волонтеров в Firestore
        db.collection("Posts").document(postId)
                .update("volunteers", volunteers)
                .addOnSuccessListener(aVoid -> {
                    updateButtonState();
                    updateVolunteersAvatars(); // Обновляем аватарки волонтеров
                    Log.d("PostDetailFragment", "Volunteers updated for post " + postId);
                })
                .addOnFailureListener(e -> Log.e("PostDetailFragment", "Error updating volunteers", e));
    }

    private void updateButtonState() {
        if (volunteers.contains(currentUserId)) {
            btnSignUp.setText("Отписаться");
        } else {
            btnSignUp.setText("Записаться");
        }
    }

    private void startChatWithAuthor() {
        if (authorUid == null || authorUid.isEmpty()) {
            Log.e("PostDetailFragment", "Author UID is null or empty");
            return;
        }

        ChatFragment chatFragment = ChatFragment.newInstance(authorUid);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, chatFragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.e("PostDetailFragment", "Activity is null");
        }
    }

    private void updateVolunteersAvatars() {
        // Очищаем контейнер перед добавлением новых аватарок
        llVolunteersContainer.removeAllViews();

        // Если волонтеров нет, скрываем контейнер и выходим из метода
        if (volunteers.isEmpty()) {
            llVolunteersContainer.setVisibility(View.GONE); // Скрываем контейнер
            Log.d("PostDetailFragment", "No volunteers to display");
            return;
        }

        // Показываем контейнер, если есть волонтеры
        llVolunteersContainer.setVisibility(View.VISIBLE);

        // Добавляем аватарки волонтеров только для текущего поста
        for (String volunteerId : volunteers) {
            // Создаем CardView для аватарки волонтера
            CardView cardView = (CardView) LayoutInflater.from(getContext())
                    .inflate(R.layout.cv_volunteer_avatar, llVolunteersContainer, false);

            ImageView imvVolunteerAvatar = cardView.findViewById(R.id.imv_volunteer_avatar);

            // Загружаем аватарку волонтера
            FirebaseFirestore.getInstance().collection("Profiles").document(volunteerId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String avatarUrl = documentSnapshot.getString("profileImageUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(this).load(avatarUrl).into(imvVolunteerAvatar);
                            } else {
                                imvVolunteerAvatar.setImageResource(R.drawable.default_profile_picture);
                            }
                        } else {
                            imvVolunteerAvatar.setImageResource(R.drawable.default_profile_picture);
                        }
                    })
                    .addOnFailureListener(e -> {
                        imvVolunteerAvatar.setImageResource(R.drawable.default_profile_picture);
                        Log.e("PostDetailFragment", "Error loading volunteer avatar", e);
                    });

            // Обработчик нажатия на аватарку волонтера
            cardView.setOnClickListener(v -> {
                // Проверяем, что это не профиль текущего пользователя
                if (!volunteerId.equals(currentUserId)) {
                    // Переходим в профиль волонтера
                    AnotherProfileFragment anotherProfileFragment = AnotherProfileFragment.newInstance(volunteerId);
                    if (getActivity() != null) {
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, anotherProfileFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                } else {
                    Log.d("PostDetailFragment", "Cannot open own profile from volunteers list");
                }
            });

            // Добавляем CardView в контейнер
            llVolunteersContainer.addView(cardView);
        }
    }
}