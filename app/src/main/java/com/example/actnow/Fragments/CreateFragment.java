package com.example.actnow.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateFragment extends Fragment {

    private EditText et_title, et_location, et_date, et_create_content;
    private ImageButton imb_image, imb_image_delete, imb_delete_post;
    private ImageView imv_post_image;
    private Button btn_post;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private Bitmap bitmap;

    // Лаунчер для выбора изображения из галереи
    ActivityResultLauncher<PickVisualMediaRequest> pickVisualMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    imv_post_image.setImageURI(uri);
                    imv_post_image.setVisibility(View.VISIBLE);
                    imb_image_delete.setVisibility(View.VISIBLE);

                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Кнопка для выбора изображения
        imb_image.setOnClickListener(v ->
                pickVisualMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        // Кнопка для удаления выбранного изображения
        imb_image_delete.setOnClickListener(v -> {
            imv_post_image.setImageDrawable(null);
            imv_post_image.setVisibility(View.GONE);
            imb_image_delete.setVisibility(View.GONE);
            bitmap = null;
        });

        // Обработчик для публикации поста
        btn_post.setOnClickListener(v -> {
            if (et_create_content.getText().toString().trim().isEmpty() && (imv_post_image.getDrawable() == null)) {
                Toast.makeText(getContext(), "Cannot Create Post, No Content Entered", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String userId = user.getUid();
                    String title = et_title.getText().toString().trim();
                    String location = et_location.getText().toString().trim();
                    String date = et_date.getText().toString().trim();
                    String content = et_create_content.getText().toString().trim();

                    // Создаем мапу для поста
                    Map<String, Object> postMap = new HashMap<>();
                    postMap.put("uid", userId);
                    postMap.put("title", title);
                    postMap.put("location", location);
                    postMap.put("date", date); // Дата проведения из календарика
                    postMap.put("content", content);
                    postMap.put("likes", new ArrayList<String>());
                    postMap.put("comments", new ArrayList<String>());
                    postMap.put("likesCount", 0);
                    postMap.put("volunteers", new ArrayList<String>()); // Поле volunteers как пустой список

                    // Форматирование даты создания поста в ISO 8601
                    String timestampString = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss", Locale.getDefault()).format(new Date());
                    postMap.put("timestamp", timestampString);

                    // Получаем имя пользователя, аватарку и город из коллекции "Profiles"
                    firestore.collection("Profiles")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String username = documentSnapshot.getString("username");
                                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                                    String city = documentSnapshot.getString("city");
                                    postMap.put("username", username);
                                    postMap.put("profileImageUrl", profileImageUrl);
                                    postMap.put("city", city);

                                    if (imv_post_image.getDrawable() != null && bitmap != null) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setMessage("Post Being Uploaded, Please Wait!");
                                        builder.setCancelable(false);
                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();

                                        uploadImageToCloudinary(bitmap, postMap, alertDialog);
                                    } else {
                                        String postId = firestore.collection("Posts").document().getId();
                                        postMap.put("id", postId);
                                        savePostToFirestoreAndRealtime(postMap, postId);
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to get username", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        // Обработчик для выбора даты проведения через календарик
        et_date.setOnClickListener(v -> showDatePickerDialog());
        et_date.setFocusable(false); // Отключаем ручной ввод, чтобы использовать только календарик
        et_date.setKeyListener(null); // Блокируем клавиатуру

        // Обработчик для удаления поста (если требуется)
        imb_delete_post.setOnClickListener(v -> {
            // Логика для удаления поста
        });
    }

    // Метод для показа DatePickerDialog
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (DatePicker view, int year1, int month1, int dayOfMonth) -> {
                    // Форматируем выбранную дату в формат "dd/MM/yyyy"
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                    et_date.setText(selectedDate);
                }, year, month, day);

        // Устанавливаем минимальную дату (сегодня)
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    // Метод загрузки изображения в Cloudinary
    private void uploadImageToCloudinary(Bitmap bitmap, Map<String, Object> postMap, AlertDialog alertDialog) {
        new Thread(() -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();

                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", "dolnejlw3",
                        "api_key", "869237653654212",
                        "api_secret", "E1GjuqHeNAaEFwXIsH6BM9KRnTA"
                ));

                Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.emptyMap());
                if (uploadResult.containsKey("secure_url")) {
                    String imageUrl = (String) uploadResult.get("secure_url");
                    postMap.put("imageUrl", imageUrl);

                    if (!postMap.containsKey("id")) {
                        String postId = firestore.collection("Posts").document().getId();
                        postMap.put("id", postId);
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            savePostToFirestoreAndRealtime(postMap, postMap.get("id").toString());
                            alertDialog.dismiss();
                        });
                    }
                }
            } catch (Exception e) {
                alertDialog.dismiss();
                Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
            }
        }).start();
    }

    // Метод сохранения поста в Firestore и в Realtime Database
    private void savePostToFirestoreAndRealtime(Map<String, Object> postMap, String postId) {
        firestore.collection("Posts")
                .document(postId)
                .set(postMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Post Created", Toast.LENGTH_SHORT).show();
                        et_title.setText("");
                        et_location.setText("");
                        et_date.setText("");
                        et_create_content.setText("");
                        imv_post_image.setImageDrawable(null);
                        imv_post_image.setVisibility(View.GONE);
                        imb_image_delete.setVisibility(View.GONE);

                        // Получаем текущего пользователя
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();

                            // Получаем текущие данные профиля пользователя из Firestore
                            firestore.collection("Profiles").document(userId)
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            // Получаем текущий счётчик постов
                                            long countPost = documentSnapshot.getLong("countPost");
                                            countPost++;

                                            // Обновляем значение countPost
                                            firestore.collection("Profiles").document(userId)
                                                    .update("countPost", countPost)
                                                    .addOnCompleteListener(updateTask -> {
                                                        if (updateTask.isSuccessful()) {
                                                            // Публикуем в Realtime Database
                                                            FirebaseDatabase.getInstance().getReference("Posts").child(postId)
                                                                    .setValue(postMap);
                                                        } else {
                                                            Toast.makeText(getContext(), "Failed to update countPost", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to get profile", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View item = inflater.inflate(R.layout.fragment_create, container, false);

        et_title = item.findViewById(R.id.et_title);
        et_location = item.findViewById(R.id.et_location);
        et_date = item.findViewById(R.id.et_date);
        et_create_content = item.findViewById(R.id.et_create_content);
        imb_image = item.findViewById(R.id.imb_image);
        imb_image_delete = item.findViewById(R.id.imb_delete_image);
        imv_post_image = item.findViewById(R.id.imv_post_image);
        btn_post = item.findViewById(R.id.btn_post);
        imb_delete_post = item.findViewById(R.id.imb_delete_post);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        return item;
    }
}