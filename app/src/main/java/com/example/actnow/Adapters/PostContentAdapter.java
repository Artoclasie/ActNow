package com.example.actnow.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.actnow.AnotherProfileActivity;
import com.example.actnow.Fragments.AnotherProfileFragment;
import com.example.actnow.Fragments.HomeFragment;
import com.example.actnow.Fragments.PostDetailFragment;
import com.example.actnow.Fragments.ProfileFragment;
import com.example.actnow.Models.PostReferenceModel;
import com.example.actnow.PostDetailActivity;
import com.example.actnow.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PostContentAdapter extends RecyclerView.Adapter<PostContentAdapter.ViewHolder> {

    private ArrayList<PostReferenceModel> postList;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final Context context;
    private ProfileFragment profileFragment; // Ссылка на фрагмент (может быть null)

    // Конструктор с двумя параметрами
    public PostContentAdapter(ArrayList<PostReferenceModel> postReferenceModels, Context context) {
        if (postReferenceModels != null) {
            Collections.reverse(postReferenceModels);
        }
        this.postList = postReferenceModels != null ? postReferenceModels : new ArrayList<>();
        this.context = context;
        this.profileFragment = null; // Без связи с ProfileFragment
    }

    // Конструктор с тремя параметрами
    public PostContentAdapter(ArrayList<PostReferenceModel> postReferenceModels, Context context, ProfileFragment profileFragment) {
        if (postReferenceModels != null) {
            Collections.reverse(postReferenceModels);
        }
        this.postList = postReferenceModels != null ? postReferenceModels : new ArrayList<>();
        this.context = context;
        this.profileFragment = profileFragment; // Ссылка на ProfileFragment
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.postcontent, parent, false);
        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (postList == null || postList.size() <= position) {
            Log.e("PostContentAdapter", "Invalid position: " + position + ", postList size: " + (postList != null ? postList.size() : 0));
            return;
        }

        PostReferenceModel post = postList.get(position);

        if (post.getUid().equals(mAuth.getCurrentUser().getUid())) {
            holder.imb_post_delete.setVisibility(View.VISIBLE);
            holder.imb_post_delete.setOnClickListener(v -> deletePost(post));
        } else {
            holder.imb_post_delete.setVisibility(View.GONE);
        }

        // Отладочные логи
        Log.d("PostContentAdapter", "Position: " + position + ", Post: " + post.getTitle());
        if (holder.tv_post_uname == null) Log.e("PostContentAdapter", "tv_post_uname is null");
        if (holder.tv_post_city == null) Log.e("PostContentAdapter", "tv_post_city is null");
        if (holder.tv_post_title == null) Log.e("PostContentAdapter", "tv_post_title is null");
        if (holder.tv_post_location == null) Log.e("PostContentAdapter", "tv_post_location is null");
        if (holder.tv_date == null) Log.e("PostContentAdapter", "tv_date is null");
        if (holder.tv_post_date == null) Log.e("PostContentAdapter", "tv_post_date is null");
        if (holder.tv_post_likes == null) Log.e("PostContentAdapter", "tv_post_likes is null");

        holder.tv_post_uname.setText(post.getUsername());
        holder.tv_post_city.setText(post.getCity());
        holder.tv_post_title.setText(post.getTitle());
        holder.tv_post_location.setText(post.getLocation());
        holder.tv_post_likes.setText(String.valueOf(post.getLikesCount()));
        holder.tv_date.setText(post.getDate());

        String timestampStr = post.getTimestamp();
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            Date date = inputFormat.parse(timestampStr);
            String formattedDate = outputFormat.format(date);
            holder.tv_post_date.setText(formattedDate);
        } catch (ParseException e) {
            holder.tv_post_date.setText(timestampStr);
            Log.e("PostContentAdapter", "ParseException for timestamp: " + timestampStr, e);
        }

        // Загрузка изображений
        if (post.getProfileImageUrl() != null) {
            Glide.with(context).load(post.getProfileImageUrl()).into(holder.imv_post_uid);
        }
        if (post.getImageUrl() != null) {
            Glide.with(context).load(post.getImageUrl()).into(holder.imv_post_content);
        }

        String currentUserId = mAuth.getCurrentUser().getUid();
        ArrayList<String> likes = post.getLikes() != null ? post.getLikes() : new ArrayList<>();

        if (likes.contains(currentUserId)) {
            holder.imb_post_likes.setImageResource(R.drawable.like_filled);
        } else {
            holder.imb_post_likes.setImageResource(R.drawable.like);
        }

        holder.imb_post_likes.setOnClickListener(v -> {
            if (likes.contains(currentUserId)) {
                likes.remove(currentUserId);
                holder.imb_post_likes.setImageResource(R.drawable.like);
            } else {
                likes.add(currentUserId);
                holder.imb_post_likes.setImageResource(R.drawable.like_filled);
            }

            FirebaseFirestore.getInstance().collection("Posts")
                    .document(post.getId())
                    .update("likes", likes, "likesCount", likes.size())
                    .addOnSuccessListener(aVoid -> {
                        post.setLikes(likes);
                        post.setLikesCount(likes.size());
                        holder.tv_post_likes.setText(String.valueOf(likes.size()));
                    })
                    .addOnFailureListener(e -> Log.e("PostAdapter", "Error updating likes", e));
        });

        holder.imb_read.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("postId", post.getId());
            intent.putExtra("title", post.getTitle());
            intent.putExtra("location", post.getLocation());
            intent.putExtra("date", post.getDate());
            intent.putExtra("content", post.getContent());
            intent.putExtra("imageUrl", post.getImageUrl());
            intent.putExtra("profileImageUrl", post.getProfileImageUrl());
            intent.putExtra("username", post.getUsername());
            intent.putExtra("uid", post.getUid());
            intent.putExtra("city", post.getCity());
            context.startActivity(intent);
        });

        holder.imv_post_uid.setOnClickListener(v -> {
            openAnotherProfile(post.getUid());
        });
    }

    @Override
    public int getItemCount() {
        return postList != null ? postList.size() : 0;
    }

    private void deletePost(PostReferenceModel post) {
        if (postList == null || postList.isEmpty()) {
            Log.e("PostContentAdapter", "Post list is null or empty");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();
        String postId = post.getId();

        // Проверяем, что пост принадлежит текущему пользователю
        if (!post.getUid().equals(currentUserId)) {
            Log.w("PostContentAdapter", "Cannot delete post: Post does not belong to the current user");
            return;
        }

        db.collection("Posts")
                .document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostContentAdapter", "Post deleted from Firestore: " + postId);

                    // Находим индекс поста в списке по postId
                    int index = -1;
                    for (int i = 0; i < postList.size(); i++) {
                        if (postList.get(i).getId().equals(postId)) {
                            index = i;
                            break;
                        }
                    }

                    // Если пост найден, удаляем его
                    if (index != -1) {
                        postList.remove(index);
                        notifyItemRemoved(index);
                        notifyItemRangeChanged(index, postList.size());
                    } else {
                        Log.w("PostContentAdapter", "Post not found in list: " + postId);
                    }

                    // Удаление из Realtime Database
                    FirebaseDatabase.getInstance().getReference("Posts")
                            .child(postId)
                            .removeValue()
                            .addOnSuccessListener(aVoid1 -> {
                                Log.d("PostContentAdapter", "Post deleted from Realtime Database: " + postId);

                                // Обновление счётчика постов
                                db.collection("Profiles").document(currentUserId)
                                        .get()
                                        .addOnSuccessListener(documentSnapshot -> {
                                            if (documentSnapshot.exists()) {
                                                Long currentPostCount = documentSnapshot.getLong("countPost");
                                                if (currentPostCount != null && currentPostCount > 0) {
                                                    db.collection("Profiles").document(currentUserId)
                                                            .update("countPost", currentPostCount - 1)
                                                            .addOnSuccessListener(aVoid2 -> {
                                                                Log.d("PostContentAdapter", "Post count updated to: " + (currentPostCount - 1));
                                                                // Уведомляем фрагмент об обновлении (если он существует)
                                                                if (profileFragment != null) {
                                                                    profileFragment.loadUserPosts(); // Перезагрузка постов
                                                                }
                                                            })
                                                            .addOnFailureListener(e -> Log.e("PostAdapter", "Error updating post count", e));
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("PostAdapter", "Error fetching user document", e));
                            })
                            .addOnFailureListener(e -> Log.e("PostAdapter", "Error deleting from Realtime Database: " + e.getMessage(), e));
                })
                .addOnFailureListener(e -> Log.e("PostAdapter", "Error deleting post: " + e.getMessage(), e));
    }

    public void updateListDirectly(List<PostReferenceModel> newList) {
        postList.clear();
        postList.addAll(newList);
        notifyDataSetChanged(); // Простое обновление всего списка
    }

    private void openAnotherProfile(String uid) {
        Log.d("PostContentAdapter", "Opening profile for uid: " + uid);
        Intent intent = new Intent(context, AnotherProfileActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("fromChatsFragment", false);
        if (context instanceof AppCompatActivity) {
            ((AppCompatActivity) context).startActivity(intent);
        } else {
            context.startActivity(intent); // Попытка запуска напрямую
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imv_post_uid, imv_post_content;
        TextView tv_post_uname, tv_post_likes, tv_post_city, tv_post_date,
                tv_post_title, tv_post_location, tv_date;
        ImageButton imb_post_likes, imb_post_delete, imb_read;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_date = itemView.findViewById(R.id.tv_date);
            imv_post_uid = itemView.findViewById(R.id.imv_post_uid);
            imv_post_content = itemView.findViewById(R.id.imv_post_content);
            tv_post_uname = itemView.findViewById(R.id.tv_post_uname);
            tv_post_likes = itemView.findViewById(R.id.tv_post_likes);
            imb_post_likes = itemView.findViewById(R.id.imb_post_likes);
            tv_post_city = itemView.findViewById(R.id.tv_post_city);
            tv_post_date = itemView.findViewById(R.id.tv_post_date);
            imb_post_delete = itemView.findViewById(R.id.imb_post_delete);
            tv_post_title = itemView.findViewById(R.id.tv_post_title);
            tv_post_location = itemView.findViewById(R.id.tv_post_location);
            imb_read = itemView.findViewById(R.id.imb_read);

            // Добавляем отладочные логи
            if (tv_date == null) Log.e("ViewHolder", "tv_date is null");
            if (imv_post_uid == null) Log.e("ViewHolder", "imv_post_uid is null");
            if (imv_post_content == null) Log.e("ViewHolder", "imv_post_content is null");
            if (tv_post_uname == null) Log.e("ViewHolder", "tv_post_uname is null");
            if (tv_post_likes == null) Log.e("ViewHolder", "tv_post_likes is null");
            if (imb_post_likes == null) Log.e("ViewHolder", "imb_post_likes is null");
            if (tv_post_city == null) Log.e("ViewHolder", "tv_post_city is null");
            if (tv_post_date == null) Log.e("ViewHolder", "tv_post_date is null");
            if (imb_post_delete == null) Log.e("ViewHolder", "imb_post_delete is null");
            if (tv_post_title == null) Log.e("ViewHolder", "tv_post_title is null");
            if (tv_post_location == null) Log.e("ViewHolder", "tv_post_location is null");
            if (imb_read == null) Log.e("ViewHolder", "imb_read is null");
        }
    }

    // Внутренний класс PostDiffCallback
    public static class PostDiffCallback extends DiffUtil.Callback {
        private final List<PostReferenceModel> oldList;
        private final List<PostReferenceModel> newList;

        public PostDiffCallback(List<PostReferenceModel> oldList, List<PostReferenceModel> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
}