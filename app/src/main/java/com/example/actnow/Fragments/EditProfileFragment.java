package com.example.actnow.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileFragment extends Fragment {
    private static final int PICK_AVATAR_REQUEST = 1;
    private static final int PICK_BACKGROUND_REQUEST = 2;
    private static final String TAG = "EditProfileFragment";

    private ImageView btnBack;
    private TextView btnSave;
    private ImageView profileImage;
    private ImageView btnChangeAvatar;
    private ImageView profileBackImage;
    private ImageView btnChangeBackground;
    private EditText etUsername, etBio, etLocation;
    private TextView tvBirthDateValue;
    private Timestamp selectedBirthDate;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private Uri selectedAvatarUri;
    private Uri selectedBackgroundUri;
    private Bitmap selectedAvatarBitmap;
    private Bitmap selectedBackgroundBitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        initViews(view);
        loadUserData();
        setListeners();
        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnSave = view.findViewById(R.id.btnSave);
        profileImage = view.findViewById(R.id.imv_post_uid);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        profileBackImage = view.findViewById(R.id.profileBackImageView);
        btnChangeBackground = view.findViewById(R.id.btnChangeBackground);
        etUsername = view.findViewById(R.id.etUsername);
        etBio = view.findViewById(R.id.etBio);
        etLocation = view.findViewById(R.id.etLocation);
        tvBirthDateValue = view.findViewById(R.id.tvBirthDateValue);
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        btnSave.setOnClickListener(v -> saveProfile());
        btnChangeAvatar.setOnClickListener(v -> openImagePicker(PICK_AVATAR_REQUEST));
        btnChangeBackground.setOnClickListener(v -> openImagePicker(PICK_BACKGROUND_REQUEST));
        tvBirthDateValue.setOnClickListener(v -> showDatePicker());
    }

    private void loadUserData() {
        if (currentUserId == null) return;

        DocumentReference userRef = db.collection("Profiles").document(currentUserId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                String bio = documentSnapshot.getString("bio");
                String city = documentSnapshot.getString("city");
                String avatarUrl = documentSnapshot.getString("avatarUrl");
                String backgroundImageUrl = documentSnapshot.getString("backgroundImageUrl");
                Timestamp birthDate = documentSnapshot.getTimestamp("birthDate");

                etUsername.setText(username != null ? username : "");
                etBio.setText(bio != null ? bio : "");
                etLocation.setText(city != null ? city : "");
                if (birthDate != null) {
                    selectedBirthDate = birthDate;
                    tvBirthDateValue.setText(formatTimestamp(birthDate));
                } else {
                    tvBirthDateValue.setText("Не указана");
                }

                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(this)
                            .load(avatarUrl)
                            .placeholder(R.drawable.default_profile_picture)
                            .into(profileImage);
                }

                if (backgroundImageUrl != null && !backgroundImageUrl.isEmpty()) {
                    Glide.with(this)
                            .load(backgroundImageUrl)
                            .placeholder(R.drawable.ava)
                            .into(profileBackImage);
                }
            } else {
                Toast.makeText(getContext(), "Профиль не найден", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading user data", e);
            Toast.makeText(getContext(), "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "Не указана";
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
        return sdf.format(date);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    selectedBirthDate = new Timestamp(selectedDate.getTime());
                    tvBirthDateValue.setText(formatTimestamp(selectedBirthDate));
                },
                year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(new Date().getTime()); // Запрет будущих дат
        datePickerDialog.show();
    }

    private void openImagePicker(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Выберите изображение"), requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            if (requestCode == PICK_AVATAR_REQUEST) {
                selectedAvatarUri = data.getData();
                try {
                    selectedAvatarBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedAvatarUri);
                    profileImage.setImageBitmap(selectedAvatarBitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading avatar", e);
                    Toast.makeText(getContext(), "Ошибка загрузки аватара: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == PICK_BACKGROUND_REQUEST) {
                selectedBackgroundUri = data.getData();
                try {
                    selectedBackgroundBitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedBackgroundUri);
                    profileBackImage.setImageBitmap(selectedBackgroundBitmap);
                } catch (IOException e) {
                    Log.e(TAG, "Error loading background", e);
                    Toast.makeText(getContext(), "Ошибка загрузки фона: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveProfile() {
        String username = etUsername.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String city = etLocation.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Введите имя");
            return;
        }

        btnSave.setEnabled(false);
        if (selectedAvatarBitmap != null || selectedBackgroundBitmap != null) {
            uploadImagesToCloudinary(username, bio, city);
        } else {
            updateProfile(username, bio, city, null, null);
        }
    }

    private void uploadImagesToCloudinary(String username, String bio, String city) {
        new Thread(() -> {
            try {
                Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                        "cloud_name", getString(R.string.cloudinary_cloud_name),
                        "api_key", getString(R.string.cloudinary_api_key),
                        "api_secret", getString(R.string.cloudinary_api_secret)
                ));

                String avatarUrl = null;
                String backgroundUrl = null;

                // Загрузка аватара, если выбран
                if (selectedAvatarBitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedAvatarBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] avatarBytes = baos.toByteArray();
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(avatarBytes, ObjectUtils.emptyMap());
                    if (uploadResult.containsKey("secure_url")) {
                        avatarUrl = (String) uploadResult.get("secure_url");
                    }
                }

                // Загрузка фона, если выбран
                if (selectedBackgroundBitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedBackgroundBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] backgroundBytes = baos.toByteArray();
                    Map<String, Object> uploadResult = cloudinary.uploader().upload(backgroundBytes, ObjectUtils.emptyMap());
                    if (uploadResult.containsKey("secure_url")) {
                        backgroundUrl = (String) uploadResult.get("secure_url");
                    }
                }

                String finalAvatarUrl = avatarUrl;
                String finalBackgroundUrl = backgroundUrl;
                requireActivity().runOnUiThread(() -> updateProfile(username, bio, city, finalAvatarUrl, finalBackgroundUrl));
            } catch (Exception e) {
                Log.e(TAG, "Error uploading images", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Ошибка загрузки изображений: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                });
            }
        }).start();
    }

    private void updateProfile(String username, String bio, String city, String avatarUrl, String backgroundUrl) {
        DocumentReference userRef = db.collection("Profiles").document(currentUserId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("bio", bio);
        updates.put("city", city);
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }
        if (backgroundUrl != null) {
            updates.put("backgroundImageUrl", backgroundUrl);
        }
        if (selectedBirthDate != null) {
            updates.put("birthDate", selectedBirthDate);
        }
        updates.put("updatedAt", Timestamp.now());

        userRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Профиль обновлен", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating profile", e);
                    Toast.makeText(getContext(), "Ошибка обновления профиля: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                });
    }
}