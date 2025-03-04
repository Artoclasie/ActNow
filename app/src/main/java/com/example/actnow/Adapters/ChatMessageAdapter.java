package com.example.actnow.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.actnow.Models.ChatModel;
import com.example.actnow.R;

import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT = 0;
    private static final int TYPE_RECEIVED = 1;

    private List<ChatModel> messageList;
    private String currentUserId;
    private String otherUserId;

    public ChatMessageAdapter(List<ChatModel> messageList, String currentUserId, String otherUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        this.otherUserId = otherUserId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sent_message, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_received_message, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatModel message = messageList.get(position);

        if (message == null) {
            Log.e("ChatMessageAdapter", "Message at position " + position + " is null");
            return;
        }

        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        } else {
            Log.e("ChatMessageAdapter", "Unknown ViewHolder type");
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel message = messageList.get(position);
        return message.getSenderId().equals(currentUserId) ? TYPE_SENT : TYPE_RECEIVED;
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder for sent messages
    public class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_text);
            tvTime = itemView.findViewById(R.id.tv_message_time);

            // Проверка на null
            if (tvMessage == null || tvTime == null) {
                throw new IllegalStateException("One or more views are missing in the layout.");
            }
        }

        public void bind(ChatModel message) {
            tvMessage.setText(message.getMessageText());
            tvTime.setText(message.getMessageTime());
        }
    }

    // ViewHolder for received messages
    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message_text);
            tvTime = itemView.findViewById(R.id.tv_message_time);

            // Проверка на null
            if (tvMessage == null || tvTime == null) {
                throw new IllegalStateException("One or more views are missing in the layout.");
            }
        }

        public void bind(ChatModel message) {
            tvMessage.setText(message.getMessageText());
            tvTime.setText(message.getMessageTime());
        }
    }
}