package com.example.actnow.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Adapters.ChatMessageAdapter;
import com.example.actnow.Models.ChatModel;
import com.example.actnow.Models.MessageModel;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {
    private String userId;
    private String currentUserId;
    private String chatId;
    private TextView tvUsername;
    private ImageView ivAvatar;
    private ImageView ivBackArrow;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView ivSendButton;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private List<MessageModel> messages;
    private ChatMessageAdapter messageAdapter;
    private NestedScrollView nestedScrollView;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String userId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("uid", userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (getArguments() != null) {
            userId = getArguments().getString("uid");
            if (userId == null || userId.isEmpty()) {
                Log.e("ChatFragment", "User ID is null or empty");
                Toast.makeText(getContext(), "Error: User ID is missing", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                return;
            } else if (currentUser == null) {
                Log.e("ChatFragment", "User not authenticated");
                Toast.makeText(getContext(), "Ошибка: Пользователь не авторизован", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                return;
            } else if (userId.equals(currentUser.getUid())) {
                Log.e("ChatFragment", "Self-chat detected with userId: " + userId);
                Toast.makeText(getContext(), "Ошибка: Невозможно начать чат с самим собой", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                return;
            }
        } else {
            Log.e("ChatFragment", "No arguments passed");
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }

        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = currentUser.getUid();
        chatId = currentUserId.compareTo(userId) < 0 ? currentUserId + "_" + userId : userId + "_" + currentUserId;
        messages = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        tvUsername = view.findViewById(R.id.tv_chat_username);
        ivAvatar = view.findViewById(R.id.iv_chat_avatar);
        ivBackArrow = view.findViewById(R.id.iv_back_arrow);
        recyclerView = view.findViewById(R.id.recyclerView_chat);
        etMessage = view.findViewById(R.id.et_chat_message);
        ivSendButton = view.findViewById(R.id.iv_send_button);
        nestedScrollView = view.findViewById(R.id.nested_scroll_view);

        // Убедимся, что фрагмент растягивается на весь экран
        view.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Отключаем ограничения контейнера
        if (view.getParent() != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            parent.setClipChildren(false);
            parent.setClipToPadding(false);
        }

        // Disable state restoration to prevent ClassCastException
        recyclerView.setSaveEnabled(false);

        // Настройка RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new ChatMessageAdapter();
        recyclerView.setAdapter(messageAdapter);

        // Загрузка данных пользователя
        loadUserData();

        // Загрузка сообщений
        loadMessages();

        // Add click listeners for navigating to AnotherProfileFragment
        ivAvatar.setOnClickListener(v -> navigateToAnotherProfile());
        tvUsername.setOnClickListener(v -> navigateToAnotherProfile());

        // Add click listener for back arrow
        ivBackArrow.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // Add click listener for send button
        ivSendButton.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage();
            }
        });

        // Прокрутка к EditText при фокусе
        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d("ChatFragment", "EditText focused, scrolling to it");
                nestedScrollView.post(() -> {
                    int[] location = new int[2];
                    etMessage.getLocationOnScreen(location);
                    int y = location[1];
                    nestedScrollView.smoothScrollTo(0, y);
                });
            }
        });

        return view;
    }

    private void loadUserData() {
        firebaseFirestore.collection("Profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);
                        if (user != null) {
                            tvUsername.setText(user.getUsername());
                            String profileImage = user.getProfileImage();
                            if (profileImage != null && !profileImage.isEmpty()) {
                                Glide.with(this)
                                        .load(profileImage)
                                        .placeholder(R.drawable.default_profile_picture)
                                        .error(R.drawable.default_profile_picture)
                                        .circleCrop()
                                        .into(ivAvatar);
                            } else {
                                ivAvatar.setImageResource(R.drawable.default_profile_picture);
                            }
                        } else {
                            Log.w("ChatFragment", "UserProfile is null for userId: " + userId);
                            tvUsername.setText("Unknown User");
                            ivAvatar.setImageResource(R.drawable.default_profile_picture);
                        }
                    } else {
                        Log.w("ChatFragment", "Profile not found for userId: " + userId);
                        tvUsername.setText("Unknown User");
                        ivAvatar.setImageResource(R.drawable.default_profile_picture);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatFragment", "Error loading user data: " + e.getMessage());
                    Toast.makeText(getContext(), "Error loading user data", Toast.LENGTH_SHORT).show();
                    tvUsername.setText("Unknown User");
                    ivAvatar.setImageResource(R.drawable.default_profile_picture);
                });
    }

    private void loadMessages() {
        CollectionReference messagesRef = firebaseFirestore
                .collection("Chats")
                .document(chatId)
                .collection("Messages");

        Query query = messagesRef.orderBy("createdAt", Query.Direction.ASCENDING);

        query.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("ChatFragment", "Listen failed: " + e.getMessage(), e);
                return;
            }

            if (snapshot != null) {
                messages.clear();
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    MessageModel message = document.toObject(MessageModel.class);
                    if (message != null) {
                        messages.add(message);
                    }
                }
                Log.d("ChatFragment", "Loaded " + messages.size() + " messages");
                messageAdapter.setMessages(messages);
                recyclerView.setVisibility(View.VISIBLE);
                // Прокручиваем к последнему сообщению
                recyclerView.post(() -> {
                    recyclerView.scrollToPosition(messages.size() - 1);
                });
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        CollectionReference messagesRef = firebaseFirestore
                .collection("Chats")
                .document(chatId)
                .collection("Messages");

        String messageId = messagesRef.document().getId();

        MessageModel message = new MessageModel();
        message.setMessageId(messageId);
        message.setChatId(chatId);
        message.setSenderId(currentUserId);
        message.setContent(messageText);
        message.setType("text");
        message.setCreatedAt(Timestamp.now());

        // Initialize chat document if it doesn't exist
        DocumentReference chatRef = firebaseFirestore.collection("Chats").document(chatId);
        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    ChatModel chat = new ChatModel(chatId, currentUserId, userId);
                    chatRef.set(chat.toMap())
                            .addOnFailureListener(e -> Log.e("ChatFragment", "Error initializing chat: " + e.getMessage()));
                }
            } else {
                Log.e("ChatFragment", "Error checking chat existence: " + task.getException());
            }
        });

        messagesRef.document(messageId).set(message.toMap())
                .addOnSuccessListener(aVoid -> {
                    updateChatMetadata(messageText);
                    etMessage.setText("");
                    // Прокручиваем к последнему сообщению после отправки
                    recyclerView.post(() -> {
                        recyclerView.scrollToPosition(messages.size());
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatFragment", "Error sending message: " + e.getMessage());
                    Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChatMetadata(String lastMessage) {
        DocumentReference chatRef = firebaseFirestore.collection("Chats").document(chatId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastMessageTime", Timestamp.now());
        updates.put("updatedAt", Timestamp.now());

        Map<String, Object> unreadCount = new HashMap<>();
        unreadCount.put(userId, 1);
        updates.put("unreadCount", unreadCount);

        chatRef.set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnFailureListener(e -> {
                    Log.e("ChatFragment", "Error updating chat metadata: " + e.getMessage());
                });
    }

    private void navigateToAnotherProfile() {
        if (getActivity() != null && userId != null) {
            AnotherProfileFragment fragment = AnotherProfileFragment.newInstance(userId);
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Log.e("ChatFragment", "Unable to navigate to AnotherProfileFragment: userId or activity is null");
            Toast.makeText(getContext(), "Error navigating to profile", Toast.LENGTH_SHORT).show();
        }
    }
}