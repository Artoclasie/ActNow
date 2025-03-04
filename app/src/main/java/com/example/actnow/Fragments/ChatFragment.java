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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Adapters.ChatMessageAdapter;
import com.example.actnow.Models.ChatModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private String userId;
    private String currentUserId;
    private TextView tvUsername;
    private ImageView ivAvatar;
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView ivSendMessage;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private List<ChatModel> chatMessages;
    private ChatMessageAdapter chatMessageAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String userId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("uid", userId); // Используем chatUserId
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("uid"); // Используем chatUserId
            if (userId == null || userId.isEmpty()) {
                Log.e("ChatFragment", "User ID is null or empty");
                Toast.makeText(getContext(), "Error: User ID is missing", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed(); // Закрываем фрагмент, если userId отсутствует
            }
        } else {
            Log.e("ChatFragment", "No arguments passed");
            requireActivity().onBackPressed(); // Закрываем фрагмент, если аргументы отсутствуют
        }

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);

        tvUsername = rootView.findViewById(R.id.tv_chat_username);
        ivAvatar = rootView.findViewById(R.id.iv_chat_avatar);
        recyclerView = rootView.findViewById(R.id.recyclerView_chat);
        etMessage = rootView.findViewById(R.id.et_chat_message);
        ivSendMessage = rootView.findViewById(R.id.iv_send_message);

        // Проверка инициализации
        if (tvUsername == null) Log.e("ChatFragment", "tvUsername is null");
        if (ivAvatar == null) Log.e("ChatFragment", "ivAvatar is null");
        if (recyclerView == null) Log.e("ChatFragment", "recyclerView is null");
        if (etMessage == null) Log.e("ChatFragment", "etMessage is null");
        if (ivSendMessage == null) Log.e("ChatFragment", "ivSendMessage is null");

        chatMessages = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(chatMessages, currentUserId, userId);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(chatMessageAdapter);

        loadUserProfile();
        loadChatMessages();

        // Обработчики кликов для перехода на AnotherProfileFragment
        ivAvatar.setOnClickListener(v -> navigateToProfile());
        tvUsername.setOnClickListener(v -> navigateToProfile());

        ivSendMessage.setOnClickListener(v -> sendMessage());

        // Настройка прокрутки и отладка высоты
        rootView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                recyclerView.post(() -> recyclerView.scrollToPosition(chatMessages.size() - 1));
            }
            Log.d("ChatFragment", "RootView height: " + (bottom - top));
            Log.d("ChatFragment", "RecyclerView height: " + recyclerView.getHeight());
            if (chatMessages != null) {
                Log.d("ChatFragment", "Messages count: " + chatMessages.size());
            }
        });

        return rootView;
    }

    private void navigateToProfile() {
        if (userId != null && !userId.isEmpty() && getActivity() != null) {
            Log.d("ChatFragment", "Navigating to AnotherProfileFragment for userId: " + userId);
            AnotherProfileFragment profileFragment = AnotherProfileFragment.newInstance(userId);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, profileFragment); // Заменяем текущий фрагмент
            transaction.addToBackStack(null); // Добавляем в стек возврата
            transaction.commit();
        } else {
            Log.e("ChatFragment", "User ID is null or activity is null, cannot navigate");
            Toast.makeText(getContext(), "Error: Cannot navigate to profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUserProfile() {
        if (userId == null || userId.isEmpty()) {
            Log.e("ChatFragment", "User ID is null or empty");
            return;
        }

        DocumentReference userDocRef = firebaseFirestore.collection("Profiles").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                tvUsername.setText(username != null ? username : "Unknown User");

                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(getContext()).load(profileImageUrl).into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.default_profile_picture);
                }
            } else {
                Log.e("ChatFragment", "User profile not found");
                Toast.makeText(getContext(), "User profile not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e("ChatFragment", "Error loading user profile", e);
            Toast.makeText(getContext(), "Error loading user profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadChatMessages() {
        if (currentUserId == null || userId == null) {
            Log.e("ChatFragment", "Current user ID or target user ID is null");
            return;
        }

        String chatId = currentUserId.compareTo(userId) < 0 ? currentUserId + "_" + userId : userId + "_" + currentUserId;
        Log.d("ChatFragment", "Loading messages for chatId: " + chatId);

        CollectionReference messagesRef = firebaseFirestore
                .collection("Chats")
                .document(chatId)
                .collection("Messages");

        Query query = messagesRef.orderBy("timestamp", Query.Direction.ASCENDING); // Сортировка по возрастанию

        query.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w("ChatFragment", "Listen failed: " + e.getMessage(), e);
                return;
            }

            if (snapshot != null) {
                chatMessages.clear();
                Log.d("ChatFragment", "Snapshot received with " + snapshot.size() + " documents");
                for (DocumentSnapshot document : snapshot.getDocuments()) {
                    ChatModel message = document.toObject(ChatModel.class);
                    if (message != null) {
                        Log.d("ChatFragment", "Loaded message: " + message.getMessageText() + " at " + message.getTimestamp());
                        chatMessages.add(message);
                    } else {
                        Log.w("ChatFragment", "Failed to convert document to ChatModel: " + document.getId());
                    }
                }
                chatMessageAdapter.notifyDataSetChanged();
                recyclerView.post(() -> recyclerView.scrollToPosition(chatMessages.size() - 1)); // Прокручиваем к последнему сообщению
            } else {
                Log.w("ChatFragment", "Snapshot is null");
            }
        });
    }

    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null || userId == null) {
            Log.e("ChatFragment", "Current user ID or target user ID is null");
            return;
        }

        String chatId = currentUserId.compareTo(userId) < 0 ? currentUserId + "_" + userId : userId + "_" + currentUserId;

        CollectionReference messagesRef = firebaseFirestore
                .collection("Chats")
                .document(chatId)
                .collection("Messages");

        String messageId = messagesRef.document().getId();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("messageText", messageText);
        messageMap.put("senderId", currentUserId);
        messageMap.put("receiverId", userId);
        messageMap.put("timestamp", System.currentTimeMillis());

        messagesRef.document(messageId).set(messageMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ChatFragment", "Message sent successfully");
                    updateParticipants(currentUserId, userId);
                    updateParticipants(userId, currentUserId);
                    etMessage.setText(""); // Очищаем поле ввода
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatFragment", "Error sending message: " + e.getMessage(), e);
                    Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateParticipants(String documentId, String participantId) {
        DocumentReference chatDocRef = firebaseFirestore.collection("Chats").document(documentId);
        chatDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> participants = (List<String>) documentSnapshot.get("participants");
                if (participants == null) {
                    participants = new ArrayList<>();
                }

                if (!participants.contains(participantId)) {
                    participants.add(participantId);
                    chatDocRef.update("participants", participants)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("ChatFragment", "Participants updated for user: " + documentId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ChatFragment", "Error updating participants: " + e.getMessage(), e);
                            });
                }
            } else {
                Map<String, Object> chatData = new HashMap<>();
                List<String> participants = new ArrayList<>();
                participants.add(participantId);
                chatData.put("participants", participants);

                chatDocRef.set(chatData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ChatFragment", "Chat document created with participants for user: " + documentId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ChatFragment", "Error creating chat document: " + e.getMessage(), e);
                        });
            }
        }).addOnFailureListener(e -> {
            Log.e("ChatFragment", "Error getting chat document: " + e.getMessage(), e);
        });
    }
}