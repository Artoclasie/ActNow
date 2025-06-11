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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessageModel> messages;
    private String currentUserId;
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_SENT_IMAGE = 3;
    private static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    public MessageAdapter() {
        this.messages = new ArrayList<>();
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent, parent, false);
                break;
            case VIEW_TYPE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received, parent, false);
                break;
            case VIEW_TYPE_SENT_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_sent_image, parent, false);
                break;
            case VIEW_TYPE_RECEIVED_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message_received_image, parent, false);
                break;
            default:
                throw new IllegalArgumentException("Invalid view type");
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messages.get(position);
        holder.bind(message);
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

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessage;
        private TextView tvTime;
        private ImageView ivImage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivImage = itemView.findViewById(R.id.iv_image);
        }

        public void bind(MessageModel message) {
            if ("image".equals(message.getType())) {
                if (tvMessage != null) tvMessage.setVisibility(View.GONE);
                if (ivImage != null) ivImage.setVisibility(View.VISIBLE);
                if (message.getImages() != null && !message.getImages().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(message.getImages().get(0))
                            .placeholder(R.drawable.default_profile_picture)
                            .error(R.drawable.default_profile_picture)
                            .into(ivImage);
                } else {
                    ivImage.setImageResource(R.drawable.default_profile_picture);
                }
            } else {
                if (tvMessage != null) tvMessage.setVisibility(View.VISIBLE);
                if (ivImage != null) ivImage.setVisibility(View.GONE);
                if (tvMessage != null) tvMessage.setText(message.getContent());
            }

            // Format the timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = message.getCreatedAt() != null ?
                    message.getCreatedAt().toDate() : new Date();
            if (tvTime != null) tvTime.setText(sdf.format(date));
        }
    }
}