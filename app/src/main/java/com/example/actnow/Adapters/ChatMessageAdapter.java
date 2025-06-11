package com.example.actnow.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Models.MessageModel;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<MessageModel> messages;
    private String currentUserId;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_SENT_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    public ChatMessageAdapter() {
        this.messages = new ArrayList<>();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(view);
            case VIEW_TYPE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(view);
            case VIEW_TYPE_SENT_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent_image, parent, false);
                return new SentImageViewHolder(view);
            case VIEW_TYPE_RECEIVED_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received_image, parent, false);
                return new ReceivedImageViewHolder(view);
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_SENT:
                ((SentMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_RECEIVED:
                ((ReceivedMessageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_SENT_IMAGE:
                ((SentImageViewHolder) holder).bind(message);
                break;
            case VIEW_TYPE_RECEIVED_IMAGE:
                ((ReceivedImageViewHolder) holder).bind(message);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = messages.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);

        if ("image".equals(message.getType())) {
            return isSent ? VIEW_TYPE_SENT_IMAGE : VIEW_TYPE_RECEIVED_IMAGE;
        } else {
            return isSent ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
        }
    }

    public void addMessage(MessageModel message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<MessageModel> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    // ViewHolder for sent text messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(MessageModel message) {
            if (tvMessage != null) {
                tvMessage.setText(message.getContent());
            }
            if (tvTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = message.getCreatedAt() != null ?
                        message.getCreatedAt().toDate() : new Date();
                tvTime.setText(sdf.format(date));
            }
        }
    }

    // ViewHolder for received text messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(MessageModel message) {
            if (tvMessage != null) {
                tvMessage.setText(message.getContent());
            }
            if (tvTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = message.getCreatedAt() != null ?
                        message.getCreatedAt().toDate() : new Date();
                tvTime.setText(sdf.format(date));
            }
        }
    }

    // ViewHolder for sent image messages
    static class SentImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvTime;

        public SentImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(MessageModel message) {
            if (ivImage != null) {
                if (message.getImages() != null && !message.getImages().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(message.getImages().get(0))
                            .placeholder(R.drawable.default_profile_picture)
                            .error(R.drawable.default_profile_picture)
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.default_profile_picture);
                }
            }
            if (tvTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = message.getCreatedAt() != null ?
                        message.getCreatedAt().toDate() : new Date();
                tvTime.setText(sdf.format(date));
            }
        }
    }

    // ViewHolder for received image messages
    static class ReceivedImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvTime;

        public ReceivedImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(MessageModel message) {
            if (ivImage != null) {
                if (message.getImages() != null && !message.getImages().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(message.getImages().get(0))
                            .placeholder(R.drawable.default_profile_picture)
                            .error(R.drawable.default_profile_picture)
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.default_profile_picture);
                }
            }
            if (tvTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = message.getCreatedAt() != null ?
                        message.getCreatedAt().toDate() : new Date();
                tvTime.setText(sdf.format(date));
            }
        }
    }
}