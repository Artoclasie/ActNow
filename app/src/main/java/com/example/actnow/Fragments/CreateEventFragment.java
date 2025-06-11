package com.example.actnow.Fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Adapters.GalleryImageAdapter;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class CreateEventFragment extends Fragment {

    private static final String TAG = "CreateEventFragment";
    private static final int MAX_GALLERY_IMAGES = 10;
    private static final int REQUEST_CODE_MAP = 1001;

    // UI элементы
    private TextInputEditText etTitle, etDescription, etMaxParticipants, etAddress, etMinAge;
    private TextInputEditText etStartDate, etEndDate, etStartTime, etEndTime;
    private ChipGroup chipGroupTags;
    private Button btnCancel, btnCreateEvent, btnSelectLocation;
    private ImageButton btnAddCoverPhoto, btnDeleteCoverPhoto, btnAddGalleryPhoto;
    private ImageView ivCoverPreview;
    private TextView tvGalleryCount, tvDescriptionCharCount;
    private RecyclerView rvGalleryImages;
    private GalleryImageAdapter galleryAdapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // Данные
    private Uri coverImageUri;
    private List<Uri> galleryImageUris;
    private List<Object> galleryImages;
    private Calendar startDate, endDate;
    private int startHour, startMinute, endHour, endMinute;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private GeoPoint selectedCoordinates;

    // Cloudinary
    private Cloudinary cloudinary;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> coverImageLauncher;
    private ActivityResultLauncher<Intent> galleryImageLauncher;

    public CreateEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        galleryImageUris = new ArrayList<>();
        galleryImages = new ArrayList<>();
        startDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
        endDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));

        // Initialize Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dolnejlw3",
                "api_key", "869237653654212",
                "api_secret", "E1GjuqHeNAaEFwXIsH6BM9KRnTA"
        ));

        // Launcher for cover image
        coverImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                coverImageUri = result.getData().getData();
                ivCoverPreview.setImageURI(coverImageUri);
                ivCoverPreview.setVisibility(View.VISIBLE);
                btnAddCoverPhoto.setVisibility(View.GONE);
                btnDeleteCoverPhoto.setVisibility(View.VISIBLE);
                validateInputs();
            }
        });

        // Launcher for gallery images
        galleryImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                int currentTotal = galleryImageUris.size();
                if (currentTotal >= MAX_GALLERY_IMAGES) {
                    Toast.makeText(getContext(), "Достигнут лимит в " + MAX_GALLERY_IMAGES + " изображений", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (result.getData().getClipData() != null) {
                    int count = Math.min(result.getData().getClipData().getItemCount(), MAX_GALLERY_IMAGES - currentTotal);
                    for (int i = 0; i < count; i++) {
                        Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                        if (uri != null && !galleryImageUris.contains(uri)) {
                            galleryImageUris.add(uri);
                            galleryImages.add(uri);
                        }
                    }
                } else if (result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && !galleryImageUris.contains(uri) && currentTotal < MAX_GALLERY_IMAGES) {
                        galleryImageUris.add(uri);
                        galleryImages.add(uri);
                    }
                }
                galleryAdapter.updateImages(galleryImages);
                updateGalleryCount();
                validateInputs();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_event, container, false);

        // Initialize UI components
        etTitle = view.findViewById(R.id.et_event_title);
        etDescription = view.findViewById(R.id.et_event_description);
        etMaxParticipants = view.findViewById(R.id.et_max_participants);
        etAddress = view.findViewById(R.id.et_address);
        etMinAge = view.findViewById(R.id.et_min_age);
        etStartDate = view.findViewById(R.id.et_start_date);
        etEndDate = view.findViewById(R.id.et_end_date);
        etStartTime = view.findViewById(R.id.et_start_time);
        etEndTime = view.findViewById(R.id.et_end_time);
        chipGroupTags = view.findViewById(R.id.chip_group_tags);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCreateEvent = view.findViewById(R.id.btn_create_event);
        btnSelectLocation = view.findViewById(R.id.btn_select_location);
        btnAddCoverPhoto = view.findViewById(R.id.btn_add_cover_photo);
        btnDeleteCoverPhoto = view.findViewById(R.id.btn_delete_cover_photo);
        btnAddGalleryPhoto = view.findViewById(R.id.btn_add_gallery_photo);
        ivCoverPreview = view.findViewById(R.id.iv_cover_preview);
        tvGalleryCount = view.findViewById(R.id.tv_gallery_count);
        tvDescriptionCharCount = view.findViewById(R.id.tv_description_char_count);
        rvGalleryImages = view.findViewById(R.id.rv_gallery_images);

        // Setup RecyclerView for gallery
        setupGalleryRecyclerView();

        // Set up listeners
        setupListeners();

        // Initial button state
        validateInputs();

        return view;
    }

    private void setupGalleryRecyclerView() {
        galleryImages.addAll(galleryImageUris);
        galleryAdapter = new GalleryImageAdapter(getContext(), galleryImages, position -> {
            galleryImageUris.remove(galleryImages.get(position));
            galleryImages.remove(position);
            galleryAdapter.updateImages(galleryImages);
            updateGalleryCount();
            validateInputs();
        });
        rvGalleryImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvGalleryImages.setAdapter(galleryAdapter);
    }

    private void setupListeners() {
        // Set up date pickers
        Calendar minDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
        minDate.set(2025, Calendar.APRIL, 15);
        Calendar maxDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
        maxDate.set(2025, Calendar.SEPTEMBER, 17);

        etStartDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, day) -> {
                        startDate.set(year, month, day);
                        etStartDate.setText(dateFormat.format(startDate.getTime()));
                        validateInputs();
                    },
                    startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            dialog.show();
        });

        etEndDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                    requireContext(),
                    (view1, year, month, day) -> {
                        endDate.set(year, month, day);
                        etEndDate.setText(dateFormat.format(endDate.getTime()));
                        validateInputs();
                    },
                    endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.get(Calendar.DAY_OF_MONTH)
            );
            dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            dialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
            dialog.show();
        });

        // Set up time pickers
        etStartTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    requireContext(),
                    (view1, hour, minute) -> {
                        if (hour < 9 || (hour == 19 && minute > 0) || hour > 19) {
                            Toast.makeText(getContext(), "Время должно быть с 09:00 до 19:00", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startHour = hour;
                        startMinute = minute;
                        etStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                        validateInputs();
                    },
                    startHour, startMinute, true
            );
            dialog.show();
        });

        etEndTime.setOnClickListener(v -> {
            TimePickerDialog dialog = new TimePickerDialog(
                    requireContext(),
                    (view1, hour, minute) -> {
                        if (hour < 9 || (hour == 19 && minute > 0) || hour > 19) {
                            Toast.makeText(getContext(), "Время должно быть с 09:00 до 19:00", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        endHour = hour;
                        endMinute = minute;
                        etEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
                        validateInputs();
                    },
                    endHour, endMinute, true
            );
            dialog.show();
        });

        // Set up select location button
        btnSelectLocation.setOnClickListener(v -> {
            MapFragment mapFragment = new MapFragment();
            Bundle args = new Bundle();
            args.putBoolean("isSelectMode", true);
            if (selectedCoordinates != null) {
                args.putDouble("lat", selectedCoordinates.getLatitude());
                args.putDouble("lng", selectedCoordinates.getLongitude());
            }
            mapFragment.setArguments(args);
            mapFragment.setTargetFragment(this, REQUEST_CODE_MAP);
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, mapFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        // Set up image upload buttons
        btnAddCoverPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            coverImageLauncher.launch(intent);
        });

        btnDeleteCoverPhoto.setOnClickListener(v -> {
            coverImageUri = null;
            ivCoverPreview.setImageDrawable(null);
            ivCoverPreview.setVisibility(View.GONE);
            btnAddCoverPhoto.setVisibility(View.VISIBLE);
            btnDeleteCoverPhoto.setVisibility(View.GONE);
            validateInputs();
        });

        btnAddGalleryPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryImageLauncher.launch(intent);
        });

        // Set up description character counter
        etDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                tvDescriptionCharCount.setText(s.length() + "/500");
                validateInputs();
            }
        });

        // Set up text change listeners for validation
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };
        etTitle.addTextChangedListener(validationWatcher);
        etMaxParticipants.addTextChangedListener(validationWatcher);
        etAddress.addTextChangedListener(validationWatcher);
        etMinAge.addTextChangedListener(validationWatcher);
        etStartDate.addTextChangedListener(validationWatcher);
        etEndDate.addTextChangedListener(validationWatcher);
        etStartTime.addTextChangedListener(validationWatcher);
        etEndTime.addTextChangedListener(validationWatcher);

        // Set up chip group listener
        chipGroupTags.setOnCheckedStateChangeListener((group, checkedIds) -> validateInputs());

        // Set up action buttons
        btnCancel.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnCreateEvent.setOnClickListener(v -> createEvent());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("latitude", 0);
            double lng = data.getDoubleExtra("longitude", 0);
            String address = data.getStringExtra("address");
            selectedCoordinates = new GeoPoint(lat, lng);
            etAddress.setText(address);
            validateInputs();
        }
    }

    private void validateInputs() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String maxParticipantsStr = etMaxParticipants.getText() != null ? etMaxParticipants.getText().toString().trim() : "";
        String startDateStr = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String endDateStr = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";
        String startTimeStr = etStartTime.getText() != null ? etStartTime.getText().toString().trim() : "";
        String endTimeStr = etEndTime.getText() != null ? etEndTime.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        String minAgeStr = etMinAge.getText() != null ? etMinAge.getText().toString().trim() : "";
        boolean hasTags = !chipGroupTags.getCheckedChipIds().isEmpty();

        // Validate title
        if (title.isEmpty()) {
            etTitle.setError("Введите название");
        } else {
            etTitle.setError(null);
        }

        // Validate description
        if (description.isEmpty()) {
            etDescription.setError("Введите описание");
        } else if (description.length() > 500) {
            etDescription.setError("Описание слишком длинное");
        } else {
            etDescription.setError(null);
        }

        // Validate max participants
        int maxParticipants = 0;
        try {
            if (!maxParticipantsStr.isEmpty()) {
                maxParticipants = Integer.parseInt(maxParticipantsStr);
                if (maxParticipants <= 0) {
                    etMaxParticipants.setError("Число участников должно быть больше 0");
                } else {
                    etMaxParticipants.setError(null);
                }
            } else {
                etMaxParticipants.setError("Введите число участников");
            }
        } catch (NumberFormatException e) {
            etMaxParticipants.setError("Неверное число участников");
        }

        // Validate dates
        try {
            if (!startDateStr.isEmpty()) {
                dateFormat.parse(startDateStr);
                etStartDate.setError(null);
            } else {
                etStartDate.setError("Введите дату начала");
            }
            if (!endDateStr.isEmpty()) {
                java.util.Date endDateParsed = dateFormat.parse(endDateStr);
                if (!startDateStr.isEmpty()) {
                    java.util.Date startDateParsed = dateFormat.parse(startDateStr);
                    if (endDateParsed.before(startDateParsed)) {
                        etEndDate.setError("Дата окончания раньше даты начала");
                    } else {
                        etEndDate.setError(null);
                    }
                }
            } else {
                etEndDate.setError("Введите дату окончания");
            }
        } catch (Exception e) {
            etStartDate.setError("Неверный формат даты");
        }

        // Validate times
        try {
            if (!startTimeStr.isEmpty()) {
                new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(startTimeStr);
                etStartTime.setError(null);
            } else {
                etStartTime.setError("Введите время начала");
            }
            if (!endTimeStr.isEmpty()) {
                new SimpleDateFormat("HH:mm", Locale.getDefault()).parse(endTimeStr);
                etEndTime.setError(null);
            } else {
                etEndTime.setError("Введите время окончания");
            }
            // Validate same-day time
            if (!startDateStr.isEmpty() && !endDateStr.isEmpty() && startDateStr.equals(endDateStr)) {
                int startTotalMinutes = startHour * 60 + startMinute;
                int endTotalMinutes = endHour * 60 + endMinute;
                if (endTotalMinutes <= startTotalMinutes) {
                    etEndTime.setError("Время окончания не может быть раньше или равно времени начала");
                } else {
                    etEndTime.setError(null);
                }
            }
        } catch (Exception e) {
            etStartTime.setError("Неверный формат времени");
        }

        // Validate min age
        if (!minAgeStr.isEmpty()) {
            try {
                int minAge = Integer.parseInt(minAgeStr);
                if (minAge < 0) {
                    etMinAge.setError("Возраст не может быть отрицательным");
                } else {
                    etMinAge.setError(null);
                }
            } catch (NumberFormatException e) {
                etMinAge.setError("Введите корректный возраст");
            }
        } else {
            etMinAge.setError(null);
        }

        // Enable button only if all inputs are valid
        boolean isValid = !title.isEmpty() &&
                !description.isEmpty() && description.length() <= 500 &&
                !maxParticipantsStr.isEmpty() && maxParticipants > 0 &&
                !startDateStr.isEmpty() && !endDateStr.isEmpty() &&
                !startTimeStr.isEmpty() && !endTimeStr.isEmpty() &&
                !address.isEmpty() && selectedCoordinates != null &&
                hasTags && coverImageUri != null;

        if (!minAgeStr.isEmpty()) {
            try {
                int minAge = Integer.parseInt(minAgeStr);
                isValid = isValid && minAge >= 0;
            } catch (NumberFormatException e) {
                isValid = false;
            }
        }

        btnCreateEvent.setEnabled(isValid);
    }

    private void updateGalleryCount() {
        int totalImages = galleryImageUris.size();
        tvGalleryCount.setText(totalImages + " изображений выбрано");
        btnAddGalleryPhoto.setVisibility(totalImages < MAX_GALLERY_IMAGES ? View.VISIBLE : View.GONE);
        rvGalleryImages.setVisibility(totalImages > 0 ? View.VISIBLE : View.GONE);
    }

    private void createEvent() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String maxParticipantsStr = etMaxParticipants.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String minAgeStr = etMinAge.getText().toString().trim();
        String startDateStr = etStartDate.getText().toString().trim();
        String endDateStr = etEndDate.getText().toString().trim();

        // Create EventModel
        String eventId = UUID.randomUUID().toString();
        String organizerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";
        EventModel event = new EventModel(eventId, organizerId, title, description);

        // Set max participants
        int maxParticipants = Integer.parseInt(maxParticipantsStr);
        event.setMaxParticipants(maxParticipants);

        // Set min age
        int minAge = minAgeStr.isEmpty() ? 15 : Integer.parseInt(minAgeStr);
        event.setMinAge(minAge);

        // Set address and coordinates
        event.setAddress(address);
        event.setCoordinates(selectedCoordinates);

        // Set tags
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < chipGroupTags.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTags.getChildAt(i);
            if (chip.isChecked()) {
                tags.add(chip.getText().toString());
            }
        }
        event.setTags(tags);

        // Set status based on current date
        Date currentDate = new Date();
        if (event.getEndDate().toDate().before(currentDate)) {
            event.setStatus("completed");
        } else if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
            event.setStatus("full");
        } else {
            event.setStatus("active");
        }

        // Set dates and times
        try {
            startDate.setTime(dateFormat.parse(startDateStr));
            endDate.setTime(dateFormat.parse(endDateStr));
            startDate.set(Calendar.HOUR_OF_DAY, startHour);
            startDate.set(Calendar.MINUTE, startMinute);
            startDate.set(Calendar.SECOND, 0);
            startDate.set(Calendar.MILLISECOND, 0);
            endDate.set(Calendar.HOUR_OF_DAY, endHour);
            endDate.set(Calendar.MINUTE, endMinute);
            endDate.set(Calendar.SECOND, 0);
            endDate.set(Calendar.MILLISECOND, 0);
            event.setStartDate(new Timestamp(startDate.getTime()));
            event.setEndDate(new Timestamp(endDate.getTime()));
            event.setStartTime(String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute));
            event.setEndTime(String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка формата даты", Toast.LENGTH_SHORT).show();
            return;
        }

        // Handle image uploads
        uploadImagesAndSaveEvent(event);
    }

    private void uploadImagesAndSaveEvent(EventModel event) {
        btnCreateEvent.setEnabled(false);

        if (coverImageUri != null) {
            new Thread(() -> {
                try {
                    Bitmap coverBitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().getContentResolver(), coverImageUri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    coverBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] coverBytes = baos.toByteArray();

                    Map<String, Object> uploadResult = cloudinary.uploader().upload(coverBytes, ObjectUtils.asMap(
                            "folder", "events/" + event.getEventId()
                    ));
                    String coverImageUrl = (String) uploadResult.get("secure_url");

                    requireActivity().runOnUiThread(() -> {
                        event.setCoverImage(coverImageUrl);
                        uploadGalleryImagesAndSave(event);
                    });
                } catch (IOException e) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Failed to load cover image bitmap", e);
                        Toast.makeText(getContext(), "Не удалось загрузить обложку", Toast.LENGTH_SHORT).show();
                        btnCreateEvent.setEnabled(true);
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error uploading cover image to Cloudinary", e);
                        Toast.makeText(getContext(), "Ошибка загрузки обложки", Toast.LENGTH_SHORT).show();
                        btnCreateEvent.setEnabled(true);
                    });
                }
            }).start();
        } else {
            uploadGalleryImagesAndSave(event);
        }
    }

    private void uploadGalleryImagesAndSave(EventModel event) {
        List<String> galleryUrls = new ArrayList<>();
        if (galleryImageUris.isEmpty()) {
            saveEventToFirestore(event, galleryUrls);
            return;
        }

        for (Uri uri : galleryImageUris) {
            new Thread(() -> {
                try {
                    Bitmap galleryBitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().getContentResolver(), uri);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    galleryBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] galleryBytes = baos.toByteArray();

                    Map<String, Object> uploadResult = cloudinary.uploader().upload(galleryBytes, ObjectUtils.asMap(
                            "folder", "events/" + event.getEventId() + "/gallery"
                    ));
                    String galleryImageUrl = (String) uploadResult.get("secure_url");

                    synchronized (galleryUrls) {
                        galleryUrls.add(galleryImageUrl);
                        if (galleryUrls.size() == galleryImageUris.size()) {
                            requireActivity().runOnUiThread(() -> {
                                event.setGallery(galleryUrls);
                                saveEventToFirestore(event, galleryUrls);
                            });
                        }
                    }
                } catch (IOException e) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Failed to load gallery image bitmap", e);
                        Toast.makeText(getContext(), "Не удалось загрузить изображение галереи", Toast.LENGTH_SHORT).show();
                        btnCreateEvent.setEnabled(true);
                    });
                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        Log.e(TAG, "Error uploading gallery image to Cloudinary", e);
                        Toast.makeText(getContext(), "Ошибка загрузки изображения галереи", Toast.LENGTH_SHORT).show();
                        btnCreateEvent.setEnabled(true);
                    });
                }
            }).start();
        }
    }

    private void saveEventToFirestore(EventModel event, List<String> galleryUrls) {
        db.collection("events").document(event.getEventId())
                .set(event.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Мероприятие успешно создано", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save event", e);
                    Toast.makeText(getContext(), "Не удалось создать мероприятие", Toast.LENGTH_SHORT).show();
                    btnCreateEvent.setEnabled(true);
                });
    }
}