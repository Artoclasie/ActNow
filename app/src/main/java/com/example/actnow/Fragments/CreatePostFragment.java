package com.example.actnow.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CreatePostFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String TAG = "CreatePostFragment";

    private TextView tv_char_count;
    private EditText etContent;
    private ImageView imvPostImage;
    private ImageButton imbAddImage, imbDeleteImage;
    private Button btnPost, btnCancel;
    private Bitmap selectedImageBitmap;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private NestedScrollView scrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Инициализация всех View
        etContent = view.findViewById(R.id.et_create_content);
        imvPostImage = view.findViewById(R.id.imv_post_image);
        imbAddImage = view.findViewById(R.id.btn_add_photo);
        imbDeleteImage = view.findViewById(R.id.btn_delete_photo);
        btnPost = view.findViewById(R.id.btn_post);
        btnCancel = view.findViewById(R.id.btn_cancel);
        tv_char_count = view.findViewById(R.id.tv_char_count);
        scrollView = view.findViewById(R.id.scroll_view);

        // Настройка слушателей
        imbAddImage.setOnClickListener(v -> openImagePicker());
        imbDeleteImage.setOnClickListener(v -> removeImage());
        btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        // Обработчик кнопки публикации
        btnPost.setOnClickListener(v -> {
            Log.d(TAG, "Post button clicked");
            createPost();
        });

        // Валидация текста с подсчетом символов
        etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tv_char_count.setText(length + "/250");

                if (length < 3) {
                    tv_char_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
                } else if (length > 250) {
                    tv_char_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
                } else {
                    tv_char_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
                }

                btnPost.setEnabled(length >= 3 && length <= 250);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Изначальное состояние
        imbDeleteImage.setVisibility(View.GONE);
        btnPost.setEnabled(false);
        tv_char_count.setText("0/250");

        // Обработка появления клавиатуры
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            int keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            if (keyboardHeight > 0 && etContent.hasFocus()) {
                scrollView.post(() -> {
                    int[] location = new int[2];
                    etContent.getLocationOnScreen(location);
                    int editTextTop = location[1];
                    int scrollTo = Math.max(0, editTextTop - keyboardHeight + etContent.getHeight());
                    scrollView.smoothScrollTo(0, scrollTo);
                    Log.d(TAG, "Keyboard height: " + keyboardHeight + ", Scrolled to: " + scrollTo);
                });
            }
            return insets;
        });

        etContent.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                scrollView.post(() -> {
                    int[] location = new int[2];
                    etContent.getLocationOnScreen(location);
                    int editTextTop = location[1];
                    int keyboardHeight = getKeyboardHeightEstimate();
                    int scrollTo = Math.max(0, editTextTop - keyboardHeight + etContent.getHeight());
                    scrollView.smoothScrollTo(0, scrollTo);
                    Log.d(TAG, "Focus scroll to: " + scrollTo);
                });
            }
        });
    }

    private int getKeyboardHeightEstimate() {
        return 300;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), selectedImageUri);
                imvPostImage.setImageBitmap(selectedImageBitmap);
                imvPostImage.setVisibility(View.VISIBLE);
                imbDeleteImage.setVisibility(View.VISIBLE);
                imbAddImage.setVisibility(View.GONE);
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Image loading error", e);
            }
        }
    }

    private void removeImage() {
        selectedImageBitmap = null;
        selectedImageUri = null;
        imvPostImage.setImageDrawable(null);
        imvPostImage.setVisibility(View.GONE);
        imbDeleteImage.setVisibility(View.GONE);
        imbAddImage.setVisibility(View.VISIBLE);
    }

    private void validateForm() {
        String content = etContent.getText().toString().trim();
        int length = content.length();

        tv_char_count.setText(length + "/250");

        boolean isValid = length >= 3 && length <= 250;
        btnPost.setEnabled(isValid);

        if (length < 3) {
            tv_char_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
        } else if (length > 250) {
            tv_char_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
        } else {
            tv_char_count.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
        }
    }

    private void createPost() {
        String content = etContent.getText().toString().trim();

        if (content.length() < 3) {
            Toast.makeText(getContext(), "Минимум 3 символа", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPost.setEnabled(false);

        if (selectedImageBitmap != null) {
            uploadImageAndCreatePost(content, mAuth.getCurrentUser().getUid());
        } else {
            createPostInFirestore(content, mAuth.getCurrentUser().getUid(), null);
        }
    }

    private void uploadImageAndCreatePost(String content, String userId) {
        new Thread(() -> {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();

                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", "dolnejlw3",
                        "api_key", "869237653654212",
                        "api_secret", "E1GjuqHeNAaEFwXIsH6BM9KRnTA"
                ));

                Map<String, Object> uploadResult = cloudinary.uploader().upload(imageBytes, ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");

                requireActivity().runOnUiThread(() -> createPostInFirestore(content, userId, imageUrl));
            } catch (Exception e) {
                Log.e(TAG, "Image upload failed", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                });
            }
        }).start();
    }

    private void createPostInFirestore(String content, String userId, String imageUrl) {
        PostReferenceModel post = new PostReferenceModel();
        post.setAuthorId(userId);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setLikesCount(0);
        post.setCreatedAt(new Timestamp(new Date(System.currentTimeMillis()))); // Конвертируем long в Timestamp
        post.setDate(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));

        db.collection("Posts")
                .add(post.toMap())
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Post created with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Post created successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating post", e);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnPost.setEnabled(true);
                });
    }
}