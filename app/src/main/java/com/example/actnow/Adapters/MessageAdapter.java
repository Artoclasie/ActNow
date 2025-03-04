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
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<ChatModel> messageList;
    private String currentUserId;

    public MessageAdapter(List<ChatModel> messageList) {
        this.messageList = messageList;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("MessageAdapter", "Current user ID: " + currentUserId);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatModel message = messageList.get(position);
        Log.d("MessageAdapter", "Message: " + message.getMessageText() + ", Sender: " + message.getSenderId());

        holder.tvMessageText.setText(message.getMessageText());

        if (message.getSenderId().equals(currentUserId)) {
            holder.tvMessageText.setBackgroundResource(R.drawable.profiles);
        } else {
            holder.tvMessageText.setBackgroundResource(R.drawable.default_profile_picture);
        }
    }

    @Override
    public int getItemCount() {
        Log.d("MessageAdapter", "Total messages: " + messageList.size());
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
        }
    }
}