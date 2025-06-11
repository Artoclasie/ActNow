package com.example.actnow.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.actnow.Adapters.ChatAdapter;
import com.example.actnow.Models.ChatModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
    private static final String TAG = "ChatsFragment";
    private RecyclerView rvChats;
    private TextView tvEmptyChats;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChatAdapter chatAdapter;
    private List<ChatModel> chats;
    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration chatListener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ChatsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        rvChats = view.findViewById(R.id.recyclerViewChats);
        tvEmptyChats = view.findViewById(R.id.tvEmptyChats);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        rvChats.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated");
            Toast.makeText(getContext(), "Ошибка: Пользователь не авторизован", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return view;
        }
        currentUserId = currentUser.getUid();
        Log.d(TAG, "Current user ID: " + currentUserId);

        db = FirebaseFirestore.getInstance();
        chats = new ArrayList<>();

        chatAdapter = new ChatAdapter(getContext(), currentUserId, chat -> openChatFragment(chat));
        rvChats.setAdapter(chatAdapter);

        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshChats);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        setupChatListener();
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (chatListener != null) {
            chatListener.remove();
        }
    }

    private void setupChatListener() {
        chatListener = db.collection("Chats")
                .whereArrayContains("participants", currentUserId)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening to chats: " + error.getMessage());
                        mainHandler.post(() -> {
                            Toast.makeText(getContext(), "Ошибка загрузки чатов: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            swipeRefreshLayout.setRefreshing(false);
                        });
                        return;
                    }

                    List<ChatModel> tempChats = new ArrayList<>();
                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            ChatModel chat = document.toObject(ChatModel.class);
                            if (chat != null) {
                                chat.setChatId(document.getId());
                                if (chat.getLastMessageTime() == null) {
                                    chat.setLastMessageTime(chat.getUpdatedAt() != null ? chat.getUpdatedAt() : chat.getCreatedAt());
                                }
                                tempChats.add(chat);
                                Log.d(TAG, "Added chat with ID: " + chat.getChatId() + ", participants: " + chat.getParticipants() + ", lastMessage: " + chat.getLastMessage());
                            }
                        }
                        Log.d(TAG, "Loaded " + tempChats.size() + " chats for user: " + currentUserId);
                    } else {
                        Log.w(TAG, "No chats found for user: " + currentUserId);
                    }

                    // Update UI on the main thread
                    mainHandler.post(() -> {
                        if (getActivity() == null) return; // Проверяем, что фрагмент всё ещё привязан
                        chats.clear();
                        chats.addAll(new ArrayList<>(tempChats));
                        Log.d(TAG, "Updated chats list with " + chats.size() + " items before adapter update");
                        chatAdapter.updateChats(new ArrayList<>(chats));
                        updateEmptyState();
                        Log.d(TAG, "Updated chats list with " + chats.size() + " items after adapter update");
                        swipeRefreshLayout.setRefreshing(false);
                    });
                });
    }

    private void refreshChats() {
        if (chatListener != null) {
            chatListener.remove();
        }
        setupChatListener();
    }

    private void updateEmptyState() {
        Log.d(TAG, "updateEmptyState: chats size = " + (chats != null ? chats.size() : "null"));
        if (chats == null || chats.isEmpty()) {
            rvChats.setVisibility(View.GONE);
            tvEmptyChats.setVisibility(View.VISIBLE);
            Log.d(TAG, "Showing empty state: chats list is empty");
        } else {
            rvChats.setVisibility(View.VISIBLE);
            tvEmptyChats.setVisibility(View.GONE);
            Log.d(TAG, "Showing RecyclerView: " + chats.size() + " chats loaded");
        }
    }

    private void openChatFragment(ChatModel chat) {
        String otherUserId = getOtherUserId(chat);
        if (otherUserId != null && !otherUserId.equals(currentUserId)) {
            ChatFragment chatFragment = ChatFragment.newInstance(otherUserId);
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, chatFragment)
                        .addToBackStack(null)
                        .commit();
            }
        } else {
            Toast.makeText(getContext(), "Ошибка: Невозможно начать чат с самим собой", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Attempted to start self-chat with userId: " + otherUserId);
        }
    }

    private String getOtherUserId(ChatModel chat) {
        for (String participant : chat.getParticipants()) {
            if (!participant.equals(currentUserId)) {
                return participant;
            }
        }
        return null;
    }
}