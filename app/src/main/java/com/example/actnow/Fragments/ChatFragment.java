package com.example.actnow.Fragments;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
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
import com.example.actnow.AnotherProfileActivity; // Import the new activity
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private String userId;
    private String currentUserId;
    private String chatId;
    private TextView tvUsername;
    private ImageView ivAvatar;
    private CardView cvAvatar;
    private ImageView ivBackArrow;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView ivSendButton;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private List<MessageModel> messages;
    private ChatMessageAdapter messageAdapter;
    private NestedScrollView nestedScrollView;
    private View rootView;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    private ListenerRegistration messagesListener;

    public ChatFragment() {
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
                showErrorAndBack("Error: User ID is missing");
                return;
            } else if (currentUser == null) {
                showErrorAndBack("Ошибка: Пользователь не авторизован");
                return;
            } else if (userId.equals(currentUser.getUid())) {
                showErrorAndBack("Ошибка: Невозможно начать чат с самим собой");
                return;
            }
        } else {
            showErrorAndBack("No arguments passed");
            return;
        }

        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = currentUser.getUid();
        chatId = currentUserId.compareTo(userId) < 0 ? currentUserId + "_" + userId : userId + "_" + currentUserId;
        messages = new ArrayList<>();
    }

    private void showErrorAndBack(String message) {
        Log.e(TAG, message);
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        initViews();
        setupRecyclerView();
        setupKeyboardBehavior();
        setupClickListeners();
        loadUserData();
        loadMessages();
        return rootView;
    }

    private void initViews() {
        tvUsername = rootView.findViewById(R.id.tv_chat_username);
        cvAvatar = rootView.findViewById(R.id.cv_profile_image);
        ivAvatar = rootView.findViewById(R.id.iv_chat_avatar);
        ivBackArrow = rootView.findViewById(R.id.iv_back_arrow);
        recyclerView = rootView.findViewById(R.id.recyclerView_chat);
        etMessage = rootView.findViewById(R.id.et_chat_message);
        ivSendButton = rootView.findViewById(R.id.iv_send_button);
        nestedScrollView = rootView.findViewById(R.id.nested_scroll_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new ChatMessageAdapter();
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setHasFixedSize(true);
    }

    private void setupKeyboardBehavior() {
        keyboardLayoutListener = () -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;
            Log.d(TAG, "Keyboard height: " + keypadHeight + ", Screen height: " + screenHeight);

            if (keypadHeight > screenHeight * 0.15) {
                recyclerView.postDelayed(() -> {
                    if (messages.size() > 0) {
                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                        Log.d(TAG, "Keyboard shown, scrolled to position: " + (messages.size() - 1));
                    }
                    nestedScrollView.post(() -> {
                        nestedScrollView.smoothScrollTo(0, etMessage.getBottom());
                    });
                }, 200);
            }
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
    }

    private void setupClickListeners() {
        cvAvatar.setOnClickListener(v -> navigateToAnotherProfile());
        tvUsername.setOnClickListener(v -> navigateToAnotherProfile());

        ivBackArrow.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        ivSendButton.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage();
            }
        });

        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && messages.size() > 0) {
                recyclerView.postDelayed(() -> {
                    recyclerView.smoothScrollToPosition(messages.size() - 1);
                    nestedScrollView.smoothScrollTo(0, etMessage.getBottom());
                    Log.d(TAG, "EditText focused, scrolled to position: " + (messages.size() - 1));
                }, 200);
            }
        });
    }

    private void loadUserData() {
        firebaseFirestore.collection("Profiles").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);
                        if (user != null) {
                            updateUserInfo(user);
                        } else {
                            setDefaultUserInfo();
                        }
                    } else {
                        setDefaultUserInfo();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user data", e);
                    setDefaultUserInfo();
                });
    }

    private void updateUserInfo(UserProfile user) {
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
    }

    private void setDefaultUserInfo() {
        tvUsername.setText("Unknown User");
        ivAvatar.setImageResource(R.drawable.default_profile_picture);
    }

    private void loadMessages() {
        messagesListener = firebaseFirestore.collection("Chats").document(chatId)
                .collection("Messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed: " + e.getMessage());
                        Toast.makeText(getContext(), "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        int previousSize = messages.size();
                        messages.clear();
                        for (DocumentSnapshot document : snapshot.getDocuments()) {
                            MessageModel message = document.toObject(MessageModel.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        Log.d(TAG, "Loaded " + messages.size() + " messages, previous size: " + previousSize);
                        messageAdapter.setMessages(messages);
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.postDelayed(() -> {
                            if (messages.size() > 0) {
                                recyclerView.smoothScrollToPosition(messages.size() - 1);
                                Log.d(TAG, "Scrolled to last message at position: " + (messages.size() - 1));
                            }
                        }, 200);
                    } else {
                        Log.d(TAG, "No messages found for chatId: " + chatId);
                        messages.clear();
                        messageAdapter.setMessages(messages);
                        messageAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) return;

        MessageModel message = new MessageModel();
        message.setMessageId(firebaseFirestore.collection("Chats").document(chatId)
                .collection("Messages").document().getId());
        message.setChatId(chatId);
        message.setSenderId(currentUserId);
        message.setContent(messageText);
        message.setType("text");
        message.setCreatedAt(Timestamp.now());

        DocumentReference chatRef = firebaseFirestore.collection("Chats").document(chatId);
        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                chatRef.set(new ChatModel(chatId, currentUserId, userId).toMap());
            }
        });

        chatRef.collection("Messages").document(message.getMessageId())
                .set(message.toMap())
                .addOnSuccessListener(aVoid -> {
                    etMessage.setText("");
                    updateChatMetadata(messageText);
                    Log.d(TAG, "Message sent: " + messageText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sending message: " + e.getMessage());
                    Toast.makeText(getContext(), "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateChatMetadata(String lastMessage) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastMessage", lastMessage);
        updates.put("lastMessageTime", Timestamp.now());
        updates.put("updatedAt", Timestamp.now());

        Map<String, Object> unreadCount = new HashMap<>();
        unreadCount.put(userId, 1);
        updates.put("unreadCount", unreadCount);

        firebaseFirestore.collection("Chats").document(chatId)
                .set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat metadata updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating chat metadata: " + e.getMessage()));
    }

    private void navigateToAnotherProfile() {
        if (!isAdded() || userId == null || userId.isEmpty()) {
            Log.e(TAG, "Navigation failed: Fragment not added or userId is null/empty, userId=" + userId);
            Toast.makeText(requireContext(), "Ошибка: ID пользователя отсутствует", Toast.LENGTH_SHORT).show();
            return;
        }
        if (getActivity() == null) {
            Log.e(TAG, "Navigation failed: Activity is null");
            return;
        }

        Log.d(TAG, "Navigating to AnotherProfileActivity with userId: " + userId);
        Intent intent = new Intent(getActivity(), AnotherProfileActivity.class);
        intent.putExtra("uid", userId);
        intent.putExtra("fromChatsFragment", false);
        startActivity(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (messagesListener != null) {
            messagesListener.remove();
            messagesListener = null;
            Log.d(TAG, "Messages listener removed");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rootView != null && keyboardLayoutListener != null) {
            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
        rootView = null;
        Log.d(TAG, "View destroyed, cleaned up resources");
    }
}