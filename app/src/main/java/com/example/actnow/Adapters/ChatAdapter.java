package com.example.actnow.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Models.ChatModel;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final String TAG = "ChatAdapter";
    private List<ChatModel> chats;
    private Context context;
    private OnChatClickListener listener;
    private String currentUserId;
    private FirebaseFirestore db;
    private Map<String, UserProfile> userProfilesCache;

    public interface OnChatClickListener {
        void onChatClick(ChatModel chat);
    }

    public ChatAdapter(Context context, String currentUserId, OnChatClickListener listener) {
        this.context = context.getApplicationContext();
        this.chats = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
        this.userProfilesCache = new HashMap<>();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chats.get(position);
        Log.d(TAG, "Binding chat at position " + position + ": " + chat.getChatId() + ", participants: " + chat.getParticipants());
        bindPrivateChat(holder, chat);
        bindCommonViews(holder, chat);
    }

    private void bindPrivateChat(ChatViewHolder holder, ChatModel chat) {
        String otherUserId = getOtherUserId(chat);
        if (otherUserId == null || otherUserId.isEmpty()) {
            holder.tvUsername.setText("Неизвестный пользователь");
            Glide.with(context)
                    .load(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(holder.ivUserAvatar);
            return;
        }

        if (userProfilesCache.containsKey(otherUserId)) {
            bindUserProfile(holder, userProfilesCache.get(otherUserId));
        } else {
            holder.tvUsername.setText("Загрузка...");
            Glide.with(context)
                    .load(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(holder.ivUserAvatar);
            loadUserProfile(otherUserId, holder);
        }
    }

    private void bindUserProfile(ChatViewHolder holder, UserProfile profile) {
        if (profile != null) {
            holder.tvUsername.setText(profile.getUsername() != null ?
                    profile.getUsername() : "Пользователь");
            String profileImage = profile.getProfileImage();
            if (profileImage != null && !profileImage.isEmpty()) {
                Glide.with(context)
                        .load(profileImage)
                        .placeholder(R.drawable.default_profile_picture)
                        .error(R.drawable.default_profile_picture)
                        .circleCrop()
                        .into(holder.ivUserAvatar);
            } else {
                Glide.with(context)
                        .load(R.drawable.default_profile_picture)
                        .circleCrop()
                        .into(holder.ivUserAvatar);
            }
        } else {
            holder.tvUsername.setText("Пользователь");
            Glide.with(context)
                    .load(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(holder.ivUserAvatar);
        }
    }

    private void loadUserProfile(String userId, ChatViewHolder holder) {
        db.collection("Profiles").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!holder.itemView.isAttachedToWindow()) {
                        return;
                    }
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        UserProfile profile = document.toObject(UserProfile.class);
                        userProfilesCache.put(userId, profile);
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && position < chats.size()) {
                            ChatModel chat = chats.get(position);
                            if (getOtherUserId(chat).equals(userId)) {
                                bindUserProfile(holder, profile);
                            }
                        }
                    } else {
                        int position = holder.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION && position < chats.size()) {
                            holder.tvUsername.setText("Пользователь");
                            Glide.with(context)
                                    .load(R.drawable.default_profile_picture)
                                    .circleCrop()
                                    .into(holder.ivUserAvatar);
                        }
                    }
                });
    }

    private void bindCommonViews(ChatViewHolder holder, ChatModel chat) {
        if (chat.getLastMessage() != null) {
            holder.tvLastMessage.setText(chat.getLastMessage());
            if (chat.getLastMessageTime() != null) {
                String timeFormat = isToday(chat.getLastMessageTime().toDate()) ? "HH:mm" : "dd MMM";
                SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
                holder.tvLastMessageTime.setText(sdf.format(chat.getLastMessageTime().toDate()));
            } else {
                holder.tvLastMessageTime.setText("");
            }

            int unreadCount = chat.getUnreadCount().getOrDefault(currentUserId, 0);
            if (unreadCount > 0) {
                holder.tvUnreadCount.setVisibility(View.VISIBLE);
                holder.tvUnreadCount.setText(unreadCount > 9 ? "9+" : String.valueOf(unreadCount));
            } else {
                holder.tvUnreadCount.setVisibility(View.GONE);
            }
        } else {
            holder.tvLastMessage.setText("Нет сообщений");
            holder.tvLastMessageTime.setText("");
            holder.tvUnreadCount.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    private boolean isToday(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return fmt.format(date).equals(fmt.format(new Date()));
    }

    private String getOtherUserId(ChatModel chat) {
        for (String participant : chat.getParticipants()) {
            if (!participant.equals(currentUserId)) {
                return participant;
            }
        }
        return null;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + chats.size());
        return chats.size();
    }

    public void updateChats(List<ChatModel> newChats) {
        this.chats.clear();
        this.chats.addAll(newChats != null ? new ArrayList<>(newChats) : new ArrayList<>());
        Log.d(TAG, "updateChats: " + chats.size() + " items");
        notifyDataSetChanged();
    }

    public void addChat(ChatModel chat) {
        this.chats.add(chat);
        notifyItemInserted(chats.size() - 1);
    }

    public void updateChat(ChatModel updatedChat) {
        for (int i = 0; i < chats.size(); i++) {
            if (chats.get(i).getChatId().equals(updatedChat.getChatId())) {
                chats.set(i, updatedChat);
                notifyItemChanged(i);
                break;
            }
        }
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUsername;
        TextView tvLastMessage;
        TextView tvLastMessageTime;
        TextView tvUnreadCount;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvLastMessageTime = itemView.findViewById(R.id.tvLastMessageTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}