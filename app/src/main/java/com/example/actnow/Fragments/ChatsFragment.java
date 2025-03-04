package com.example.actnow.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.AnotherProfileActivity;
import com.example.actnow.Models.MessageModel;
import com.example.actnow.Models.ProfileModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatsFragment extends Fragment {
    private RecyclerView rvChats;
    private ChatAdapter chatAdapter;
    private List<ProfileModel> chatUsers = new ArrayList<>();
    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration chatListener;

    public ChatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        rvChats = view.findViewById(R.id.recyclerViewChats);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("ChatsFragment", "Current user ID: " + currentUserId);
        db = FirebaseFirestore.getInstance();

        // Инициализация адаптера
        chatAdapter = new ChatAdapter(chatUsers);
        rvChats.setAdapter(chatAdapter);

        // Создание документа, если он отсутствует
        createChatDocumentIfNotExists();

        loadChatUsers();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (chatListener != null) {
            chatListener.remove(); // Удаляем Listener
        }
    }

    private void createChatDocumentIfNotExists() {
        db.collection("Chats")
                .document(currentUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (!document.exists()) {
                            // Создаем новый документ, если он отсутствует
                            Map<String, Object> chatData = new HashMap<>();
                            chatData.put("participants", new ArrayList<String>()); // Пустой список участников
                            db.collection("Chats")
                                    .document(currentUserId)
                                    .set(chatData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("ChatsFragment", "Chat document created for user: " + currentUserId);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("ChatsFragment", "Error creating chat document: ", e);
                                    });
                        }
                    } else {
                        Log.e("ChatsFragment", "Error checking chat document: ", task.getException());
                    }
                });
    }

    private void loadChatUsers() {
        chatListener = db.collection("Chats")
                .document(currentUserId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("ChatsFragment", "Error listening to chat document: ", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (participants != null && !participants.isEmpty()) {
                            Log.d("ChatsFragment", "Total chat participants: " + participants.size());

                            // Очищаем текущий список пользователей
                            chatUsers.clear();

                            for (String userId : participants) {
                                if (!userId.equals(currentUserId)) { // Исключаем текущего пользователя
                                    Log.d("ChatsFragment", "Chat participant: " + userId);
                                    loadUserProfile(userId); // Загружаем профиль участника
                                    loadLastMessage(userId); // Загружаем последнее сообщение
                                }
                            }
                        } else {
                            Log.e("ChatsFragment", "No chat participants found");
                        }
                    } else {
                        Log.e("ChatsFragment", "Chat document does not exist");
                    }
                });
    }

    private void loadLastMessage(String userId) {
        String chatId = currentUserId + "_" + userId; // Формируем chatId
        Log.d("ChatsFragment", "Loading last message for chatId: " + chatId);

        // Создаем ссылку на подколлекцию Messages для этого чата
        CollectionReference messagesRef = db
                .collection("Chats")
                .document(chatId)
                .collection("Messages");

        // Загружаем последнее сообщение
        messagesRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        MessageModel lastMessage = document.toObject(MessageModel.class);
                        if (lastMessage != null) {
                            Log.d("ChatsFragment", "Last message: " + lastMessage.getContent());
                            Log.d("ChatsFragment", "Last message timestamp: " + lastMessage.getTimestamp());

                            // Обновляем данные в списке пользователей
                            for (ProfileModel user : chatUsers) {
                                if (user.getUid().equals(userId)) {
                                    user.setLastMessage(lastMessage.getContent());
                                    user.setLastMessageTimestamp(lastMessage.getTimestamp());
                                    chatAdapter.notifyDataSetChanged();
                                    Log.d("ChatsFragment", "Last message and timestamp updated for user: " + userId);
                                    break;
                                }
                            }
                        } else {
                            Log.e("ChatsFragment", "Last message is null for chatId: " + chatId);
                        }
                    } else {
                        Log.d("ChatsFragment", "No messages found for chatId: " + chatId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatsFragment", "Error loading last message: ", e);
                });
    }

    private void loadUserProfile(String userId) {
        db.collection("Profiles")
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ProfileModel user = document.toObject(ProfileModel.class);
                            if (user != null) {
                                Log.d("ChatsFragment", "User loaded: " + user.getUsername() + ", Avatar URL: " + user.getProfileImageUrl());
                                chatUsers.add(user);
                                if (chatAdapter != null) {
                                    chatAdapter.notifyDataSetChanged(); // Обновляем адаптер
                                }
                            } else {
                                Log.e("ChatsFragment", "User data is null for userId: " + userId);
                            }
                        } else {
                            Log.e("ChatsFragment", "No such profile with userId: " + userId);
                        }
                    } else {
                        Log.e("ChatsFragment", "Error getting profile: ", task.getException());
                    }
                });
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<ProfileModel> users;

        public ChatAdapter(List<ProfileModel> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ProfileModel user = users.get(position);
            if (user == null) {
                Log.e("ChatAdapter", "User is null at position: " + position);
                return;
            }

            // Отображаем имя пользователя и аватар
            holder.tvUserName.setText(user.getUsername());
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfileImageUrl())
                    .placeholder(R.drawable.default_profile_picture)
                    .into(holder.ivUserAvatar);

            // Отображаем последнее сообщение и время
            if (user.getLastMessage() != null) {
                holder.tvLastMessage.setText(user.getLastMessage());
                holder.tvLastMessageTime.setText(formatTimestamp(user.getLastMessageTimestamp()));
            } else {
                holder.tvLastMessage.setText("No messages yet");
                holder.tvLastMessageTime.setText("");
            }

            // Обработка клика по элементу списка
            holder.itemView.setOnClickListener(v -> openChatFragment(user));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        // Метод для форматирования времени
        private String formatTimestamp(long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserName;
            ImageView ivUserAvatar;
            TextView tvLastMessage; // Для последнего сообщения
            TextView tvLastMessageTime; // Для времени отправки

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tv_chat_username);
                ivUserAvatar = itemView.findViewById(R.id.imv_post_uid);
                tvLastMessage = itemView.findViewById(R.id.tv_chat_last_message); // ID для последнего сообщения
                tvLastMessageTime = itemView.findViewById(R.id.tv_chat_time); // ID для времени
            }
        }
    }

    private static final int REQUEST_CODE_CHAT = 1001;

    private void openChatFragment(ProfileModel user) {
        if (user == null) {
            Log.e("ChatsFragment", "User is null");
            return;
        }

        String userId = user.getUid();
        if (userId == null || userId.isEmpty()) {
            Log.e("ChatsFragment", "User ID is null or empty");
            return;
        }

        Intent intent = new Intent(getContext(), AnotherProfileActivity.class);
        intent.putExtra("uid", userId); // Передаем userId
        intent.putExtra("fromChatsFragment", true); // Флаг, указывающий, что переход из ChatsFragment
        startActivityForResult(intent, REQUEST_CODE_CHAT); // Используем startActivityForResult
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHAT) {
            // Возвращаемся в ChatsFragment
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack(); // Закрываем ChatFragment
            }
        }
    }
}