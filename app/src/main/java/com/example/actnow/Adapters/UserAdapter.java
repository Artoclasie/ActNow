package com.example.actnow.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<UserProfile> users;
    private Context context;
    private final OnUserClickListener clickListener;

    public interface OnUserClickListener {
        void onUserClick(UserProfile user);
    }

    public UserAdapter(List<UserProfile> users, OnUserClickListener clickListener) {
        this.users = users;
        this.clickListener = clickListener;
        this.context = null; // Will be set in onCreateViewHolder
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_organizer, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserProfile user = users.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.tvCity.setText(user.getCity());

        // Load profile image
        String avatarUrl = user.getProfileImage();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .into(holder.ivAvatar);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.default_profile_picture);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onUserClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvCity;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCity = itemView.findViewById(R.id.tv_organizer_role);
        }
    }
}