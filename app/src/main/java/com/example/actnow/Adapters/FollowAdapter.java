package com.example.actnow.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.Fragments.AnotherProfileFragment;
import com.example.actnow.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.FollowViewHolder> {

    private List<Map<String, Object>> followList;
    private Context context;

    public FollowAdapter(List<Map<String, Object>> followList, Context context) {
        this.followList = followList != null ? followList : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public FollowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow, parent, false);
        return new FollowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowViewHolder holder, int position) {
        Map<String, Object> user = followList.get(position);
        if (user == null) {
            Log.e("FollowAdapter", "User data is null at position: " + position);
            return;
        }

        String username = (String) user.get("username");
        String city = (String) user.get("city");
        String avatarUrl = (String) user.get("avatarUrl");
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            avatarUrl = (String) user.get("profileImageUrl");
        }

        holder.tvOrganizerName.setText(username != null ? username : "Unknown");
        holder.tvOrganizerCity.setText(city != null ? city : "Город не указан");

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .into(holder.ivOrganizerPhoto);
        } else {
            holder.ivOrganizerPhoto.setImageResource(R.drawable.default_profile_picture);
        }

        // Получаем userId из данных
        String userId = (String) user.get("userId");
        Log.d("FollowAdapter", "Binding user at position " + position + ": " + username + ", userId: " + userId);

        // Устанавливаем OnClickListener на всю карточку
        holder.cvOrganizerPhoto.setOnClickListener(v -> {
            if (userId != null && context instanceof FragmentActivity) {
                Log.d("FollowAdapter", "Navigating to AnotherProfileFragment for userId: " + userId);
                FragmentActivity activity = (FragmentActivity) context;
                AnotherProfileFragment fragment = AnotherProfileFragment.newInstance(userId);
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                Log.e("FollowAdapter", "userId is null or context is not FragmentActivity");
            }
        });

        // Также можно добавить клик на имя и город, если нужно
        holder.tvOrganizerName.setOnClickListener(v -> holder.cvOrganizerPhoto.performClick());
        holder.tvOrganizerCity.setOnClickListener(v -> holder.cvOrganizerPhoto.performClick());
    }

    @Override
    public int getItemCount() {
        return followList != null ? followList.size() : 0;
    }

    public void updateData(List<Map<String, Object>> newList) {
        if (newList == null) {
            this.followList = new ArrayList<>();
        } else {
            this.followList = new ArrayList<>(newList);
        }
        Log.d("FollowAdapter", "Updated with " + this.followList.size() + " items");
        notifyDataSetChanged();
    }

    static class FollowViewHolder extends RecyclerView.ViewHolder {
        CardView cvOrganizerPhoto;
        ImageView ivOrganizerPhoto;
        TextView tvOrganizerName, tvOrganizerCity;

        public FollowViewHolder(@NonNull View itemView) {
            super(itemView);
            cvOrganizerPhoto = itemView.findViewById(R.id.cv_organizer_photo);
            ivOrganizerPhoto = itemView.findViewById(R.id.iv_organizer_photo);
            tvOrganizerName = itemView.findViewById(R.id.tv_organizer_name);
            tvOrganizerCity = itemView.findViewById(R.id.tv_organizer_city);
        }
    }
}