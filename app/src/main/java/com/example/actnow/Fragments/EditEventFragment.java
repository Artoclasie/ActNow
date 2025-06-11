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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Adapters.GalleryImageAdapter;
import com.example.actnow.Models.EventModel;
import com.example.actnow.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.firestore.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class EditEventFragment extends Fragment {

    private static final String TAG = "EditEventFragment";
    private static final int MAX_GALLERY_IMAGES = 10;

    // Firebase
    private FirebaseFirestore db;

    // Cloudinary
    private Cloudinary cloudinary;

    // UI элементы
    private ImageView ivCoverPreview;
    private ImageButton btnAddCoverPhoto, btnDeleteCoverPhoto;
    private ImageButton btnAddGalleryPhoto;
    private TextView tvGalleryCount;
    private RecyclerView rvGalleryImages;
    private TextInputEditText etEventTitle, etEventDescription, etStartDate, etEndDate, etStartTime, etEndTime;
    private TextInputEditText etMaxParticipants, etAddress, etMinAge;
    private TextView tvDescriptionCharCount;
    private Chip chipAnimals, chipVeterans, chipYouth, chipEcology, chipEducation, chipSport;
    private Button btnCancel, btnCreateEvent;

    // Данные
    private EventModel event;
    private String eventId;
    private Uri coverImageUri;
    private List<Uri> galleryImageUris = new ArrayList<>();
    private List<String> existingGalleryUrls = new ArrayList<>();
    private List<Object> galleryImages = new ArrayList<>(); // Для RecyclerView (String или Uri)
    private GalleryImageAdapter galleryAdapter;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private Calendar startDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
    private Calendar endDate = Calendar.getInstance(TimeZone.getTimeZone("Europe/Minsk"));
    private int startHour, startMinute, endHour, endMinute;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> coverImageLauncher;
    private ActivityResultLauncher<Intent> galleryImageLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();

        // Initialize Cloudinary
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dolnejlw3",
                "api_key", "869237653654212",
                "api_secret", "E1GjuqHeNAaEFwXIsH6BM9KRnTA"
        ));

        // Initialize image pickers
        coverImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                coverImageUri = result.getData().getData();
                ivCoverPreview.setImageURI(coverImageUri);
                ivCoverPreview.setVisibility(View.VISIBLE);
                btnDeleteCoverPhoto.setVisibility(View.VISIBLE);
                btnAddCoverPhoto.setVisibility(View.GONE);
                validateInputs();
            }
        });

        galleryImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                int currentTotal = existingGalleryUrls.size() + galleryImageUris.size();
                if (currentTotal >= MAX_GALLERY_IMAGES) {
                    Toast.makeText(getContext(), "Достигнут лимит в " + MAX_GALLERY_IMAGES + " изображений", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (result.getData().getClipData() != null) {
                    int count = Math.min(result.getData().getClipData().getItemCount(), MAX_GALLERY_IMAGES - currentTotal);
                    for (int i = 0; i < count; i++) {
                        Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                        if (!galleryImageUris.contains(uri)) {
                            galleryImageUris.add(uri);
                            galleryImages.add(uri);
                        }
                    }
                } else if (result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    if (!galleryImageUris.contains(uri) && currentTotal < MAX_GALLERY_IMAGES) {
                        galleryImageUris.add(uri);
                        galleryImages.add(uri);
                    }
                }
                galleryAdapter.updateImages(galleryImages);
                updateGalleryCount();
                validateInputs();
            }
        });

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_event, container, false);
        initViews(view);
        setupGalleryRecyclerView();
        setupListeners();
        loadEventData();
        return view;
    }

    private void initViews(View view) {
        ivCoverPreview = view.findViewById(R.id.iv_cover_preview);
        btnAddCoverPhoto = view.findViewById(R.id.btn_add_cover_photo);
        btnDeleteCoverPhoto = view.findViewById(R.id.btn_delete_cover_photo);
        btnAddGalleryPhoto = view.findViewById(R.id.btn_add_gallery_photo);
        tvGalleryCount = view.findViewById(R.id.tv_gallery_count);
        rvGalleryImages = view.findViewById(R.id.rv_gallery_images);
        etEventTitle = view.findViewById(R.id.et_event_title);
        etEventDescription = view.findViewById(R.id.et_event_description);
        etStartDate = view.findViewById(R.id.et_start_date);
        etEndDate = view.findViewById(R.id.et_end_date);
        etStartTime = view.findViewById(R.id.et_start_time);
        etEndTime = view.findViewById(R.id.et_end_time);
        etMaxParticipants = view.findViewById(R.id.et_max_participants);
        etAddress = view.findViewById(R.id.et_address);
        etMinAge = view.findViewById(R.id.et_min_age);
        tvDescriptionCharCount = view.findViewById(R.id.tv_description_char_count);
        chipAnimals = view.findViewById(R.id.chip_animals);
        chipVeterans = view.findViewById(R.id.chip_veterans);
        chipYouth = view.findViewById(R.id.chip_youth);
        chipEcology = view.findViewById(R.id.chip_ecology);
        chipEducation = view.findViewById(R.id.chip_education);
        chipSport = view.findViewById(R.id.chip_sport);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnCreateEvent = view.findViewById(R.id.btn_create_event);
    }

    private void setupGalleryRecyclerView() {
        galleryImages.addAll(existingGalleryUrls);
        galleryImages.addAll(galleryImageUris);
        galleryAdapter = new GalleryImageAdapter(getContext(), galleryImages, position -> {
            Object image = galleryImages.get(position);
            if (image instanceof String) {
                existingGalleryUrls.remove(image);
            } else if (image instanceof Uri) {
                galleryImageUris.remove(image);
            }
            galleryImages.remove(position);
            galleryAdapter.updateImages(galleryImages);
            updateGalleryCount();
            validateInputs();
        });
        rvGalleryImages.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvGalleryImages.setAdapter(galleryAdapter);
    }

    private void setupListeners() {
        // Image pickers
        btnAddCoverPhoto.setOnClickListener(v -> openImagePicker(coverImageLauncher, false));
        btnDeleteCoverPhoto.setOnClickListener(v -> {
            coverImageUri = null;
            ivCoverPreview.setImageDrawable(null);
            ivCoverPreview.setVisibility(View.GONE);
            btnDeleteCoverPhoto.setVisibility(View.GONE);
            btnAddCoverPhoto.setVisibility(View.VISIBLE);
            validateInputs();
        });
        btnAddGalleryPhoto.setOnClickListener(v -> openImagePicker(galleryImageLauncher, true));

        // Date and time pickers
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

        // Description character counter
        etEventDescription.addTextChangedListener(new TextWatcher() {
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

        // Input validation
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) { validateInputs(); }
        };
        etEventTitle.addTextChangedListener(inputWatcher);
        etMaxParticipants.addTextChangedListener(inputWatcher);
        etAddress.addTextChangedListener(inputWatcher);
        etMinAge.addTextChangedListener(inputWatcher);

        // Chip listeners
        View.OnClickListener chipListener = v -> validateInputs();
        chipAnimals.setOnClickListener(chipListener);
        chipVeterans.setOnClickListener(chipListener);
        chipYouth.setOnClickListener(chipListener);
        chipEcology.setOnClickListener(chipListener);
        chipEducation.setOnClickListener(chipListener);
        chipSport.setOnClickListener(chipListener);

        // Buttons
        btnCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnCreateEvent.setOnClickListener(v -> updateEvent());
    }

    private void openImagePicker(ActivityResultLauncher<Intent> launcher, boolean multiple) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        if (multiple) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        launcher.launch(intent);
    }

    private void loadEventData() {
        if (eventId == null) {
            Toast.makeText(getContext(), "Мероприятие не указано", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        event = documentSnapshot.toObject(EventModel.class);
                        if (event != null) {
                            event.setEventId(eventId);
                            populateFields();
                            validateInputs();
                        }
                    } else {
                        Toast.makeText(getContext(), "Мероприятие не найдено", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка загрузки мероприятия", e);
                    Toast.makeText(getContext(), "Ошибка загрузки мероприятия", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateFields() {
        // Cover image
        if (event.getCoverImage() != null && !event.getCoverImage().isEmpty()) {
            Glide.with(this)
                    .load(event.getCoverImage())
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(ivCoverPreview);
            ivCoverPreview.setVisibility(View.VISIBLE);
            btnDeleteCoverPhoto.setVisibility(View.VISIBLE);
            btnAddCoverPhoto.setVisibility(View.GONE);
        }

        // Gallery images
        if (event.getGallery() != null) {
            existingGalleryUrls.addAll(event.getGallery());
            galleryImages.addAll(existingGalleryUrls);
            galleryAdapter.updateImages(galleryImages);
            updateGalleryCount();
        }

        // Title
        etEventTitle.setText(event.getTitle());

        // Description
        etEventDescription.setText(event.getDescription());
        tvDescriptionCharCount.setText((event.getDescription() != null ? event.getDescription().length() : 0) + "/500");

        // Dates
        if (event.getStartDate() != null) {
            startDate.setTime(event.getStartDate().toDate());
            etStartDate.setText(dateFormat.format(startDate.getTime()));
        }
        if (event.getEndDate() != null) {
            endDate.setTime(event.getEndDate().toDate());
            etEndDate.setText(dateFormat.format(endDate.getTime()));
        }

        // Times
        if (event.getStartTime() != null) {
            try {
                java.util.Date time = timeFormat.parse(event.getStartTime());
                startHour = time.getHours();
                startMinute = time.getMinutes();
                etStartTime.setText(event.getStartTime());
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing start time", e);
            }
        }
        if (event.getEndTime() != null) {
            try {
                java.util.Date time = timeFormat.parse(event.getEndTime());
                endHour = time.getHours();
                endMinute = time.getMinutes();
                etEndTime.setText(event.getEndTime());
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing end time", e);
            }
        }

        // Max participants
        etMaxParticipants.setText(String.valueOf(event.getMaxParticipants()));

        // Address
        etAddress.setText(event.getAddress());

        // Min age
        if (event.getMinAge() > 0) {
            etMinAge.setText(String.valueOf(event.getMinAge()));
        }

        // Tags
        if (event.getTags() != null) {
            for (String tag : event.getTags()) {
                switch (tag) {
                    case "Животные": chipAnimals.setChecked(true); break;
                    case "Ветераны": chipVeterans.setChecked(true); break;
                    case "Молодеж": chipYouth.setChecked(true); break;
                    case "Экология": chipEcology.setChecked(true); break;
                    case "Образование": chipEducation.setChecked(true); break;
                    case "Спорт": chipSport.setChecked(true); break;
                }
            }
        }
    }

    private void validateInputs() {
        boolean isValid = true;

        // Title
        String title = etEventTitle.getText() != null ? etEventTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            etEventTitle.setError("Введите название");
            isValid = false;
        } else {
            etEventTitle.setError(null);
        }

        // Description
        String description = etEventDescription.getText() != null ? etEventDescription.getText().toString().trim() : "";
        if (description.length() > 500) {
            etEventDescription.setError("Описание слишком длинное");
            isValid = false;
        } else {
            etEventDescription.setError(null);
        }

        // Dates
        String startDateStr = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String endDateStr = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";
        try {
            if (!startDateStr.isEmpty()) {
                dateFormat.parse(startDateStr);
                etStartDate.setError(null);
            } else {
                etStartDate.setError("Введите дату начала");
                isValid = false;
            }
            if (!endDateStr.isEmpty()) {
                java.util.Date endDateParsed = dateFormat.parse(endDateStr);
                if (!startDateStr.isEmpty()) {
                    java.util.Date startDateParsed = dateFormat.parse(startDateStr);
                    if (endDateParsed.before(startDateParsed)) {
                        etEndDate.setError("Дата окончания раньше даты начала");
                        isValid = false;
                    } else {
                        etEndDate.setError(null);
                    }
                }
            } else {
                etEndDate.setError("Введите дату окончания");
                isValid = false;
            }
        } catch (ParseException e) {
            etStartDate.setError("Неверный формат даты");
            isValid = false;
        }

        // Times
        String startTimeStr = etStartTime.getText() != null ? etStartTime.getText().toString().trim() : "";
        String endTimeStr = etEndTime.getText() != null ? etEndTime.getText().toString().trim() : "";
        try {
            if (!startTimeStr.isEmpty()) {
                timeFormat.parse(startTimeStr);
                etStartTime.setError(null);
            } else {
                etStartTime.setError("Введите время начала");
                isValid = false;
            }
            if (!endTimeStr.isEmpty()) {
                timeFormat.parse(endTimeStr);
                etEndTime.setError(null);
            } else {
                etEndTime.setError("Введите время окончания");
                isValid = false;
            }
            if (!startDateStr.isEmpty() && !endDateStr.isEmpty() && startDateStr.equals(endDateStr)) {
                int startTotalMinutes = startHour * 60 + startMinute;
                int endTotalMinutes = endHour * 60 + endMinute;
                if (endTotalMinutes <= startTotalMinutes) {
                    etEndTime.setError("Время окончания не может быть раньше или равно времени начала");
                    isValid = false;
                } else {
                    etEndTime.setError(null);
                }
            }
        } catch (ParseException e) {
            etStartTime.setError("Неверный формат времени");
            isValid = false;
        }

        // Max participants
        String maxParticipantsStr = etMaxParticipants.getText() != null ? etMaxParticipants.getText().toString().trim() : "";
        if (maxParticipantsStr.isEmpty() || Integer.parseInt(maxParticipantsStr) <= 0) {
            etMaxParticipants.setError("Введите корректное число участников");
            isValid = false;
        } else if (event.getCurrentParticipants() > Integer.parseInt(maxParticipantsStr)) {
            etMaxParticipants.setError("Число участников меньше текущего");
            isValid = false;
        } else {
            etMaxParticipants.setError(null);
        }

        // Address
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        if (address.isEmpty()) {
            etAddress.setError("Введите адрес");
            isValid = false;
        } else {
            etAddress.setError(null);
        }

        // Min age
        String minAgeStr = etMinAge.getText() != null ? etMinAge.getText().toString().trim() : "";
        if (!minAgeStr.isEmpty()) {
            try {
                int minAge = Integer.parseInt(minAgeStr);
                if (minAge < 0) {
                    etMinAge.setError("Возраст не может быть отрицательным");
                    isValid = false;
                } else {
                    etMinAge.setError(null);
                }
            } catch (NumberFormatException e) {
                etMinAge.setError("Введите корректный возраст");
                isValid = false;
            }
        }

        // Tags
        boolean hasTags = chipAnimals.isChecked() || chipVeterans.isChecked() || chipYouth.isChecked() ||
                chipEcology.isChecked() || chipEducation.isChecked() || chipSport.isChecked();

        // Cover image (required)
        if (coverImageUri == null && (event.getCoverImage() == null || event.getCoverImage().isEmpty())) {
            isValid = false;
        }

        btnCreateEvent.setEnabled(isValid && hasTags);
    }

    private void updateEvent() {
        Map<String, Object> updates = new HashMap<>();

        // Title
        String title = etEventTitle.getText().toString().trim();
        if (!title.equals(event.getTitle())) {
            updates.put("title", title);
        }

        // Description
        String description = etEventDescription.getText().toString().trim();
        if (!description.equals(event.getDescription())) {
            updates.put("description", description);
        }

        // Dates
        try {
            String startDateStr = etStartDate.getText().toString().trim();
            if (!startDateStr.equals(dateFormat.format(event.getStartDate().toDate()))) {
                startDate.setTime(dateFormat.parse(startDateStr));
                startDate.set(Calendar.HOUR_OF_DAY, startHour);
                startDate.set(Calendar.MINUTE, startMinute);
                startDate.set(Calendar.SECOND, 0);
                startDate.set(Calendar.MILLISECOND, 0);
                updates.put("startDate", new Timestamp(startDate.getTime()));
            }
            String endDateStr = etEndDate.getText().toString().trim();
            if (!endDateStr.equals(dateFormat.format(event.getEndDate().toDate()))) {
                endDate.setTime(dateFormat.parse(endDateStr));
                endDate.set(Calendar.HOUR_OF_DAY, endHour);
                endDate.set(Calendar.MINUTE, endMinute);
                endDate.set(Calendar.SECOND, 0);
                endDate.set(Calendar.MILLISECOND, 0);
                updates.put("endDate", new Timestamp(endDate.getTime()));
            }
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Ошибка формата даты", Toast.LENGTH_SHORT).show();
            return;
        }

        // Times
        String startTime = etStartTime.getText().toString().trim();
        if (!startTime.equals(event.getStartTime())) {
            updates.put("startTime", startTime);
        }
        String endTime = etEndTime.getText().toString().trim();
        if (!endTime.equals(event.getEndTime())) {
            updates.put("endTime", endTime);
        }

        // Max participants
        int maxParticipants = Integer.parseInt(etMaxParticipants.getText().toString().trim());
        if (maxParticipants != event.getMaxParticipants()) {
            updates.put("maxParticipants", maxParticipants);
        }

        // Address
        String address = etAddress.getText().toString().trim();
        if (!address.equals(event.getAddress())) {
            updates.put("address", address);
            updates.put("coordinates", new GeoPoint(0, 0)); // Placeholder
        }

        // Min age
        String minAgeStr = etMinAge.getText().toString().trim();
        int minAge = minAgeStr.isEmpty() ? 15 : Integer.parseInt(minAgeStr);
        if (minAge != event.getMinAge()) {
            updates.put("minAge", minAge);
        }

        // Tags
        List<String> tags = new ArrayList<>();
        if (chipAnimals.isChecked()) tags.add("Животные");
        if (chipVeterans.isChecked()) tags.add("Ветераны");
        if (chipYouth.isChecked()) tags.add("Молодеж");
        if (chipEcology.isChecked()) tags.add("Экология");
        if (chipEducation.isChecked()) tags.add("Образование");
        if (chipSport.isChecked()) tags.add("Спорт");
        if (!tags.equals(event.getTags())) {
            updates.put("tags", tags);
        }

        // Handle image uploads
        uploadImages(updates);
    }

    private void uploadImages(Map<String, Object> updates) {
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
                            "folder", "events/" + eventId
                    ));
                    String coverImageUrl = (String) uploadResult.get("secure_url");

                    requireActivity().runOnUiThread(() -> {
                        updates.put("coverImage", coverImageUrl);
                        uploadGalleryImages(updates);
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
            uploadGalleryImages(updates);
        }
    }

    private void uploadGalleryImages(Map<String, Object> updates) {
        List<String> newGalleryUrls = new ArrayList<>(existingGalleryUrls);
        if (galleryImageUris.isEmpty()) {
            if (!newGalleryUrls.equals(event.getGallery())) {
                updates.put("gallery", newGalleryUrls);
            }
            saveEvent(updates);
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
                            "folder", "events/" + eventId + "/gallery"
                    ));
                    String galleryImageUrl = (String) uploadResult.get("secure_url");

                    synchronized (newGalleryUrls) {
                        newGalleryUrls.add(galleryImageUrl);
                        if (newGalleryUrls.size() == existingGalleryUrls.size() + galleryImageUris.size()) {
                            requireActivity().runOnUiThread(() -> {
                                updates.put("gallery", newGalleryUrls);
                                saveEvent(updates);
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

    private void saveEvent(Map<String, Object> updates) {
        if (updates.isEmpty()) {
            Toast.makeText(getContext(), "Изменения отсутствуют", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    sendNotifications();
                    Toast.makeText(getContext(), "Мероприятие обновлено", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Ошибка обновления мероприятия", e);
                    Toast.makeText(getContext(), "Ошибка обновления мероприятия", Toast.LENGTH_SHORT).show();
                    btnCreateEvent.setEnabled(true);
                });
    }

    private void sendNotifications() {
        db.collection("events").document(eventId)
                .collection("Participants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        String participantId = snapshot.getId();
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("userId", participantId);
                        notification.put("eventId", eventId);
                        notification.put("title", "Мероприятие обновлено");
                        notification.put("message", "Мероприятие '" + event.getTitle() + "' было изменено организатором.");
                        notification.put("createdAt", new java.util.Date());
                        notification.put("read", false);

                        db.collection("Notifications").document()
                                .set(notification)
                                .addOnFailureListener(e -> Log.e(TAG, "Ошибка отправки уведомления для " + participantId, e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Ошибка получения участников для уведомлений", e));
    }

    private void updateGalleryCount() {
        int totalImages = existingGalleryUrls.size() + galleryImageUris.size();
        tvGalleryCount.setText(totalImages + " изображений выбрано");
        btnAddGalleryPhoto.setVisibility(totalImages < MAX_GALLERY_IMAGES ? View.VISIBLE : View.GONE);
    }
}