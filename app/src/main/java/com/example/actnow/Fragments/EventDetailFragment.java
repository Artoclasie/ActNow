package com.example.actnow.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Adapters.CommentAdapter;
import com.example.actnow.Adapters.GalleryAdapter;
import com.example.actnow.Adapters.ParticipantAdapter;
import com.example.actnow.Models.CommentModel;
import com.example.actnow.Models.EventModel;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventDetailFragment extends Fragment implements CommentAdapter.OnCommentInteractionListener {

    private static final String TAG = "EventDetailFragment";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;

    // UI элементы
    private ImageButton btnBack;
    private ImageView imvEventContent;
    private ImageView ivEventCover;
    private TextView tvEventTitle, tvEventDate, tvEventTime, tvDescription, tvDirections;
    private View organizerLayout;
    private TextView tvOrganizerName, tvOrganizerRole;
    private ImageView ivOrganizerPhoto;
    private MaterialButton btnAction, btnShowMap;
    private RecyclerView rvGallery, rvParticipants, rvComments;
    private EditText etComment;
    private ImageView ivSendComment;
    private TextView tvCommentHint;
    private NestedScrollView nestedScrollView;
    private View commentInputLayout;

    // Адаптеры
    private GalleryAdapter galleryAdapter;
    private ParticipantAdapter participantAdapter;
    private CommentAdapter commentAdapter;

    // Данные
    private EventModel event;
    private List<String> galleryImages = new ArrayList<>();
    private List<UserProfile> participants = new ArrayList<>();
    private List<CommentModel> comments = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFirebase();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_detail, container, false);
        initViews(view);
        setupRecyclerViews();
        setupOrganizerClick();
        setupBackButton();
        loadEventData();
        return view;
    }

    private void initViews(View view) {
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        btnBack = view.findViewById(R.id.btnBack);
        imvEventContent = view.findViewById(R.id.imv_event_content);
        ivEventCover = view.findViewById(R.id.iv_event_cover);
        tvEventTitle = view.findViewById(R.id.tv_event_title);
        tvEventDate = view.findViewById(R.id.tv_event_date);
        tvEventTime = view.findViewById(R.id.tv_event_time);
        tvDescription = view.findViewById(R.id.tv_description);
        tvDirections = view.findViewById(R.id.tv_directions);
        btnAction = view.findViewById(R.id.btn_action);
        btnShowMap = view.findViewById(R.id.btn_show_map);
        rvGallery = view.findViewById(R.id.rv_gallery);
        rvParticipants = view.findViewById(R.id.rv_participants);
        rvComments = view.findViewById(R.id.rv_comments);
        etComment = view.findViewById(R.id.et_comment);
        ivSendComment = view.findViewById(R.id.iv_send_comment);
        tvCommentHint = view.findViewById(R.id.tv_comment_hint);
        commentInputLayout = view.findViewById(R.id.comment_input_layout);
        organizerLayout = view.findViewById(R.id.organizer_layout);
        tvOrganizerName = view.findViewById(R.id.tv_organizer_name);
        tvOrganizerRole = view.findViewById(R.id.tv_organizer_role);
        ivOrganizerPhoto = view.findViewById(R.id.iv_organizer_photo);

        // Начальное состояние кнопки
        btnAction.setText("Загрузка...");
        btnAction.setEnabled(false);
    }

    private void setupRecyclerViews() {
        galleryAdapter = new GalleryAdapter(galleryImages, false, null, imageUrl -> {
            ImageViewerFragment fragment = new ImageViewerFragment();
            Bundle args = new Bundle();
            args.putString("imageUrl", imageUrl);
            fragment.setArguments(args);
            getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.enter_from_right,
                            R.anim.exit_to_left,
                            R.anim.enter_from_left,
                            R.anim.exit_to_right
                    )
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvGallery.setLayoutManager(layoutManager);
        rvGallery.setAdapter(galleryAdapter);
        rvGallery.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(@NonNull RecyclerView rv, @NonNull android.view.MotionEvent e) {}

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
        });

        participantAdapter = new ParticipantAdapter(participants);
        rvParticipants.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvParticipants.setAdapter(participantAdapter);

        commentAdapter = new CommentAdapter(getContext(), comments, this);
        rvComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvComments.setAdapter(commentAdapter);
    }

    private void setupOrganizerClick() {
        organizerLayout.setOnClickListener(v -> {
            if (event != null && event.getOrganizerId() != null) {
                AnotherProfileFragment fragment = new AnotherProfileFragment();
                Bundle args = new Bundle();
                args.putString("userId", event.getOrganizerId());
                fragment.setArguments(args);
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    private void loadEventData() {
        Bundle args = getArguments();
        if (args == null) {
            Log.e(TAG, "No arguments provided");
            return;
        }

        String eventId = args.getString("eventId");
        String title = args.getString("title");
        String imageUrl = args.getString("imageUrl");

        Log.d(TAG, "Received event data - ID: " + eventId + ", Title: " + title + ", Image: " + imageUrl);

        if (eventId == null) {
            Log.e(TAG, "Event ID is null");
            Toast.makeText(getContext(), "Ошибка: ID мероприятия отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) return;
                    if (documentSnapshot.exists()) {
                        event = documentSnapshot.toObject(EventModel.class);
                        if (event != null) {
                            event.setEventId(eventId);
                            if (title != null) {
                                event.setTitle(title);
                            }
                            if (imageUrl != null) {
                                event.setCoverImage(imageUrl);
                            }
                            updateUI();
                            loadOrganizer();
                            loadParticipants();
                            loadComments();
                            updateActionButton();
                            promptForRating();
                        } else {
                            Log.e(TAG, "Failed to parse EventModel");
                            Toast.makeText(getContext(), "Ошибка: данные мероприятия некорректны", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Event document does not exist");
                        Toast.makeText(getContext(), "Мероприятие не найдено", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading event", e);
                    Toast.makeText(getContext(), "Ошибка загрузки мероприятия", Toast.LENGTH_SHORT).show();
                    btnAction.setText("Ошибка");
                    btnAction.setEnabled(false);
                });
    }

    private void updateUI() {
        if (!isAdded() || getContext() == null) return;
        Log.d(TAG, "Binding event: " + event.getEventId() + ", Title: " + event.getTitle() +
                ", StartDate: " + event.getStartDate() + ", EndDate: " + event.getEndDate() +
                ", Address: " + event.getAddress() + ", coverImage: " + event.getCoverImage());

        String title = event.getTitle() != null ? event.getTitle() : "Без названия";
        if ("completed".equals(event.getStatus())) {
            title = "Мероприятие окончено! " + title;
        }
        tvEventTitle.setText(title);
        tvDescription.setText(event.getDescription() != null ? event.getDescription() : "Описание отсутствует");

        if (event.getCoverImage() != null && !event.getCoverImage().isEmpty()) {
            Log.d(TAG, "Loading image into imv_event_content: " + event.getCoverImage());
            Glide.with(this)
                    .load(event.getCoverImage())
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(imvEventContent);
        } else {
            Log.w(TAG, "Cover image is null or empty for event " + event.getEventId());
            imvEventContent.setImageResource(R.drawable.default_image);
        }

        if (event.getCoverImage() != null && !event.getCoverImage().isEmpty()) {
            Log.d(TAG, "Loading image into iv_event_cover: " + event.getCoverImage());
            Glide.with(this)
                    .load(event.getCoverImage())
                    .placeholder(R.drawable.default_image)
                    .error(R.drawable.default_image)
                    .into(ivEventCover);
        } else {
            Log.w(TAG, "Cover image is null or empty for event " + event.getEventId());
            ivEventCover.setImageResource(R.drawable.default_image);
        }

        String address = event.getAddress();
        tvDirections.setText(address != null && !address.isEmpty() ? address : "Адрес не указан");
        btnShowMap.setVisibility(address != null && !address.isEmpty() ? View.VISIBLE : View.GONE);
        btnShowMap.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=" + address));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open map", e);
                Toast.makeText(getContext(), "Ошибка открытия карты", Toast.LENGTH_SHORT).show();
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy", new Locale("ru"));
        if (event.getStartDate() != null) {
            tvEventDate.setText(dateFormat.format(event.getStartDate().toDate()));
        } else {
            tvEventDate.setText("Дата не указана");
        }
        String timeText = (event.getStartTime() != null ? event.getStartTime() : "Не указано") + " - " +
                (event.getEndTime() != null ? event.getEndTime() : "Не указано") + " (EUROPE/MINSK)";
        tvEventTime.setText(timeText);

        ChipGroup chipGroupTags = getView().findViewById(R.id.chip_group_tags);
        chipGroupTags.removeAllViews();
        if (event.getTags() != null && !event.getTags().isEmpty()) {
            for (String tag : event.getTags()) {
                Chip chip = new Chip(getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice);
                chip.setText(tag);
                chip.setTextSize(12);
                chip.setEnabled(false);
                chipGroupTags.addView(chip);
            }
        } else {
            Chip chip = new Chip(getContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Choice);
            chip.setText("Без категорий");
            chip.setTextSize(12);
            chip.setEnabled(false);
            chipGroupTags.addView(chip);
        }

        galleryImages.clear();
        if (event.getGallery() != null) {
            galleryImages.addAll(event.getGallery());
        }
        galleryAdapter.notifyDataSetChanged();

        TextView btnShowMore = getView().findViewById(R.id.btn_show_more);
        tvDescription.setMaxLines(4);
        btnShowMore.setOnClickListener(v -> {
            if (tvDescription.getMaxLines() == 4) {
                tvDescription.setMaxLines(Integer.MAX_VALUE);
                btnShowMore.setText("Скрыть");
            } else {
                tvDescription.setMaxLines(4);
                btnShowMore.setText("Показать больше");
            }
        });

        ivSendComment.setOnClickListener(v -> postComment());
    }

    private void updateActionButton() {
        if (!isAdded() || getContext() == null) return;
        if (currentUserId == null || event == null) {
            Log.d(TAG, "No user or event, disabling action button");
            btnAction.setText("Войдите в аккаунт");
            btnAction.setEnabled(false);
            tvCommentHint.setVisibility(View.VISIBLE);
            commentInputLayout.setVisibility(View.GONE);
            return;
        }

        // Проверка для организатора
        if (currentUserId.equals(event.getOrganizerId())) {
            Log.d(TAG, "User is organizer, status: " + event.getStatus());
            if ("completed".equals(event.getStatus())) {
                btnAction.setText("Мероприятие завершено");
                btnAction.setEnabled(false);
            } else {
                btnAction.setText("Изменить");
                btnAction.setEnabled(true);
                btnAction.setOnClickListener(v -> {
                    Log.d(TAG, "Navigating to EditEventFragment for event: " + event.getEventId());
                    EditEventFragment fragment = new EditEventFragment();
                    Bundle args = new Bundle();
                    args.putString("eventId", event.getEventId());
                    fragment.setArguments(args);
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                });
            }
            tvCommentHint.setVisibility(View.VISIBLE);
            commentInputLayout.setVisibility(View.GONE);
            return;
        }

        // Загрузка профиля пользователя
        db.collection("Profiles").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    UserProfile user = documentSnapshot.exists() ? documentSnapshot.toObject(UserProfile.class) : null;
                    if (user == null) {
                        Log.w(TAG, "User profile not found");
                        btnAction.setText("Ошибка профиля");
                        btnAction.setEnabled(false);
                        tvCommentHint.setVisibility(View.VISIBLE);
                        commentInputLayout.setVisibility(View.GONE);
                        return;
                    }

                    Log.d(TAG, "User profile loaded, accountType: " + user.getAccountType());
                    if ("legal_entity".equals(user.getAccountType())) {
                        btnAction.setText("Юр. лица не могут участвовать");
                        btnAction.setEnabled(false);
                        tvCommentHint.setVisibility(View.VISIBLE);
                        commentInputLayout.setVisibility(View.GONE);
                        return;
                    }

                    int userAge = calculateAge(user.getBirthDate());
                    Integer minAge = event.getMinAge();

                    // Проверка статуса события
                    if ("completed".equals(event.getStatus())) {
                        checkReviewStatus();
                    } else if (event.getCurrentParticipants() >= event.getMaxParticipants()) {
                        btnAction.setText("Уже максимальное число участников");
                        btnAction.setEnabled(false);
                        tvCommentHint.setVisibility(View.VISIBLE);
                        commentInputLayout.setVisibility(View.GONE);
                    } else if (minAge != null && userAge < minAge) {
                        btnAction.setText("Ваш возраст не подходит (мин. " + minAge + ")");
                        btnAction.setEnabled(false);
                        tvCommentHint.setVisibility(View.VISIBLE);
                        commentInputLayout.setVisibility(View.GONE);
                    } else {
                        checkParticipationStatus();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading user profile", e);
                    btnAction.setText("Ошибка загрузки профиля");
                    btnAction.setEnabled(false);
                    tvCommentHint.setVisibility(View.VISIBLE);
                    commentInputLayout.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkReviewStatus() {
        db.collection("Reviews")
                .whereEqualTo("userId", event.getOrganizerId())
                .whereEqualTo("eventId", event.getEventId())
                .whereEqualTo("reviewerId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;
                    db.collection("events").document(event.getEventId())
                            .collection("Participants").document(currentUserId)
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                boolean isParticipant = snapshot.exists();
                                if (isParticipant && querySnapshot.isEmpty()) {
                                    Log.d(TAG, "User can leave a review");
                                    btnAction.setText("Оставить отзыв");
                                    btnAction.setEnabled(true);
                                    btnAction.setOnClickListener(v -> openReviewDialog());
                                } else {
                                    Log.d(TAG, "Review already left or not a participant");
                                    btnAction.setText("Мероприятие завершено");
                                    btnAction.setEnabled(false);
                                }
                                tvCommentHint.setVisibility(isParticipant ? View.GONE : View.VISIBLE);
                                commentInputLayout.setVisibility(isParticipant ? View.VISIBLE : View.GONE);
                                if (isParticipant) {
                                    animateCommentInput();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                Log.e(TAG, "Error checking participant status", e);
                                btnAction.setText("Ошибка");
                                btnAction.setEnabled(false);
                                tvCommentHint.setVisibility(View.VISIBLE);
                                commentInputLayout.setVisibility(View.GONE);
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error checking reviews", e);
                    btnAction.setText("Ошибка");
                    btnAction.setEnabled(false);
                    tvCommentHint.setVisibility(View.VISIBLE);
                    commentInputLayout.setVisibility(View.GONE);
                });
    }

    private void checkParticipationStatus() {
        db.collection("events").document(event.getEventId())
                .collection("Participants").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    boolean isParticipant = snapshot.exists();
                    Log.d(TAG, "User is participant: " + isParticipant);
                    btnAction.setText(isParticipant ? "Изменить ответ" : "Присоединиться");
                    btnAction.setEnabled(true);
                    btnAction.setOnClickListener(v -> toggleParticipation());
                    tvCommentHint.setVisibility(isParticipant ? View.GONE : View.VISIBLE);
                    commentInputLayout.setVisibility(isParticipant ? View.VISIBLE : View.GONE);
                    if (isParticipant) {
                        animateCommentInput();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error checking participation", e);
                    btnAction.setText("Ошибка проверки участия");
                    btnAction.setEnabled(false);
                    tvCommentHint.setVisibility(View.VISIBLE);
                    commentInputLayout.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Ошибка проверки участия", Toast.LENGTH_SHORT).show();
                });
    }

    private int calculateAge(Timestamp birthDate) {
        if (birthDate == null) return 0;
        Date date = birthDate.toDate();
        Date now = new Date();
        long diffInMillies = now.getTime() - date.getTime();
        return (int) (diffInMillies / (1000L * 60 * 60 * 24 * 365));
    }

    private void toggleParticipation() {
        if (currentUserId == null || event == null || currentUserId.equals(event.getOrganizerId()) || "completed".equals(event.getStatus())) {
            Log.d(TAG, "Cannot toggle participation: invalid state");
            return;
        }

        db.collection("events").document(event.getEventId())
                .collection("Participants").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    if (snapshot.exists()) {
                        showParticipationDialog();
                    } else {
                        addParticipant();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error toggling participation", e);
                    Toast.makeText(getContext(), "Ошибка изменения участия", Toast.LENGTH_SHORT).show();
                });
    }

    private void showParticipationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Изменить участие")
                .setMessage("Идете на мероприятие?")
                .setPositiveButton("Да", (dialog, which) -> {
                    updateActionButton();
                    loadParticipants();
                    loadComments();
                    Toast.makeText(getContext(), "Вы остаетесь участником", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Нет", (dialog, which) -> removeParticipant())
                .setNeutralButton("Отмена", null)
                .show();
    }

    private void addParticipant() {
        if (event.getCurrentParticipants() >= event.getMaxParticipants() || "completed".equals(event.getStatus())) {
            Log.d(TAG, "Cannot add participant: event full or completed");
            updateActionButton();
            return;
        }

        Map<String, Object> participantData = new HashMap<>();
        participantData.put("userId", currentUserId);
        participantData.put("joinedAt", new Date());

        db.collection("events").document(event.getEventId())
                .collection("Participants").document(currentUserId)
                .set(participantData)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    db.collection("events").document(event.getEventId())
                            .update("currentParticipants", event.getCurrentParticipants() + 1)
                            .addOnSuccessListener(aVoid1 -> {
                                event.setCurrentParticipants(event.getCurrentParticipants() + 1);
                                updateActionButton();
                                loadParticipants();
                                loadComments();
                                showCalendarPrompt();
                                Toast.makeText(getContext(), "Вы присоединились к мероприятию", Toast.LENGTH_SHORT).show();
                                animateCommentInput();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                Log.e(TAG, "Error updating participant count", e);
                                Toast.makeText(getContext(), "Ошибка обновления участников", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error adding participant", e);
                    Toast.makeText(getContext(), "Ошибка присоединения", Toast.LENGTH_SHORT).show();
                });
    }

    private void showCalendarPrompt() {
        new AlertDialog.Builder(getContext())
                .setTitle("Добавить в календарь")
                .setMessage("Хотите добавить мероприятие в ваш календарь?")
                .setPositiveButton("Да", (dialog, which) -> addToCalendar())
                .setNegativeButton("Нет", null)
                .show();
    }

    private void addToCalendar() {
        if (event.getStartDate() == null || event.getEndDate() == null) {
            Toast.makeText(getContext(), "Невозможно добавить в календарь: дата не указана", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(android.provider.CalendarContract.Events.CONTENT_URI)
                .putExtra(android.provider.CalendarContract.Events.TITLE, event.getTitle())
                .putExtra(android.provider.CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, event.getAddress())
                .putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartDate().toDate().getTime())
                .putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, event.getEndDate().toDate().getTime());
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка открытия календаря", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error adding to calendar", e);
        }
    }

    private void removeParticipant() {
        if ("completed".equals(event.getStatus())) {
            Log.d(TAG, "Cannot remove participant: event completed");
            updateActionButton();
            return;
        }

        db.collection("events").document(event.getEventId())
                .collection("Participants").document(currentUserId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    db.collection("events").document(event.getEventId())
                            .update("currentParticipants", event.getCurrentParticipants() - 1)
                            .addOnSuccessListener(aVoid1 -> {
                                event.setCurrentParticipants(event.getCurrentParticipants() - 1);
                                updateActionButton();
                                loadParticipants();
                                loadComments();
                                commentInputLayout.setVisibility(View.GONE);
                                Toast.makeText(getContext(), "Вы отменили участие", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                Log.e(TAG, "Error updating participant count", e);
                                Toast.makeText(getContext(), "Ошибка обновления участников", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error removing participant", e);
                    Toast.makeText(getContext(), "Ошибка отмены участия", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadOrganizer() {
        if (event == null || event.getOrganizerId() == null) {
            tvOrganizerName.setText("Неизвестный организатор");
            tvOrganizerRole.setText("Организатор");
            ivOrganizerPhoto.setImageResource(R.drawable.default_profile_picture);
            return;
        }

        db.collection("Profiles").document(event.getOrganizerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    if (documentSnapshot.exists()) {
                        UserProfile organizer = documentSnapshot.toObject(UserProfile.class);
                        if (organizer != null) {
                            tvOrganizerName.setText(organizer.getUsername() != null ? organizer.getUsername() : "Неизвестный организатор");
                            tvOrganizerRole.setText("Организатор");
                            if (organizer.getProfileImage() != null && !organizer.getProfileImage().isEmpty()) {
                                Glide.with(this)
                                        .load(organizer.getProfileImage())
                                        .placeholder(R.drawable.default_profile_picture)
                                        .error(R.drawable.default_image)
                                        .into(ivOrganizerPhoto);
                            } else {
                                ivOrganizerPhoto.setImageResource(R.drawable.default_profile_picture);
                            }
                        }
                    } else {
                        tvOrganizerName.setText("Неизвестный организатор");
                        tvOrganizerRole.setText("Организатор");
                        ivOrganizerPhoto.setImageResource(R.drawable.default_profile_picture);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    tvOrganizerName.setText("Ошибка загрузки организатора");
                    tvOrganizerRole.setText("Организатор");
                    ivOrganizerPhoto.setImageResource(R.drawable.default_profile_picture);
                });
    }

    private void loadParticipants() {
        if (event == null) return;

        db.collection("events").document(event.getEventId())
                .collection("Participants")
                .orderBy("joinedAt", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    participants.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        db.collection("Profiles").document(snapshot.getId())
                                .get()
                                .addOnSuccessListener(profileSnapshot -> {
                                    if (!isAdded()) return;
                                    if (profileSnapshot.exists()) {
                                        UserProfile participant = profileSnapshot.toObject(UserProfile.class);
                                        if (participant != null) {
                                            participants.add(participant);
                                            participantAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Ошибка загрузки участников", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadComments() {
        if (event == null) return;

        db.collection("events").document(event.getEventId())
                .collection("Comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    comments.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        CommentModel comment = snapshot.toObject(CommentModel.class);
                        if (comment != null) {
                            comment.setCommentId(snapshot.getId());
                            comments.add(comment);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                    Log.d(TAG, "Loaded " + comments.size() + " comments");
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error loading comments", e);
                    Toast.makeText(getContext(), "Ошибка загрузки комментариев", Toast.LENGTH_SHORT).show();
                });
    }

    private void postComment() {
        if (currentUserId == null || event == null) {
            Toast.makeText(getContext(), "Вы должны быть участником, чтобы комментировать", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = etComment.getText() != null ? etComment.getText().toString().trim() : "";
        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Введите комментарий", Toast.LENGTH_SHORT).show();
            return;
        }

        String commentId = db.collection("events").document(event.getEventId()).collection("Comments").document().getId();
        CommentModel comment = new CommentModel(
                commentId,
                event.getEventId(),
                currentUserId,
                null,
                null,
                content
        );
        List<String> imageUrls = new ArrayList<>();

        db.collection("events").document(event.getEventId())
                .collection("Comments").document(commentId)
                .set(comment)
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    etComment.setText("");
                    loadComments();
                    Toast.makeText(getContext(), "Комментарий добавлен", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Log.e(TAG, "Error posting comment", e);
                    Toast.makeText(getContext(), "Ошибка добавления комментария", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onLikeComment(String commentId, boolean isLiked) {
        if (currentUserId == null) return;

        db.collection("events").document(event.getEventId())
                .collection("Comments").document(commentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded()) return;
                    CommentModel comment = documentSnapshot.toObject(CommentModel.class);
                    if (comment != null) {
                        if (isLiked) {
                            comment.addLike(currentUserId);
                        } else {
                            comment.removeLike(currentUserId);
                        }
                        db.collection("events").document(event.getEventId())
                                .collection("Comments").document(commentId)
                                .update("likes", comment.getLikes())
                                .addOnSuccessListener(aVoid -> loadComments())
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Ошибка обновления лайков", Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Ошибка получения комментария", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDeleteComment(CommentModel comment) {
        if (currentUserId == null || !currentUserId.equals(comment.getAuthorId())) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Удалить комментарий")
                .setMessage("Вы уверены, что хотите удалить этот комментарий?")
                .setPositiveButton("Да", (dialog, which) -> {
                    db.collection("events").document(event.getEventId())
                            .collection("Comments").document(comment.getCommentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                if (!isAdded()) return;
                                loadComments();
                                Toast.makeText(getContext(), "Комментарий удален", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                Toast.makeText(getContext(), "Ошибка удаления комментария", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Нет", null)
                .show();
    }

    private void animateCommentInput() {
        if (commentInputLayout.getVisibility() == View.VISIBLE) return;

        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        commentInputLayout.setVisibility(View.VISIBLE);
        commentInputLayout.startAnimation(slideUp);

        etComment.requestFocus();
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(commentInputLayout);
        if (controller != null) {
            controller.show(WindowInsetsCompat.Type.ime());
        }

        nestedScrollView.post(() -> {
            if (nestedScrollView != null) {
                nestedScrollView.smoothScrollTo(0, commentInputLayout.getBottom());
            }
        });
    }

    private void promptForRating() {
        if (event == null || currentUserId == null || currentUserId.equals(event.getOrganizerId())) return;

        db.collection("events").document(event.getEventId())
                .collection("Participants").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) return;
                    if (snapshot.exists() && "completed".equals(event.getStatus())) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Оцените мероприятие")
                                .setMessage("Пожалуйста, оставьте отзыв об этом мероприятии.")
                                .setPositiveButton("Оценить", (dialog, which) -> openReviewDialog())
                                .setNegativeButton("Отказаться", null)
                                .show();
                    }
                });
    }

    private void openReviewDialog() {
        if (event != null && event.getOrganizerId() != null) {
            RateEventFragment fragment = RateEventFragment.newInstance(event.getEventId(), event.getOrganizerId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}