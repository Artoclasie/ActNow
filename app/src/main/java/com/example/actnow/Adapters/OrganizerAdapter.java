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
import com.example.actnow.Models.UserProfile;
import com.example.actnow.R;
import java.util.List;

public class OrganizerAdapter extends RecyclerView.Adapter<OrganizerAdapter.OrganizerViewHolder> {
    private static final String TAG = "OrganizerAdapter";
    private final List<UserProfile> organizers;
    private Context context;
    private final OnOrganizerClickListener clickListener;

    public interface OnOrganizerClickListener {
        void onOrganizerClick(UserProfile user);
    }

    public OrganizerAdapter(List<UserProfile> organizers, OnOrganizerClickListener clickListener) {
        this.organizers = organizers;
        this.clickListener = clickListener;
    }

    public OrganizerAdapter(List<UserProfile> organizers) {
        this.organizers = organizers;
        this.clickListener = null;
    }

    @NonNull
    @Override
    public OrganizerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_organizers, parent, false);
        return new OrganizerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizerViewHolder holder, int position) {
        UserProfile organizer = organizers.get(position);

        // Log data for debugging
        Log.d(TAG, "Binding organizer at position " + position + ": userId=" + organizer.getUserId() +
                ", name=" + organizer.getName() + ", city=" + organizer.getCity() +
                ", avatarUrl=" + organizer.getProfileImage());

        if (holder.tvOrganizerName == null) {
            Log.e(TAG, "tvOrganizerName is null in OrganizerViewHolder");
            return;
        }
        if (holder.tvOrganizerCity == null) {
            Log.e(TAG, "tvOrganizerCity is null in OrganizerViewHolder");
            return;
        }
        if (holder.ivOrganizerPhoto == null) {
            Log.e(TAG, "ivOrganizerPhoto is null in OrganizerViewHolder");
            return;
        }

        holder.tvOrganizerName.setText(organizer.getName() != null ? organizer.getName() : "Без имени");
        holder.tvOrganizerCity.setText(organizer.getCity() != null ? organizer.getCity() : "Не указан город");

        String avatarUrl = organizer.getProfileImage();
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_profile_picture)
                    .error(R.drawable.default_profile_picture)
                    .circleCrop()
                    .into(holder.ivOrganizerPhoto);
        } else {
            Log.w(TAG, "No avatarUrl for userId=" + organizer.getUserId());
            holder.ivOrganizerPhoto.setImageResource(R.drawable.default_profile_picture);
        }

        if (clickListener != null) {
            holder.itemView.setOnClickListener(v -> clickListener.onOrganizerClick(organizer));
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return organizers.size();
    }

    static class OrganizerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOrganizerPhoto;
        TextView tvOrganizerName;
        TextView tvOrganizerCity;

        OrganizerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrganizerPhoto = itemView.findViewById(R.id.iv_organizer_photo);
            tvOrganizerName = itemView.findViewById(R.id.tv_organizer_name);
            tvOrganizerCity = itemView.findViewById(R.id.tv_organizer_city);
            Log.d(TAG, "OrganizerViewHolder initialized - ivOrganizerPhoto: " + (ivOrganizerPhoto != null) +
                    ", tvOrganizerName: " + (tvOrganizerName != null) +
                    ", tvOrganizerCity: " + (tvOrganizerCity != null));
        }
    }
}